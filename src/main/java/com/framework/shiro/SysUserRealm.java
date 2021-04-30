package com.framework.shiro;

import com.framework.antong.sys.bean.UserAuthed;
import com.framework.antong.sys.entity.SysUser;
import com.framework.antong.sys.enums.UserStatusEnums;
import com.framework.antong.sys.service.SysUserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>Version: 1.0
 */
public class SysUserRealm extends AuthorizingRealm {

    private static final Logger logger = LoggerFactory.getLogger(SysUserRealm.class);

    @Autowired
    private SysUserService sysUserService;

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {

        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        return authorizationInfo;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {

        logger.info("开始系统用户认证");
        CustomizedToken customizedToken = (CustomizedToken) token;
        String userName = customizedToken.getUsername();
        SysUser sysUser = sysUserService.queryUserByUserName(userName);


        if (sysUser != null && UserStatusEnums.NORMAL.value().equals(sysUser.getUserStatus())) {
            //交给AuthenticatingRealm使用CredentialsMatcher进行密码匹配，如果觉得人家的不好可以自定义实现
            UserAuthed userAuthed = new UserAuthed();
            userAuthed.setSysUserId(sysUser.getSysUserId());
            userAuthed.setUserName(sysUser.getUserName());
            userAuthed.setRealName(sysUser.getRealName());

            // 通过SimpleAuthenticationInfo进行比对,依据的从数据库查询到的用户信息和前文中CustomizedToken中定义的用户信息
            SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(
                    userAuthed, //用户相关信息(用户名等信息)
                    sysUser.getPassword(), //数据库中获取的密码
                    ByteSource.Util.bytes(sysUser.getUserName()),// salt=username+adUserId
                    getName()  //realm name
            );
            return authenticationInfo;
        } else if (sysUser != null && UserStatusEnums.STOP_USE.value().equals(sysUser.getUserStatus())) {
            throw new DisabledAccountException();//账户被禁用
        } else {
            throw new UnknownAccountException();//没找到帐号
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

    @Override
    public CredentialsMatcher getCredentialsMatcher() {
        return new HashedCredentialsMatcher("md5");
    }


}
