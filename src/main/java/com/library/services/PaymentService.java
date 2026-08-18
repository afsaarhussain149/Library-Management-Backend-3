package com.library.services;

import com.library.bean.ChangeSeatRequest;
import com.library.bean.PaymentDetails;
import com.library.bean.UpdateStatusRequest;
import com.library.bean.VerifyPaymentRequest;

import java.util.Map;

public interface PaymentService {

	Map<String, Object> pendingCashUsers(int page);

	Map<String, Object> usersWithPaymentsCount();

	Map<String, Object> cashRequest(PaymentDetails paymentDetails);

	Map<String, Object> approveCashPayment(String userId);

	Map<String, Object> pendingCash();

	Map<String, Object> activeInactiveCount();

	Map<String, Object> updateStatus(UpdateStatusRequest request);

	Map<String, Object> usersWithPayments(int page, String userId, String fullName, String phone, String status);

	Map<String, Object> createOrder(PaymentDetails paymentDetails);

	Map<String, Object> usersWithPaymentsAll(String userId, String fullName, String phone, String status);

	Map<String, Object> usersStatusCount();

	Map<String, Object> verifyPayment(VerifyPaymentRequest request);

	Map<String, Object> paymentsByUser(String userId);

	Map<String, Object> checkSeat(Integer seatNo, String shift, Integer excludeUserId);

	Map<String, Object> allPayments(int page);

	Map<String, Object> addSeatBlockFlag();

	Map<String, Object> setAllActive();

	Map<String, Object> seatsStatus(String shift);
	
	Map<String, Object> changeSeat(ChangeSeatRequest request);
	
	Map<String, Object> rejectCashPayment(String userId);
	
	Map<String, Object> seatDetails(String studentName, String phone, String seatNo, String month);
	
	Map<String, Object> feeRecords(String studentName, String phone, String paymentMode, String month);
	
	Map<String, Object> seatsOverview();
}
