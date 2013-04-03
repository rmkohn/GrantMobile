package com.example.grantmobile;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {

	TextView tvTest; 
	String testArray[] ; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Uri data 				= getIntent().getData();
	/*	@SuppressWarnings("unused")
		String scheme	 		= data.getScheme(); // "grantapp"
		String host 			= data.getHost(); // "mid-state"
		List<String> params 	= data.getPathSegments();
		String first 			= params.get(0); // "999"
		//String second 			= params.get(1); // "instructor"
		*/
		tvTest 					= (TextView) findViewById(R.id.tvTest); 
		tvTest.setText("Uri data: " + data);
		//Toast.makeText(this, "Uri data: " + data, Toast.LENGTH_LONG).show();
		
		int workMonthId = getWorkMonthId(data);
		
		/*Map<String, String> paramsMap = getParameters(data); 
		
		String results ="";
		
		for(Map.Entry<String, String> entry:paramsMap.entrySet())
		{
			results += entry.getKey() + " is " + entry.getValue() + "\n";
					
		}*/
		
		tvTest.setText("ID = " + workMonthId);
	}
	private int getWorkMonthId(Uri data) {
		
		//return IntParse(data.getQueryParameter("ID"));
		//tvTest.setText(data.getQueryParameter("ID"));
		return Integer.parseInt(data.getQueryParameter("ID"));
		
		
		/*String [] params;
		String afterQuery = data.getQuery();
		params = afterQuery.split("&");
		
		//tvTest.setText("ID = " + params[0]);
		tvTest.setText(Integer.parseInt(params[0].substring(params[0].indexOf('=')+1)));
		return Integer.parseInt(params[0].substring(params[0].indexOf('=')+1));*/
	}
	

	
	/*private Map<String, String> getParameters(Uri data) 
	{
		Map<String, String> paramsMap = new HashMap<String, String>();
		
		
		String [] params;
		String afterQuery = data.getQuery();
		tvTest.setText("After the query: " + afterQuery);
		params = afterQuery.split("&");
		
		paramsMap.put("supervisorId", params[1].substring(params[1].indexOf('=')+1));
		paramsMap.put("employeeId", params[2].substring(params[2].indexOf('=')+1));
		paramsMap.put("month", params[3].substring(params[3].indexOf('=')+1));
		paramsMap.put("year", params[4].substring(params[4].indexOf('=')+1));
		paramsMap.put("grantId", params[5].substring(params[5].indexOf('=')+1));
		
		
		return paramsMap;
		
		
		
		
	}*/
	



	private int IntParse(String queryParameter) {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	} 

}
