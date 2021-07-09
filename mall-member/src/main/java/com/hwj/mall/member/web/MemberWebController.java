package com.hwj.mall.member.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author hwj
 */
@Controller
public class MemberWebController {

    @GetMapping("/memberOrder.html")
    public String memberOrderPage() {

        return "orderList";
    }

}
