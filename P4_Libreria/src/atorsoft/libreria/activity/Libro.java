package atorsoft.libreria.activity;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import atorsoft.libreria.database.LibreriaDBHelper;
import atorsoft.libreria.exception.CampoVacioException;
import atorsoft.libreria.listener.GuardarDatos;

import atorsoft.libreria.R;

public class Libro extends Activity {

	private Long idLibro;

	private EditText etTitulo;
	private EditText etAutor;
	private EditText etISBN;
	private EditText etPrecio;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_libro);

		// Obtención de ID
		Bundle extras = getIntent().getExtras();
		idLibro = (savedInstanceState == null) ? null
				: (Long) savedInstanceState
						.getSerializable(LibreriaDBHelper._ID);
		if (idLibro == null) {
			idLibro = extras != null ? extras.getLong(LibreriaDBHelper._ID)
					: null;
		}
		etTitulo = (EditText) findViewById(R.id.etTitulo);
		etAutor = (EditText) findViewById(R.id.etAutor);
		etISBN = (EditText) findViewById(R.id.etISBN);
		etPrecio = (EditText) findViewById(R.id.etPrecio);

		Button bGuardar = (Button) findViewById(R.id.bGuardar);
		bGuardar.setOnClickListener(new GuardarDatos(this));

		// Edición de libro
		if (idLibro != null) {
			setTitle(getString(R.string.editar_libro));
			bGuardar.setText(R.string.actualizar);
			cargarDatos();
		}

	}

	private void cargarDatos() {
		Cursor cursorLibro = Libreria.getDBHelper()
				.getLibro(idLibro.intValue());
		startManagingCursor(cursorLibro);
		cursorLibro.moveToFirst();

		etTitulo.setText(cursorLibro.getString(cursorLibro
				.getColumnIndexOrThrow(LibreriaDBHelper.TITULO)));
		etAutor.setText(cursorLibro.getString(cursorLibro
				.getColumnIndexOrThrow(LibreriaDBHelper.AUTOR)));
		etISBN.setText(cursorLibro.getString(cursorLibro
				.getColumnIndexOrThrow(LibreriaDBHelper.ISBN)));
		etPrecio.setText(Float.toString(cursorLibro.getFloat(cursorLibro
				.getColumnIndexOrThrow(LibreriaDBHelper.PRECIO))));
	}

	public Long getIdLibro() {
		return idLibro;
	}

	public String getTitulo() throws CampoVacioException {
		String valor = etTitulo.getText().toString().trim();
		if ("".equals(valor)) {
			throw new CampoVacioException(getString(R.string.no_valor) + " "
					+ getString(R.string.titulo));
		}
		return valor;
	}

	public String getAutor() throws CampoVacioException {
		String valor = etAutor.getText().toString().trim();
		if ("".equals(valor)) {
			throw new CampoVacioException(getString(R.string.no_valor) + " "
					+ getString(R.string.autor));
		}
		return valor;
	}

	public String getISBN() throws CampoVacioException {
		String valor = etISBN.getText().toString().trim();
		if ("".equals(valor)) {
			throw new CampoVacioException(getString(R.string.no_valor) + " "
					+ getString(R.string.isbn));
		}
		return valor;
	}

	public float getPrecio() throws CampoVacioException, NumberFormatException {
		String valor = etPrecio.getText().toString().trim();
		if ("".equals(valor)) {
			throw new CampoVacioException(getString(R.string.no_valor) + " "
					+ getString(R.string.precio));
		}
		float valorF;
		try {
			valorF = Float.parseFloat(etPrecio.getText().toString());
		} catch (NumberFormatException nfe) {
			throw new NumberFormatException(getString(R.string.no_numerico)
					+ " " + getString(R.string.precio));
		}
		return valorF;
	}

	public void limpiarDatos() {
		etTitulo.setText("");
		etAutor.setText("");
		etISBN.setText("");
		etPrecio.setText("");
	}

}
