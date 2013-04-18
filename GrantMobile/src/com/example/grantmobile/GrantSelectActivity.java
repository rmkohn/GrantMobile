package com.example.grantmobile;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TableLayout;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.os.Build;

public class GrantSelectActivity extends Activity {
	AutoCompleteTextView[] grantViews;
	
	// TEMPORARY until we decide which values to display and which to discard
	JSONObject[] grants;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_grant_select);
		// Show the Up button in the action bar.
		setupActionBar();
		
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
				loadAutocompleteViews();
			}
		});
	}

	protected void loadAutocompleteViews() {
		String[] grantNames = new String[grants.length];
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

}
