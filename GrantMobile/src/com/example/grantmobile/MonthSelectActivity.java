package com.example.grantmobile;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

// this activity gets reopened for a split-second when the next-opened view rotates
// that split-second is enough to shut down GrantService if the foreground activity (this)
// doesn't also use it.
// (this may all be an emulator artifact; its timescale and memory management are a tiny bit wonky)
public class MonthSelectActivity extends GrantServiceBindingActivity {

	private ListView mYearView;
	private ListView mMonthView;
	private TextView mSelectedDateView;
	private View     continueButton;
	
	public static final String TAG_INTENT_MONTH = "month";
	public static final String TAG_INTENT_YEAR = "year";
	
	private int selectedYear = 2099;
	private int selectedMonth = 0;
	
	Calendar tmpCalendar = new GregorianCalendar();
	Calendar currentTime = new GregorianCalendar();

	public static final String[] monthNames = GrantApp.monthNames;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_month_select);

		Log.i("monthselect", getIntent().getExtras().toString());

		mYearView               = (ListView)findViewById(R.id.listYear);
		mMonthView              = (ListView)findViewById(R.id.listMonth);
		mSelectedDateView       = (TextView)findViewById(R.id.selected_month);

		final Integer[] years = new Integer[4];
		int start = Calendar.getInstance().get(Calendar.YEAR);
		for (int i = 0; i < years.length; i++) {
			years[i] = start-i;
		}
		mYearView.setAdapter(new ArrayAdapter<Integer>(this, android.R.layout.simple_list_item_single_choice, years));
		mMonthView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, monthNames));
		mMonthView.setAdapter(new BaseAdapter() {
			@Override public int getCount() { return 12; }
			@Override public Object getItem(int position) { return monthNames[position]; }
			@Override public long getItemId(int position) { return position; }
			@Override public View getView(int position, View convertView, ViewGroup parent) {
				if (convertView == null)
					convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_single_choice, parent, false);
				TextView text = (TextView)convertView.findViewById(android.R.id.text1);
				text.setText(monthNames[position]);
				text.setEnabled(isEnabled(position));
				return convertView;
			}
			@Override public boolean isEnabled(int position) {
				return isMonthEnabled(selectedYear, position + Calendar.JANUARY);
			}
			@Override
			public boolean areAllItemsEnabled() { return false; }
		});
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
				((BaseAdapter)mMonthView.getAdapter()).notifyDataSetChanged();
				updateDateText();
			}
		};

		mMonthView.setOnItemClickListener(listener);
		mYearView.setOnItemClickListener(listener);

		continueButton = findViewById(R.id.year_chosen_button);
		continueButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(MonthSelectActivity.this, GrantSelectActivity.class);
				i.putExtras(getIntent());
				i.putExtra(TAG_INTENT_YEAR, selectedYear);
				i.putExtra(TAG_INTENT_MONTH, selectedMonth+1);
				startActivity(i);
			}
		});
		
		updateDateText();
	}
	
	private void updateDateText() {
		if (selectedMonth >= 0 && selectedMonth < monthNames.length)
			mSelectedDateView.setText(monthNames[selectedMonth] + " " + selectedYear);
		continueButton.setEnabled(isMonthEnabled(selectedYear, selectedMonth));
	}
	
	public boolean isMonthEnabled(int year, int month) {
		tmpCalendar.set(year, month, 1);
		return tmpCalendar.before(currentTime);
	}

}
