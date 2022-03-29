package com.framework.shiro;

import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authc.pam.AtLeastOneSuccessfulStrategy;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
@Component
@ConfigurationProperties(prefix = ShiroConfig.PREFIX)
public class ShiroConfig {

    public static final String PREFIX = "shiro";

    private String loginUrl;

    private String successUrl;

    private String unauthorizedUrl;

    private String hashIterations;

    private String hashAlgorithmName;

    private Long sessionTimeout;

    /**
     * SecurityManager
     * @return
     */
    @Bean(name = "securityManager")
    public SecurityManager securityManager(){
        DefaultWebSecurityManager securityManager =  new DefaultWebSecurityManager();
        //设置realm
        securityManager.setAuthenticator(new CustomizedModularRealmAuthenticator());
        CustomizedSessionManager customizedSessionManager = new CustomizedSessionManager();
        //设置session过期时间 1000 = 1秒
        //customizedSessionManager.setGlobalSessionTimeout(1800000L);
        customizedSessionManager.setGlobalSessionTimeout(this.getSessionTimeout());
        securityManager.setSessionManager(customizedSessionManager);
        List<Realm> realms = new ArrayList<>();
        realms.add(sysUserRealm());
        realms.add(openIdRealm());
        securityManager.setRealms(realms);

        return securityManager;
    }

    /**
     * ShiroFilterFactoryBean 处理拦截资源文件问题。
     * 注意：单独一个ShiroFilterFactoryBean配置是或报错的，因为在
     * 初始化ShiroFilterFactoryBean的时候需要注入：SecurityManager
     *
     Filter Chain定义说明
     1、一个URL可以配置多个Filter，使用逗号分隔
     2、当设置多个过滤器时，全部验证通过，才视为通过
     3、部分过滤器可指定参数，如perms，roles
     *
     */
    @Bean(name = "shiroFilter")
    public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager){

        ShiroFilterFactoryBean shiroFilterFactoryBean  = new ShiroFilterFactoryBean();

        shiroFilterFactoryBean.setSecurityManager(securityManager);

/*        // 如果不设置默认会自动寻找Web工程根目录下的"/login.jsp"页面
        shiroFilterFactoryBean.setLoginUrl("/login");
        // 登录成功后要跳转的链接
        shiroFilterFactoryBean.setSuccessUrl("/index");
        //未授权界面;
        shiroFilterFactoryBean.setUnauthorizedUrl("/unauthorized");*/

        //配置义拦截器
        Map<String, Filter> customisedFilter = new LinkedHashMap<>();

        //Filter ajaxAuthorizationFilter = new AjaxAuthorizationFilter();
        //customisedFilter.put("ajaxAuthorizationFilter",ajaxAuthorizationFilter);

        Filter customizedAuthorizationFiler = new CustomizedAuthorizationFilter();
        customisedFilter.put("customizedAuthorizationFiler",customizedAuthorizationFiler);
        shiroFilterFactoryBean.setFilters(customisedFilter);

        //配置过滤器
        Map<String,String> filterChainDefinitions =new LinkedHashMap<>();

        filterChainDefinitions.put("/doLogout","anon");
        filterChainDefinitions.put("/doLoginByPassword","anon");
        filterChainDefinitions.put("/doLoginByOpenId","anon");
        filterChainDefinitions.put("/webjars/**", "anon");
        filterChainDefinitions.put("/swagger/**", "anon");
        filterChainDefinitions.put("/v2/api-docs", "anon");
        filterChainDefinitions.put("/swagger-ui.html", "anon");
        filterChainDefinitions.put("/swagger-ui.html/*", "anon");
        filterChainDefinitions.put("swagger-ui.html", "anon");
        filterChainDefinitions.put("/swagger-resources/**", "anon");
        filterChainDefinitions.put("/swagger-resources", "anon");
        filterChainDefinitions.put("/test/**", "anon");
        filterChainDefinitions.put("/produce/**", "anon");
        filterChainDefinitions.put("/notifyAll/**", "anon");
        filterChainDefinitions.put("/**", "customizedAuthorizationFiler");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitions);

        return shiroFilterFactoryBean;
    }

    /**
     * 使用自定义realm管理器
     * @return
     */
    @Bean
    public CustomizedModularRealmAuthenticator customizedModularRealmAuthenticator(){
        //自己重写的ModularRealmAuthenticator
        CustomizedModularRealmAuthenticator customizedModularRealmAuthenticator = new CustomizedModularRealmAuthenticator();
        customizedModularRealmAuthenticator.setAuthenticationStrategy(new AtLeastOneSuccessfulStrategy());
        return customizedModularRealmAuthenticator;
    }

    @Bean
    public SysUserRealm sysUserRealm () {
        SysUserRealm sysUserRealm = new SysUserRealm();
        sysUserRealm.setCredentialsMatcher(hashedCredentialsMatcher());//设置解密规则
        return sysUserRealm;
    }

    @Bean
    public OpenIdRealm openIdRealm () {
        OpenIdRealm openIdRealm = new OpenIdRealm();
        return openIdRealm;
    }


    /**
     *  开启Shiro的注解(如@RequiresRoles,@RequiresPermissions)
     * @return
     */
    @Bean
    public DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator(){
        DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        advisorAutoProxyCreator.setProxyTargetClass(true);
        return advisorAutoProxyCreator;
    }

    /**
     * 开启aop注解支持
     * @param securityManager
     * @return
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }



    /**
     * 加密方法
     * @return
     */
    @Bean(name = "credentialsMatcher")
    public HashedCredentialsMatcher hashedCredentialsMatcher() {
        HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
        hashedCredentialsMatcher.setHashAlgorithmName(this.getHashAlgorithmName());// 散列算法:这里使用MD5算法;
        hashedCredentialsMatcher.setHashIterations(Integer.parseInt(this.getHashIterations()));// 散列的次数，比如散列两次，相当于md5(md5(""));
        hashedCredentialsMatcher.setStoredCredentialsHexEncoded(true);// 表示是否存储散列后的密码为16进制，需要和生成密码时的一样，默认是base64；
        return hashedCredentialsMatcher;
    }

    public static String getPREFIX() {
        return PREFIX;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getSuccessUrl() {
        return successUrl;
    }

    public void setSuccessUrl(String successUrl) {
        this.successUrl = successUrl;
    }

    public String getUnauthorizedUrl() {
        return unauthorizedUrl;
    }

    public void setUnauthorizedUrl(String unauthorizedUrl) {
        this.unauthorizedUrl = unauthorizedUrl;
    }

    public String getHashIterations() {
        return hashIterations;
    }

    public void setHashIterations(String hashIterations) {
        this.hashIterations = hashIterations;
    }

    public String getHashAlgorithmName() {
        return hashAlgorithmName;
    }

    public void setHashAlgorithmName(String hashAlgorithmName) {
        this.hashAlgorithmName = hashAlgorithmName;
    }

    public Long getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(Long sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }
}
