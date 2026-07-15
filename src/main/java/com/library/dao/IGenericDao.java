package com.library.dao;

import java.io.Serializable;
import java.util.List;

import com.library.bean.PaginationModel;


public interface IGenericDao <T extends Serializable> {
	
	  
	   List<T> executeDDLHQL(String hqlquery, Object[] listofparameter);
	   List<T> executeDDLSQL(String sqlquery, Object[] listofparameter);
	   PaginationModel getPaginationWithQuery(PaginationModel paginationModel, Integer currentPage, String hqlquery, Object[] listofparameter);
	   PaginationModel getPaginationWithQuery50(PaginationModel paginationModel, Integer currentPage, String hqlquery, Object[] listofparameter);
	   public void setClazz( Class< T > clazzToSet );
	   PaginationModel getPaginationWithQueryWithoutPagesCount(PaginationModel paginationModel, Integer currentPage, String hqlquery, Object[] listofparameter);
	   PaginationModel getPaginationWithSQLQueryWithoutPagesCount(PaginationModel paginationModel, Integer currentPage, String hqlquery, Object[] listofparameter);
	   public void executeDMLSQL(String sqlquery, Object[] listofparameter);
	 
	   public <T> void save(final T entity);
	   PaginationModel getPaginationWithSQLQuery(PaginationModel paginationModel, Integer currentPage, String sqlquery, Object[] listofparameter);
	   public <T> void update(final T entity);
}
