package com.library.servicesImpl;

import com.library.bean.ApiResponse;
import com.library.bean.JavaConstant;
import com.library.bean.QueryDetails;
import com.library.dao.IGenericDao;
import com.library.services.QueryService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@SuppressWarnings({ "rawtypes" })
public class QueryServiceImpl implements QueryService {

	@Autowired
	IGenericDao iGenericDao;

	// ============ GET / ============
	@Override
	public List<Map> getAllQueries() {
		return iGenericDao.executeDDLSQL(JavaConstant.GET_ALL_PUBLIC_QUERY, new Object[] {});
	}

	// ============ POST / ============
	@Override
	@Transactional
	public ApiResponse addQuery(QueryDetails d) {
		try {
			if (d.getName() == null || d.getMail() == null || d.getSubject() == null || d.getMessage() == null) {
				return new ApiResponse(false, "All fields are required");
			}
			iGenericDao.executeDMLSQL(JavaConstant.INSERT_PUBLIC_QUERY,
					new Object[] { d.getName(), d.getMail(), d.getSubject(), d.getMessage() });
			return new ApiResponse(true, "Query submitted successfully");
		} catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse(false, "Server error");
		}
	}

	// ============ DELETE /:id ============
	@Override
	@Transactional
	public ApiResponse deleteQuery(Integer id) {
		try {
			List<Map> existing = iGenericDao.executeDDLSQL(JavaConstant.GET_PUBLIC_QUERY_BY_ID, new Object[] { id });
			if (existing == null || existing.isEmpty()) {
				return new ApiResponse(false, "Query not found");
			}
			iGenericDao.executeDMLSQL(JavaConstant.DELETE_PUBLIC_QUERY, new Object[] { id });
			return new ApiResponse(true, "Query deleted successfully");
		} catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse(false, "Server Error");
		}
	}
}
