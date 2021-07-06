package com.hwj.mall.order.dao;

import com.hwj.mall.order.entity.MqMessageEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 
 * 
 * @author hwj
 * @email huangwenjun@gmail.com
 * @date 2021-07-06 10:13:09
 */
@Mapper
public interface MqMessageDao extends BaseMapper<MqMessageEntity> {
	
}
