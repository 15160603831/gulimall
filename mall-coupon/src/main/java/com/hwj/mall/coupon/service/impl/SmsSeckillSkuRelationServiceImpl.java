package com.hwj.mall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.Query;

import com.hwj.mall.coupon.dao.SmsSeckillSkuRelationDao;
import com.hwj.mall.coupon.entity.SmsSeckillSkuRelationEntity;
import com.hwj.mall.coupon.service.SmsSeckillSkuRelationService;
import org.springframework.util.StringUtils;


@Service("smsSeckillSkuRelationService")
public class SmsSeckillSkuRelationServiceImpl extends ServiceImpl<SmsSeckillSkuRelationDao, SmsSeckillSkuRelationEntity> implements SmsSeckillSkuRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Object promotionSessionId = params.get("promotionSessionId");
        LambdaQueryWrapper<SmsSeckillSkuRelationEntity> wrapper = new QueryWrapper<SmsSeckillSkuRelationEntity>()
                .lambda()
                .eq(!StringUtils.isEmpty(promotionSessionId), SmsSeckillSkuRelationEntity::getPromotionSessionId, promotionSessionId);
        IPage<SmsSeckillSkuRelationEntity> page = this.page(
                new Query<SmsSeckillSkuRelationEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

}