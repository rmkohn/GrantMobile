package com.example.grantmobile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.grantmobile.CalendarSquare.DaySquare;
import com.example.grantmobile.CalendarSquare.ICalendarSquare;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public class CalendarActivity extends BaseCalendarActivity {
	
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
	
	TextView headerView;

	/**
	 * This procedure initializes the layout.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		headerView = (TextView) findViewById(R.id.calendarHeader);
		headerFlipper.setDisplayedChild(headerFlipper.indexOfChild(headerView));
		
		workMonthId = getIntent().getIntExtra("workMonthId", -1);
		
		if (isServiceBound())
			fillCalendarData(workMonthId);
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

	protected void onBound() {
		fillCalendarData(workMonthId);
	}
	
	private void fillCalendarData(final int id) {
		getService().sendEmailRequest(String.valueOf(id), new CalendarResultHandler());
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
				grantApprovalStatus  = result.optString("status", "none");
				grantName            = grantinfo.getString("grantTitle");
				grantNumber          = grantinfo.getString("grantNumber");
				grantCatalogNumber   = grantinfo.getString("stateCatalogNum");

				workMonthId = result.getInt("id");

				// if pending
				if (grantApprovalStatus.equals("pending")) {
					// get that info ready to display, also set year and monthNumber class-level variables
					initHeaderMessage(month, year, grantName, grantNumber, employeeName);

					// set up calendar, now that we know what month it is
					createCalendarArray(year, month);

					// get time arrays
					JSONObject hours = result.getJSONObject("hours");
					JSONArray leavehours    = hours.getJSONArray("leave");
					JSONArray nongranthours = hours.getJSONArray("non-grant");
					JSONArray granthours    = hours.getJSONArray("grant");

					loadCalendar(granthours, nongranthours, leavehours);
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
			
			// show dialog for closing early
			closeEarlyDialog("none");
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
			openDialog();
			break;
		}// end switch
		
		return true;
	}
	
	private void openDialog() {
		//show dialog box
		SubmitDialog dialog = new SubmitDialog();
		FragmentManager manager = getSupportFragmentManager();
		dialog.setUserID(732);
		dialog.show(manager, "");
	}

	@Override
	protected void openDetailView(ICalendarSquare detailSquare)
	{
		if (!(detailSquare instanceof DaySquare))
			return;
		DaySquare square = (DaySquare) detailSquare;
		
		Intent i = new Intent(this, DetailViewActivity.class);
		
		i.putExtra(TAG_REQUEST_ID, String.valueOf(workMonthId));
		i.putExtra(TAG_DAY_OF_MONTH, String.valueOf(square.dailyNumber));
		
		startActivity(i);
	}

	private void closeEarlyDialog(String status) {
		
		// Variables for title and message
		String title = "";
		String message = "";
		
		// Determine title and message
		if (status.equals("new")) {
			
			title = "Not Available Yet";
			message = "This grant is still being filled out. You shouldn't even see this.";
			
		} else {

			if (status.equals("none")) {

				title = "No Grant";
				message = "There is no grant here. How did you get here?";

			} else {

				if (status.equals("disapproved")) {
				
					title = "Grant Disapproved";
					message = "Entries for this month have not yet been resubmitted.";
				
				} else {
					
					title = "Grant Approved";
					message = "This grant has already been approved.";
					
				}// end if

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
	
	@Override
	protected void onEmailButtonClicked() {
		openDialog();
	}
		/**
	 * This procedure initializes the header message with sample data
	 */
	private void initHeaderMessage()
	{	
		// load sample data
		initHeaderMessage(1, 2000, "Loading", "Loading", "Palmer Eldritch");
	}
	
	/**
	 * This procedure initializes the header message.
	 */
	public void initHeaderMessage(int month, int headerYear, String grantName, String grantCatalogNum, String employeeName)
	{
		// Variables
		
		// Long date message
		String longDate = "";
		
		// Load month, year, and grant name
		// (Using provided data)
		String grant = grantName + " " + grantCatalogNum;
		
		String monthName = CalendarArray.monthNames[month-1];
	
		// Determine long date
		longDate = monthName + ", " + String.valueOf(headerYear);
				
		// Determine header message
		headerView.setText(grant.trim() + "\n" + "Employee Name: " + employeeName + "\n" + longDate);
	}

}
