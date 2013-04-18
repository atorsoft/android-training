package atorsoft.libreria.activity;

import atorsoft.libreria.R;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import atorsoft.libreria.database.LibreriaDBHelper;

public class Libreria extends TabActivity {

	private static LibreriaDBHelper mDBHelper;

	private static boolean mCargaLista;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_libreria);

		// Conexión con la base de datos
		mDBHelper = new LibreriaDBHelper(this);
		mDBHelper.abrir();

		// Recursos
		Resources res = getResources(); // Para la obtencion de los recursos

		// TabHost
		TabHost tabHost = getTabHost(); // TabHost de la actividad

		// Pestañas
		String tag; // Etiqueta de cada pestaña
		Intent intent; // Intent que se utilizará en cada pestaña
		TabHost.TabSpec tabSpec; // Propiedades de la pestaña

		// Listado
		tag = res.getString(R.string.listado);
		intent = new Intent().setClass(this, Listado.class);
		tabSpec = tabHost.newTabSpec(tag)
				.setIndicator(tag, res.getDrawable(R.drawable.tab_listado))
				.setContent(intent);
		tabHost.addTab(tabSpec);

		// Añadir
		tag = res.getString(R.string.anadir);
		intent = new Intent().setClass(this, Libro.class);
		tabSpec = tabHost.newTabSpec(tag)
				.setIndicator(tag, res.getDrawable(R.drawable.tab_anadir))
				.setContent(intent);
		tabHost.addTab(tabSpec);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mDBHelper != null) {
			mDBHelper.cerrar();
		}
	}

	public static LibreriaDBHelper getDBHelper() {
		return mDBHelper;
	}

	public static boolean isCargaLista() {
		return mCargaLista;
	}

	public static void setCargaLista(boolean cargaLista) {
		Libreria.mCargaLista = cargaLista;
	}

}
