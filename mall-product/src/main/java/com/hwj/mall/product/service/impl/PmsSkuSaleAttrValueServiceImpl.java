package com.hwj.mall.product.service.impl;

import com.hwj.mall.product.vo.SkuItemSaleAttrVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.Query;

import com.hwj.mall.product.dao.PmsSkuSaleAttrValueDao;
import com.hwj.mall.product.entity.PmsSkuSaleAttrValueEntity;
import com.hwj.mall.product.service.PmsSkuSaleAttrValueService;
import org.springframework.transaction.annotation.Transactional;

@Transactional(rollbackFor = Exception.class)
@Service("pmsSkuSaleAttrValueService")
public class PmsSkuSaleAttrValueServiceImpl extends ServiceImpl<PmsSkuSaleAttrValueDao, PmsSkuSaleAttrValueEntity> implements PmsSkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PmsSkuSaleAttrValueEntity> page = this.page(
                new Query<PmsSkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<PmsSkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 获取spu下所有销售属性
     *
     * @param spuId
     * @return
     */
    @Override
    public List<SkuItemSaleAttrVo> getSaleAttrBySpuId(Long spuId) {
        PmsSkuSaleAttrValueDao dao = this.baseMapper;
        List<SkuItemSaleAttrVo> saleAttrVos = dao.getSaleAttrBySpuId( spuId);
        return saleAttrVos;
    }

}