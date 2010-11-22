package org.jtb.quakealert;

import android.content.Context;

public enum Units {
	METRIC(0), US(1);
	
	private int position;

	private Units(int position) {
		this.position = position;
	}

	public String getTitle(Context context) {
		return context.getResources().getStringArray(R.array.units_entries)[position];
	}	
}
