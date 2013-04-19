package com.example.grantmobile.CalendarSquare;

public class WrapperSquare implements ICalendarSquare {
	ICalendarSquare wrapped;

	public WrapperSquare(ICalendarSquare wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public String getMessage() {
		return wrapped.getMessage();
	}
}
