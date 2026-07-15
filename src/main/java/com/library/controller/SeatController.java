package com.library.controller;

import com.library.bean.SeatSelectionDetails;
import com.library.services.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// Equivalent of Node's routes/seat.js, mounted at both /api/seats and /api/seat-selection
@RestController
@RequestMapping({ "/api/seats", "/api/seat-selection" })
public class SeatController {

	@Autowired
	SeatService seatService;

	@PostMapping
	public ResponseEntity<Map<String, Object>> save(@RequestBody SeatSelectionDetails details) {
		Map<String, Object> result = seatService.saveSeatSelection(details);
		int httpStatus = (int) result.remove("httpStatus");
		return ResponseEntity.status(httpStatus).body(result);
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<Map<String, Object>> byUser(@PathVariable Integer userId) {
		return ResponseEntity.ok(seatService.getSeatSelectionsByUser(userId));
	}

	@GetMapping("/plan/{planId}")
	public ResponseEntity<Map<String, Object>> byPlan(@PathVariable Integer planId) {
		return ResponseEntity.ok(seatService.getBookedSeatsByPlan(planId));
	}
}
