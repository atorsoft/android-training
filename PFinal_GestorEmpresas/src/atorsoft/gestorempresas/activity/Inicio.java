package atorsoft.gestorempresas.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import atorsoft.gestorempresas.R;

public class Inicio extends Activity implements OnClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Configuración de pantalla completa
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_inicio);

		// Botones de opciones
		((ImageButton) findViewById(R.id.ibCrear)).setOnClickListener(this);
		((ImageButton) findViewById(R.id.ibEliminar)).setOnClickListener(this);
		((ImageButton) findViewById(R.id.ibBuscar)).setOnClickListener(this);
		((ImageButton) findViewById(R.id.ibAccederWeb))
				.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.admin_categorias, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.admin_categorias:
			Intent intentCategorias = new Intent(this, Categorias.class);
			startActivity(intentCategorias);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		switch (v.getId()) {
		case R.id.ibCrear:
			intent = new Intent(this, Creacion.class);
			startActivityForResult(intent, 0);
			break;
		case R.id.ibEliminar:
			intent = new Intent(this, Eliminacion.class);
			startActivity(intent);
			break;
		case R.id.ibBuscar:
			intent = new Intent(this, Busqueda.class);
			startActivity(intent);
			break;
		case R.id.ibAccederWeb:
			intent = new Intent(this, AccesoWeb.class);
			startActivity(intent);
			break;
		}

	}

}
