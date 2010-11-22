package org.jtb.quakealert;

import android.app.AlarmManager;
import android.content.Context;

enum Interval {
	DAY(AlarmManager.INTERVAL_DAY, 4),
	HALF_DAY(AlarmManager.INTERVAL_HALF_DAY, 3),
	HOUR(AlarmManager.INTERVAL_HOUR, 2),
	HALF_HOUR(AlarmManager.INTERVAL_HALF_HOUR, 1),
	FIFTEEN_MINUTES(AlarmManager.INTERVAL_FIFTEEN_MINUTES, 0);
	
	private long value;
	private int position;
	
	private Interval(long value, int position) {
		this.value = value;
		this.position = position;
	}
	
	public long getValue() {
		return value;
	}
	
	public String getTitle(Context context) {
		return context.getResources().getStringArray(R.array.interval_entries)[position];
	}
}
