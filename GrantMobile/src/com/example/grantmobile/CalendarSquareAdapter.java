package com.example.grantmobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CalendarSquareAdapter extends BaseAdapter {
	private CalendarArray calendar;
	private Context mContext;
	private LayoutInflater inflater;

	public CalendarSquareAdapter(Context c, CalendarArray calendar) {
		mContext = c;
		this.calendar = calendar;
		inflater = LayoutInflater.from(c);
	}

	public int getCount() {
		return calendar.getNumberOfSquares();
	}

	public Object getItem(int position) {
		return calendar.getSquare(position);
	}

	public long getItemId(int position) {
		return position;
	}

	// create a new ImageView for each item referenced by the Adapter
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView view;
		if (convertView == null) {  // if it's not recycled, initialize some attributes
			view = (TextView)inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
//			imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
		} else {
			view = (TextView) convertView;
		}
		
		view.setText(calendar.getSquare(position).getMessage());

		return view;
	}
}
