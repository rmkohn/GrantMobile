package com.example.grantmobile;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import org.json.JSONArray;

import com.example.grantmobile.CalendarSquare.DaySquare;
import com.example.grantmobile.CalendarSquare.ICalendarSquare;
import com.example.grantmobile.CalendarSquare.PlaceholderSquare;
import com.example.grantmobile.CalendarSquare.TotalSquare;

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
	private static final String[] squareTitles = {
		"Su","Mo","Tu","We","Th","Fr","Sa","To"
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
	// Calendar square size width
	int calendarSquareSizeW;
	// Calendar square size height
	int calendarSquareSizeH;
	// Calendar square array list
	List<ICalendarSquare> calendar = new ArrayList<ICalendarSquare>();
	// squares which correspond to actual days in a month
	List<DaySquare> realDays = new ArrayList<DaySquare>();
	// The enlarged day square
	ICalendarSquare enlargedSquare;
	// how much larger the enlarged square is than a regular square
	private static final float ENLARGED_SQUARE_MULTIPLIER = 3.0f;
	
	
	public static interface OnCalSquareTapListener
	{
		public void onTap(ICalendarSquare square);
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
    
    /*
     * conveninece methods to convert between calendar square positions
     * and pixels
     * These should be the only methods to know about details like the
     * header and margins
     */
    // get left pixel X from calendar square X
    private int getPixelX(int squareX) {
    	return calendarMarginX + squareX * calendarSquareSizeW;
    }
    
    // get top pixel Y from calendar square Y
    private int getPixelY(int squareY) {
    	return calendarMarginY + (squareY+1) * calendarSquareSizeH;
    }
    
    // get calendar square X from pixel X, or -1 if there is no such square
    private int getContainingSquareX(int x) {
    	int squareX = (x - calendarMarginX) / calendarSquareSizeW;
    	if (squareX < 0 || squareX >= DAYSINAWEEK + 1)
    		return -1;
    	return squareX;
    }
    
    // get cqlendar square Y from pixel Y, or -1 if there is no such square
    private int getContainingSquareY(int y) {
    	int squareY = (y - calendarMarginY) / calendarSquareSizeH - 1;
    	if (squareY < 0 || squareY >= WEEKSTODRAW)
    		return -1;
    	return squareY;
    }
    
    // convenience method to set curRect
    private void setRect(Rect rect, int x, int y) {
    	int posX = getPixelX(x);
    	int posY = getPixelY(y);
        rect.set(posX, posY, posX + calendarSquareSizeW, posY + calendarSquareSizeH);
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
	    for ( ICalendarSquare curSquare: calendar) {
	    	setRect(curRect, curSquare.getX(), curSquare.getY());

	        // Determine color
	        paint.setColor(oddFlag ? dailyBackgroundColorOdd : dailyBackgroundColorEven);
	        oddFlag = !oddFlag;

	        // Draw rectangle determined
    		canvas.drawRect(curRect, paint);

	        // Determine display
	        String thisDisplay = curSquare.getMessage();

	        // Show display
	        paint.setColor(dailyForegroundColor);
	        canvas.drawText(thisDisplay, curRect.left + 10, curRect.top + 10, paint);

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
	    
	    // draw enlarged square
	    if (enlargedSquare != null) {
	    	paint.setColor(enlargedBackgroundColor);
	    	setRect(curRect, enlargedSquare.getX(), enlargedSquare.getY());
	    	// embiggen rect
	    	float offset = (ENLARGED_SQUARE_MULTIPLIER - 1) / 2;
	    	curRect.left   -= calendarSquareSizeW * offset;
	    	curRect.right  += calendarSquareSizeW * offset;
	    	curRect.top    -= calendarSquareSizeH * offset;
	    	curRect.bottom += calendarSquareSizeH * offset;
	    	int minX = getPixelX(0), minY = getPixelY(0);
	    	// maxX: left side of rect when its right side is at the rightmost point
	    	// of the rightmost square
	    	// maxY: same, but subtract 1 for footer
	    	int maxX = getPixelX(DAYSINAWEEK + 1) - curRect.width();
	    	int maxY = getPixelY(WEEKSTODRAW) - curRect.height();
	    	// check left/right bounds
	    	if      (curRect.left < minX) curRect.offsetTo(minX, curRect.top);
	    	else if (curRect.left > maxX) curRect.offsetTo(maxX, curRect.top);
	    	// check top/bottom bounds
	    	if      (curRect.top  < minY) curRect.offsetTo(curRect.left, minY);
	    	else if (curRect.top  > maxY) curRect.offsetTo(curRect.left, maxY);
	    	canvas.drawRect(curRect, paint);
	    	
	    	paint.setColor(enlargedForegroundColor);
	    	canvas.drawText(enlargedSquare.getMessage(),
	    		curRect.left + 10, curRect.top + 10, paint);
	    	
	    }// end if

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
		// Daily number for days initialization
		int thisDailyNumber;
		
		// make arraylists to replace the originals when we're done
		ArrayList<ICalendarSquare> tmpCalendar =
				new ArrayList<ICalendarSquare>(WEEKSTODRAW * (DAYSINAWEEK + 1));
		ArrayList<DaySquare> tmpRealDays = new ArrayList<DaySquare>(daysInMonth);
		
		// Initialize calendar square size
		
		// Determine calendar height by subtracting margins
		calendarWidth = getWidth() - (calendarMarginX * 2);
		calendarHeight = getHeight() - (calendarMarginY * 2);
		
		// Determine individual square sizes
		calendarSquareSizeW = calendarWidth / (DAYSINAWEEK + 1);
			// plus one from weekly totals 
		calendarSquareSizeH = calendarHeight / (WEEKSTODRAW + 2);
			// plus header and footer

		for (int x = 0; x < squareTitles.length; x++) {
			tmpCalendar.add(new PlaceholderSquare(squareTitles[x], x, 0));
		}
		
//		PlaceholderSquare emptyDay = new PlaceholderSquare("-");
		
		// effective "day" of Sunday spot on first week
		Log.i("loadcalendar", ""+firstDay);
		thisDailyNumber = 1 - firstDay;
		
		// Vertical
		for (int yy = 1; yy < WEEKSTODRAW; yy++)
		{
			// used for weekly totals
			ArrayList<DaySquare> realDaysInWeek = new ArrayList<DaySquare>();
			// Horizontal
			for (int xx = 0; xx <= 6; xx++)
			{
				ICalendarSquare curSquare;
				
				if (thisDailyNumber > 0 && thisDailyNumber <= daysInMonth) {
					// create real day and add to weekSquares and realDays
					DaySquare daySquare = new DaySquare(thisDailyNumber, 0, 0, 0, xx, yy);
					curSquare = daySquare;
					realDaysInWeek.add(daySquare);
					tmpRealDays.add(daySquare);
				} else {
					// create placeholder day
					curSquare = new PlaceholderSquare("-", xx, yy);
				}
				
				// add whatever square we made to calendar and advance a day
				tmpCalendar.add(curSquare);
				thisDailyNumber++;
			} // end for xx
			// add total square for week
			if (realDaysInWeek.size() > 0) {
				tmpCalendar.add(new TotalSquare(realDaysInWeek, 7, yy));
			} else {
				tmpCalendar.add(new PlaceholderSquare("-", 7, yy));
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
	
	/**
	 * This procedure loads the calendar with the data for hours, and totals.
	 */
	public void loadCalendarData() {
		for (DaySquare square: realDays)
		{
			monthTotalGrantHours += square.grantHours;
			monthTotalNonGrantHours += square.nonGrantHours;
			monthTotalLeaveHours += square.leave;
			monthTotalHours += square.totalHours();
		}
		
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
	            // The touching X coordinate
	            int x = (int)event.getX();
	            // The touching Y coordinate
	            int y = (int)event.getY();
	            
	            // Check for tapping of arrows around enlarged box if a box is present
	            if (enlargedSquare != null && enlargedSquare instanceof DaySquare) {
	            	
	            	//tapped = true;
	            	
	            }
	            // If nothing tapped
	            if (!tapped) {
	            	// the square that was tapped before
	            	ICalendarSquare oldEnlargedSquare = enlargedSquare;
	            	
	            	enlargedSquare = null;
	            	
	            	// get touch position in terms of calendar squares
	            	int calX = getContainingSquareX(x);
	            	int calY = getContainingSquareY(y);
	            	
	            	// is tap pos in bounds? should prolly check margins vs getHeight()/Width() instead
	            	if (calX != -1 && calY != -1) {
	            		enlargedSquare = calendar.get(calY * 8 + calX);
	            		tapped = true;
	            		
	            		// repeated tap?
		            	if (oldEnlargedSquare == enlargedSquare
		            		&& calendarSquareListener != null)
		            	{
	            			calendarSquareListener.onTap(enlargedSquare);
		            	}
	            	} // end if in bounds
	            	if (oldEnlargedSquare != enlargedSquare) {
	            		invalidate();
	            	}
	            } // end if !tapped
	            return tapped;
	        }
	    };
	    return new GestureDetector(this.getContext(), gl);
	}

	public void loadTimes(JSONArray granthours, JSONArray nongranthours, JSONArray leavehours)
	{
		// load hours into calendar squares
		for (DaySquare cal : realDays)
		{
			cal.leave         = (int)leavehours   .optDouble(cal.dailyNumber-1, 0);
			cal.nonGrantHours = (int)nongranthours.optDouble(cal.dailyNumber-1, 0);
			cal.grantHours    = (int)granthours   .optDouble(cal.dailyNumber-1, 0);
		}
	}
	
	public void setOnCalSquareTapListener(OnCalSquareTapListener listener)
	{
		this.calendarSquareListener = listener;
	}
	
}

