package com.example.grantmobile;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.json.JSONObject;

import com.example.grantmobile.GrantService.GrantData;
import com.example.grantmobile.GrantService.WeakrefHandler;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class DetailEditActivity extends Activity {
	// these two named parameter are for the Intent interface to this activity (both reference Strings)
	public static final String TAG_REQUEST_ID = "RequestId"; // required, no default!!!!
	public static final String TAG_DAY_OF_MONTH = "DayOfMonth"; // optional, default is first day of month

	double[] grantHours; // grant hour array 
	double[] nonGrantHours; // non-grant hour array
	double[] leaveHours;  // leave hour array
	
//	private static final String TAG_HOURS = "hours"; 
//	private static final String TAG_GRANT = "grant";
	private static final String TAG_NON_GRANT = "non-grant";
	private static final String TAG_LEAVE = "leave";
	
	private int month;
	private int year;
	
	private int employeeid;
	private String firstName;
	private String lastName;
	
	private int grantId;
	String stateCatalogNum;
	private String grantNumber;
	private String grantTitle;
	
	// day of week string array
	static final String[] DOW = { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday",
							"Friday", "Saturday" };
	static final String[] MOY = { "January", "February", "March", "April", "May", "June",
									"July", "August", "September", "October", "November", "December" };
		
	int dowStart; // month starts on Wednesday for test, use WED - 1 
	int domCurrent; // current day of month
	int moy;
	
	TextView grantNameView;
	TextView grantIdView;
	TextView employeeNameView;
	TextView catalogView;
		
	TextView dateView;
	TextView dayView;
	EditText grantHoursView;
	EditText nonGrantHoursView;
	EditText leaveHoursView;
	
	TextView dayTotalHoursView;
	TextView monthTotalHoursView;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_detail_view);
		
