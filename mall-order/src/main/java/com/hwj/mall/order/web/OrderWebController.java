package com.hwj.mall.order.web;

import com.hwj.mall.order.service.OrderService;
import com.hwj.mall.order.vo.OrderConfirmVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
}
