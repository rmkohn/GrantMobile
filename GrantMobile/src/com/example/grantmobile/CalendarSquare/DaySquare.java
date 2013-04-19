/*****************************************************************************/
/** This class allows the storing of data for calendar squares including    **/
/** position, size, day of the month, the various hours, and a display      **/
/** string.                                                                 **/
/**                                                                         **/
/** 03/16/2013 NPK Created with core parameters for position, and hours.    **/
/**                Also made the TotalHours method.                         **/
/** 03/19/2013 NPK Added size, and leave time parameters. Updated the       **/
/**                method TotalHours to use leave hours. Added constructor  **/
/**                which auto-initializes hours.                            **/
/**                                                                         **/
/**                                                                         **/
/**                                                                         **/
/**                                                                         **/
/*****************************************************************************/

package com.example.grantmobile.CalendarSquare;


public class DaySquare implements ICalendarSquare {

	public int dailyNumber = 0;
	public int grantHours = 0;
	public int nonGrantHours = 0;
	public int leave = 0;
	
	/**
	 * This constructor is for calendar squares using all parameters.
	 * @param x X coordinate of calendar square
	 * @param y Y coordinate of calendar square
	 * @param w Width of square
	 * @param h Height of square
	 * @param d Daily number, as in 5 for the 5th of the month
	 * @param g Grant hours
	 * @param n Non-grant hours
	 * @param l Leave hours
	 * @param wt Weekly totals
	 * @param fd First day of the week
	 * @param ds Display string for messages, titles, or totals
	 */
	public DaySquare(int d, int g, int n, int l)
	{
		dailyNumber = d;
		grantHours = g;
		nonGrantHours = n;
		leave = l;
	}
	
	/**
	 * This procedure returns the total number of hours on this calendar square.
	 * @return The total number of hours
	 */
	public int totalHours() {
		return (grantHours + nonGrantHours) - leave;
	}
	
	public String toString() {
		return String.format("day square:" + getMessage());
	}

	@Override
	public String getMessage() {
//		return String.format("%d - %d - %d", grantHours, nonGrantHours, leave);
		return String.valueOf(dailyNumber);
	}
}
