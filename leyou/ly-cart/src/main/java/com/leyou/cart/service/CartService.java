package com.leyou.cart.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.cart.entity.Cart;
import com.leyou.cart.inteceptors.UserTokenInterceptor;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UserTokenInterceptor tokenInterceptor;

    /**
    *@Description 添加到购物车
    *@Param 购物车
    *@Return void
    *@Author Jiangtao
    *@Date 2020/3/1
    *@Time 11:44
    */
    public void addCart(Cart cart) {
        //我们设计redis的hash结构组成购物车 而这里的cart实例通常只包括一个sku和数量
        // userid-->skudid-->cart实例
        UserInfo userInfo = tokenInterceptor.getUserInfo();

        String key = String.valueOf(userInfo.getId());

        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(key);

        //hKey其实就是sku的id
        String hKey = String.valueOf(cart.getSkuId());

        //判断用户是否把此sku加入过redis购物车，如果有，说明加入过
        if (ops.hasKey(hKey)) {
            //如果有,先取出购物车
            String cartJson = ops.get(hKey);

            //将Redis中的购物车Json数据转换为对象
            Cart storeCart = JsonUtils.nativeRead(cartJson, new TypeReference<Cart>() {});

            //更改先前购物车中的数量 原先的sku数量加上新增的sku数量
            storeCart.setNum(storeCart.getNum() + cart.getNum());

            //TODO ,除了数量之外其他也应该更新

            //更新数量后要重新保存
            ops.put(hKey, JsonUtils.toString(storeCart));

        } else {//没有加入
            ops.put(hKey, JsonUtils.toString(cart));
        }

    }

    /**
    *@Description 查询购物车
    *@Param null
    *@Return List<Cart>
    *@Author Jiangtao
    *@Date 2020/3/1
    *@Time 11:54
    */
    public List<Cart> queryCarts() {

        UserInfo userInfo = tokenInterceptor.getUserInfo();

        String key = String.valueOf(userInfo.getId());
        //先判断，这个用户是否曾加入数据到购物车
        if (redisTemplate.hasKey(key)) {

            BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(key);

            //获取到当前用户的所有购物车json对象
            return ops.values()//List<String>
                    .stream()//stream流进行流式运算Stream<String>
                    .map(value -> {
                        return JsonUtils.nativeRead(value, new TypeReference<Cart>() {
                        });//Stream<Cart>
                    }).collect(Collectors.toList());//List<Cart>
        } else {
            throw new LyException(ExceptionEnum.CART_IS_NULL);
        }
    }
    
    /**
    *@Description 
    *@Param 
    *@Return 
    *@Author Jiangtao
    *@Date 2020/3/1
    *@Time 11:55
    */
    public void modifyCartNum(Long id, Integer num) {

        UserInfo userInfo = tokenInterceptor.getUserInfo();

        String key = String.valueOf(userInfo.getId());
        //直接根据用户的Id获取redis的操作对象
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(key);

        //hKey就是skuId
        String hKey = String.valueOf(id);

        //判断要修改的sku商品是否在数据库中
        if (ops.hasKey(hKey)) {//存在修改数量

            Cart cart = JsonUtils.nativeRead(ops.get(hKey), new TypeReference<Cart>() {
            });

            cart.setNum(num);

            ops.put(hKey, JsonUtils.toString(cart));
        } else { //商品已经不在，所以抛出异常
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }

    }
    /**
    *@Description 
    *@Param 
    *@Return 
    *@Author Jiangtao
    *@Date 2020/3/1
    *@Time 11:55
    */
    public void mergeCart(List<Cart> carts) {
        carts.forEach(this::addCart);
    }
}
