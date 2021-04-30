package com.framework.antong.test;

import com.framework.AnTongApplication;
import com.framework.antong.sys.controller.SysUserController;
import com.framework.antong.sys.entity.SysUser;
import com.framework.antong.sys.mapper.SysUserMapper;
import com.framework.redis.RedisUtils;
import com.framework.shiro.ShiroMd5Util;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;

/**
 * author: chenkaihang
 * date: 2020/12/25 下午2:52
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {AnTongApplication.class})
//@Transactional(rollbackFor = Exception.class)
public class UserTest {

    private static final Logger logger = LoggerFactory.getLogger(UserTest.class);

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private RedisUtils redisUtils;

    @Test
    public void contextLoads() {
    }

    @Test
    public void getLog() {
        logger.info("我是info");
        System.out.println(logger.isDebugEnabled());
        if (logger.isDebugEnabled()) {
            logger.debug("我是debug");
        }
        logger.warn("我是warn");
        logger.error("我是error");
    }

    @Test
    public void insertUser() {
        SysUser sysUser = new SysUser();
        sysUser.setUserName("kai").setPassword(ShiroMd5Util.sysMd5("123456","kai")).setUserStatus("50").setRealName("陈凯航").setBirthday(new Date()).setGender(1);
        sysUserMapper.insert(sysUser);
    }

    @Test
    public void updateUser() {
        SysUser sysUser = sysUserMapper.selectById(210428161257001L);
        sysUser.setRealName("陈安童").setGender(0);
        sysUserMapper.updateById(sysUser);
        System.out.println(sysUser);
    }

    @Test
    public void selectUser() {
        List<SysUser> users = sysUserMapper.selectList(null);
        System.out.println(users);
    }

    @Test
    public void deleteUser() {
        sysUserMapper.deleteById(210428161257001L);
    }

    @Test
    public void redisTest() {
        redisUtils.set("username","测试用户");
        System.out.println("redis测试成功");
    }


}
