package com.hwj.mall.cart.controller;

import com.hwj.mall.cart.service.CartService;
import com.hwj.mall.cart.vo.CartItemVO;
import com.hwj.mall.cart.vo.CartVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * @author hwj
 */
@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {
        CartVO cartVO = cartService.getCart();
        model.addAttribute("cart", cartVO);
        return "cartList";
    }


    /**
     * 加入购物车
     *
     * @return
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {

        cartService.addToCart(skuId, num);
//
//        model.addAttribute("skuId", skuId);
        redirectAttributes.addAttribute("skuId", skuId);
        return "redirect:http://cart.mall.com/addToCartSuccess.html";
    }

    @GetMapping("addToCartSuccess.html")
    private String addToCartSuccessPage(@RequestParam("skuId") Long skuId, Model model) {
        //重定向
        CartItemVO cartItemVO = cartService.getCartItem(skuId);
        model.addAttribute("item", cartItemVO);
        return "success";
    }

    @ApiOperation("勾选购物项")
    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId, @RequestParam("check") Integer check) {
        cartService.checkItem(skuId, check);
        return "redirect:http://cart.mall.com/cart.html";
    }

    @GetMapping("/countItem")
    @ApiOperation("改变数量")
    public String countItem(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num) {
        cartService.countItm(skuId, num);
        return "redirect:http://cart.mall.com/cart.html";
    }

    @GetMapping("/deleteItem")
    @ApiOperation("删除购物项")
    public String deleteItem(@RequestParam("skuId") Long skuId) {
        cartService.deleteItem(skuId);
        return "redirect:http://cart.mall.com/cart.html";
    }

    @ApiOperation("获取用户选中购物项")
    @GetMapping("/currentUserCartItem")
    @ResponseBody
    public List<CartItemVO> currentUserCartItem() {
        List<CartItemVO> userCartItem = cartService.getUserCartItem();
        return userCartItem;
    }
}
