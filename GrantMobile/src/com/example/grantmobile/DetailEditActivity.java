
package com.example.grantmobile;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.grantmobile.GrantService.GrantData;
import com.example.grantmobile.GrantService.ServiceCallback;

import android.os.Bundle;
import android.app.Activity;
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

public class DetailEditActivity extends GrantServiceBindingActivity {
	// these two named parameter are for the Intent interface to this activity (both reference Strings)
	public static final String TAG_REQUEST_ID = "RequestId"; // required, no default!!!!
	public static final String TAG_DAY_OF_MONTH = "DayOfMonth"; // optional, default is first day of month

	// do not try to save to GrantService unless we've successfully loaded hours from it
	// (this could happen when the server has not yet replied, and the user rotates their device)
	private boolean hoursLoaded = false;
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
	
	// day of week string array
	static final String[] DOW = { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday",
							"Friday", "Saturday" };
	static final String[] MOY = { "January", "February", "March", "April", "May", "June",
									"July", "August", "September", "October", "November", "December" };
		
	int dowStart; // month starts on Wednesday for test, use WED - 1 
	int domCurrent; // current day of month
	int moy;
	
	TextView grantNameView, grantIdView, employeeNameView, catalogView;		
	TextView dateView, dayView;
	TextView dayTotalHoursView, monthTotalHoursView;
	EditText grantHoursView, nonGrantHoursView, leaveHoursView;
	
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
		grantId = intent.getIntExtra("grantid", -1);
		EmployeeDialog.Employee employee = (EmployeeDialog.Employee)intent.getSerializableExtra(LoginActivity.TAG_INTENT_USERID);
		employeeid = employee.id;
		firstName = employee.firstname;
		lastName = employee.lastname;
		
		// setup view for displaying grant info
		grantNameView = (TextView)findViewById(R.id.grantNameTv);
		grantIdView = (TextView)findViewById(R.id.grantIdTv);
		employeeNameView = (TextView)findViewById(R.id.employeeNameTv);
		catalogView = (TextView)findViewById(R.id.catalogTv);

		// setup view for displaying specific info for a given date
		dateView = (TextView)findViewById(R.id.dateTv);
		dayView = (TextView)findViewById(R.id.dayOfWeekTv);
		grantHoursView = (EditText)findViewById(R.id.grantHoursTv);
		nonGrantHoursView = (EditText)findViewById(R.id.nonGrantHoursTv);
		leaveHoursView = (EditText)findViewById(R.id.leaveHoursTv);

		dayTotalHoursView = (TextView)findViewById(R.id.dayTotalHoursTv);
		monthTotalHoursView = (TextView)findViewById(R.id.monthTotalHoursTv);
		
		loadGrantInfo();
		
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
        
