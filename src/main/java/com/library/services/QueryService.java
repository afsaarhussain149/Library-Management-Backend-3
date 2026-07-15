package com.library.services;

import com.library.bean.ApiResponse;
import com.library.bean.QueryDetails;
import java.util.List;
import java.util.Map;

public interface QueryService {
	List<Map> getAllQueries();
	ApiResponse addQuery(QueryDetails details);
	ApiResponse deleteQuery(Integer id);
}
