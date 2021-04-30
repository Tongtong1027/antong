package com.framework.common.utils;

import com.framework.common.enums.ResultCodeEnums;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * author: chenkaihang
 * date: 2020/9/3 5:26 下午
 */
@Data
@Accessors(chain = true)
@ApiModel(value="接口返回对象", description="接口返回对象")
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("响应是否成功")
    private Boolean success;

    @ApiModelProperty(
            value = "响应码,0表示成功",
            example = "0"
    )
    private Integer responseCode;

    @ApiModelProperty("响应消息")
    private String responseMessage;

    @ApiModelProperty("返回值")
    private T data;

    public Result() {

    }

    public static Result ok() {
        return ok(ResultCodeEnums.OK);
    }

    public static Result ok(String msg) {
        return ok().setResponseMessage(msg);
    }

    public static Result ok(ResultCodeEnums rCodeEnum) {
        Result r = new Result();
        r.setResponseMessage(rCodeEnum.getMsg());
        r.setResponseCode(rCodeEnum.getCode());
        r.setSuccess(rCodeEnum.getSuccess());
        return r;
    }

    public static Result error() {
        return error(ResultCodeEnums.ERROR);
    }

    public static Result error(String msg) {
        return error().setResponseMessage(msg);
    }

    public static Result error(Integer code, String msg) {
        Result r = new Result();
        r.setResponseCode(code);
        r.setResponseMessage(msg);
        return r;
    }

    public static Result error(ResultCodeEnums rCodeEnum) {
        Result r = new Result();
        r.setResponseCode(rCodeEnum.getCode());
        r.setResponseMessage(rCodeEnum.getMsg());
        r.setSuccess(rCodeEnum.getSuccess());
        return r;
    }

    public Result success(Boolean success) {
        this.setSuccess(success);
        return this;
    }

    public Result code(Integer code) {
        this.setResponseCode(code);
        return this;
    }

    public Result msg(String msg) {
        this.setResponseMessage(msg);
        return this;
    }

    public Result data(T data) {
        this.setData(data);
        return this;
    }

}

