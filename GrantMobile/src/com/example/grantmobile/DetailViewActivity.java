package com.example.grantmobile;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class DetailViewActivity extends Activity {
	// these two named parameter are for the Intent interface to this activity (both reference Strings)
	public static final String TAG_REQUEST_ID = "RequestId"; // required, no default!!!!
	public static final String TAG_DAY_OF_MONTH = "DayOfMonth"; // optional, default is first day of month

	private static final String TAG = "detailview";
	private static String requestURL = "http://mid-state.net/mobileclass2/android";
	
	JSONParser jParser = new JSONParser();
	ArrayList<String> grantHours; // grant hour array 
	ArrayList<String> nonGrantHours; // non-grant hour array
	ArrayList<String> leaveHours;  // leave hour array
	HashMap<String,String> map;
	
	private static final String TAG_SUCCESS = "success";  // "true" is good
	private static final String TAG_MESSAGE = "message";

	private static final String TAG_HOURS = "hours"; 
	private static final String TAG_GRANT = "grant";
	private static final String TAG_NON_GRANT = "non-grant";
	private static final String TAG_LEAVE = "leave";
	
	private static final String TAG_MONTH = "month";
	private static final String TAG_YEAR = "year";
	
	private static final String TAG_EMPLOYEE = "employee";
	private static final String TAG_FIRST_NAME = "firstname";
	private static final String TAG_LAST_NAME = "lastname";
	private static final String TAG_EMPLOYEE_ID = "id";
	
	private static final String TAG_GRANT_ID = "ID";
	private static final String TAG_STATE_CATALOG_NUM = "stateCatalogNum";
	private static final String TAG_GRANT_NUMBER = "grantNumber";
	private static final String TAG_GRANT_TITLE = "grantTitle";

	JSONObject json = null; // entire json object

	// day of week string array
	static final String[] DOW = { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday",
							"Friday", "Saturday" };
	static final String[] MOY = { "January", "February", "March", "April", "May", "June",
									"July", "August", "September", "October", "November", "December" };
		
	Integer dowStart; // month starts on Wednesday for test, use WED - 1 
	Integer domCurrent; // current day of month
	Integer moy;
	public static String requestId;
	
	TextView grantNameView;
	TextView grantIdView;
	TextView employeeNameView;
	TextView catalogView;
		
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
		context = this;
		setContentView(R.layout.activity_detail_view);
		
		grantHours = new ArrayList<String>(); // grant hour array 
		nonGrantHours = new ArrayList<String>(); // non-grant hour array
		leaveHours = new ArrayList<String>();  // leave hour array
		map = new HashMap<String,String>();
		
		// intent interface
		Intent intent = getIntent();
		String str = intent.getStringExtra(TAG_DAY_OF_MONTH);
		if (str==null) {
			domCurrent = 1;
		} else {
			domCurrent = Integer.valueOf(str);
		}
		str = intent.getStringExtra(TAG_REQUEST_ID);
		if (str==null) {
			requestId = "23"; // for test
		} else {
			requestId = str;
		}

		// setup view for displaying grant info
		grantNameView = (TextView)findViewById(R.id.grantNameView);
		grantIdView = (TextView)findViewById(R.id.grantIdView);
		employeeNameView = (TextView)findViewById(R.id.employeeNameView);
		catalogView = (TextView)findViewById(R.id.catalogView);

		// setup view for displaying specific info for a given date
		dateView = (TextView)findViewById(R.id.dateView);
		dayView = (TextView)findViewById(R.id.dayOfWeekView);
		grantHoursView = (TextView)findViewById(R.id.grantHoursView);
		nonGrantHoursView = (TextView)findViewById(R.id.nonGrantHoursView);
		leaveHoursView = (TextView)findViewById(R.id.leaveHoursView);

		dayTotalHoursView = (TextView)findViewById(R.id.dayTotalHoursView);
		monthTotalHoursView = (TextView)findViewById(R.id.monthTotalHoursView);
		
        final Button backwardButton = (Button) findViewById(R.id.backBtn);
        backwardButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	if (--domCurrent == 0) domCurrent = grantHours.size();  // underflow wrap
            	updateView();
            }
        });
        final Button forwardButton = (Button) findViewById(R.id.nextBtn);
        forwardButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if (++domCurrent >  grantHours.size()) domCurrent = 1; // overflow wrap
            	updateView();
            }
        });
        
        final Button returnButton = (Button) findViewById(R.id.returnBtn);
        returnButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(context, "Return to Calendar View", Toast.LENGTH_LONG).show();
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        
		// start access of grant data, updateView() below will access the data when ready
