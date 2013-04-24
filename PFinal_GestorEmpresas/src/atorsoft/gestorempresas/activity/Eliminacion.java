package atorsoft.gestorempresas.activity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import atorsoft.gestorempresas.alert.AlertaOK;
import atorsoft.gestorempresas.alert.AlertaOKCancel;
import atorsoft.gestorempresas.provider.contract.GestorEmpresasContract.EmpresaColumnas;

import atorsoft.gestorempresas.R;

public class Eliminacion extends ListActivity {

	// Empresa seleccionada
	private long idEmpresa;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listado_empresas);
		cargarLista();
	}

	private void cargarLista() {
		// Consulta de empresas mediante proveedor de contenido
		Cursor cursorEmpresas = getContentResolver().query(
				EmpresaColumnas.CONTENT_URI, null, null, null, null);
		startManagingCursor(cursorEmpresas);

		// Adaptador de categorías
		ListAdapter adapterEmpresas = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_1, cursorEmpresas,
				new String[] { EmpresaColumnas.NOMBRE },
				new int[] { android.R.id.text1 });

		// Asignación del adaptador
		setListAdapter(adapterEmpresas);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		idEmpresa = id;
		String nombreEmpresa = ((TextView) v.findViewById(android.R.id.text1))
				.getText().toString();
		String titulo = this.getString(R.string.alerta_titulo);
		String mensaje = this.getString(R.string.alerta_mensaje_ini)
				+ nombreEmpresa + this.getString(R.string.alerta_mensaje_fin);

		// Mensaje de confirmación de eliminación
		AlertaOKCancel.mostrar(this, titulo, mensaje,
				new AlertDialog.OnClickListener() {
					public void onClick(DialogInterface dlg, int i) {
						eliminarEmpresa();
					}
				}, null);
	}

	private void eliminarEmpresa() {
		try {
			Uri uri = ContentUris.withAppendedId(EmpresaColumnas.CONTENT_URI,
					idEmpresa);
			// Eliminación de elemento mediante proveedor de contenido
			getContentResolver().delete(uri, null, null);
		} catch (Exception e) {
			AlertaOK.mostrar(this, null, e.getMessage());
		}
	}

}
