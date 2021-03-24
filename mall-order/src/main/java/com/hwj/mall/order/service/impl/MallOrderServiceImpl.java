package com.hwj.mall.order.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.Query;

import com.hwj.mall.order.dao.MallOrderDao;
import com.hwj.mall.order.entity.MallOrderEntity;
import com.hwj.mall.order.service.MallOrderService;


@Service("mallOrderService")
public class MallOrderServiceImpl extends ServiceImpl<MallOrderDao, MallOrderEntity> implements MallOrderService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MallOrderEntity> page = this.page(
                new Query<MallOrderEntity>().getPage(params),
                new QueryWrapper<MallOrderEntity>()
        );

        return new PageUtils(page);
    }

}