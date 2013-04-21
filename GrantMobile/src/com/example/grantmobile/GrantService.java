package com.example.grantmobile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class GrantService extends IntentService {
	
	public static final String TAG_REQUEST_TYPE = "requestType";
	
	private static final String TAG_SUCCESS = "success";  // "true" is good
	private static final String TAG_MESSAGE = "message";

	private static final String TAG_HOURS = "hours"; 
	private static final String TAG_GRANT = "grant";
	private static final String TAG_NON_GRANT = "non-grant";
	private static final String TAG_LEAVE = "leave";
	
	private static final String TAG_MONTH = "month";
	private static final String TAG_YEAR = "year";
	
	private static final String TAG_EMPLOYEE = "employee";
	private static final String TAG_FIRST_NAME = "firstname";
	private static final String TAG_LAST_NAME = "lastname";
	private static final String TAG_EMPLOYEE_ID = "id";
	
	private static final String TAG_GRANT_ID = "ID";
	private static final String TAG_STATE_CATALOG_NUM = "stateCatalogNum";
	private static final String TAG_GRANT_NUMBER = "grantNumber";
	private static final String TAG_GRANT_TITLE = "grantTitle";
	
	private static String requestURL = "http://mid-state.net/mobileclass2/android";
	private static final String TAG = "grantservice";
	public static String requestId;
	private int result;
	
	private static Map<GrantData, Map<String, double[]>> viewRequestCache;
	
	JSONParser jParser;
	ArrayList<String> grantHours; // grant hour array 
	ArrayList<String> nonGrantHours; // non-grant hour array
	ArrayList<String> leaveHours;  // leave hour array
	JSONObject json = null; // entire json object
	
	public GrantService() {
		super("GrantService");
		jParser = new JSONParser();
		grantHours = new ArrayList<String>(); // grant hour array 
		nonGrantHours = new ArrayList<String>(); // non-grant hour array
		leaveHours = new ArrayList<String>();  // leave hour array
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
	    	Bundle data;
	    	if (extras.get(TAG_REQUEST_TYPE).equals(CalendarEditActivity.TAG_VIEWREQUEST_TYPE)) {
	    		data = getViewRequest(extras);
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
	    	messenger.send(msg);
	    } catch (android.os.RemoteException e1) {
	    	Log.w(getClass().getName(), "Exception sending message", e1);
	    }
	}
	
	private Bundle getViewRequest(Bundle extras) {
		Log.i(TAG, "handling viewrequest");
		GrantData     data = (GrantData) extras.getSerializable(CalendarEditActivity.TAG_REQUEST_DETAILS);
		int[]     grantids =             extras.getIntArray    (CalendarEditActivity.TAG_REQUEST_GRANTS);
		Map<String, double[]> allHours = getFromCache(data);
		
		Set<String> grantStrings = new HashSet<String>();
		for (int id: grantids) {
			grantStrings.add(String.valueOf(id));
		}
		grantStrings.add("non-grant");
		grantStrings.add("leave");
		
		if (!allHours.keySet().containsAll(grantStrings)) {
			Log.i(TAG, "retrieving values");
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("q", "viewrequest"));
			params.add(new BasicNameValuePair("withextras", "true"));
			params.add(new BasicNameValuePair("employee", String.valueOf(data.employeeid)));
			params.add(new BasicNameValuePair("year", String.valueOf(data.year)));
			params.add(new BasicNameValuePair("month", String.valueOf(data.month)));
			params.add(new BasicNameValuePair("grant", Arrays.toString(grantids).replaceAll("\\[|\\]| ", "")));
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
		
	private Map<String, double[]> getFromCache(GrantData data) {
		if (viewRequestCache == null)
			viewRequestCache = new HashMap<GrantService.GrantData, Map<String,double[]>>();
		Map<String, double[]> result = viewRequestCache.get(data);
		if (result == null) {
			result = new HashMap<String, double[]>();
			viewRequestCache.put(data, result);
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
	
	public static class GrantData implements Serializable {
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
		public GrantData(int year, int month, int employeeid) {
			this.year = year;
			this.month = month;
			this.employeeid = employeeid;
		}
	}
	
}