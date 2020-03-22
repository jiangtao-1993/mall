package com.leyou.order.service;

import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.clients.ItemClient;
import com.leyou.item.dto.SkuDTO;
import com.leyou.order.dto.CartDTO;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.entity.Order;
import com.leyou.order.entity.OrderDetail;
import com.leyou.order.entity.OrderLogistics;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.inteceptors.UserTokenInterceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderLogisticsMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.utils.PayHelper;
import com.leyou.user.clients.UserClient;
import com.leyou.user.dto.AddressDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderService {

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private ItemClient itemClient;

    @Autowired
    private UserTokenInterceptor userTokenInterceptor;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private UserClient userClient;
    @Autowired
    private OrderLogisticsMapper orderLogisticsMapper;

    @Transactional
    public Long createOrder(OrderDTO orderDTO) {

        //下单之前要先看是否有可用的库存

        //从拦截器中获取解析到的用户的信息
        UserInfo userInfo = userTokenInterceptor.getUserInfo();

        //创建生成orderId，雪花算法
        long orderId = idWorker.nextId();

        //实际购买商品的skuId和num组成的对象集合
        List<CartDTO> carts = orderDTO.getCarts();

        //cartsMap key就是sku的id，value就是当前sku所购买的数量
        Map<Long, Integer> cartsMap = carts.stream()
                .collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum));

        //跨服务查询item服务，根据sku的id集合查询sku的集合信息
        List<SkuDTO> skuDTOS = this.itemClient.querySkuByIds(new ArrayList<>(cartsMap.keySet()));

        //订单详情的集合
        List<OrderDetail> orderDetails = new ArrayList<>();

        //总价
        long totalFee = 0;
        //遍历sku集合，获取到每个sku，然后取出价格*数量获取到小计，把小计累加就得到最终的商品总价
        for (SkuDTO skuDTO : skuDTOS) {

            Integer num = cartsMap.get(skuDTO.getId());
            //单价*数量
            totalFee += skuDTO.getPrice() * num;

            OrderDetail orderDetail = BeanHelper.copyProperties(skuDTO, OrderDetail.class);
            orderDetail.setOrderId(orderId);
            orderDetail.setId(null);//id一定要置空，如果不再置空，自动增长失效
            orderDetail.setSkuId(skuDTO.getId());
            orderDetail.setNum(num);
            //sku可以有多张图片，只取第一张
            orderDetail.setImage(StringUtils.substringBefore(skuDTO.getImages(), ","));

            orderDetail.setCreateTime(new Date());
            orderDetail.setUpdateTime(orderDetail.getCreateTime());

            orderDetails.add(orderDetail);
        }


        Order order = new Order();
        order.setOrderId(orderId);
        order.setTotalFee(totalFee);//总价单价*数量
        order.setActualFee(totalFee);//实际支付价格totalFee-优惠+运费
        //TODO 校验paymentType要么，1，要么2，除此之外报错
        order.setPaymentType(orderDTO.getPaymentType());
        order.setPostFee(0L);//包邮，邮费为0
        order.setUserId(userInfo.getId());//取自于用户拦截器
        order.setStatus(OrderStatusEnum.INIT.value());//订单状态默认1


        //保存订单信息
        int count = this.orderMapper.insertSelective(order);

        if (count != 1) {
            throw new LyException(ExceptionEnum.DATA_SAVE_ERROR);
        }


        //保存订单详情信息，订单详情，基于sku生成
        count = this.orderDetailMapper.insertList(orderDetails);

        if (count != carts.size()) {
            throw new LyException(ExceptionEnum.DATA_SAVE_ERROR);
        }

        //首先要根据用户id，以及addressId，去user服务中查询地址的详细信息
        AddressDTO addressDTO = this.userClient.queryAddressById(userInfo.getId(), orderDTO.getAddressId());

        //基于查询到的详细信息，转换为物流信息
        OrderLogistics orderLogistics = BeanHelper.copyProperties(addressDTO, OrderLogistics.class);
        orderLogistics.setOrderId(orderId);

        //保存物流信息
        count = this.orderLogisticsMapper.insertSelective(orderLogistics);

        if (1 != count) {
            throw new LyException(ExceptionEnum.DATA_SAVE_ERROR);
        }

        //调用item服务进行库存的删减
        this.itemClient.minusStock(cartsMap);

        return orderId;
    }

    public Order queryOrderById(Long orderId) {
        Order order = this.orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new LyException(ExceptionEnum.DATA_NOT_FOUND);
        }
        return order;
    }

    @Autowired
    private PayHelper payHelper;

    @Autowired
    private StringRedisTemplate redisTemplate;


    public String generatePayUrl(Long orderId) {

        String key = String.valueOf(orderId);

        //如果redis中有对应的key，并且剩余有效时间超过30s，则直接返回
        if (redisTemplate.hasKey(key) && redisTemplate.getExpire(key, TimeUnit.SECONDS) > 30) {
            return redisTemplate.opsForValue().get(key);
        }

        //生成真实的支付地址，totalPay要写实际支付的金额，desc表示实际购买的商品名称，1个实际名称+其他等

        //根据订单id查询对应的订单详情，取出每一个购买的商品，
        String url = this.payHelper.createOrder(orderId, 1L, "旺仔牛奶");

        //支付链接有效期2小时
        redisTemplate.opsForValue().set(key, url, 2, TimeUnit.HOURS);
        return url;
    }

    @Transactional
    public void handleNotify(Map<String, String> result) {
        // 1 签名校验
        try {
            payHelper.isValidSign(result);
        } catch (Exception e) {
            log.error("【微信回调】微信签名有误！, result: {}", result, e);
            throw new LyException(ExceptionEnum.INVALID_NOTIFY_SIGN, e);
        }
        // 2、业务校验
        payHelper.checkResultCode(result);

        // 3 校验金额数据，对账
        String totalFeeStr = result.get("total_fee");
        String tradeNo = result.get("out_trade_no");
        if (StringUtils.isEmpty(totalFeeStr) || StringUtils.isEmpty(tradeNo)) {
            // 回调参数中必须包含订单编号和订单金额
            throw new LyException(ExceptionEnum.INVALID_NOTIFY_PARAM);
        }
        // 3.1 获取结果中的金额
        long totalFee = Long.valueOf(totalFeeStr);
        // 3.2 获取订单
        Long orderId = Long.valueOf(tradeNo);
        //根据订单编号，查询订单
        Order order = orderMapper.selectByPrimaryKey(orderId);
        // 3.3.判断订单的状态，保证幂等
        if (!order.getStatus().equals(OrderStatusEnum.INIT.value())) {
            // 订单已经支付，返回成功
            return;
        }
        // 3.4.判断金额是否一致
        if (totalFee != /*order.getActualPay()*/ 1) {
            // 金额不符
            throw new LyException(ExceptionEnum.INVALID_NOTIFY_PARAM);
        }

        // 4 修改订单状态
        Order orderStatus = new Order();
        //227修改订单状态为PAY_UP
        orderStatus.setStatus(OrderStatusEnum.PAY_UP.value());
        orderStatus.setOrderId(orderId);
        orderStatus.setPayTime(new Date());
        int count = orderMapper.updateByPrimaryKeySelective(orderStatus);
        if (count != 1) {
            log.error("【微信回调】更新订单状态失败，订单id：{}", orderId);
            throw new LyException(ExceptionEnum.DATA_MODIFY_ERROR);
        }
        log.info("【微信回调】, 订单支付成功! 订单编号:{}", orderId);
    }

    public Integer queryOrderStateById(Long orderId) {
        Order order = this.orderMapper.selectByPrimaryKey(orderId);

        if (null == order) {
            throw new LyException(ExceptionEnum.DATA_NOT_FOUND);
        }
        return order.getStatus();
    }

    @Transactional
    public void clearOrder() {
        //查询订单，超时的订单，当前时间-创建时间>24小时

        Order record = new Order();
        record.setStatus(OrderStatusEnum.INIT.value());

        //查询所有的未支付的订单
        List<Order> orders = this.orderMapper.select(record);

        Map<Long, Integer> skuMap = new HashMap<>();
        orders.forEach(order -> {
            //如果创建时间+24小时比当前时间还早，则过期了
            if (new DateTime(order.getCreateTime()).plusHours(24).isBeforeNow()) {
                order.setStatus(OrderStatusEnum.CLOSED.value());//设置订单状态为关闭
                order.setCloseTime(new Date());//设置关闭时间
                //关闭失效订单
                this.orderMapper.updateByPrimaryKeySelective(order);

                //加入库存增加操作，统计有哪些订单的哪些商品要做库存添加
                //根据当前订单，查询订单详情，获取到商品的skuId以及num数量
                OrderDetail orderDetailRecord = new OrderDetail();
                orderDetailRecord.setOrderId(order.getOrderId());
                //根据订单id查询订单详情
                List<OrderDetail> orderDetails = this.orderDetailMapper.select(orderDetailRecord);
                //统计库存，因为设计多个订单，所以一定会有重复的商品，
                orderDetails.forEach(orderDetail -> {
                    Long key = orderDetail.getSkuId();
                    Integer num = orderDetail.getNum();
                    if (skuMap.containsKey(key)) {//如果这个商品已经在sku中添加过则需要做数量的累加
                        skuMap.put(key, skuMap.get(key) + num);
                    } else {//没有添加过，则直接添加
                        skuMap.put(orderDetail.getSkuId(), num);
                    }
                });
            }
        });

        //如果当前扫描没有符合的条件的则，不添加
        if (!CollectionUtils.isEmpty(skuMap)){
            //当统计完成后添加库存，
            this.itemClient.plusStock(skuMap);
        }

    }
}
