package com.framework.antong.sys.enums;

/**
 * author: chenkaihang
 * date: 2021/4/28 2:13 下午
 */
public enum LoginTypeEnums {

    /**
     * 用户名密码登录
     */
    USERNAME_PWD(0,"Sys"),

    /**
     * 微信openId登录
     */
    OPEN_ID(1,"OpenId");


    private final Integer code;

    private final String type;

    LoginTypeEnums(Integer code,String type) {
        this.code = code;
        this.type = type;
    }

    public Integer value() {
        return code;
    }

    public String type() {
        return type;
    }


    public static LoginTypeEnums getEnum(Integer code) {
        for (LoginTypeEnums enumCode : LoginTypeEnums.values()) {
            if (enumCode.value().equals(code)) {
                return enumCode;
            }
        }
        return null;
    }
}
