package com.hwj.mall.order.controller;

import java.util.Arrays;
import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hwj.mall.order.entity.MallOrderEntity;
import com.hwj.mall.order.service.MallOrderService;
import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.R;



/**
 * 
 *
 * @author hwj
 * @email huangwenjun@mail.com
 * @date 2021-03-23 17:48:45
 */
@RestController
@RequestMapping("order/mallorder")
public class MallOrderController {
    @Autowired
    private MallOrderService mallOrderService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = mallOrderService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Integer id){
		MallOrderEntity mallOrder = mallOrderService.getById(id);

        return R.ok().put("mallOrder", mallOrder);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody MallOrderEntity mallOrder){
		mallOrderService.save(mallOrder);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody MallOrderEntity mallOrder){
		mallOrderService.updateById(mallOrder);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids){
		mallOrderService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
