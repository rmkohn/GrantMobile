package com.example.grantmobile;

import com.example.grantmobile.GrantService.GrantBinder;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;

public abstract class GrantServiceBindingActivity extends FragmentActivity {
	private GrantService service;
	public GrantService getService() {
		return service;
	}
	@Override
	protected void onStart() {
		super.onStart();
		Intent intent = new Intent(this, GrantService.class);
		bindService(intent, conn, Context.BIND_AUTO_CREATE);
	}
	@Override
	protected void onStop() {
		super.onStop();
		if (service != null) {
			unbindService(conn);
			service = null;
		}
	}
	
	// callback for bindService
	private final ServiceConnection conn = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			service = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
			service = ((GrantBinder)serviceBinder).getService();
		}
	};

}
