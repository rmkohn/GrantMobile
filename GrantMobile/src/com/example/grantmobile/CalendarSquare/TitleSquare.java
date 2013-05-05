package com.example.grantmobile.CalendarSquare;

public class TitleSquare implements ICalendarSquare {
	String message;

	public TitleSquare(String message) {
		this.message = message;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public int getHighlightColor() {
		return SquareColors.DEFAULT_COLOR;
	}
}
