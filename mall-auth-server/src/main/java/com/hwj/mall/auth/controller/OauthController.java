package com.hwj.mall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.hwj.common.constant.AuthConstant;
import com.hwj.common.utils.R;
import com.hwj.mall.auth.feign.MemberFeignService;
import com.hwj.common.vo.MemberEntity;
import com.hwj.mall.auth.vo.SocialUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hwj
 */
@Controller
@Slf4j
public class OauthController {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private MemberFeignService memberFeignService;


    @GetMapping("/oauth2.0/weibo/success")
    public String authorize(@RequestParam("code") String code, HttpSession session) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String url = "https://api.weibo.com/oauth2/access_token?client_id=1176371989&client_secret=3e29b8257b21ccd8c815632bffb72f1a" +
                "&grant_type=authorization_code&redirect_uri=http://auth.mall.com/oauth2.0/weibo/success&code=" + code;
        HttpEntity<SocialUser> request = new HttpEntity<>(null, headers);
        headers.setContentType(MediaType.APPLICATION_JSON);
        //发送post请求换取token
        ResponseEntity<SocialUser> exchange = restTemplate.exchange(url, HttpMethod.POST, request, SocialUser.class);
        Map<String, String> errors = new HashMap<>();
        if (exchange.getBody() != null) {
            //登入
            R r = memberFeignService.login(exchange.getBody());
            if (r.getCode() == 0) {
                String jsonString = JSON.toJSONString(r.get("memberEntity"));
                System.out.println("----------------" + jsonString);
                MemberEntity memberResponseVo = JSON.parseObject(jsonString, new TypeReference<MemberEntity>() {
                });
                System.out.println("----------------" + memberResponseVo);
                session.setAttribute(AuthConstant.LOGIN_USER, memberResponseVo);
                //成功回首页
                return "redirect:http://mall.com";
            } else {
                return "redirect:http://auth.mall.com/login.html";
            }
        } else {
            errors.put("msg", "获得第三方授权失败，请重试");
            session.setAttribute("errors", errors);
            return "redirect:http://auth.mall.com/login.html";
        }
    }


}
