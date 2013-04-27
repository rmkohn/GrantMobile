package com.example.grantmobile;

import com.example.grantmobile.GrantService.GrantBinder;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public abstract class GrantServiceBindingActivity extends FragmentActivity {
	private GrantService service;
	public GrantService getService() {
		return service;
	}
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		bind();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
	}
	@Override
	protected void onStop() {
		Log.w("grantservice binder", "activity stopped");
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		Log.w("grantservice binder", "activity destroyed");
		super.onDestroy();
//		GrantService temp = service;
		unbind();
//		if (isFinishing()) {
//			Log.w("grantservice binder", "finish() called, stopping the service");
//			// stopService() stops the service unconditionally, but stopSelf() is okay
//			temp.stopSelf();
//		}
	}
	
	private void bind() {
		if (service == null) {
			Intent intent = new Intent(this, GrantService.class);
			bindService(intent, conn, Context.BIND_AUTO_CREATE);
//			bindService(intent, conn, 0);
//			startService(new Intent(this, GrantService.class));
		}
	}
	
	private void unbind() {
		if (service != null) {
			unbindService(conn);
			service = null;
		}
	}
	
	protected boolean isServiceBound() {
		return service != null;
	}
	
	// override me
	protected void onBound() { }
	
	// callback for bindService
	private final ServiceConnection conn = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.e("grantservice binder", "unintended disconnect?!");
			service = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
			service = ((GrantBinder)serviceBinder).getService();
			onBound();
		}
	};

}