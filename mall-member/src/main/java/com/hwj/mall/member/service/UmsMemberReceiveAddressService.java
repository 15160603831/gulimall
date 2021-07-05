package com.hwj.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hwj.common.utils.PageUtils;
import com.hwj.mall.member.entity.UmsMemberReceiveAddressEntity;

import java.util.List;
import java.util.Map;

/**
 * 会员收货地址
 *
 * @author hwj
 * @email huangwenjun@mail.com
 * @date 2021-03-23 17:44:25
 */
public interface UmsMemberReceiveAddressService extends IService<UmsMemberReceiveAddressEntity> {


    PageUtils queryPage(Map<String, Object> params);

    List<UmsMemberReceiveAddressEntity> getAddress(Long memberId);
}

