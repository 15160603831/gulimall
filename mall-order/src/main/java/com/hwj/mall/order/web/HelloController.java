package com.hwj.mall.order.web;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author hwj
 */
@Controller
public class HelloController {

    @GetMapping("/{page}.html")
    public String listPage(@PathVariable("page") String page) {
        return page;
    }

}
