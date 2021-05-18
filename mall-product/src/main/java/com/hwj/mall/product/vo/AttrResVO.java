package com.hwj.mall.product.vo;

import lombok.Data;

/**
 * @author hwj
 */
@Data
public class AttrResVO extends AttrVO{

    private String catelogName;

    private String groupName;

    private Long[] catelogPath;

}
