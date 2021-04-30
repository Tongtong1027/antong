package com.framework.shiro;

import com.framework.antong.sys.bean.LoginInfoDto;
import com.framework.antong.sys.enums.LoginTypeEnums;
import com.framework.common.utils.Constant;
import lombok.Data;
import org.apache.shiro.authc.UsernamePasswordToken;

/**
 * @Auther: ronghai
 * @Date: 2019/6/22 : 19:14
 * @Desc:
 */
@Data
public class CustomizedToken extends UsernamePasswordToken {

    private LoginTypeEnums loginTypeEnums;

    private LoginInfoDto loginInfoDto;

    public CustomizedToken(final LoginInfoDto loginInfoDto,final LoginTypeEnums loginTypeEnums,boolean rememberMe) {
        super(loginInfoDto.getUserName(),LoginTypeEnums.USERNAME_PWD.equals(loginTypeEnums)?loginInfoDto.getPassword(): Constant.SYS_CODE,rememberMe);
        this.loginTypeEnums = loginTypeEnums;
        this.loginInfoDto = loginInfoDto;
    }

}
