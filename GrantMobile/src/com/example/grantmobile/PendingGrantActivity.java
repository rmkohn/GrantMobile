package com.example.grantmobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.grantmobile.DBAdapter.Hours;
import com.example.grantmobile.EmployeeDialog.Employee;
import com.example.grantmobile.GrantService.GrantData;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

public class PendingGrantActivity extends GrantServiceBindingActivity {
	ExpandableListView pendingGrantView;
	int empid;
	List<ExpandableGrantData> groups;
	SparseArray<String> grantIdMappings;
	
	public class ExpandableGrantData implements Comparable<ExpandableGrantData>{
		GrantData data;
		GrantStatusAdapter adapter;
		public ExpandableGrantData(GrantData data, GrantStatusAdapter adapter) {
			this.data = data;
			this.adapter = adapter;
		}
		public int compareTo(ExpandableGrantData another) {
			int priority = adapter.getPriority() - another.adapter.getPriority();
			if (priority != 0)
				return priority;
			int year = data.year - another.data.year;
			if (year != 0)
				return year;
			int month = data.month - another.data.month;
			return month;
		}
	}

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		empid = ((Employee)getIntent().getSerializableExtra(LoginActivity.TAG_INTENT_USERID)).id;
		setContentView(R.layout.activity_pending_grants);
		pendingGrantView = (ExpandableListView) findViewById(R.id.pendingGrants);
	}

	@Override
	protected void onBound() {
		getService().getGrants(new JSONParser.SimpleResultHandler<JSONObject[]>(this) {
			public void onSuccess(JSONObject[] result) throws JSONException {
				grantIdMappings = new SparseArray<String>(result.length);
				for (JSONObject j: result) {
					grantIdMappings.append(j.getInt("ID"), j.getString("grantTitle"));
				}
				loadRequests();
			}
		});
	}
	private void loadRequests() {
		getService().loadNewRequests(empid, new JSONParser.SimpleResultHandler<Map<GrantData, Map<String, Hours>>>(this) {
			public void onSuccess(Map<GrantData, Map<String, Hours>> result) {
				groups = new ArrayList<ExpandableGrantData>(result.size());
				for (GrantData d: result.keySet()) {
					if (d.employeeid == empid) {
						groups.add(getGrantEntry(d, result.get(d)));
					}
				}
				Collections.sort(groups, Collections.reverseOrder());
				updateList();
			}
		});
	}
	
	private OnClickListener openMonthListener = new OnClickListener() {
		public void onClick(View v) {
			ExpandableGrantData data = (ExpandableGrantData)v.getTag();
			Intent intent = new Intent(PendingGrantActivity.this, CalendarEditActivity.class);
			intent.putExtras(getIntent());
			intent.putExtra(MonthSelectActivity.TAG_INTENT_YEAR, data.data.year);
			intent.putExtra(MonthSelectActivity.TAG_INTENT_MONTH, data.data.month+1);
			
			int[] grantids = new int[data.adapter.getCount()];
			String[] grantnames = new String[data.adapter.getCount()];
		
			for (int i = 0; i < grantids.length; i++) {
				grantids[i] = data.adapter.getGrantId(i);
				grantnames[i] = data.adapter.getName(i);
			}

			intent.putExtra(GrantSelectActivity.TAG_INTENT_GRANT_IDS, grantids);
			intent.putExtra(GrantSelectActivity.TAG_INTENT_GRANT_NAMES, grantnames);
			
			startActivity(intent);
		}
	};
		
	private ExpandableGrantData getGrantEntry(GrantData data, Map<String, Hours> hourmap) {
		final SparseArray<Hours> hours = new SparseArray<Hours>(hourmap.size());
		List<Integer> ids = new ArrayList<Integer>(hourmap.size());
		for (Entry<String, Hours> entry: hourmap.entrySet()) {
			try {
				int id = Integer.parseInt(entry.getKey());
				ids.add(id);
				hours.append(id, entry.getValue());
			} catch (NumberFormatException e) { }
		}
		Collections.sort(ids, new Comparator<Integer>() {
			public int compare(Integer lhs, Integer rhs) {
				int priority = hours.get(rhs).status.getPriority() - hours.get(lhs).status.getPriority();
				if (priority != 0)
					return priority;
				return grantIdMappings.get(lhs).compareTo(grantIdMappings.get(rhs));
			}
		});
		int[] idArray = new int[ids.size()];
		for (int j = 0; j < idArray.length; j++) {
			idArray[j] = ids.get(j);
		}
		GrantStatusAdapter adapter = 
				new GrantStatusAdapter(PendingGrantActivity.this, hours, idArray, grantIdMappings);
		return new ExpandableGrantData(data, adapter);
	}
	
	private void updateList() {
		pendingGrantView.setAdapter(new GrantAdapter());
	}
	
	private class GrantAdapter extends BaseExpandableListAdapter {

		@Override
		public int getGroupCount() {
			return groups.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return groups.get(groupPosition).adapter.getCount();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return groups.get(groupPosition);
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return groups.get(groupPosition).adapter.getItem(childPosition);
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public boolean hasStableIds() { return true; }

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			if (convertView == null)
				convertView = getLayoutInflater().inflate(R.layout.pending_grant_month_row, parent, false);
			GrantData datum = groups.get(groupPosition).data;
			String text = GrantApp.monthNames[datum.month] + " " + datum.year;
			TextView tv = (TextView)convertView.findViewById(android.R.id.text1);
			int drawableResource = groups.get(groupPosition).adapter.getHighestPriorityStatus().getDrawable();
			Drawable drawable = getResources().getDrawable(drawableResource);
			tv.setText(text);
			GrantStatusAdapter.setDrawable(tv, drawable, 60);
			View button = convertView.findViewById(R.id.btnLoadPendingGrant);
			button.setOnClickListener(openMonthListener);
			button.setTag(groups.get(groupPosition));
			// allow list to expand, as per stackoverflow.com/questions/11818278#11857492
			button.setFocusable(false);
			
			return convertView;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			return groups.get(groupPosition).adapter.getView(childPosition, convertView, parent);
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return false;
		}
	}

}
