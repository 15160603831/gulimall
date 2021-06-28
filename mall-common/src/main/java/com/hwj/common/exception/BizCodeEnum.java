package com.hwj.common.exception;


/**
 * 错误码列表
 * 10 通用
 * 001：参数数据校验
 * 002:参数格式校验失败
 * 003:短信验证码频率太高
 * 11：商品
 * 12订单
 * 13购物车
 * 14物流
 * 15用户
 * 150001
 *
 * @author hwj
 */

public enum BizCodeEnum {

    UNKNOW_EXCEPTION(10001, "系统未知异常"),
    VAILD_EXCEPTION(10002, "参数格式校验失败"),
    SMS_CODE_EXCEPTION(10003, "短信验证码频率太高,稍后再试"),
    PRODUCT_UP_EXCEPTION(11003, "商品上架异常"),
    USER_NAME_EXIST_EXCEPTION(150001, "用户名存在"),
    USER_PHONE_EXIST_EXCEPTION(150002, "手机号码存在"),
    USER_LOGIN_ERROR_EXCEPTION(150003, "账号密码错误");


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
