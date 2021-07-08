package com.hwj.mall.order.interceptor;


import com.hwj.common.constant.AuthConstant;
import com.hwj.common.vo.MemberEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component  //加入容器
public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberEntity> loginUser = new ThreadLocal<>();

    /**
     * 前置拦截、
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {


        String uri = request.getRequestURI();
        boolean match = new AntPathMatcher().match("/order/order/infoByOrderSn/**", uri);
        if (match) {
            return true;
        }
        MemberEntity attribute = (MemberEntity) request.getSession().getAttribute(AuthConstant.LOGIN_USER);
        if (attribute != null) {
            loginUser.set(attribute);
            return true;
        } else {
            request.getSession().setAttribute("msg", "请先登入");
            response.sendRedirect("http://auth.mall.com/login.html");
            return false;
        }
    }
}
