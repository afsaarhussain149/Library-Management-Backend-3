package com.library.servicesImpl;

import com.library.bean.JavaConstant;
import com.library.bean.UserSelectionDetails;
import com.library.dao.IGenericDao;
import com.library.services.SelectionService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@SuppressWarnings({ "rawtypes" })
public class SelectionServiceImpl implements SelectionService {

	@Autowired
	IGenericDao iGenericDao;

	// ============ POST / (save plan selection) ============
	@Override
	@Transactional
	public Map<String, Object> saveSelection(UserSelectionDetails d) {
		Map<String, Object> result = new LinkedHashMap<>();
		try {
			if (d.getSelectedOption() == null) {
				result.put("httpStatus", 400);
				result.put("message", "Please select a plan and option.");
				return result;
			}

			iGenericDao.executeDMLSQL(JavaConstant.INSERT_USER_SELECTION, new Object[] {
					d.getUserId(), d.getFullName(), d.getEmail(), d.getPlanHours(), d.getSelectedOption(), d.getPrice()
			});

			result.put("httpStatus", 201);
			result.put("message", "Plan selected successfully");
			result.put("userId", d.getUserId());
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.put("httpStatus", 500);
			result.put("message", e.getMessage());
			return result;
		}
	}

	// ============ GET /:userId ============
	@Override
	public Map<String, Object> getSelectionsByUser(Integer userId) {
		Map<String, Object> result = new LinkedHashMap<>();
		List<Map> selections = iGenericDao.executeDDLSQL(JavaConstant.GET_USER_SELECTIONS_BY_USER, new Object[] { userId });

		if (selections == null || selections.isEmpty()) {
			result.put("httpStatus", 404);
			result.put("message", "No plans found for this user.");
			return result;
		}

		result.put("httpStatus", 200);
		result.put("userId", userId);
		result.put("totalPlans", selections.size());
		result.put("plans", selections);
		return result;
	}
}
