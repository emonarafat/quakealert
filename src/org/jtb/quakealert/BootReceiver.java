package org.jtb.quakealert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		QuakePrefs prefs = new QuakePrefs(context);
		if (!prefs.isNotificationsEnabled()) {
			return;
		}
		if (!prefs.isBootStart()) {
			return;
		}
		context.sendBroadcast(new Intent("schedule", null, context, RefreshReceiver.class));
	}
}
