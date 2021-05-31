package com.hwj.common.exception;


/**
 * 错误码列表
 * 10 通用
 * 001：参数数据校验
 * 11：商品
 * 12订单
 * 13购物车
 * 14物流
 *
 * @author hwj
 */

public enum BizCodeEnum {

    UNKNOW_EXCEPTION(10001, "系统未知异常"),
    VAILD_EXCEPTION(10002, "参数格式校验失败"),
    PRODUCT_UP_EXCEPTION(10003, "商品上架异常");
    private int code;

    private String msg;

    BizCodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }


    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
