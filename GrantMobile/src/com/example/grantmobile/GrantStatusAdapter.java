
package com.example.grantmobile;

import com.example.grantmobile.DBAdapter.Hours;
import com.example.grantmobile.DBAdapter.Hours.GrantStatus;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class GrantStatusAdapter extends BaseAdapter {
	private Context context;
	private SparseArray<Hours> granthours;
	private int[] grantids;
	private SparseArray<String> grantnames;
	
	public GrantStatusAdapter(Context context, SparseArray<Hours> granthours, int[] grantids, SparseArray<String> grantnames) {
		this.context = context;
		this.granthours = granthours;
		this.grantids = grantids;
		this.grantnames = grantnames;
	}
	Hours defaultStatus = new Hours(Hours.GrantStatus.New, null);
	
	public Hours.GrantStatus getHighestPriorityStatus() {
		Hours.GrantStatus highest = GrantStatus.none;
		for (int id: grantids) {
			Hours.GrantStatus test = granthours.get(id).status;
			if (test.getPriority() > highest.getPriority()) {
				highest = test;
			}
		}
		return highest;
	}
	
	public int getPriority() {
		return getHighestPriorityStatus().getPriority();
	}
	
	public int getCount() { return grantids.length; }
	
	public Object getItem(int position) {
		return getName(position);
	}
	
	public String getName(int position) {
		return grantnames.get(grantids[position]);
	}
	public Hours getHours(int position) {
		return granthours.get(grantids[position]);
	}
	public int getGrantId(int position) {
		return grantids[position];
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
			convertView = (TextView)LayoutInflater.from(context).inflate(id, parent, false);
		}
		TextView text = (TextView) convertView.findViewById(android.R.id.text1);
		text.setText(grantnames.get(grantids[position]));
		setDrawable(text, position);
		return convertView;
	}

	private void setDrawable(TextView t, int pos) {
		if (granthours == null || grantids == null)
			return;
		Drawable d = context.getResources().getDrawable(granthours.get(grantids[pos], defaultStatus).status.getDrawable());
		setDrawable(t, d, 50);
	}
	
	public static void setDrawable(TextView t, Drawable d, int height) {
		float ratio = ((float)d.getIntrinsicWidth()) / ((float)d.getIntrinsicHeight());
		d.setBounds(0, 0, (int)(ratio*height), height);
		t.setCompoundDrawables(null, null, d, null);
	}
}
