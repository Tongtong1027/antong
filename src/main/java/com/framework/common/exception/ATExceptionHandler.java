package com.framework.common.exception;

import com.framework.common.enums.ResultCodeEnums;
import com.framework.common.utils.Result;
import org.apache.shiro.authz.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.ServletException;
import java.util.Iterator;
import java.util.List;

/**
 * 异常处理器
 *
 * @author Mark sunlightcs@gmail.com
 */
@RestControllerAdvice
public class ATExceptionHandler {
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 处理自定义异常
	 */
	@ExceptionHandler(ATException.class)
	public Result handleRRException(ATException e){
		Result r = new Result();
		return r.code(e.getCode()).msg(e.getMessage()).setSuccess(false);
	}

	@ExceptionHandler(NoHandlerFoundException.class)
	public Result handlerNoFoundException(Exception e) {
		logger.error(e.getMessage(), e);
		return Result.error(ResultCodeEnums.NOT_FOUND);
	}

	@ExceptionHandler(DuplicateKeyException.class)
	public Result handleDuplicateKeyException(DuplicateKeyException e){
		logger.error(e.getMessage(), e);
		return Result.error(ResultCodeEnums.DUPLICATE_KEY);
	}

	@ExceptionHandler(AuthorizationException.class)
	public Result handleAuthorizationException(AuthorizationException e){
		logger.error(e.getMessage(), e);
		return Result.error(ResultCodeEnums.UNAUTHORIZED);
	}

	@ExceptionHandler(Exception.class)
	public Result handleException(Exception e){
		logger.error(e.getMessage(), e);
		return Result.error(e.getMessage());
	}

	@ExceptionHandler(ServletException.class)
	@ResponseBody
	public Result error(ServletException e) {
		return Result.error().msg(e.getMessage()).setSuccess(false);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseBody
	public Result error(HttpMessageNotReadableException e) {
		return Result.error(ResultCodeEnums.PARAM_ERROR).msg(e.getMessage());
	}


	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseBody
	public Result error(MethodArgumentNotValidException e) {
		BindingResult result = e.getBindingResult();
		StringBuffer errorBuffer = new StringBuffer();
		errorBuffer.append("参数错误:");
		if (result.hasErrors()) {
			List<ObjectError> errors = result.getAllErrors();
			Iterator<ObjectError> iterator = errors.iterator();
			while (iterator.hasNext()){
				FieldError fieldError = (FieldError) iterator.next();
				errorBuffer.append(fieldError.getDefaultMessage());
				if(iterator.hasNext()){
					errorBuffer.append(",");
				}
			}
		}
		errorBuffer.append(".");
		return Result.error(ResultCodeEnums.PARAM_ERROR).msg(errorBuffer.toString());
	}
}
