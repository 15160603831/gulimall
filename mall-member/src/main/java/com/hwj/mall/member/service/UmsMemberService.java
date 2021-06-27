package com.hwj.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hwj.common.utils.PageUtils;
import com.hwj.mall.member.entity.UmsMemberEntity;
import com.hwj.mall.member.exception.PhoneNumExistException;
import com.hwj.mall.member.exception.UserExistException;
import com.hwj.mall.member.vo.MemberRegisterVo;

import java.util.Map;

/**
 * 会员
 *
 * @author hwj
 * @email huangwenjun@mail.com
 * @date 2021-03-23 17:44:26
 */
public interface UmsMemberService extends IService<UmsMemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 注册用户
     *
     * @param vo
     */
    void register(MemberRegisterVo vo);

    /**
     * 手机号是否唯一
     *
     * @param phone
     * @return
     */
    void checkPhoneUnique(String phone) throws PhoneNumExistException;

    /**
     * 用户名是否唯一
     *
     * @param userName
     * @return
     */
    void checkUserName(String userName) throws UserExistException;


}

