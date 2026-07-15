package com.library.controller;

import com.library.bean.ApiResponse;
import com.library.bean.ComplaintDetails;
import com.library.services.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Equivalent of Node's routes/complaint.js mounted at /api/complaint
@RestController
@RequestMapping("/api/complaint")
public class ComplaintController {

	@Autowired
	ComplaintService complaintService;

	private Map<String, Object> wrap(List<Map> data) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("success", true);
		result.put("count", data.size());
		result.put("data", data);
		return result;
	}

	@GetMapping("/complaints")
	public Map<String, Object> getComplaints() {
		return wrap(complaintService.getAllComplaints());
	}

	@GetMapping("/get-complaints")
	public Map<String, Object> getComplaintsAdmin() {
		return wrap(complaintService.getAllComplaints());
	}

	@PostMapping("/add-complaint")
	public ResponseEntity<ApiResponse> addComplaint(@RequestBody ComplaintDetails details) {
		ApiResponse response = complaintService.addComplaint(details);
		HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
		return ResponseEntity.status(status).body(response);
	}

	@DeleteMapping("/complaints/{id}")
	public ResponseEntity<ApiResponse> deleteComplaint(@PathVariable Integer id) {
		ApiResponse response = complaintService.deleteComplaint(id);
		HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.NOT_FOUND;
		return ResponseEntity.status(status).body(response);
	}

	@GetMapping("/get-complaints/{userId}")
	public ResponseEntity<Map<String, Object>> getComplaintsByUser(@PathVariable Integer userId) {
		return ResponseEntity.ok(complaintService.getComplaintsByUser(userId));
	}
}
