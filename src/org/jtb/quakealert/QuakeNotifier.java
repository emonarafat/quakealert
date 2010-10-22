package org.jtb.quakealert;

import java.util.Date;
import java.util.HashSet;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;

public class QuakeNotifier {
	private static final int ALERT_ID = 0;

	private Context context;
	private QuakePrefs quakePrefs;

	public QuakeNotifier(Context context) {
		this.context = context;
		quakePrefs = new QuakePrefs(context);
	}

	public void alert() {
		HashSet<String> newIds = quakePrefs.getNewIds();
		int size = newIds.size();
		if (size == 0) {
			return;
		}

		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		int icon = android.R.drawable.stat_sys_warning;
		CharSequence tickerText = "Quake Alert!";
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		if (quakePrefs.isNotificationAlert()) {
			notification.sound = quakePrefs.getNotificationAlertSound();
		}
		if (quakePrefs.isNotificationFlash()) {
			notification.flags |= Notification.FLAG_SHOW_LIGHTS;
			notification.ledOffMS = 250;
			notification.ledOnMS = 500;
			notification.ledARGB = Color.parseColor("#ff0000");
		}
		if (quakePrefs.isNotificationVibrate()) {
			notification.vibrate = new long[] { 100, 100, 100, 100, 100, 100,
					100, 100 };
		}
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		if (size > 1) {
			notification.number = size;
		}

		CharSequence contentTitle = "Quake Alert!";
		CharSequence contentText = size + " new M" + quakePrefs.getMagnitude()
				+ "+ quakes";
		if (quakePrefs.getRange() > 0) {
			Distance d = new Distance(quakePrefs.getRange());
			contentText = contentText + " within " + d.toString(quakePrefs);
		}

		Intent notificationIntent = new Intent(context,
				NotificationClickReceiver.class);
		PendingIntent contentIntent = PendingIntent.getBroadcast(context, 0,
				notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);
		nm.notify(ALERT_ID, notification);
		Log.d("quakealert", "notification sent, " + size + " new");
	}
}
