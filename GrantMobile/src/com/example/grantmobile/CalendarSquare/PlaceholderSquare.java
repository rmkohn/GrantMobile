package com.example.grantmobile.CalendarSquare;

public class PlaceholderSquare implements ICalendarSquare {

	public PlaceholderSquare() { }

	@Override
	public String getMessage() {
		return "";
	}

	@Override
	public int getHighlightColor() {
		return SquareColors.DEFAULT_COLOR;
	}
}
