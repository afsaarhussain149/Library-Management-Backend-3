package com.library.services;

import com.library.bean.ShiftSelectionDetails;
import java.util.Map;

public interface ShiftService {
	Map<String, Object> saveShiftSelection(ShiftSelectionDetails details);
	Map<String, Object> getShiftSelectionsByUser(Integer userId);
}
