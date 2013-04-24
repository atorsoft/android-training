package atorsoft.gestorempresas.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import atorsoft.gestorempresas.provider.contract.GestorEmpresasContract.CategoriaColumnas;

import atorsoft.gestorempresas.R;

public class Busqueda extends Activity implements OnCheckedChangeListener,
		OnItemSelectedListener, OnClickListener {

	// Tipos de búsqueda
	public static final int POR_NOMBRE = 0;
	public static final int POR_CATEGORIA = 1;
	private int mTipoBusqueda;

	// Parámetros enviados a la actividad de resultados
	public static final String PARAMETRO_TIPO_BUSQUEDA = "tipo_busqueda";
	public static final String PARAMETRO_CRITERIO = "criterio";

	private LinearLayout mLayoutBusquedaNombre;
	private LinearLayout mLayoutBusquedaCategoria;
	private EditText mEtBusquedaNombre;
	private TextView mTvBusquedaIdCategoria;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_busqueda);

		((RadioGroup) findViewById(R.id.opcionBusqueda))
				.setOnCheckedChangeListener(this);

		mLayoutBusquedaNombre = (LinearLayout) findViewById(R.id.layoutBusquedaNombre);
		mLayoutBusquedaCategoria = (LinearLayout) findViewById(R.id.layoutBusquedaCategoria);
		mEtBusquedaNombre = (EditText) findViewById(R.id.etBusquedaNombre);
		mTvBusquedaIdCategoria = (TextView) findViewById(R.id.tvBusquedaIdCategoria);

		Spinner spinnerCategorias = (Spinner) findViewById(R.id.spBusquedaCategoria);
		cargarCategorias(spinnerCategorias);
		spinnerCategorias.setOnItemSelectedListener(this);

		((Button) findViewById(R.id.bBuscar)).setOnClickListener(this);
	}

	private void cargarCategorias(Spinner spinner) {
		// Cursor de categorías
		Cursor cursorCategorias = getContentResolver().query(
				CategoriaColumnas.CONTENT_URI, null, null, null, null);
		startManagingCursor(cursorCategorias);

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
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.rbNombre:
			mLayoutBusquedaCategoria.setVisibility(View.INVISIBLE);
			mLayoutBusquedaNombre.setVisibility(View.VISIBLE);
			mTipoBusqueda = POR_NOMBRE;
			break;
		case R.id.rbCategoria:
			mLayoutBusquedaNombre.setVisibility(View.INVISIBLE);
			mLayoutBusquedaCategoria.setVisibility(View.VISIBLE);
			mTipoBusqueda = POR_CATEGORIA;
			break;
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		mTvBusquedaIdCategoria.setText(String.valueOf(id));
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	@Override
	public void onClick(View v) {
		String criterio = null;
		switch (mTipoBusqueda) {
		case POR_NOMBRE:
			criterio = mEtBusquedaNombre.getText().toString().trim();
			break;
		case POR_CATEGORIA:
			criterio = mTvBusquedaIdCategoria.getText().toString().trim();
			break;
		}
		Intent intentResultados = new Intent(this, BusquedaResultados.class);
		intentResultados.putExtra(PARAMETRO_TIPO_BUSQUEDA, mTipoBusqueda);
		intentResultados.putExtra(PARAMETRO_CRITERIO, criterio);
		startActivity(intentResultados);
	}

}
