package com.framework.shiro;

import com.framework.antong.sys.bean.LoginInfoDto;
import com.framework.antong.sys.bean.UserAuthed;
import com.framework.antong.sys.enums.LoginTypeEnums;
import com.framework.antong.sys.service.SysUserService;
import com.framework.common.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @Auther: ckh
 * @Date: 2021/04/28 : 13:25
 * @Desc:
 */
@Api(description = "登录控制器")
@RestController
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private SysUserService sysUserService;
    /**
     * 用户名密码登陆方法
     * @param request
     * @return
     */
    @ApiOperation(value = "用户名密码登录接口",notes = "必传参数userName、password")
    @RequestMapping(value = "/doLoginByPassword", method = {RequestMethod.POST})
    public Result doLoginByPassword(HttpServletRequest request, @RequestBody LoginInfoDto loginInfoDto) {
        logger.info("PC端登录操作开始");
        Map<String, Object> result = sysUserService.doLoginByPassword(request, loginInfoDto, LoginTypeEnums.USERNAME_PWD);
        return Result.ok().data(result);
    }

    /**
     * 微信端登录
     * @param request
     * @return
     */
    @ApiOperation(value = "微信登录接口",notes = "必传参数userName、password")
    @RequestMapping(value = "/doLoginByOpenId", method = {RequestMethod.POST})
    public Result doLoginByOpenId(HttpServletRequest request, @RequestBody LoginInfoDto loginInfoDto) {
        logger.info("微信端登录操作开始");
        Map<String, Object> result = sysUserService.doLoginByOpenId(request, loginInfoDto, LoginTypeEnums.OPEN_ID);
        return Result.ok().data(result);
    }

    /**
     * shiro 登出方法
     *
     * @return
     */
    @ApiOperation(value = "注销接口", notes = "无需传参")
    @RequestMapping(value = "/doLogout", method = {RequestMethod.POST, RequestMethod.GET})
    public Result doLogoutFromVue(ServletRequest request) {
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            logger.info("用户：" + ((UserAuthed) subject.getPrincipal()).getUserName() + "注销成功");
            subject.logout();
        }
        return Result.ok();
    }



}
