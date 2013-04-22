package com.example.grantmobile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
	private int result;
	
	JSONParser jParser;
	ArrayList<String> grantHours; // grant hour array 
	ArrayList<String> nonGrantHours; // non-grant hour array
	ArrayList<String> leaveHours;  // leave hour array
	HashMap<String,String> map;
	JSONObject json = null; // entire json object
	
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
		JSONArray jsa = null; // json array
		JSONObject jso, jsom, jsoh = null; // misc., message & hours object
	    Bundle extras = intent.getExtras();
    	Message msg = Message.obtain();
	    if (extras != null) {
	    	Messenger messenger = (Messenger) extras.get("MESSENGER");
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
			    	msg.setData(returnData);
			    	result = Activity.RESULT_OK;
	    		} else {
	    			result = Activity.RESULT_CANCELED;
	    		}
			    try {
		    		msg.arg1 = result;
			   		messenger.send(msg);
			    } catch (android.os.RemoteException e1) {
			    	Log.w(getClass().getName(), "Exception sending message", e1);
			    }
	    	} catch (JSONException e) {
	    		e.printStackTrace();
	    	}
	    }
	}
}