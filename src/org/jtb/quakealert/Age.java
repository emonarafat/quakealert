package org.jtb.quakealert;

import android.content.Context;

public enum Age {
	ONE(1, 0), THREE(3, 1), FIVE(5, 2), SEVEN(7, 3);
	
	private int position;
	private long ms;
	
	private Age(int days, int position) {
		this.ms = 1000 * 60 * 60 * 24 * days;
		this.position = position;
	}

	public String getTitle(Context context) {
		return context.getResources().getStringArray(R.array.maxAge_entries)[position];
	}	
	
	public long toMillis() {
		return ms;
	}
	
}
