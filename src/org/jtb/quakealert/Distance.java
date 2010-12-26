package org.jtb.quakealert;

public class Distance {
	private int meters;

	public Distance(int meters) {
		this.meters = meters;
	}

	public Distance(float meters) {
		this.meters = (int) meters;
	}

	public int getMeters() {
		return meters;
	}

	public int getKilometers() {
		return (int) (meters * 0.001);
	}

	public int getMiles() {
		return (int) (meters * 0.00062137119);
	}

	public String toKilometersString() {
		return getKilometers() + " km";
	}

	public String toMilesString() {
		return getMiles() + " miles";
	}

	public String toString(Prefs qp) {
		Units units = qp.getUnits();
		if (meters == -1) {
			return "Show All";
		}
		if (units == Units.METRIC) {
			return toKilometersString();
		}

		return toMilesString();
	}
}
