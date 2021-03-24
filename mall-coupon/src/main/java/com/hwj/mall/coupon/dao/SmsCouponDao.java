package com.hwj.mall.coupon.dao;

import com.hwj.mall.coupon.entity.SmsCouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author hwj
 * @email huangwenjun@mail.com
 * @date 2021-03-23 17:34:45
 */
@Mapper
public interface SmsCouponDao extends BaseMapper<SmsCouponEntity> {
	
}
