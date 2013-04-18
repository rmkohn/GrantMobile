package com.example.grantmobile;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.os.Build;

public class GrantSelectActivity extends Activity {
	AutoCompleteTextView[] grantViews;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_grant_select);
		// Show the Up button in the action bar.
		setupActionBar();
		
		grantViews = new AutoCompleteTextView[4];
		grantViews[0] = (AutoCompleteTextView)findViewById(R.id.grant1Autocomplete);
		grantViews[1] = (AutoCompleteTextView)findViewById(R.id.grant2Autocomplete);
		grantViews[2] = (AutoCompleteTextView)findViewById(R.id.grant3Autocomplete);
		grantViews[3] = (AutoCompleteTextView)findViewById(R.id.grant4Autocomplete);
		
		loadGrants();
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
