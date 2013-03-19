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

package com.example.grantmobile;

public class CalendarSquare {

	public int positionX = 0;
	public int positionY = 0;
	public int sizeW = 0;
	public int sizeH = 0;
	public int dailyNumber = 0;
	public int grantHours = 0;
	public int nonGrantHours = 0;
	public int leave = 0;
	public String displayString = "";
	
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
	 * @param ds Display string for messages, titles, or totals
	 */
	public CalendarSquare(int x, int y, int w, int h, int d, int g, int n, int l, String ds)
	{
		positionX = x;
		positionY = y;
		sizeW = w;
		sizeH = h;
		dailyNumber = d;
		grantHours = g;
		nonGrantHours = n;
		leave = l;
		displayString = ds;
	}
	
	/**
	 * This constructor is for calendar squares using all except the hour parameters. 
	 * With this constructor, hours are defaulted to zero.
	 * @param x X coordinate of calendar square
	 * @param y Y coordinate of calendar square
	 * @param w Width of square
	 * @param h Height of square
	 * @param d Daily number, as in 5 for the 5th of the month
	 * @param ds Display string for messages, titles, or totals
	 */
	public CalendarSquare(int x, int y, int w, int h, int d, String ds)
	{
		positionX = x;
		positionY = y;
		sizeW = w;
		sizeH = h;
		dailyNumber = d;
		grantHours = 0;
		nonGrantHours = 0;
		leave = 0;
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
		hours = (grantHours + nonGrantHours) - leave;
		
		return hours;
		
	}
}
