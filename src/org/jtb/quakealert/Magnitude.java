package org.jtb.quakealert;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

public enum Magnitude {
	M1(1.0f, 0), M3(3.0f, 1), M4(4.0f, 2), M5(5.0f, 3), M6(6.0f, 4), M7(7.0f, 5), M8(
			8.0f, 6), M9(9.0f, 7);

	@SuppressWarnings("serial")
	private static final Map<Float,Magnitude> byValue = new HashMap<Float, Magnitude>() {
		{
			for (Magnitude m : Magnitude.values()) {
				put(m.getValue(), m);
			}
		}
	};

	private int position;
	private float value;

	private Magnitude(float value, int position) {
		this.value = value;
		this.position = position;
	}

	public String getTitle(Context context) {
		return context.getResources().getStringArray(R.array.magnitude_entries)[position];
	}
	
	public float getValue() {
		return value;
	}

	public static Magnitude valueOf(float f) {
		Magnitude m = byValue.get(f);
		if (m == null) {
			throw new IllegalArgumentException(f + " has no value");
		}
		return m;
	}
}
