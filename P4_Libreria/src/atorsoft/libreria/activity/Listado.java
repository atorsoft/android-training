package atorsoft.libreria.activity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import atorsoft.libreria.database.LibreriaDBHelper;

import atorsoft.libreria.R;

public class Listado extends ListActivity {

	// Libro seleccionado
	private long idLibro;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_listado);

		Libreria.setCargaLista(true);
		registerForContextMenu(getListView());
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (Libreria.isCargaLista()) {
			cargarLista();
			Libreria.setCargaLista(false);
		}
	}

	private void cargarLista() {
		Cursor cursorLibros = Libreria.getDBHelper().getLibros();
		startManagingCursor(cursorLibros);

		String[] from = new String[] { LibreriaDBHelper._ID,
				LibreriaDBHelper.TITULO, LibreriaDBHelper.AUTOR,
				LibreriaDBHelper.ISBN, LibreriaDBHelper.PRECIO };
		int[] to = new int[] { R.id.row_id, R.id.row_titulo, R.id.row_autor,
				R.id.row_isbn, R.id.row_precio };

		ListAdapter adapterLibros = new SimpleCursorAdapter(this,
				R.layout.list_row_libro, cursorLibros, from, to);

		setListAdapter(adapterLibros);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_listado, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// Información del elemento pulsado
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
				.getMenuInfo();
		idLibro = menuInfo.id;
		View row_vista = menuInfo.targetView;
		String tituloLibro = ((TextView) row_vista
				.findViewById(R.id.row_titulo)).getText().toString();

		// Opción seleccionada
		switch (item.getItemId()) {
		case R.id.editar:
			// Actividad Libro en modo edición
			Intent intentLibro = new Intent(this, Libro.class);
			intentLibro.putExtra(LibreriaDBHelper._ID, idLibro);
			startActivityForResult(intentLibro, 0);
			return true;
		case R.id.eliminar:
			// Mensaje de confirmación
			String titulo = this.getString(R.string.alerta_titulo);
			String mensaje = this.getString(R.string.alerta_mensaje_ini)
					+ tituloLibro + this.getString(R.string.alerta_mensaje_fin);
			mostrarConfirmacionEliminar(titulo, mensaje);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void mostrarConfirmacionEliminar(String titulo, String mensaje) {
		AlertDialog.Builder alerta = new AlertDialog.Builder(this);
		alerta.setTitle(titulo);
		alerta.setMessage(mensaje);
		alerta.setPositiveButton(android.R.string.ok,
				new AlertDialog.OnClickListener() {
					public void onClick(DialogInterface dlg, int i) {
						eliminarLibro();
					}
				});
		alerta.setNegativeButton(android.R.string.cancel, null);
		alerta.show();
	}

	private void eliminarLibro() {
		Libreria.getDBHelper().deleteLibro(idLibro);
		cargarLista();
	}

}
