package com.hwj.mall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.hwj.common.constant.AuthConstant;
import com.hwj.common.exception.BizCodeEnum;
import com.hwj.common.utils.R;
import com.hwj.mall.auth.feign.MemberFeignService;
import com.hwj.mall.auth.feign.ThirdPartyFeignService;
import com.hwj.mall.auth.vo.UserLoginVo;
import com.hwj.mall.auth.vo.UserRegisterVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.jws.WebParam;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author hwj
 */
@Controller
public class LoginController {

    @Autowired
    private ThirdPartyFeignService thirdPartyFeignService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private MemberFeignService memberFeignService;

    @Value("${spring.cache.timeout.code-time}")
    Long codeTime;

    @ResponseBody
    @GetMapping("/sms/sendCode")
    @ApiOperation("发送短信验证码")
    public R sendCode(@RequestParam("phone") String phone) {

        String redisCode = redisTemplate.opsForValue().get(AuthConstant.SMS_CODE + phone);
        if (!StringUtils.isEmpty(redisCode)) {
            long l = Long.parseLong(redisCode.split("_")[1]);
            Long.parseLong(redisCode.split("_")[1]);
            //小于60秒
            if (System.currentTimeMillis() - l < 600000) {
                //60秒内不能再发
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
            }
        }
        String code = (int) ((Math.random() * 9 + 1) * 100000) + "";
        //缓存验证码，防止同一个phone再次发送验证码
        redisTemplate.opsForValue().set(AuthConstant.SMS_CODE + phone, code + "_" + System.currentTimeMillis(), codeTime, TimeUnit.MINUTES);
//        thirdPartyFeignService.sendSms(phone, code);
        return R.ok();
    }


    @PostMapping("/register")
    @ApiOperation("注册")
    public String register(@Valid UserRegisterVo vo, BindingResult result, RedirectAttributes attributes) {
        Map<String, String> errors = new HashMap<>();
        if (result.hasErrors()) {
            //1.1 如果校验不通过，则封装校验结果
            errors = result.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            attributes.addFlashAttribute("errors", errors);
            //校验出错，转发回
            return "redirect:http://auth.mall.com/register.html";
        } else {
            //校验验证码
            String redisCode = redisTemplate.opsForValue().get(AuthConstant.SMS_CODE + vo.getPhone());
            if (!StringUtils.isEmpty(redisCode)) {
                if (vo.getCode().equals(redisCode.split("_")[0])) {
                    //验证码通过，调用会员中心注册
                    //删除验证码
                    redisTemplate.delete(AuthConstant.SMS_CODE + vo.getPhone());
                    R r = memberFeignService.register(vo);
                    if (r.getCode() == 0) {
                        //调用成功，重定向登录页
                        return "redirect:http://auth.mall.com/login.html";
                    } else {
                        //调用失败，返回注册页并显示错误信息
                        String msg = (String) r.get("msg");
                        errors.put("msg", msg);
                        attributes.addFlashAttribute("errors", errors);
                        return "redirect:http://auth.mall.com/register.html";
                    }
                } else {
                    //2.2 验证码错误
                    errors.put("code", "验证码错误");
                    attributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.mall.com/register.html";
                }
            }
        }
        return "redirect:http://auth.mall.com/register.html";
    }


    @PostMapping("/login")
    @ApiOperation("登入")
    public String login(UserLoginVo vo, RedirectAttributes attributes) {
        R login = memberFeignService.login(vo);
        if (login.getCode() == 0) {
            return "redirect:http://mall.com";
        } else {
            String msg = (String) login.get("msg");
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", msg);
            attributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.mall.com/login.html";
        }
    }


}

