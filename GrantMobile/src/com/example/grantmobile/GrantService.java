package com.example.grantmobile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.grantmobile.DetailViewActivity.JSONResultHandler;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class GrantService extends Service {
	
	public static final String TAG_REQUEST_DETAILS = "requestDetails";
	
	private static String requestURL = "http://mid-state.net/mobileclass2/android";
	private static final String TAG = "grantservice";
	
	DBAdapter db;
	GrantBinder binder;

	@Override
	public void onCreate() {
		super.onCreate();
		db = new DBAdapter();
		binder = new GrantBinder();
		Log.w(TAG, "newly created");
	}

	@Override
	public void onDestroy() {
		// TODO save to permanent storage (server or database)
		super.onDestroy();
		Log.w(TAG, "grant service shut down");
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	public void uploadHours(final GrantData data, final String[] grantStrings, ServiceCallback<Integer> callback) {
		new AsyncServiceCallback<Integer>(callback) {
			Integer doInBackground() {
				Map<String, double[]> hours = db.getTimes(data, grantStrings);
				try {
					// this is seriously the only way to load variables into the json object
					// the map constructor for JSONObject and collection constructor for JSONArray don't work
					JSONObject jsonHours = new JSONObject();
					for (String key: hours.keySet()) {
						JSONArray array = new JSONArray();
						for (double hour: hours.get(key)) {
							array.put(hour);
						}
						jsonHours.put(key, array);
					}
					Log.i(TAG, jsonHours.toString(2));
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("q", "updatehours"));
					params.add(new BasicNameValuePair("employee", String.valueOf(data.employeeid)));
					params.add(new BasicNameValuePair("year", String.valueOf(data.year)));
					params.add(new BasicNameValuePair("month", String.valueOf(data.month)));
					params.add(new BasicNameValuePair("hours", jsonHours.toString()));
					params.add(new BasicNameValuePair("supervisor", "-1")); // not actually used, due to reasons
					JSONObject result = JSONParser.makeHttpRequest(requestURL, "GET", params);
					Log.i(TAG, result.toString());
					return result.getBoolean("success") ? Activity.RESULT_OK : Activity.RESULT_CANCELED;
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return Activity.RESULT_CANCELED;
			}
		};
	}
	
	public int saveHours(GrantData data, Map<String, double[]> hours) {
		for (String key: hours.keySet()) {
			db.saveEntry(data, key, hours.get(key));
		}
		return Activity.RESULT_OK;
	}
	
	public int deleteHours(GrantData data, String[] grantStrings) {
		int deletedCount = db.deleteEntries(data, grantStrings);
		return deletedCount == grantStrings.length ? Activity.RESULT_OK : Activity.RESULT_CANCELED;
	}

	public static interface ServiceCallback<T> {
		public void run(T result);
	}
	
	abstract class AsyncServiceCallback<T> extends AsyncTask<Void, Void, T> {
		private ServiceCallback<T> callback;
		public AsyncServiceCallback(ServiceCallback<T> callback) {
			this.callback = callback;
		}
		@Override protected T doInBackground(Void... params) {
			return doInBackground();
		}
		abstract T doInBackground();
		@Override protected void onPostExecute(T result) {
			callback.run(result);
		}
		public void execute() {
			super.execute((Void[])null);
		}
	}
	
	public void getHours(final GrantData data, final String[] grantStrings, ServiceCallback<Map<String, double[]>> callback) {
		// see if the db has our entries
		new AsyncServiceCallback<Map<String, double[]>>(callback) {
			@Override
			protected Map<String, double[]> doInBackground() {
				Map<String, double[]> allHours = db.getTimes(data, grantStrings);

				if (allHours.size() < grantStrings.length) {
					// not enough entries? okay, fetch the ones we're missing
					Set<String> missingKeys = new HashSet<String>(Arrays.asList(grantStrings));
					missingKeys.removeAll(allHours.keySet());
					Log.i(TAG, "retrieving values");
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("q", "viewrequest"));
					params.add(new BasicNameValuePair("employee", String.valueOf(data.employeeid)));
					params.add(new BasicNameValuePair("year", String.valueOf(data.year)));
					params.add(new BasicNameValuePair("month", String.valueOf(data.month)));
					params.add(new BasicNameValuePair("grant", DBAdapter.mkString(missingKeys, ",", "", "")));
					Log.i(TAG, "making request with params: " + params);
					JSONObject json = JSONParser.makeHttpRequest(requestURL, "GET", params);
					try {
						if (json.getBoolean("success")) {
							// load doubles from json
							JSONObject message = json.getJSONObject("message");
							// (I don't know why this is unchecked)
							@SuppressWarnings("unchecked")
							Iterator<String> keys = message.keys();
							while(keys.hasNext()) {
								String key = keys.next();
								JSONArray jsonHours = message.getJSONArray(key);
								double[] hours = new double[jsonHours.length()];
								for (int i = 0; i < hours.length; i++) {
									hours[i] = jsonHours.getDouble(i);
								}
								// save to our returned map, and to the db
								allHours.put(key, hours);
								db.saveEntry(data, key, hours);
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
						return null;
					}
				} else {
					Log.i(TAG, "got values from cache");
				}
				return allHours;
			}
		}.execute();
	}
	
	public class GrantBinder extends Binder {
		GrantService getService() {
			return GrantService.this;
		}
	}
	
	// class to hold the time/employee details needed to specify a grant's hours
	// TODO: give it a more meaningful (or meaningful at all) name
	public static class GrantData implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 3194878610273425327L;
		public static final int GRANTID_LEAVE = -1;
		public static final int GRANTID_NONGRANT = -2;
		int employeeid;
		int year;
		int month;
		@Override public int hashCode() {
			// pretty much copied out of the android reference, is this the best approach?
			int result = 0xABCD;
			result += 31 * result + employeeid;
			result += 31 * result + year;
			result += 31 * result + month;
			return result;
		}
		@Override public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof GrantData))
				return false;
			GrantData other = (GrantData) o;
			return employeeid == other.employeeid && year == other.year && month == other.month;
		}
		public GrantData(int year, int month, int employeeid) {
			this.year = year;
			this.month = month;
			this.employeeid = employeeid;
		}
		@Override public String toString() {
			return String.format("GrantData(%d, %d, %d)", year, month, employeeid);
		}
	}

	public void sendEmailRequest(String requestId, JSONResultHandler jsonResultHandler) {
		new JSONParser.RequestBuilder()
		.setUrl(requestURL)
		.addParam("q", "email")
		.addParam("id", requestId)
		.makeRequest(jsonResultHandler);
	}
	
}