package com.hwj.mall.member.service.impl;

import com.hwj.mall.member.dao.UmsMemberLevelDao;
import com.hwj.mall.member.entity.UmsMemberLevelEntity;
import com.hwj.mall.member.exception.PhoneNumExistException;
import com.hwj.mall.member.exception.UserExistException;
import com.hwj.mall.member.service.UmsMemberLevelService;
import com.hwj.mall.member.vo.MemberRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.Query;

import com.hwj.mall.member.dao.UmsMemberDao;
import com.hwj.mall.member.entity.UmsMemberEntity;
import com.hwj.mall.member.service.UmsMemberService;


@Service("umsMemberService")
public class UmsMemberServiceImpl extends ServiceImpl<UmsMemberDao, UmsMemberEntity> implements UmsMemberService {

    @Autowired
    private UmsMemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<UmsMemberEntity> page = this.page(
                new Query<UmsMemberEntity>().getPage(params),
                new QueryWrapper<UmsMemberEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 注册会员
     *
     * @param vo
     */
    @Override
    public void register(MemberRegisterVo vo) {
        UmsMemberEntity entity = new UmsMemberEntity();
        //是否唯一
        this.checkPhoneUnique(vo.getPhone());
        this.checkUserName(vo.getUserName());
        //等级
        UmsMemberLevelEntity memberLevelEntity = memberLevelDao.getDefaultLevel();
        entity.setLevelId(memberLevelEntity.getId());
        //手机号、用户名
        entity.setMobile(vo.getPhone());
        entity.setUsername(vo.getUserName());

        //密码加密
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(vo.getPassword());
        entity.setPassword(encode);
        baseMapper.insert(entity);

    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneNumExistException {

        Integer count = baseMapper.selectCount(new QueryWrapper<UmsMemberEntity>().lambda().eq(UmsMemberEntity::getMobile, phone));
        if (count > 0) {
            throw new PhoneNumExistException();
        }
    }

    @Override
    public void checkUserName(String userName) throws UserExistException {
        Integer count = baseMapper.selectCount(new QueryWrapper<UmsMemberEntity>().lambda().eq(UmsMemberEntity::getUsername, userName));
        if (count > 0) {
            throw new UserExistException();
        }
    }

}