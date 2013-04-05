package com.example.grantmobile;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;


public class CalendarActivity extends FragmentActivity {
	
	// Name of grant
	String grantName;
	// 000-000 formatted number for grant
	String grantNumber = "";
	// State catalog number for grant
	String grantCatalogNumber = "";
	// Database ID of grant
	int grantId = 0;
	// Database ID of WorkMonth
	int workMonthId = 0;
	// Name of employee working on grant
	String employeeName = "";
	// Name of grant supervisor
	String supervisorName = "";

    
    CalendarView calendarView;
    
	/**
	 * This procedure initializes the layout.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		int id = getIntent().getIntExtra("workMonthId", -1);
		
		String destUri = UriHelper.serverAddress + "?q=email&ID="+id;
		new CalendarLoader(destUri.toString(), this).execute();
		
		setContentView(R.layout.activity_calendar_view);
		calendarView = (CalendarView) findViewById(R.id.calendarView1);
		
		calendarView.setOnCalSquareTapListener(new CalendarView.OnCalSquareTapListener()
		{
			public void onTap(CalendarSquare square)
			{
				openDetailView(square);
			}
		});
	}

	/**
	 * This procedure initializes the options menu.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	

	
	private void fillCalendarData(final int id) {
	    new CalendarLoader(UriHelper.serverAddress + "?q=email&id="+id, this).execute();
	}	
	
	
    class CalendarLoader extends UriHelper.JsonLoader<JSONObject>
    {
    	public CalendarLoader(String uri, Activity a) {
			super(uri, a);
		}

		private String getEmployeeName(JSONObject employeeObj) throws JSONException
    	{
    		return employeeObj.getString("firstname")
    		     + " "
    		     + employeeObj.getString("lastname");
    	}

		@Override
		protected void onSuccess(JSONObject result)
		{
			try
			{
				// get the date
				int month = result.getInt("month") + 1; // server months are 0-indexed
				int year  = result.getInt("year");
				
				// load some general class-level data that will be needed later
				supervisorName = getEmployeeName(result.getJSONObject("supervisor"));
				employeeName = getEmployeeName(result.getJSONObject("employee"));
				
				// get some info about the grant
				JSONObject grantinfo = result.getJSONObject("grant");
				grantId              = grantinfo.getInt("ID");
				grantName            = grantinfo.getString("grantTitle");
				grantNumber          = grantinfo.getString("grantNumber");
				grantCatalogNumber   = grantinfo.getString("stateCatalogNum");
				
				workMonthId = result.getInt("id");
				
				// get that info ready to display, also set year and monthNumber class-level variables
				calendarView.initHeaderMessage(month, year, grantName, grantNumber);
				
				// set up calendar, now that we know what month it is
				calendarView.initCalendar();
				
				// get time arrays
				JSONObject hours = result.getJSONObject("hours");
				JSONArray leavehours    = hours.getJSONArray("leave");
				JSONArray nongranthours = hours.getJSONArray("non-grant");
				JSONArray granthours    = hours.getJSONArray(Integer.toString(grantId));
				
				calendarView.loadTimes(granthours, nongranthours, leavehours);
				
				// finally, display calendar
				calendarView.loadCalendarData();
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}

		@Override
		protected void onFailure(String errorMessage) {
			super.onFailure(errorMessage);
			Toast.makeText(CalendarActivity.this, errorMessage, Toast.LENGTH_LONG).show();
		}
	}
	

    /**
     * This procedure handles all of the options menu selection events.
     */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	
		// Variables
		
		// The id of the menu item chosen
		int itemId = 0;	
		
		// Determine id of item chosen, and respond accordingly
		itemId = item.getItemId();
		
		switch (itemId) {
		case (R.id.mnuLoad)	:
			final EditText e = new EditText(this);
			new AlertDialog.Builder(this)
			.setTitle("pick id")
			.setView(e)
			.setPositiveButton("ok", new AlertDialog.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					fillCalendarData(Integer.parseInt(e.getText().toString()));
				}
			})
			.show();
			
			break;
		case (R.id.mnuDetail) :
			Intent intent = new Intent(this, DetailViewActivity.class);
			startActivity(intent);
			break;
		case (R.id.mnuDialog) :
			//show dialog box
			SubmitDialog dialog = new SubmitDialog();
			FragmentManager manager = getSupportFragmentManager();
			dialog.setUserID(732);
			dialog.show(manager, "");
			break;
		}// end switch
		
		return true;
		
	}
	
	
	private void openDetailView(CalendarSquare detailSquare)
	{
		if (detailSquare.dailyNumber < 1)
			return;
		
		
		Intent i = new Intent(this, DetailViewActivity.class);
		
		int daysInMonth = calendarView.daysInMonth;
		float[] granthrs = new float[daysInMonth];
		float[] leavehrs = new float[daysInMonth];
		float[] nongranthrs = new float[daysInMonth];
		
		for (CalendarSquare square : calendarView.calendar)
		{
			if (square.dailyNumber > 0)
			{
				granthrs   [square.dailyNumber - 1] = square.grantHours;
				nongranthrs[square.dailyNumber - 1] = square.nonGrantHours;
				leavehrs   [square.dailyNumber - 1] = square.leave;
			}
		}
		i.putExtra("GHRS",          granthrs);
		i.putExtra("NGHRS",         nongranthrs);
		i.putExtra("LHRS",          leavehrs);
		i.putExtra("YR",            calendarView.year);
		i.putExtra("MNTH",          calendarView.monthNumber);
		i.putExtra("dateOfMonth",   detailSquare.dailyNumber);
		i.putExtra("grantName",     grantName);
		i.putExtra("grantNumber",   grantNumber);
		i.putExtra("employeeName",  employeeName);
		i.putExtra("catalogNumber", grantCatalogNumber);
		i.putExtra("workMonth",     workMonthId);
		
		startActivity(i);
	}
	

}
