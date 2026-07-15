package com.library.controller;

import com.library.bean.UserSelectionDetails;
import com.library.services.SelectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// Equivalent of Node's routes/selection.js mounted at /api/selection
@RestController
@RequestMapping("/api/selection")
public class SelectionController {

	@Autowired
	SelectionService selectionService;

	@PostMapping
	public ResponseEntity<Map<String, Object>> save(@RequestBody UserSelectionDetails details) {
		Map<String, Object> result = selectionService.saveSelection(details);
		int httpStatus = (int) result.remove("httpStatus");
		return ResponseEntity.status(httpStatus).body(result);
	}

	@GetMapping("/{userId}")
	public ResponseEntity<Map<String, Object>> byUser(@PathVariable Integer userId) {
		Map<String, Object> result = selectionService.getSelectionsByUser(userId);
		int httpStatus = (int) result.remove("httpStatus");
		return ResponseEntity.status(httpStatus).body(result);
	}
}
