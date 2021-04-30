package com.framework.shiro;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.framework.antong.sys.bean.LoginInfoDto;
import com.framework.antong.sys.bean.UserAuthed;
import com.framework.antong.sys.entity.SysUser;
import com.framework.antong.sys.enums.UserStatusEnums;
import com.framework.antong.sys.service.SysUserService;
import com.framework.common.utils.Constant;
import com.framework.common.utils.RandomStrUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * author: chenkaihang
 * date: 2021/4/28 3:40 下午
 */
public class OpenIdRealm extends AuthorizingRealm {

    private static final Logger logger = LoggerFactory.getLogger(OpenIdRealm.class);

    @Autowired
    private SysUserService sysUserService;

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        return authorizationInfo;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        logger.info("开始系统用户认证");
        CustomizedToken customizedToken = (CustomizedToken) token;
        LoginInfoDto loginInfoDto = customizedToken.getLoginInfoDto();
        SysUser sysUser = sysUserService.queryUserByOpenId(loginInfoDto.getOpenId());
        if(sysUser == null) {
            sysUser = new SysUser();
            sysUser.setUserName(RandomStrUtils.getStringRandom(10)).setUserStatus(UserStatusEnums.NORMAL.value()).setOpenId(loginInfoDto.getOpenId());
            sysUserService.save(sysUser);
        }
        if (UserStatusEnums.NORMAL.value().equals(sysUser.getUserStatus())) {
            //交给AuthenticatingRealm使用CredentialsMatcher进行密码匹配，如果觉得人家的不好可以自定义实现
            UserAuthed userAuthed = new UserAuthed();
            userAuthed.setSysUserId(sysUser.getSysUserId());
            userAuthed.setUserName(sysUser.getUserName());
            userAuthed.setRealName(sysUser.getRealName());

            // 通过SimpleAuthenticationInfo进行比对,依据的从数据库查询到的用户信息和前文中CustomizedToken中定义的用户信息
            SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(
                    userAuthed,
                    Constant.SYS_CODE,
                    getName()  //realm name
            );
            return authenticationInfo;
        } else if (UserStatusEnums.STOP_USE.value().equals(sysUser.getUserStatus())) {
            throw new DisabledAccountException();//账户被禁用
        } else {
            throw new RuntimeException();//未知异常
        }
    }

    @Override
    public void clearCachedAuthorizationInfo(PrincipalCollection principals) {
        super.clearCachedAuthorizationInfo(principals);
    }

    @Override
    public void clearCachedAuthenticationInfo(PrincipalCollection principals) {
        super.clearCachedAuthenticationInfo(principals);
    }

    @Override
    public void clearCache(PrincipalCollection principals) {
        super.clearCache(principals);
    }

}
