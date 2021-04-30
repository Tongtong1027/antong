package com.framework.antong.sys.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.framework.antong.sys.entity.SysUser;
import com.framework.antong.sys.service.SysUserService;
import com.framework.common.utils.PageUtils;
import com.framework.common.utils.Result;
import com.framework.redis.RedisUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author Chen
 * @since 2020-09-04
 */
@Api(description = "用户控制器")
@RestController
@RequestMapping("/sysUser")
public class SysUserController {

    private static final Logger logger = LoggerFactory.getLogger(SysUserController.class);
    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private RedisUtils redisUtils;

    @ApiOperation(value="列表")
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo", value = "分页启始值", required = false, paramType = "query", dataType = "Integer"),
            @ApiImplicitParam(name = "pageSize", value = "分页大小", required = false, paramType = "query", dataType = "Integer"),
            @ApiImplicitParam(name = "sortField", value = "排序字段", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "sortOrder", value = "排序规则 asc:正排 desc:倒排", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "realName", value = "团队名称", required = false, paramType = "query", dataType = "String")
    })
    public Result list(@RequestParam(value = "pageNo",required = false,defaultValue ="0") Integer pageNo,
                       @RequestParam(value = "pageSize",required = false,defaultValue ="10") Integer pageSize,
                       @RequestParam(value = "sortField",required = false,defaultValue ="create_time") String sortField,
                       @RequestParam(value = "sortOrder",required = false,defaultValue ="desc") String sortOrder,
                       @RequestParam(value = "realName",required = false) String realName) {
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<SysUser>().like("real_name", realName);
        Page page = sysUserService.getBaseMapper().selectPage(PageUtils.parsePageInfo(pageNo, pageSize, sortField, sortOrder), StringUtils.isEmpty(realName)?null:queryWrapper);
        return Result.ok().data(page);
    }

}
