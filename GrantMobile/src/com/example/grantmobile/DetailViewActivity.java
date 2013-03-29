package com.example.grantmobile;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class DetailViewActivity extends Activity {

	static final String[] DOW = 
			new String[] { "Monday", "Tuesday", "Wednesday", "Thursday",
							"Friday", "Saturday", "Sunday" };
	
	static final String[] GHRS = 
			new String[] { "4", "6" , "6", "0", "0",
							"3", "4" , "5", "4", "2", "0", "0",	
							"5", "2", "1", "0" , "0", "0", "0",
							"7", "6", "5", "4" , "3", "2", "1",
							"3", "6", "7", "3" , "0"};
	static final String[] NGHRS = 
			new String[] { "4", "2" , "2", "0", "0",
							"7", "4" , "3", "4", "6", "0", "0",
							"3", "6", "7", "8" , "0", "0", "0",
							"1", "2", "3", "4" , "5", "6", "7",
							"5", "2", "1", "5" , "8"};
	static final String[] LHRS = 
			new String[] { "0", "0" , "0", "0", "0",
							"0", "0" , "0", "0", "0", "0", "0",
							"0", "0" , "0", "0", "8", "0", "0",
							"0", "0", "0", "0" , "0", "0", "0",
							"0", "0", "0", "0" , "0"};
	
	// these arguments will be passed in via intent from calendar view
	static final String YR = "2013";
	static final String MNTH = "5";
	static final String dateOfMonth = "14";
	static final String grantName = "U.S. Grant";
	static final String grantId = "111-111";
	static final String employeeName = "Davy Jones";
	static final String catalogNumber = "11.111";
	
	// GHRS[], NGHRS[] and LHRS[] will be gotten from grant database
	// DOM.length, DOW[] (Mon=0; Sun=6) must be determined from date
	Integer dow_start = 1; // month starts on Wednesday for test, use WED - 1 
	Integer dom_current = Integer.valueOf(dateOfMonth);
	
	TextView dateView;
	TextView dayView;
	TextView grantHoursView;
	TextView nonGrantHoursView;
	TextView leaveHoursView;
	
	TextView dayTotalHoursView;
	TextView monthTotalHoursView;
	
	Context context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail_view);
		context = this;
		
		// write out preamble
		TextView grantNameView = (TextView)findViewById(R.id.grantNameView);
		TextView grantIdView = (TextView)findViewById(R.id.grantIdView);
		TextView employeeNameView = (TextView)findViewById(R.id.employeeNameView);
		TextView catalogView = (TextView)findViewById(R.id.catalogView);
		
		grantNameView.setText(grantName);
		grantIdView.setText(grantId);
		employeeNameView.setText(employeeName);
		catalogView.setText(catalogNumber);
		
		// setup view for displaying specific data for a given date
		dateView = (TextView)findViewById(R.id.dateView);
		dayView = (TextView)findViewById(R.id.dayOfWeekView);
		grantHoursView = (TextView)findViewById(R.id.grantHoursView);
		nonGrantHoursView = (TextView)findViewById(R.id.nonGrantHoursView);
		leaveHoursView = (TextView)findViewById(R.id.leaveHoursView);

		dayTotalHoursView = (TextView)findViewById(R.id.dayTotalHoursView);
		monthTotalHoursView = (TextView)findViewById(R.id.monthTotalHoursView);
	
		// write out date specific data
		updateView();
		
        final Button backwardButton = (Button) findViewById(R.id.button1);
        backwardButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	if (--dom_current == 0) dom_current = GHRS.length;  // underflow wrap
            	updateView();
            }
        });
        final Button forwardButton = (Button) findViewById(R.id.button2);
        forwardButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if (++dom_current >  GHRS.length) dom_current = 1; // overflow wrap
            	updateView();
            }
        });
        
        final Button returnButton = (Button) findViewById(R.id.button3);
        returnButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(context, "Return to Calendar View", Toast.LENGTH_LONG).show();
            }
        });
	}
        
    private void updateView() {
    	
		String DOM_string = String.valueOf(dom_current);
		String date = MNTH + "/" + DOM_string + "/" + YR;
		
    	dateView.setText(date);
    	dayView.setText(DOW[(dom_current+dow_start)%7]);
    	grantHoursView.setText(GHRS[dom_current-1]);
    	nonGrantHoursView.setText(NGHRS[dom_current-1]);
    	leaveHoursView.setText(LHRS[dom_current-1]);
    		
    	Integer dayTotal = Integer.valueOf(GHRS[dom_current-1])+Integer.valueOf(NGHRS[dom_current-1])+Integer.valueOf(LHRS[dom_current-1]);
    	dayTotalHoursView.setText(dayTotal.toString());
    	Integer tgh = 0;
    	for (int i = 0; i < GHRS.length; i++) {
    		tgh = tgh + Integer.valueOf(GHRS[i]);
    	}
    	monthTotalHoursView.setText(tgh.toString());
	}
}
