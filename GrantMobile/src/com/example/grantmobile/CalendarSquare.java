package com.example.grantmobile;
/*****************************************************************************/
/**                                                                         **/
/**                                                                         **/
/**                                                                         **/
/**                                                                         **/
/**                                                                         **/
/**                                                                         **/
/**                                                                         **/
/**                                                                         **/
/**                                                                         **/
/**                                                                         **/
/**                                                                         **/
/**                                                                         **/
/**                                                                         **/
/**                                                                         **/
/**                                                                         **/
/*****************************************************************************/


public class CalendarSquare {

	public int positionX = 0;
	public int positionY = 0;
	public int dailyNumber = 0;
	public int grantHours = 0;
	public int nonGrantHours = 0;
	public int leave = 0;
	public String displayString = "";
	
	/**
	 * This constructor is for calendar squares using all parameters.
	 * @param x X coordinate of calendar square
	 * @param y Y coordinate of calendar square
	 * @param d Daily number, as in 5 for the 5th of the month
	 * @param g Grant hours
	 * @param n Non-grant hours
	 * @param l Leave hours
	 * @param ds Display string for messages, titles, or totals
	 */
	public CalendarSquare(int x, int y, int d, int g, int n, int l, String ds)
	{
		
		positionX = x;
		positionY = y;
		dailyNumber = d;
		grantHours = g;
		nonGrantHours = n;
		leave = l;
		displayString = ds;
	}
	
	/**
	 * This procedure returns the total number of hours on this calendar square.
	 * @return The total number of hours
	 */
	public int totalHours() {
		
		// Variables
		
		// Total number of hours
		int hours;
		
		// Calculate hours
		hours = grantHours + nonGrantHours;
		
		return hours;
		
	}
}
