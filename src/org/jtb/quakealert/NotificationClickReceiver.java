package org.jtb.quakealert;

import java.util.Collections;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationClickReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Collections.sort(RefreshService.matchQuakes, new Quake.ListComparator());
	
		Intent listIntent = new Intent(context, ListQuakesActivity.class);
		listIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(listIntent);
	}

}
