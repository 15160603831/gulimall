package com.hwj.mall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.hwj.common.constant.AuthConstant;
import com.hwj.common.utils.R;
import com.hwj.mall.auth.feign.MemberFeignService;
import com.hwj.mall.auth.vo.MemberEntity;
import com.hwj.mall.auth.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hwj
 */
public class OauthController {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private MemberFeignService memberFeignService;


    @RequestMapping("/oauth2.0/weibo/success")
    public String authorize(String code, HttpSession session) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("client_id", "2144471074");
        headers.set("client_secret", "ff63a0d8d591a85a29a19492817316ab");
        headers.set("grant_type", "authorization_code");
        headers.set("redirect_uri", "http://auth.mall.com/oauth2.0/weibo/success");
        headers.set("code", code);
        String url = "https://api.weibo.com/oauth2/access_token";
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
                System.out.println("----------------"+memberResponseVo);
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
