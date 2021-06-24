package com.hwj.mall.product.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author hwj
 */
@Data
@ApiModel("spu规格属性")
public class SpuItemAttrGroupVo {

    @ApiModelProperty("组名")
    private String attrGroupName;

    List<SpuBaseAttrVo> attrs;

}
