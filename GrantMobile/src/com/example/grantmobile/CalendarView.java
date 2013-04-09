/*****************************************************************************/
/** This activity is to allow the user to preview the hours on the calendar **/
/** and other incomplete details which will be determined as time passes.   **/
/**                                                                         **/
/** 03/16/2013 NPK Began programming with main procedures which determine   **/
/**                calendar square data, and draw it with (currently) made  **/
/**                up data. Formatting is not yet in place, but is          **/
/**                somewhat customizable.                                   **/
/** 03/20/2013 NPK Now determines first day of month correctly, and uses    **/
/**                leave hours of calendar square class.                    **/
/** 03/24/2013 NPK Calendar now sized dynamically based upon screen size.   **/
/**                                                                         **/
/**                                                                         **/
/**                                                                         **/
/**                                                                         **/
/**                                                                         **/
/*****************************************************************************/


package com.example.grantmobile;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.json.JSONArray;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;

public class CalendarView extends View {
	
	

	// Constants

    // Days in a week
	private static final int DAYSINAWEEK = 7;
	// Weeks to draw
	private static final int WEEKSTODRAW = 7;
	
	// Calendar drawing constants
	
	// Screen background color
	private static final int screenBackgroundColor = Color.BLUE;
	// Header background color
	private static final int headerBackgroundColor = Color.BLACK;
	// Header foreground color
	private static final int headerForegroundColor = Color.WHITE;										 
	// Daily background color odd
	private static final int dailyBackgroundColorOdd = Color.WHITE;
	// Daily background color even
	private static final int dailyBackgroundColorEven = Color.LTGRAY;	
	// Daily foreground color
	private static final int dailyForegroundColor = Color.RED;
	// Footer background color
	private static final int footerBackgroundColor = Color.BLACK;
	// Footer foreground color 
	private static final int footerForegroundColor = Color.WHITE;
	// Calendar initial horizontal margin
	private static final int calendarMarginX = 10;
	// Calendar initial vertical margin
	private static final int calendarMarginY = 10;
	
	public static final String[] monthNames = {
		"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"
	};
	
	// Variables
	
	// The paint brush
	Paint paint = new Paint();
	// A placeholder rectangle for drawing
	Rect curRect = new Rect();
	// Detector for taps on calendar squares
	GestureDetector tapDetector;
	// Listener to send taps on calendar squares to
	OnCalSquareTapListener calendarSquareListener;
	
	// Calendar settings
	
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
	// Calendar square titles
	ArrayList<String> squareTitles = new ArrayList<String>();
	// Calendar square size width
	int calendarSquareSizeW;
	// Calendar square size height
	int calendarSquareSizeH;
	// Calendar square array list
	ArrayList<CalendarSquare> calendar = new ArrayList<CalendarSquare>();
	
