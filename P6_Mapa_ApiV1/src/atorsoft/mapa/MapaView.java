package atorsoft.mapa;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import atorsoft.mapa.overlay.PosicionOverlay;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class MapaView extends MapView {

	private PosicionOverlay mPosicionOverlay;
	boolean mActionMove = false;

	public MapaView(Context context, String apiKey) {
		super(context, apiKey);
	}

	public MapaView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MapaView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			mActionMove = true;
		}
		if (event.getAction() == MotionEvent.ACTION_UP) {
			if (!mActionMove) {
				GeoPoint geoPoint = getProjection().fromPixels(
						(int) event.getX(), (int) event.getY());
				mostrarPosicion(geoPoint);
			} else {
				mActionMove = false;
			}
		}
		return super.onTouchEvent(event);
	}

	private void mostrarPosicion(GeoPoint geoPoint) {
		if (mPosicionOverlay == null) {
			Bitmap icono = BitmapFactory.decodeResource(getResources(),
					R.drawable.posicion);
			mPosicionOverlay = new PosicionOverlay(new BitmapDrawable(icono));
		}
		List<Overlay> mapOverlays = getOverlays();
		if (mapOverlays.contains(mPosicionOverlay)) {
			mapOverlays.remove(mPosicionOverlay);
		}
		mPosicionOverlay.cambiarCoordenadas(geoPoint);
		mapOverlays.add(mPosicionOverlay);
	}

}
