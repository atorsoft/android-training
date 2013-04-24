package atorsoft.gestorempresas.activity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import atorsoft.gestorempresas.alert.AlertaOK;
import atorsoft.gestorempresas.alert.AlertaOKCancel;
import atorsoft.gestorempresas.provider.contract.GestorEmpresasContract.CategoriaColumnas;

import atorsoft.gestorempresas.R;

public class Categorias extends ListActivity {

	// Categoría seleccionada para eliminar
	private long idCategoria;
	private EditText etNombreCategoria;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_categorias);

		etNombreCategoria = (EditText) findViewById(R.id.etNombreCategoria);

		ImageButton bAnadir = (ImageButton) findViewById(R.id.ibAnadir);
		bAnadir.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				anadirCategoria();
			}
		});

		cargarLista();
	}

	private void cargarLista() {
		// Consulta de categorías mediante proveedor de contenido
		Cursor cursorCategorias = getContentResolver().query(
				CategoriaColumnas.CONTENT_URI, null, null, null, null);
		startManagingCursor(cursorCategorias);

		// Adaptador de categorías
		ListAdapter adapterCategorias = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_1, cursorCategorias,
				new String[] { CategoriaColumnas.NOMBRE },
				new int[] { android.R.id.text1 });

		// Asignación del adaptador
		setListAdapter(adapterCategorias);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		idCategoria = id;
		String nombreCategoria = ((TextView) v.findViewById(android.R.id.text1))
				.getText().toString();
		String titulo = this
				.getString(R.string.alerta_eliminacion_categoria_titulo);
		String mensaje = this
				.getString(R.string.alerta_eliminacion_categoria_mensaje_ini)
				+ nombreCategoria
				+ this.getString(R.string.alerta_eliminacion_categoria_mensaje_fin);

		// Mensaje de confirmación de eliminación
		AlertaOKCancel.mostrar(this, titulo, mensaje,
				new AlertDialog.OnClickListener() {
					public void onClick(DialogInterface dlg, int i) {
						eliminarCategoria();
					}
				}, null);
	}

	private void eliminarCategoria() {
		try {
			Uri uri = ContentUris.withAppendedId(CategoriaColumnas.CONTENT_URI,
					idCategoria);
			// Eliminación de elemento mediante proveedor de contenido
			getContentResolver().delete(uri, null, null);
		} catch (Exception e) {
			AlertaOK.mostrar(this, null, e.getMessage());
		}
	}

	private void anadirCategoria() {
		try {
			String categoria = etNombreCategoria.getText().toString().trim();
			if (categoria != null && !"".equals(categoria)) {
				ContentValues valores = new ContentValues();
				valores.put(CategoriaColumnas.NOMBRE, categoria);
				// Inserción de elemento mediante proveedor de contenido
				getContentResolver().insert(CategoriaColumnas.CONTENT_URI,
						valores);
				etNombreCategoria.setText("");
			}
		} catch (Exception e) {
			AlertaOK.mostrar(this, null, e.getMessage());
		}
	}

}
