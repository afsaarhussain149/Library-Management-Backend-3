package com.library.bean;

public class ComplaintDetails {
	private Integer complaintId;
	private Integer userId;
	private String message;
	private String issueType;
	private String status;
	private String createdAt;

	public Integer getComplaintId() { return complaintId; }
	public void setComplaintId(Integer complaintId) { this.complaintId = complaintId; }
	public Integer getUserId() { return userId; }
	public void setUserId(Integer userId) { this.userId = userId; }
	public String getMessage() { return message; }
	public void setMessage(String message) { this.message = message; }
	public String getIssueType() { return issueType; }
	public void setIssueType(String issueType) { this.issueType = issueType; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	public String getCreatedAt() { return createdAt; }
	public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
