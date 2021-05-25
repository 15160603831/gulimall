package com.hwj.mall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.Query;
import com.hwj.mall.ware.dao.WmsPurchaseDetailDao;
import com.hwj.mall.ware.entity.WmsPurchaseDetailEntity;
import com.hwj.mall.ware.service.WmsPurchaseDetailService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;


@Service("wmsPurchaseDetailService")
public class WmsPurchaseDetailServiceImpl extends ServiceImpl<WmsPurchaseDetailDao, WmsPurchaseDetailEntity> implements WmsPurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<WmsPurchaseDetailEntity> wrapper = new QueryWrapper<>();
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            wrapper.eq("status", status);
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            wrapper.eq("ware_id", wareId);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(w -> {
                w.like("purchase_id", key).or().eq("sku_id", key);
            });
        }
        IPage<WmsPurchaseDetailEntity> page = this.page(
                new Query<WmsPurchaseDetailEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<WmsPurchaseDetailEntity> listDetailByPurchaseId(Long id) {
        List<WmsPurchaseDetailEntity> purchaseId = this.list(new QueryWrapper<WmsPurchaseDetailEntity>().eq("purchase_id", id));

        return purchaseId;
    }

}