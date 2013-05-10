package com.example.grantmobile.CalendarSquare;

import java.util.List;

public class TotalSquare implements ICalendarSquare {
	public List<DaySquare> squares;
	public double grant = 0;
	public double nongrant = 0;
	public double leave = 0;

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
		return "T: " + (int)(grant+nongrant+leave);
	}
	
	@Override
	public int getHighlightColor() {
		updateTimes();
		return SquareColors.getHighlightColor(grant, nongrant, leave);
	}
	
}
