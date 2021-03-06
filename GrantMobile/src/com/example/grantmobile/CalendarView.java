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
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.support.v4.app.FragmentManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

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
	private static final int enlargedBackgroundColor = Color.WHITE;
	// Enlarged square foreground color 
	private static final int enlargedForegroundColor = Color.BLACK;
	// Enlarged square border color
	private static final int enlargedBorderColor = Color.BLACK;
	// Approval/disapproval button background color
	private static final int approvalButtondBackgroundColor = Color.WHITE;
	// Approval/disapproval button foreground color 
	private static final int approvalButtonForegroundColor = Color.BLACK;
	// Approval/disapproval button border color
	private static final int approvalButtonBorderColor = Color.BLACK;
	// Calendar initial horizontal margin
	private static final int calendarMarginX = 10;
	// Calendar initial vertical margin
	private static final int calendarMarginY = 10;
	
	public static final String[] monthNames = {
		"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
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
	// Font metrics for sizing
	FontMetrics fm = new FontMetrics();
	// Fragment management
	FragmentManager frag;
	
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
	// Date for top of the calendar
	String headerDate = "";
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
	ArrayList<CalendarSquare> calendar;
	// The enlarged day square
	CalendarSquare enlargedSquare = new CalendarSquare();
	// The enlarged day square's reference to main squares index
	int enlargedSquareIndex;
    // The width for the header and footer
    int headerAndFooterWidth;
    // Footer height position
    int footerY;
    // Employee Name
    String employee;
    // Employee HeaderText
    String headerEmployee;
	
	
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
    	// Initialize calendar square array
    	calendar = new ArrayList<CalendarSquare>();
    	
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

	    // Current calendar square index
	    int currentCalendarSquareIndex = 0;
	    // The current display for the current calendar square or button
	    String thisDisplay = "";
	    // The optimal font size for the current text planned to be displayed
	    int fontSize;
	    // Odd or even flag
	    Boolean oddFlag = true;

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
	    
	    // Draw header text
	    fontSize = 20;
	    paint.setTextSize(fontSize);
	    canvas.drawText(headerMessage, calendarMarginX + 10,
	            calendarMarginY + 20, paint);
	    canvas.drawText(headerEmployee, calendarMarginX + 10,
	            calendarMarginY + 45, paint);
	    canvas.drawText(headerDate, calendarMarginX + (calendarSquareSizeW * 5) + 10,
	            calendarMarginY + 45, paint);

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
	        
	        // Determine and font size
	        if (calendar.get(currentCalendarSquareIndex).weeklyTotal) {
	        	fontSize = 20;	
	        } else {
	        	fontSize = 34;
	        }// end if
	        
		    paint.setTextSize(fontSize);

	        // Show display
		        paint.setColor(dailyForegroundColor);
	        canvas.drawText(thisDisplay,
	                calendar.get(currentCalendarSquareIndex).positionX
	                + 10,
	                calendar.get(currentCalendarSquareIndex).positionY
	                + 28,
	                paint);

	    }// end for

	    // Draw footer
	    paint.setColor(footerBackgroundColor);
	    paint.setStyle(Style.FILL_AND_STROKE);
	    curRect.set(
	            calendarMarginX,
	            footerY,
	            calendarMarginX + (headerAndFooterWidth / 2),
	            footerY + calendarSquareSizeH
	            );
	    canvas.drawRect(curRect, paint);
	    
	    // Draw footer text
	    paint.setColor(footerForegroundColor);
	    thisDisplay = footerMessage;
	    canvas.drawText(thisDisplay,
	    		((headerAndFooterWidth / 4) * 1) - (thisDisplay.length() * (fontSize / 4)),
	    		footerY + calendarMarginY + 28,
	    		paint
	    		);
	     
	    // Draw button for approval/disapproval
	    
    	// Draw button with border    	
    	paint.setColor(approvalButtondBackgroundColor);
    	paint.setStyle(Style.FILL_AND_STROKE);
    	curRect.set(
			(headerAndFooterWidth / 2),
			footerY,
			headerAndFooterWidth + calendarMarginX,
			footerY + calendarSquareSizeH
			);
    	canvas.drawRect(curRect, paint);
    	
    	paint.setStyle(Style.STROKE);
    	paint.setColor(approvalButtonBorderColor);
    	canvas.drawRect(curRect, paint);
    	
    	// Draw button text
    	
    	// Determine size
	    fontSize = 16;
	    paint.setTextSize(fontSize);
    	
    	// Button Text
	    thisDisplay = "Approval/Disapproval";
    	paint.setColor(approvalButtonForegroundColor);
    	paint.setStyle(Style.FILL);
    	canvas.drawText(thisDisplay,
    		((headerAndFooterWidth / 4) * 3) - (thisDisplay.length() * (fontSize / 4)),
    		footerY + calendarMarginY + 28,
    		paint
    		);

	    // Draw footer text
	    paint.setColor(footerForegroundColor);

	    fontSize = 18;
	    paint.setTextSize(fontSize);
	    
	    canvas.drawText(footerMessage,
	    		headerAndFooterWidth + calendarMarginX,
	            footerY + 25, paint);

	    // Draw enlarged square and details if applicable
	    if (enlargedSquare.positionX != 0) {
	    	
	    	// Draw square with border    	
	    	paint.setColor(enlargedBackgroundColor);
	    	paint.setStyle(Style.FILL_AND_STROKE);
	    	curRect.set(
    			enlargedSquare.positionX,
    			enlargedSquare.positionY,
    			enlargedSquare.positionX + enlargedSquare.sizeW,
    			enlargedSquare.positionY + enlargedSquare.sizeH
    			);
	    	canvas.drawRect(curRect, paint);
	    	
	    	paint.setStyle(Style.STROKE);
	    	paint.setColor(enlargedBorderColor);
	    	canvas.drawRect(curRect, paint);
	    	
	    	// Draw text
	    	
	    	// Determine size
		    fontSize = 16;
		    paint.setTextSize(fontSize);
	    	
	    	// Info header
	    	paint.setColor(enlargedForegroundColor);
	    	paint.setStyle(Style.FILL);
	    	canvas.drawText(enlargedSquare.displayString,
	    		enlargedSquare.positionX + 10,
	    		enlargedSquare.positionY + 30,
	    		paint
	    		);
	    	
	    	// Grant Hours
	    	canvas.drawText(
	    			String.valueOf(
	    					"GRANT: " + enlargedSquare.grantHours
	    			),
		    		enlargedSquare.positionX + (int)(calendarSquareSizeW*0.25) + 10,
		    		enlargedSquare.positionY + (int)(calendarSquareSizeH*1) + 10,
		    		paint
		    		);
	    	
	    	// Non-grant Hours
	    	canvas.drawText(
	    			String.valueOf(
	    					"NON-GRANT: " + enlargedSquare.nonGrantHours
	    			),
		    		enlargedSquare.positionX + (int)(calendarSquareSizeW*0.25) + 10,
		    		enlargedSquare.positionY + (int)(calendarSquareSizeH*1.5) + 10,
		    		paint
		    		);
	    	
	    	// Leave Hours
	    	canvas.drawText(
	    			String.valueOf(
	    					"LEAVE: " + enlargedSquare.leave
	    			),
		    		enlargedSquare.positionX + (int)(calendarSquareSizeW*0.25) + 10,
		    		enlargedSquare.positionY + (int)(calendarSquareSizeH*2) + 10,
		    		paint
		    		);
	    	
	    	// Total Hours
	    	canvas.drawText(
	    			String.valueOf(
	    					"TOTAL: " + enlargedSquare.totalHours()
	    			),
		    		enlargedSquare.positionX + (int)(calendarSquareSizeW*0.25) + 10,
		    		enlargedSquare.positionY + (int)(calendarSquareSizeH*2.5) + 10,
		    		paint
		    		);
	    	
	    }// end if
	    
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
	        int bottom)
	{
	    // this is the earliest possible point at which initCalendar() can be called
	    // any earlier, and it won't be able to autosize
	    initCalendar();
	    loadCalendarData();
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
		// Load sample data
		initHeaderMessage(1, 1900, "Loading", "Loading", "Loading");
	}
	
	/**
	 * This procedure initializes the header message.
	 */
	public void initHeaderMessage(int month, int headerYear, String grantName, String grantCatalogNum, String employeeName)
	{	
		// Variables

		// Load month, year, and grant name
		// (Using provided data)
		monthNumber = month;
		year = headerYear;
		grant = grantName + " " + grantCatalogNum;
		employee = employeeName;
		
		// Get the month's details
		getMonthDetails();
	
		// Determine long date
		headerDate = monthName + ", " + year;
				
		// Determine header message
		headerMessage = grant.trim();
		
		//Set name of employee being approved
		headerEmployee = "Employee Name: " + employee;
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
		// Daily number for days initialization
		int thisDailyNumber = -1;
		// Daily number for square setting
		int setDailyNumber = -1;
		// Initial display for a calendar square
		String initialDisplay = "";
		// Days started to being counted flag
		Boolean daysStarted = false;
		// Days done being counted flag
		Boolean daysEnded = false;
		
		// make sure calendar is empty before starting
		calendar.clear();
		
		// Initialize calendar square size
		
		// Determine calendar height by subtracting margins
		calendarWidth = getWidth() - (calendarMarginX * 2);
		calendarHeight = getHeight() - (calendarMarginY * 2);
		
		// Determine individual square sizes
		calendarSquareSizeW = calendarWidth / (DAYSINAWEEK + 1);
			// plus one from weekly totals 
		calendarSquareSizeH = calendarHeight / (WEEKSTODRAW + 2);
			// plus header and footer
		
		// Calculate header and footer widths
	    headerAndFooterWidth = calendarSquareSizeW * (DAYSINAWEEK + 1);
	    
	    // Determine footer position
	    footerY = calendarMarginY + (calendarSquareSizeH * (WEEKSTODRAW + 1));
				
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
							setDailyNumber = thisDailyNumber;
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
							setDailyNumber = thisDailyNumber;
							
							// Set initial display to daily number
							initialDisplay = String.valueOf(thisDailyNumber);
							
						} else
						{
							// If location for daily totals, start with N/A
							initialDisplay = "N/A";
							setDailyNumber = 0;
							
						}// end if
						
						// If past the end of the month, use zero as daily
						// number, and set display to dash
						if ((thisDailyNumber > daysInMonth) && (!daysEnded)) {
							daysEnded = true;
							thisDailyNumber = -1;
							setDailyNumber = thisDailyNumber;
							initialDisplay = "-";
						}// end if

					}// end if
					
				}// end if
				
				// If days have ended, check for daily total square to determine enlargement ability
				if (daysEnded) {
					
					// Initialize display to dash 
					initialDisplay = "-";
					
					// If weekly total, allow enlarging
					if (xx == DAYSINAWEEK)
					{
						
						setDailyNumber = 0;
					
					} else {
					// If not weekly total, don't allow enlarging
						
						setDailyNumber = -1;
						
					}// end if
				
				}// end if
				
				// Initialize this calendar square
				calendar.add(
					new CalendarSquare(
						currentX, currentY,
						calendarSquareSizeW, calendarSquareSizeH,
						setDailyNumber, initialDisplay)
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
		Toast.makeText(getContext(), "LOADING...", Toast.LENGTH_LONG).show();
		
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
		// Total hours for a week
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
						String.valueOf(weekTotalGrantHours);

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
			
			// First day of week test
			if (i % (DAYSINAWEEK + 1) == 0) {
				calendar.get(i).firstDayOfWeek = true;
			}// end if
			
		}// end for
		
		// Determine footer message from monthly totals
		footerMessage = "Monthly Grant Hours: " +
			String.valueOf(monthTotalGrantHours);
		
		// Clear enlarged square
		enlargedSquare = new CalendarSquare();
		
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
		                    Log.i("drawview", "found matching square for day " + square.dailyNumber + ".");
		                    if (square.firstDayOfWeek) {
		                    	Log.i("drawview","it is a first day");
		                    }// end if
		                    if (square.weeklyTotal) {
		                    	Log.i("drawview","it is a weekly total");
		                    }// end if
		                    
		                    // If second tap here, load details view
		                    if (enlargedSquareIndex == calendarSquareIndex) {
		                    	
		                    	// Prepare to load details view
		                    	loadDetails = true;		                    	
		                    	
		                    }// end if
		                    
		                    // Save square if daily number appropriate and year valid
		                    if (year != 1900) {
			                    enlargedSquareIndex = calendarSquareIndex;
			                    enlargedSquare = new CalendarSquare(square);
		                    }// end if
		                    
		                    // Note tapped
		                    tapped = true;
		                   
		                }// end if
		                
		                // Increase index
		                calendarSquareIndex += 1;
		                
		            }// end for
		            
	            }// end if
	            
	            // If something tapped now, update class variables for enlarged box
	            if (tapped) {
	            	
	            	// Test daily number and year. If in range, make enlarged square
	            	if ((enlargedSquare.dailyNumber != -1) && (year != 1900)) {
	            	
		            	// Update enlarged information
		            	enlargedSquare.sizeW = calendarSquareSizeW * 3;
		            	enlargedSquare.sizeH = calendarSquareSizeH * 3;
		            	
	            		enlargedSquare.positionX -= calendarSquareSizeW;
	            		enlargedSquare.positionY -= calendarSquareSizeH;
		            	
		            	if (enlargedSquare.firstDayOfWeek) {
		            		enlargedSquare.positionX += calendarSquareSizeW;
		            	}// end if
		            	
		            	if (enlargedSquare.weeklyTotal) {
		            		enlargedSquare.positionX -= calendarSquareSizeW;
		            	}// end if
		            	
		            	// Set display string
		            	
		            	// If not weekly total, use date
		            	if (enlargedSquare.dailyNumber != 0) {
		            		enlargedSquare.displayString = 
		            			monthNumber + "/" + enlargedSquare.dailyNumber + "/" + year + ":";
		            		
		            	} else {
		            		// If weekly total, use week range
		            		enlargedSquare.displayString = "WEEK " +
		            			String.valueOf((int)(enlargedSquareIndex / (DAYSINAWEEK + 1))) + 
		            			":";
		            		
		            	}// end if
		            	
	            	} else {
	            		// If out of range, hide square
		            	enlargedSquare.sizeW = 0;
		            	enlargedSquare.sizeH = 0;
		            	
	            		enlargedSquare.positionX = 0;
	            		enlargedSquare.positionY = 0;
	            		
	            		enlargedSquare.displayString = "";
		            	
	            	}// end if
	            	
	            }// end if
	            
	            // If should load details, load now
	            if (loadDetails) {
	            	
                    if (calendarSquareListener != null)
                    {
                    	calendarSquareListener.onTap(enlargedSquare);
                    }// end if
	            	
	            }// end if
	            
	            // Check for approval/disapproval button if nothing tapped as of now
	            if (((x >= (headerAndFooterWidth / 2)) &&
                    (y >= footerY) &&
                    (x <= headerAndFooterWidth) &&
                    (y <= (footerY + calendarSquareSizeH))
                    && (!tapped))) {
	            	
	    			// Show dialog box
	    			SubmitDialog dialog = new SubmitDialog();
	    			dialog.setUserID(732);
	    			dialog.show(frag, "");
	            	
	            }// end if
	            
	            // If a tap happened, update screen
	            if (tapped) {
	            	
	            	invalidate();
	            	
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

	public void setFrag(FragmentManager frag) {
		this.frag = frag;
	}
	
}

