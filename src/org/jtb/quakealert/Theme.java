package org.jtb.quakealert;

import android.content.Context;

public enum Theme {
	LIGHT(R.style.Theme_Light, 1), DEFAULT(R.style.Theme, 0);
	
	private int id;
	private int position;
	
	private Theme(int id, int position) {
		this.id = id;
		this.position = position;
	}
	
	public int getId() {
		return id;
	}
	
	public String getTitle(Context context) {
		return context.getResources().getStringArray(R.array.theme_entries)[position];
	}	
}
