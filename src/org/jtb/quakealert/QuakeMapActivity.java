package org.jtb.quakealert;

import java.util.List;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class QuakeMapActivity extends MapActivity {
	static QuakeMapActivity mThis;

	private MapView mMapView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);

		mThis = this;
		mMapView = (MapView) findViewById(R.id.map_view);

		Integer p = savedInstanceState != null ? (Integer) savedInstanceState
				.get("org.jtb.quakealert.quake.position") : null;
		if (p == null) {
			Bundle extras = getIntent().getExtras();
			p = extras != null ? (Integer) extras
					.get("org.jtb.quakealert.quake.position") : null;
		}

		Quake quake = QuakeRefreshService.matchQuakes.get(p);
		mMapView.setBuiltInZoomControls(true);
		mMapView.setSatellite(true);
		
		List<Overlay> mapOverlays = mMapView.getOverlays();
		int size = QuakeRefreshService.matchQuakes.size();
		for (int i = size - 1; i != -1; i--) {
			Quake q = QuakeRefreshService.matchQuakes.get(i);

			Drawable drawable = this.getResources().getDrawable(R.drawable.one);
			QuakeOverlay itemizedOverlay = new QuakeOverlay(drawable, q);

			OverlayItem overlayitem = new OverlayItem(q.getGeoPoint(), ""
					+ q.getMagnitude(), "");
			itemizedOverlay.addOverlay(overlayitem);
			mapOverlays.add(itemizedOverlay);
		}

		QuakePrefs qp = new QuakePrefs(this);
		if (qp.isZoomToFit()) {
			zoomToSpan(QuakeRefreshService.matchQuakes, quake);
		} else {
			zoomToMagnitude(quake);
		}
		mMapView.getController().animateTo(quake.getGeoPoint());
	}

	private void zoomToMagnitude(Quake quake) {
		int zoom;
		if (quake.getMagnitude() < 2) {
			zoom = 11;
		} else if (quake.getMagnitude() < 3) {
			zoom = 10;
		} else if (quake.getMagnitude() < 4) {
			zoom = 9;
		} else if (quake.getMagnitude() < 5) {
			zoom = 8;
		} else if (quake.getMagnitude() < 6) {
			zoom = 7;
		} else {
			zoom = 6;
		}
		mMapView.getController().setZoom(zoom);
	}

	private void zoomToSpan(List<Quake> quakeList, Quake center) {
		int latMax = quakeList.get(0).getLatitudeE6();
		int latMin = quakeList.get(0).getLatitudeE6();
		int lonMax = quakeList.get(0).getLongitudeE6();
		int lonMin = quakeList.get(0).getLongitudeE6();
		int latC = center.getLatitudeE6();
		int lonC = center.getLongitudeE6();

		for (Quake q : quakeList) {
			if (q.getLatitudeE6() > latMax) {
				latMax = q.getLatitudeE6();
			}
			if (q.getLatitudeE6() < latMin) {
				latMin = q.getLatitudeE6();
			}
			if (q.getLongitudeE6() > lonMax) {
				lonMax = q.getLongitudeE6();
			}
			if (q.getLongitudeE6() < lonMin) {
				lonMin = q.getLongitudeE6();
			}
		}

		int latM = (latMax + latMin) / 2;
		int lonM = (lonMax + lonMin) / 2;

		if (latC - latM > 0) {
			latMax += latC - latM;
		} else {
			latMin += latC - latM;
		}
		if (lonC - lonM > 0) {
			lonMax += lonC - lonM;
		} else {
			lonMin += latC - latM;
		}

		mMapView.getController().zoomToSpan(latMax - latMin, lonMax - lonMin);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

}
