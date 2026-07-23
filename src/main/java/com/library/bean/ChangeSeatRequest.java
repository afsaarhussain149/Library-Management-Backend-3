package com.library.bean;

import java.util.List;

public class ChangeSeatRequest {

	private Integer userId;
	private List<Integer> newSeats;

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public List<Integer> getNewSeats() {
		return newSeats;
	}

	public void setNewSeats(List<Integer> newSeats) {
		this.newSeats = newSeats;
	}
}