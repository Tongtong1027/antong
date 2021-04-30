package com.framework.common.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.framework.common.xss.SQLFilter;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.List;

/**
 * 分页工具类
 *
 * @author Mark sunlightcs@gmail.com
 */
public class PageUtils<T> implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 总记录数
	 */
	private int totalCount;
	/**
	 * 每页记录数
	 */
	private int pageSize;
	/**
	 * 总页数
	 */
	private int totalPage;
	/**
	 * 当前页数
	 */
	private int currPage;
	/**
	 * 列表数据
	 */
	private List<T> list;

	public PageUtils() {
	}

	/**
	 * 分页
	 * @param list        列表数据
	 * @param totalCount  总记录数
	 * @param pageSize    每页记录数
	 * @param currPage    当前页数
	 */
	public PageUtils(List<T> list, int totalCount, int pageSize, int currPage) {
		this.list = list;
		this.totalCount = totalCount;
		this.pageSize = pageSize;
		this.currPage = currPage;
		this.totalPage = (int)Math.ceil((double)totalCount/pageSize);
	}

	/**
	 * 分页
	 */
	public PageUtils(IPage<T> page) {
		this.list = page.getRecords();
		this.totalCount = (int)page.getTotal();
		this.pageSize = (int)page.getSize();
		this.currPage = (int)page.getCurrent();
		this.totalPage = (int)page.getPages();
	}

	/**
	 * 分页
	 */
	public static <T>PageUtils parseResult(IPage<T> page) {
		PageUtils pageUtils = new PageUtils(page);
		return pageUtils;
	}

	/**
	 * 处理分页参数
	 */
	public static Page parsePageInfo(Integer pageNo,Integer pageSize,String sortField,String sortOrder) {
		Page page = new Page<>(pageNo,pageSize);
		String orderField = null;
		String order = sortOrder;
		//排序字段
		//防止SQL注入（因为sidx、order是通过拼接SQL实现排序的，会有SQL注入风险）
		if(!StringUtils.isEmpty(sortField)){
			orderField = SQLFilter.sqlInject(sortField);
		}
		//前端字段排序
		if(StringUtils.isNotEmpty(orderField) && StringUtils.isNotEmpty(order)){
			if(Constant.ASC.equalsIgnoreCase(order)) {
				return  page.addOrder(OrderItem.asc(orderField));
			}else {
				return page.addOrder(OrderItem.desc(orderField));
			}
		}
		return page;
	}


	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}

	public int getCurrPage() {
		return currPage;
	}

	public void setCurrPage(int currPage) {
		this.currPage = currPage;
	}

	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}

}
