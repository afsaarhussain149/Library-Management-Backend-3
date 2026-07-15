package com.library.controller;

import com.library.services.PlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// Equivalent of Node's routes/plan.js mounted at /api/plans
@RestController
@RequestMapping("/api/plans")
public class PlanController {

	@Autowired
	PlanService planService;

	@GetMapping
	public List<Map<String, Object>> getPlans() {
		return planService.getPlans();
	}
}
