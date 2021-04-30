package com.framework.common.enums;

import lombok.Getter;

/**
 * author: chenkaihang
 * date: 2020/9/3 5:27 下午
 */
@Getter
public enum ResultCodeEnums {

    OK(true,0,"成功"),

    ERROR(false, 500,"系统异常，请联系管理员"),

    NOT_FOUND(false, 404,"路径不存在，请检查路径是否正确"),

    UNAUTHORIZED(true,401,"没有权限，请联系管理员授权"),

    DUPLICATE_KEY(true,421,"数据库中已存在该记录"),

    OFF_LINE(true,-1,"身份校验失败,请重新登录"),

    NULL_POINTER(false,1000,"空指针异常"),

    PARAM_ERROR(false,1002,"参数错误");


    /**
     * 响应是否成功
     */
    private Boolean success;

    /**
     * 响应状态码
     */
    private Integer code;

    /**
     * 响应信息
     */
    private String msg;

    ResultCodeEnums(Boolean success, Integer code, String msg) {
        this.success = success;
        this.code = code;
        this.msg = msg;
    }
}
