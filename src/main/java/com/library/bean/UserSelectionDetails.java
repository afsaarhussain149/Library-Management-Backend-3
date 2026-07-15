package com.library.bean;

public class UserSelectionDetails {
	private Integer selectionId;
	private Integer userId;
	private String fullName;
	private String email;
	private Integer planHours;
	private String selectedOption;
	private Double price;
	private String createdAt;

	public Integer getSelectionId() { return selectionId; }
	public void setSelectionId(Integer selectionId) { this.selectionId = selectionId; }
	public Integer getUserId() { return userId; }
	public void setUserId(Integer userId) { this.userId = userId; }
	public String getFullName() { return fullName; }
	public void setFullName(String fullName) { this.fullName = fullName; }
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	public Integer getPlanHours() { return planHours; }
	public void setPlanHours(Integer planHours) { this.planHours = planHours; }
	public String getSelectedOption() { return selectedOption; }
	public void setSelectedOption(String selectedOption) { this.selectedOption = selectedOption; }
	public Double getPrice() { return price; }
	public void setPrice(Double price) { this.price = price; }
	public String getCreatedAt() { return createdAt; }
	public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