		if (getService() != null)
			loadHours();
	}
	
		
	private void loadGrantInfo() {
		if (isServiceBound()) {
			Log.i("detailedit", "loading grant info");
			getService().getGrantByParameter("ID", Integer.valueOf(grantId), new ServiceCallback<JSONObject>() {
				public void run(JSONObject result) {
					updateGrantViews(result);
				}
			});
		}
	}
	
	private void updateGrantViews(JSONObject result) {
		Log.i("detailedit", "updating grant info views");
		try {
			grantNameView.setText(result.getString(DetailViewActivity.TAG_GRANT_TITLE));
			grantIdView.setText(result.getString(DetailViewActivity.TAG_GRANT_NUMBER));
			catalogView.setText(result.getString(DetailViewActivity.TAG_STATE_CATALOG_NUM));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void loadHours() {
		String[] hours = { String.valueOf(grantId), "non-grant", "leave" };
	    GrantData grantdata = new GrantData(year, month, employeeid);
		getService().getHours(grantdata, hours, new DetailHandler());
	}
	
	@Override
	protected void onBound() {
		loadGrantInfo();
		loadHours();
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
        
		employeeNameView.setText(firstName + " " + lastName);
    	
		moy = month;
    	dateView.setText(MOY[moy]+" "+domString+", "+year);
    	dayView.setText(DOW[(domCurrent+dowStart-1)%7]);
    	grantHoursView.setText(getFormattedHours(grantHours[domCurrent-1]));
    	nonGrantHoursView.setText(getFormattedHours(nonGrantHours[domCurrent-1]));
    	leaveHoursView.setText(getFormattedHours(leaveHours[domCurrent-1]));
    	Log.i("detailedit", String.format("loaded hours for day %d: %.1f, %.1f, %.1f", domCurrent,
    			grantHours[domCurrent-1],
    			nonGrantHours[domCurrent-1],
    			leaveHours[domCurrent-1]));
    	
    	double dayTotal = grantHours[domCurrent-1] + nonGrantHours[domCurrent-1] + leaveHours[domCurrent-1];
    	dayTotalHoursView.setText(getFormattedHours(dayTotal));
    	double tgh = 0;
    	for (double gh: grantHours)
    		tgh = tgh + gh;
    	monthTotalHoursView.setText(getFormattedHours(tgh));
	}
    
    private void saveHours() {
    	double grant    = getTimeFromView(grantHoursView);
    	double nongrant = getTimeFromView(nonGrantHoursView);
    	double leave    = getTimeFromView(leaveHoursView);
    	grantHours   [domCurrent-1] = grant;
    	leaveHours   [domCurrent-1] = leave;
    	nonGrantHours[domCurrent-1] = nongrant;
    	Log.i("detailedit", String.format("saving hours for day %d: %.1f, %.1f, %.1f", domCurrent, grant, nongrant, leave));
    }
    
    private double getTimeFromView(EditText view) {
    	try {
    		return Double.valueOf(view.getText().toString());
    	} catch (NumberFormatException e) {
    		return 0;
    	}
    }
    
    private String getFormattedHours(double hours) {
    	return String.format(Locale.US, "%.1f", hours).replace(".0", "");
    }
    
    public class DetailHandler implements GrantService.ServiceCallback<Map<String, double[]>> {
		public void run(Map<String, double[]> result) {
			if (result != null) {
				grantHours = result.get(String.valueOf(grantId));
				nonGrantHours = result.get(TAG_NON_GRANT);
				leaveHours = result.get(TAG_LEAVE);
				updateView();
				hoursLoaded = true;
			} else {
				Toast.makeText(DetailEditActivity.this, "Download failed.",
						Toast.LENGTH_LONG).show();
			}
			
		}
    }
    
    public class SaveHandler implements GrantService.ServiceCallback<Integer> {
		@Override
		public void run(Integer result) {
			String message = (result == Activity.RESULT_OK)
				? "hours saved"
				: "something went wrong";
			Toast.makeText(DetailEditActivity.this, message, Toast.LENGTH_LONG).show();
		}
    }

	@Override
	protected void onUnbound() {
		saveHours();
	    GrantData data = new GrantData(year, month, employeeid);
	    Map<String, double[]> hourBundle = new HashMap<String, double[]>();
	    hourBundle.put(TAG_NON_GRANT, nonGrantHours);
	    hourBundle.put(TAG_LEAVE, leaveHours);
	    hourBundle.put(String.valueOf(grantId), grantHours);
	    if (hoursLoaded) {
		    getService().saveHours(data, hourBundle);
		    getService().uploadHours(data, hourBundle, new SaveHandler());
	    }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.edit, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		GrantService.GrantData data = new GrantService.GrantData(year, month, employeeid);
		String[] grantstrings = GrantService.getGrantStrings(new int[] { grantId });
		switch (item.getItemId()) {
		case R.id.mnuLoad:
			// IntentService uses a single background thread, so this should work
			getService().deleteHours(data, grantstrings);
			updateView();
			break;
		case R.id.mnuDialog:
			getService().getSupervisors(new ServiceCallback<JSONObject[]>() {
				public void run(JSONObject[] result) {
					EmployeeDialog dialog = new EmployeeDialog();
					List<EmployeeDialog.Employee> supervisors = new ArrayList<EmployeeDialog.Employee>(result.length);
					for (JSONObject obj: result) {
						supervisors.add(EmployeeDialog.Employee.fromJson(obj));
					}
					dialog.setItems(supervisors);
					dialog.setData(new GrantData(year, month, employeeid));
					dialog.setGrantid(grantId);
					dialog.show(getSupportFragmentManager(), "");
				}
			});
			break;
		case R.id.mnuUpload:
			getService().uploadHours(data, grantstrings, new ServiceCallback<Integer>() {
				public void run(Integer result) {
					String message = (result == Activity.RESULT_OK)
						? "uploaded successfully"
						: "error uploading";
					Toast.makeText(DetailEditActivity.this, message, Toast.LENGTH_LONG).show();
				}
			});
			break;
		default: return false;
		}
		return true;
	}

	

}
