package atorsoft.libreria.cliente;

import android.app.Activity;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import atorsoft.libreria.cliente.LibreriaContract.LibroColumnas;


public class DetalleLibro extends Activity {

	private TextView titulo;
	private TextView autor;
	private TextView isbn;
	private TextView precio;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detalle_libro);

		// Obtención de ID
		Bundle extras = getIntent().getExtras();
		Long idLibro = (savedInstanceState == null) ? null
				: (Long) savedInstanceState.getSerializable(LibroColumnas._ID);
		if (idLibro == null) {
			idLibro = extras != null ? extras.getLong(LibroColumnas._ID) : null;
		}

		// Campos de detalle
		titulo = (TextView) findViewById(R.id.titulo);
		autor = (TextView) findViewById(R.id.autor);
		isbn = (TextView) findViewById(R.id.isbn);
		precio = (TextView) findViewById(R.id.precio);

		cargarDatos(idLibro);
	}

	private void cargarDatos(Long idLibro) {
		Uri uri = ContentUris
				.withAppendedId(LibroColumnas.CONTENT_URI, idLibro);
		Cursor cursorLibro = getContentResolver().query(uri, null, null, null,
				null);
		startManagingCursor(cursorLibro);
		cursorLibro.moveToFirst();

		titulo.setText(cursorLibro.getString(cursorLibro
				.getColumnIndexOrThrow(LibroColumnas.TITULO)));
		autor.setText(cursorLibro.getString(cursorLibro
				.getColumnIndexOrThrow(LibroColumnas.AUTOR)));
		isbn.setText(cursorLibro.getString(cursorLibro
				.getColumnIndexOrThrow(LibroColumnas.ISBN)));
		precio.setText(Float.toString(cursorLibro.getFloat(cursorLibro
				.getColumnIndexOrThrow(LibroColumnas.PRECIO))));
	}

}
