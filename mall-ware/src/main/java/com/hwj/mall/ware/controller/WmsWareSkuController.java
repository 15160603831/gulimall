package com.hwj.mall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


import com.hwj.common.exception.BizCodeEnum;
import com.hwj.common.exception.NoStockException;
import com.hwj.mall.ware.vo.LockStockResult;
import com.hwj.mall.ware.vo.SkuHasStockVO;
import com.hwj.mall.ware.vo.WareSkuLockVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hwj.mall.ware.entity.WmsWareSkuEntity;
import com.hwj.mall.ware.service.WmsWareSkuService;
import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.R;


/**
 * 商品库存
 *
 * @author hwj
 * @email huangwenjun@mail.com
 * @date 2021-03-23 17:50:33
 */
@RestController
@RequestMapping("/ware/waresku")
public class WmsWareSkuController {
    @Autowired
    private WmsWareSkuService wmsWareSkuService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = wmsWareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        WmsWareSkuEntity wmsWareSku = wmsWareSkuService.getById(id);

        return R.ok().put("wmsWareSku", wmsWareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody WmsWareSkuEntity wmsWareSku) {
        wmsWareSkuService.save(wmsWareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody WmsWareSkuEntity wmsWareSku) {
        wmsWareSkuService.updateById(wmsWareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        wmsWareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    /**
     * 查询sku是否有库存
     */
    @PostMapping("/has-stock")
    @ApiOperation(value = "查询sku是否有库存")
    public List<SkuHasStockVO> getSkuHasStock(@RequestBody List<Long> skuIdList) {
        List<SkuHasStockVO> vos = wmsWareSkuService.getSkuHasStock(skuIdList);
        return vos;
    }

    /**
     * 锁库存
     *
     * @param vo
     * @return
     */
    @PostMapping("order-lock-stock")
    @ApiOperation("订单锁库存")
    public R orderLockStock(@RequestBody WareSkuLockVo vo) {
        try {
            Boolean stockResults = wmsWareSkuService.orderLockStock(vo);
            return R.ok();
        } catch (NoStockException e) {
            return R.error(BizCodeEnum.NO_STOCK_EXCEPTION.getCode(), BizCodeEnum.NO_STOCK_EXCEPTION.getMsg());
        }
    }

}
