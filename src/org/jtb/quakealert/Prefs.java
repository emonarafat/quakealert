package org.jtb.quakealert;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

public class Prefs {
	private Context context = null;

	public Prefs(Context context) {
		this.context = context;
	}

	public String getString(String key, String def) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		String s = prefs.getString(key, def);
		return s;
	}

	private int getInt(String key, int def) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		int i = Integer.parseInt(prefs.getString(key, Integer.toString(def)));
		return i;
	}

	public long getLong(String key, long def) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		long l = Long.parseLong(prefs.getString(key, Long.toString(def)));
		return l;
	}

	private void setString(String key, String val) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor e = prefs.edit();
		e.putString(key, val);
		e.commit();
	}

	private void setBoolean(String key, boolean val) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor e = prefs.edit();
		e.putBoolean(key, val);
		e.commit();
	}

	private void setInt(String key, int val) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor e = prefs.edit();
		e.putString(key, Integer.toString(val));
		e.commit();
	}

	private void setLong(String key, long val) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor e = prefs.edit();
		e.putString(key, Long.toString(val));
		e.commit();
	}

	private boolean getBoolean(String key, boolean def) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean b = prefs.getBoolean(key, def);
		return b;
	}

	public boolean isNotificationFlash() {
		return getBoolean("notificationFlash", true);
	}

	public boolean isNotificationAlert() {
		return getBoolean("notificationAlert", false);
	}

	public boolean isNotificationVibrate() {
		return getBoolean("notificationVibrate", false);
	}

	public boolean isWarn(String warnId) {
		return getBoolean(warnId, true);
	}

	public void setWarn(String warnId, boolean warn) {
		setBoolean(warnId, warn);
	}


	public Interval getInterval() {
		String is = getString("interval", "HOUR");
		Interval i = Interval.valueOf(is);
		if (i != null) {
			return i;
		} 
		Log.e("quakealert", "invalid interval found: " + is);
		setInterval(Interval.HOUR);
		return Interval.HOUR;
	}

	public void setInterval(Interval i) {
		setString("interval", i.toString());
	}

	public int getRange() {
		return getInt("range", -1);
	}

	public void setRange(int meters) {
		setInt("range", meters);
	}

	public Magnitude getMagnitude() {
		String m = getString("magnitude", Magnitude.M3.toString());
		return Magnitude.valueOf(m);
	}

	public void setMagnitude(Magnitude mag) {
		setString("magnitude", mag.toString());
	}

	public boolean isNotificationsEnabled() {
		return getBoolean("notificationsEnabled", true);
	}

	public Units getUnits() {
		String u = getString("units", "METRIC");
		return Units.valueOf(u);
	}

	public Theme getTheme() {
		String t = getString("theme", "LIGHT");
		return Theme.valueOf(t);
	}

	public Age getMaxAge() {
		String a = getString("maxAge", "THREE");
		return Age.valueOf(a);
	}

	public void setUnits(Units units) {
		setString("units", units.toString());
	}

	public boolean isUpgradedTo(int version) {
		int upgradedTo = getInt("upgradedTo", 0);
		if (upgradedTo >= version) {
			return true;
		}
		return false;
	}

	public void setUpgradedTo(int version) {
		setInt("upgradedTo", version);
	}

	public Uri getNotificationAlertSound() {
		String s = getString("notificationAlertSound", "");
		if (s == null || s.length() == 0) {
			return null;
		}
		Uri sound = Uri.parse(s);
		
		return sound;
	}	
	
	public void setNotificationAlertSound(Uri soundUri) {
		setString("notificationAlertSound", soundUri.toString());
	}
	
	public boolean isZoomToFit() {
		return getBoolean("zoomToFit", false);
	}

	public boolean isBootStart() {
		return getBoolean("bootStart", false);
	}
	
	public long getLastUpdate() {
		return getLong("lastUpdate", -1);
	}
	
	public void setLastUpdate() {
		setLong("lastUpdate", System.currentTimeMillis());
	}
	
	public boolean isUseLocation() {
		return getBoolean("useLocation", true);
	}
}
