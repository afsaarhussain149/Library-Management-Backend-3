package com.library.bean;

import java.util.List;
import java.util.Map;

public class PaymentDetails {

	private Integer paymentId;
	private String userId;
	private Double amount;
	private String currency;
	private Boolean isActive;
	private Boolean planExpireSeatBlock;
	private String paymentMode;      // online / cash

	// plan (flattened from Node's embedded plan {hours, type, amount})
	private Integer planHours;
	private String planType;
	private Double planAmount;

	// shift (flattened from Node's embedded shift {label, time})
	private String shiftLabel;
	private String shiftTime;

	private List<Integer> seats;
	private String endPlanDate;

	private String razorpayOrderId;
	private String razorpayPaymentId;
	private String razorpaySignature;

	private String status;           // created / paid / pending / failed
	private Boolean isApprovedByAdmin;
	private Map<String, Object> metadata;

	private String createdAt;

	public Integer getPaymentId() { return paymentId; }
	public void setPaymentId(Integer paymentId) { this.paymentId = paymentId; }
	public String getUserId() { return userId; }
	public void setUserId(String userId) { this.userId = userId; }
	public Double getAmount() { return amount; }
	public void setAmount(Double amount) { this.amount = amount; }
	public String getCurrency() { return currency; }
	public void setCurrency(String currency) { this.currency = currency; }
	public Boolean getIsActive() { return isActive; }
	public void setIsActive(Boolean isActive) { this.isActive = isActive; }
	public Boolean getPlanExpireSeatBlock() { return planExpireSeatBlock; }
	public void setPlanExpireSeatBlock(Boolean planExpireSeatBlock) { this.planExpireSeatBlock = planExpireSeatBlock; }
	public String getPaymentMode() { return paymentMode; }
	public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }
	public Integer getPlanHours() { return planHours; }
	public void setPlanHours(Integer planHours) { this.planHours = planHours; }
	public String getPlanType() { return planType; }
	public void setPlanType(String planType) { this.planType = planType; }
	public Double getPlanAmount() { return planAmount; }
	public void setPlanAmount(Double planAmount) { this.planAmount = planAmount; }
	public String getShiftLabel() { return shiftLabel; }
	public void setShiftLabel(String shiftLabel) { this.shiftLabel = shiftLabel; }
	public String getShiftTime() { return shiftTime; }
	public void setShiftTime(String shiftTime) { this.shiftTime = shiftTime; }
	public List<Integer> getSeats() { return seats; }
	public void setSeats(List<Integer> seats) { this.seats = seats; }
	public String getEndPlanDate() { return endPlanDate; }
	public void setEndPlanDate(String endPlanDate) { this.endPlanDate = endPlanDate; }
	public String getRazorpayOrderId() { return razorpayOrderId; }
	public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }
	public String getRazorpayPaymentId() { return razorpayPaymentId; }
	public void setRazorpayPaymentId(String razorpayPaymentId) { this.razorpayPaymentId = razorpayPaymentId; }
	public String getRazorpaySignature() { return razorpaySignature; }
	public void setRazorpaySignature(String razorpaySignature) { this.razorpaySignature = razorpaySignature; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	public Boolean getIsApprovedByAdmin() { return isApprovedByAdmin; }
	public void setIsApprovedByAdmin(Boolean isApprovedByAdmin) { this.isApprovedByAdmin = isApprovedByAdmin; }
	public Map<String, Object> getMetadata() { return metadata; }
	public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
	public String getCreatedAt() { return createdAt; }
	public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
