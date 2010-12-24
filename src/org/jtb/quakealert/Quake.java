package org.jtb.quakealert;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.graphics.Color;
import android.location.Location;
import android.util.Log;

import com.google.android.maps.GeoPoint;

public class Quake {
	private static final String USGS_URL_PREFIX = "http://earthquake.usgs.gov/earthquakes/recenteqsus/Quakes/";
	private static final Pattern QUAKE_PATTERN = Pattern
			.compile("([^,]+),([^,]+),([^,]+),\"([^\"]+) UTC\",([^,]+),([^,]+),([^,]+),([^,]+),\\s?([^,]+),\"([^\"]+)\"");
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"EEEE, MMMM d, yyyy HH:mm:ss", Locale.US);
	private static final SimpleDateFormat SHORT_DATE_FORMAT = new SimpleDateFormat(
			"EEE, h:mm a", Locale.US);
	private static final SimpleDateFormat LIST_DATE_FORMAT = new SimpleDateFormat(
			"EEEE, MMM d, h:mm a", Locale.US);
	private static final int ZONE_OFFSET = Calendar.getInstance().get(
			Calendar.ZONE_OFFSET);
	private static final int DST_OFFSET = Calendar.getInstance().get(
			Calendar.DST_OFFSET);

	static class ListComparator implements Comparator<Quake> {
		public int compare(Quake q1, Quake q2) {
			return q2.getDate().compareTo(q1.getDate());
		}
	}

	private String source;
	private String id;
	private String version;
	private Date date;
	private double latitude;
	private int latitudeE6;
	private double longitude;
	private int longitudeE6;
	private float magnitude;
	private float depth;
	private int nst;
	private String region;
	private int color = 0;
	private GeoPoint geoPoint;
	private boolean newQuake = false;

	public Quake(String line, long lastUpdate) throws NumberFormatException,
			ParseException {
		// line = line.replace("  ", " ");
		Matcher m = QUAKE_PATTERN.matcher(line);
		if (!m.matches()) {
			return;
		}

		source = m.group(1);
		id = m.group(2);
		version = m.group(3);
		region = m.group(10);

		date = parseDate(m.group(4));

		latitude = Double.parseDouble(m.group(5));
		longitude = Double.parseDouble(m.group(6));
		magnitude = Float.parseFloat(m.group(7));
		depth = Float.parseFloat(m.group(8));
		nst = Integer.parseInt(m.group(9));

		latitudeE6 = (int) (latitude * Math.pow(10, 6));
		longitudeE6 = (int) (longitude * Math.pow(10, 6));

		if (magnitude < 2) {
			color = Color.parseColor("#00ff00");
		} else if (magnitude < 3) {
			color = Color.parseColor("#ccff66");
		} else if (magnitude < 4) {
			color = Color.parseColor("#ffff33");
		} else if (magnitude < 5) {
			color = Color.parseColor("#ffcc00");
		} else if (magnitude < 6) {
			color = Color.parseColor("#ff9900");
		} else {
			color = Color.parseColor("#ff3300");
		}

		geoPoint = new GeoPoint(latitudeE6, longitudeE6);

		if (date.getTime() > lastUpdate) {
			newQuake = true;
		}
	}

	private static Date parseDate(String s) throws ParseException {
		try {
			// Log.d("quakealert", "s: + " + s);
			Date d = DATE_FORMAT.parse(s);
			// Log.d("quakealert", "d: + " + getDateString(d));
			d = new Date(d.getTime() + ZONE_OFFSET + DST_OFFSET);
			// Log.d("quakealert", "d: + " + getDateString(d));

			return d;
		} catch (ParseException e) {
			Log.e("quakealert", "could not parse date from string: " + s);
			throw e;
		}
	}

	@Override
	public String toString() {
		return "{ source=" + source + ", id=" + id + ", version=" + version
				+ ", date=" + getListDateString() + ", latitude=" + latitude
				+ ",longitude=" + longitude + ", magnitude=" + magnitude
				+ ", depth=" + depth + ", nst=" + nst + ", region=" + region
				+ " }";
	}

	public String getListDateString() {
		return getListDateString(date);
	}

	public String getShortDateString() {
		return getShortDateString(date);
	}

	public static String getListDateString(Date d) {
		return LIST_DATE_FORMAT.format(d);
	}

	public static String getShortDateString(Date d) {
		return SHORT_DATE_FORMAT.format(d);
	}

	public boolean matches(float magnitude, int range, Location location, Age age) {
		// matches magnitude?
		if (this.magnitude < magnitude) {
			return false;
		}
		
		// matches location?
		if (range > 0) {
			float distance = getDistance(location);
			// Log.d("quakealert", "range: " + range +
			// ", distance: "
			// + distance);
			if (distance > range) {
				return false;
			}
		}
		
		// matches age?
		if (date.getTime() < (System.currentTimeMillis() - age.toMillis())) {
			return false;
		}

		return true;
	}

	public float getDistance(Location location) {
		if (location == null) {
			return -1;
		}
		Location quakeLocation = new Location(location);
		quakeLocation.setLatitude(latitude);
		quakeLocation.setLongitude(longitude);
		float distance = location.distanceTo(quakeLocation);
		return distance;

	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Quake)) {
			return false;
		}
		Quake other = (Quake) o;
		return id.equals(other.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	public Date getDate() {
		return date;
	}

	public float getMagnitude() {
		return magnitude;
	}

	public String getRegion() {
		return region;
	}

	public int getColor() {
		return color;
	}

	public String getId() {
		return id;
	}

	public int getLatitudeE6() {
		return latitudeE6;
	}

	public int getLongitudeE6() {
		return longitudeE6;
	}

	public GeoPoint getGeoPoint() {
		return geoPoint;
	}

	public float getDepth() {
		return depth;
	}

	public String getSource() {
		return source;
	}

	public boolean isNewQuake() {
		return newQuake;
	}

	public String getDetailUrl() {
		String u = USGS_URL_PREFIX + source + id + ".php";
		return u;
	}
}
