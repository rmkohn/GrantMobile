package com.example.grantmobile.CalendarSquare;

public class PlaceholderSquare implements ICalendarSquare {
	String message;

	public PlaceholderSquare(String message) {
		this.message = message;
	}

	@Override
	public String getMessage() {
		return message;
	}
}
