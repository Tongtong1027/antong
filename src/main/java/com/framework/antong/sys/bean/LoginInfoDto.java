package com.framework.antong.sys.bean;

import lombok.Data;

/**
 * author: chenkaihang
 * date: 2021/4/28 2:12 下午
 */
@Data
public class LoginInfoDto {

    /**
     * 用户名
     */
    private String userName;
    /**
     * 密码
     */
    private String password;

    /**
     * openId
     */
    private String openId;
}
