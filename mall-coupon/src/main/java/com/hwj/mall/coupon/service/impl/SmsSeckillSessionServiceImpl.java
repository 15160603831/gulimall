package com.hwj.mall.coupon.service.impl;

import com.hwj.mall.coupon.entity.SmsSeckillSkuRelationEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.Query;

import com.hwj.mall.coupon.dao.SmsSeckillSessionDao;
import com.hwj.mall.coupon.entity.SmsSeckillSessionEntity;
import com.hwj.mall.coupon.service.SmsSeckillSessionService;
import org.springframework.util.CollectionUtils;


@Service("smsSeckillSessionService")
public class SmsSeckillSessionServiceImpl extends ServiceImpl<SmsSeckillSessionDao, SmsSeckillSessionEntity> implements SmsSeckillSessionService {

    @Autowired
    SmsSeckillSkuRelationServiceImpl smsSeckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SmsSeckillSessionEntity> page = this.page(
                new Query<SmsSeckillSessionEntity>().getPage(params),
                new QueryWrapper<SmsSeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 查最近三天的活动
     *
     * @return
     */
    @Override
    public List<SmsSeckillSessionEntity> getLates3DaysSession() {

        //所有活动
        List<SmsSeckillSessionEntity> list = baseMapper.selectList(new QueryWrapper<SmsSeckillSessionEntity>()
                .lambda().between(SmsSeckillSessionEntity::getStartTime, this.getStartTime(), this.getEndTime()));
        // 所有商品
        if (!CollectionUtils.isEmpty(list)) {
            List<SmsSeckillSessionEntity> collect = list.stream().map(session -> {
                Long id = session.getId();
                List<SmsSeckillSkuRelationEntity> skuLists = smsSeckillSkuRelationService.list(new QueryWrapper<SmsSeckillSkuRelationEntity>()
                        .lambda().eq(SmsSeckillSkuRelationEntity::getPromotionSessionId, id));
                session.setRelationSkus(skuLists);
                return session;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    //当前天数的 00:00:00
    private String getStartTime() {
        LocalDate now = LocalDate.now();
        LocalDateTime time = now.atTime(LocalTime.MIN);
        String format = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return format;
    }

    //当前天数+2 23:59:59..
    private String getEndTime() {
        LocalDate now = LocalDate.now();
        LocalDateTime time = now.plusDays(2).atTime(LocalTime.MAX);
        String format = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return format;
    }


}