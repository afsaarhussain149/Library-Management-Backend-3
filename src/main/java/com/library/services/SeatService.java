package com.library.services;

import com.library.bean.SeatSelectionDetails;
import java.util.Map;

public interface SeatService {
	Map<String, Object> saveSeatSelection(SeatSelectionDetails details);
	Map<String, Object> getSeatSelectionsByUser(Integer userId);
	Map<String, Object> getBookedSeatsByPlan(Integer planId);
}
