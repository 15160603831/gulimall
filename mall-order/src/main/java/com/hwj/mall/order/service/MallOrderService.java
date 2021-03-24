package com.hwj.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hwj.common.utils.PageUtils;
import com.hwj.mall.order.entity.MallOrderEntity;

import java.util.Map;

/**
 * 
 *
 * @author hwj
 * @email huangwenjun@mail.com
 * @date 2021-03-23 17:48:45
 */
public interface MallOrderService extends IService<MallOrderEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

