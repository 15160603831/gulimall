package com.hwj.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hwj.common.utils.PageUtils;
import com.hwj.mall.product.entity.PmsAttrEntity;
import com.hwj.mall.product.vo.AttrResVO;
import com.hwj.mall.product.vo.AttrVO;

import java.util.Map;

/**
 * 商品属性
 *
 * @author hwj
 * @email huangwenjun@mail.com
 * @date 2021-03-23 17:29:10
 */
public interface PmsAttrService extends IService<PmsAttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVO pmsAttr);

    PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String type);

    AttrResVO getAttrInfo(Long attrId);

    void updateByAttr(AttrVO attrVO);
}

