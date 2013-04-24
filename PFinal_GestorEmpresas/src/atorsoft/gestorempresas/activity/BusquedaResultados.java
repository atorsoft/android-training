package atorsoft.gestorempresas.activity;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import atorsoft.gestorempresas.provider.contract.GestorEmpresasContract.EmpresaColumnas;

import atorsoft.gestorempresas.R;

public class BusquedaResultados extends ListActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listado_empresas);

		int tipoBusqueda = getIntent().getIntExtra(
				Busqueda.PARAMETRO_TIPO_BUSQUEDA, -1);
		String criterio = getIntent().getStringExtra(
				Busqueda.PARAMETRO_CRITERIO);

		cargarLista(buscarEmpresas(tipoBusqueda, criterio));
	}

	private Cursor buscarEmpresas(int tipoBusqueda, String criterio) {
		String selection = null;
		String[] selectionArgs = null;
		switch (tipoBusqueda) {
		case Busqueda.POR_NOMBRE:
			if (!"".equals(criterio.trim()) && criterio != null) {
				selection = "UPPER(" + EmpresaColumnas.NOMBRE + ") LIKE ?";
				criterio = "%" + criterio + "%";
				selectionArgs = new String[] { criterio.toUpperCase() };
			}
			break;
		case Busqueda.POR_CATEGORIA:
			selection = EmpresaColumnas.ID_CATEGORIA + "=?";
			selectionArgs = new String[] { criterio };
			break;
		}

		// Consulta de empresas mediante proveedor de contenido
		Cursor cursorEmpresas = getContentResolver().query(
				EmpresaColumnas.CONTENT_URI, null, selection, selectionArgs,
				null);

		return cursorEmpresas;
	}

	private void cargarLista(Cursor cursorEmpresas) {
		startManagingCursor(cursorEmpresas);
		// Adaptador de categorías
		ListAdapter adapterEmpresas = new SimpleCursorAdapter(this,
				R.layout.list_row_empresa, cursorEmpresas, new String[] {
						EmpresaColumnas.NOMBRE, EmpresaColumnas.DIRECCION,
						EmpresaColumnas.EMAIL, EmpresaColumnas.WEB,
						EmpresaColumnas.CATEGORIA }, new int[] {
						R.id.row_nombre, R.id.row_direccion, R.id.row_email,
						R.id.row_web, R.id.row_categoria });

		// Asignación del adaptador
		setListAdapter(adapterEmpresas);
	}
}
