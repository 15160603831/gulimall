package com.hwj.mall.search.service;

import com.hwj.mall.search.vo.SearchParamVO;
import com.hwj.mall.search.vo.SearchResult;

/**
 * @author hwj
 */
public interface MallSearchService {

    /**
     * @param searchParamVO 检索的条件
     * @return 返回检索的结果
     */
    SearchResult search(SearchParamVO searchParamVO);
}
