package com.framework.shiro;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.session.mgt.SessionContext;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.Serializable;

/**
 * 自定义session管理器，重写getSessionId方法(如果请求头当中包含Token，优先使用请求头中的token，如果没有携带id参数则按照父类的方式在cookie进行获取)
 */
public class CustomizedSessionManager extends DefaultWebSessionManager {

    //定义一个线程、存放当前登录用户携带的token
    private static final ThreadLocal<String> T1 = new ThreadLocal<>();

    private static final Logger LOG = LoggerFactory.getLogger(DefaultSessionManager.class);

    /**
     * 这个是服务端要返回给客户端，
     */
    public static String TOKEN_NAME = "TOKEN";


    /**
     * 这个是客户端请求给服务端带的header
     */
    public final static String HEADER_TOKEN_NAME = "token";
    public final static Logger LOGGER = LoggerFactory.getLogger(CustomizedSessionManager.class);
    private static final String REFERENCED_SESSION_ID_SOURCE = "Stateless request";

    /**
     * 重写getSessionId,分析请求头中的指定参数，做用户凭证sessionId
     */
    @Override
    protected Serializable getSessionId(ServletRequest request, ServletResponse response){
        String token = ShiroUtils.getToken(request);
        T1.set(token);
        if(StringUtils.isNotEmpty(token)){
            //如果请求头中有 authToken 则其值为sessionId
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE,REFERENCED_SESSION_ID_SOURCE);
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID,token);
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_IS_VALID,Boolean.TRUE);
            return token;
        }else {
            //如果headerToken、paramToken没有携带Token参数则按照父类的方式在cookie进行获取
            return super.getSessionId(request, response);
        }
    }

    /**
     * 重写shiro创建session方法
     * @param context
     * @return
     */
    @Override
    protected Session doCreateSession(SessionContext context) {
        Session session = this.newSessionInstance(context);

        if (LOG.isTraceEnabled()) {
            LOG.trace("Creating session for host {}", session.getHost());
        }
        session.setAttribute("token", T1.get());
        T1.remove();
        super.create(session);
        return session;
    }

}
