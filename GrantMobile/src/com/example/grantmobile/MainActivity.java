package com.example.grantmobile;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity 
{
	TextView tvTest; 
		
		
		
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Uri data 					= getIntent().getData();
		
		tvTest 						= (TextView) findViewById(R.id.tvTest); 
		
		tvTest.setText("Uri data: " + data);//FOR TESTING PURPOSES ONLY.  D E L E T E BEFORE GOING INTO PRODUCTION
		
		// special handling for old-style email links
		if (data != null && data.getPath().contains("aspx")) {
			new JSONParser.RequestBuilder()
			.setUrl(GrantService.requestURL)
			.addParam("grant", data.getQueryParameter("GrantID"))
			.addParam("employee", data.getQueryParameter("Employee"))
			.addParam("month", data.getQueryParameter("month"))
			.addParam("year", data.getQueryParameter("Year"))
			.addParam("supervisor", data.getQueryParameter("ID"))
			.addParam("q", "email")
			.makeRequest(new JSONParser.SimpleResultHandler() {
				public void onSuccess(Object result) throws JSONException {
					useId(((JSONObject)result).getInt("id"));
				}
			});
		} else {
			useId(getWorkMonthId(data));
		}
	}
		
	private void useId(int workMonthId) {
		
		tvTest.setText("ID = " 		+ workMonthId);//FOR TESTING PURPOSES ONLY.  D E L E T E BEFORE GOING INTO PRODUCTION
		
		/******************************************************************************************************
		 * CHECKS TO SEE IF THE APP WAS OPENED BY CLICKING THE APP ICON OR BY THE EMAIL LINK.                 *
		 *                                                                                                    *
		 ******************************************************************************************************/
		if (workMonthId == -1)
		{
		    Intent intent = new Intent(this, LoginActivity.class);
		    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    startActivity(intent);
		}//ENDIF
		
		else
		{
			Intent intent = new Intent(this, CalendarActivity.class);
			intent.putExtra("workMonthId", workMonthId);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}//ENDELSE
		
	}//END ONCREATE
		
	/**********************************************************************************************************
	 * CHECKS TO SEE IF THE APP WAS OPENED BY CLICKING THE APP ICON OR BY THE EMAIL LINK.  IF DATA IS NULL, IT*
	 * RETURNS A -1 THAT SIGNIFIES THAT THE APP HAS BEEN OPENED BY CLICKING ON THE APP. IF DATA IS NOT NULL,  *
	 * IT RETURNS THE WORKMONTHID NUMBER THAT WILL BE NEEDED IN CALENDAR ACTIVITY.                            *
	 *                                                                                                        *
	 * @param data                                                                                            *
	 * @return                                                                                                *
	 *                                                                                                        *
	 **********************************************************************************************************/
		
	private int getWorkMonthId(Uri data)
	{
		if (data == null)
		{
			return -1;
		}//ENDIF
		
		String id = data.getQueryParameter("ID");
		
		if (id != null) {
			try {
				return Integer.parseInt(id);
			} catch (NumberFormatException e) { }
		}
		return -1;
	}//END GETWORKMONTHID
		
		
		
	/*@SuppressWarnings("unused")
	private int IntParse(String queryParameter) 
	{
		// TODO Auto-generated method stub
		return 0;
	}//END INTPARSE
*/	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}//END ONCREATEOPTIONSMENU 

}//END MAIN ACTIVITY
