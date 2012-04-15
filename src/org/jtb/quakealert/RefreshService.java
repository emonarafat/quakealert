package org.jtb.quakealert;

import java.util.ArrayList;
import java.util.Collections;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class RefreshService extends IntentService {
	private static final Quakes quakes = new Quakes();
	static Location location = null;
	static ArrayList<Quake> matchQuakes = new ArrayList<Quake>();
	static int newCount;

	private void refresh() {
		Log.d("quakealert", "refreshing");
		Prefs quakePrefs = new Prefs(this);
		sendBroadcast(new Intent("showRefreshDialog"));
		setLocation(this, quakePrefs);

		int range = quakePrefs.getRange();
		// if we can't get a location, and the range isn't already set to
		// show all,
		// and we haven't explicitly disabled using the location
		// then set the range to show all
		if (location == null && range != -1 && quakePrefs.isUseLocation()) {
			Log.w("quakealert", "location unknown, showing all");
			sendBroadcast(new Intent("showUnknownLocationMessage"));
		}

		long lastUpdate = quakePrefs.getLastUpdate();
		quakes.update(lastUpdate);
		ArrayList<Quake> quakeList = quakes.get();

		if (quakeList == null) {
			Log.e("quakealert",
					"quake list empty (network error?), aborting refresh");
			sendBroadcast(new Intent("showNetworkErrorDialog"));
			return;
		}
		Log.d("quakealert", "raw quake list size: " + quakeList.size());

		matchQuakes = getQuakeMatches(this, quakeList);
		if (matchQuakes == null) {
			Log.d("quakealert", "no matches");
			return;
		}

		int mqsSize = matchQuakes.size();
		Log.d("quakealert", mqsSize + " matches");

		Collections.sort(matchQuakes, new Quake.ListComparator());

		if (newCount > 0 && quakePrefs.isNotificationsEnabled()) {
			QuakeNotifier quakeNotifier = new QuakeNotifier(this);
			quakeNotifier.alert();
		}

		sendBroadcast(new Intent("updateList"));
	}

	public RefreshService() {
		super("quakeRefreshService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d("quakealert", "refresh service, refreshing ...");
		new Upgrader(this).upgrade();
		refresh();
	}

	private static void setLocation(Context context, Prefs quakePrefs) {
		if (quakePrefs.isUseLocation()) {
			LocationManager lm = (LocationManager) context
					.getSystemService(Context.LOCATION_SERVICE);

			location = null;
			for (String providerName : lm.getProviders(true)) {
				location = lm.getLastKnownLocation(providerName);
				if (location != null) {
					return;
				}
			}

			Log.w("quakealert", "could to find location");
			return;
		} else {
			location = null;
		}
	}

	private static ArrayList<Quake> getQuakeMatches(Context context,
			ArrayList<Quake> quakeList) {
		Prefs prefs = new Prefs(context);

		if (quakeList == null || quakeList.size() == 0) {
			return null;
		}

		int range = prefs.getRange();
		float magnitude = prefs.getMagnitude().getValue();

		ArrayList<Quake> matchQuakes = new ArrayList<Quake>();
		newCount = 0;

		int quakeListSize = quakeList.size();
		Age age = prefs.getMaxAge();
		for (int i = 0; i < quakeListSize; i++) {
			Quake quake = quakeList.get(i);
			if (quake.matches(magnitude, range, location, age)) {
				matchQuakes.add(quake);
				if (quake.isNewQuake()) {
					newCount++;
				}
			}
		}

		return matchQuakes;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		sendBroadcast(new Intent("dismissRefreshDialog"));
		Lock.release();
	}
}
