package com.hwj.mall.cart.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.hwj.common.utils.R;
import com.hwj.mall.cart.Interceptor.CartInterceptor;
import com.hwj.mall.cart.feign.ProductFeignService;
import com.hwj.mall.cart.service.CartService;
import com.hwj.mall.cart.to.UserInfoTo;
import com.hwj.mall.cart.vo.CartItemVO;
import com.hwj.mall.cart.vo.SkuInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author hwj
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ProductFeignService productFeignService;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    private final String CART_PREFIX = "mall:cart";

    /**
     * 添加购物车
     *
     * @param skuId
     * @param num
     * @return
     */
    @Override
    public CartItemVO addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        //1判断是否已经登录
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItemVO cartItem = new CartItemVO();
        //查询当前商品信息
        CompletableFuture<Void> getSkuInfo = CompletableFuture.runAsync(() -> {
            R r = productFeignService.info(skuId);

            SkuInfoVo skuInfoVo = JSON.parseObject(JSON.toJSONString(r.get("pmsSkuInfo")), new TypeReference<SkuInfoVo>() {
            });
            //2、商品信息
            cartItem.setCheck(true);
            cartItem.setCount(num);
            cartItem.setImage(skuInfoVo.getSkuDefaultImg());
            cartItem.setTitle(skuInfoVo.getSkuTitle());
            cartItem.setSkuId(skuId);
            cartItem.setPrice(skuInfoVo.getPrice());
        }, threadPoolExecutor);
        //远程查询sku组成信息
        CompletableFuture<Void> getSkuSaleAttr = CompletableFuture.runAsync(() -> {
            //销售属性
            List<String> skuSaleAttrValue = productFeignService.getSkuSaleAttrValue(skuId);
            cartItem.setSkuAttrValues(skuSaleAttrValue);
        }, threadPoolExecutor);

        CompletableFuture.allOf(getSkuInfo,getSkuSaleAttr).get();
        String toJSONString = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(), toJSONString);
        return cartItem;
    }

    /**
     * 获取redis中购物车
     *
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        String cartKey = "";
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() != null) {
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        } else {
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
        return operations;
    }
}