//		new GetGrantData().execute();
	    intent = new Intent(context,GrantService.class);
	    // Create a new Messenger for the communication back
	    Messenger messenger = new Messenger(handler);
	    intent.putExtra("MESSENGER", messenger); // pass the callback
	    intent.putExtra(DetailViewActivity.TAG_REQUEST_ID, requestId); // pass the parameter
	    startService(intent);
	}
        
    private void updateView() {
		String domString = String.valueOf(domCurrent);
    	
		// figure out what first day-of-week is for this month (dowStart)
    	Calendar cal = Calendar.getInstance();
    	cal.set(Calendar.DATE,1);
    	cal.set(Calendar.MONTH, Integer.valueOf(map.get(TAG_MONTH)));
    	cal.set(Calendar.YEAR, Integer.valueOf(map.get(TAG_YEAR)));
    	cal.set(Calendar.DAY_OF_MONTH, 1); 	
        dowStart = (cal.get(Calendar.DAY_OF_WEEK)-Calendar.SUNDAY)%7;
        
		grantNameView.setText(map.get(TAG_GRANT_TITLE));
		grantIdView.setText(map.get(TAG_GRANT_ID));
		employeeNameView.setText(map.get(TAG_FIRST_NAME)+ " " + map.get(TAG_LAST_NAME));
		catalogView.setText(map.get(TAG_STATE_CATALOG_NUM));
    	
		moy = Integer.parseInt(map.get(TAG_MONTH));
    	dateView.setText(MOY[moy]+" "+domString+", "+map.get(TAG_YEAR));
    	dayView.setText(DOW[(domCurrent+dowStart-1)%7]);
    	grantHoursView.setText(grantHours.get(domCurrent-1));
    	nonGrantHoursView.setText(nonGrantHours.get(domCurrent-1));
    	leaveHoursView.setText(leaveHours.get(domCurrent-1));
    		
    	Integer dayTotal = Integer.valueOf(grantHours.get(domCurrent-1))+Integer.valueOf(nonGrantHours.get(domCurrent-1))+Integer.valueOf(leaveHours.get(domCurrent-1));
    	dayTotalHoursView.setText(dayTotal.toString());
    	Integer tgh = 0;
    	for (int i = 0; i < grantHours.size(); i++)
    		tgh = tgh + Integer.valueOf(grantHours.get(i));
    	monthTotalHoursView.setText(tgh.toString());
	}
	
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(Message message) {
			if (message.arg1 == RESULT_OK) {
//				Toast.makeText(DetailViewActivity.this,
//					message.getData().toString(), Toast.LENGTH_LONG)
//		            .show();
				grantHours = message.getData().getStringArrayList(TAG_GRANT);
				nonGrantHours = message.getData().getStringArrayList(TAG_NON_GRANT);
				leaveHours = message.getData().getStringArrayList(TAG_LEAVE);
				map.put(TAG_GRANT_TITLE,message.getData().getString(TAG_GRANT_TITLE));
				map.put(TAG_GRANT_ID,message.getData().getString(TAG_GRANT_ID));
				map.put(TAG_STATE_CATALOG_NUM,message.getData().getString(TAG_STATE_CATALOG_NUM));		    	
				map.put(TAG_FIRST_NAME,message.getData().getString(TAG_FIRST_NAME));
				map.put(TAG_LAST_NAME,message.getData().getString(TAG_LAST_NAME));
				map.put(TAG_MONTH,message.getData().getString(TAG_MONTH));
				map.put(TAG_YEAR,message.getData().getString(TAG_YEAR));
				moy = Integer.parseInt(message.getData().getString(TAG_MONTH));
				updateView();
			} else {
				Toast.makeText(DetailViewActivity.this, "Download failed.",
		            Toast.LENGTH_LONG).show();
			}
	    };
	};
}
