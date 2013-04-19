package atorsoft.mapa.overlay;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class LocalizacionOverlay extends ItemizedOverlay<OverlayItem> {

	private OverlayItem mOverlayItem;

	public LocalizacionOverlay(Drawable drawable) {
		super(boundCenter(drawable));
	}

	public void cambiarCoordenadas(GeoPoint geoPoint) {
		mOverlayItem = new OverlayItem(geoPoint, "", "");
		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		return mOverlayItem;
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, false);
	}

}
