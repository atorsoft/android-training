package atorsoft.mapa;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;
import atorsoft.mapa.overlay.LocalizacionOverlay;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class Mapa extends MapActivity implements LocationListener {

	private static final int ZOOM = 18;
	private static final int DESPLAZAMIENTO = 100;

	private MapView mMapView;
	private LocationManager mLocationManager;
	private LocalizacionOverlay mLocalizacionOverlay;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mapa);

		mMapView = (MapView) findViewById(R.id.mapview);
		mMapView.setBuiltInZoomControls(true);
		mMapView.getZoomButtonsController().setAutoDismissed(false);

		// Controles
		ImageView arriba = (ImageView) findViewById(R.id.arriba);
		ImageView abajo = (ImageView) findViewById(R.id.abajo);
		ImageView izda = (ImageView) findViewById(R.id.izda);
		ImageView dcha = (ImageView) findViewById(R.id.dcha);
		ImageView centro = (ImageView) findViewById(R.id.centro);

		GestorControles gestorControles = new GestorControles();
		arriba.setOnClickListener(gestorControles);
		abajo.setOnClickListener(gestorControles);
		izda.setOnClickListener(gestorControles);
		dcha.setOnClickListener(gestorControles);
		centro.setOnClickListener(gestorControles);

		// Localización
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (mLocationManager == null) {
			Toast.makeText(this, "Error al recuperar el LocationManager",
					Toast.LENGTH_LONG).show();
		}
		activarLocalizacion();
	}

	private void activarLocalizacion() {
		try {
			// Recepción de datos del GPS
			mLocationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 30000, 20,
					(LocationListener) this);
		} catch (Exception e) {
			Toast.makeText(this, "Error al inicializar el GPS",
					Toast.LENGTH_LONG).show();
		}
	}

	private void mostrarLocalizacion(GeoPoint geoPoint) {
		if (mLocalizacionOverlay == null) {
			Drawable drawable = this.getResources().getDrawable(
					R.drawable.localizacion);
			mLocalizacionOverlay = new LocalizacionOverlay(drawable);
		}
		List<Overlay> mapOverlays = mMapView.getOverlays();
		if (mapOverlays.contains(mLocalizacionOverlay)) {
			mapOverlays.remove(mLocalizacionOverlay);
		}
		mLocalizacionOverlay.cambiarCoordenadas(geoPoint);
		mapOverlays.add(mLocalizacionOverlay);

		MapController mapController = mMapView.getController();
		mapController.animateTo(geoPoint);
		int zoom = (mMapView.getMaxZoomLevel() > ZOOM) ? ZOOM : mMapView
				.getMaxZoomLevel();
		mapController.setZoom(zoom);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void onLocationChanged(Location location) {
		BigDecimal latitud = new BigDecimal(location.getLatitude()).setScale(6,
				RoundingMode.HALF_DOWN);
		BigDecimal longitud = new BigDecimal(location.getLongitude()).setScale(
				6, RoundingMode.HALF_DOWN);
		int latitudE6 = latitud.multiply(new BigDecimal(1E6)).intValue();
		int longitudE6 = longitud.multiply(new BigDecimal(1E6)).intValue();

		mostrarLocalizacion(new GeoPoint(latitudE6, longitudE6));

		// Una vez señalizada la localización finalizamos el listener
		mLocationManager.removeUpdates((LocationListener) this);
	}

	@Override
	public void onProviderDisabled(String provider) {
		if (LocationManager.GPS_PROVIDER.equalsIgnoreCase(provider)) {
			Toast.makeText(this, "GPS deshabilitado: localización por red",
					Toast.LENGTH_LONG).show();
			mLocationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 30000, 20,
					(LocationListener) this);
		} else {
			Toast.makeText(this,
					"Provider deshabilitado: " + provider.toUpperCase(),
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onProviderEnabled(String provider) {
		if (LocationManager.GPS_PROVIDER.equalsIgnoreCase(provider)) {
			Toast.makeText(this, "GPS habilitado", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this,
					"Provider habilitado: " + provider.toUpperCase(),
					Toast.LENGTH_LONG).show();
		}

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	private class GestorControles implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.arriba:
				moverMapa(R.id.arriba);
				break;
			case R.id.abajo:
				moverMapa(R.id.abajo);
				break;
			case R.id.izda:
				moverMapa(R.id.izda);
				break;
			case R.id.dcha:
				moverMapa(R.id.dcha);
				break;
			case R.id.centro:
				activarLocalizacion();
				break;
			}
		}

		private void moverMapa(int direccion) {
			GeoPoint centroGP = mMapView.getMapCenter();
			Projection proyeccion = mMapView.getProjection();
			Point centroP = new Point();
			proyeccion.toPixels(centroGP, centroP);
			switch (direccion) {
			case R.id.arriba:
				centroP.y -= DESPLAZAMIENTO;
				break;
			case R.id.abajo:
				centroP.y += DESPLAZAMIENTO;
				break;
			case R.id.izda:
				centroP.x -= DESPLAZAMIENTO;
				break;
			case R.id.dcha:
				centroP.x += DESPLAZAMIENTO;
				break;
			}
			mMapView.getController().setCenter(
					proyeccion.fromPixels(centroP.x, centroP.y));
		}
	}
}
