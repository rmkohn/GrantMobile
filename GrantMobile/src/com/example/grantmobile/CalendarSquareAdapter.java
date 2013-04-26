package com.example.grantmobile;

import com.example.grantmobile.CalendarSquare.DaySquare;
import com.example.grantmobile.CalendarSquare.ICalendarSquare;

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
		ICalendarSquare square = calendar.getSquare(position);
		int resource = square instanceof DaySquare
			? R.layout.calendar_date_element
			: R.layout.calendar_placeholder_element;
		if (convertView == null) {  // if it's not recycled, initialize some attributes
			view = (TextView)inflater.inflate(resource, parent, false);
//			imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
		} else {
			view = (TextView) convertView;
		}
		
		view.setBackgroundColor(square.getHighlightColor());
		view.setText(square.getMessage());

		return view;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public boolean isEnabled(int position) {
		return calendar.getSquare(position) instanceof DaySquare;
	}

	@Override
	public int getItemViewType(int position) {
		return calendar.getSquare(position) instanceof DaySquare ? 0 : 1;
	}
}
