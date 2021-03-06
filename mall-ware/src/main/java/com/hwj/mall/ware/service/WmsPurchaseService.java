package com.hwj.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hwj.common.utils.PageUtils;
import com.hwj.mall.ware.entity.WmsPurchaseEntity;
import com.hwj.mall.ware.vo.MergeVO;
import com.hwj.mall.ware.vo.PurchaseDoneVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author hwj
 * @email huangwenjun@mail.com
 * @date 2021-03-23 17:50:33
 */
public interface WmsPurchaseService extends IService<WmsPurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceived(Map<String, Object> params);

    void mergePurchase(MergeVO mergeVO);

    void received(List<Long> ids);

    void done(PurchaseDoneVo vo);
}