//		grantHours = new ArrayList<String>(); // grant hour array 
//		nonGrantHours = new ArrayList<String>(); // non-grant hour array
//		leaveHours = new ArrayList<String>();  // leave hour array
//		
		// intent interface
		Intent intent = getIntent();
		domCurrent = intent.getIntExtra(TAG_DAY_OF_MONTH, 0);
		GrantData grantdata = (GrantData)intent.getSerializableExtra(GrantService.TAG_REQUEST_DETAILS);
		year = grantdata.year;
		month = grantdata.month;
		employeeid = grantdata.employeeid;
		grantId = intent.getIntExtra("grantid", -1);
		
		// setup view for displaying grant info
		grantNameView = (TextView)findViewById(R.id.grantNameView);
		grantIdView = (TextView)findViewById(R.id.grantIdView);
		employeeNameView = (TextView)findViewById(R.id.employeeNameView);
		catalogView = (TextView)findViewById(R.id.catalogView);

		// setup view for displaying specific info for a given date
		dateView = (TextView)findViewById(R.id.dateView);
		dayView = (TextView)findViewById(R.id.dayOfWeekView);
		grantHoursView = (EditText)findViewById(R.id.grantHoursView);
		nonGrantHoursView = (EditText)findViewById(R.id.nonGrantHoursView);
		leaveHoursView = (EditText)findViewById(R.id.leaveHoursView);

		dayTotalHoursView = (TextView)findViewById(R.id.dayTotalHoursView);
		monthTotalHoursView = (TextView)findViewById(R.id.monthTotalHoursView);
		
        final Button backwardButton = (Button) findViewById(R.id.backBtn);
        backwardButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	saveHours();
            	if (--domCurrent == 0) domCurrent = grantHours.length; // underflow wrap
            	updateView();
            }
        });
        final Button forwardButton = (Button) findViewById(R.id.nextBtn);
        forwardButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	saveHours();
            	if (++domCurrent >  grantHours.length) domCurrent = 1; // overflow wrap
            	updateView();
            }
        });
        
        final Button returnButton = (Button) findViewById(R.id.returnBtn);
        returnButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	saveHours();
                Toast.makeText(DetailEditActivity.this, "Return to Calendar View", Toast.LENGTH_LONG).show();
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        
        OnFocusChangeListener editListener = new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				saveHours();
			}
		};
		
		// prevent NPE if onFocusChange fires before we've loaded hours from the server
		int monthLength = new GregorianCalendar(year, month, 1).getActualMaximum(Calendar.DATE);
		grantHours    = new double[monthLength];
		leaveHours    = new double[monthLength];
		nonGrantHours = new double[monthLength];
		
		grantHoursView   .setOnFocusChangeListener(editListener);
		nonGrantHoursView.setOnFocusChangeListener(editListener);
		leaveHoursView   .setOnFocusChangeListener(editListener);
        
		String[] hours = { String.valueOf(grantId), "non-grant", "leave" };
		GrantService.getHours(this, new DetailHandler().setParent(this), grantdata, hours);
	}
        
    private void updateView() {
		String domString = String.valueOf(domCurrent);
    	
		// figure out what first day-of-week is for this month (dowStart)
    	Calendar cal = Calendar.getInstance();
    	cal.set(Calendar.DATE,1);
    	cal.set(Calendar.MONTH, month);
    	cal.set(Calendar.YEAR, year);
    	cal.set(Calendar.DAY_OF_MONTH, 1); 	
        dowStart = (cal.get(Calendar.DAY_OF_WEEK)-Calendar.SUNDAY)%7;
        
        grantNameView.setText(grantTitle);
		grantIdView.setText(grantNumber);
		employeeNameView.setText(firstName + " " + lastName);
		catalogView.setText(stateCatalogNum);
    	
		moy = month;
    	dateView.setText(MOY[moy]+" "+domString+", "+year);
    	dayView.setText(DOW[(domCurrent+dowStart-1)%7]);
    	grantHoursView.setText(String.valueOf(grantHours[domCurrent-1]));
    	nonGrantHoursView.setText(String.valueOf(nonGrantHours[domCurrent-1]));
    	leaveHoursView.setText(String.valueOf(leaveHours[domCurrent-1]));
    		
    	double dayTotal = grantHours[domCurrent-1] + nonGrantHours[domCurrent-1] + leaveHours[domCurrent-1];
    	dayTotalHoursView.setText(String.valueOf(dayTotal));
    	double tgh = 0;
    	for (double gh: grantHours)
    		tgh = tgh + gh;
    	monthTotalHoursView.setText(String.valueOf(tgh));
	}
    private void saveHours() {
    	double grant    = getTimeFromView(grantHoursView);
    	double nongrant = getTimeFromView(nonGrantHoursView);
    	double leave    = getTimeFromView(leaveHoursView);
    	grantHours   [domCurrent-1] = grant;
    	leaveHours   [domCurrent-1] = leave;
    	nonGrantHours[domCurrent-1] = nongrant;
    }
    private double getTimeFromView(EditText view) {
    	try {
    		return Double.valueOf(view.getText().toString());
    	} catch (NumberFormatException e) {
    		return 0;
    	}
    }
	
	public static class DetailHandler extends WeakrefHandler<DetailEditActivity> {
		@Override
		public void handleMessage(Message message) {
			DetailEditActivity a = parent.get();
			if (a == null)
			{
				return;
			}
			if (message.arg1 == RESULT_OK) {
				a.grantHours = message.getData().getDoubleArray(String.valueOf(a.grantId));
				a.nonGrantHours = message.getData().getDoubleArray(TAG_NON_GRANT);
				a.leaveHours = message.getData().getDoubleArray(TAG_LEAVE);
				a.updateView();
			} else {
				Toast.makeText(a, "Download failed.",
						Toast.LENGTH_LONG).show();
			}
		}
	}
	
	public static class SaveHandler extends WeakrefHandler<DetailEditActivity> {
		public void handleMessage(Message message) {
			String result = (message.arg1 == Activity.RESULT_OK)
				? "hours saved"
				: "something went wrong";
			Toast.makeText(parent.get(), result, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	    GrantData data = new GrantData(year, month, employeeid);
	    Bundle hourBundle = new Bundle();
	    hourBundle.putDoubleArray(TAG_NON_GRANT, nonGrantHours);
	    hourBundle.putDoubleArray(TAG_LEAVE, leaveHours);
	    hourBundle.putDoubleArray(String.valueOf(grantId), grantHours);
	    GrantService.saveHours(this, new SaveHandler().setParent(this), data, hourBundle);
	}

}