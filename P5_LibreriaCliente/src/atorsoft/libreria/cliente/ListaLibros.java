package atorsoft.libreria.cliente;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import atorsoft.libreria.cliente.LibreriaContract.LibroColumnas;


public class ListaLibros extends ListActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lista_libros);
		setTitle(R.string.title_activity_lista_libros);
		cargarLista();
	}

	private void cargarLista() {
		Cursor cursorLibros = getContentResolver().query(
				LibroColumnas.CONTENT_URI, null, null, null, null);
		startManagingCursor(cursorLibros);

		ListAdapter adapterLibros = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_1, cursorLibros,
				new String[] { LibroColumnas.TITULO },
				new int[] { android.R.id.text1 });

		setListAdapter(adapterLibros);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent detalleLibro = new Intent(this, DetalleLibro.class);
		detalleLibro.putExtra(LibroColumnas._ID, id);
		startActivity(detalleLibro);
	}
}
