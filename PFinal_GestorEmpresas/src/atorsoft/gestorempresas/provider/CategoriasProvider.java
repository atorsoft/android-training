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
import atorsoft.gestorempresas.provider.contract.GestorEmpresasContract;
import atorsoft.gestorempresas.provider.contract.GestorEmpresasContract.CategoriaColumnas;
import atorsoft.gestorempresas.provider.exception.CampoUnicoException;
import atorsoft.gestorempresas.provider.exception.ClaveForaneaException;
import atorsoft.gestorempresas.provider.javabean.CategoriaBean;


public class CategoriasProvider extends ContentProvider {

	// Acceso a base de datos
	private GestorEmpresasDatabase mGestorEmpresasDataBase;

	// UriMatcher
	private static final int CATEGORIAS = 0;
	private static final int CATEGORIA_ID = 1;

	private static final UriMatcher sUriMatcher;

	// Mapeo de columnas
	private static HashMap<String, String> sCategoriaProjectionMap;

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(GestorEmpresasContract.AUTHORITY_CATEGORIAS,
				"categorias", CATEGORIAS);
		sUriMatcher.addURI(GestorEmpresasContract.AUTHORITY_CATEGORIAS,
				"categorias/#", CATEGORIA_ID);

		sCategoriaProjectionMap = new HashMap<String, String>();
		sCategoriaProjectionMap.put(CategoriaColumnas._ID,
				GestorEmpresasDatabase._ID + " AS " + CategoriaColumnas._ID);
		sCategoriaProjectionMap.put(CategoriaColumnas.NOMBRE,
				GestorEmpresasDatabase.NOMBRE + " AS "
						+ CategoriaColumnas.NOMBRE);
	}

	@Override
	public boolean onCreate() {
		mGestorEmpresasDataBase = new GestorEmpresasDatabase(getContext());
		return true;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case CATEGORIAS:
			return CategoriaColumnas.CONTENT_DIR_MIME_TYPE;
		case CATEGORIA_ID:
			return CategoriaColumnas.CONTENT_ITEM_MIME_TYPE;
		default:
			throw new IllegalArgumentException("URL desconocida: " + uri);
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		// Columnas
		if (projection == null) {
			projection = sCategoriaProjectionMap.values().toArray(
					new String[] {});
		}

		switch (sUriMatcher.match(uri)) {
		case CATEGORIAS:
			break;
		case CATEGORIA_ID:
			selection = GestorEmpresasDatabase._ID + "=?";
			selectionArgs = new String[] { uri.getLastPathSegment() };
			break;
		default:
			throw new IllegalArgumentException("URL desconocida: " + uri);
		}

		// Ordenación
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = CategoriaColumnas.DEFAULT_SORT_ORDER;
		} else {
			orderBy = sortOrder;
		}

		// Ejecución de consulta
		Cursor cursor = mGestorEmpresasDataBase.queryCategorias(projection,
				selection, selectionArgs, null, null, orderBy);

		// Asignación de inspección de uri al cursor: le permite conocer cuándo
		// cambian los datos
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// Validación de Uri
		if (sUriMatcher.match(uri) != CATEGORIAS) {
			throw new IllegalArgumentException("URI no válida: " + uri);
		}

		// Validación de valores
		if (values == null) {
			throw new IllegalArgumentException("Faltan valores");
		}

		// Validación de campos obligatorios de la tabla Categorias
		// Nombre
		String nombre = values.containsKey(CategoriaColumnas.NOMBRE) ? values
				.getAsString(CategoriaColumnas.NOMBRE).trim() : null;
		if (nombre == null || "".equals(nombre)) {
			throw new IllegalArgumentException("Campo obligatorio: "
					+ CategoriaColumnas.NOMBRE);
		}

		// Nueva categoría
		CategoriaBean categoria = new CategoriaBean(nombre);

		// Ejecución de inserción
		long rowId = 0;
		try {
			rowId = mGestorEmpresasDataBase.insertCategoria(categoria);
		} catch (CampoUnicoException cue) {
			if (CategoriaColumnas.NOMBRE.equalsIgnoreCase(cue.getCampo())) {
				throw new SQLException("La categoría indicada ya existe");
			} else {
				throw new SQLException(cue.getMessage());
			}
		}

		// Notificación de cambio de dato
		if (rowId > 0) {
			Uri uriCategoria = ContentUris.withAppendedId(
					CategoriaColumnas.CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(uriCategoria, null);
			return uriCategoria;
		}

		throw new SQLException("Error en la inserción de elemento: " + uri);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count;
		switch (sUriMatcher.match(uri)) {
		case CATEGORIA_ID:
			String idCategoria = uri.getLastPathSegment();
			try {
				long id = Long.parseLong(idCategoria);
				count = mGestorEmpresasDataBase.deleteCategoria(id);
			} catch (NumberFormatException nfe) {
				throw new IllegalArgumentException(
						"ID de elemento no numérico: " + idCategoria);
			} catch (ClaveForaneaException cfe) {
				if (cfe.getTabla().equalsIgnoreCase(
						GestorEmpresasDatabase.TABLE_EMPRESAS)) {
					throw new SQLException(
							"No es posible eliminar la categoría, existen empresas asociadas.");
				} else {
					throw new SQLException(cfe.getMessage());
				}
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
