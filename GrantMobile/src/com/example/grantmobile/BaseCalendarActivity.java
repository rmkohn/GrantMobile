package com.example.grantmobile;

import org.json.JSONArray;

import com.example.grantmobile.CalendarSquare.DaySquare;
import com.example.grantmobile.CalendarSquare.ICalendarSquare;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.ViewFlipper;

public abstract class BaseCalendarActivity extends FragmentActivity {
	
	protected static final String TAG_REQUEST_ID = "RequestId"; // required, no default!!!!
	protected static final String TAG_DAY_OF_MONTH = "DayOfMonth"; // optional, default = first day of month	
	
	public static final String requestURL = "http://mid-state.net/mobileclass2/android";
    
	TextView footerView;
	GridView calendarGrid;
	ViewFlipper headerFlipper;
	
	private CalendarArray calendar;
    
	/**
	 * This procedure initializes the layout.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_calendar_view);
		
		calendarGrid  = (GridView)    findViewById(R.id.calendarGrid);
		footerView    = (TextView)    findViewById(R.id.calendarFooter);
		headerFlipper = (ViewFlipper) findViewById(R.id.calendarHeaderFlipper);
		
		calendarGrid.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				openDetailView(calendar.getSquare(position));
			}
		});
	}
	
	/**
	 * This procedure loads the calendar with the data for hours, and totals.
	 */
	public void initFooterMessage() {
		int monthTotalGrantHours = 0;
		int monthTotalNonGrantHours = 0;
		int monthTotalLeaveHours = 0;
		int monthTotalHours = 0;
		
		for (int day = 1; day <= calendar.getNumberOfDays(); day++) {
			DaySquare square = calendar.getDay(day);
			monthTotalGrantHours += square.grantHours;
			monthTotalNonGrantHours += square.nonGrantHours;
			monthTotalLeaveHours += square.leave;
			monthTotalHours += square.totalHours();
		}
		
		// Determine footer message from monthly totals
		footerView.setText("Total Hours This Month: " +
			String.valueOf(monthTotalHours));
		
	}
	
	protected void createCalendarArray(int year, int month) {
		calendar = new CalendarArray(year, month);
		recreateAdapter();
	}
	
	public void loadCalendar(JSONArray granthours, JSONArray nongranthours,
			JSONArray leavehours) {
		calendar.loadTimes(granthours, nongranthours, leavehours);
		initFooterMessage();

		// finally, display calendar
		recreateAdapter();
	}
	
	public int getYear() {
		return calendar.year;
	}
	
	public int getMonth() {
		return calendar.monthNumber;
	}
	
	private void recreateAdapter() {
		calendarGrid.setAdapter(new CalendarSquareAdapter(this, calendar));
	}
	
	protected abstract void openDetailView(ICalendarSquare detailSquare);
	

}
