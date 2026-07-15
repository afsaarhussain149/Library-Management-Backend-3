package com.library.servicesImpl;

import com.library.bean.JavaConstant;
import com.library.bean.SeatSelectionDetails;
import com.library.dao.IGenericDao;
import com.library.services.SeatService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@SuppressWarnings({ "rawtypes" })
public class SeatServiceImpl implements SeatService {

	@Autowired
	IGenericDao iGenericDao;

	// ============ POST / (save seat selection) ============
	@Override
	@Transactional
	public Map<String, Object> saveSeatSelection(SeatSelectionDetails d) {
		Map<String, Object> result = new LinkedHashMap<>();
		try {
			if (d.getUserId() == null || d.getPlanId() == null || d.getSeatNo() == null) {
				result.put("httpStatus", 400);
				result.put("message", "All fields are required.");
				return result;
			}

			List<Map> existing = iGenericDao.executeDDLSQL(JavaConstant.CHECK_SEAT_TAKEN_FOR_PLAN,
					new Object[] { d.getPlanId(), d.getSeatNo() });
			if (existing != null && !existing.isEmpty()) {
				result.put("httpStatus", 400);
				result.put("message", "Seat already booked.");
				return result;
			}

			iGenericDao.executeDMLSQL(JavaConstant.INSERT_SEAT_SELECTION,
					new Object[] { d.getUserId(), d.getPlanId(), d.getSeatNo() });

			List<Map> inserted = iGenericDao.executeDDLSQL(JavaConstant.CHECK_SEAT_TAKEN_FOR_PLAN,
					new Object[] { d.getPlanId(), d.getSeatNo() });

			result.put("httpStatus", 201);
			result.put("message", "Seat booked successfully.");
			result.put("data", inserted != null && !inserted.isEmpty() ? inserted.get(0) : null);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.put("httpStatus", 500);
			result.put("message", e.getMessage());
			return result;
		}
	}

	// ============ GET /user/:userId ============
	@Override
	public Map<String, Object> getSeatSelectionsByUser(Integer userId) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("data", iGenericDao.executeDDLSQL(JavaConstant.GET_SEAT_SELECTIONS_BY_USER, new Object[] { userId }));
		return result;
	}

	// ============ GET /plan/:planId ============
	@Override
	public Map<String, Object> getBookedSeatsByPlan(Integer planId) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("data", iGenericDao.executeDDLSQL(JavaConstant.GET_BOOKED_SEATS_BY_PLAN, new Object[] { planId }));
		return result;
	}
}
