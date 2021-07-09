package com.hwj.mall.order.web;

import com.alibaba.fastjson.JSON;
import com.hwj.common.exception.NoStockException;
import com.hwj.common.utils.PageUtils;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Controller
public class OrderWebController {

    @Autowired
    private OrderService orderService;


    @ApiOperation("订单确认页")
    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = orderService.confirmVo();
        System.out.println(JSON.toJSONString(orderConfirmVo));
        model.addAttribute("orderConfirmData", orderConfirmVo);
        return "confirm";
    }

    @PostMapping("/submitOrder")
    @ApiOperation("提交订单")
    public String submitOrder(OrderSubmitVo orderSubmitVo, Model model, RedirectAttributes attributes) {
        try {
            //创建订单
            SubmitOrderResponseVo submitOrderResponseVo = orderService.submitOrder(orderSubmitVo);
            System.out.println(orderSubmitVo.toString());
            if (submitOrderResponseVo.getCode() == 0) {
                //下单成功支付选择页
                model.addAttribute("submitOrderResponseVo", submitOrderResponseVo);
                return "pay";
            } else {
                String msg = "下单失败;";
                switch (submitOrderResponseVo.getCode()) {
                    case 1:
                        msg += "防重令牌校验失败";
                        break;
                    case 2:
                        msg += "商品价格发生变化";
                        break;
                    case 3:
                        msg += "商品库存不足";
                        break;
                }
                attributes.addFlashAttribute("msg", msg);
                //下单失败回确认订单页
                return "redirect:http://order.mall.com/toTrade";
            }
        } catch (Exception e) {
            if (e instanceof NoStockException) {
                String msg = "下单失败，商品无库存";
                attributes.addFlashAttribute("msg", msg);
            }
            return "redirect:http://order.mall.com/toTrade";
        }
    }

    @GetMapping("/memberOrder.html")
    public String memberOrderPage(@RequestParam(value = "pageNum", required = false, defaultValue = "0") Integer pageNum,
                                  Model model) {
        Map<String, Object> params = new HashMap<>();
        params.put("page", pageNum.toString());
        PageUtils page = orderService.queryPageWithItem(params);
        System.out.println(JSON.toJSONString(page));
        model.addAttribute("pageUtil", page);
        return "list";
    }

}
