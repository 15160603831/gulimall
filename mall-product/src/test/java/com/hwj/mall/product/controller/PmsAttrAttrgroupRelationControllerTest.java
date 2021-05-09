package com.hwj.mall.product.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hwj.mall.product.entity.PmsBrandEntity;
import com.hwj.mall.product.service.PmsBrandService;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;


@SpringBootTest
@RunWith(SpringRunner.class)
public class PmsAttrAttrgroupRelationControllerTest {

    @Autowired
    private PmsBrandService pmsBrandService;



    @Test
    public void save() {
        PmsBrandEntity entity = new PmsBrandEntity();
        entity.setDescript("九牧哈哈哈");
        entity.setLogo("https://jm-retail.obs.cn-south-1.myhuaweicloud.com/mall/20210315/pic/1615797007332_2021年春季成教报名简章.jpg");
        entity.setName("2021年春季成教报名简章");
        entity.setShowStatus(1);
        boolean save = pmsBrandService.save(entity);
    }

    @Test
    public void select() {

        QueryWrapper<PmsBrandEntity> wrapper=new QueryWrapper();
        wrapper.eq("brand_id","2");

        List<PmsBrandEntity> list = pmsBrandService.list(wrapper);
        list.forEach(t->{
            System.out.println(t);
        });
    }


}