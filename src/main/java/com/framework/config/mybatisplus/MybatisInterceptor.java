package com.framework.config.mybatisplus;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.springframework.stereotype.Component;

/**
 * mybatis拦截器，自动注入创建人、创建时间、修改人、修改时间
 * @Author scott
 * @Date  2019-01-19
 *
 */
@Slf4j
@Component
@Intercepts({ @Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }) })
public class MybatisInterceptor /**implements Interceptor*/ {
//
//	@Override
//	public Object intercept(Invocation invocation) throws Throwable {
//		MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
//		String sqlId = mappedStatement.getId();
//		log.debug("------sqlId------" + sqlId);
//		SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
//		Object parameter = invocation.getArgs()[1];
//		log.debug("------sqlCommandType------" + sqlCommandType);
//
//		if (parameter == null) {
//			return invocation.proceed();
//		}
//		if (SqlCommandType.INSERT == sqlCommandType) {
//			UserAuthed loginUser = this.getLoginUser();
//			Field[] fields = oConvertUtils.getAllFields(parameter);
//			for (Field field : fields) {
//				log.debug("------field.name------" + field.getName());
//				try {
//					if ("createBy".equals(field.getName())) {
//						field.setAccessible(true);
//						Object local_createBy = field.get(parameter);
//						field.setAccessible(false);
//						if (local_createBy == null || local_createBy.equals("")) {
//							if (loginUser != null) {
//								// 登录人账号
//								field.setAccessible(true);
//								field.set(parameter, loginUser.getAdUserId());
//								field.setAccessible(false);
//							}
//						}
//					}
//					// 注入创建时间
//					if ("createTime".equals(field.getName())) {
//						field.setAccessible(true);
//						Object local_createDate = field.get(parameter);
//						field.setAccessible(false);
//						if (local_createDate == null || local_createDate.equals("")) {
//							field.setAccessible(true);
//							field.set(parameter, new Date());
//							field.setAccessible(false);
//						}
//					}
//					//注入部门编码
////					if ("sysOrgCode".equals(field.getName())) {
////						field.setAccessible(true);
////						Object local_sysOrgCode = field.get(parameter);
////						field.setAccessible(false);
////						if (local_sysOrgCode == null || local_sysOrgCode.equals("")) {
////							// 获取登录用户信息
////							if (sysUser != null) {
////								field.setAccessible(true);
////								field.set(parameter, sysUser.getOrgCode());
////								field.setAccessible(false);
////							}
////						}
////					}
//				} catch (Exception e) {
//				}
//			}
//		}
//		if (SqlCommandType.UPDATE == sqlCommandType) {
//			UserAuthed loginUser = this.getLoginUser();
//			Field[] fields = null;
//			if (parameter instanceof ParamMap) {
//				ParamMap<?> p = (ParamMap<?>) parameter;
//				//update-begin-author:scott date:20190729 for:批量更新报错issues/IZA3Q--
//				if (p.containsKey("et")) {
//					parameter = p.get("et");
//				} else {
//					parameter = p.get("param1");
//				}
//				//update-end-author:scott date:20190729 for:批量更新报错issues/IZA3Q-
//
//				//update-begin-author:scott date:20190729 for:更新指定字段时报错 issues/#516-
//				if (parameter == null) {
//					return invocation.proceed();
//				}
//				//update-end-author:scott date:20190729 for:更新指定字段时报错 issues/#516-
//
//				fields = oConvertUtils.getAllFields(parameter);
//			} else {
//				fields = oConvertUtils.getAllFields(parameter);
//			}
//
//			for (Field field : fields) {
//				log.debug("------field.name------" + field.getName());
//				try {
//					if ("updateBy".equals(field.getName())) {
//						//获取登录用户信息
//						if (loginUser != null) {
//							// 登录账号
//							field.setAccessible(true);
//							field.set(parameter, loginUser.getAdUserId());
//							field.setAccessible(false);
//						}
//					}
//					if ("updateTime".equals(field.getName())) {
//						field.setAccessible(true);
//						field.set(parameter, new Date());
//						field.setAccessible(false);
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}
//		return invocation.proceed();
//	}
//
//	@Override
//	public Object plugin(Object target) {
//		return Plugin.wrap(target, this);
//	}
//
//	@Override
//	public void setProperties(Properties properties) {
//		// TODO Auto-generated method stub
//	}
//
//	//update-begin--Author:scott  Date:20191213 for：关于使用Quzrtz 开启线程任务， #465
//	private UserAuthed getLoginUser() {
//		UserAuthed loginUser = null;
//		try {
//			loginUser = SecurityUtils.getSubject().getPrincipal() != null ? (UserAuthed) SecurityUtils.getSubject().getPrincipal() : null;
//		} catch (Exception e) {
//			//e.printStackTrace();
//			loginUser = null;
//		}
//		return loginUser;
//	}
//	//update-end--Author:scott  Date:20191213 for：关于使用Quzrtz 开启线程任务， #465

}
