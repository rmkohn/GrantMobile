package com.example.grantmobile;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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

// this activity gets reopened for a split-second when CalendarEditView rotates
// that split-second is enough to shut down GrantService if the foreground activity (this)
// doesn't also use it.
// (this may all be an emulator artifact; its timescale and memory management are a tiny bit wonky)
public class GrantSelectActivity extends GrantServiceBindingActivity {
	public static final String TAG_INTENT_GRANT_NAMES = "grantnames";

	public static final String TAG_INTENT_GRANT_IDS = "grantids";

	ArrayList<Grant> selectedGrants;
	
	// TEMPORARY until we decide which values to display and which to discard
	JSONObject[] grants;
	Grant[] grantInfo;
	
	public static class Grant {
		String name;
		int id;
		public String toString() { return name; }
		public Grant(String name, int id) { this.name = name; this.id = id; }
	}
	
	TableLayout grantTable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_grant_select);
		// Show the Up button in the action bar.
		setupActionBar();
		
		dumpBundle(getIntent().getExtras());
		
		Button continueButton = (Button)findViewById(R.id.grant_select_continue);
		
		grantTable = (TableLayout)findViewById(R.id.grantTable);
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
		GrantSelectDialog dialog = new GrantSelectDialog();
		dialog.setGrants(getUnchosenGrants());
		dialog.setCallback(new ServiceCallback<Grant>() {
			public void run(Grant result) {
				if (result != null)
					addGrant(result);
			}
		});
		dialog.show(getSupportFragmentManager(), "");
	}
	
	public List<Grant> getUnchosenGrants() {
		ArrayList<Grant> unchosenGrants = new ArrayList<Grant>(Arrays.asList(grantInfo));
		unchosenGrants.removeAll(selectedGrants);
		return unchosenGrants;
	}
	
	public void addGrant(Grant grant) {
//		Log.i("addgrant", "adding grant " + grantIndex);
//		Log.i("addgrant", "grants: " + grants);
//		Log.i("addgrant", "this grant: "  + grants[grantIndex]);
		final View grantRow = getLayoutInflater().inflate(R.layout.tablerow_grantselect, grantTable, false);
		((TextView)grantRow.findViewById(R.id.grant_select_grantname)).setText(grant.name);
		grantRow.findViewById(R.id.removeGrant).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				int delIndex = grantTable.indexOfChild(grantRow);
				grantTable.removeViewAt(delIndex);
				selectedGrants.remove(delIndex);
			}
		});
		selectedGrants.add(grant);
		grantTable.addView(grantRow);
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
//		boolean grantFail = false;
//		
//		ArrayList<JSONObject> grantidlist = new ArrayList<JSONObject>();
//		for (int i = 0; i < 4; i++) {
//			String text = grantViews[i].getText().toString();
//			int pos = Arrays.binarySearch(grantNames, text);
//			if (pos >= 0) {
//				grantidlist.add(grants[pos]);
//			} else if (text != null && !text.equals("")) {
//				grantViews[i].setError("Choose a grant from the dropdown, or leave empty");
//				grantFail = true;
//			}
//		}
//		
//		if (grantFail) {
//			return;
//		} else if (grantidlist.size() == 0) {
//			grantViews[0].setError("Select one or more grants");
//			return;
//		}
		
		Intent intent = new Intent(GrantSelectActivity.this, CalendarEditActivity.class);
		intent.putExtras(getIntent());

		int[] grantids = new int[selectedGrants.size()];
		String[] grantnames = new String[selectedGrants.size()];
//		try {
			for (int i = 0; i < grantids.length; i++) {
				Grant grant = selectedGrants.get(i);
				grantids[i]   = grant.id;
				grantnames[i] = grant.name;
			}
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}

		intent.putExtra(TAG_INTENT_GRANT_IDS, grantids);
		intent.putExtra(TAG_INTENT_GRANT_NAMES, grantnames);
		intent.putExtra("editing", true);
		startActivity(intent);
	}

	private void loadGrants() {
		new JSONParser.RequestBuilder("http://mid-state.net/mobileclass2/android")
		.addParam("q", "listallgrants")
		.makeRequest(new JSONParser.SimpleResultHandler() {
			@Override
			public void onSuccess(Object result) throws JSONException, IOException {
				JSONArray jsonGrants = (JSONArray) result;
				grants = new JSONObject[jsonGrants.length()];
				for (int i = 0; i < jsonGrants.length(); i++) {
					grants[i] = jsonGrants.getJSONObject(i);
				}
				Arrays.sort(grants, grantComparator);
				finishGrantInit();
			}
		});
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
		selectedGrants = new ArrayList<Grant>();
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
	
	Comparator<JSONObject> grantComparator = new Comparator<JSONObject>() {
		public int compare(JSONObject lhs, JSONObject rhs) {
			try {
				return lhs.getString("grantTitle").compareTo(rhs.getString("grantTitle"));
			} catch (JSONException e) {
				return 0;
			}
		}
	};

}
