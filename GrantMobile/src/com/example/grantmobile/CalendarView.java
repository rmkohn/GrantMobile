//package com.example.grantmobile;
//
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Collections;
//import java.util.GregorianCalendar;
//import java.util.List;
//
//import org.json.JSONArray;
//
//import com.example.grantmobile.CalendarSquare.DaySquare;
//import com.example.grantmobile.CalendarSquare.ICalendarSquare;
//import com.example.grantmobile.CalendarSquare.PlaceholderSquare;
//import com.example.grantmobile.CalendarSquare.TotalSquare;
//
//import android.content.Context;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.graphics.Paint.Style;
//import android.graphics.Rect;
//import android.util.AttributeSet;
//import android.util.Log;
//import android.view.GestureDetector;
//import android.view.GestureDetector.SimpleOnGestureListener;
//import android.view.MotionEvent;
//import android.view.View;
//import android.widget.Toast;
//
//public class CalendarView extends View {
//	
//	// Constants
//
//	// Calendar drawing constants
//	
//	// Header background color
//	private static final int headerBackgroundColor = Color.BLACK;
//	// Header foreground color
//	private static final int headerForegroundColor = Color.WHITE;										 
//	// Daily background color odd
//	private static final int dailyBackgroundColorOdd = Color.WHITE;
//	// Daily background color even
//	private static final int dailyBackgroundColorEven = Color.LTGRAY;	
//	// Daily foreground color
//	private static final int dailyForegroundColor = Color.RED;
//	// Footer background color
//	private static final int footerBackgroundColor = Color.BLACK;
//	// Footer foreground color 
//	private static final int footerForegroundColor = Color.WHITE;
//	// Enlarged square background color
//	private static final int enlargedBackgroundColor = Color.BLUE;
//	// Enlarged square foreground color 
//	private static final int enlargedForegroundColor = Color.YELLOW;
//	// Calendar initial horizontal margin
//	private static final int calendarMarginX = 10;
//	// Calendar initial vertical margin
//	private static final int calendarMarginY = 10;
//	// Variables
//	
//	// The paint brush
//	Paint paint = new Paint();
//	// A placeholder rectangle for drawing
//	Rect curRect = new Rect();
//	// Detector for taps on calendar squares
//	GestureDetector tapDetector;
//	// Listener to send taps on calendar squares to
//	OnCalSquareTapListener calendarSquareListener;
//	
//	// Calendar settings
//	
//	// The enlarged day square
//	ICalendarSquare enlargedSquare;
//	// how much larger the enlarged square is than a regular square
//	private static final float ENLARGED_SQUARE_MULTIPLIER = 3.0f;
//	
//	
//	public static interface OnCalSquareTapListener
//	{
//		public void onTap(ICalendarSquare square);
//	}
//		
//	public CalendarView(Context context) {
//        super(context);
//        init();
//    }
//
//    public CalendarView(Context context, AttributeSet attrs, int defStyle) {
//        super(context, attrs, defStyle);
//        init();
//    }
//
//    public CalendarView(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        init();
//    }
//    
//    public void init()
//    {
//        // Initialize header message
//        initHeaderMessage();
//        
//        // Initialize gesture listener
//        tapDetector = getGestureListener();
//    }
//    
//    /*
//     * conveninece methods to convert between calendar square positions
//     * and pixels
//     * These should be the only methods to know about details like the
//     * header and margins
//     */
//    // get left pixel X from calendar square X
//    private int getPixelX(int squareX) {
//    	return calendarMarginX + squareX * calendarSquareSizeW;
//    }
//    
//    // get top pixel Y from calendar square Y
//    private int getPixelY(int squareY) {
//    	return calendarMarginY + (squareY+1) * calendarSquareSizeH;
//    }
//    
//    // get calendar square X from pixel X, or -1 if there is no such square
//    private int getContainingSquareX(int x) {
//    	int squareX = (x - calendarMarginX) / calendarSquareSizeW;
//    	if (squareX < 0 || squareX >= DAYSINAWEEK + 1)
//    		return -1;
//    	return squareX;
//    }
//    
//    // get cqlendar square Y from pixel Y, or -1 if there is no such square
//    private int getContainingSquareY(int y) {
//    	int squareY = (y - calendarMarginY) / calendarSquareSizeH - 1;
//    	if (squareY < 0 || squareY >= WEEKSTODRAW)
//    		return -1;
//    	return squareY;
//    }
//    
//    // convenience method to set curRect
//    private void setRect(Rect rect, int x, int y) {
//    	int posX = getPixelX(x);
//    	int posY = getPixelY(y);
//        rect.set(posX, posY, posX + calendarSquareSizeW, posY + calendarSquareSizeH);
//    }
//    
//	/**
//	 * This procedure draws the screen with the current details.
//	 */
//	@Override
//	protected void onDraw (Canvas canvas)
//	{
//	    // Variables
//
//	    // The width for the header and footer
//	    int headerAndFooterWidth;
//	    // Footer Y position
//	    int footerY;
//	    // Odd or even flag
//	    Boolean oddFlag = true;
//
//	    // Calculate header and footer widths
//	    headerAndFooterWidth = calendarSquareSizeW * (DAYSINAWEEK + 1);
//
//	    // Draw header
//	    paint.setColor(headerBackgroundColor);
//	    paint.setStyle(Style.FILL_AND_STROKE);
//	    curRect.set(
//	            calendarMarginX,
//	            calendarMarginY,
//	            calendarMarginX + headerAndFooterWidth,
//	            calendarMarginY + calendarSquareSizeH
//	            );
//	    canvas.drawRect(curRect, paint);
//	    paint.setColor(headerForegroundColor);
//	    canvas.drawText(headerMessage, calendarMarginX + 10,
//	            calendarMarginY + 10, paint);
//
//	    // Draw inner calendar squares
//	    paint.setStyle(Style.FILL_AND_STROKE);
//
//	    // Loop of using calendar square index
//	    for ( ICalendarSquare curSquare: calendar) {
//	    	setRect(curRect, curSquare.getX(), curSquare.getY());
//
//	        // Determine color
//	        paint.setColor(oddFlag ? dailyBackgroundColorOdd : dailyBackgroundColorEven);
//	        oddFlag = !oddFlag;
//
//	        // Draw rectangle determined
//    		canvas.drawRect(curRect, paint);
//
//	        // Determine display
//	        String thisDisplay = curSquare.getMessage();
//
//	        // Show display
//	        paint.setColor(dailyForegroundColor);
//	        canvas.drawText(thisDisplay, curRect.left + 10, curRect.top + 10, paint);
//
//	    }// end for
//
//	    // Determine footer positions
//	    footerY = 
//	            calendarMarginY + (calendarSquareSizeH * (WEEKSTODRAW + 1));
//
//	    // Draw footer
//	    paint.setColor(footerBackgroundColor);
//	    paint.setStyle(Style.FILL_AND_STROKE);
//	    curRect.set(
//	            calendarMarginX,
//	            footerY,
//	            calendarMarginX + headerAndFooterWidth,
//	            footerY + calendarSquareSizeH
//	            );
//	    canvas.drawRect(curRect, paint);
//
//	    // Draw footer text
//	    paint.setColor(footerForegroundColor);
//	    canvas.drawText(footerMessage, calendarMarginX + 10,
//	            footerY + 10, paint);
//	    
//	    // draw enlarged square
//	    if (enlargedSquare != null) {
//	    	paint.setColor(enlargedBackgroundColor);
//	    	setRect(curRect, enlargedSquare.getX(), enlargedSquare.getY());
//	    	// embiggen rect
//	    	float offset = (ENLARGED_SQUARE_MULTIPLIER - 1) / 2;
//	    	curRect.left   -= calendarSquareSizeW * offset;
//	    	curRect.right  += calendarSquareSizeW * offset;
//	    	curRect.top    -= calendarSquareSizeH * offset;
//	    	curRect.bottom += calendarSquareSizeH * offset;
//	    	int minX = getPixelX(0), minY = getPixelY(0);
//	    	// maxX: left side of rect when its right side is at the rightmost point
//	    	// of the rightmost square
//	    	// maxY: same, but subtract 1 for footer
//	    	int maxX = getPixelX(DAYSINAWEEK + 1) - curRect.width();
//	    	int maxY = getPixelY(WEEKSTODRAW) - curRect.height();
//	    	// check left/right bounds
//	    	if      (curRect.left < minX) curRect.offsetTo(minX, curRect.top);
//	    	else if (curRect.left > maxX) curRect.offsetTo(maxX, curRect.top);
//	    	// check top/bottom bounds
//	    	if      (curRect.top  < minY) curRect.offsetTo(curRect.left, minY);
//	    	else if (curRect.top  > maxY) curRect.offsetTo(curRect.left, maxY);
//	    	canvas.drawRect(curRect, paint);
//	    	
//	    	paint.setColor(enlargedForegroundColor);
//	    	canvas.drawText(enlargedSquare.getMessage(),
//	    		curRect.left + 10, curRect.top + 10, paint);
//	    	
//	    }// end if
//
//	}
//	
//	/**
//	 * This procedure handles touch events. Currently, in the event of a
//	 * square's touching, it displays the daily number of that square.
//	 */
//	@Override
//	public boolean onTouchEvent(MotionEvent event)
//	{
//		Log.i("drawview", "got touch evt "+Math.round(event.getX())+","+Math.round(event.getY()));
//		// standard gesture handler, route events to GestureDetector
//		if (tapDetector.onTouchEvent(event))
//		        return true;
//		return super.onTouchEvent(event);
//	}	
//
//
//	private GestureDetector getGestureListener()
//	{
//	    SimpleOnGestureListener gl = new GestureDetector.SimpleOnGestureListener()
//	    {
//			@Override
//	        public boolean onDown(MotionEvent event)
//	        {
//				// Flag for tapping
//				boolean tapped = false;
//	            // The touching X coordinate
//	            int x = (int)event.getX();
//	            // The touching Y coordinate
//	            int y = (int)event.getY();
//	            
//	            // Check for tapping of arrows around enlarged box if a box is present
//	            if (enlargedSquare != null && enlargedSquare instanceof DaySquare) {
//	            	
//	            	//tapped = true;
//	            	
//	            }
//	            // If nothing tapped
//	            if (!tapped) {
//	            	// the square that was tapped before
//	            	ICalendarSquare oldEnlargedSquare = enlargedSquare;
//	            	
//	            	enlargedSquare = null;
//	            	
//	            	// get touch position in terms of calendar squares
//	            	int calX = getContainingSquareX(x);
//	            	int calY = getContainingSquareY(y);
//	            	
//	            	// is tap pos in bounds? should prolly check margins vs getHeight()/Width() instead
//	            	if (calX != -1 && calY != -1) {
//	            		enlargedSquare = calendar.get(calY * 8 + calX);
//	            		tapped = true;
//	            		
//	            		// repeated tap?
//		            	if (oldEnlargedSquare == enlargedSquare
//		            		&& calendarSquareListener != null)
//		            	{
//	            			calendarSquareListener.onTap(enlargedSquare);
//		            	}
//	            	} // end if in bounds
//	            	if (oldEnlargedSquare != enlargedSquare) {
//	            		invalidate();
//	            	}
//	            } // end if !tapped
//	            return tapped;
//	        }
//	    };
//	    return new GestureDetector(this.getContext(), gl);
//	}
//
//
//	
//	public void setOnCalSquareTapListener(OnCalSquareTapListener listener)
//	{
//		this.calendarSquareListener = listener;
//	}//
//	
//}
//
