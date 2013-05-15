package com.example.grantmobile;

import com.example.grantmobile.GrantService.GrantBinder;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public abstract class GrantServiceBindingActivity extends QuittableActivity {
	private GrantService service;
	public GrantService getService() {
		return service;
	}
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		bind();
	}
	@Override
	protected void onStop() {
		Log.w("grantservice binder", "activity stopped");
		super.onStop();
		unbind();
	}
	
	@Override
	protected void onDestroy() {
		Log.w("grantservice binder", "activity destroyed");
		super.onDestroy();
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
			onUnbound();
			// keep service around a little longer (it shuts itself down after 5 seconds)
			Intent waitIntent = new Intent(this, GrantService.class);
			startService(waitIntent);
			// now unbind it
			unbindService(conn);
			service = null;
		}
	}
	
	protected boolean isServiceBound() {
		return service != null;
	}
	
	/**
	 * This method is called as soon as GrantService is bound.
	 * Override it if your activity uses GrantService when it starts up.
	 * <br/>The default implementation does nothing.
	 */
	protected void onBound() { }
	/**
	 * This method is called immediately before unbinding GrantService.
	 * If your activity needs to save data into GrantService before it closes,
	 * this is the place to do it.
	 * <br/>The default implementation does nothing.
	 */
	protected void onUnbound() { }
	
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
