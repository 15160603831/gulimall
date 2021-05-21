package com.hwj.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hwj.common.utils.PageUtils;
import com.hwj.mall.member.entity.UmsIntegrationChangeHistoryEntity;

import java.util.Map;

/**
 * 积分变化历史记录
 *
 * @author hwj
 * @email huangwenjun@mail.com
 * @date 2021-03-23 17:34:43
 */
public interface UmsIntegrationChangeHistoryService extends IService<UmsIntegrationChangeHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

