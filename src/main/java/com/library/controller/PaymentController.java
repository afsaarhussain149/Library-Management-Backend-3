package com.library.controller;

import com.library.bean.ChangeSeatRequest;
import com.library.bean.PaymentDetails;
import com.library.bean.UpdateStatusRequest;
import com.library.bean.VerifyPaymentRequest;
import com.library.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// Equivalent of Node's routes/payment.js mounted at /api/payments
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

	@Autowired
	PaymentService paymentService;

	@GetMapping("/pending-cash-users/{page}")
	public ResponseEntity<Map<String, Object>> pendingCashUsers(@PathVariable int page) {
		return ResponseEntity.ok(paymentService.pendingCashUsers(page));
	}

	@GetMapping("/users-with-payments/count")
	public ResponseEntity<Map<String, Object>> usersWithPaymentsCount() {
		return ResponseEntity.ok(paymentService.usersWithPaymentsCount());
	}

	@PostMapping("/cash-request")
	public ResponseEntity<Map<String, Object>> cashRequest(@RequestBody PaymentDetails paymentDetails) {
		return ResponseEntity.ok(paymentService.cashRequest(paymentDetails));
	}

	@PatchMapping("/approve/{id}")
	public ResponseEntity<Map<String, Object>> approve(@PathVariable String id) {
		return ResponseEntity.ok(paymentService.approveCashPayment(id));
	}

	@GetMapping("/pending-cash")
	public ResponseEntity<Map<String, Object>> pendingCash() {
		return ResponseEntity.ok(paymentService.pendingCash());
	}

	@GetMapping("/users/active-inactive-count")
	public ResponseEntity<Map<String, Object>> activeInactiveCount() {
		return ResponseEntity.ok(paymentService.activeInactiveCount());
	}

	@PutMapping("/update-status")
	public ResponseEntity<Map<String, Object>> updateStatus(@RequestBody UpdateStatusRequest request) {
		return ResponseEntity.ok(paymentService.updateStatus(request));
	}

	@GetMapping("/users-with-payments/{page}")
	public ResponseEntity<Map<String, Object>> usersWithPayments(
			@PathVariable int page,
			@RequestParam(required = false) String userId,
			@RequestParam(required = false) String fullName,
			@RequestParam(required = false) String phone,
			@RequestParam(required = false) String status) {
		return ResponseEntity.ok(paymentService.usersWithPayments(page, userId, fullName, phone, status));
	}

	@PostMapping("/create-order")
	public ResponseEntity<Map<String, Object>> createOrder(@RequestBody PaymentDetails paymentDetails) {
		Map<String, Object> result = paymentService.createOrder(paymentDetails);
		int httpStatus = (int) result.remove("httpStatus");
		return ResponseEntity.status(httpStatus).body(result);
	}

	@GetMapping("/users-with-payments-all")
	public ResponseEntity<Map<String, Object>> usersWithPaymentsAll(
			@RequestParam(required = false) String userId,
			@RequestParam(required = false) String fullName,
			@RequestParam(required = false) String phone,
			@RequestParam(required = false) String status) {
		return ResponseEntity.ok(paymentService.usersWithPaymentsAll(userId, fullName, phone, status));
	}

	@GetMapping("/users-status-count")
	public ResponseEntity<Map<String, Object>> usersStatusCount() {
		return ResponseEntity.ok(paymentService.usersStatusCount());
	}

	@PostMapping("/verify")
	public ResponseEntity<Map<String, Object>> verify(@RequestBody VerifyPaymentRequest request) {
		Map<String, Object> result = paymentService.verifyPayment(request);
		int httpStatus = (int) result.remove("httpStatus");
		return ResponseEntity.status(httpStatus).body(result);
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<Map<String, Object>> paymentsByUser(@PathVariable String userId) {
		return ResponseEntity.ok(paymentService.paymentsByUser(userId));
	}

	@GetMapping("/seat/check")
	public ResponseEntity<Map<String, Object>> checkSeat(@RequestParam Integer seatNo, @RequestParam String shift,
			@RequestParam(required = false) Integer excludeUserId) {
		return ResponseEntity.ok(paymentService.checkSeat(seatNo, shift, excludeUserId));
	}

	@GetMapping("/all/{page}")
	public ResponseEntity<Map<String, Object>> allPayments(@PathVariable int page) {
		return ResponseEntity.ok(paymentService.allPayments(page));
	}

	@PutMapping("/add-seat-block-flag")
	public ResponseEntity<Map<String, Object>> addSeatBlockFlag() {
		return ResponseEntity.ok(paymentService.addSeatBlockFlag());
	}

	@PutMapping("/set-active")
	public ResponseEntity<Map<String, Object>> setActive() {
		return ResponseEntity.ok(paymentService.setAllActive());
	}

	@GetMapping("/seats/status")
	public ResponseEntity<Map<String, Object>> seatsStatus(@RequestParam String shift) {
		Map<String, Object> result = paymentService.seatsStatus(shift);
		int httpStatus = (int) result.remove("httpStatus");
		return ResponseEntity.status(httpStatus).body(result);
	}
	
	// Admin reassigns a student's booked seat(s) to a new seat number
	@PutMapping("/change-seat")
	public ResponseEntity<Map<String, Object>> changeSeat(@RequestBody ChangeSeatRequest request) {
		Map<String, Object> result = paymentService.changeSeat(request);
		int httpStatus = (int) result.remove("httpStatus");
		return ResponseEntity.status(httpStatus).body(result);
	}
	
	@PatchMapping("/reject/{id}")
	   public ResponseEntity<Map<String, Object>> reject(@PathVariable String id) {
	       return ResponseEntity.ok(paymentService.rejectCashPayment(id));
	   }
}
