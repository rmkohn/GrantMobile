package com.example.grantmobile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.grantmobile.CalendarSquare.DaySquare;
import com.example.grantmobile.CalendarSquare.ICalendarSquare;
import com.example.grantmobile.DBAdapter.Hours;
import com.example.grantmobile.EmployeeDialog.Employee;
import com.example.grantmobile.GrantService.GrantData;

public class CalendarEditActivity extends BaseCalendarActivity {
	Spinner grantSpinner;
	int[] grantids;
	String[] grantnames;
//	int user.id;
	Employee user;
	SparseArray<Hours> granthours;
	double[] nongranthours;
	double[] leavehours;
	
	int savedSpinnerPos = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			savedSpinnerPos = savedInstanceState.getInt("spinnerPos", -1);
		}
		grantSpinner = (Spinner) findViewById(R.id.calendarGrantSwitcher);
		headerFlipper.setDisplayedChild(headerFlipper.indexOfChild(grantSpinner));
		
		GrantApp.dumpBundle(getIntent().getExtras());
		
		Intent i = getIntent();
		grantids = i.getIntArrayExtra(GrantSelectActivity.TAG_INTENT_GRANT_IDS);
		grantnames = i.getStringArrayExtra(GrantSelectActivity.TAG_INTENT_GRANT_NAMES);
		user = (Employee)i.getSerializableExtra(LoginActivity.TAG_INTENT_USERID);
		
		createCalendarArray(
			i.getIntExtra(MonthSelectActivity.TAG_INTENT_YEAR,  -1),
			i.getIntExtra(MonthSelectActivity.TAG_INTENT_MONTH, -1)
		);
		
		grantSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				updateCalendar();
			}
			public void onNothingSelected(AdapterView<?> parent) { }
		});
		
		((Button)findViewById(R.id.btnApproveDisapprove)).setText("Send Email");
	}
	
	private void populateGrantSpinner() {
//		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(
//				this, android.R.layout.simple_spinner_item, grantnames);
//		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		GrantAdapter spinnerAdapter = new GrantAdapter();
		grantSpinner.setAdapter(spinnerAdapter);
		if (savedSpinnerPos != -1)
			grantSpinner.setSelection(savedSpinnerPos, false);
	}
	
	private void loadCalendar() {
		Log.i("calendareditactivity", "loadcalendar");
		final GrantService.GrantData data = new GrantService.GrantData(getYear(), getMonth()-1, user.id);
//		GrantService.getHours(this, new CalendarEditHandler().setParent(this), data, getGrantStrings());
		getService().getHours(data, getGrantStrings(), new JSONParser.SimpleResultHandler<Map<String, Hours>>(this) {
			public void onSuccess(Map<String, Hours> result) {
				assignNewData(result);
			}
		});
	}

	private String[] getGrantStrings() {
		return GrantService.getGrantStrings(grantids);
	}
	
	protected void updateCalendar() {
		if (granthours != null && grantSpinner != null && grantids != null) {
			Log.i("updatecalendar", String.format("selected grant %d: %s\nnongrant: %s\nleave: %s",
					grantSpinner.getSelectedItemPosition(), getSelectedGrantHours(),
					nongranthours, leavehours));
			loadCalendar(getSelectedGrantHours(), nongranthours, leavehours);
		}
	}
	
	protected double[] getSelectedGrantHours() {
		return granthours.get(getSelectedGrantId()).hours;
	}
	
	protected int getSelectedGrantId() {
		return grantids[grantSpinner.getSelectedItemPosition()];
	}
	
	@Override
	protected void openDetailView(ICalendarSquare detailSquare) {
		if (detailSquare instanceof DaySquare) {
			Intent i = new Intent(this, DetailEditActivity.class);
			i.putExtra(DetailEditActivity.TAG_DAY_OF_MONTH, ((DaySquare) detailSquare).dailyNumber);
			i.putExtra(GrantService.TAG_REQUEST_DETAILS, new GrantService.GrantData(getYear(), getMonth()-1, user.id));
			i.putExtra(LoginActivity.TAG_INTENT_USERID, user);
			i.putExtra("grantid", grantids[grantSpinner.getSelectedItemPosition()]);
			startActivity(i);
		}
	}
	
	@Override
	protected void onEmailButtonClicked() {
		openEmailDialog();
	}
	
	private void openEmailDialog() {
		getService().getSupervisors(new JSONParser.SimpleResultHandler<JSONObject[]>(this) {
			public void onSuccess(JSONObject[] result) {
				EmployeeDialog dialog = new EmployeeDialog();
				List<EmployeeDialog.Employee> supervisors = new ArrayList<EmployeeDialog.Employee>(result.length);
				for (JSONObject obj: result) {
					supervisors.add(EmployeeDialog.Employee.fromJson(obj));
				}
				dialog.setItems(supervisors);
				dialog.setData(new GrantData(getYear(), getMonth()-1, user.id));
				dialog.setGrantid(getSelectedGrantId());
				dialog.show(getSupportFragmentManager(), "");
			}
		});
	}
	
	public void assignNewData(Map<String, Hours> data) {
		SparseArray<Hours> granthours = new SparseArray<Hours>();
		for (int i: grantids) {
			granthours.append(i, data.get(String.valueOf(i)));
		}
		this.granthours    = granthours;
		this.nongranthours = data.get("non-grant").hours;
		this.leavehours    = data.get("leave").hours;
		this.populateGrantSpinner();
		this.updateCalendar();
	}
	
	private final Runnable updateStatus = new Runnable() {
		public void run() {
			savedSpinnerPos = grantSpinner.getSelectedItemPosition();
			((BaseAdapter)grantSpinner.getAdapter()).notifyDataSetChanged();
		}
	};
	
	@Override
	protected void onResume() {
		super.onResume();
		if (getService() != null)
			loadCalendar();
	}
	
	@Override
	protected void onBound() {
		loadCalendar();
		getService().addUpdateCallback(updateStatus);
	}
	
	@Override
	protected void onUnbound() {
		getService().removeUpdateCallback(updateStatus);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		int pos = grantSpinner.getSelectedItemPosition();
		if (pos == Spinner.INVALID_POSITION) pos = -1;
		outState.putInt("spinnerPos", pos);
	}
	
	private class GrantAdapter extends BaseAdapter {
		Hours defaultStatus = new Hours(Hours.GrantStatus.New, null);
		public int getCount() { return grantnames.length; }
		public Object getItem(int position) {
			return grantnames[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return getView(position, convertView, parent, android.R.layout.simple_spinner_item);
		}
		
		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			return getView(position, convertView, parent, android.R.layout.simple_spinner_dropdown_item);
		}
		
		private View getView(int position, View convertView, ViewGroup parent, int id) {
			if (convertView == null) {
				convertView = (TextView)getLayoutInflater().inflate(id, parent, false);
			}
			TextView text = (TextView) convertView.findViewById(android.R.id.text1);
			text.setText(grantnames[position]);
			setDrawable(text, position);
			return convertView;
		}
		
		private void setDrawable(TextView t, int pos) {
			try {
				if (granthours == null || grantids == null)
					return;
				Drawable d = getResources().getDrawable(granthours.get(grantids[pos], defaultStatus).status.getDrawable());
				float ratio = ((float)d.getIntrinsicWidth()) / ((float)d.getIntrinsicHeight());
				d.setBounds(0, 0, (int)(ratio*50), 50);
				t.setCompoundDrawables(null, null, d, null);
			} catch (Exception e) { } // our assumptions about a system-controlled layout are wrong, so no pretty pictures.
			                          // Not really a huge deal.
		}
	}
}
