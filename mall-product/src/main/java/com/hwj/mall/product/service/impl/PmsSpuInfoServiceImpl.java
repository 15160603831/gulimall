package com.hwj.mall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.hwj.common.constant.ProductConstant;
import com.hwj.common.to.SkuHasStockVO;
import com.hwj.common.to.SkuReductionTo;
import com.hwj.common.to.SpuBoundTo;
import com.hwj.common.to.es.SkuEsModel;
import com.hwj.common.utils.R;
import com.hwj.mall.product.entity.*;
import com.hwj.mall.product.feign.CouponFeignServer;
import com.hwj.mall.product.feign.SearchFeignServer;
import com.hwj.mall.product.feign.WareFeignServer;
import com.hwj.mall.product.service.*;
import com.hwj.mall.product.vo.Attr;
import com.hwj.mall.product.vo.BaseAttrs;
import com.hwj.mall.product.vo.Bounds;
import com.hwj.mall.product.vo.Images;
import com.hwj.mall.product.vo.Skus;
import com.hwj.mall.product.vo.SpuSaveVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.Query;

import com.hwj.mall.product.dao.PmsSpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


@Transactional(rollbackFor = Exception.class)
@Service("pmsSpuInfoService")
public class PmsSpuInfoServiceImpl extends ServiceImpl<PmsSpuInfoDao, PmsSpuInfoEntity> implements PmsSpuInfoService {

    @Autowired
    PmsSpuInfoDescService pmsSpuInfoDescService;
    @Autowired
    PmsSpuImagesService pmsSpuImagesService;
    @Autowired
    PmsAttrService pmsAttrService;
    @Autowired
    PmsProductAttrValueService attrValueService;
    @Autowired
    PmsSkuInfoService pmsSkuInfoService;
    @Autowired
    PmsSkuImagesService pmsSkuImagesService;
    @Autowired
    PmsSkuSaleAttrValueService pmsSkuSaleAttrValueService;
    @Autowired
    CouponFeignServer couponFeignService;
    @Autowired
    PmsBrandService brandService;
    @Autowired
    PmsCategoryService categoryService;
    @Autowired
    WareFeignServer wareFeignServer;
    @Autowired
    private SearchFeignServer searchFeignServer;


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
        collect.stream().forEach(a -> {
            attrs1.forEach(b -> {
                if (a.getAttrId().equals(b.getAttrId())) {
                    a.setAttrName(b.getAttrName());
                }
            });
        });
        attrValueService.saveProductAttr(collect);

        //保存sku积分信息 `mall-coupon`.`sms_spu_bounds`
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(infoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r.getCode() != 0) {
            log.error("远程保存spu积分信息失败");
        }

