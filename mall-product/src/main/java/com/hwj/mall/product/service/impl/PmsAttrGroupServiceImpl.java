package com.hwj.mall.product.service.impl;


import com.hwj.mall.product.entity.PmsAttrEntity;
import com.hwj.mall.product.service.PmsAttrService;
import com.hwj.mall.product.vo.AttrGroupWithAttrsVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
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

import com.hwj.mall.product.dao.PmsAttrGroupDao;
import com.hwj.mall.product.entity.PmsAttrGroupEntity;
import com.hwj.mall.product.service.PmsAttrGroupService;


@Service("pmsAttrGroupService")
public class PmsAttrGroupServiceImpl extends ServiceImpl<PmsAttrGroupDao, PmsAttrGroupEntity> implements PmsAttrGroupService {
    @Autowired
    PmsAttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PmsAttrGroupEntity> page = this.page(
                new Query<PmsAttrGroupEntity>().getPage(params),
                new QueryWrapper<PmsAttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long cateLogId) {
        String key = (String) params.get("key");
        QueryWrapper<PmsAttrGroupEntity> queryWrapper = new QueryWrapper<>();

        if (!StringUtils.isEmpty(key)){
            queryWrapper.and(obj->{
                obj.eq("attr_group_id",key).or().like("attr_group_name",key);
            });
        }

        if (cateLogId == 0) {
            IPage<PmsAttrGroupEntity> page = this.page(new Query<PmsAttrGroupEntity>().getPage(params), queryWrapper);
            return new PageUtils(page);
        } else {
            queryWrapper.eq("catelog_id", cateLogId);
            IPage<PmsAttrGroupEntity> page = this.page(new Query<PmsAttrGroupEntity>().getPage(params),queryWrapper);
            return new PageUtils(page);
        }

    }
    /**
     * 根据分类id查出所有的分组以及这些组里面的属性
     * @param catelogId
     * @return
     */
    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        //com.atguigu.gulimall.product.vo
        //1、查询分组信息
        List<PmsAttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<PmsAttrGroupEntity>().eq("catelog_id", catelogId));

        //2、查询所有属性
        List<AttrGroupWithAttrsVo> collect = attrGroupEntities.stream().map(group -> {
            AttrGroupWithAttrsVo attrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(group,attrsVo);
            List<PmsAttrEntity> attrs = attrService.getRelationAttr(attrsVo.getAttrGroupId());
            attrsVo.setAttrs(attrs);
            return attrsVo;
        }).collect(Collectors.toList());

        return collect;


    }

    @Override
    public PmsAttrGroupEntity getById(Long attrGroupId) {
        PmsAttrGroupEntity pmsAttrGroupEntity = baseMapper.selectById(attrGroupId);
        return pmsAttrGroupEntity;
    }


}