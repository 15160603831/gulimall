package com.hwj.mall.product.service.impl;

import com.hwj.mall.product.entity.PmsAttrEntity;
import com.hwj.mall.product.entity.PmsProductAttrValueEntity;
import com.hwj.mall.product.entity.PmsSpuImagesEntity;
import com.hwj.mall.product.entity.PmsSpuInfoDescEntity;
import com.hwj.mall.product.service.PmsAttrService;
import com.hwj.mall.product.service.PmsSpuImagesService;
import com.hwj.mall.product.service.PmsSpuInfoDescService;
import com.hwj.mall.product.vo.BaseAttrs;
import com.hwj.mall.product.vo.SpuSaveVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.Query;

import com.hwj.mall.product.dao.PmsSpuInfoDao;
import com.hwj.mall.product.entity.PmsSpuInfoEntity;
import com.hwj.mall.product.service.PmsSpuInfoService;
import org.springframework.transaction.annotation.Transactional;


@Transactional(rollbackFor = Exception.class)
@Service("pmsSpuInfoService")
public class PmsSpuInfoServiceImpl extends ServiceImpl<PmsSpuInfoDao, PmsSpuInfoEntity> implements PmsSpuInfoService {

    @Autowired
    PmsSpuInfoDescService pmsSpuInfoDescService;
    @Autowired
    PmsSpuImagesService pmsSpuImagesService;
    @Autowired
    PmsAttrService pmsAttrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PmsSpuInfoEntity> page = this.page(
                new Query<PmsSpuInfoEntity>().getPage(params),
                new QueryWrapper<PmsSpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveInfo(SpuSaveVO vo) {
        //1.保存spu基本信息  pms_spu_info
        PmsSpuInfoEntity infoEntity = new PmsSpuInfoEntity();
        BeanUtils.copyProperties(vo, infoEntity);
        infoEntity.setCreateTime(new Date());
        infoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(infoEntity);

        //2.保存spu的描述图片 pms_spu_info_desc
        List<String> decript = vo.getDecript();
        PmsSpuInfoDescEntity descEntity = new PmsSpuInfoDescEntity();
        descEntity.setSpuId(infoEntity.getId());
        descEntity.setDecript(String.join(",", decript));
        pmsSpuInfoDescService.saveSpuInfoDesc(descEntity);

        //3.保存spu图片集 pms_spu_images
        List<String> images = vo.getImages();
        pmsSpuImagesService.saveImage(infoEntity.getId(), images);

        //4.保存spu 的规格参数：pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<PmsProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
            PmsProductAttrValueEntity attrValueEntity = new PmsProductAttrValueEntity();
            attrValueEntity.setAttrId(attr.getAttrId());
            attrValueEntity.setAttrName("");
            attrValueEntity.setAttrValue(attr.getAttrValues());
            attrValueEntity.setQuickShow(attr.getShowDesc());
            attrValueEntity.setSpuId(infoEntity.getId());
            return attrValueEntity;
        }).collect(Collectors.toList());
        List<Long> attrs = collect.stream().map(PmsProductAttrValueEntity::getAttrId).distinct().collect(Collectors.toList());
        List<PmsAttrEntity> attrs1 = pmsAttrService.getAttrs(attrs);
        collect.stream().forEach(a->{
            attrs1.forEach(b->{
                if (a.getAttrId().equals(b.getAttrId())){
                    a.setAttrName(b.getAttrName());
                }
            });
        });
//        pmsAttrService.

        //保存sku积分信息 `mall-coupon`.`sms_spu_bounds`
        //5.保存spu对应的sku信息
        //5.1、sku基本信息： pms_sku_info
        //5.2、sku图片信息：pms_sku_images
        //5.3、sku销售属性：pms_sku_sale_attr_value
        //5.4、sku优惠信息：


    }


    @Override
    public void saveBaseSpuInfo(PmsSpuInfoEntity pmsSpuInfoEntity) {
        baseMapper.insert(pmsSpuInfoEntity);
    }


}