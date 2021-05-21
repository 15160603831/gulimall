package com.hwj.mall.member.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.Query;
import com.hwj.mall.member.dao.UmsGrowthChangeHistoryDao;
import com.hwj.mall.member.entity.UmsGrowthChangeHistoryEntity;
import com.hwj.mall.member.service.UmsGrowthChangeHistoryService;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("umsGrowthChangeHistoryService")
public class UmsGrowthChangeHistoryServiceImpl extends ServiceImpl<UmsGrowthChangeHistoryDao, UmsGrowthChangeHistoryEntity> implements UmsGrowthChangeHistoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<UmsGrowthChangeHistoryEntity> page = this.page(
                new Query<UmsGrowthChangeHistoryEntity>().getPage(params),
                new QueryWrapper<UmsGrowthChangeHistoryEntity>()
        );

        return new PageUtils(page);
    }

}