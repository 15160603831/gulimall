package com.hwj.mall.cart.controller;

import com.hwj.mall.cart.service.CartService;
import com.hwj.mall.cart.vo.CartItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.concurrent.ExecutionException;


/**
 * @author hwj
 */
@Controller
public class CartController {

    @Autowired
    private CartService cartService;

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
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, Model model) throws ExecutionException, InterruptedException {

        CartItemVO cartItemVO = cartService.addToCart(skuId, num);
        model.addAttribute("item", cartItemVO);
        return "success";
    }
}
