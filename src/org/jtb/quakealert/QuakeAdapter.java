package org.jtb.quakealert;

import java.util.List;

import android.app.Activity;
import android.graphics.Typeface;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class QuakeAdapter extends ArrayAdapter<Quake> {
	private List<Quake> quakes;
	private Location location;
	private Prefs quakePrefs;
	private LayoutInflater inflater;

	QuakeAdapter(Activity context, List<Quake> quakes) {
		super(context, R.layout.quake, quakes);
		this.quakes = quakes;

		quakePrefs = new Prefs(context);
		this.inflater = context.getLayoutInflater();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		if (convertView != null) {
			view = convertView;
		} else {
			view = inflater.inflate(R.layout.quake, null);
		}

		Quake q = quakes.get(position);

		View severityView = view.findViewById(R.id.severity);
		severityView.setBackgroundColor(q.getColor());

		TextView row1Text = (TextView) view.findViewById(R.id.row1_text);
		row1Text.setText("M" + q.getMagnitude() + " - " + q.getRegion());
		if (q.isNewQuake()) {
			row1Text.setTypeface(Typeface.DEFAULT_BOLD);
		} else {
			row1Text.setTypeface(Typeface.DEFAULT);
		}

		TextView row2Text = (TextView) view.findViewById(R.id.row2_text);
		String row2 = q.getListDateString();
		if (location != null) {
			Distance d = new Distance(q.getDistance(location));
			row2 += ", " + d.toString(quakePrefs);
		}
		row2Text.setText(row2);

		return view;
	}

	public List<Quake> getQuakes() {
		return quakes;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
}
