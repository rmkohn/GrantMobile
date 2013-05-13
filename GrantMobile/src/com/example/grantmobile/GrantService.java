package com.example.grantmobile;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.grantmobile.DBAdapter.Hours;
import com.example.grantmobile.DBAdapter.Hours.GrantStatus;
import com.example.grantmobile.JSONParser.ResultHandler;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.JsonReader;
import android.util.Log;

public class GrantService extends Service {
	
	public static final String TAG_REQUEST_DETAILS = "requestDetails";
	
	private static final String TAG = "grantservice";
	
	DBAdapter db;
	GrantBinder binder;
	Map<Map<String, String>, Object> queryCache;
	Set<Runnable> updateListeners;

	@Override
	public void onCreate() {
		super.onCreate();
		db = new DBAdapter();
		binder = new GrantBinder();
		queryCache = new HashMap<Map<String, String>, Object>();
		updateListeners = new HashSet<Runnable>();
		Log.w(TAG, "newly created");
	}

	@Override
	public void onDestroy() {
		// TODO save only the changed values to permanent storage (server or database)
		for (final GrantData entry: db.cache.keySet()) {
			this.uploadHoursFromDB(entry, db.getAllTimes(entry), new JSONParser.SimpleResultHandler<JSONObject>(null) {
				public void onSuccess(JSONObject result) {
					Log.i(TAG, "saved hours for " + entry + " to server");
				}
				public void onFailure(String errorMessage) {
					Log.i(TAG, "failed to save hours for " + entry);
				}
				public void onError(Exception e) {
					e.printStackTrace();
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
	
	@Override
	public int onStartCommand(Intent intent, int flags, final int startId) {
		// calling startService() keeps the service alive for 5 seconds
		// this is used as a horrible hack to keep it from shutting down when the device
		// is rotated (which destroys the activity binding us)
		new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
			public void run() {
				stopSelf(startId);
			}
		}, 5000);
		return Service.START_NOT_STICKY;
	}
	
	public boolean addUpdateCallback(Runnable callback) {
		return updateListeners.add(callback);
	}
	public boolean removeUpdateCallback(Runnable callback) {
		return updateListeners.remove(callback);
	}
	public void notifyUpdateListeners() {
		for (Runnable listener: updateListeners)
			listener.run();
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Object> void sendGenericRequest(final Map<String, String> params, ResultHandler<T> handler) {
		Object cached = queryCache.get(params);
		if (cached == null) {
			new JSONParser.RequestBuilder()
			.setUrl(GrantApp.requestURL)
			.addAllParams(params)
			.makeRequest(new JSONParser.ResultHandlerWrapper<Object, T>(handler) {
				public void onSuccess(Object result) throws JSONException {
					Log.i(TAG + " sendGenericRequest", "caching result for " + params);
					super.onWrappedSuccess((T)result);
					queryCache.put(params, result);
				}
			});
		} else {
			Log.i(TAG + " sendGenericRequest", "retrieving result for " + params);
			try {
				handler.onSuccess((T)cached);
			} catch (Exception e) {
				// caching is done after onSuccess() completes the first time, so this should not happen
				throw(new RuntimeException(e));
			}
		}
	}
	
	public void sendEmailRequest(final Uri emailUri, final ResultHandler<JSONObject> jsonResultHandler) {
		Map<String, String> query = new HashMap<String, String>();
		query.put("q", "email");
		if (emailUri.getPath().contains("aspx")) { // old-style email
			query.put("grant", emailUri.getQueryParameter("GrantID"));
			query.put("employee", emailUri.getQueryParameter("Employee"));
			query.put("month", emailUri.getQueryParameter("month"));
			query.put("year", emailUri.getQueryParameter("Year"));
			query.put("supervisor", emailUri.getQueryParameter("ID"));
		} else {
			query.put("id", emailUri.getQueryParameter("ID"));
		}
		sendGenericRequest(query, jsonResultHandler);
	}
	
	public void uploadHours(final GrantData data, final String[] grantStrings, ResultHandler<JSONObject> callback) {
		uploadHoursFromDB(data, db.getTimes(data, grantStrings), callback);
	}
	
	public void uploadHours(final GrantData data, final Map<String, double[]> hours, ResultHandler<JSONObject> callback) {
		try {
			// this is seriously the only way to load variables into the json object
			// the map constructor for JSONObject and collection constructor for JSONArray don't work
			JSONObject jsonHours = new JSONObject();
			for (String key: hours.keySet()) {
				jsonHours.put(key, GrantApp.getJSONArray(hours.get(key)));
			}
			uploadHours(data, jsonHours, callback);
		} catch (JSONException e) {
			callback.onError(e);
		}
	}
	
	public void uploadHoursFromDB(final GrantData data, final Map<String, Hours> hours, ResultHandler<JSONObject> callback) {
		try {
			JSONObject jsonHours = new JSONObject();
			for (String key: hours.keySet()) {
				jsonHours.put(key, GrantApp.getJSONArray(hours.get(key).hours));
			}
			uploadHours(data, jsonHours, callback);
		} catch (JSONException e) {
			callback.onError(e);
		}
	}
	
	private void uploadHours(GrantData data, JSONObject jsonHours, ResultHandler<JSONObject> callback) throws JSONException {
			Log.i(TAG, jsonHours.toString(2));
			new JSONParser.RequestBuilder(GrantApp.requestURL)
			.addParam("q", "updatehours")
			.addParam("employee", String.valueOf(data.employeeid))
			.addParam("year", String.valueOf(data.year))
			.addParam("month", String.valueOf(data.month))
			.addParam("hours", jsonHours.toString())
			.addParam("supervisor", "-1") // not actually used, due to reasons
			.makeRequest(callback);
	}
	
	public int saveHours(GrantData data, Map<String, double[]> hours) {
		boolean success = true;
		for (String key: hours.keySet()) {
			if (!db.saveEntry(data, key, hours.get(key)))
				success = false;
		}
		notifyUpdateListeners();
		return success ? Activity.RESULT_OK : Activity.RESULT_CANCELED;
	}
	
	public int saveHourStatus(GrantData data, int grant, Hours.GrantStatus status) {
		int ret = db.updateStatus(data, String.valueOf(grant), status) ? Activity.RESULT_OK : Activity.RESULT_CANCELED;
		notifyUpdateListeners();
		return ret;
	}
	
	public int deleteHours(GrantData data, String[] grantStrings) {
		int deletedCount = db.deleteEntries(data, grantStrings);
		return deletedCount == grantStrings.length ? Activity.RESULT_OK : Activity.RESULT_CANCELED;
	}
	
	public void getHours(final GrantData data, final String[] grantStrings, ResultHandler<Map<String, Hours>> handler) {
		// see if the db has our entries
		final Map<String, Hours> allHours = db.getTimes(data, grantStrings);

		if (allHours.size() < grantStrings.length) {
			// not enough entries? okay, fetch the ones we're missing
			final Set<String> missingKeys = new HashSet<String>(Arrays.asList(grantStrings));
			missingKeys.removeAll(allHours.keySet());
			Log.i(TAG, "retrieving values");
			new JSONParser.RequestBuilder(GrantApp.requestURL)
			.addParam("q", "viewrequest")
			.addParam("employee", String.valueOf(data.employeeid))
			.addParam("year", String.valueOf(data.year))
			.addParam("month", String.valueOf(data.month))
			.addParam("grant", DBAdapter.mkString(missingKeys, ",", "", ""))
			.addParam("withstatus", "true")
			.makeRequest(new JSONParser.ResultHandlerWrapper<JSONObject, Map<String, Hours>>(handler) {
				public void onSuccess(JSONObject message) throws JSONException {
					// load doubles from json
					JSONObject granthours = message.getJSONObject("hours");
					JSONObject statuses = message.getJSONObject("status"); // statuses? stati?
					// (I don't know why this is unchecked)
					@SuppressWarnings("unchecked")
					Iterator<String> keys = granthours.keys();
					while(keys.hasNext()) {
						String key = keys.next();
						JSONArray jsonHours = granthours.getJSONArray(key);
						Hours.GrantStatus status = Hours.GrantStatus.valueOf(statuses.optString(key, "none"));
						double[] hours = new double[jsonHours.length()];
						for (int i = 0; i < hours.length; i++) {
							hours[i] = jsonHours.getDouble(i);
						}
						// save to our returned map, and to the db
						Hours hoursWithStatus = new Hours(status, hours);
						allHours.put(key, hoursWithStatus);
						db.saveEntry(data, key, hoursWithStatus);
					}
					missingKeys.removeAll(allHours.keySet());
					if (!missingKeys.isEmpty()) {
						Log.e("grantservice", "still missing values!");
						for (String s: missingKeys) {
							Log.e("grantservice", s);
						}
						super.onFailure("missing values");
					} else {
						super.onWrappedSuccess(allHours);
					}
				}
			});
		} else {
			Log.i(TAG, "got values from cache");
			try {
				handler.onSuccess(allHours);
			} catch (Exception e) {
				throw(new RuntimeException(e)); // as with sendGenericRequest, this shouldn't happen
			}
		}
	}
	
	public void getGrants(final JSONParser.ResultHandler<JSONObject[]> handler) {
		Map<String, String> query = new HashMap<String, String>(1);
		query.put("q", "listallgrants");
		sendGenericRequest(query, new JSONParser.ResultHandlerWrapper<JSONArray, JSONObject[]>(handler) {
			@Override public void onSuccess(JSONArray result) throws JSONException {
				onWrappedSuccess(getJSONObjectArray(result));
			}
		});
	}
	
	public void getGrantByParameter(final String key, final Object param, final ResultHandler<JSONObject> handler) {
		getGrants(new JSONParser.ResultHandlerWrapper<JSONObject[], JSONObject>(handler) {
			public void onSuccess(JSONObject[] result) throws JSONException {
				try {
					for (JSONObject grant: result) {
						if (grant.get(key).equals(param)) {
							super.onWrappedSuccess(grant);
							return;
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				super.onWrappedSuccess(null);
			}
		});
	}
	
	public void getSupervisors(final ResultHandler<JSONObject[]> handler) {
		Map<String, String> query = new HashMap<String, String>(1);
		query.put("q", "listsupervisors");
		sendGenericRequest(query, new JSONParser.ResultHandlerWrapper<JSONArray, JSONObject[]>(handler) {
			@Override public void onSuccess(JSONArray result) throws JSONException {
				super.onWrappedSuccess(getJSONObjectArray(result));
			}
		});
	}
	
	public void saveNewRequests() {
		JSONArray users = new JSONArray();
		for (Entry<GrantData, Map<String, Hours>> entry: db.cache.entrySet()) {
			try {
				for (Entry<String, Hours> grant: entry.getValue().entrySet()) {
					if (grant.getValue().status == Hours.GrantStatus.New) {
						JSONObject data = new JSONObject();
						data.put("year", entry.getKey().year);
						data.put("month", entry.getKey().month);
						data.put("empid", entry.getKey().employeeid);
						data.put("grants", grant.getKey());
						users.put(data);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		OutputStreamWriter out = null;
		try {
			out = new OutputStreamWriter(new BufferedOutputStream(
					openFileOutput("new_grants", Context.MODE_PRIVATE)));
			out.write(users.toString());
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			if (out != null) {
				try {
					out.close();
				} catch (IOException f) {
					f.printStackTrace();
				}
			}
		}
	}
	
	public void loadNewRequests(int empid, final ResultHandler<Map<GrantData, Map<String, Hours>>> handler) {
		new JSONParser.RequestBuilder(GrantApp.requestURL)
		.addParam("q", "listrequests")
		.addParam("employee", String.valueOf(empid))
		.makeRequest(new JSONParser.ResultHandlerWrapper<JSONArray, Map<GrantData, Map<String, Hours>>>(handler) {
			public void onSuccess(JSONArray result) throws JSONException {
				String newRequests = GrantApp.readFile(GrantService.this, "new_grants");
				if (newRequests != null)
					addStatusesToDb(new JSONArray(newRequests));
				addStatusesToDb(result);
				super.onWrappedSuccess(db.cache);
			}
		});
	}
	
	private void addStatusesToDb(JSONArray statuses) throws JSONException {
		for (int i = 0; i < statuses.length(); i++) {
			JSONObject obj = statuses.getJSONObject(i);
			GrantData data = new GrantData(obj.getInt("year"), obj.getInt("month"), obj.getJSONObject("employee").getInt("id"));
			db.updateStatus(data, obj.getString("grant"), GrantStatus.valueOf(obj.optString("status", "New")));
		}
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
	
	public static JSONObject[] getJSONObjectArray(JSONArray array) throws JSONException {
		JSONObject[] result = new JSONObject[array.length()];
		for (int i = 0; i < array.length(); i++) {
			JSONObject grant = array.getJSONObject(i);
			result[i] = grant;
		}
		return result;
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
}