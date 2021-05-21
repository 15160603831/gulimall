package com.hwj.mall.coupon.service.impl;

import com.hwj.common.to.MemberPrice;
import com.hwj.common.to.SkuReductionTo;
import com.hwj.mall.coupon.entity.SmsMemberPriceEntity;
import com.hwj.mall.coupon.entity.SmsSkuLadderEntity;
import com.hwj.mall.coupon.service.SmsMemberPriceService;
import com.hwj.mall.coupon.service.SmsSkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.Query;

import com.hwj.mall.coupon.dao.SmsSkuFullReductionDao;
import com.hwj.mall.coupon.entity.SmsSkuFullReductionEntity;
import com.hwj.mall.coupon.service.SmsSkuFullReductionService;
import org.springframework.transaction.annotation.Transactional;

@Transactional(rollbackFor = Exception.class)
@Service("smsSkuFullReductionService")
public class SmsSkuFullReductionServiceImpl extends ServiceImpl<SmsSkuFullReductionDao, SmsSkuFullReductionEntity> implements SmsSkuFullReductionService {

    @Autowired
    SmsSkuLadderService skuLadderService;

    @Autowired
    SmsMemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SmsSkuFullReductionEntity> page = this.page(
                new Query<SmsSkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SmsSkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 增加满减优惠
     *
     * @param reductionTo
     */
    @Override
    public void saveSkuReduction(SkuReductionTo reductionTo) {
        //sms_sku_ladder
        SmsSkuLadderEntity skuLadderEntity = new SmsSkuLadderEntity();
        skuLadderEntity.setSkuId(reductionTo.getSkuId());
        skuLadderEntity.setFullCount(reductionTo.getFullCount());
        skuLadderEntity.setDiscount(reductionTo.getDiscount());
        skuLadderEntity.setAddOther(reductionTo.getCountStatus());
        if (reductionTo.getFullCount() > 0) {
            skuLadderService.save(skuLadderEntity);
        }


        //2、sms_sku_full_reduction
        SmsSkuFullReductionEntity reductionEntity = new SmsSkuFullReductionEntity();
        BeanUtils.copyProperties(reductionTo, reductionEntity);
        if (reductionEntity.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
            this.save(reductionEntity);
        }
        //3、sms_member_price
        List<MemberPrice> memberPrice = reductionTo.getMemberPrice();

        List<SmsMemberPriceEntity> collect = memberPrice.stream().map(item -> {
            SmsMemberPriceEntity priceEntity = new SmsMemberPriceEntity();
            priceEntity.setSkuId(reductionTo.getSkuId());
            priceEntity.setMemberLevelId(item.getId());
            priceEntity.setMemberLevelName(item.getName());
            priceEntity.setMemberPrice(item.getPrice());
            priceEntity.setAddOther(1);
            return priceEntity;
        }).filter(item -> {
            return item.getMemberPrice().compareTo(new BigDecimal("0")) == 1;
        }).collect(Collectors.toList());

        memberPriceService.saveBatch(collect);
    }

}