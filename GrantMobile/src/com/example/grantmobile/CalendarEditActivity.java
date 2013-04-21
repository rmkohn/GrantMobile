package com.example.grantmobile;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.grantmobile.CalendarSquare.DaySquare;
import com.example.grantmobile.CalendarSquare.ICalendarSquare;

public class CalendarEditActivity extends BaseCalendarActivity {
	Spinner grantSpinner;
	int[] grantids;
	String[] grantnames;
	int userid;
	SparseArray<double[]> granthours;
	double[] nongranthours;
	double[] leavehours;
	
	public static final String TAG_REQUEST_DETAILS = "viewRequestDetails";
	public static final String TAG_VIEWREQUEST_TYPE = "viewRequest";
	public static final String TAG_REQUEST_GRANTS = "viewRequestGrants";

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
		
		loadCalendar();
	}
	
	private void populateGrantSpinner() {
		grantSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, grantnames));
	}
	
	private void loadCalendar() {
		Log.i(this.getClass().getSimpleName(), "loadcalendar");
		ArrayList<String> loadGrants = new ArrayList<String>();
		for (int id: grantids) {
			loadGrants.add(String.valueOf(id));
		}
		Intent i = new Intent(this, GrantService.class);
		i.putExtra(TAG_REQUEST_DETAILS, new GrantService.GrantData(getYear(), getMonth()-1, userid));
		i.putExtra(TAG_REQUEST_GRANTS, grantids);
		i.putExtra(GrantService.TAG_REQUEST_TYPE, TAG_VIEWREQUEST_TYPE);
		i.putExtra("MESSENGER", new Messenger(new CalendarEditHandler(this)));
		startService(i);
	}
	
	protected void updateCalendar() {
		if (granthours != null && grantSpinner != null)
			loadCalendar(getSelectedGrantHours(), nongranthours, leavehours);
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
			i.putExtra(TAG_REQUEST_DETAILS, new GrantService.GrantData(getYear(), getMonth()-1, userid));
			i.putExtra("grantid", grantids[grantSpinner.getSelectedItemPosition()]);
			startActivity(i);
		}
	}
	
	public static class CalendarEditHandler extends Handler {
		WeakReference<CalendarEditActivity> parent;
		public CalendarEditHandler(CalendarEditActivity parent) {
			this.parent = new WeakReference<CalendarEditActivity>(parent);
		}
		
		@Override
		public void handleMessage(Message msg) {
			CalendarEditActivity a = parent.get();
			if (a == null)
				return;
			if (msg.arg1 == Activity.RESULT_OK) {
				Bundle data = msg.getData();
				SparseArray<double[]> granthours = new SparseArray<double[]>();
				for (int i: a.grantids) {
					granthours.append(i, data.getDoubleArray(String.valueOf(i)));
				}
				a.granthours    = granthours;
				a.nongranthours = data.getDoubleArray("non-grant");
				a.leavehours    = data.getDoubleArray("leave");
				
				a.updateCalendar();
			}
		}
			
		
	}


}
