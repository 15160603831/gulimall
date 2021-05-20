package com.hwj.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwj.common.constant.ProductConstant;
import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.Query;
import com.hwj.mall.product.dao.PmsAttrAttrgroupRelationDao;
import com.hwj.mall.product.dao.PmsAttrDao;
import com.hwj.mall.product.dao.PmsAttrGroupDao;
import com.hwj.mall.product.dao.PmsCategoryDao;
import com.hwj.mall.product.entity.PmsAttrAttrgroupRelationEntity;
import com.hwj.mall.product.entity.PmsAttrEntity;
import com.hwj.mall.product.entity.PmsAttrGroupEntity;
import com.hwj.mall.product.entity.PmsCategoryEntity;
import com.hwj.mall.product.service.PmsAttrService;
import com.hwj.mall.product.service.PmsCategoryService;
import com.hwj.mall.product.vo.AttrGroupRelationVo;
import com.hwj.mall.product.vo.AttrResVO;
import com.hwj.mall.product.vo.AttrVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional
@Service("pmsAttrService")
public class PmsAttrServiceImpl extends ServiceImpl<PmsAttrDao, PmsAttrEntity> implements PmsAttrService {

    @Autowired
    private PmsAttrAttrgroupRelationDao relationDao;
    @Autowired
    private PmsAttrGroupDao attrGroupDao;
    @Autowired
    private PmsCategoryDao categoryDao;
    @Autowired
    private PmsCategoryService pmsCategoryService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PmsAttrEntity> page = this.page(
                new Query<PmsAttrEntity>().getPage(params),
                new QueryWrapper<PmsAttrEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveAttr(AttrVO pmsAttr) {
        PmsAttrEntity pmsAttrEntity = new PmsAttrEntity();
        BeanUtils.copyProperties(pmsAttr, pmsAttrEntity);
        //保存数据
        this.save(pmsAttrEntity);
        //保存关联关系
        PmsAttrAttrgroupRelationEntity pmsAttrAttrgroupRelationEntity = new PmsAttrAttrgroupRelationEntity();
        pmsAttrAttrgroupRelationEntity.setAttrGroupId(pmsAttr.getAttrGroupId());
        pmsAttrAttrgroupRelationEntity.setAttrId(pmsAttrEntity.getAttrId());
        relationDao.insert(pmsAttrAttrgroupRelationEntity);

    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String type) {
        QueryWrapper<PmsAttrEntity> wrapper = new QueryWrapper<PmsAttrEntity>()
                .eq("attr_type", "base".equalsIgnoreCase(type) ? ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() : ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());

        if (catelogId != 0) {
            wrapper.eq("catelog_id", catelogId);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((w) -> {
                w.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<PmsAttrEntity> page = this.page(
                new Query<PmsAttrEntity>().getPage(params),
                wrapper
        );
        PageUtils pageUtils = new PageUtils(page);
        List<PmsAttrEntity> records = page.getRecords();
        List<AttrResVO> respVos = records.stream().map(attrEntity -> {
            AttrResVO attrResVO = new AttrResVO();
            BeanUtils.copyProperties(attrEntity, attrResVO);

            //1、设置分类和分组的名字
            if ("base".equalsIgnoreCase(type)) {
                PmsAttrAttrgroupRelationEntity attrId = relationDao.selectOne(new QueryWrapper<PmsAttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
                if (attrId != null && attrId.getAttrGroupId() != null) {
                    PmsAttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrId.getAttrGroupId());
                    attrResVO.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
            PmsCategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            if (categoryEntity != null) {
                attrResVO.setCatelogName(categoryEntity.getName());
            }
            return attrResVO;
        }).collect(Collectors.toList());

        pageUtils.setList(respVos);
        return pageUtils;
    }

    @Override
    public AttrResVO getAttrInfo(Long attrId) {
        AttrResVO attrResVO = new AttrResVO();
        PmsAttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity, attrResVO);
        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            //1、设置分组信息
            PmsAttrAttrgroupRelationEntity attr_id = relationDao.selectOne(new QueryWrapper<PmsAttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            if (attr_id != null) {
                attrResVO.setAttrGroupId(attr_id.getAttrGroupId());
                PmsAttrGroupEntity pmsAttrGroupEntity = attrGroupDao.selectById(attr_id.getAttrGroupId());
                if (pmsAttrGroupEntity != null) {
                    attrResVO.setGroupName(pmsAttrGroupEntity.getAttrGroupName());
                }
            }
        }

        //2、设置分类信息
        Long catelogId = attrEntity.getCatelogId();
        Long[] catelogPath = pmsCategoryService.findCatelogPath(catelogId);
        attrResVO.setCatelogPath(catelogPath);

        PmsCategoryEntity pmsCategoryEntity = categoryDao.selectById(catelogId);
        if (pmsCategoryEntity != null) {
            attrResVO.setCatelogName(pmsCategoryEntity.getName());
        }
        return attrResVO;
    }

    @Override
    public void updateByAttr(AttrVO attrVO) {
        PmsAttrEntity pmsAttrEntity = new PmsAttrEntity();
        BeanUtils.copyProperties(attrVO, pmsAttrEntity);
        this.updateById(pmsAttrEntity);
        PmsAttrAttrgroupRelationEntity pmsAttrAttrgroupRelationEntity = new PmsAttrAttrgroupRelationEntity();
        pmsAttrAttrgroupRelationEntity.setAttrGroupId(attrVO.getAttrGroupId());
        pmsAttrAttrgroupRelationEntity.setAttrId(attrVO.getAttrId());
        relationDao.update(pmsAttrAttrgroupRelationEntity, new UpdateWrapper<PmsAttrAttrgroupRelationEntity>().eq("attr_id", attrVO.getAttrId()));
    }

    /**
     * 根据分组id查找关联的所有基本属性
     *
     * @param attrgroupId
     * @return
     */
    @Override
    public List<PmsAttrEntity> getRelationAttr(Long attrgroupId) {
        List<PmsAttrAttrgroupRelationEntity> entities = relationDao.selectList(new QueryWrapper<PmsAttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));

        List<Long> attrIds = entities.stream().map((attr) -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());

        if (attrIds == null || attrIds.size() == 0) {
            return null;
        }
        Collection<PmsAttrEntity> attrEntities = this.listByIds(attrIds);
        return (List<PmsAttrEntity>) attrEntities;
    }

    /**
     * 获取当前分组没有关联的所有属性
     *
     * @param params
     * @param attrgroupId
     * @return
     */
    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId) {
        //1、当前分组只能关联自己所属的分类里面的所有属性
        PmsAttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        //2、当前分组只能关联别的分组没有引用的属性
        //2.1)、当前分类下的其他分组
        List<PmsAttrGroupEntity> group = attrGroupDao.selectList(new QueryWrapper<PmsAttrGroupEntity>().eq("catelog_id", catelogId));
        List<Long> collect = group.stream().map(item -> {
            return item.getAttrGroupId();
        }).collect(Collectors.toList());

        //2.2)、这些分组关联的属性
        List<PmsAttrAttrgroupRelationEntity> groupId = relationDao.selectList(new QueryWrapper<PmsAttrAttrgroupRelationEntity>().in("attr_group_id", collect));
        List<Long> attrIds = groupId.stream().map(item -> {
            return item.getAttrId();
        }).collect(Collectors.toList());

        //2.3)、从当前分类的所有属性中移除这些属性；
        QueryWrapper<PmsAttrEntity> wrapper = new QueryWrapper<PmsAttrEntity>().eq("catelog_id", catelogId).eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        if (attrIds != null && attrIds.size() > 0) {
            wrapper.notIn("attr_id", attrIds);
        }
        String key = (String) params.get("key");
        if (!org.springframework.util.StringUtils.isEmpty(key)) {
            wrapper.and((w) -> {
                w.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<PmsAttrEntity> page = this.page(new Query<PmsAttrEntity>().getPage(params), wrapper);

        PageUtils pageUtils = new PageUtils(page);

        return pageUtils;
    }

    @Override
    public void deleteRelation(AttrGroupRelationVo[] vos) {
        //relationDao.delete(new QueryWrapper<>().eq("attr_id",1L).eq("attr_group_id",1L));
        List<PmsAttrAttrgroupRelationEntity> entities = Arrays.asList(vos).stream().map((item) -> {
            PmsAttrAttrgroupRelationEntity relationEntity = new PmsAttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, relationEntity);
            return relationEntity;
        }).collect(Collectors.toList());
        relationDao.deleteBatchRelation(entities);
    }

    @Override
    public List<PmsAttrEntity> getAttrs(List<Long> attrs) {
        return baseMapper.selectBatchIds(attrs);
    }

}