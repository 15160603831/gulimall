package com.hwj.mall.cart.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.hwj.common.utils.R;
import com.hwj.mall.cart.Interceptor.CartInterceptor;
import com.hwj.mall.cart.feign.ProductFeignService;
import com.hwj.mall.cart.service.CartService;
import com.hwj.mall.cart.to.UserInfoTo;
import com.hwj.mall.cart.vo.CartItemVO;
import com.hwj.mall.cart.vo.CartVO;
import com.hwj.mall.cart.vo.SkuInfoVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

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
        String res = (String) cartOps.get(skuId.toString());
        if (StringUtils.isEmpty(res)) {
            CartItemVO cartItem = new CartItemVO();
            //添加新商品到购物车
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

            CompletableFuture.allOf(getSkuInfo, getSkuSaleAttr).get();
            String toJSONString = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(), toJSONString);
            return cartItem;
        } else {
            //购物车有商品，修改数量
            CartItemVO item = JSON.parseObject(res, CartItemVO.class);
            item.setCount(item.getCount() + num);
            cartOps.put(skuId.toString(), JSON.toJSONString(item));
            return item;

        }
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

    /**
     * 获取购物车中sku购物项
     *
     * @param skuId
     * @return
     */
    @Override
    public CartItemVO getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String o = (String) cartOps.get(skuId.toString());
        CartItemVO itemVO = JSON.parseObject(o, CartItemVO.class);
        return itemVO;
    }

    /**
     * 获取整个购物车
     *
     * @return
     */
    @Override
    public CartVO getCart() throws ExecutionException, InterruptedException {
        CartVO cartVO = new CartVO();
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        //是否登入购物车
        if (userInfoTo.getUserId() != null) {
            //登入状态
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
            //是否存在临时购物车
            List<CartItemVO> tempCartItem = this.getCartItem(CART_PREFIX + userInfoTo.getUserKey());
            if (tempCartItem != null) {
                //存在数据合并购物车
                for (CartItemVO item : tempCartItem) {
                    this.addToCart(item.getSkuId(), item.getCount());
                }
                //清空临时购物车
                this.clearCart(CART_PREFIX + userInfoTo.getUserKey());
            }
            //获取登入后的购物车
            List<CartItemVO> cartItem = getCartItem(cartKey);
            cartVO.setItems(cartItem);
            return cartVO;
        } else {
            String cartKey = CART_PREFIX + userInfoTo.getUserKey();
            //获取临时购物车
            List<CartItemVO> cartItem = this.getCartItem(cartKey);
            cartVO.setItems(cartItem);
        }
        return cartVO;
    }

    /**
     * 清空购物车
     *
     * @param cartKey
     */
    @Override
    public void clearCart(String cartKey) {
        //未登入
        redisTemplate.delete(cartKey);
    }

    /**
     * 勾选购物项
     *
     * @param skuId
     * @param check
     */
    @Override
    public void checkItem(Long skuId, Integer check) {
        CartItemVO cartItem = getCartItem(skuId);
        cartItem.setCheck(check == 1 ? true : false);
        String s = JSON.toJSONString(cartItem);
        this.getCartOps().put(skuId.toString(), s);
    }

    @Override
    public void countItm(Long skuId, Integer num) {
        CartItemVO cartItem = this.getCartItem(skuId);
        cartItem.setCount(num);
        String s = JSON.toJSONString(cartItem);
        this.getCartOps().put(skuId.toString(), s);
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = this.getCartOps();
        cartOps.delete(skuId.toString());
    }

    /**
     * 获取前选中的购物项
     *
     * @return
     */
    @Override
    public List<CartItemVO> getUserCartItem() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() == null) {
            return null;
        } else {
            String s = CART_PREFIX + userInfoTo.getUserId();
            List<CartItemVO> cartItem = this.getCartItem(s);
            List<CartItemVO> collect = cartItem.stream()
                    .filter(item -> item.getCheck() == true)
                    .map(item -> {
                        //最新价格
                        R r = productFeignService.getPrice(item.getSkuId());
                        SkuInfoVo skuInfoVo = JSON.parseObject(JSON.toJSONString(r.get("PmsSkuInfoEntity")), new TypeReference<SkuInfoVo>() {
                        });
                        item.setPrice(skuInfoVo.getPrice());
                        return item;
                    })
                    .collect(Collectors.toList());
            return collect;
        }
    }


    private List<CartItemVO> getCartItem(String cartKey) {
        //未登入
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
        List<Object> values = operations.values();

        if (values != null && values.size() > 0) {
            List<CartItemVO> collect = values.stream().map(obj -> {
                CartItemVO itemVO = JSON.parseObject((String) obj, CartItemVO.class);
                return itemVO;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

}
