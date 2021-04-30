package com.framework.shiro;

import com.framework.antong.sys.bean.UserAuthed;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * @Auther: ronghai
 * @Date: 2019/7/3 : 15:28
 * @Desc:
 */
public class ShiroUtils {

    public static UserAuthed getUserAuthed(){
        UserAuthed userAuthed = (UserAuthed) SecurityUtils.getSubject().getPrincipal();
        return userAuthed;
    }

    public static Long getSysUserId(){
        return getUserAuthed().getSysUserId();
    }

    public static String getUserName(){
        return getUserAuthed().getUserName();
    }

    public static String getRealName(){
        return getUserAuthed().getRealName();
    }

    public static String getToken(ServletRequest request) {
        String paramToken = WebUtils.toHttp(request).getParameter("token");
        String headerToken = WebUtils.toHttp(request).getHeader("X-Token");
        if (StringUtils.isNotEmpty(paramToken)) {
            return paramToken;
        } else if (StringUtils.isNotEmpty(headerToken)) {
            return headerToken;
        } else {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            Cookie[] cookies = httpRequest.getCookies();
            if (cookies != null) {
                for (int i = 0; i < cookies.length; i++) {
                    if ("JSESSIONID".equalsIgnoreCase(cookies[i].getName())) {
                        return cookies[i].getValue();
                    }
                }
            }
            return null;
        }
    }

}
