package com.library.servicesImpl;

import com.library.bean.JavaConstant;
import com.library.bean.ShiftSelectionDetails;
import com.library.dao.IGenericDao;
import com.library.services.ShiftService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ShiftServiceImpl implements ShiftService {

	@Autowired
	IGenericDao iGenericDao;

	// ============ POST / (save shift selection) ============
	@Override
	@Transactional
	public Map<String, Object> saveShiftSelection(ShiftSelectionDetails d) {
		Map<String, Object> result = new LinkedHashMap<>();
		try {
			if (d.getUserId() == null || d.getPlanId() == null || d.getShiftTime() == null || d.getShiftLabel() == null) {
				result.put("httpStatus", 400);
				result.put("message", "All fields are required.");
				return result;
			}

			iGenericDao.executeDMLSQL(JavaConstant.INSERT_SHIFT_SELECTION,
					new Object[] { d.getUserId(), d.getPlanId(), d.getShiftLabel(), d.getShiftTime() });

			result.put("httpStatus", 201);
			result.put("message", "Shift selection saved successfully");
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
	public Map<String, Object> getShiftSelectionsByUser(Integer userId) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("data", iGenericDao.executeDDLSQL(JavaConstant.GET_SHIFT_SELECTIONS_BY_USER, new Object[] { userId }));
		return result;
	}
}
