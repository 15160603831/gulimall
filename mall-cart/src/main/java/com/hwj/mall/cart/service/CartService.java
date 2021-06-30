package com.hwj.mall.cart.service;

import com.hwj.mall.cart.vo.CartItemVO;

import java.util.concurrent.ExecutionException;

/**
 * @author hwj
 */
public interface CartService {
    /**
     * 添加购物车
     *
     * @param skuId
     * @param num
     * @return
     */
    CartItemVO addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;
}
