package com.library.bean;

public class SeatSelectionDetails {
	private Integer seatSelectionId;
	private Integer userId;
	private Integer planId;
	private Integer seatNo;
	private String status; // booked / available
	private String createdAt;

	public Integer getSeatSelectionId() { return seatSelectionId; }
	public void setSeatSelectionId(Integer seatSelectionId) { this.seatSelectionId = seatSelectionId; }
	public Integer getUserId() { return userId; }
	public void setUserId(Integer userId) { this.userId = userId; }
	public Integer getPlanId() { return planId; }
	public void setPlanId(Integer planId) { this.planId = planId; }
	public Integer getSeatNo() { return seatNo; }
	public void setSeatNo(Integer seatNo) { this.seatNo = seatNo; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	public String getCreatedAt() { return createdAt; }
	public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
