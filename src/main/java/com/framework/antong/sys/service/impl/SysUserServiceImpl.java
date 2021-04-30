package com.framework.antong.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.framework.antong.sys.bean.LoginInfoDto;
import com.framework.antong.sys.bean.UserAuthed;
import com.framework.antong.sys.entity.SysUser;
import com.framework.antong.sys.enums.LoginTypeEnums;
import com.framework.antong.sys.mapper.SysUserMapper;
import com.framework.antong.sys.service.SysUserService;
import com.framework.shiro.CustomizedToken;
import com.framework.shiro.ShiroUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.CredentialsException;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author Chen
 * @since 2020-09-04
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private static final Logger logger = LoggerFactory.getLogger(SysUserServiceImpl.class);

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public Map<String, Object> doLoginByPassword(HttpServletRequest request, LoginInfoDto loginInfoDto, LoginTypeEnums loginTypeEnums) {
        logger.info("用户" + loginInfoDto.getUserName() + "开始登录," + "》》》登录方式:" + loginTypeEnums.type());
        checkDate(loginInfoDto, loginTypeEnums);

        String rememberMe = request.getParameter("rememberMe");
        CustomizedToken token = new CustomizedToken(loginInfoDto, loginTypeEnums,Boolean.parseBoolean(rememberMe));
        try {
            Subject subject = SecurityUtils.getSubject();
            //如果当前用户未注销，使用新的账号登录，先注销后再登录
            if (ShiroUtils.getUserAuthed() != null) {
                subject.logout();
            }
            // key 自动去对应的realm中进行比对
            subject.login(token);
            Map<String, Object> ret = new HashMap<>();
            ret.put("token", subject.getSession().getId());
            ret.put("userName",ShiroUtils.getUserName());
            //pad端需要真实用户姓名
            ret.put("realName",ShiroUtils.getRealName());
            UserAuthed userAuthed = (UserAuthed) subject.getPrincipal();
            logger.info("用户:" + userAuthed.getUserName() + " 》》》登录成功");
            return ret;
        } catch (UnknownAccountException ex) {
            throw new RuntimeException("无此账号!");
        } catch (IncorrectCredentialsException ex) {
            throw new RuntimeException("密码错误");
        } catch (DisabledAccountException ex) {
            throw new RuntimeException("账号已被禁用");
        } catch (CredentialsException ex) {
            throw new RuntimeException("没权限登录该门户");
        } catch (Exception ex) {
            throw new RuntimeException("登陆异常，请联系管理员");
        }
    }

    @Override
    public SysUser queryUserByUserName(String userName) {
        return sysUserMapper.selectOne(new QueryWrapper<SysUser>().lambda().eq(SysUser::getUserName,userName));
    }

    @Override
    public Map<String, Object> doLoginByOpenId(HttpServletRequest request, LoginInfoDto loginInfoDto, LoginTypeEnums loginTypeEnums) {
        logger.info("微信用户" + loginInfoDto.getOpenId() + "开始登录," + "》》》登录方式:" + loginTypeEnums.type());
        checkDate(loginInfoDto, loginTypeEnums);
        String rememberMe = request.getParameter("rememberMe");
        CustomizedToken token = new CustomizedToken(loginInfoDto, loginTypeEnums,Boolean.parseBoolean(rememberMe));
        try {
            Subject subject = SecurityUtils.getSubject();
            //如果当前用户未注销，使用新的账号登录，先注销后再登录
            if (ShiroUtils.getUserAuthed() != null) {
                subject.logout();
            }
            // key 自动去对应的realm中进行比对
            subject.login(token);
            Map<String, Object> ret = new HashMap<>();
            ret.put("token", subject.getSession().getId());
            ret.put("userName", ShiroUtils.getUserName());
            //pad端需要真实用户姓名
            ret.put("realName", ShiroUtils.getRealName());
            UserAuthed userAuthed = (UserAuthed) subject.getPrincipal();
            logger.info("用户:" + userAuthed.getUserName() + " 》》》登录成功");
            return ret;
        } catch (DisabledAccountException ex) {
            throw new RuntimeException("账号已被禁用");
        } catch (CredentialsException ex) {
            throw new RuntimeException("没权限登录该门户");
        } catch (Exception ex) {
            throw new RuntimeException("登陆异常，请联系管理员");
        }
    }

    @Override
    public SysUser queryUserByOpenId(String openId) {
        return sysUserMapper.selectOne(new QueryWrapper<SysUser>().lambda().eq(SysUser::getOpenId,openId));
    }

    private void checkDate(LoginInfoDto loginInfoDto, LoginTypeEnums loginTypeEnums) {
        if (LoginTypeEnums.USERNAME_PWD.equals(loginTypeEnums)) {
            if (StringUtils.isEmpty(loginInfoDto.getUserName())) {
                throw new RuntimeException("用户名不能为空！");
            }
            if (StringUtils.isEmpty(loginInfoDto.getPassword())) {
                throw new RuntimeException("密码不能为空！");
            }
        }else {
            if(StringUtils.isEmpty(loginInfoDto.getOpenId())) {
                if (StringUtils.isEmpty(loginInfoDto.getUserName())) {
                    throw new RuntimeException("用户名不能为空！");
                }
                if (StringUtils.isEmpty(loginInfoDto.getPassword())) {
                    throw new RuntimeException("密码不能为空！");
                }
            }
        }
    }
}
