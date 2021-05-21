package com.hwj.mall.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwj.common.utils.Query;
import com.hwj.mall.member.dao.UmsIntegrationChangeHistoryDao;
import com.hwj.mall.member.entity.UmsIntegrationChangeHistoryEntity;
import com.hwj.mall.member.service.UmsIntegrationChangeHistoryService;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.hwj.common.utils.PageUtils;




@Service("umsIntegrationChangeHistoryService")
public class UmsIntegrationChangeHistoryServiceImpl extends ServiceImpl<UmsIntegrationChangeHistoryDao, UmsIntegrationChangeHistoryEntity> implements UmsIntegrationChangeHistoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<UmsIntegrationChangeHistoryEntity> page = this.page(
                new Query<UmsIntegrationChangeHistoryEntity>().getPage(params),
                new QueryWrapper<UmsIntegrationChangeHistoryEntity>()
        );

        return new PageUtils(page);
    }

}