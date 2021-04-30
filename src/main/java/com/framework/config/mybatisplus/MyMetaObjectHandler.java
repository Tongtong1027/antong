package com.framework.config.mybatisplus;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.framework.antong.sys.bean.UserAuthed;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * mybatisplus属性自动填充
 * author: chenkaihang
 * date: 2020/8/11 3:29 下午
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    private static final Logger LOG = LoggerFactory.getLogger(MyMetaObjectHandler.class);

    @Override
    public void insertFill(MetaObject metaObject) {
        LOG.info(" -------------------- start insert fill ...  --------------------");
        //  通过名字修改属性值           属性名    属性值        元数据(参数)
        this.setFieldValByName("isActive","Y",metaObject);
        this.setFieldValByName("createTime",new Date(),metaObject);
        if(this.getLoginUser() == null) {
            this.setFieldValByName("createBy",0L,metaObject);
        }else {
            this.setFieldValByName("createBy",this.getLoginUser().getSysUserId(),metaObject);
        }
        //this.setFieldValByName("updateTime",new Date(),metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        LOG.info(" -------------------- start update fill ...  --------------------");
        this.setFieldValByName("updateTime",new Date(),metaObject);
        if(this.getLoginUser() == null) {
            this.setFieldValByName("updateBy",0L,metaObject);
        }else {
            this.setFieldValByName("updateBy",this.getLoginUser().getSysUserId(),metaObject);
        }
    }
    
    private UserAuthed getLoginUser() {
        UserAuthed loginUser = null;
        try {
            loginUser = SecurityUtils.getSubject().getPrincipal() != null ? (UserAuthed) SecurityUtils.getSubject().getPrincipal() : null;
        } catch (Exception e) {
            //e.printStackTrace();
            loginUser = null;
        }
        return loginUser;
    }
}
