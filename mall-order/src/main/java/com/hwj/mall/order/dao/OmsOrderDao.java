package com.hwj.mall.order.dao;

import com.hwj.mall.order.entity.OmsOrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author hwj
 * @email huangwenjun@mail.com
 * @date 2021-03-23 17:48:45
 */
@Mapper
public interface OmsOrderDao extends BaseMapper<OmsOrderEntity> {
	
}
