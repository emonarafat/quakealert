package org.jtb.quakealert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.PowerManager;
import android.util.Log;

public class QuakeRefreshService extends IntentService {
	private static int MAX_QUAKES = 1000;
	private static final Quakes quakes = new Quakes();

	static ArrayList<Quake> matchQuakes = new ArrayList<Quake>();

	class RefreshRunner implements Runnable {
		private Context context;

		public RefreshRunner(Context context) {
			this.context = context;
		}

		public void run() {
			Log.d(getClass().getSimpleName(), "refreshing");
			try {
				context.sendBroadcast(new Intent("showRefreshDialog"));

				quakes.update();
				ArrayList<Quake> quakeList = quakes.get();
				if (quakeList == null) {
					context.sendBroadcast(new Intent("dismissRefreshDialog"));
					return;
				}

				QuakePrefs quakePrefs = new QuakePrefs(context);
				HashSet<String> newIds = quakePrefs.getNewIds();
				HashSet<String> matchIds = quakePrefs.getMatchIds();

				matchQuakes = getQuakeMatches(context, quakeList);
				int mqsSize = matchQuakes.size();
				Log.d(getClass().getSimpleName(), "found " + mqsSize
						+ " matches");

				for (int i = 0; i < mqsSize; i++) {
					Quake q = matchQuakes.get(i);
					if (!matchIds.contains(q.getId())) {
						newIds.add(q.getId());
					}
				}
				Collections.sort(matchQuakes, new Quake.ListComparator(newIds));
				if (mqsSize > MAX_QUAKES) {
					matchQuakes = new ArrayList<Quake>(matchQuakes.subList(0,
							MAX_QUAKES));
				}

				quakePrefs.setMatchIds(matchQuakes);
				newIds.retainAll(quakePrefs.getMatchIds());
				Log.d(getClass().getSimpleName(), newIds.size() + " new");
				quakePrefs.setNewIds(newIds);

				context.sendBroadcast(new Intent("updateList"));

				if (quakePrefs.isNotificationsEnabled()) {
					QuakeNotifier quakeNotifier = new QuakeNotifier(context);
					quakeNotifier.alert();
				}
				context.sendBroadcast(new Intent("dismissRefreshDialog"));
			} finally {
				context.sendBroadcast(new Intent("release", null, context,
						WakeLockReceiver.class));
			}
		}
	}

	public QuakeRefreshService() {
		super("quakeRefreshService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(getClass().getSimpleName(), "received intent, action: "
				+ intent.getAction());

		if (intent.getAction().equals("refresh")) {
			new Thread(new RefreshRunner(this)).start();
		}
	}

	private static Location getLocation(Context context) {
		LocationManager lm = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		String name = lm.getBestProvider(new Criteria(), true);
		if (name == null) {
			// TODO: error dialog an exit (this.finish())?
			Log.e(QuakeRefreshService.class.getSimpleName(),
					"no best location provider returned");
		}
		// LocationProvider lp = lm.getProvider(name);
		Location l = lm.getLastKnownLocation(name);

		return l;
	}

	private static ArrayList<Quake> getQuakeMatches(Context context,
			ArrayList<Quake> quakeList) {
		QuakePrefs prefs = new QuakePrefs(context);

		if (quakeList == null || quakeList.size() == 0) {
			return null;
		}

		int range = prefs.getRange();
		float magnitude = prefs.getMagnitude();
		Location location = getLocation(context);

		ArrayList<Quake> matchQuakes = new ArrayList<Quake>();
		int quakeListSize = quakeList.size();
		for (int i = 0; i < quakeListSize; i++) {
			Quake quake = quakeList.get(i);
			if (quake.matches(magnitude, range, location)) {
				matchQuakes.add(quake);
			}
		}

		return matchQuakes;
	}
}
