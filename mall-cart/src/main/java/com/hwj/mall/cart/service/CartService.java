package com.hwj.mall.cart.service;

import com.hwj.mall.cart.vo.CartItemVO;
import com.hwj.mall.cart.vo.CartVO;

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

    /**
     * 获取购物车中sku购物项
     *
     * @param skuId
     * @return
     */
    CartItemVO getCartItem(Long skuId);

    /**
     * 获取整个购物车
     *
     * @return
     */
    CartVO getCart() throws ExecutionException, InterruptedException;

    /**
     * 清空购物车
     *
     * @param cartKey
     */
    void clearCart(String cartKey);

    void checkItem(Long skuId, Integer check);

    void countItm(Long skuId, Integer num);

    void deleteItem(Long skuId);
}