        //5.保存spu对应的sku信息
        List<Skus> skus = vo.getSkus();
        if (skus != null && skus.size() > 0) {
            skus.forEach(item -> {
                //5.1、sku基本信息： pms_sku_info
                String defaultImg = "";
                for (Images image : item.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        defaultImg = image.getImgUrl();
                    }
                }
                PmsSkuInfoEntity skuInfoEntity = new PmsSkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setBrandId(infoEntity.getBrandId());
                skuInfoEntity.setCatalogId(infoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(infoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                pmsSkuInfoService.saveSkuInfo(skuInfoEntity);

                //5.2、sku图片信息：pms_sku_images
                Long skuId = skuInfoEntity.getSkuId();
                List<PmsSkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
                    PmsSkuImagesEntity skuImagesEntity = new PmsSkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter(entity -> {
                    //返回true就是需要，false就是剔除
                    return !StringUtils.isEmpty(entity.getImgUrl());
                }).collect(Collectors.toList());
                pmsSkuImagesService.saveBatch(imagesEntities);

                //5.3、sku销售属性：pms_sku_sale_attr_value
                List<Attr> attr = item.getAttr();
                List<PmsSkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(a -> {
                    PmsSkuSaleAttrValueEntity attrValueEntity = new PmsSkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, attrValueEntity);
                    attrValueEntity.setSkuId(skuId);
                    return attrValueEntity;
                }).collect(Collectors.toList());
                pmsSkuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                //5.4、sku优惠信息：
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                System.out.println("优惠信息====" + JSON.toJSONString(skuReductionTo));
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0) {
                        log.error("远程保存sku优惠信息失败");
                    }
                }
            });
        }
    }


    @Override
    public void saveBaseSpuInfo(PmsSpuInfoEntity pmsSpuInfoEntity) {
        baseMapper.insert(pmsSpuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {

        QueryWrapper<PmsSpuInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(w -> {
                w.eq("id", key).or().like("spu_name", key);
            });
        }
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            queryWrapper.eq("publish_status", status);
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }
        String catalogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catalogId) && !"0".equalsIgnoreCase(catalogId)) {
            queryWrapper.eq("catalog_id", catalogId);
        }


        IPage<PmsSpuInfoEntity> page = this.page(
                new Query<PmsSpuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    /**
     * 商品上架
     *
     * @param spuId
     */
    @Override
    public void up(Long spuId) {
        List<PmsSkuInfoEntity> skus = pmsSkuInfoService.getSkuBySpuId(spuId);
        List<Long> skuIdList = skus.stream().map(PmsSkuInfoEntity::getSkuId).collect(Collectors.toList());


        //查询当前sku商品的可以被检索的规格属性
        List<PmsProductAttrValueEntity> baseAttrs = attrValueService.baseAttrlistForspu(spuId);
        List<Long> attrIds = baseAttrs.stream().map(PmsProductAttrValueEntity::getAttrId).collect(Collectors.toList());
        List<Long> searchIds = pmsAttrService.selectSearchAttrIds(attrIds);
        Set<Long> ids = new HashSet<>(searchIds);
        List<SkuEsModel.Attr> searchAttrs = baseAttrs.stream().filter(entity -> {
            return ids.contains(entity.getAttrId());
        }).map(entity -> {
            SkuEsModel.Attr attr = new SkuEsModel.Attr();
            BeanUtils.copyProperties(entity, attr);
            return attr;
        }).collect(Collectors.toList());

        Map<Long, Boolean> stockMap = null;
        try {
            //发送feign请求是否还有库存 hasStock
            List<SkuHasStockVO> skuHasStock = wareFeignServer.getSkuHasStock(skuIdList);
            stockMap = skuHasStock.stream().collect(Collectors.toMap(SkuHasStockVO::getSkuId, item -> item.getHasStock()));
        } catch (Exception e) {
            log.error("库存服务查询异常：原因{}", e);
        }

        //组装skues数据
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> skuProduct = skus.stream().map(skuItem -> {
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(skuItem, skuEsModel);
            skuEsModel.setSkuPrice(skuItem.getPrice());
            skuEsModel.setSkuImg(skuItem.getSkuDefaultImg());
            //TODO 2、热度评分。0
            skuEsModel.setHotScore(0L);
            //TODO 3、查询品牌和分类的名字信息
            PmsBrandEntity brandEntity = brandService.getById(skuItem.getBrandId());
            skuEsModel.setBrandName(brandEntity.getName());
            skuEsModel.setBrandImg(brandEntity.getLogo());
            PmsCategoryEntity categoryEntity = categoryService.getById(skuItem.getCatalogId());
            skuEsModel.setCatalogName(categoryEntity.getName());
            skuEsModel.setAttrs(searchAttrs);
            //设置库存信息
            if (finalStockMap == null) {
                skuEsModel.setHasStock(true);
            } else {
                skuEsModel.setHasStock(finalStockMap.get(skuItem.getSkuId()));

            }
            return skuEsModel;
        }).collect(Collectors.toList());
        //将数据发送给es
        R r = searchFeignServer.productStatusUp(skuProduct);
        if (r.getCode() == 0) {
            //成功
            //修改当前sku上下架状态
            baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        } else {
            //失败
            //
            log.error("商品远程es保存失败");
        }


    }


}