package com.hwj.mall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.Query;
import com.hwj.common.utils.R;
import com.hwj.mall.product.dao.PmsSkuInfoDao;
import com.hwj.mall.product.entity.PmsSkuImagesEntity;
import com.hwj.mall.product.entity.PmsSkuInfoEntity;
import com.hwj.mall.product.entity.PmsSpuInfoDescEntity;
import com.hwj.mall.product.feign.SeckilFeignService;
import com.hwj.mall.product.service.PmsAttrGroupService;
import com.hwj.mall.product.service.PmsSkuImagesService;
import com.hwj.mall.product.service.PmsSkuInfoService;
import com.hwj.mall.product.service.PmsSkuSaleAttrValueService;
import com.hwj.mall.product.service.PmsSpuInfoDescService;
import com.hwj.mall.product.vo.SeckillSkuVo;
import com.hwj.mall.product.vo.SkuItemSaleAttrVo;
import com.hwj.mall.product.vo.SkuItemVo;
import com.hwj.mall.product.vo.SpuItemAttrGroupVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

@Transactional(rollbackFor = Exception.class)
@Service("pmsSkuInfoService")
public class PmsSkuInfoServiceImpl extends ServiceImpl<PmsSkuInfoDao, PmsSkuInfoEntity> implements PmsSkuInfoService {


    @Autowired
    private PmsSkuImagesService skuImagesService;
    @Autowired
    private PmsSpuInfoDescService spuInfoDescService;
    @Autowired
    private PmsAttrGroupService attrGroupService;
    @Autowired
    private PmsSkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    private ThreadPoolExecutor executor;
    @Autowired
    private SeckilFeignService seckilFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PmsSkuInfoEntity> page = this.page(
                new Query<PmsSkuInfoEntity>().getPage(params),
                new QueryWrapper<PmsSkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(PmsSkuInfoEntity skuInfoEntity) {
        baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<PmsSkuInfoEntity> queryWrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(w -> {
                w.eq("sku_id", key).or().like("sku_name", key);
            });
        }
        String catalogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catalogId) && !"0".equalsIgnoreCase(catalogId)) {
            queryWrapper.eq("catalog_id", catalogId);
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }
        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.ge("price", min);
        }
        String max = (String) params.get("max");
        if (!StringUtils.isEmpty(max)) {
            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                if (bigDecimal.compareTo(new BigDecimal("0")) == 1) {
                    queryWrapper.le("price", max);
                }
            } catch (Exception e) {
            }
        }
        IPage<PmsSkuInfoEntity> page = this.page(
                new Query<PmsSkuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    /**
     * 查询所有spuid对应的sku信息
     *
     * @param spuId
     * @return
     */
    @Override
    public List<PmsSkuInfoEntity> getSkuBySpuId(Long spuId) {
        QueryWrapper wrapper = new QueryWrapper<PmsSkuInfoEntity>().eq("spu_id", spuId);
        List list = baseMapper.selectList(wrapper);
        return list;
    }

    /**
     * 获取sku基本信息
     *
     * @param skuId sku
     */
    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo vo = new SkuItemVo();
        //线程池
        CompletableFuture<PmsSkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            //info
            PmsSkuInfoEntity info = getById(skuId);
            vo.setSkuInfoEntity(info);
            return info;
        }, executor);
        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync(res -> {
            //spu销售参数组合
            List<SkuItemSaleAttrVo> saleAttrs = skuSaleAttrValueService.getSaleAttrBySpuId(res.getSpuId());
            vo.setSaleAttrs(saleAttrs);
        }, executor);
        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync(res -> {
            //spu介绍
            PmsSpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(res.getSpuId());
            vo.setDesp(spuInfoDescEntity);
        }, executor);

        CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync(res -> {
            //规格参数
            List<SpuItemAttrGroupVo> groupAttrs = attrGroupService.getAtrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
            vo.setGroupAttrs(groupAttrs);
        }, executor);

        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            //image图片信息
            List<PmsSkuImagesEntity> images = skuImagesService.getImagesBySkuId(skuId);
            vo.setSkuImagesEntities(images);
        }, executor);

        CompletableFuture<Void> seckillSkuRedisTo = CompletableFuture.runAsync(() -> {
            //查询商品是否参加秒杀优惠
            R r = seckilFeignService.getSeckillSkuInfo(skuId);
            if (r.getCode() == 0) {
                SeckillSkuVo seckillSkuVo = JSON.parseObject(JSON.toJSONString(r.get("SeckillSkuRedisTo")), new TypeReference<SeckillSkuVo>() {
                });
                vo.setSeckillSkuVo(seckillSkuVo);
            }
        }, executor);


        //等待所有任务都完成
        CompletableFuture.allOf(saleAttrFuture, descFuture, baseAttrFuture, imageFuture, seckillSkuRedisTo).get();


        return vo;
    }
}