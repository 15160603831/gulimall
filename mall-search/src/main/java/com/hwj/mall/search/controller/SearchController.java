package com.hwj.mall.search.controller;

import com.hwj.mall.search.service.MallSearchService;
import com.hwj.mall.search.vo.SearchParamVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author hwj
 */
@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParamVO searchParamVO) {
//        Object result = mallSearchService.search(searchParam);
        return "list";
    }
}
