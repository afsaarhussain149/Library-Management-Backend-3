package com.library.bean;

public class QueryDetails {
	private Integer queryId;
	private String name;
	private String mail;
	private String subject;
	private String message;
	private String createdAt;

	public Integer getQueryId() { return queryId; }
	public void setQueryId(Integer queryId) { this.queryId = queryId; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getMail() { return mail; }
	public void setMail(String mail) { this.mail = mail; }
	public String getSubject() { return subject; }
	public void setSubject(String subject) { this.subject = subject; }
	public String getMessage() { return message; }
	public void setMessage(String message) { this.message = message; }
	public String getCreatedAt() { return createdAt; }
	public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
