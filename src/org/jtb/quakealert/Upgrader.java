package org.jtb.quakealert;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class Upgrader {
	private QuakePrefs mQuakePrefs;
	private Context mContext;
	
	public Upgrader(Context c) {
		mContext = c;
		mQuakePrefs = new QuakePrefs(mContext);
	}
	
	public void upgrade() {
		PackageManager manager = mContext.getPackageManager();
		PackageInfo info;
		try {
			info = manager.getPackageInfo(mContext.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			Log.e("quakealert", "could not get version", e);
			return;
		}

		if (!mQuakePrefs.isUpgradedTo(31)) {
			// set old float magnitude value to constant
			String m = mQuakePrefs.getString("magnitude", null);
			if (m != null && !m.startsWith("M")) {
				float f = Float.parseFloat(m);
				Magnitude mag = Magnitude.valueOf(f);
				if (mag == null) {
					mag = Magnitude.M3;
				}
				mQuakePrefs.setMagnitude(mag);
			}

			// set old string units value to constant
			String u = mQuakePrefs.getString("units", null);
			if (u != null && Character.isLowerCase(u.charAt(0))) {
				Units units;
				if (u.equals("metric")) {
					units = Units.METRIC;
				} else {
					units = Units.US;
				}
				mQuakePrefs.setUnits(units);
			}

		}

		mQuakePrefs.setUpgradedTo(info.versionCode);
	}
}
