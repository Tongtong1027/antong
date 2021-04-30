package com.framework.common.enums;

/**
 * author: chenkaihang
 * date: 2020/9/4 10:19 上午
 */
public enum ResponseCodeEnums {

    /**
     * 成功
     */
    SUCCESS(1),

    /**
     * 失败
     */
    ERROR(0);

    private final Integer code;

    ResponseCodeEnums(Integer code) {
        this.code = code;
    }

    public Integer value() {
        return code;
    }

    public static ResponseCodeEnums getEnum(String code) {
        for (ResponseCodeEnums enumCode : ResponseCodeEnums.values()) {
            if (enumCode.value().equals(code)) {
                return enumCode;
            }
        }
        return null;
    }
}
