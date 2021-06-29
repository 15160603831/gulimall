package com.hwj.mall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.Query;
import com.hwj.mall.member.dao.UmsMemberDao;
import com.hwj.mall.member.dao.UmsMemberLevelDao;
import com.hwj.mall.member.entity.UmsMemberEntity;
import com.hwj.mall.member.entity.UmsMemberLevelEntity;
import com.hwj.mall.member.exception.PhoneNumExistException;
import com.hwj.mall.member.exception.UserExistException;
import com.hwj.mall.member.service.UmsMemberService;
import com.hwj.mall.member.vo.MemberLoginVo;
import com.hwj.mall.member.vo.MemberRegisterVo;
import com.hwj.mall.member.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;


@Service("umsMemberService")
public class UmsMemberServiceImpl extends ServiceImpl<UmsMemberDao, UmsMemberEntity> implements UmsMemberService {

    @Autowired
    private UmsMemberLevelDao memberLevelDao;
    @Autowired
    private RestTemplate restTemplate;

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
        entity.setNickname(vo.getUserName());

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

    /**
     * 登入
     *
     * @param vo
     * @return
     */
    @Override
    public UmsMemberEntity login(MemberLoginVo vo) {
        LambdaQueryWrapper<UmsMemberEntity> queryWrapper =
                new QueryWrapper<UmsMemberEntity>().lambda()
                        .eq(UmsMemberEntity::getMobile, vo.getLoginAccount())
                        .or()
                        .eq(UmsMemberEntity::getUsername, vo.getLoginAccount());
        UmsMemberEntity entity = baseMapper.selectOne(queryWrapper);
        if (entity == null) {
            //登入失败
            return null;
        } else {
            String passwordDb = entity.getPassword();
            //明文密码和加盐密码校验
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            boolean matches = passwordEncoder.matches(vo.getPassword(), passwordDb);
            if (matches) {
                return entity;
            } else {
                return null;
            }
        }
    }

    /**
     * 微博登入
     *
     * @param socialUser
     * @return
     */
    @Override
    public UmsMemberEntity authLogin(SocialUser socialUser) {
        //登入和注册
        String uid = socialUser.getUid();
        UmsMemberEntity entity = baseMapper.selectOne(new QueryWrapper<UmsMemberEntity>().lambda()
                .eq(UmsMemberEntity::getUid, uid));
        if (entity != null) {
            //用户已注册
            UpdateWrapper<UmsMemberEntity> updateWrapper = new UpdateWrapper();
            updateWrapper.lambda().eq(UmsMemberEntity::getId, entity.getId())
                    .set(UmsMemberEntity::getAccessToken, socialUser.getAccess_token())
                    .set(UmsMemberEntity::getExpiresIn, socialUser.getExpires_in());
            baseMapper.update(null, updateWrapper);
        } else {
            //没有查到用户进行注册
            HttpHeaders headers = new HttpHeaders();
            headers.set("access_token", socialUser.getAccess_token());
            headers.set("uid", socialUser.getUid());
            HttpEntity<String> request = new HttpEntity<>(null, headers);
            String url = "https://api.weibo.com/2/users/show.json";

            ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            //获取社交用户账号信息 查询成功
            if (exchange.getStatusCodeValue() == 200) {
                String json = null;
                //获得昵称，性别，头像
                JSONObject jsonObject = JSON.parseObject(exchange.getBody());
                String name = jsonObject.getString("name");
                String gender = jsonObject.getString("gender");
                String profile_image_url = jsonObject.getString("profile_image_url");
                //保存用户信息
                entity = new UmsMemberEntity();
                entity.setNickname(name)
                        .setGender("m".equals(gender) ? 0 : 1)
                        .setHeader(profile_image_url)
                        .setAccessToken(socialUser.getAccess_token())
                        .setUid(socialUser.getUid())
                        .setExpiresIn(socialUser.getExpires_in());
                baseMapper.insert(entity);
            } else {
                //2 否则更新令牌等信息并返回
                entity.setAccessToken(socialUser.getAccess_token());
                entity.setUid(socialUser.getUid());
                entity.setExpiresIn(socialUser.getExpires_in());
                this.updateById(entity);
            }
        }

        return entity;
    }

}