package com.hwj.mall.search.controller;

import com.hwj.mall.search.service.MallSearchService;
import com.hwj.mall.search.vo.SearchParamVO;
import com.hwj.mall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author hwj
 */
@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParamVO searchParamVO, Model model, HttpServletRequest request) {
        String queryString = request.getQueryString();
        searchParamVO.set_queryString(queryString);
        SearchResult result = mallSearchService.search(searchParamVO);
        model.addAttribute("result", result);
        return "list";
    }
}
