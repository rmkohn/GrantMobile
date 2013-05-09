package com.example.grantmobile;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.widget.TextView;

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
		
		/******************************************************************************************************
		 * CHECKS TO SEE IF THE APP WAS OPENED BY CLICKING THE APP ICON OR BY THE EMAIL LINK.                 *
		 *                                                                                                    *
		 ******************************************************************************************************/
		if (getIntent().getData() == null)
		{
		    Intent intent = new Intent(this, LoginActivity.class);
		    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    startActivity(intent);
		}//ENDIF
		
		else
		{
			Intent intent = new Intent(this, CalendarActivity.class);
			intent.putExtra("launchUri", getIntent().getData());
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}//ENDELSE
		
	}//END ONCREATE

}//END MAIN ACTIVITY
