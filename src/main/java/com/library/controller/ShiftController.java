package com.library.controller;

import com.library.bean.ShiftSelectionDetails;
import com.library.services.ShiftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// Equivalent of Node's routes/shift.js mounted at /api/shift-selection
@RestController
@RequestMapping("/api/shift-selection")
public class ShiftController {

	@Autowired
	ShiftService shiftService;

	@PostMapping
	public ResponseEntity<Map<String, Object>> save(@RequestBody ShiftSelectionDetails details) {
		Map<String, Object> result = shiftService.saveShiftSelection(details);
		int httpStatus = (int) result.remove("httpStatus");
		return ResponseEntity.status(httpStatus).body(result);
	}

	@GetMapping("/{userId}")
	public ResponseEntity<Map<String, Object>> byUser(@PathVariable Integer userId) {
		return ResponseEntity.ok(shiftService.getShiftSelectionsByUser(userId));
	}
}
