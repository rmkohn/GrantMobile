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
	
	public static final String TAG_SUCCESS = "success";  // "true" is good
	public static final String TAG_MESSAGE = "message";

	public static final String TAG_HOURS = "hours"; 
	public static final String TAG_GRANT = "grant";
	public static final String TAG_NON_GRANT = "non-grant";
	public static final String TAG_LEAVE = "leave";
	
	public static final String TAG_MONTH = "month";
	public static final String TAG_YEAR = "year";
	
	public static final String TAG_EMPLOYEE = "employee";
	public static final String TAG_FIRST_NAME = "firstname";
	public static final String TAG_LAST_NAME = "lastname";
	public static final String TAG_EMPLOYEE_ID = "id";
	
	public static final String TAG_GRANT_ID = "ID";
	public static final String TAG_STATE_CATALOG_NUM = "stateCatalogNum";
	public static final String TAG_GRANT_NUMBER = "grantNumber";
	public static final String TAG_GRANT_TITLE = "grantTitle";
	
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
	
	
	
	
	JSONParser jParser;
	ArrayList<String> grantHours; // grant hour array 
	ArrayList<String> nonGrantHours; // non-grant hour array
	ArrayList<String> leaveHours;  // leave hour array
	HashMap<String,String> map;
	JSONObject json = null; // entire json object
	DBAdapter db;
	GrantBinder binder;

	
	public GrantService() {
		super("GrantService");
		jParser = new JSONParser();
		grantHours = new ArrayList<String>(); // grant hour array 
		nonGrantHours = new ArrayList<String>(); // non-grant hour array
		leaveHours = new ArrayList<String>();  // leave hour array
		map = new HashMap<String,String>();
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.i(TAG, "handling intent");
	    Bundle extras = intent.getExtras();
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
	    		data = getEmailRequest(extras);
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
	
	private int uploadHours(Bundle extras) {
		Log.i(TAG, "saving hours");
		GrantData        data = (GrantData) extras.getSerializable(GrantService.TAG_REQUEST_DETAILS);
		String[] grantStrings =             extras.getStringArray (GrantService.TAG_REQUEST_GRANTS);
		DBAdapter db = getDB();
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
	
	private int saveHours(Bundle extras) {
		Log.i(TAG, "saving hours");
		GrantData     data = (GrantData) extras.getSerializable(GrantService.TAG_REQUEST_DETAILS);
		Bundle hourBundle  = extras.getBundle("hours");
		DBAdapter db = getDB();
		for (String key: hourBundle.keySet()) {
			db.saveEntry(data, key, hourBundle.getDoubleArray(key));
		}
		return Activity.RESULT_OK;
	}
	
	private int deleteHours(Bundle extras) {
		Log.i(TAG, "deleting hours");
		GrantData        data = (GrantData) extras.getSerializable(GrantService.TAG_REQUEST_DETAILS);
		String[] grantStrings =             extras.getStringArray (GrantService.TAG_REQUEST_GRANTS);
		DBAdapter db = getDB();
		int deletedCount = db.deleteEntries(data, grantStrings);
		return deletedCount == grantStrings.length ? Activity.RESULT_OK : Activity.RESULT_CANCELED;
	}

	private Bundle getViewRequest(Bundle extras) {
		Log.i(TAG, "handling viewrequest");
		GrantData        data = (GrantData) extras.getSerializable(GrantService.TAG_REQUEST_DETAILS);
		String[] grantStrings =             extras.getStringArray (GrantService.TAG_REQUEST_GRANTS);
		DBAdapter db = getDB();
		
		Map<String, double[]> allHours = db.getTimes(data, grantStrings);
		
		if (allHours.size() < grantStrings.length) {
			Set<String> missingKeys = new HashSet<String>(Arrays.asList(grantStrings));
			missingKeys.removeAll(allHours.keySet());
//			missingKeys.remove("non-grant");
//			missingKeys.remove("leave");
			Log.i(TAG, "retrieving values");
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("q", "viewrequest"));
			params.add(new BasicNameValuePair("employee", String.valueOf(data.employeeid)));
			params.add(new BasicNameValuePair("year", String.valueOf(data.year)));
			params.add(new BasicNameValuePair("month", String.valueOf(data.month)));
			params.add(new BasicNameValuePair("grant", DBAdapter.mkString(missingKeys, ",", "", "")));
			Log.i(TAG, "making request with params: " + params);
			json = JSONParser.makeHttpRequest(requestURL, "GET", params);
			try {
				if (json.getBoolean("success")) {
					JSONObject message = json.getJSONObject("message");
					@SuppressWarnings("unchecked")
					Iterator<String> keys = message.keys();
					while(keys.hasNext()) {
						String key = keys.next();
						JSONArray jsonHours = message.getJSONArray(key);
						double[] hours = new double[jsonHours.length()];
						for (int i = 0; i < hours.length; i++) {
							hours[i] = jsonHours.getDouble(i);
						}
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
		
	private Bundle getEmailRequest(Bundle extras) {
		JSONArray jsa = null; // json array
		JSONObject jso, jsom, jsoh = null; // misc., message & hours object
		requestId = extras.getString(DetailViewActivity.TAG_REQUEST_ID);
		Log.d(TAG+"_REQ_ID", "requestId = " + requestId);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("q","email"));
		params.add(new BasicNameValuePair("id",requestId)); // this will come from intent
		json = JSONParser.makeHttpRequest(requestURL, "GET", params);
		Log.d(TAG+"_GET_RET", json.toString());
		try {
			String s = json.getString(TAG_SUCCESS);
			if (s.equals("true")) {
				Log.d(TAG+"_processing...", "success = " + s);
				Bundle returnData = new Bundle(); // going to return a "Bundle"
				jsom = json.getJSONObject(TAG_MESSAGE);
				jsoh = jsom.getJSONObject(TAG_HOURS);
				jsa = jsoh.getJSONArray(TAG_GRANT);
				for (int i = 0; i < jsa.length(); i++) {
					grantHours.add((Integer.valueOf(jsa.getInt(i))).toString());  // these are the grant hours for each day of month
				}
				returnData.putStringArrayList(TAG_GRANT,grantHours);
				jsa = jsoh.getJSONArray(TAG_NON_GRANT);
				for (int i = 0; i < jsa.length(); i++){
					nonGrantHours.add((Integer.valueOf(jsa.getInt(i))).toString());  // these are the non-grant hours for each day of month
				}
				returnData.putStringArrayList(TAG_NON_GRANT,nonGrantHours);
				jsa = jsoh.getJSONArray(TAG_LEAVE);
				for (int i = 0; i < jsa.length(); i++){
					leaveHours.add((Integer.valueOf(jsa.getInt(i))).toString());  // these are the leave hours for each day of month
				}
				returnData.putStringArrayList(TAG_LEAVE,leaveHours);
				returnData.putString(TAG_MONTH,(Integer.valueOf(jsom.getInt(TAG_MONTH))).toString());
				returnData.putString(TAG_YEAR,(Integer.valueOf(jsom.getInt(TAG_YEAR))).toString());
				jso = jsom.getJSONObject(TAG_EMPLOYEE);
				returnData.putString(TAG_FIRST_NAME, jso.getString(TAG_FIRST_NAME));
				returnData.putString(TAG_LAST_NAME, jso.getString(TAG_LAST_NAME));
				Log.d(TAG+"_GET_REQ", "last name = " + jso.getString(TAG_LAST_NAME));
				returnData.putString(TAG_EMPLOYEE_ID, jso.getString(TAG_EMPLOYEE_ID));
				jso = jsom.getJSONObject(TAG_GRANT);	
				returnData.putString(TAG_GRANT_ID, jso.getString(TAG_GRANT_ID));
				returnData.putString(TAG_GRANT_NUMBER, jso.getString(TAG_GRANT_NUMBER));
				returnData.putString(TAG_GRANT_TITLE, jso.getString(TAG_GRANT_TITLE));
				returnData.putString(TAG_STATE_CATALOG_NUM, jso.getString(TAG_STATE_CATALOG_NUM));	
				// now return via extras the grant and hour data...	
				return returnData;
			} else {
				return null;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private DBAdapter getDB() {
		return db;
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

	public static ComponentName getHours(Context context, Handler callback, GrantData details, String[] grants) {
	    Intent intent = getCommonIntent(context, callback, details, TAG_VIEWREQUEST_TYPE);
	    intent.putExtra(TAG_REQUEST_GRANTS, grants);
	    return context.startService(intent);
	}
	
	public static ComponentName saveHours(Context context, Handler callback, GrantData details, Bundle hours) {
	    Intent intent = getCommonIntent(context, callback, details, TAG_SAVEREQUEST_TYPE);
	    intent.putExtra("hours", hours);
	    return context.startService(intent);
	}
	
	public static ComponentName uploadHours(Context context, Handler callback, GrantData details, String[] grants) {
	    Intent intent = getCommonIntent(context, callback, details, TAG_UPLOAD_TYPE);
	    intent.putExtra(TAG_REQUEST_GRANTS, grants);
	    return context.startService(intent);
	}
	
	public static ComponentName deleteHours(Context context, Handler callback, GrantData details, String[] grants) {
	    Intent intent = getCommonIntent(context, callback, details, TAG_DELETE_TYPE);
	    intent.putExtra(TAG_REQUEST_GRANTS, grants);
	    return context.startService(intent);
	}
	
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
	
	public static abstract class WeakrefHandler<T> extends Handler {
		WeakReference<T> parent;
		public WeakrefHandler<T> setParent(T parent) {
			this.parent = new WeakReference<T>(parent);
			return this;
		}
	}
	
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
	
}