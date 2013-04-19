package atorsoft.mapa.overlay;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class PosicionOverlay extends ItemizedOverlay<OverlayItem> {

	private static final int TAM_TEXTO_ETIQUETA = 12;
	private static final int MARGEN_TEXTO_ETIQUETA = 3;

	private int alturaIcono;
	private TextPaint mTextPaint = new TextPaint();
	private Paint mPaint = new Paint();

	private OverlayItem mOverlayItem;

	public PosicionOverlay(Drawable drawable) {
		super(boundCenterBottom(drawable));
		alturaIcono = ((BitmapDrawable) drawable).getBitmap().getHeight();
		mTextPaint.setTextSize(TAM_TEXTO_ETIQUETA);
		mTextPaint.setTextAlign(Paint.Align.CENTER);
		mTextPaint.setARGB(255, 255, 255, 255);
		mPaint.setARGB(130, 0, 0, 0);
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
		super.draw(canvas, mapView, shadow);

		// Posición del icono en coordenadas de pantalla
		GeoPoint geoPoint = mOverlayItem.getPoint();
		Point pixelPoint = new Point();
		mapView.getProjection().toPixels(geoPoint, pixelPoint);

		// Etiqueta
		String textoEtiqueta = mOverlayItem.routableAddress();
		Rect rectEtiqueta = new Rect();
		mTextPaint.getTextBounds(textoEtiqueta, 0, textoEtiqueta.length(),
				rectEtiqueta);
		rectEtiqueta.inset(-MARGEN_TEXTO_ETIQUETA, -MARGEN_TEXTO_ETIQUETA);
		// BoundCenterBottom: El punto de coordenadas es el punto central
		// inferior del icono
		rectEtiqueta.offsetTo(pixelPoint.x - rectEtiqueta.width() / 2,
				pixelPoint.y - alturaIcono - MARGEN_TEXTO_ETIQUETA);

		canvas.drawRoundRect(new RectF(rectEtiqueta), 2, 2, mPaint);
		canvas.drawText(textoEtiqueta, rectEtiqueta.left + rectEtiqueta.width()
				/ 2, rectEtiqueta.bottom - MARGEN_TEXTO_ETIQUETA, mTextPaint);
	}

}
