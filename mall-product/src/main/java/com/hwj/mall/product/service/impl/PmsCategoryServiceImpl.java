package com.hwj.mall.product.service.impl;

import com.hwj.mall.product.vo.Catalog2Vo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
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
                .filter(t -> t.getParentCid() == 0)
                .map((menu) -> {
                    menu.setChildren(getChildren(menu, pmsCategoryEntities));
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

    //[2,25,225]
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);

        Collections.reverse(parentPath);


        return parentPath.toArray(new Long[parentPath.size()]);
    }


    //225,25,2
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        //1、收集当前节点id
        paths.add(catelogId);
        PmsCategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;

    }

    /**
     * 查出所有1级分类
     *
     * @return
     */
    @Override
    public List<PmsCategoryEntity> getLevel1Catagories() {

        List<PmsCategoryEntity> parent_cid = baseMapper.selectList(new QueryWrapper<PmsCategoryEntity>().eq("parent_cid", 0));
        return parent_cid;
    }

    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        List<PmsCategoryEntity> level1Catagories = this.getLevel1Catagories();

        Map<String, List<Catalog2Vo>> collect = level1Catagories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //// 拿到每一个一级分类 然后查询他们的二级分类
            List<PmsCategoryEntity> entities = baseMapper.selectList(new QueryWrapper<PmsCategoryEntity>().eq("parent_cid", v.getCatId()));
            List<Catalog2Vo> catalog2Vos = null;
            if (entities != null) {
                catalog2Vos = entities.stream().map(l2 -> {
                    Catalog2Vo catalog2Vo = new Catalog2Vo(v.getCatId().toString(), l2.getCatId().toString(), l2.getName(), null);
                    // 找当前二级分类的三级分类
                    List<PmsCategoryEntity> entities1 = baseMapper.selectList(new QueryWrapper<PmsCategoryEntity>().eq("parent_cid", l2.getCatId()));
                    // 三级分类有数据的情况下
                    if (entities1 != null) {
//                            Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                        List<Catalog2Vo.Catalog3Vo> catalog3Vo = entities1.stream().map(l3 -> new Catalog2Vo.Catalog3Vo(l3.getCatId().toString(), l2.getCatId().toString(), l3.getName())).collect(Collectors.toList());
                        catalog2Vo.setCatalog3List(catalog3Vo);
                    }
                    return catalog2Vo;
                }).collect(Collectors.toList());
            }
            return catalog2Vos;
        }));
        return collect;
    }


}