package com.hwj.mall.ware.service.impl;

import com.hwj.common.constant.WareConstant;
import com.hwj.mall.ware.entity.WmsPurchaseDetailEntity;
import com.hwj.mall.ware.service.WmsPurchaseDetailService;
import com.hwj.mall.ware.vo.MergeVO;
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

import com.hwj.mall.ware.dao.WmsPurchaseDao;
import com.hwj.mall.ware.entity.WmsPurchaseEntity;
import com.hwj.mall.ware.service.WmsPurchaseService;
import org.springframework.transaction.annotation.Transactional;

@Transactional(rollbackFor = Exception.class)
@Service("wmsPurchaseService")
public class WmsPurchaseServiceImpl extends ServiceImpl<WmsPurchaseDao, WmsPurchaseEntity> implements WmsPurchaseService {

    @Autowired
    WmsPurchaseDetailService purchaseDetailService;
    @Autowired
    private WmsPurchaseDetailService detailService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WmsPurchaseEntity> page = this.page(
                new Query<WmsPurchaseEntity>().getPage(params),
                new QueryWrapper<WmsPurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceived(Map<String, Object> params) {
        IPage<WmsPurchaseEntity> page = this.page(
                new Query<WmsPurchaseEntity>().getPage(params),
                new QueryWrapper<WmsPurchaseEntity>().eq("status", 0).or().eq("status", 1)
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchase(MergeVO mergeVO) {
        Long purchaseId = mergeVO.getPurchaseId();
        if (purchaseId == null) {
            WmsPurchaseEntity wmsPurchaseEntity = new WmsPurchaseEntity();
            wmsPurchaseEntity.setStatus(0);
            wmsPurchaseEntity.setCreateTime(new Date());
            wmsPurchaseEntity.setUpdateTime(new Date());
            baseMapper.insert(wmsPurchaseEntity);
            purchaseId = wmsPurchaseEntity.getId();
        }
        List<Long> items = mergeVO.getItems();
        Long finalPurchaseId = purchaseId;
        List<WmsPurchaseDetailEntity> collect = items.stream().map(i -> {
            WmsPurchaseDetailEntity entity = new WmsPurchaseDetailEntity();
            entity.setId(i);
            entity.setPurchaseId(finalPurchaseId);
            entity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            return entity;
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(collect);
    }

    @Override
    public void received(List<Long> ids) {
        //确认当前采购单是已分配状态
        List<WmsPurchaseEntity> collect = ids.stream().map(t -> {
            WmsPurchaseEntity byId = this.getById(t);
            return byId;
        }).filter(item -> {
            if (item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() ||
                    item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()) {
                return true;
            }
            return false;
        }).map(item->{
          item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
          return item;
        }).collect(Collectors.toList());

        //修改采购单
        this.updateBatchById(collect);

        //改变采购项的状态
        collect.stream().forEach(item->{
            List<WmsPurchaseDetailEntity> entities = detailService.listDetailByPurchaseId(item.getId());
            List<WmsPurchaseDetailEntity> detailEntities = entities.stream().map(entity -> {
                WmsPurchaseDetailEntity entity1 = new WmsPurchaseDetailEntity();
                entity1.setId(entity.getId());
                entity1.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                return entity1;
            }).collect(Collectors.toList());
            detailService.updateBatchById(detailEntities);
        });


    }

}