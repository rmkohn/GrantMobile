package com.example.grantmobile;

import com.example.grantmobile.CalendarSquare.DaySquare;
import com.example.grantmobile.CalendarSquare.ICalendarSquare;
import com.example.grantmobile.CalendarSquare.TitleSquare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CalendarSquareAdapter extends BaseAdapter {
	private CalendarArray calendar;
	private LayoutInflater inflater;

	public CalendarSquareAdapter(Context c, CalendarArray calendar) {
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
		int width = parent.getWidth() / 8;
		TextView view;
		ICalendarSquare square = calendar.getSquare(position);
		if (convertView == null) {  // if it's not recycled, initialize some attributes
			int resource = square instanceof DaySquare
				? R.layout.calendar_date_element
				: R.layout.calendar_placeholder_element;
			convertView = inflater.inflate(resource, parent, false);
//			imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
		}
		if (square instanceof TitleSquare) {
			convertView.setMinimumHeight(0);
			convertView.setMinimumWidth(0);
		} else {
			convertView.setMinimumHeight(width);
			convertView.setMinimumWidth(width);
		}
		
		view = (TextView) convertView.findViewById(R.id.calendarTextView);
		
		view.setBackgroundColor(square.getHighlightColor());
		view.setText(square.getMessage());
		
		return convertView;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public int getViewTypeCount() {
		return 3;
	}

	@Override
	public boolean isEnabled(int position) {
		return calendar.getSquare(position) instanceof DaySquare;
	}

	@Override
	public int getItemViewType(int position) {
		ICalendarSquare square = calendar.getSquare(position);
		if      (square instanceof DaySquare)   return 0;
		else if (square instanceof TitleSquare) return 1;
		return 2;
	}
}
