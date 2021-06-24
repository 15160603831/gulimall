package com.hwj.mall.product.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hwj.mall.product.dao.PmsAttrGroupDao;
import com.hwj.mall.product.entity.PmsBrandEntity;
import com.hwj.mall.product.service.PmsBrandService;


import com.hwj.mall.product.vo.SpuItemAttrGroupVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.UUID;


@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class PmsAttrAttrgroupRelationControllerTest {

    @Autowired
    private PmsBrandService pmsBrandService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    PmsAttrGroupDao pmsAttrGroupDao;

    @Test
    public void redisson() {
        System.out.println(redissonClient);
    }


    @Test
    public void save() {
//        PmsBrandEntity entity = new PmsBrandEntity();
//        entity.setDescript("九牧哈哈哈");
//        entity.setLogo("https://jm-retail.obs.cn-south-1.myhuaweicloud.com/mall/20210315/pic/1615797007332_2021年春季成教报名简章.jpg");
//        entity.setName("2021年春季成教报名简章");
//        entity.setShowStatus(1);
//        boolean save = pmsBrandService.save(entity);

        redisTemplate.opsForValue().set("hello", "dd" + UUID.randomUUID().toString());

        String hello = redisTemplate.opsForValue().get("hello");
        System.out.println("保存的数据：" + hello);
    }

    @Test
    public void select() {

        QueryWrapper<PmsBrandEntity> wrapper = new QueryWrapper();
        wrapper.eq("brand_id", "2");

        List<PmsBrandEntity> list = pmsBrandService.list(wrapper);
        list.forEach(t -> {
            System.out.println(t);
        });
    }


    @Test
    public void test(){
        List<SpuItemAttrGroupVo> atrGroupWithAttrsBySpuId = pmsAttrGroupDao.getAtrGroupWithAttrsBySpuId(32L, 225L);
        System.out.println(atrGroupWithAttrsBySpuId);
    }
//

}