package com.leyou.search.service;

import com.leyou.common.vo.PageResult;
import com.leyou.item.clients.ItemClient;
import com.leyou.item.dto.SpuDTO;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class IndexTest {

    @Autowired
    private ElasticsearchTemplate esTemplate;

    @Autowired
    private ItemClient itemClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private IndexService indexService;

    @Test
    public void testCreateIndex() {
        esTemplate.createIndex(Goods.class);//创建索引库
        esTemplate.putMapping(Goods.class);//添加映射
    }

    @Test
    public void addData() {

        int page = 1;
        while (true) {
            //死循环，分页查询spu
            PageResult<SpuDTO> spuDTOPageResult = this.itemClient.querySpuByPage(page, 50, true, null);
            page++;

            if (null == spuDTOPageResult) {
                break;
            }

            //从返回结果中取出spuDTO的集合
            List<SpuDTO> items = spuDTOPageResult.getItems();

            //把spuDTO转换为goods
            List<Goods> goodsList = items.stream()//Stream<SpuDTO>
                    .map(item -> indexService.buildGoods(item))//Stream<Goods>
                    .collect(Collectors.toList());//List<Goods>

            //批量保存goods对象
            this.goodsRepository.saveAll(goodsList);

        }
    }

}
