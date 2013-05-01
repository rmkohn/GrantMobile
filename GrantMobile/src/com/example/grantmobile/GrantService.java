package com.example.grantmobile;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.grantmobile.JSONParser.ResultHandler;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseArray;

public class GrantService extends Service {
	
	public static final String TAG_REQUEST_DETAILS = "requestDetails";
	
	private static String requestURL = "http://mid-state.net/mobileclass2/android";
	private static final String TAG = "grantservice";
	
	DBAdapter db;
	GrantBinder binder;
//	Map<String, JSONObject> emailRequestCache;
	Map<Map<String, String>, Object> queryCache;
//	JSONObject[] grants;

	@Override
	public void onCreate() {
		super.onCreate();
		db = new DBAdapter();
		binder = new GrantBinder();
//		emailRequestCache = new HashMap<String, JSONObject>();
		queryCache = new HashMap<Map<String, String>, Object>();
		Log.w(TAG, "newly created");
	}

	@Override
	public void onDestroy() {
		// TODO save only the changed values to permanent storage (server or database)
		for (final Map.Entry<GrantData, Map<String, double[]>> entry: db.cache.entrySet()) {
			this.uploadHours(entry.getKey(), entry.getValue(), new ServiceCallback<Integer>() {
				public void run(Integer result) {
					if (result == Activity.RESULT_OK) {
						Log.i(TAG, "saved hours for " + entry.getKey() + " to server");
					} else {
						Log.i(TAG, "failed to save hours for " + entry.getKey());
					}
				}
			});
		}
		super.onDestroy();
		Log.w(TAG, "grant service shut down");
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	public void sendGenericRequest(final Map<String, String> params, ResultHandler handler) {
		Object cached = queryCache.get(params);
		if (cached == null) {
			new JSONParser.RequestBuilder()
			.setUrl(requestURL)
			.addAllParams(params)
			.makeRequest(new JSONParser.ResultHandlerWrapper(handler) {
				public void onSuccess(Object result) throws JSONException, IOException {
					Log.i(TAG + " sendGenericRequest", "caching result for " + params);
					super.onSuccess(result);
					queryCache.put(params, result);
				}
			});
		} else {
			Log.i(TAG + " sendGenericRequest", "retrieving result for " + params);
			try {
				handler.onSuccess(cached);
			} catch (Exception e) {
				// caching is done after onSuccess() completes the first time, so this should not happen
				throw(new RuntimeException(e));
			}
		}
	}

	public void sendEmailRequest(final String requestId, final ResultHandler jsonResultHandler) {
		Map<String, String> query = new HashMap<String, String>(2);
		query.put("q", "email");
		query.put("id", requestId);
		sendGenericRequest(query, jsonResultHandler);
	}
	
	public void uploadHours(final GrantData data, final String[] grantStrings, ServiceCallback<Integer> callback) {
		uploadHours(data, db.getTimes(data, grantStrings), callback);
	}
	
	public void uploadHours(final GrantData data, final Map<String, double[]> hours, ServiceCallback<Integer> callback) {
		new AsyncServiceCallback<Integer>(callback) {
			Integer doInBackground() {
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
					JSONObject result = new JSONParser.RequestBuilder(requestURL)
					.addParam("q", "updatehours")
					.addParam("employee", String.valueOf(data.employeeid))
					.addParam("year", String.valueOf(data.year))
					.addParam("month", String.valueOf(data.month))
					.addParam("hours", jsonHours.toString())
					.addParam("supervisor", "-1") // not actually used, due to reasons
					.makeHttpRequest();
					Log.i(TAG, result.toString());
					return result.getBoolean("success") ? Activity.RESULT_OK : Activity.RESULT_CANCELED;
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return Activity.RESULT_CANCELED;
			}
		}.execute();
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
					JSONObject json = new JSONParser.RequestBuilder(requestURL)
					.addParam("q", "viewrequest")
					.addParam("employee", String.valueOf(data.employeeid))
					.addParam("year", String.valueOf(data.year))
					.addParam("month", String.valueOf(data.month))
					.addParam("grant", DBAdapter.mkString(missingKeys, ",", "", ""))
					.makeHttpRequest();
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
					missingKeys.removeAll(allHours.keySet());
					if (!missingKeys.isEmpty()) {
						Log.e("grantservice", "still missing values!");
						for (String s: missingKeys) {
							Log.e("grantservice", s);
						}
					}
				} else {
					Log.i(TAG, "got values from cache");
				}
				return allHours;
			}
		}.execute();
	}
	
	public void getGrants(final ServiceCallback<JSONObject[]> callback) {
		Map<String, String> query = new HashMap<String, String>(1);
		query.put("q", "listallgrants");
		sendGenericRequest(query, new JSONParser.SimpleResultHandler() {
			@Override
			public void onSuccess(Object result) throws JSONException, IOException {
				JSONArray jsonGrants = (JSONArray) result;
				JSONObject[] grants = new JSONObject[jsonGrants.length()];
				for (int i = 0; i < jsonGrants.length(); i++) {
					JSONObject grant = jsonGrants.getJSONObject(i);
					grants[i] = grant;
				}
				callback.run(grants);
			}
		});
	}
	
	public void getGrantByParameter(final String key, final Object param, final ServiceCallback<JSONObject> callback) {
		getGrants(new ServiceCallback<JSONObject[]>() {
			public void run(JSONObject[] result) {
				try {
					for (JSONObject grant: result) {
						if (grant.get(key).equals(param)) {
							callback.run(grant);
							return;
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				callback.run(null);
			}
		});
	}
	
	public class GrantBinder extends Binder {
		GrantService getService() {
			return GrantService.this;
		}
	}
	
	public static String[] getGrantStrings(int[] grantids) {
		String[] grantStrings = new String[grantids.length + 2];
		for (int i = 0; i < grantids.length; i++) {
			grantStrings[i] = String.valueOf(grantids[i]);
		}
		grantStrings[grantids.length]   = "non-grant";
		grantStrings[grantids.length+1] = "leave";
		return grantStrings;
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
			return String.format(Locale.US, "GrantData(%d, %d, %d)", year, month, employeeid);
		}
	}
	
	public static interface ServiceCallback<T> {
		public void run(T result);
	}
	
	public static class ResultHandlerCallback implements ServiceCallback<JSONObject> {
		private JSONParser.ResultHandler handler;
		public ResultHandlerCallback(JSONParser.ResultHandler handler) {
			this.handler = handler;
		}
		public void run(JSONObject result) {
			JSONParser.handleResults(result, handler);
		}
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
}