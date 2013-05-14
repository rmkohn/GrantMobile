package com.example.grantmobile;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;

public class QuittableActivity extends FragmentActivity {
	public static final int RESULT_QUITTING = Activity.RESULT_FIRST_USER + 1;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_QUITTING) {
			quitApp();
		}
	}

	@Override
	public void startActivity(Intent intent) {
		super.startActivityForResult(intent, 0);
	}
	
	public void quitApp() {
		setResult(RESULT_QUITTING);
		finish();
	}
}
