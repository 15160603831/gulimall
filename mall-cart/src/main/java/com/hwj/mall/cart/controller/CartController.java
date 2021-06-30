package com.hwj.mall.cart.controller;

import com.hwj.common.constant.AuthConstant;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

/**
 * @author hwj
 */
@Controller
public class CartController {

    @GetMapping("/cart.html")
    public String cartListPage() {

        return "cartList";
    }


    /**
     * 加入购物车
     *
     * @return
     */
    @GetMapping("/addToCart")
    public String addToCart() {

        return "success";
    }
}
