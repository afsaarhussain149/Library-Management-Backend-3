package com.library.bean;

public class VerifyPaymentRequest {
	private String razorpayOrderId;
	private String razorpayPaymentId;
	private String razorpaySignature;
	private Integer paymentId;

	public String getRazorpayOrderId() { return razorpayOrderId; }
	public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }
	public String getRazorpayPaymentId() { return razorpayPaymentId; }
	public void setRazorpayPaymentId(String razorpayPaymentId) { this.razorpayPaymentId = razorpayPaymentId; }
	public String getRazorpaySignature() { return razorpaySignature; }
	public void setRazorpaySignature(String razorpaySignature) { this.razorpaySignature = razorpaySignature; }
	public Integer getPaymentId() { return paymentId; }
	public void setPaymentId(Integer paymentId) { this.paymentId = paymentId; }
}
