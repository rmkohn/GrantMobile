package com.example.grantmobile.CalendarSquare;

import java.util.List;

public class TotalSquare implements ICalendarSquare {
	public List<DaySquare> squares;

	public TotalSquare(List<DaySquare> squares) {
		this.squares = squares;
	}
	
	

	@Override
	public String getMessage() {
		int grant = 0, leave = 0, nongrant = 0;
		for (DaySquare square: squares) {
			grant    += square.grantHours;
			leave    += square.leave;
			nongrant += square.nonGrantHours;
		}
		return "T: " + grant;
//		return String.format("%d - %d - %d", grant, nongrant, leave);
	}
}
