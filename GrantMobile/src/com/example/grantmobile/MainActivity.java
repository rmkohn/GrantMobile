package com.example.grantmobile;

import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends QuittableActivity 
{
	TextView tvTest; 
		
		
		
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Uri data 					= getIntent().getData();
		
		if (checkQuitting(getIntent())) {
			return;
		}
		
		tvTest 						= (TextView) findViewById(R.id.tvTest); 
		
		tvTest.setText("Uri data: " + data);//FOR TESTING PURPOSES ONLY.  D E L E T E BEFORE GOING INTO PRODUCTION
		
		/******************************************************************************************************
		 * CHECKS TO SEE IF THE APP WAS OPENED BY CLICKING THE APP ICON OR BY THE EMAIL LINK.                 *
		 *                                                                                                    *
		 ******************************************************************************************************/
		
	}//END ONCREATE
	
	@Override
	protected void onStart() {
		super.onStart();
		if (isFinishing())
			return;
		if (getIntent().getData() == null)
		{
		    Intent intent = new Intent(this, LoginActivity.class);
//		    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    startActivity(intent);
		}//ENDIF
		
		else
		{
			Intent intent = new Intent(this, CalendarActivity.class);
			intent.putExtra("launchUri", getIntent().getData());
			intent.addFlags( Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}//ENDELSE
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		finish();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		checkQuitting(intent);
	}
	
	private boolean checkQuitting(Intent intent) {
		if (intent.getExtras() != null && intent.getExtras().getBoolean("quit", false)) {
			Log.i("mainactivitiy", "quitting");
			finish();
			return true;
		}
		Log.i("mainactivitiy", "not quitting");
		return false;
	}

}//END MAIN ACTIVITY
