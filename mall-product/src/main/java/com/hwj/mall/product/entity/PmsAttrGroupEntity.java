package com.hwj.mall.product.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 属性分组
 * 
 * @author hwj
 * @email huangwenjun@mail.com
 * @date 2021-03-23 17:29:10
 */
@Data
@TableName("pms_attr_group")
public class PmsAttrGroupEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 分组id
	 */
	@TableId
	private Long attrGroupId;
	/**
	 * 组名
	 */
	private String attrGroupName;
	/**
	 * 排序
	 */
	private Integer sort;
	/**
	 * 描述
	 */
	private String descript;
	/**
	 * 组图标
	 */
	private String icon;
	/**
	 * 所属分类id
	 */
	private Long catelogId;
	/**
	 * 删除标记(0:正常;1:删除)
	 */
	@TableLogic
	private Boolean deleteFlag;

	@TableField(exist = false)
	private Long[] catelogPath;

}
