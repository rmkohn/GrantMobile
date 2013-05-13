package com.example.grantmobile;

import java.lang.reflect.Array;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.Toast;

// App-level stuff that really, truly doesn't belong anywhere else
public final class GrantApp {
	public static final String requestURL = "http://mid-state.net/mobileclass2/android";
	public static final String[] monthNames = {
		"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"
	};

	private GrantApp() { }
	
	public static JSONArray getJSONArray(Object array) {
		JSONArray j = new JSONArray();
		for (int i = 0; i < Array.getLength(array); i++)
			j.put(Array.get(array, i));
		return j;
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
	
	@SuppressLint("ShowToast")
	public static Toast makeIconToast(Context context, String message, int length) {
		Toast ret = new Toast(context);
		ret.setView(LayoutInflater.from(context).inflate(R.layout.toast_layout, null));
		((TextView)ret.getView().findViewById(android.R.id.message)).setText(message);
		ret.setDuration(length);
		return ret;
	}

}
