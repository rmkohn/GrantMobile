package com.example.grantmobile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.grantmobile.CalendarSquare.ICalendarSquare;

public class CalendarEditActivity extends BaseCalendarActivity {
	Spinner grantSpinner;
	int[] grantids;
	String[] grantnames;
	int userid;
	Map<Integer, JSONArray> time;
	int KEY_NONGRANTHOURS = -1;
	int KEY_LEAVEHOURS    = -2;

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
		new JSONParser.RequestBuilder("http://mid-state.net/mobileclass2/android")
		.addParam("employee", String.valueOf(userid))
		.addParam("year", String.valueOf(getYear()))
		.addParam("month", String.valueOf(getMonth()))
		.addParam("grant", Arrays.toString(grantids).replaceAll("\\[|\\]| ", ""))
		.makeRequest(new JSONParser.SimpleResultHandler() {

			@Override
			public void onSuccess(Object oResult) throws JSONException,
					IOException {
				JSONObject result = (JSONObject) oResult;
				time = new HashMap<Integer, JSONArray>();
				Iterator<String> keys = result.keys();
				while (keys.hasNext()) {
					String key = keys.next();
					JSONArray value = result.getJSONArray(key);
					if (key.equals("nongrant"))
						time.put(KEY_NONGRANTHOURS, value);
					else if (key.equals("leave"))
						time.put(KEY_LEAVEHOURS, value);
					else
						time.put(Integer.parseInt(key), value);
				}
				updateCalendar();
			}
			
		});
	}
	
	protected void updateCalendar() {
		loadCalendar(getSelectedGrantHours(), time.get(KEY_NONGRANTHOURS), time.get(KEY_LEAVEHOURS));
	}
	
	protected JSONArray getSelectedGrantHours() {
		return time.get(grantSpinner.getSelectedItemPosition());
	}
	
	@Override
	protected void openDetailView(ICalendarSquare detailSquare) {
		
	}


}
