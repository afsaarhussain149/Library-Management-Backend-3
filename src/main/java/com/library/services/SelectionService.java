package com.library.services;

import com.library.bean.UserSelectionDetails;
import java.util.Map;

public interface SelectionService {
	Map<String, Object> saveSelection(UserSelectionDetails details);
	Map<String, Object> getSelectionsByUser(Integer userId);
}
