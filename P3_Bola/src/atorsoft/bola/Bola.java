package atorsoft.bola;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class Bola extends Activity {

	private BolaView bolaView = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Configuración de pantalla
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// Creación de superficie
		bolaView = new BolaView(this);
		setContentView(bolaView);
	}

	@Override
	protected void onResume() {
		super.onResume();
		bolaView.getThread().setEjecucion(true);
	}

	@Override
	protected void onPause() {
		super.onPause();
		bolaView.getThread().setEjecucion(false);
	}

}
