package com.example.grantmobile;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import org.json.JSONArray;

import android.util.Log;

import com.example.grantmobile.CalendarSquare.DaySquare;
import com.example.grantmobile.CalendarSquare.ICalendarSquare;
import com.example.grantmobile.CalendarSquare.PlaceholderSquare;
import com.example.grantmobile.CalendarSquare.TitleSquare;
import com.example.grantmobile.CalendarSquare.TotalSquare;

public class CalendarArray {
	
    // Days in a week
	private static final int DAYSINAWEEK = 7;
	// Weeks to draw
	private static final int WEEKSTODRAW = 7;
	
	public static final String[] monthNames = {
		"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"
	};
	private static final String[] squareTitles = {
		"Su","Mo","Tu","We","Th","Fr","Sa","To"
	};
	
	// Month number for calendar
	int monthNumber = 0;
	// Month name for calendar
	String monthName = "";
	// Days in month for calendar
	int daysInMonth = 0;
	// First day of month number for week before
	int firstDay = 0;
	// Year for calendar
	int year = 0;																
	// Grant title
	String grant = "";			
	// Message for top of the calendar
	String headerMessage = "";
	// Message for bottom of the calendar
	String footerMessage = "";
	// Total grant hours for a month
	int monthTotalGrantHours = 0;
	// Total non-grant hours for a month
	int monthTotalNonGrantHours = 0;
	// Total leave hours for a month
	int monthTotalLeaveHours = 0;
	// Total hours for a month
	int monthTotalHours = 0;
	// Calendar square array list
	private List<ICalendarSquare> calendar = new ArrayList<ICalendarSquare>();
	// squares which correspond to actual days in a month
	private List<DaySquare> realDays = new ArrayList<DaySquare>();
	
	

	public CalendarArray(int year, int month) {
		this.year = year;
		this.monthNumber = month;
		getMonthDetails();
		initCalendar();
	}
	
	public void loadTimes(double[] granthours, double[] nongranthours, double[] leavehours) {
		for (DaySquare cal : realDays) {
			cal.leave         = leavehours   [cal.dailyNumber-1];
			cal.nonGrantHours = nongranthours[cal.dailyNumber-1];
			cal.grantHours    = granthours   [cal.dailyNumber-1];
		}
	}
	
	public void loadTimes(JSONArray granthours, JSONArray nongranthours, JSONArray leavehours)
	{
		// load hours into calendar squares
		for (DaySquare cal : realDays)
		{
			cal.leave         = leavehours   .optDouble(cal.dailyNumber-1, 0);
			cal.nonGrantHours = nongranthours.optDouble(cal.dailyNumber-1, 0);
			cal.grantHours    = granthours   .optDouble(cal.dailyNumber-1, 0);
		}
	}
	/**
	 * This procedure initializes the calendar.
	 */
	public void initCalendar()
	{
		// Variables

		// Daily number for days initialization
		int thisDailyNumber;
		
		// make arraylists to replace the originals when we're done
		ArrayList<ICalendarSquare> tmpCalendar =
				new ArrayList<ICalendarSquare>(WEEKSTODRAW * (DAYSINAWEEK + 1));
		ArrayList<DaySquare> tmpRealDays = new ArrayList<DaySquare>(daysInMonth);
		
		// Initialize calendar square size
		

		for (int x = 0; x < squareTitles.length; x++) {
			tmpCalendar.add(new TitleSquare(squareTitles[x]));
		}
		
//		PlaceholderSquare emptyDay = new PlaceholderSquare("-");
		
		// effective "day" of Sunday spot on first week
		Log.i("loadcalendar", ""+firstDay);
		thisDailyNumber = 1 - firstDay;
		
		PlaceholderSquare placeholder = new PlaceholderSquare();
		
		// Vertical
		while (thisDailyNumber < daysInMonth)
		{
			// used for weekly totals
			ArrayList<DaySquare> realDaysInWeek = new ArrayList<DaySquare>();
			// Horizontal
			for (int xx = 0; xx <= 6; xx++)
			{
				ICalendarSquare curSquare;
				
				if (thisDailyNumber > 0 && thisDailyNumber <= daysInMonth) {
					// create real day and add to weekSquares and realDays
					DaySquare daySquare = new DaySquare(thisDailyNumber, 0, 0, 0);
					curSquare = daySquare;
					realDaysInWeek.add(daySquare);
					tmpRealDays.add(daySquare);
				} else {
					// create placeholder day
					curSquare = placeholder;
				}
				
				// add whatever square we made to calendar and advance a day
				tmpCalendar.add(curSquare);
				thisDailyNumber++;
			} // end for xx
			// add total square for week
			if (realDaysInWeek.size() > 0) {
				tmpCalendar.add(new TotalSquare(realDaysInWeek));
			} else {
				tmpCalendar.add(placeholder);
			}
		} // end for yy
		// we are gonna be in a bad way if these get out of sync, so make it tougher for that to happen
		this.calendar = Collections.unmodifiableList(tmpCalendar);
		this.realDays = Collections.unmodifiableList(tmpRealDays);
	}

	
	/**
	 * This procedure returns the month name of the current month number.
	 */
	private void getMonthDetails()
	{
		// Variables
		
		// Calendar object for day of week testing
		Calendar cal;
		
		// Determine month name and number of days in the month
		cal = new GregorianCalendar(year, monthNumber - 1, 1);
		monthName = monthNames[monthNumber - 1];
		daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		
		// Determine what day of the week the first of the month is
		
		// First check if valid month
		if (monthName != "")
		{
			firstDay = cal.get(Calendar.DAY_OF_WEEK) - 1;
			
		}// end if
		
	}
	
	public ICalendarSquare getSquare(int pos) {
		return calendar.get(pos);
	}
	
	public DaySquare getDay(int dayOfMonth) {
		return realDays.get(dayOfMonth-1);
	}
	
	public int getNumberOfDays() {
		return realDays.size();
	}
	
	public int getNumberOfSquares() {
		return calendar.size();
	}

}
