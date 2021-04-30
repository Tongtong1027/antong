package com.framework.shiro;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.JavaUuidSessionIdGenerator;
import org.apache.shiro.session.mgt.eis.SessionIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class CustomizedSessionIdGenerator implements SessionIdGenerator {

	private static final Logger logger = LoggerFactory.getLogger(CustomizedSessionIdGenerator.class);

	@Override
	public Serializable generateId(Session session) {
		String token = (String) session.getAttribute("token");
		if(StringUtils.isNotEmpty(token)){
			logger.info("续用前端已有JSESSIONID: "+ token);
			return token;
		}else {
			Serializable uuid = new JavaUuidSessionIdGenerator().generateId(session);
			return uuid;
		}
	}

}

