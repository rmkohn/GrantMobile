package com.example.grantmobile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class CalendarActivity extends FragmentActivity {
	
	private static final String TAG_REQUEST_ID = "RequestId"; // required, no default!!!!
	private static final String TAG_DAY_OF_MONTH = "DayOfMonth"; // optional, default = first day of month	
	
	public static final String requestURL = "http://mid-state.net/mobileclass2/android";
	
	// Name of grant
	String grantName;
	// 000-000 formatted number for grant
	String grantNumber = "";
	// State catalog number for grant
	String grantCatalogNumber = "";
	// Database ID of grant
	int grantId = 0;
	// Grant approval status
	String grantApprovalStatus = "";
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
		
		fillCalendarData(id);
		
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
		new JSONParser.RequestBuilder(requestURL)
		.setUrl(requestURL)
		.addParam("q", "email")
		.addParam("id", String.valueOf(id))
		.makeRequest(new CalendarResultHandler());
		
	}	
	
    class CalendarResultHandler extends JSONParser.SimpleResultHandler
    {

		private String getEmployeeName(JSONObject employeeObj) throws JSONException
    	{
    		return employeeObj.getString("firstname")
    		     + " "
    		     + employeeObj.getString("lastname");
    	}

		@Override
		public void onSuccess(Object oResult)
		{
			
			
			try
			{
				JSONObject result = (JSONObject) oResult;
				// get the date
				int month = result.getInt("month") + 1; // server months are 0-indexed
				int year  = result.getInt("year");
				
				// load some general class-level data that will be needed later
				supervisorName = getEmployeeName(result.getJSONObject("supervisor"));
				employeeName = getEmployeeName(result.getJSONObject("employee"));
				
				// get some info about the grant
				JSONObject grantinfo = result.getJSONObject("grant");
				grantId              = grantinfo.getInt("ID");
				if (result.isNull("status")) {
					grantApprovalStatus  = "none";
				} else {
					grantApprovalStatus  = result.getString("status");
				}// end if
				grantName            = grantinfo.getString("grantTitle");
				grantNumber          = grantinfo.getString("grantNumber");
				grantCatalogNumber   = grantinfo.getString("stateCatalogNum");
				
				workMonthId = result.getInt("id");
				
				// if pending
				if (grantApprovalStatus == "pending") {
				
					// get that info ready to display, also set year and monthNumber class-level variables
					calendarView.initHeaderMessage(month, year, grantName, grantNumber, employeeName);
					
					// set up calendar, now that we know what month it is
					calendarView.initCalendar();
					calendarView.setFrag(getSupportFragmentManager());
					
					// get time arrays
					JSONObject hours = result.getJSONObject("hours");
					JSONArray leavehours    = hours.getJSONArray("leave");
					JSONArray nongranthours = hours.getJSONArray("non-grant");
					JSONArray granthours    = hours.getJSONArray("grant");
					
					calendarView.loadTimes(granthours, nongranthours, leavehours);
					
					// finally, display calendar
					calendarView.loadCalendarData();
				
				} else {
					
					// show dialog for closing early
					closeEarlyDialog(grantApprovalStatus);
					
				}// end if
				
			}
			catch (JSONException e)
			{
				Toast.makeText(getApplicationContext(), "Error loading grant information", Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
		}

		@Override
		public void onFailure(String errorMessage) {
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
		
		i.putExtra(TAG_REQUEST_ID, String.valueOf(workMonthId));
		i.putExtra(TAG_DAY_OF_MONTH, String.valueOf(detailSquare.dailyNumber));
		
		startActivity(i);
	}
	

	private void closeEarlyDialog(String status) {
		
		// Variables for title and message
		String title = "";
		String message = "";
		
		// Determine title and message
		if (status == "new") {
			
			title = "Not Availabley Yet";
			message = "This grant is still being filled out. You shouldn't even see this.";
			
		} else {
			
			if (status == "none") {
			
				title = "No Grant";
				message = "There is no grant here. How did you get here?";
				
			} else {
				
				title = "Already Handled";
				message = "Grant already handled";
				
			}// end if
		
		}// end if
			
		// Show dialog with determined information
		new AlertDialog.Builder(this)
		.setTitle(title)
		.setMessage(message)
		.setNeutralButton("OK", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				
				finish();
				
			}
			
		})
		.setCancelable(false)
		.show();
		
	}


}
