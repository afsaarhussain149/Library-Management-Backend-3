package com.library.daoImpl;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

import org.hibernate.transform.AliasToEntityMapResultTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.library.bean.PaginationModel;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

public abstract class AbstractJpaDao < T extends Serializable >{

	  private Class< T > clazz;

	@PersistenceContext
	EntityManager entityManager;

	@Autowired
	Environment environment;

	
/***** Save data method *******/
	

	public <T> void save(T entity) {
		entityManager.persist(entity);
	}

	/***** update data method *******/

	public <T> void update(T entity) {
		entityManager.merge(entity);
	}

	/***** delete data method *******/

	public <T> void delete(T entity) {
		entityManager.remove(entity);
	}
	
	
	
	/***** Execute DDL query in HQL method *******/

	@SuppressWarnings("unchecked")
	public <T> List<T> executeDDLHQL(String hqlquery, Object[] listofparameter) {
		List<T> list = null;
		try {
			Query query = entityManager.createQuery(hqlquery);
			for (int c = 0; c < listofparameter.length; c++) {
				query.setParameter(c + 1, listofparameter[c]);
			}
			list = query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	public <T> List<T> executeDDLSQL(String sqlquery, Object[] listofparameter) {
		List<T> list = null;
		try {
			Query query = entityManager.createNativeQuery(sqlquery);
			query.unwrap(org.hibernate.query.Query.class)
					.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);
			for (int c = 0; c < listofparameter.length; c++) {
				query.setParameter(c + 1, listofparameter[c]);
			}
			list = query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public void executeDMLSQL(String sqlquery, Object[] listofparameter){
		   try {
			   Query query =entityManager.createNativeQuery( sqlquery );
			   for (int c = 0; c < listofparameter.length; c++) {
					query.setParameter(c+1, listofparameter[c]);
				}
			   query.executeUpdate();
		  }catch(Exception e) {
			  e.printStackTrace();
		  }
	   }



	/***** Pagination for Query *******/

	public PaginationModel getPaginationWithQuery(PaginationModel paginationModel, Integer currentPage, String hqlquery,
			Object[] listofparameter) {
		try {
			Integer pageLimit = getPageLimit();
			Query query = entityManager.createQuery(hqlquery);
			for (int c = 0; c < listofparameter.length; c++) {
				query.setParameter(c + 1, listofparameter[c]);
			}
			Integer totalRecords = query.getResultList().size();
			paginationModel.setCurrentPageNo(currentPage);
			paginationModel.setTotalRecords(Long.valueOf(totalRecords));
			query.setFirstResult((currentPage * pageLimit) - pageLimit);
			query.setMaxResults(pageLimit);
			paginationModel.setTotalPages((totalRecords + pageLimit - 1) / pageLimit);
			paginationModel.setPageLimit(pageLimit);
			paginationModel.setPaginationListRecords(query.getResultList());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return paginationModel;
	}

	/***** Get default page limit method *******/

	public int getPageLimit() throws IOException {
		if (environment.getProperty("PAGE_LIMIT") != null)
			return Integer.parseInt(environment.getProperty("PAGE_LIMIT"));
		else
			return 10;
	}
	
	 /*****Set generic class method*******/

	   public void setClazz( Class< T > clazzToSet ) { 
	      this.clazz = clazzToSet;
	   }
	   
		/***** Pagination for Query *******/
	   	public PaginationModel getPaginationWithQueryWithoutPagesCount(PaginationModel paginationModel, Integer currentPage, String hqlquery, Object[] listofparameter){
	   		try {
				Integer pageLimit = getPageLimit();
				Query query = entityManager.createQuery(hqlquery);
				for (int c = 0; c < listofparameter.length; c++) {
					query.setParameter(c + 1, listofparameter[c]);
				}
				paginationModel.setCurrentPageNo(currentPage);
				paginationModel.setTotalRecords(0L);
				query.setFirstResult((currentPage * pageLimit) - pageLimit);
				query.setMaxResults(pageLimit);
				paginationModel.setTotalPages(0);
				paginationModel.setPageLimit(pageLimit);
				paginationModel.setPaginationListRecords( query.getResultList());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return paginationModel;
		}
	   	
	   	
	   	public PaginationModel getPaginationWithQuery50(PaginationModel paginationModel, Integer currentPage, String hqlquery,
				Object[] listofparameter) {
			try {
				Integer pageLimit = 50;
				Query query = entityManager.createQuery(hqlquery);
				for (int c = 0; c < listofparameter.length; c++) {
					query.setParameter(c + 1, listofparameter[c]);
				}
				Integer totalRecords = query.getResultList().size();
				paginationModel.setCurrentPageNo(currentPage);
				paginationModel.setTotalRecords(Long.valueOf(totalRecords));
				query.setFirstResult((currentPage * pageLimit) - pageLimit);
				query.setMaxResults(pageLimit);
				paginationModel.setTotalPages((totalRecords + pageLimit - 1) / pageLimit);
				paginationModel.setPageLimit(pageLimit);
				paginationModel.setPaginationListRecords(query.getResultList());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return paginationModel;
		}
	   	
	   	/***** Pagination for SQL Query *******/
	     @SuppressWarnings("deprecation")
	     public PaginationModel getPaginationWithSQLQueryWithoutPagesCount(PaginationModel paginationModel, Integer currentPage, String sqlquery, Object[] listofparameter){
	             try {
	                     Integer pageLimit = getPageLimit();
	                     Query query = entityManager.createNativeQuery(sqlquery);
	                     query.unwrap( org.hibernate.query.Query.class ).setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);
	                     for (int c = 0; c < listofparameter.length; c++) {
	                             query.setParameter(c + 1, listofparameter[c]);
	                     }
	                     paginationModel.setCurrentPageNo(currentPage);
	                     paginationModel.setTotalRecords(0L);
	                     query.setFirstResult((currentPage * pageLimit) - pageLimit);
	                     query.setMaxResults(pageLimit);
	                     paginationModel.setTotalPages(0);
	                     paginationModel.setPageLimit(pageLimit);
	                     paginationModel.setPaginationListRecords( query.getResultList());
	             } catch (Exception e) {
	                     e.printStackTrace();
	             }
	             return paginationModel;
	     }
	     
	 
	     /***** Pagination for SQL Query *******/
		   	@SuppressWarnings("deprecation")
			public PaginationModel getPaginationWithSQLQuery(PaginationModel paginationModel, Integer currentPage, String sqlquery, Object[] listofparameter){
				try {
					Integer pageLimit = getPageLimit();
					Query query = entityManager.createNativeQuery(sqlquery);
					query.unwrap( org.hibernate.query.Query.class ).setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);
					for (int c = 0; c < listofparameter.length; c++) {
						query.setParameter(c + 1, listofparameter[c]);
					}
					Query countQuery = entityManager.createNativeQuery("select count(*) from ( " + sqlquery + " ) x");
					for (int c = 0; c < listofparameter.length; c++) {
						countQuery.setParameter(c + 1, listofparameter[c]);
					}
					Long totalRecords = ((BigInteger) countQuery.getSingleResult()).longValue();
					Integer totalPages = ((int) (Math.ceil((totalRecords+pageLimit-1)/pageLimit)));
					paginationModel.setCurrentPageNo(currentPage);
					paginationModel.setTotalRecords(Long.valueOf(totalRecords));
					query.setFirstResult((currentPage * pageLimit) - pageLimit);
					query.setMaxResults(pageLimit);
					paginationModel.setTotalPages(totalPages);
					paginationModel.setPageLimit(pageLimit);
					paginationModel.setPaginationListRecords( query.getResultList());
				} catch (Exception e) {
					e.printStackTrace();
				}
				return paginationModel;
			}


}
