package com.library.bean;

public class Status {

	private String StatusCode;
	private String GeneratedCode;

	public Status() {
	}

	public Status(String StatusCode, String GeneratedCode) {
		this.StatusCode = StatusCode;
		this.GeneratedCode = GeneratedCode;
	}

	public String getStatusCode() {
		return StatusCode;
	}

	public void setStatusCode(String StatusCode) {
		this.StatusCode = StatusCode;
	}

	public String getGeneratedCode() {
		return GeneratedCode;
	}

	public void setGeneratedCode(String GeneratedCode) {
		this.GeneratedCode = GeneratedCode;
	}

}
