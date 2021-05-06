package com.hwj.mall.product.service.impl;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.Query;

import com.hwj.mall.product.dao.PmsCategoryDao;
import com.hwj.mall.product.entity.PmsCategoryEntity;
import com.hwj.mall.product.service.PmsCategoryService;


@Service("pmsCategoryService")
public class PmsCategoryServiceImpl extends ServiceImpl<PmsCategoryDao, PmsCategoryEntity> implements PmsCategoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PmsCategoryEntity> page = this.page(
                new Query<PmsCategoryEntity>().getPage(params),
                new QueryWrapper<PmsCategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<PmsCategoryEntity> listWithTree() {
        //查出所有分类
        List<PmsCategoryEntity> pmsCategoryEntities = baseMapper.selectList(null);
        //组装成树形结构
        //2.1找到所有一级分类
        List<PmsCategoryEntity> levelMenu = pmsCategoryEntities.stream()
                .filter(t -> t.getParentCid()==0)
                .map((menu) -> {
                    menu.setChildren(getChildren(menu,pmsCategoryEntities));
                    return menu;
                })
                .sorted(Comparator.comparingInt(sort -> (sort.getSort() == null ? 0 : sort.getSort())))
                .collect(Collectors.toList());
        return levelMenu;
    }

    /**
     * 删除商品分类
     *
     * @param catIds
     */
    @Override
    public void removeMenuByIds(List<Long> catIds) {
         baseMapper.deleteBatchIds(catIds);
    }

    /**
     * 递归查询所有菜单下的子菜单
     *
     * @param root 菜单
     * @param all  子菜单
     * @return 所有菜单
     */
    private List<PmsCategoryEntity> getChildren(PmsCategoryEntity root, List<PmsCategoryEntity> all) {
        List<PmsCategoryEntity> pmsCategoryEntities = all.stream()
                .filter(pmsCategoryEntity -> {
                    return pmsCategoryEntity.getParentCid() == root.getCatId();
                })
                .map(pmsCategoryEntity -> {
                    pmsCategoryEntity.setChildren(getChildren(pmsCategoryEntity, all));
                    return pmsCategoryEntity;
                })
                .sorted(Comparator.comparingInt(sort -> (sort.getSort() == null ? 0 : sort.getSort())))
                .collect(Collectors.toList());
        return pmsCategoryEntities;
    }


}