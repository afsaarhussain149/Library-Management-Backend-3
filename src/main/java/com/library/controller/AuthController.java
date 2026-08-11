package com.library.controller;

import com.library.bean.*;
import com.library.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

// Equivalent of Node's routes/auth.js mounted at /api/auth
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@Autowired
	AuthService authService;

	// POST /adminregister
	@PostMapping(value = "/adminregister", consumes = { "multipart/form-data" })
	public ResponseEntity<ApiResponse> adminRegister(
			@RequestParam(required = false) String name,
			@RequestParam String phone,
			@RequestParam String password,
			@RequestParam(value = "photo", required = false) MultipartFile photo) {

		AdminDetails admin = new AdminDetails();
		admin.setName(name);
		admin.setPhone(phone);
		admin.setPassword(password);

		ApiResponse response = authService.adminRegister(admin, photo);
		HttpStatus status = response.isSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST;
		return ResponseEntity.status(status).body(response);
	}

	// POST /adminlogin
	@PostMapping("/adminlogin")
	public ResponseEntity<Map<String, Object>> adminLogin(@RequestBody Map<String, String> body) {
		Map<String, Object> result = authService.adminLogin(body.get("phone"), body.get("password"));
		int httpStatus = (int) result.remove("httpStatus");
		return ResponseEntity.status(httpStatus).body(result);
	}

	// PUT /admin-edit-profile
	@PutMapping(value = "/admin-edit-profile", consumes = { "multipart/form-data" })
	public ResponseEntity<ApiResponse> adminEditProfile(
			@RequestParam String phone,
			@RequestParam(required = false) String name,
			@RequestParam(required = false) String newPhone,
			@RequestParam(required = false) String password,
			@RequestParam(value = "photo", required = false) MultipartFile photo) {

		ApiResponse response = authService.adminEditProfile(phone, name, newPhone, password, photo);
		HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.NOT_FOUND;
		return ResponseEntity.status(status).body(response);
	}

	// POST /register
	@PostMapping("/register")
	public ResponseEntity<ApiResponse> register(@RequestBody UserDetails userDetails) {
		ApiResponse response = authService.register(userDetails);
		HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
		return ResponseEntity.status(status).body(response);
	}

	// POST /forgot-password
	@PostMapping("/forgot-password")
	public ResponseEntity<ApiResponse> forgotPassword(
	        @RequestBody ForgotPasswordRequest request) {

	    ApiResponse response =
	            authService.forgotPassword(request);

	    return ResponseEntity.ok(response);
	}
	
	// POST /verify-otp
	@PostMapping("/verify-otp")
	public ResponseEntity<ApiResponse> verifyOtp(
	        @RequestBody VerifyOtpRequest request) {

	    ApiResponse response =
	            authService.verifyOtp(request);

	    HttpStatus status =
	            response.isSuccess()
	                    ? HttpStatus.OK
	                    : HttpStatus.BAD_REQUEST;

	    return ResponseEntity
	            .status(status)
	            .body(response);
	}
	
	// POST /reset-password
	@PostMapping("/reset-password")
	public ResponseEntity<ApiResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
		ApiResponse response = authService.resetPassword(request);
		HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
		return ResponseEntity.status(status).body(response);
	}

	// POST /unpaid-user
	@PostMapping("/unpaid-user")
	public ResponseEntity<Map<String, Object>> unpaidUser(@RequestBody Map<String, String> body) {
		Map<String, Object> result = authService.unpaidUser(body.get("phoneNumber"));
		int httpStatus = (int) result.remove("httpStatus");
		return ResponseEntity.status(httpStatus).body(result);
	}

	// POST /login
	@PostMapping("/login")
	public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body) {
		Map<String, Object> result = authService.login(body.get("phoneNumber"), body.get("password"));
		int httpStatus = (int) result.remove("httpStatus");
		return ResponseEntity.status(httpStatus).body(result);
	}

	// POST /register-basic
	@PostMapping("/register-basic")
	public ResponseEntity<ApiResponse> registerBasic(@RequestBody UserDetails userDetails) {
		ApiResponse response = authService.registerBasic(userDetails);
		HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
		return ResponseEntity.status(status).body(response);
	}

	// POST /complete-profile
	@PostMapping(value = "/complete-profile", consumes = { "multipart/form-data" })
	public ResponseEntity<ApiResponse> completeProfile(
			@RequestParam String phoneNumber,
			@RequestParam Map<String, String> allParams,
			@RequestParam(value = "photo", required = false) MultipartFile photo) {

		Map<String, Object> fields = new HashMap<>(allParams);
		fields.remove("phoneNumber");
		fields.remove("photo");

		ApiResponse response = authService.completeProfile(phoneNumber, fields, photo);
		HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.NOT_FOUND;
		return ResponseEntity.status(status).body(response);
	}

	// PUT /user/edit-profile
	@PutMapping(value = "/user/edit-profile", consumes = { "multipart/form-data" })
	public ResponseEntity<ApiResponse> editProfile(
			@RequestParam Integer userId,
			@RequestParam Map<String, String> allParams,
			@RequestParam(value = "photo", required = false) MultipartFile photo) {

		Map<String, Object> fields = new HashMap<>(allParams);
		fields.remove("userId");
		fields.remove("photo");

		ApiResponse response = authService.editProfile(userId, fields, photo);
		HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.NOT_FOUND;
		return ResponseEntity.status(status).body(response);
	}
	
	@GetMapping("/all-users")
	public ResponseEntity<Map<String, Object>> getAllUsers(
			@RequestParam(required = false) String fullName,
			@RequestParam(required = false) String phone,
			@RequestParam(required = false) String userId) {

		Map<String, Object> result = authService.getAllUsers(fullName, phone, userId);
		return ResponseEntity.ok(result);
	}
}
