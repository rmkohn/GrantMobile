package com.example.grantmobile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TableLayout;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;

public class GrantSelectActivity extends Activity {
	public static final String TAG_INTENT_GRANT_NAMES = "grantnames";

	public static final String TAG_INTENT_GRANT_IDS = "grantids";

	AutoCompleteTextView[] grantViews;
	
	// TEMPORARY until we decide which values to display and which to discard
	JSONObject[] grants;
	String[] grantNames;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_grant_select);
		// Show the Up button in the action bar.
		setupActionBar();
		
		dumpBundle(getIntent().getExtras());
		
		
		grantViews = new AutoCompleteTextView[4];
		TableLayout grantTable = (TableLayout)findViewById(R.id.grantTable);
		for (int i = 0; i < grantViews.length; i++) {
			View grantRow = getLayoutInflater().inflate(R.layout.tablerow_grantselect, grantTable, false);
			grantViews[i] = (AutoCompleteTextView) grantRow.findViewById(R.id.grantrow_autocomplete);
			grantViews[i].setThreshold(0);
			grantTable.addView(grantRow);
		}
		grantViews[0].requestFocus();
		
		loadGrants();
		
		findViewById(R.id.grant_select_continue).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	loadCalendarEditActivity();
            }
        });
	}

	public static void dumpBundle(Bundle extras) {
		Set<String> keys = extras.keySet();
		JSONObject bundle = new JSONObject();
		try {
			for (String key: keys) {
				bundle.put(key, extras.get(key));
			}
			Log.i("grantselect", bundle.toString(2));
		} catch (JSONException e) { e.printStackTrace(); }
	}

	protected void loadCalendarEditActivity() {
		boolean grantFail = false;
		
		ArrayList<JSONObject> grantidlist = new ArrayList<JSONObject>();
		for (int i = 0; i < 4; i++) {
			String text = grantViews[i].getText().toString();
			int pos = Arrays.binarySearch(grantNames, text);
			if (pos >= 0) {
				grantidlist.add(grants[pos]);
			} else if (text != null && !text.equals("")) {
				grantViews[i].setError("Choose a grant from the dropdown, or leave empty");
				grantFail = true;
			}
		}
		
		if (grantFail) {
			return;
		} else if (grantidlist.size() == 0) {
			grantViews[0].setError("Select one or more grants");
			return;
		}
		
		Intent intent = new Intent(GrantSelectActivity.this, CalendarEditActivity.class);
		intent.putExtras(getIntent());

		int[] grants = new int[grantidlist.size()];
		String[] grantnames = new String[grantidlist.size()];
		try {
			for (int i = 0; i < grantidlist.size(); i++) {
				grants[i] = grantidlist.get(i).getInt("ID");
				grantnames[i] = grantidlist.get(i).getString("grantTitle");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		intent.putExtra(TAG_INTENT_GRANT_IDS, grants);
		intent.putExtra(TAG_INTENT_GRANT_NAMES, grantnames);
		intent.putExtra("editing", true);
		startActivity(intent);
		// TODO Auto-generated method stub

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
				loadAutocompleteViews();
			}
		});
	}

	protected void loadAutocompleteViews() {
		grantNames = new String[grants.length];
		try {
			for (int i = 0; i < grantNames.length; i++) {
				grantNames[i] = grants[i].getString("grantTitle");
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return;
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
			this, android.R.layout.simple_dropdown_item_1line, grantNames
		);
		for (AutoCompleteTextView view: grantViews) {
			view.setAdapter(adapter);
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
