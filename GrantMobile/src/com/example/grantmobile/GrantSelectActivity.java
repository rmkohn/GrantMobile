package com.example.grantmobile;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.grantmobile.GrantService.ServiceCallback;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Button;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;

public class GrantSelectActivity extends GrantServiceBindingActivity {
	public static final String TAG_INTENT_GRANT_NAMES = "grantnames";

	public static final String TAG_INTENT_GRANT_IDS = "grantids";

	ArrayList<Grant> selectedGrants;
	
	// TEMPORARY until we decide which values to display and which to discard
	JSONObject[] grants;
	Grant[] grantInfo;
	
	public static class Grant implements Serializable {
		private static final long serialVersionUID = 0L;
		String name;
		int id;
		public String toString() { return name; }
		public Grant(String name, int id) { this.name = name; this.id = id; }
	}
	
	TableLayout grantTable;
	Button continueButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_grant_select);
		// Show the Up button in the action bar.
		setupActionBar();
		
		selectedGrants = new ArrayList<Grant>();
		dumpBundle(getIntent().getExtras());
		
		grantTable = (TableLayout)findViewById(R.id.grantTable);
		continueButton = (Button)findViewById(R.id.grant_select_continue);
		if (savedInstanceState != null) {
			// casting to a generic type
			@SuppressWarnings("unchecked")
			ArrayList<Grant> oldgrants =
				(ArrayList<Grant>)savedInstanceState.getSerializable("grants");
			if (oldgrants != null) {
				for (Grant grant: oldgrants) {
					addGrant(grant);
				}
			}
		}
		updateContinueButton();
		
		findViewById(R.id.grant_select_addgrant).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showAddGrantDialog();
			}
		});
		
		loadGrants();
		
		continueButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	loadCalendarEditActivity();
            }
        });
	}

	public void showAddGrantDialog() {
		SelectionDialog<Grant> dialog = new GrantAdderDialog();
		dialog.setItems(getUnchosenGrants());
		dialog.show(getSupportFragmentManager(), "");
	}
	
	public List<Grant> getUnchosenGrants() {
		ArrayList<Grant> unchosenGrants = new ArrayList<Grant>(Arrays.asList(grantInfo));
		unchosenGrants.removeAll(selectedGrants);
		return unchosenGrants;
	}
	
	public void addGrant(Grant grant) {
		final View grantRow = getLayoutInflater().inflate(R.layout.tablerow_grantselect, grantTable, false);
		((TextView)grantRow.findViewById(R.id.grant_select_grantname)).setText(grant.name);
		grantRow.findViewById(R.id.removeGrant).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				int delIndex = grantTable.indexOfChild(grantRow);
				grantTable.removeViewAt(delIndex);
				selectedGrants.remove(delIndex);
				updateContinueButton();
			}
		});
		selectedGrants.add(grant);
		grantTable.addView(grantRow);
		updateContinueButton();
	}
	
	public static void dumpBundle(Bundle extras) {
		Set<String> keys = extras.keySet();
		JSONObject bundle = new JSONObject();
		try {
			for (String key: keys) {
				Object value = extras.get(key);
				Object jsonval = value.getClass().isArray() ? getJSONArray(value) : value;
				bundle.put(key, jsonval);
			}
			Log.i("grantselect", bundle.toString(2));
		} catch (JSONException e) { e.printStackTrace(); }
	}
	
	public static JSONArray getJSONArray(Object array) {
		JSONArray j = new JSONArray();
		for (int i = 0; i < Array.getLength(array); i++)
			j.put(Array.get(array, i));
		return j;
	}

	protected void loadCalendarEditActivity() {
		Intent intent = new Intent(GrantSelectActivity.this, CalendarEditActivity.class);
		intent.putExtras(getIntent());

		int[] grantids = new int[selectedGrants.size()];
		String[] grantnames = new String[selectedGrants.size()];
		
		for (int i = 0; i < grantids.length; i++) {
			Grant grant = selectedGrants.get(i);
			grantids[i]   = grant.id;
			grantnames[i] = grant.name;
		}

		intent.putExtra(TAG_INTENT_GRANT_IDS, grantids);
		intent.putExtra(TAG_INTENT_GRANT_NAMES, grantnames);
		intent.putExtra("editing", true);
		startActivity(intent);
	}
	
	protected void onBound() {
		loadGrants();
	}

	private void loadGrants() {
		if (grants == null && isServiceBound()) {
			getService().getGrants(new ServiceCallback<JSONObject[]>() {
				public void run(JSONObject[] result) {
					grants = result;
					finishGrantInit();
				}
			});
		}
	}

	private void updateContinueButton() {
		int visibility = selectedGrants.size() > 0
				? View.VISIBLE
				: View.INVISIBLE;
		continueButton.setVisibility(visibility);
	}
	
	protected void finishGrantInit() {
//		grantNames = new String[grants.length];
		grantInfo = new Grant[grants.length];
		try {
			for (int i = 0; i < grantInfo.length; i++) {
				grantInfo[i] = new Grant(grants[i].getString("grantTitle"), grants[i].getInt("ID"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return;
		}
	}
	
	public static class GrantAdderDialog extends SelectionDialog<Grant> {
		public void onResult(Grant result) {
			if (result != null)
				((GrantSelectActivity)getActivity()).addGrant(result);
		}
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if (getActionBar() != null) {
				getActionBar().setDisplayHomeAsUpEnabled(true);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.grant_select, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("grants", selectedGrants);
	}

}
