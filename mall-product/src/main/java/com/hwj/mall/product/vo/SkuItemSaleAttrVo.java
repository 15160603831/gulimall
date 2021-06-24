package com.hwj.mall.product.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;


@Data
@ApiModel("spu销售属性信息")
public class SkuItemSaleAttrVo {

    @ApiModelProperty("属性id")
    private Long attrId;

    @ApiModelProperty("属性名")
    private String attrName;

    @ApiModelProperty("分组id")
    private String attrValues;


}
