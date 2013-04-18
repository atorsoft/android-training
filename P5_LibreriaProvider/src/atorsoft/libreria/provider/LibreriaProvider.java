package atorsoft.libreria.provider;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import atorsoft.libreria.database.LibreriaDBHelper;
import atorsoft.libreria.provider.LibreriaContract.LibroColumnas;


public class LibreriaProvider extends ContentProvider {

	// Acceso a base de datos
	private LibreriaDBHelper mLibreriaDBHelper;

	// UriMatcher
	private static final int LIBROS = 0;
	private static final int LIBRO_ID = 1;
	private static final UriMatcher sUriMatcher;

	// Mapeo de columnas
	private static HashMap<String, String> sLibroProjectionMap;

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(LibreriaContract.AUTHORITY, "libros", LIBROS);
		sUriMatcher.addURI(LibreriaContract.AUTHORITY, "libros/#", LIBRO_ID);

		sLibroProjectionMap = new HashMap<String, String>();
		sLibroProjectionMap.put(LibroColumnas._ID, LibreriaDBHelper._ID
				+ " AS " + LibroColumnas._ID);
		sLibroProjectionMap.put(LibroColumnas.TITULO, LibreriaDBHelper.TITULO
				+ " AS " + LibroColumnas.TITULO);
		sLibroProjectionMap.put(LibroColumnas.AUTOR, LibreriaDBHelper.AUTOR
				+ " AS " + LibroColumnas.AUTOR);
		sLibroProjectionMap.put(LibroColumnas.ISBN, LibreriaDBHelper.ISBN
				+ " AS " + LibroColumnas.ISBN);
		sLibroProjectionMap.put(LibroColumnas.PRECIO, LibreriaDBHelper.PRECIO
				+ " AS " + LibroColumnas.PRECIO);
	}

	@Override
	public boolean onCreate() {
		mLibreriaDBHelper = new LibreriaDBHelper(getContext());
		return true;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case LIBROS:
			return LibroColumnas.CONTENT_DIR_MIME_TYPE;
		case LIBRO_ID:
			return LibroColumnas.CONTENT_ITEM_MIME_TYPE;
		default:
			throw new IllegalArgumentException("URL desconocida: " + uri);
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		// Columnas
		if (projection == null) {
			projection = sLibroProjectionMap.values().toArray(new String[] {});
		}

		switch (sUriMatcher.match(uri)) {
		case LIBROS:
			break;
		case LIBRO_ID:
			selection = LibreriaDBHelper._ID + "=?";
			selectionArgs = new String[] { uri.getLastPathSegment() };
			break;
		default:
			throw new IllegalArgumentException("URL desconocida: " + uri);
		}

		// Ordenación
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = LibroColumnas.DEFAULT_SORT_ORDER;
		} else {
			orderBy = sortOrder;
		}

		// Ejecución de consulta
		Cursor cursor = mLibreriaDBHelper.queryLibros(projection, selection,
				selectionArgs, null, null, orderBy);

		// Asignación de inspección de uri al cursor: le permite conocer cuándo
		// cambian los datos
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new UnsupportedOperationException("Operación no implementada");
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		throw new UnsupportedOperationException("Operación no implementada");
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException("Operación no implementada");
	}

}
