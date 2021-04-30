package com.framework.shiro;

import com.framework.common.enums.ResultCodeEnums;
import com.framework.common.utils.JsonUtils;
import com.framework.common.utils.Result;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.filter.authz.AuthorizationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @Auther: ronghai
 * @Date: 2019/3/28 : 17:58
 * @Desc:
 */

public class CustomizedAuthorizationFilter extends AuthorizationFilter {

    private static final Logger logger = LoggerFactory.getLogger(CustomizedAuthorizationFilter.class);

    //自定义认证方法
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        Subject subject = getSubject(request,response);
        if (subject.getPrincipal() == null) {
            return false;
        }else {
            return true;
        }
    }

    //访问拒绝才会进来此方法
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws IOException {
        Subject subject = getSubject(request, response);
        if (subject.getPrincipal() == null) {
            // 未登录，提示用户重新登录
            saveRequest(request);
            setHeader((HttpServletRequest) request,(HttpServletResponse) response);
            PrintWriter out = response.getWriter();
            response.setContentType("application/json; charset=utf-8");
            response.setCharacterEncoding("utf-8");
            out.write(JsonUtils.getBeanToJson(Result.ok(ResultCodeEnums.OFF_LINE)));
            out.flush();
            out.close();
        } else {
            //匿名访问地址
            String unauthorizedUrl = getUnauthorizedUrl();
            if (StringUtils.hasText(unauthorizedUrl)) {
                //如果匿名访问地址存在，则跳转去匿名访问地址
                WebUtils.issueRedirect(request, response, unauthorizedUrl);
            } else {
                //不存在则返回401
                WebUtils.toHttp(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
        }
        return false;
    }

    /**
     * 在访问过来的时候检测是否为OPTIONS请求，如果是就直接返回true
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (httpRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            setHeader(httpRequest,httpResponse);
            return true;
        }
        return super.preHandle(request,response);
    }

    /**
     * 为response设置header，实现跨域
     */
    private void setHeader(HttpServletRequest request,HttpServletResponse response){
        //跨域的header设置
        response.setHeader("Access-control-Allow-Origin", request.getHeader("Origin"));
        response.setHeader("Access-Control-Allow-Methods", request.getMethod());
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"));
        //防止乱码，适用于传输JSON数据
        response.setHeader("Content-Type","application/json;charset=UTF-8");
        response.setStatus(HttpStatus.OK.value());
    }

}
