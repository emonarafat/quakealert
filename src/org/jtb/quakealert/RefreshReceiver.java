package org.jtb.quakealert;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

public class RefreshReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("quakealert", "refresh receiver, action: " + intent.getAction());

		if (intent.getAction().equals("location")) {
			Lock.acquire(context);
			Intent broadcastIntent = new Intent("refresh", null, context,
					RefreshReceiver.class);
			Intent locationIntent = new Intent(context, LocationService.class);
			locationIntent.putExtra("timeout", (long)(1000 * 60 * 2)); // 2 minutes
			locationIntent.putExtra("broadcastIntent", broadcastIntent);
			context.startService(locationIntent);
		} else if (intent.getAction().equals("refresh")) {
			context.startService(new Intent(context,
					RefreshService.class));			
		} else if (intent.getAction().equals("schedule")) {
			schedule(context);
		} else if (intent.getAction().equals("cancel")) {
			cancel(context);
		}
	}

	private void schedule(Context context) {
		AlarmManager mgr = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent("location", null, context,
				RefreshReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i,
				PendingIntent.FLAG_UPDATE_CURRENT);
		mgr.cancel(pi);
		Prefs prefs = new Prefs(context);
		//mgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
		//		SystemClock.elapsedRealtime() + prefs.getInterval().getValue(),
		//		prefs.getInterval().getValue(), pi);
		mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
		 SystemClock.elapsedRealtime(), 60000, pi);
	}

	private void cancel(Context context) {
		AlarmManager mgr = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent("location", null, context,
				RefreshReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i,
				PendingIntent.FLAG_CANCEL_CURRENT);
		mgr.cancel(pi);
	}
}
