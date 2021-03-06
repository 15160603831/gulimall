package com.hwj.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hwj.common.utils.PageUtils;
import com.hwj.mall.product.entity.PmsCategoryEntity;
import com.hwj.mall.product.vo.Catalog2Vo;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author hwj
 * @email huangwenjun@mail.com
 * @date 2021-03-23 17:29:10
 */
public interface PmsCategoryService extends IService<PmsCategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<PmsCategoryEntity> listWithTree();

    /**
     * 删除商品分类
     *
     * @param catIds
     */
    void removeMenuByIds(List<Long> catIds);

    /**
     * 找到catelogId的完整路径；
     * [父/子/孙]
     * @param catelogId
     * @return
     */
    Long[] findCatelogPath(Long catelogId);

    List<PmsCategoryEntity> getLevel1Catagories();

    Map<String, List<Catalog2Vo>> getCatalogJson();
}

