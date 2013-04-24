package atorsoft.gestorempresas.provider;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Patterns;
import atorsoft.gestorempresas.provider.contract.GestorEmpresasContract;
import atorsoft.gestorempresas.provider.contract.GestorEmpresasContract.EmpresaColumnas;
import atorsoft.gestorempresas.provider.exception.CampoUnicoException;
import atorsoft.gestorempresas.provider.exception.ClaveForaneaException;
import atorsoft.gestorempresas.provider.javabean.EmpresaBean;


public class EmpresasProvider extends ContentProvider {

	// Acceso a base de datos
	private GestorEmpresasDatabase mGestorEmpresasDataBase;

	// UriMatcher
	private static final int EMPRESAS = 0;
	private static final int EMPRESA_ID = 1;

	private static final UriMatcher sUriMatcher;

	// Mapeo de columnas
	private static HashMap<String, String> sEmpresaProjectionMap;

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(GestorEmpresasContract.AUTHORITY_EMPRESAS,
				"empresas", EMPRESAS);
		sUriMatcher.addURI(GestorEmpresasContract.AUTHORITY_EMPRESAS,
				"empresas/#", EMPRESA_ID);

		sEmpresaProjectionMap = new HashMap<String, String>();
		sEmpresaProjectionMap.put(EmpresaColumnas._ID,
				GestorEmpresasDatabase._ID + " AS " + EmpresaColumnas._ID);
		sEmpresaProjectionMap
				.put(EmpresaColumnas.NOMBRE, GestorEmpresasDatabase.NOMBRE
						+ " AS " + EmpresaColumnas.NOMBRE);
		sEmpresaProjectionMap.put(EmpresaColumnas.DIRECCION,
				GestorEmpresasDatabase.DIRECCION + " AS "
						+ EmpresaColumnas.DIRECCION);
		sEmpresaProjectionMap.put(EmpresaColumnas.EMAIL,
				GestorEmpresasDatabase.EMAIL + " AS " + EmpresaColumnas.EMAIL);
		sEmpresaProjectionMap.put(EmpresaColumnas.WEB,
				GestorEmpresasDatabase.WEB + " AS " + EmpresaColumnas.WEB);
		sEmpresaProjectionMap.put(EmpresaColumnas.ID_CATEGORIA,
				GestorEmpresasDatabase.ID_CATEGORIA + " AS "
						+ EmpresaColumnas.ID_CATEGORIA);
		sEmpresaProjectionMap.put(EmpresaColumnas.CATEGORIA,
				GestorEmpresasDatabase.CATEGORIA + " AS "
						+ EmpresaColumnas.CATEGORIA);
	}

	@Override
	public boolean onCreate() {
		mGestorEmpresasDataBase = new GestorEmpresasDatabase(getContext());
		return true;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case EMPRESAS:
			return EmpresaColumnas.CONTENT_DIR_MIME_TYPE;
		case EMPRESA_ID:
			return EmpresaColumnas.CONTENT_ITEM_MIME_TYPE;
		default:
			throw new IllegalArgumentException("URL desconocida: " + uri);
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		// Columnas
		if (projection == null) {
			projection = sEmpresaProjectionMap.values()
					.toArray(new String[] {});
		}

		switch (sUriMatcher.match(uri)) {
		case EMPRESAS:
			break;
		case EMPRESA_ID:
			selection = GestorEmpresasDatabase._ID + "=?";
			selectionArgs = new String[] { uri.getLastPathSegment() };
			break;
		default:
			throw new IllegalArgumentException("URL desconocida: " + uri);
		}

		// Ordenación
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = EmpresaColumnas.DEFAULT_SORT_ORDER;
		} else {
			orderBy = sortOrder;
		}

		// Ejecución de consulta
		Cursor cursor = mGestorEmpresasDataBase.queryEmpresas(projection,
				selection, selectionArgs, null, null, orderBy);

		// Asignación de inspección de uri al cursor: le permite conocer cuándo
		// cambian los datos
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// Validación de Uri
		if (sUriMatcher.match(uri) != EMPRESAS) {
			throw new IllegalArgumentException("URI no válida: " + uri);
		}

		// Validación de valores
		if (values == null) {
			throw new IllegalArgumentException("Faltan valores");
		}

		// Validación de campos obligatorios de la tabla Empresas
		// Nombre
		String nombre = values.containsKey(EmpresaColumnas.NOMBRE) ? values
				.getAsString(EmpresaColumnas.NOMBRE).trim() : null;
		if (nombre == null || "".equals(nombre)) {
			throw new IllegalArgumentException("Campo obligatorio: "
					+ EmpresaColumnas.NOMBRE);
		}
		// Dirección
		String direccion = values.containsKey(EmpresaColumnas.DIRECCION) ? values
				.getAsString(EmpresaColumnas.DIRECCION).trim() : null;
		if (direccion == null || "".equals(direccion)) {
			throw new IllegalArgumentException("Campo obligatorio: "
					+ EmpresaColumnas.DIRECCION);
		}
		// Email
		String email = values.containsKey(EmpresaColumnas.EMAIL) ? values
				.getAsString(EmpresaColumnas.EMAIL).trim() : null;
		if (email == null || "".equals(email)) {
			throw new IllegalArgumentException("Campo obligatorio: "
					+ EmpresaColumnas.EMAIL);
		}
		// Web
		String web = values.containsKey(EmpresaColumnas.WEB) ? values
				.getAsString(EmpresaColumnas.WEB).trim() : null;
		if (web == null || "".equals(web)) {
			throw new IllegalArgumentException("Campo obligatorio: "
					+ EmpresaColumnas.WEB);
		}
		// Id Categoría
		Integer categoria = values.containsKey(EmpresaColumnas.ID_CATEGORIA) ? values
				.getAsInteger(EmpresaColumnas.ID_CATEGORIA) : null;
		if (categoria == null) {
			throw new IllegalArgumentException("Campo obligatorio: "
					+ EmpresaColumnas.ID_CATEGORIA);
		}

		// Validación de formato de campos especiales
		// Email
		if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
			throw new IllegalArgumentException("Campo con formato incorrecto: "
					+ EmpresaColumnas.EMAIL);
		}

		// Web
		if (!Patterns.WEB_URL.matcher(web).matches()) {
			throw new IllegalArgumentException("Campo con formato incorrecto: "
					+ EmpresaColumnas.WEB);
		}

		// Nueva empresa
		EmpresaBean empresa = new EmpresaBean(nombre, direccion, email, web,
				categoria);

		// Ejecución de inserción
		long rowId = 0;
		try {
			rowId = mGestorEmpresasDataBase.insertEmpresa(empresa);
		} catch (CampoUnicoException cue) {
			if (EmpresaColumnas.NOMBRE.equalsIgnoreCase(cue.getCampo())) {
				throw new SQLException("La empresa indicada ya existe");
			} else {
				throw new SQLException(cue.getMessage());
			}
		} catch (ClaveForaneaException cfe) {
			throw new SQLException("La categoría indicada no es válida");
		}

		// Notificación de cambio de dato
		if (rowId > 0) {
			Uri uriEmpresa = ContentUris.withAppendedId(
					EmpresaColumnas.CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(uriEmpresa, null);
			return uriEmpresa;
		}

		throw new SQLException("Error en la inserción de elemento: " + uri);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count;
		switch (sUriMatcher.match(uri)) {
		case EMPRESA_ID:
			String idEmpresa = uri.getLastPathSegment();
			try {
				long id = Long.parseLong(idEmpresa);
				count = mGestorEmpresasDataBase.deleteEmpresa(id);
			} catch (NumberFormatException nfe) {
				throw new IllegalArgumentException(
						"ID de elemento no numérico: " + idEmpresa);
			} catch (Exception e) {
				throw new SQLException("Error en la eliminación de elemento: "
						+ uri);
			}
			break;
		default:
			throw new IllegalArgumentException("URI no válida: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

}
