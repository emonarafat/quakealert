package org.jtb.quakealert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

public class QuakePrefs {
	private Context context = null;

	public QuakePrefs(Context context) {
		this.context = context;
	}

	private String getString(String key, String def) {
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

	private float getFloat(String key, float def) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		float f = Float.parseFloat(prefs.getString(key, Float.toString(def)));
		return f;
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

	public float getMagnitude() {
		return getFloat("magnitude", (float) 3.0);
	}

	public boolean isNotificationsEnabled() {
		return getBoolean("notificationsEnabled", true);
	}

	public String getUnits() {
		return getString("units", "metric");
	}

	public void setUnits(String units) {
		setString("units", units);
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

	public HashSet<String> getMatchIds() {
		String idsString = getString("matchIds", null);
		HashSet<String> ids = split(idsString, ',');
		return ids;
	}

	public void setMatchIds(List<Quake> quakeList) {
		setString("matchIds", toIdsString(quakeList));
	}

	public HashSet<String> getNewIds() {
		String idsString = getString("newIds", null);
		HashSet<String> ids = split(idsString, ',');
		return ids;
	}

	public void setNewIds(List<Quake> quakeList) {
		setString("newIds", toIdsString(quakeList));
	}

	public void setNewIds(HashSet<String> ids) {
		setString("newIds", toIdsString(ids));
	}

	public void clearNewIds() {
		setString("newIds", "");
	}

	private static String toIdsString(List<Quake> quakeList) {
		StringBuffer b = new StringBuffer();
		if (quakeList != null) {
			int quakeListSize = quakeList.size();
			for (int i = 0; i < quakeListSize; i++) {
				b.append(quakeList.get(i).getId());
				if (i < quakeListSize-1) {
					b.append(',');
				}
			}
		}
		return b.toString();
	}

	private static String toIdsString(HashSet<String> ids) {
		StringBuffer b = new StringBuffer();
		if (ids != null) {
			for (Iterator<String> i = ids.iterator(); i.hasNext();) {
				b.append(i.next());
				if (i.hasNext()) {
					b.append(',');
				}
			}
		}
		return b.toString();
	}
	
	private static HashSet<String> split(String s, char c) {
		HashSet<String> tokens = new HashSet<String>();
		if (s == null || s.length() == 0) {
			return tokens;
		}
		StringTokenizer st = new StringTokenizer(s, ",");
		while (st.hasMoreTokens()) {
			tokens.add(st.nextToken());
		}

		return tokens;
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
}
