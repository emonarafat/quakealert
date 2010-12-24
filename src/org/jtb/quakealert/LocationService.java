package org.jtb.quakealert;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class LocationService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onStart(Intent intent, final int startId) {
		handleCommand(intent, startId);
	}

	private void handleCommand(Intent intent, final int startId) {
		super.onStart(intent, startId);

		final Timer timer = new Timer();

		final LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				Log.d("quakealert", "location service, location changed: " + location);
				timer.cancel();
				sendBroadcast(new Intent("refresh", null, LocationService.this,
						RefreshReceiver.class));				
				stopSelfResult(startId);
			}

			public void onProviderDisabled(String provider) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}
		};
		
		final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
				locationListener);
		
		TimerTask tt = new TimerTask() {

			@Override
			public void run() {
				Log.d("quakealert", "location serivce, could not get location, aborting");
				lm.removeUpdates(locationListener);
				sendBroadcast(new Intent("refresh", null, LocationService.this,
						RefreshReceiver.class));				
				stopSelfResult(startId);
			}
			
		};
		timer.schedule(tt, 1000 * 60 * 2); // 2 minutes
	}

}
