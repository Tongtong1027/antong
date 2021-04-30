package com.framework.antong.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.framework.antong.sys.bean.LoginInfoDto;
import com.framework.antong.sys.entity.SysUser;
import com.framework.antong.sys.enums.LoginTypeEnums;
import com.framework.common.utils.Result;


import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author Chen
 * @since 2020-09-04
 */
public interface SysUserService extends IService<SysUser> {

    Map<String, Object> doLoginByPassword(HttpServletRequest request, LoginInfoDto map, LoginTypeEnums usernamePwd);

    SysUser queryUserByUserName(String username);

    Map<String, Object> doLoginByOpenId(HttpServletRequest request, LoginInfoDto loginInfoDto, LoginTypeEnums openId);

    SysUser queryUserByOpenId(String openId);
}
