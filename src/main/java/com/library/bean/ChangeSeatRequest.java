package com.library.bean;

import java.util.List;

public class ChangeSeatRequest {

	private Integer userId;
	private List<Integer> newSeats;

	// Optional: admin can also change the shift/time along with the seat.
	// If left null, the student's existing shiftLabel/shiftTime is kept as-is.
	private String shiftLabel;
	private String shiftTime;

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

	public String getShiftLabel() {
		return shiftLabel;
	}

	public void setShiftLabel(String shiftLabel) {
		this.shiftLabel = shiftLabel;
	}

	public String getShiftTime() {
		return shiftTime;
	}

	public void setShiftTime(String shiftTime) {
		this.shiftTime = shiftTime;
	}
}