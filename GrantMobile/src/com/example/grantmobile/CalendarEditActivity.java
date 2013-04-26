package com.example.grantmobile;

import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.grantmobile.CalendarSquare.DaySquare;
import com.example.grantmobile.CalendarSquare.ICalendarSquare;
import com.example.grantmobile.GrantService.ServiceCallback;

public class CalendarEditActivity extends BaseCalendarActivity {
	Spinner grantSpinner;
	int[] grantids;
	String[] grantnames;
	int userid;
	SparseArray<double[]> granthours;
	double[] nongranthours;
	double[] leavehours;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		grantSpinner = (Spinner) findViewById(R.id.calendarGrantSwitcher);
		headerFlipper.setDisplayedChild(headerFlipper.indexOfChild(grantSpinner));
		
		GrantSelectActivity.dumpBundle(getIntent().getExtras());
		
		Intent i = getIntent();
		grantids = i.getIntArrayExtra(GrantSelectActivity.TAG_INTENT_GRANT_IDS);
		grantnames = i.getStringArrayExtra(GrantSelectActivity.TAG_INTENT_GRANT_NAMES);
		userid = i.getIntExtra(LoginActivity.TAG_INTENT_USERID, -1);
		
		createCalendarArray(
			i.getIntExtra(MonthSelectActivity.TAG_INTENT_YEAR,  -1),
			i.getIntExtra(MonthSelectActivity.TAG_INTENT_MONTH, -1)
		);
		
		grantSpinner.setAdapter(new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_dropdown_item, grantnames));
		populateGrantSpinner();
		
		grantSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				updateCalendar();
			}
			public void onNothingSelected(AdapterView<?> parent) { }
		});
	}
	
	private void populateGrantSpinner() {
		grantSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, grantnames));
	}
	
	private void loadCalendar() {
		Log.i("calendareditactivity", "loadcalendar");
		final GrantService.GrantData data = new GrantService.GrantData(getYear(), getMonth()-1, userid);
//		GrantService.getHours(this, new CalendarEditHandler().setParent(this), data, getGrantStrings());
		getService().getHours(data, getGrantStrings(), new GrantService.ServiceCallback<Map<String,double[]>>() {
			public void run(Map<String, double[]> result) {
				assignNewData(result);
			}
		});
	}

	private String[] getGrantStrings() {
		String[] grantStrings = new String[grantids.length + 2];
		for (int i = 0; i < grantids.length; i++) {
			grantStrings[i] = String.valueOf(grantids[i]);
		}
		grantStrings[grantids.length]   = "non-grant";
		grantStrings[grantids.length+1] = "leave";
		return grantStrings;
	}
	
	protected void updateCalendar() {
		if (granthours != null && grantSpinner != null) {
			Log.i("updatecalendar", String.format("selected grant %d: %s\nnongrant: %s\nleave: %s",
					grantSpinner.getSelectedItemPosition(), getSelectedGrantHours(),
					nongranthours, leavehours));
			loadCalendar(getSelectedGrantHours(), nongranthours, leavehours);
		}
	}
	
	protected double[] getSelectedGrantHours() {
		int grantid = grantids[grantSpinner.getSelectedItemPosition()];
		return granthours.get(grantid);
	}
	
	@Override
	protected void openDetailView(ICalendarSquare detailSquare) {
		if (detailSquare instanceof DaySquare) {
			Intent i = new Intent(this, DetailEditActivity.class);
			i.putExtra(DetailEditActivity.TAG_DAY_OF_MONTH, ((DaySquare) detailSquare).dailyNumber);
			i.putExtra(GrantService.TAG_REQUEST_DETAILS, new GrantService.GrantData(getYear(), getMonth()-1, userid));
			i.putExtra("grantid", grantids[grantSpinner.getSelectedItemPosition()]);
			startActivity(i);
		}
	}
	
	public void assignNewData(Map<String, double[]> data) {
		SparseArray<double[]> granthours = new SparseArray<double[]>();
		for (int i: grantids) {
			granthours.append(i, data.get(String.valueOf(i)));
		}
		this.granthours    = granthours;
		this.nongranthours = data.get("non-grant");
		this.leavehours    = data.get("leave");
		this.updateCalendar();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (getService() != null)
			loadCalendar();
	}
	
	@Override
	protected void onBound() {
		loadCalendar();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.edit, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		GrantService.GrantData data = new GrantService.GrantData(getYear(), getMonth()-1, userid);
		String[] grantstrings = getGrantStrings();
		switch (item.getItemId()) {
		case R.id.mnuLoad:
			// IntentService uses a single background thread, so this should work
			getService().deleteHours(data, grantstrings);
			loadCalendar();
			break;
		case R.id.mnuDialog:
			Toast.makeText(this, "Sorry, not implemented yet", Toast.LENGTH_LONG).show();
			break;
		case R.id.mnuUpload:
			getService().uploadHours(data, grantstrings, new ServiceCallback<Integer>() { 
				public void run(Integer result) {
					String message = (result == Activity.RESULT_OK)
						? "uploaded successfully"
						: "error uploading";
					Toast.makeText(CalendarEditActivity.this, message, Toast.LENGTH_LONG).show();
				}
			});
			break;
		default: return false;
		}
		return true;
	}


}
