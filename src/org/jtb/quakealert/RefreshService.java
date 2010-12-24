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
	private static int MAX_QUAKES = 100;
	private static final Quakes quakes = new Quakes();
	static Location location = null;
	static ArrayList<Quake> matchQuakes = new ArrayList<Quake>();
	static int newCount;

	private void refresh() {
		Log.d("quakealert", "refreshing");
		try {
			QuakePrefs quakePrefs = new QuakePrefs(this);
			sendBroadcast(new Intent("showRefreshDialog"));
			setLocation(this, quakePrefs);

			int range = quakePrefs.getRange();
			if (location == null && range != -1) {
				Log.d("quakealert", "location unknown, showing all");
				quakePrefs.setRange(-1);
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

			matchQuakes = getQuakeMatches(this, quakeList);
			if (matchQuakes == null) {
				Log.d("quakealert", "no matches");
				return;
			}

			int mqsSize = matchQuakes.size();
			Log.d("quakealert", mqsSize + " matches");

			Collections.sort(matchQuakes, new Quake.ListComparator());
			if (mqsSize > MAX_QUAKES) {
				matchQuakes = new ArrayList<Quake>(matchQuakes.subList(0,
						MAX_QUAKES));
			}

			if (newCount > 0 && quakePrefs.isNotificationsEnabled()) {
				QuakeNotifier quakeNotifier = new QuakeNotifier(this);
				quakeNotifier.alert();
			}

			sendBroadcast(new Intent("updateList"));
		} finally {
			sendBroadcast(new Intent("dismissRefreshDialog"));
			Lock.release();
		}
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

	private static void setLocation(Context context, QuakePrefs quakePrefs) {
		LocationManager lm = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		String name = lm.getBestProvider(new Criteria(), true);
		if (name == null) {
			Log.e("quakealert", "no best location provider returned");
			location = null;
			return;
		}
		location = lm.getLastKnownLocation(name);
	}

	private static ArrayList<Quake> getQuakeMatches(Context context,
			ArrayList<Quake> quakeList) {
		QuakePrefs prefs = new QuakePrefs(context);

		if (quakeList == null || quakeList.size() == 0) {
			return null;
		}

		int range = prefs.getRange();
		float magnitude = prefs.getMagnitude().getValue();
		if (location == null && range != -1) {
			return null;
		}

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
}
