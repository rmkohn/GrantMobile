package com.example.grantmobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
				
			}
			public void onNothingSelected(AdapterView<?> parent) { }
		});
	}
	
	private void populateGrantSpinner() {
		
	}

	@Override
	protected void openDetailView(ICalendarSquare detailSquare) {
		
	}


}