	public static interface OnCalSquareTapListener
	{
		public void onTap(CalendarSquare square);
	}
	
		
	public CalendarView(Context context) {
        super(context);
        init();
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public void init()
    {
        // Initialize header message
        initHeaderMessage();
        
        // Initialize gesture listener
        tapDetector = getGestureListener();
    }


	/**
	 * This procedure draws the screen with the current details.
	 */
	@Override
	protected void onDraw (Canvas canvas)
	{
	    // Variables

	    // The width for the header and footer
	    int headerAndFooterWidth;
	    // Footer Y position
	    int footerY;
	    // Current calendar square index
	    int currentCalendarSquareIndex = 0;
	    // The current display for the current calendar square
	    String thisDisplay = "";
	    // Odd or even flag
	    Boolean oddFlag = true;

	    // Calculate header and footer widths
	    headerAndFooterWidth = calendarSquareSizeW * (DAYSINAWEEK + 1);

	    // Draw header
	    paint.setColor(headerBackgroundColor);
	    paint.setStyle(Style.FILL_AND_STROKE);
	    curRect.set(
	            calendarMarginX,
	            calendarMarginY,
	            calendarMarginX + headerAndFooterWidth,
	            calendarMarginY + calendarSquareSizeH
	            );
	    canvas.drawRect(curRect, paint);
	    paint.setColor(headerForegroundColor);
	    canvas.drawText(headerMessage, calendarMarginX + 10,
	            calendarMarginY + 10, paint);

	    // Draw inner calendar squares
	    paint.setStyle(Style.FILL_AND_STROKE);

	    // Loop of using calendar square index
	    for (
	            currentCalendarSquareIndex = 0;
	            currentCalendarSquareIndex < calendar.size();
	            currentCalendarSquareIndex = currentCalendarSquareIndex + 1
	            ) {

	        // Determine rectangle to draw
	        curRect.set(
	                calendar.get(currentCalendarSquareIndex).positionX,
	                calendar.get(currentCalendarSquareIndex).positionY,
	                calendar.get(currentCalendarSquareIndex).positionX +
	                calendar.get(currentCalendarSquareIndex).sizeW,
	                calendar.get(currentCalendarSquareIndex).positionY +
	                calendar.get(currentCalendarSquareIndex).sizeH
	                );

	        // Determine color
	        if (oddFlag) {
	            paint.setColor(dailyBackgroundColorOdd);
	        }
	        else
	        {
	            paint.setColor(dailyBackgroundColorEven);
	        }// end if
	        oddFlag = !oddFlag;

	        // Draw rectangle determined
	    		canvas.drawRect(curRect, paint);

	        // Determine display
	        thisDisplay =
	                calendar.get(currentCalendarSquareIndex).displayString;

	        // Show display
		        paint.setColor(dailyForegroundColor);
	        canvas.drawText(thisDisplay,
	                calendar.get(currentCalendarSquareIndex).positionX
	                + 10,
	                calendar.get(currentCalendarSquareIndex).positionY
	                + 10,
	                paint);

	    }// end for

	    // Determine footer positions
	    footerY = 
	            calendarMarginY + (calendarSquareSizeH * (WEEKSTODRAW + 1));

	    // Draw footer
	    paint.setColor(footerBackgroundColor);
	    paint.setStyle(Style.FILL_AND_STROKE);
	    curRect.set(
	            calendarMarginX,
	            footerY,
	            calendarMarginX + headerAndFooterWidth,
	            footerY + calendarSquareSizeH
	            );
	    canvas.drawRect(curRect, paint);

	    // Draw footer text
	    paint.setColor(footerForegroundColor);
	    canvas.drawText(footerMessage, calendarMarginX + 10,
	            footerY + 10, paint);

	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
	        int bottom)
	{
	    // this is the earliest possible point at which initCalendar() can be called
	    // any earlier, and it won't be able to autosize
	    initCalendar();
	}
		
	
	
	/**
	 * This procedure handles touch events. Currently, in the event of a
	 * square's touching, it displays the daily number of that square.
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		Log.i("drawview", "got touch evt "+Math.round(event.getX())+","+Math.round(event.getY()));
		// standard gesture handler, route events to GestureDetector
		if (tapDetector.onTouchEvent(event))
		        return true;
		return super.onTouchEvent(event);
		    }
			
			

	/**
	 * This procedure initializes the header message with sample data
	 */
	private void initHeaderMessage()
	{	
		// load sample data
		initHeaderMessage(2, 2013, "", "GRANT-101-101-101");
	}
	
	/**
	 * This procedure initializes the header message.
	 */
	public void initHeaderMessage(int month, int year, String grantName, String grantCatalogNum)
	{
		// Variables
		
		// Long date message
		String longDate = "";
		
		// Load month, year, and grant name
		// (Using provided data)
		monthNumber = month;
		this.year = year;
		grant = grantName + " " + grantCatalogNum;
		
		// Get the month's details
		getMonthDetails();
	
		// Determine long date
		longDate = monthName + ", " + year;
				
		// Determine header message
		headerMessage = grant.trim() + " : " + longDate;
	}
	
	/**
	 * This procedure initializes the calendar.
	 */
	public void initCalendar()
	{
		// Variables

		// Width of the calendar
		int calendarWidth;
		// Height of the calendar
		int calendarHeight;
		// Current X position
		int currentX;
		// Current Y position
		int currentY;
		// Daily number for this calendar square
		int thisDailyNumber = -1;
		// Initial display for a calendar square
		String initialDisplay = "";
		// Days started to being counted flag
		Boolean daysStarted = false;
		// Days done being counted flag
		Boolean daysEnded = false;
		
		// make sure calendar is empty before starting
		calendar.clear();
		
		if (getWidth() == 0)
		{
		    return;
		}
		
		// Initialize calendar square size
		
		// Determine calendar height by subtracting margins
		calendarWidth = getWidth() - (calendarMarginX * 2);
		calendarHeight = getHeight() - (calendarMarginY * 2);
		
		// Determine individual square sizes
		calendarSquareSizeW = calendarWidth / (DAYSINAWEEK + 1);
			// plus one from weekly totals 
		calendarSquareSizeH = calendarHeight / (WEEKSTODRAW + 2);
			// plus header and footer

				
		// Initialize calendar square titles
		Collections.addAll(squareTitles,
			"Su","Mo","Tu","We","Th","Fr","Sa","To");
		
		// Initialize daily number
		thisDailyNumber = 0;
		
		// Loops of initializing calendar squares:
		
		// Vertical
		for (int yy = 0; yy < WEEKSTODRAW; yy++)
		{
			// Calculate current Y coordinate
			currentY = calendarMarginY + (calendarSquareSizeH * (yy + 1)); 
			
			// Horizontal
			for (int xx = 0; xx <= 7; xx++)
			{
				// Calculate current X coordinate
				currentX = calendarMarginX + (calendarSquareSizeW * xx);

				// For first row, set display to title
				if (yy == 0)
				{
					
					initialDisplay = squareTitles.get(xx);
					
				}
				else
				{
				// For additional rows, determine daily number
					
					// Initialize display to set to dash
					initialDisplay = "-";
					
					// If day counting hasn't started, check if first day
					if (!daysStarted)
					{
						if (xx == firstDay)
						{
							daysStarted = true;
							thisDailyNumber = 0;
						}
						
					}// end if
					
					// After days have started, but not yet ended
					if ((daysStarted) && (!daysEnded))
					{
						
						// If not the location for the weekly totals
						if ((xx != DAYSINAWEEK) && (!daysEnded))
						{							
							
							// Increase daily number							
							thisDailyNumber = thisDailyNumber + 1;
							
							// Set initial display to daily number
							initialDisplay = String.valueOf(thisDailyNumber);
							
						} else
						{
							// If location for daily totals, start with N/A
							initialDisplay = "N/A";
							
						}// end if
						
						// If past the end of the month, use zero as daily
						// number, and set display to dash
						if ((thisDailyNumber > daysInMonth) && (!daysEnded)) {
							daysEnded = true;
							thisDailyNumber = 0;
							initialDisplay = "-";
						}// end if

					}// end if
					
				}// end if
				
				// If days have ended, check for daily total square
				if ((daysEnded) && (xx == DAYSINAWEEK))
				{
					initialDisplay = "N/A";
				
				}// end if
				
				// Initialize this calendar square
				calendar.add(
					new CalendarSquare(
						currentX, currentY,
						calendarSquareSizeW, calendarSquareSizeH,
						thisDailyNumber, initialDisplay)
				);
				
			}// end for
			
		}// end for
		
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
	
	
		
	
	/**
	 * This procedure loads the calendar with the data for hours, and totals.
	 */
	public void loadCalendarData() {
	
		// Variables
		
		// Total grant hours for a day
		int dayTotalGrantHours = 0;
		// Total non-grant hours for a day
		int dayTotalNonGrantHours = 0;
		// Total leave hours for a day or week
		int dayTotalLeaveHours = 0;
		// Total hours for a day or week
		int dayTotalHours = 0;
		// Total grant hours for a week
		int currentTotalGrantHours = 0;
		// Total non-grant hours for a week
		int currentTotalNonGrantHours = 0;
		// Total leave hours for a week
		int currentTotalLeaveHours = 0;
		// Total hours for a day or week
		int currentTotalHours = 0;
		
		// Determine weekly and monthly totals and note information
		for (int i = 0; i < calendar.size(); i++)
		{
			
			// If not the first week test
			if (i >= (DAYSINAWEEK + 1)) {
			
				// End of week test
				// If end of week, determine weekly totals, and set it
				if ((i % (DAYSINAWEEK + 1)) == DAYSINAWEEK) {
				
					// Set display information as
					// total hours concatenated string
					calendar.get(i).displayString =
						String.valueOf(currentTotalGrantHours) + "|" +
						String.valueOf(currentTotalNonGrantHours) + "|" +
						String.valueOf(currentTotalLeaveHours) + "|" +
						String.valueOf(currentTotalHours);
					
					// Reset weekly totals
					currentTotalGrantHours = 0;
					currentTotalNonGrantHours = 0;
					currentTotalLeaveHours = 0;
					currentTotalHours = 0;
					
				}
				else if ((calendar.get(i).dailyNumber > daysInMonth)
						|| (calendar.get(i).dailyNumber < 1))
				{
					// Daily number out of range test. If so, set display string to dash
					calendar.get(i).displayString = "-";
					
				}
				else
				{
					// If not end of week and within range, determine
					// daily information
					dayTotalGrantHours = calendar.get(i).grantHours;
					dayTotalNonGrantHours = calendar.get(i).nonGrantHours;
					dayTotalLeaveHours = calendar.get(i).leave;
					dayTotalHours = calendar.get(i).totalHours();
					
					// Add daily information to weekly totals
					currentTotalGrantHours += dayTotalGrantHours;
					currentTotalNonGrantHours += dayTotalNonGrantHours;
					currentTotalLeaveHours += dayTotalLeaveHours;
					currentTotalHours += dayTotalHours;
					
					// Increase totals for monthly total
					monthTotalGrantHours += dayTotalGrantHours;
					monthTotalNonGrantHours += dayTotalNonGrantHours;
					monthTotalLeaveHours += dayTotalLeaveHours;
					monthTotalHours += dayTotalHours;
					
					// Set display information as
					// total hours concatenated string
					calendar.get(i).displayString =
						String.valueOf(dayTotalGrantHours) + "|" +
						String.valueOf(dayTotalNonGrantHours) + "|" +
						String.valueOf(dayTotalLeaveHours) + "|" +
						String.valueOf(dayTotalHours);
				
				}// end if
				
			}// end if
			
		}// end for
		
		// Determine footer message from monthly totals
		footerMessage = "Total Hours This Month: " +
			String.valueOf(monthTotalGrantHours) + "|" +
			String.valueOf(monthTotalNonGrantHours) + "|" +
			String.valueOf(monthTotalLeaveHours) + "|" +
			String.valueOf(monthTotalHours);
		
		// Refresh screen
        invalidate();
	}
	
	private GestureDetector getGestureListener()
	{
	    SimpleOnGestureListener gl = new GestureDetector.SimpleOnGestureListener()
	    {
			@Override
	        public boolean onDown(MotionEvent event)
	        {
	            // The touching X coordinate
	            int x = (int)event.getX();
	            // The touching Y coordinate
	            int y = (int)event.getY();

	            // Check for any calendar square touches, and toast daily
	            // number of those touched
	            for (CalendarSquare square : calendar)
	            {
	                if ((x >= square.positionX) &&
	                        (y >= square.positionY) &&
	                        (x <= (square.positionX) + square.sizeW) &&
	                        (y <= (square.positionY) + square.sizeH))
	                {
	                    Log.i("drawview", "found matching square for day " + square.dailyNumber);
	                    if (calendarSquareListener != null)
	                    {
	                    	calendarSquareListener.onTap(square);
	                    }
	                    return true;
	                }// end if   	
	            }// end for
	            return false;
	        }

	    };
	    return new GestureDetector(this.getContext(), gl);
	}

	public void loadTimes(JSONArray granthours, JSONArray nongranthours, JSONArray leavehours)
	{
		// load hours into calendar squares
		for (CalendarSquare cal : calendar)
		{
			// don't mess with squares that don't contain actual days
			if (cal.dailyNumber != 0)
			{
				cal.leave         = (int)leavehours   .optDouble(cal.dailyNumber-1, 0);
				cal.nonGrantHours = (int)nongranthours.optDouble(cal.dailyNumber-1, 0);
				cal.grantHours    = (int)granthours   .optDouble(cal.dailyNumber-1, 0);
			}
		}
	}
	
	public void setOnCalSquareTapListener(OnCalSquareTapListener listener)
	{
		this.calendarSquareListener = listener;
	}
	
}

