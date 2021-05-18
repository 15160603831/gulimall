package com.hwj.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.additional.update.impl.UpdateChainWrapper;
import com.hwj.mall.product.dao.PmsAttrAttrgroupRelationDao;
import com.hwj.mall.product.dao.PmsAttrGroupDao;
import com.hwj.mall.product.dao.PmsCategoryDao;
import com.hwj.mall.product.entity.PmsAttrAttrgroupRelationEntity;
import com.hwj.mall.product.entity.PmsAttrGroupEntity;
import com.hwj.mall.product.entity.PmsCategoryEntity;
import com.hwj.mall.product.service.PmsCategoryService;
import com.hwj.mall.product.vo.AttrResVO;
import com.hwj.mall.product.vo.AttrVO;
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

import com.hwj.mall.product.dao.PmsAttrDao;
import com.hwj.mall.product.entity.PmsAttrEntity;
import com.hwj.mall.product.service.PmsAttrService;
import org.springframework.transaction.annotation.Transactional;

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
        QueryWrapper<PmsAttrEntity> wrapper = new QueryWrapper<>();
        if (catelogId != 0) {
            wrapper.eq("catelof_id", catelogId);
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
//        if(attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()){
        //1、设置分组信息
        PmsAttrAttrgroupRelationEntity attr_id = relationDao.selectOne(new QueryWrapper<PmsAttrAttrgroupRelationEntity>().eq("attr_id", attrId));
        if (attr_id != null) {
            attrResVO.setAttrGroupId(attr_id.getAttrGroupId());
            PmsAttrGroupEntity pmsAttrGroupEntity = attrGroupDao.selectById(attr_id.getAttrGroupId());
            if (pmsAttrGroupEntity != null) {
                attrResVO.setGroupName(pmsAttrGroupEntity.getAttrGroupName());
            }
//            }
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

}