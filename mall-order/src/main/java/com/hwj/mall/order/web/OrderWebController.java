package com.hwj.mall.order.web;

import com.hwj.mall.order.service.OrderService;
import com.hwj.mall.order.vo.OrderConfirmVo;
import com.hwj.mall.order.vo.OrderSubmitVo;
import com.hwj.mall.order.vo.SubmitOrderResponseVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@Controller
public class OrderWebController {

    @Autowired
    private OrderService orderService;


    @ApiOperation("订单确认页")
    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = orderService.confirmVo();
        model.addAttribute("orderConfirmData", orderConfirmVo);
        return "confirm";
    }

    @PostMapping("/submitOrder")
    @ApiOperation("提交订单")
    public String submitOrder(@RequestBody OrderSubmitVo orderSubmitVo) {
        //创建订单
        SubmitOrderResponseVo submitOrderResponseVo = orderService.submitOrder(orderSubmitVo);
        System.out.println(orderSubmitVo.toString());
        if (submitOrderResponseVo.getCode() == 0) {
            //下单成功支付选择页

            return "pay";
        } else {
            //下单失败回确认订单页
            return "redirect:http://order.mall.com/toTrade";
        }

    }
}
