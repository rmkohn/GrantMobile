package com.example.grantmobile;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;

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
	// Enlarged square background color
	private static final int enlargedBackgroundColor = Color.BLUE;
	// Enlarged square foreground color 
	private static final int enlargedForegroundColor = Color.YELLOW;
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
	// The enlarged day view
	int enlargedDay = 0;
	// Is the enlarged a weekly total?
	boolean enlargedWeeklyTotal = false;
	// The enlarged day's calendar square index
	int enlargedDayIndex = -1;
	// The enlarged day's starting X coordinate
	int enlargedX = 0;
	// The enlarged day's starting Y coordinate
	int enlargedY = 0;
	// The enlarged day's width
	int enlargedW = 0;
	// The enlarged day's height
	int enlargedH = 0;
	
	
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
		initHeaderMessage(1, 2000, "Loading", "Loading");
	}
	
	/**
	 * This procedure initializes the header message.
	 */
	public void initHeaderMessage(int month, int headerYear, String grantName, String grantCatalogNum)
	{
		// Variables
		
		// Long date message
		String longDate = "";
		
		// Load month, year, and grant name
		// (Using provided data)
		monthNumber = month;
		year = headerYear;
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
		int weekTotalGrantHours = 0;
		// Total non-grant hours for a week
		int weekTotalNonGrantHours = 0;
		// Total leave hours for a week
		int weekTotalLeaveHours = 0;
		// Total hours for a day or week
		int weekTotalHours = 0;
		
		// Determine weekly and monthly totals and note information
		for (int i = 0; i < calendar.size(); i++)
		{
			
			// If not the first week test
			if (i >= (DAYSINAWEEK + 1)) {
			
				// End of week test
				// If end of week, determine weekly totals, and set it
				if ((i % (DAYSINAWEEK + 1)) == DAYSINAWEEK) {
				
					// Set weekly totals for this square
					calendar.get(i).grantHours = weekTotalGrantHours;
					calendar.get(i).nonGrantHours = weekTotalNonGrantHours;
					calendar.get(i).leave = weekTotalLeaveHours;
					
					// Set display information as total hours 
					calendar.get(i).displayString = "T: " +
						String.valueOf(weekTotalHours);

					// Set weekly totals property on
					calendar.get(i).weeklyTotal = true;
					
					// Reset weekly totals
					weekTotalGrantHours = 0;
					weekTotalNonGrantHours = 0;
					weekTotalLeaveHours = 0;
					weekTotalHours = 0;					
					
				}
				else if ((calendar.get(i).dailyNumber > daysInMonth)
						|| (calendar.get(i).dailyNumber < 1))
				{
					// Daily number out of range test. If so, set display string to dash
					calendar.get(i).displayString = "-";
					
					// Set weekly totals off
					calendar.get(i).weeklyTotal = false;
					
				}
				else
				{
					// If not end of week and within range, determine daily information
					dayTotalGrantHours = calendar.get(i).grantHours;
					dayTotalNonGrantHours = calendar.get(i).nonGrantHours;
					dayTotalLeaveHours = calendar.get(i).leave;
					dayTotalHours = calendar.get(i).totalHours();
					
					// Update weekly hours
					weekTotalGrantHours += dayTotalGrantHours;
					weekTotalNonGrantHours += dayTotalNonGrantHours;
					weekTotalLeaveHours += dayTotalLeaveHours;
					weekTotalHours += dayTotalHours; 
					
					// Increase totals for monthly total
					monthTotalGrantHours += dayTotalGrantHours;
					monthTotalNonGrantHours += dayTotalNonGrantHours;
					monthTotalLeaveHours += dayTotalLeaveHours;
					monthTotalHours += dayTotalHours;
					
					// Set display information as daily number only
					calendar.get(i).displayString = String.valueOf(calendar.get(i).dailyNumber);
					
					// Set weekly totals off
					calendar.get(i).weeklyTotal = false;
				
				}// end if
				
			}// end if
			
		}// end for
		
		// Determine footer message from monthly totals
		footerMessage = "Total Hours This Month: " +
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
				// Flag for tapping
				boolean tapped = false;
				// Flag for loading details
				boolean loadDetails = false;
				// Index of calendar square
				int calendarSquareIndex = 0;
	            // The touching X coordinate
	            int x = (int)event.getX();
	            // The touching Y coordinate
	            int y = (int)event.getY();
	            
	            // Check for tapping of arrows around enlarged box if a box is present
	            if ((enlargedDay >= 1) && (enlargedDay <= daysInMonth)) {
	            	
	            }// end if
	            
	            // If nothing tapped
	            if (!tapped) {

		            // Check for any calendar square touches and react to them
		            for (CalendarSquare square : calendar)
		            {
		                if (((x >= square.positionX) &&
		                        (y >= square.positionY) &&
		                        (x <= (square.positionX + square.sizeW)) &&
		                        (y <= (square.positionY + square.sizeH))
		                        && (!tapped)))
		                {
		                	// Note tap in log
		                    Log.i("drawview", "found matching square for day " + square.dailyNumber);
		                    
		                    // If second tap here, load details view
		                    if (enlargedDayIndex == calendarSquareIndex) {
		                    	
		                    	// Prepare to load details view
		                    	loadDetails = true;		                    	
		                    	
		                    }// end if
		                    
		                    // Save square details to class variables
		                    enlargedDay = square.dailyNumber;
		                    enlargedWeeklyTotal = square.weeklyTotal;
		                    enlargedDayIndex = calendarSquareIndex;
		                    
		                    // Note tapped
		                    if (calendarSquareListener != null)
		                    {
		                    	calendarSquareListener.onTap(square);
		                    }// end if
		                    tapped = true;
		                   
		                }// end if
		                
		                // Increase index
		                calendarSquareIndex += 1;
		                
		            }// end for
		            
	            }// end if
	            
	            // If something tapped now, update class variables for enlarged box
	            if (tapped) {
	            	
	            	// Update position and size
	            	
	            }// end if
	            
	            // If should load details, load now
	            if (loadDetails) {
	            	
	            	// Load details here
	            	
	            }// end if
	            
	            return tapped;
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

