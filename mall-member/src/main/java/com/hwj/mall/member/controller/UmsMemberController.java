package com.hwj.mall.member.controller;

import com.hwj.common.exception.BizCodeEnum;
import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.R;
import com.hwj.mall.member.entity.UmsMemberEntity;
import com.hwj.mall.member.exception.PhoneNumExistException;
import com.hwj.mall.member.exception.UserExistException;
import com.hwj.mall.member.feign.MallCouponFeignServer;
import com.hwj.mall.member.service.UmsMemberService;
import com.hwj.mall.member.vo.MemberRegisterVo;
import com.hwj.mall.member.vo.MemberLoginVo;
import com.hwj.mall.member.vo.SocialUser;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;


/**
 * 会员
 *
 * @author hwj
 * @email huangwenjun@mail.com
 * @date 2021-03-23 17:44:26
 */
@RestController
@ResponseBody
@RequestMapping("member/member")
public class UmsMemberController {
    @Autowired
    private UmsMemberService umsMemberService;

    @Autowired
    private MallCouponFeignServer couponMFeignServer;


    @RequestMapping("/coupon")
    public R test() {
        UmsMemberEntity entity = new UmsMemberEntity();
        entity.setNickname("李四");
        R r = couponMFeignServer.memberCoupon();
        return R.ok().put("member", entity).put("coupon", r.get("coupons"));
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = umsMemberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        UmsMemberEntity umsMember = umsMemberService.getById(id);

        return R.ok().put("umsMember", umsMember);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody UmsMemberEntity umsMember) {
        umsMemberService.save(umsMember);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody UmsMemberEntity umsMember) {
        umsMemberService.updateById(umsMember);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        umsMemberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    /**
     * 会员注册
     */
    @PostMapping("/register")
    public R register(@RequestBody MemberRegisterVo vo) {
        try {
            umsMemberService.register(vo);
        } catch (PhoneNumExistException e) {
            return R.error(BizCodeEnum.USER_PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnum.USER_PHONE_EXIST_EXCEPTION.getMsg());
        } catch (UserExistException e) {
            return R.error(BizCodeEnum.USER_NAME_EXIST_EXCEPTION.getCode(), BizCodeEnum.USER_NAME_EXIST_EXCEPTION.getMsg());
        }
        return R.ok();
    }


    @ApiOperation("账号密码登入")
    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo) {
        UmsMemberEntity entity = umsMemberService.login(vo);
        if (entity != null) {
            return R.ok().put("memberEntity", entity);
        } else {
            return R.error(BizCodeEnum.USER_LOGIN_ERROR_EXCEPTION.getCode(), BizCodeEnum.USER_LOGIN_ERROR_EXCEPTION.getMsg());
        }
    }

    @ApiOperation("微博登入")
    @PostMapping("/oauth2/login")
    public R login(@RequestBody SocialUser socialUser) {
        UmsMemberEntity entity = umsMemberService.authLogin(socialUser);
        if (entity != null) {
            return R.ok().put("memberEntity",entity);
        } else {
            return R.error(BizCodeEnum.USER_LOGIN_ERROR_EXCEPTION.getCode(), BizCodeEnum.USER_LOGIN_ERROR_EXCEPTION.getMsg());
        }
    }


}
