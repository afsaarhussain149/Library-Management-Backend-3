package com.library.bean;

public class ShiftSelectionDetails {
	private Integer shiftSelectionId;
	private Integer userId;
	private Integer planId;
	private String shiftLabel;
	private String shiftTime;
	private String createdAt;

	public Integer getShiftSelectionId() { return shiftSelectionId; }
	public void setShiftSelectionId(Integer shiftSelectionId) { this.shiftSelectionId = shiftSelectionId; }
	public Integer getUserId() { return userId; }
	public void setUserId(Integer userId) { this.userId = userId; }
	public Integer getPlanId() { return planId; }
	public void setPlanId(Integer planId) { this.planId = planId; }
	public String getShiftLabel() { return shiftLabel; }
	public void setShiftLabel(String shiftLabel) { this.shiftLabel = shiftLabel; }
	public String getShiftTime() { return shiftTime; }
	public void setShiftTime(String shiftTime) { this.shiftTime = shiftTime; }
	public String getCreatedAt() { return createdAt; }
	public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
