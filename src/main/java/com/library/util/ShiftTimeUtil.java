package com.library.util;

/**
 * Equivalent of Node's parseShift()/isOverlap() helper functions used in
 * routes/payment.js for detecting seat/shift-time clashes.
 */
public class ShiftTimeUtil {

	public static class Range {
		public final int start;
		public final int end;
		public Range(int start, int end) { this.start = start; this.end = end; }
	}

	/** Parses strings like "8 AM - 12 PM" into minute-of-day start/end. */
	public static Range parseShift(String shiftStr) {
		String[] parts = shiftStr.split("-");
		String start = parts[0].trim();
		String end = parts[1].trim();
		return new Range(toMinutes(start), toMinutes(end));
	}

	private static int toMinutes(String time) {
		String[] tokens = time.trim().split(" ");
		int hour = Integer.parseInt(tokens[0]);
		String period = tokens.length > 1 ? tokens[1] : "";
		if ("PM".equalsIgnoreCase(period) && hour != 12) hour += 12;
		if ("AM".equalsIgnoreCase(period) && hour == 12) hour = 0;
		return hour * 60;
	}

	public static boolean isOverlap(Range a, Range b) {
		return a.start < b.end && b.start < a.end;
	}
}
