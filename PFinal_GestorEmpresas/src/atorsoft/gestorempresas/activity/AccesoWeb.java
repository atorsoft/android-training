package atorsoft.gestorempresas.activity;

import java.net.MalformedURLException;
import java.net.URL;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import atorsoft.gestorempresas.alert.AlertaOK;
import atorsoft.gestorempresas.provider.contract.GestorEmpresasContract.EmpresaColumnas;

import atorsoft.gestorempresas.R;

public class AccesoWeb extends ListActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listado_empresas);
		cargarLista();
	}

	private void cargarLista() {
		// Cursor de empresas
		Cursor cursorEmpresas = getContentResolver().query(
				EmpresaColumnas.CONTENT_URI, null, null, null, null);
		startManagingCursor(cursorEmpresas);

		// Adaptador de categorías
		ListAdapter adapterEmpresas = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_2, cursorEmpresas,
				new String[] { EmpresaColumnas.NOMBRE, EmpresaColumnas.WEB },
				new int[] { android.R.id.text1, android.R.id.text2 });

		// Asignación del adaptador
		setListAdapter(adapterEmpresas);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String urlEmpresa = ((TextView) v.findViewById(android.R.id.text2))
				.getText().toString();
		accederWeb(urlEmpresa);
	}

	public void accederWeb(String urlString) {
		try {
			// Validación de URL
			if (!URLUtil.isHttpUrl(urlString) && !URLUtil.isHttpsUrl(urlString)) {
				urlString = "http://" + urlString;
			}
			URL url = new URL(urlString);

			// Acceso a la web
			Intent intentWeb = new Intent(Intent.ACTION_VIEW, Uri.parse(url
					.toString()));
			startActivity(intentWeb);

		} catch (MalformedURLException mue) {
			AlertaOK.mostrar(this, null,
					getString(R.string.error_web_no_valida));
		} catch (Exception e) {
			AlertaOK.mostrar(this, null, getString(R.string.error_acceso_web));
		}
	}

}
