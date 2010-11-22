package org.jtb.quakealert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

public class QuakeOverlay extends ItemizedOverlay<OverlayItem> {
	private static final int TEXTSIZE = 16;
	private static final int TEXTLINES = 2;
	private static final int PADDING = 2;

	private static class DrawValues {

		static Paint markOutlinePaint = new Paint();
		static Paint markGlowPaint = new Paint();
		static Paint bgOutlinePaint = new Paint();
		static Paint bgGlowPaint = new Paint();

		static {
			markOutlinePaint.setColor(Color.BLACK);
			markOutlinePaint.setAntiAlias(true);
			markOutlinePaint.setStrokeWidth(1);
			markOutlinePaint.setStyle(Paint.Style.STROKE);

			markGlowPaint.setColor(Color.WHITE);
			markGlowPaint.setAntiAlias(true);
			markGlowPaint.setStyle(Paint.Style.STROKE);
			markGlowPaint.setStrokeWidth(1);

			bgOutlinePaint.setColor(Color.BLACK);
			bgOutlinePaint.setStyle(Paint.Style.STROKE);
			bgOutlinePaint.setStrokeWidth(1);
			bgOutlinePaint.setAntiAlias(true);

			bgGlowPaint.setColor(Color.WHITE);
			bgGlowPaint.setStyle(Paint.Style.STROKE);
			bgGlowPaint.setStrokeWidth(1);
			bgGlowPaint.setAntiAlias(true);
		}

		Paint textPaint = new Paint();
		int dmgMeters;
		Paint markPaint = new Paint();
		String s1;
		String s2;
		int labelWidth;
		int markRadius;
		Paint feelPaint = new Paint();
		Paint feelOutlinePaint = new Paint();
		Paint bgPaint = new Paint();

		DrawValues(Quake quake) {
			s1 = "M" + quake.getMagnitude();
			s2 = quake.getShortDateString();

			textPaint.setAntiAlias(true);
			textPaint.setColor(Color.BLACK);
			textPaint.setTextSize(TEXTSIZE);
			textPaint.setFakeBoldText(true);

			labelWidth = (int) (1 + Math.max(textPaint.measureText(s1),
					textPaint.measureText(s2)));

			markRadius = (int) (quake.getMagnitude() * 2);

			// the circle to mark the spot
			markPaint.setColor(quake.getColor());
			markPaint.setAntiAlias(true);
			markPaint.setAlpha(150);
			
			// "feel" circle
			feelPaint.setColor(quake.getColor());
			feelPaint.setAntiAlias(true);
			feelPaint.setAlpha(50);
			feelOutlinePaint.setColor(quake.getColor());
			feelOutlinePaint.setAntiAlias(true);
			feelOutlinePaint.setStyle(Paint.Style.STROKE);
			feelOutlinePaint.setStrokeWidth(1);

			// background
			bgPaint.setColor(quake.getColor());
			bgPaint.setAntiAlias(true);

			dmgMeters = (int) Math.max(quake.getMagnitude() * 10, Math.pow(
					quake.getMagnitude(), 3)) * 1000;
		}
	}

	private HashMap<Quake, DrawValues> drawValues = new HashMap<Quake, DrawValues>();
	private List<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Quake quake;

	public QuakeOverlay(Drawable defaultMarker, Quake quake) {
		super(boundCenterBottom(defaultMarker));
		this.quake = quake;

	}

	private DrawValues getDrawValues(Quake quake) {
		DrawValues dvs = drawValues.get(quake);
		if (dvs == null) {
			dvs = new DrawValues(quake);
		}

		return dvs;
	}

	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}

	public void addOverlay(OverlayItem overlay) {
		mOverlays.add(overlay);
		populate();
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if (shadow) {
			return;
		}
		
		Projection projection = mapView.getProjection();
		Point point = new Point();
		projection.toPixels(quake.getGeoPoint(), point);

		int h = mapView.getHeight();
		int w = mapView.getWidth();
		int sx = mapView.getScrollX();
		int sy = mapView.getScrollY();

		if (point.x < sx || point.x > sx + w) {
			return;
		}
		if (point.y < sy || point.y > sy + h) {
			return;
		}

		DrawValues dvs = getDrawValues(quake);
		int zoom = mapView.getZoomLevel();

		int radius;
		if (zoom >= 6) {
			radius = 4;
		} else {
			radius = dvs.markRadius;
		}
		
		canvas.drawCircle(point.x, point.y, radius + 2,
				DrawValues.markGlowPaint);
		canvas.drawCircle(point.x, point.y, radius + 1,
				DrawValues.markOutlinePaint);
		canvas.drawCircle(point.x, point.y, radius, dvs.markPaint);

		int dmgRadius = (int) projection.metersToEquatorPixels(dvs.dmgMeters);
		if (dmgRadius > 3 * dvs.markRadius) {
			canvas.drawCircle(point.x, point.y, dmgRadius + 1,
					dvs.feelOutlinePaint);
			canvas.drawCircle(point.x, point.y, dmgRadius, dvs.feelPaint);
		}

		// Log.d("quakealert", "zoom: " + zoom);
		if (zoom >= 6) {
			RectF rect = new RectF();
			int rx1 = point.x + radius + PADDING * 2;
			int ry1 = point.y - (TEXTSIZE * TEXTLINES) / 2 - PADDING;
			int rx2 = rx1 + dvs.labelWidth + PADDING * 2;
			int ry2 = ry1 + TEXTSIZE * TEXTLINES + PADDING * 2;
			rect.set(rx1, ry1, rx2, ry2);

			RectF rectOutline = new RectF();
			int rox1 = rx1 - 1;
			int roy1 = ry1 - 1;
			int rox2 = rx2 + 1;
			int roy2 = ry2 + 1;
			rectOutline.set(rox1, roy1, rox2, roy2);

			RectF rectGlow = new RectF();
			int rgx1 = rx1 - 2;
			int rgy1 = ry1 - 2;
			int rgx2 = rx2 + 2;
			int rgy2 = ry2 + 2;
			rectGlow.set(rgx1, rgy1, rgx2, rgy2);

			canvas.drawRoundRect(rectGlow, 2, 2, DrawValues.bgGlowPaint);
			canvas.drawRoundRect(rectOutline, 2, 2, DrawValues.bgOutlinePaint);
			canvas.drawRoundRect(rect, 2, 2, dvs.bgPaint);	
			
			int t1x = rx1 + PADDING;
			int t1y = ry1 + TEXTSIZE;
			canvas.drawText(dvs.s1, t1x, t1y, dvs.textPaint);

			int t2x = rx1 + PADDING;
			int t2y = t1y + TEXTSIZE;
			canvas.drawText(dvs.s2, t2x, t2y, dvs.textPaint);
		}
	}

}
