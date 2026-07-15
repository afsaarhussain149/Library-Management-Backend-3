package com.library.bean;

import java.io.Serializable;
import java.util.List;

public class PaginationModel implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int currentPageNo;
	private int totalPages;
	private int pageLimit;
	private Long totalRecords;
	private List<?> paginationListRecords;
	
	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	
	public int getCurrentPageNo() {
		return currentPageNo;
	}

	public void setCurrentPageNo(int currentPageNo) {
		this.currentPageNo = currentPageNo;
	}

	public int getPageLimit() {
		return pageLimit;
	}

	public Long getTotalRecords() {
		return totalRecords;
	}


	public void setPageLimit(int pageLimit) {
		this.pageLimit = pageLimit;
	}

	public void setTotalRecords(Long totalRecords) {
		this.totalRecords = totalRecords;
	}

	public List<?> getPaginationListRecords() {
		return paginationListRecords;
	}

	public void setPaginationListRecords(List<?> paginationListRecords) {
		this.paginationListRecords = paginationListRecords;
	}

}
