package com.example.grantmobile;

import java.util.Calendar;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MonthSelectActivity extends Activity {

	private ListView mYearView;
	private ListView mMonthView;
	private TextView mSelectedDateView;
	
	public static final String TAG_INTENT_MONTH = "month";
	public static final String TAG_INTENT_YEAR = "year";
	
	private int selectedYear = 2099;
	private int selectedMonth = 0;

	public static final String[] monthNames = {
		"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"
	};
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_month_select);

		Log.i("monthselect", getIntent().getExtras().toString());

		mYearView               = (ListView)findViewById(R.id.listYear);
		mMonthView              = (ListView)findViewById(R.id.listMonth);
		mSelectedDateView       = (TextView)findViewById(R.id.selected_month);

		final Integer[] years = new Integer[10];
		int start = Calendar.getInstance().get(Calendar.YEAR) - 7;
		for (int i = 0; i < years.length; i++) {
			years[i] = i+start;
		}
		mYearView.setAdapter(new ArrayAdapter<Integer>(this, android.R.layout.simple_list_item_single_choice, years));
		mMonthView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, monthNames));
		mYearView.setItemsCanFocus(false);
		mYearView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mMonthView.setItemsCanFocus(false);
		mMonthView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		

		OnItemClickListener listener = new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.i("monthselectactivity.listener", "selected item " + position);
				if (parent == mYearView)
					selectedYear = years[position];
				else if (parent == mMonthView)
					selectedMonth = position;
				updateDateText();
			}
		};

		mMonthView.setOnItemClickListener(listener);
		mYearView.setOnItemClickListener(listener);

		findViewById(R.id.year_chosen_button).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(MonthSelectActivity.this, GrantSelectActivity.class);
				i.putExtras(getIntent());
				i.putExtra(TAG_INTENT_YEAR, selectedYear);
				i.putExtra(TAG_INTENT_MONTH, selectedMonth);
				startActivity(i);
			}
		});

	}
	
	private void updateDateText() {
		if (selectedMonth >= 0 && selectedMonth < monthNames.length)
			mSelectedDateView.setText(monthNames[selectedMonth] + " " + selectedYear);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.grant_select, menu);
		return true;
	}


}
