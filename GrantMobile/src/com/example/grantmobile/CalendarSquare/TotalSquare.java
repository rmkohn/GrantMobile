package com.example.grantmobile.CalendarSquare;

import java.util.List;

public class TotalSquare implements ICalendarSquare {
	public List<DaySquare> squares;
	public int grant = 0;
	public int nongrant = 0;
	public int leave = 0;

	public TotalSquare(List<DaySquare> squares) {
		this.squares = squares;
	}
	
	public void updateTimes() {
		grant = nongrant = leave = 0;
		for (DaySquare square: squares) {
			grant    += square.grantHours;
			leave    += square.leave;
			nongrant += square.nonGrantHours;
		}
	}

	@Override
	public String getMessage() {
		updateTimes();
		return "T: " + grant;
	}
	
	@Override
	public int getHighlightColor() {
		updateTimes();
		return SquareColors.getHighlightColor(grant, nongrant, leave);
	}
	
}
