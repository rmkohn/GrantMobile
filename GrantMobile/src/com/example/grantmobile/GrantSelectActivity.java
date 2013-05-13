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

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Button;
import android.content.Intent;

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
		if (grantInfo == null) {
			return;
		}
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
			getService().getGrants(new JSONParser.SimpleResultHandler<JSONObject[]>(this) {
				public void onSuccess(JSONObject[] result) {
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

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("grants", selectedGrants);
	}

}
