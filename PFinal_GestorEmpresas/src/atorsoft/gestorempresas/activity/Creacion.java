package atorsoft.gestorempresas.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import atorsoft.gestorempresas.alert.AlertaOK;
import atorsoft.gestorempresas.exception.CampoVacioException;
import atorsoft.gestorempresas.provider.contract.GestorEmpresasContract.CategoriaColumnas;
import atorsoft.gestorempresas.provider.contract.GestorEmpresasContract.EmpresaColumnas;

import atorsoft.gestorempresas.R;

public class Creacion extends Activity implements OnItemSelectedListener,
		OnClickListener {

	private EditText etNombre;
	private EditText etDireccion;
	private EditText etEmail;
	private EditText etWeb;
	private TextView tvIdCategoria;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_creacion);

		etNombre = (EditText) findViewById(R.id.etNombre);
		etDireccion = (EditText) findViewById(R.id.etDireccion);
		etEmail = (EditText) findViewById(R.id.etEmail);
		etWeb = (EditText) findViewById(R.id.etWeb);
		tvIdCategoria = (TextView) findViewById(R.id.tvIdCategoria);

		Spinner spinnerCategorias = (Spinner) findViewById(R.id.spCategoria);
		cargarCategorias(spinnerCategorias);
		spinnerCategorias.setOnItemSelectedListener(this);

		Button bGuardar = (Button) findViewById(R.id.bGuardar);
		bGuardar.setOnClickListener(this);
	}

	private void cargarCategorias(Spinner spinner) {
		// Consulta de categorías mediante proveedor de contenido
		Cursor cursorCategorias = getContentResolver().query(
				CategoriaColumnas.CONTENT_URI, null, null, null, null);
		startManagingCursor(cursorCategorias);

		// Mensaje de aviso si no hay categorías configuradas
		if (cursorCategorias == null || cursorCategorias.getCount() == 0) {
			AlertaOK.mostrar(this, getString(R.string.no_categorias),
					getString(R.string.alerta_admin_categorias));
			return;
		}

		// Adaptador de categorías
		SimpleCursorAdapter adapterCategorias = new SimpleCursorAdapter(this,
				android.R.layout.simple_spinner_item, cursorCategorias,
				new String[] { CategoriaColumnas.NOMBRE },
				new int[] { android.R.id.text1 });
		adapterCategorias
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// Asignación del adaptador al spinner
		spinner.setAdapter(adapterCategorias);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		tvIdCategoria.setText(String.valueOf(id));
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	private String getNombre() throws CampoVacioException {
		String valor = etNombre.getText().toString().trim();
		if ("".equals(valor)) {
			etNombre.requestFocus();
			throw new CampoVacioException(getString(R.string.no_valor) + " "
					+ getString(R.string.nombre));
		}
		return valor;
	}

	private String getDireccion() throws CampoVacioException {
		String valor = etDireccion.getText().toString().trim();
		if ("".equals(valor)) {
			etDireccion.requestFocus();
			throw new CampoVacioException(getString(R.string.no_valor) + " "
					+ getString(R.string.direccion));
		}
		return valor;
	}

	private String getEmail() throws CampoVacioException {
		String valor = etEmail.getText().toString().trim();
		if ("".equals(valor)) {
			etEmail.requestFocus();
			throw new CampoVacioException(getString(R.string.no_valor) + " "
					+ getString(R.string.email));
		}
		return valor;
	}

	private String getWeb() throws CampoVacioException {
		String valor = etWeb.getText().toString().trim();
		if ("".equals(valor)) {
			etWeb.requestFocus();
			throw new CampoVacioException(getString(R.string.no_valor) + " "
					+ getString(R.string.web));
		}
		return valor;
	}

	private String getCategoria() throws CampoVacioException {
		String valor = tvIdCategoria.getText().toString().trim();
		if ("".equals(valor)) {
			throw new CampoVacioException(getString(R.string.no_valor) + " "
					+ getString(R.string.categoria));
		}
		return valor;
	}

	@Override
	public void onClick(View v) {
		if (crearEmpresa()) {
			setResult(Activity.RESULT_OK);
			finish();
		}
	}

	private boolean crearEmpresa() {
		boolean resultado = false;
		try {
			ContentValues valores = new ContentValues();
			valores.put(EmpresaColumnas.NOMBRE, getNombre());
			valores.put(EmpresaColumnas.DIRECCION, getDireccion());
			valores.put(EmpresaColumnas.EMAIL, getEmail());
			valores.put(EmpresaColumnas.WEB, getWeb());
			valores.put(EmpresaColumnas.ID_CATEGORIA, getCategoria());

			// Inserción de elemento mediante proveedor de contenido
			getContentResolver().insert(EmpresaColumnas.CONTENT_URI, valores);

			resultado = true;
			Toast.makeText(this, getString(R.string.empresa_creada),
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			AlertaOK.mostrar(this, null, e.getMessage());
		}
		return resultado;
	}

}
