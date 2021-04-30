package com.framework.antong.sys.enums;

/**
 * author: chenkaihang
 * date: 2021/4/28 2:58 下午
 */
public enum UserStatusEnums {
    /**
     * 未激活
     */
    NOT_ACTIVE("10"),

    /**
     * 正常
     */
    NORMAL("50"),

    /**
     * 停用
     */
    STOP_USE("99");


    private final String code;

    UserStatusEnums(String code) {
        this.code = code;
    }

    public String value() {
        return code;
    }

    public static UserStatusEnums getEnum(String code) {
        for (UserStatusEnums enumCode : UserStatusEnums.values()) {
            if (enumCode.value().equals(code)) {
                return enumCode;
            }
        }
        return null;
    }
}
