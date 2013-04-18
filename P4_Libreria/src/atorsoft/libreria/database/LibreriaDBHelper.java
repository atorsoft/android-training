package atorsoft.libreria.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import atorsoft.libreria.javabean.LibroBean;

public class LibreriaDBHelper {

	private Context mContexto = null;

	private LibreriaOpenHelper mLibreriaOpenHelper = null;
	private SQLiteDatabase mLibreriaDB = null;

	// Base de Datos
	private static final String DATABASE_NAME = "libreria";
	private static final int DATABASE_VERSION = 2;

	// Tabla y campos
	private static final String DATABASE_TABLE = "libreria";
	public static final String _ID = "_id";
	public static final String ISBN = "isbn";
	public static final String TITULO = "titulo";
	public static final String AUTOR = "autor";
	public static final String PRECIO = "precio";
	private static final String[] columnas = new String[] { _ID, ISBN, TITULO,
			AUTOR, PRECIO };

	// SQL de creación de la tabla
	private static final String DATABASE_CREATE_TABLE = "create table "
			+ DATABASE_TABLE + " (" + _ID + " integer primary key," + ISBN
			+ " text not null, " + TITULO + " text not null, " + AUTOR
			+ " text not null, " + PRECIO + " float not null)";

	public LibreriaDBHelper(Context contexto) {
		this.mContexto = contexto;
	}

	public LibreriaDBHelper abrir() throws SQLException {
		mLibreriaOpenHelper = new LibreriaOpenHelper(mContexto);
		mLibreriaDB = mLibreriaOpenHelper.getWritableDatabase();
		return this;
	}

	public void cerrar() {
		mLibreriaOpenHelper.close();
	}

	// Obtener todos los libros
	public Cursor getLibros() {
		return mLibreriaDB.query(DATABASE_TABLE, columnas, null, null, null,
				null, TITULO);
	}

	// Obtener libro
	public Cursor getLibro(long idLibro) {
		return mLibreriaDB.query(DATABASE_TABLE, columnas, _ID + "=?",
				new String[] { Long.toString(idLibro) }, null, null, null);
	}

	// Añadir libro
	public long insertLibro(LibroBean libro) {
		return mLibreriaDB.insert(DATABASE_TABLE, null, obtenerValores(libro));
	}

	// Editar libro
	public int updateLibro(long idLibro, LibroBean libro) {
		return mLibreriaDB.update(DATABASE_TABLE, obtenerValores(libro), _ID
				+ "=?", new String[] { Long.toString(idLibro) });
	}

	// Eliminar libro
	public int deleteLibro(long idLibro) {
		return mLibreriaDB.delete(DATABASE_TABLE, _ID + "=?",
				new String[] { Long.toString(idLibro) });
	}

	private ContentValues obtenerValores(LibroBean libro) {
		ContentValues valores = new ContentValues();
		valores.put(ISBN, libro.getIsbn());
		valores.put(TITULO, libro.getTitulo());
		valores.put(AUTOR, libro.getAutor());
		valores.put(PRECIO, libro.getPrecio());
		return valores;
	}

	private static class LibreriaOpenHelper extends SQLiteOpenHelper {

		public LibreriaOpenHelper(Context contexto) {
			super(contexto, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			crearTablas(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			eliminarTablas(db);
			crearTablas(db);
		}

		private void crearTablas(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE_TABLE);
		}

		private void eliminarTablas(SQLiteDatabase db) {
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
		}

	}

}
