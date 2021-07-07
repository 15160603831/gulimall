package com.hwj.mall.ware.service.impl;

import com.hwj.common.exception.NoStockException;
import com.hwj.common.utils.R;
import com.hwj.mall.ware.feign.ProductFeignService;
import com.hwj.mall.ware.vo.LockStockResult;
import com.hwj.mall.ware.vo.OrderItemVo;
import com.hwj.mall.ware.vo.SkuHasStockVO;
import com.hwj.mall.ware.vo.WareSkuLockVo;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.Query;

import com.hwj.mall.ware.dao.WmsWareSkuDao;
import com.hwj.mall.ware.entity.WmsWareSkuEntity;
import com.hwj.mall.ware.service.WmsWareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("wmsWareSkuService")
public class WmsWareSkuServiceImpl extends ServiceImpl<WmsWareSkuDao, WmsWareSkuEntity> implements WmsWareSkuService {
    @Autowired
    WmsWareSkuDao wareSkuDao;

    @Autowired
    ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<WmsWareSkuEntity> wrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            wrapper.eq("sku_id", skuId);
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            wrapper.eq("ware_id", wareId);
        }
        IPage<WmsWareSkuEntity> page = this.page(
                new Query<WmsWareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //1、判断如果还没有这个库存记录新增
        List<WmsWareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WmsWareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (entities == null || entities.size() == 0) {
            WmsWareSkuEntity skuEntity = new WmsWareSkuEntity();
            skuEntity.setSkuId(skuId);
            skuEntity.setStock(skuNum);
            skuEntity.setWareId(wareId);
            skuEntity.setStockLocked(0);
            //TODO 远程查询sku的名字，如果失败，整个事务无需回滚
            //1、自己catch异常
            //TODO 还可以用什么办法让异常出现以后不回滚？高级
            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");

                if (info.getCode() == 0) {
                    skuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {

            }


            wareSkuDao.insert(skuEntity);
        } else {
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }
    }

    /**
     * 查询sku是否有库存
     */
    @Override
    public List<SkuHasStockVO> getSkuHasStock(List<Long> skuIds) {
        List<SkuHasStockVO> collect = skuIds.stream().map(sku -> {
            SkuHasStockVO vo = new SkuHasStockVO();
            Long count = baseMapper.getSkuStock(sku);
            vo.setHasStock(count == null ? false : count > 0);
            vo.setSkuId(sku);
            return vo;
        }).collect(Collectors.toList());
        return collect;
    }

    /**
     * 锁订单库存
     * (rollbackFor = NoStockException.class)
     *
     * @param vo
     * @return
     */
    @Transactional
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {
        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
            //skuId
            Long skuId = item.getSkuId();
            skuWareHasStock.setNum(item.getCount());
            skuWareHasStock.setSkuId(skuId);
            ////找出所有库存大于商品数的仓库
            List<Long> wareIds = wareSkuDao.listWareIdHasStock(skuId);
            skuWareHasStock.setWareIds(wareIds);
            return skuWareHasStock;
        }).collect(Collectors.toList());

        //锁定库存
        for (SkuWareHasStock hasStock : collect) {
            boolean lock = true;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareIds();
            //如果没有满足条件的仓库，抛出异常
            if (wareIds == null && wareIds.size() == 0) {
                throw new NoStockException(skuId);
            } else {
                for (Long wareId : wareIds) {
                    Long count = wareSkuDao.lockWareSku(skuId, hasStock.getNum(), wareId);
                    if (count == 0) {
                        lock = false;
                    } else {

                    }
                }
                if (lock == false) {
                    //当前商品所有仓库都没有锁住
                    throw new NoStockException(skuId);
                }
            }
        }

        //锁定成功
        return true;
    }

    @Data
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareIds;
    }

}