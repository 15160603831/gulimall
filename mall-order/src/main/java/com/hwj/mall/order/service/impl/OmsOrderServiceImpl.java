package com.hwj.mall.order.service.impl;

import com.hwj.common.to.SkuHasStockVO;
import com.hwj.common.vo.MemberEntity;
import com.hwj.mall.order.feign.CartFeignService;
import com.hwj.mall.order.feign.MemberFeignService;
import com.hwj.mall.order.feign.WmsFeignService;
import com.hwj.mall.order.interceptor.LoginUserInterceptor;
import com.hwj.mall.order.vo.MemberAddressVo;
import com.hwj.mall.order.vo.OrderConfirmVo;
import com.hwj.mall.order.vo.OrderItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.Query;

import com.hwj.mall.order.dao.OmsOrderDao;
import com.hwj.mall.order.entity.OmsOrderEntity;
import com.hwj.mall.order.service.OmsOrderService;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("omsOrderService")
public class OmsOrderServiceImpl extends ServiceImpl<OmsOrderDao, OmsOrderEntity> implements OmsOrderService {

    @Autowired
    private MemberFeignService memberFeignService;
    @Autowired
    private CartFeignService cartFeignService;
    @Autowired
    private ThreadPoolExecutor executor;
    @Autowired
    private WmsFeignService wmsFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OmsOrderEntity> page = this.page(
                new Query<OmsOrderEntity>().getPage(params),
                new QueryWrapper<OmsOrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 订单确认页返回项
     *
     * @return
     */
    @Override
    public OrderConfirmVo confirmVo() throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        MemberEntity memberEntity = LoginUserInterceptor.loginUser.get();
        //获取之前的请求
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        //会员地址
        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            //每个线程都需要共享之前的请求
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> address = memberFeignService.getAddress(memberEntity.getId());
            orderConfirmVo.setAddress(address);
        }, executor);

        CompletableFuture<Void> orderItemFuture = CompletableFuture.runAsync(() -> {
            //每个线程都需要共享之前的请求
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //购物车
            List<OrderItemVo> item = cartFeignService.currentUserCartItem();
            orderConfirmVo.setOrderItemVos(item);
        }, executor).thenRunAsync(() -> {
            List<OrderItemVo> orderItemVos = orderConfirmVo.getOrderItemVos();
            List<Long> collect = orderItemVos.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
            List<SkuHasStockVO> skuHasStock = wmsFeignService.getSkuHasStock(collect);
            if (skuHasStock != null) {
                Map<Long, Boolean> map = skuHasStock.stream().collect(Collectors.toMap(SkuHasStockVO::getSkuId, SkuHasStockVO::getHasStock));
                orderConfirmVo.setHasStock(map);
            }

        });
        //3、用户积分
        orderConfirmVo.setIntegration(memberEntity.getIntegration());

        CompletableFuture.allOf(getAddressFuture, orderItemFuture).get();
        //todo 防重令牌
        return orderConfirmVo;
    }

}