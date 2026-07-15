package com.library.controller;

import com.library.bean.ApiResponse;
import com.library.bean.QueryDetails;
import com.library.services.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Equivalent of Node's routes/query.js mounted at /api/query
@RestController
@RequestMapping("/api/query")
public class QueryController {

	@Autowired
	QueryService queryService;

	@GetMapping
	public Map<String, Object> getAll() {
		List<Map> data = queryService.getAllQueries();
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("success", true);
		result.put("data", data);
		return result;
	}

	@PostMapping
	public ResponseEntity<ApiResponse> add(@RequestBody QueryDetails details) {
		ApiResponse response = queryService.addQuery(details);
		HttpStatus status = response.isSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST;
		return ResponseEntity.status(status).body(response);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse> delete(@PathVariable Integer id) {
		ApiResponse response = queryService.deleteQuery(id);
		HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.NOT_FOUND;
		return ResponseEntity.status(status).body(response);
	}
}
