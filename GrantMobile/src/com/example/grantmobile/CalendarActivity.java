package com.example.grantmobile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.grantmobile.CalendarSquare.DaySquare;
import com.example.grantmobile.CalendarSquare.ICalendarSquare;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
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
	// Database ID of request
	int workmonthId = 0;
	// Database ID of supervisor
	int supervisorId = 0;
	// Uri the app was opened with
	Uri launchUri = null;
	// Grant approval status
	String grantApprovalStatus = "";
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
		
//		workMonthId = getIntent().getIntExtra("workMonthId", -1);
		launchUri = getIntent().getParcelableExtra("launchUri");
		
		if (isServiceBound())
			fillCalendarData(launchUri);
	}
	
	protected void onBound() {
		fillCalendarData(launchUri);
	}
	
	private void fillCalendarData(Uri uri) {
		getService().sendEmailRequest(uri, new CalendarResultHandler(this));
	}	
	
    class CalendarResultHandler extends JSONParser.SimpleResultHandler<JSONObject>
    {

		public CalendarResultHandler(Context ctx) {
			super(ctx);
		}

		private String getEmployeeName(JSONObject employeeObj) throws JSONException
    	{
    		return employeeObj.getString("firstname")
    		     + " "
    		     + employeeObj.getString("lastname");
    	}

		@Override
		public void onSuccess(JSONObject result)
		{
			try
			{
				// get the date
				int month = result.getInt("month") + 1; // server months are 0-indexed
				int year  = result.getInt("year");
				
				workmonthId = result.getInt("id");
				supervisorId = result.getJSONObject("supervisor").getInt("id");

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
	
	
	private void openDialog() {
		//show dialog box
		SubmitDialog dialog = new SubmitDialog();
		FragmentManager manager = getSupportFragmentManager();
		dialog.setUserID(supervisorId);
		dialog.setWorkmonthID(workmonthId);
		dialog.show(manager, "");
	}

	@Override
	protected void openDetailView(ICalendarSquare detailSquare)
	{
		if (!(detailSquare instanceof DaySquare))
			return;
		DaySquare square = (DaySquare) detailSquare;
		
		Intent i = new Intent(this, DetailViewActivity.class);
		
		i.putExtra("launchUri", getIntent().getParcelableExtra("launchUri"));
		
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
			message = "This grant is being filled out for the first time.";
			
		} else {

			if (status.equals("none")) {

				title = "No Grant";
				message = "There is no grant with that ID.";

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
		
		String monthName = GrantApp.monthNames[month-1];
	
		// Determine long date
		longDate = monthName + ", " + String.valueOf(headerYear);
				
		// Determine header message
		headerView.setText(grant.trim() + "\n" + "Employee Name: " + employeeName + "\n" + longDate);
	}

}
