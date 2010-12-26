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
		super.onStart(intent, startId);
		handleCommand(intent, startId);
	}

	private void handleCommand(Intent intent, final int startId) {
		final Intent broadcastIntent = intent
				.getParcelableExtra("broadcastIntent");

		Prefs prefs = new Prefs(this);
		// if we're not using location, just send the
		// broadcast intent
		if (!prefs.isUseLocation()) {
			Log.d("quakealert", "location service, not using location");
			sendBroadcast(broadcastIntent);
			return;
		}

		final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Location l = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if (l != null) {
			// we have a last known location, go ahead and send
			// the passed broadcast intent
			Log.d("quakealert", "location service, got last known location: " + l);
			sendBroadcast(broadcastIntent);
			return;

		}

		// we don't have a last known location, wait for the current location
		Log.d("quakealert", "location service, could not get last known location");
		long timeout = intent.getLongExtra("timeout", 1000 * 60 * 2);
		final Timer timer = new Timer();

		final LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				Log.d("quakealert", "location service, got location changed: "
						+ location);
				lm.removeUpdates(this);
				timer.cancel();
				sendBroadcast(broadcastIntent);
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

		lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
				locationListener);

		// create a timer to abort waiting for the location after the
		// passed timeout
		TimerTask tt = new TimerTask() {

			@Override
			public void run() {
				Log.d("quakealert",
						"location serivce, did not get location changed");
				lm.removeUpdates(locationListener);
				sendBroadcast(broadcastIntent);
				stopSelfResult(startId);
			}

		};
		timer.schedule(tt, timeout); // 2 minutes
	}

}
