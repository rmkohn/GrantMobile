package com.example.grantmobile.CalendarSquare;

public class PlaceholderSquare implements ICalendarSquare {
	String message;
	int x, y;

	public PlaceholderSquare(String message, int x, int y) {
		this.message = message;
		this.x = x;
		this.y = y;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

}
