package com.example.grantmobile;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

public class GrantService extends IntentService {
	
	private static String requestURL = "http://mid-state.net/mobileclass2/android";
	private static final String TAG = "grantservice";
	public static String requestId;
//	private int result;
	
	public static final String TAG_REQUEST_TYPE = "requestType";
	
	public static final String TAG_SAVEREQUEST_TYPE = "saveHours";
	public static final String TAG_VIEWREQUEST_TYPE = "getHours";
	public static final String TAG_UPLOAD_TYPE = "uploadHours";
	public static final String TAG_DELETE_TYPE = "removeHours";

	public static final String TAG_REQUEST_GRANTS = "grantIds";
	public static final String TAG_REQUEST_DETAILS = "grantDetails";
	
	
	
	
	DBAdapter db;
	GrantBinder binder;

	
	public GrantService() {
		super("GrantService");
	}
	
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
	
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.i(TAG, "handling intent");
	    Bundle extras = intent.getExtras();
	    if (extras == null)
	    	return;
    	Messenger messenger = (Messenger) extras.get("MESSENGER");
    	Message msg = Message.obtain();
    	int result = Activity.RESULT_CANCELED;
	    if (extras != null) {
	    	Bundle data = null;
	    	Object requestType = extras.get(TAG_REQUEST_TYPE);
	    	if (GrantService.TAG_VIEWREQUEST_TYPE.equals(requestType)) {
	    		data = getViewRequest(extras);
	    	} else if (GrantService.TAG_SAVEREQUEST_TYPE.equals(requestType)) {
	    		result = saveHours(extras);
	    	} else if (GrantService.TAG_UPLOAD_TYPE.equals(requestType)) {
	    		result = uploadHours(extras);
	    	} else if (GrantService.TAG_DELETE_TYPE.equals(requestType)) {
	    		result = deleteHours(extras);
	    	} else {
	    		throw new RuntimeException("no such request type: " + requestType);
	    	}
	    	if (data != null) {
	    		result = Activity.RESULT_OK;
	    		msg.setData(data);
	    	}
	    }
	    try {
	    	msg.arg1 = result;
	    	if (messenger != null) {
		    	messenger.send(msg);
	    	}
	    } catch (android.os.RemoteException e1) {
	    	Log.w(getClass().getName(), "Exception sending message", e1);
	    }
	}
	
	// handle upload intent, upload hours to the server
	private int uploadHours(Bundle extras) {
		Log.i(TAG, "saving hours");
		GrantData        data = (GrantData) extras.getSerializable(GrantService.TAG_REQUEST_DETAILS);
		String[] grantStrings =             extras.getStringArray (GrantService.TAG_REQUEST_GRANTS);
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
	
	// handle save hours intent, save entries into the database
	private int saveHours(Bundle extras) {
		Log.i(TAG, "saving hours");
		GrantData     data = (GrantData) extras.getSerializable(GrantService.TAG_REQUEST_DETAILS);
		Bundle hourBundle  = extras.getBundle("hours");
		return saveHours(data, hourBundle);
	}
	public int saveHours(GrantData data, Bundle hourBundle) {
		for (String key: hourBundle.keySet()) {
			db.saveEntry(data, key, hourBundle.getDoubleArray(key));
		}
		return Activity.RESULT_OK;
	}
	
	// handle delete hours intent, remove entries from the database
	private int deleteHours(Bundle extras) {
		Log.i(TAG, "deleting hours");
		GrantData        data = (GrantData) extras.getSerializable(GrantService.TAG_REQUEST_DETAILS);
		String[] grantStrings =             extras.getStringArray (GrantService.TAG_REQUEST_GRANTS);
		return deleteHours(data, grantStrings);
	}
	public int deleteHours(GrantData data, String[] grantStrings) {
		int deletedCount = db.deleteEntries(data, grantStrings);
		return deletedCount == grantStrings.length ? Activity.RESULT_OK : Activity.RESULT_CANCELED;
	}

	// retrieve hours from database/server using a viewrequest command
	// viewrequest returns the same data as the "hours" portion of an email command but it can return
	// hours for multiple grants, and works for unsent grant requests
	private Bundle getViewRequest(Bundle extras) {
		Log.i(TAG, "handling viewrequest");
		GrantData        data = (GrantData) extras.getSerializable(GrantService.TAG_REQUEST_DETAILS);
		String[] grantStrings =             extras.getStringArray (GrantService.TAG_REQUEST_GRANTS);
		return getViewRequest(data, grantStrings);
	}
	public Bundle getViewRequest(GrantData data, String[] grantStrings) {
		// see if the db has our entries
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
		Bundle result = new Bundle();
		for (String s: grantStrings) {
			result.putDoubleArray(s, allHours.get(s));
		}
		return result;
	}
		
	/** the following static methods fill out an Intent with the appropriate extras for a particular command
	 *  they're not necessary in any way, just slightly more convenient
	 */
	
	// retrieve hours from database/server, and save any download hours to database
	// to get updated hours from the server, you need to call deleteHours() first - there's no method to
	// do both yet, sorry
	public static ComponentName getHours(Context context, Handler callback, GrantData details, String[] grants) {
	    Intent intent = getCommonIntent(context, callback, details, TAG_VIEWREQUEST_TYPE);
	    intent.putExtra(TAG_REQUEST_GRANTS, grants);
	    return context.startService(intent);
	}
	
	/**
	 * Save hours into database (only!)
	 * @param hours String -> double[] map of hours
	 */
	public static ComponentName saveHours(Context context, Handler callback, GrantData details, Bundle hours) {
	    Intent intent = getCommonIntent(context, callback, details, TAG_SAVEREQUEST_TYPE);
	    intent.putExtra("hours", hours);
	    return context.startService(intent);
	}
	
	// upload hours from database to server (they must be saved to the database first)
	public static ComponentName uploadHours(Context context, Handler callback, GrantData details, String[] grants) {
	    Intent intent = getCommonIntent(context, callback, details, TAG_UPLOAD_TYPE);
	    intent.putExtra(TAG_REQUEST_GRANTS, grants);
	    return context.startService(intent);
	}
	
	// delete hours from database
	public static ComponentName deleteHours(Context context, Handler callback, GrantData details, String[] grants) {
	    Intent intent = getCommonIntent(context, callback, details, TAG_DELETE_TYPE);
	    intent.putExtra(TAG_REQUEST_GRANTS, grants);
	    return context.startService(intent);
	}
	
	// helper method, set some details used in common by the above methods
	private static Intent getCommonIntent(Context context, Handler callback, GrantData details, String requestType) {
	    Intent intent = new Intent(context, GrantService.class);
	    intent.putExtra(TAG_REQUEST_DETAILS, details);
		intent.putExtra(TAG_REQUEST_TYPE, requestType);
		if (callback != null)
			intent.putExtra("MESSENGER", new Messenger(callback));
		return intent;
	}
	
	public class GrantBinder extends Binder {
		GrantService getService() {
			return GrantService.this;
		}
	}
	
	// helper class to make lint stop whining about leaked references
	// extend as MyRefHandler<MyActivity> and create with new MyRefHandler().setParent(this)
	public static abstract class WeakrefHandler<T> extends Handler {
		WeakReference<T> parent;
		public WeakrefHandler<T> setParent(T parent) {
			this.parent = new WeakReference<T>(parent);
			return this;
		}
	}
	
	// simple handler to pop up a Toast on success/failure
	public static class ToastHandler extends WeakrefHandler<Context> {
		String successMessage, failureMessage;
		public ToastHandler(String successMessage, String failureMessage) {
			this.successMessage = successMessage;
			this.failureMessage = failureMessage;
		}
		public void handleMessage(Message msg) {
			String resultText = msg.arg1 == Activity.RESULT_OK ? successMessage : failureMessage;
			Toast.makeText(parent.get(), resultText, Toast.LENGTH_LONG).show();
		}
	}
	
	// class to hold the time/employee details needed to specify a grant's hours
	// supposed to be the basis for a HashMap key-value store, but that didn't work out
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

	public void sendEmailRequest(JSONResultHandler jsonResultHandler) {
		new JSONParser.RequestBuilder()
		.setUrl(requestURL)
		.addParam("q", "email")
		.addParam("id", String.valueOf(requestId))
		.makeRequest(jsonResultHandler);
	}
	
}