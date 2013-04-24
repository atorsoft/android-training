package atorsoft.gestorempresas.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import atorsoft.gestorempresas.provider.exception.CampoUnicoException;
import atorsoft.gestorempresas.provider.exception.ClaveForaneaException;
import atorsoft.gestorempresas.provider.javabean.CategoriaBean;
import atorsoft.gestorempresas.provider.javabean.EmpresaBean;


public class GestorEmpresasDatabase {

	private DatabaseOpenHelper mDatabaseOpenHelper = null;

	// Base de Datos
	private static final String DATABASE_NAME = "empresas";
	private static final int DATABASE_VERSION = 2;

	// Tablas, trigger y vista
	protected static final String TABLE_EMPRESAS = "empresas";
	protected static final String TABLE_CATEGORIAS = "categorias";
	private static final String TRIGGER_FK_CATEGORIA = "fk_categoria";
	private static final String VIEW_EMPRESAS = "empresas_vw";

	// Campos
	protected static final String _ID = "_id";
	protected static final String NOMBRE = "nombre";
	protected static final String DIRECCION = "direccion";
	protected static final String EMAIL = "email";
	protected static final String WEB = "web";
	protected static final String ID_CATEGORIA = "id_categoria";

	// Alias
	protected static final String CATEGORIA = "categoria";

	// SQL de creación de la tabla Categorias
	private static final String CREATE_TABLE_CATEGORIAS = "CREATE TABLE "
			+ TABLE_CATEGORIAS + " (" + _ID + " INTEGER PRIMARY KEY," + NOMBRE
			+ " TEXT NOT NULL, UNIQUE(" + NOMBRE + " COLLATE NOCASE))";

	// SQL de creación de la tabla Empresas
	private static final String CREATE_TABLE_EMPRESAS = "CREATE TABLE "
			+ TABLE_EMPRESAS + " (" + _ID + " INTEGER PRIMARY KEY," + NOMBRE
			+ " TEXT NOT NULL, " + DIRECCION + " TEXT NOT NULL, " + EMAIL
			+ " TEXT NOT NULL, " + WEB + " TEXT NOT NULL, " + ID_CATEGORIA
			+ " INTEGER NOT NULL, FOREIGN KEY(" + ID_CATEGORIA
			+ ") REFERENCES " + TABLE_CATEGORIAS + " (" + _ID + "), UNIQUE("
			+ NOMBRE + " COLLATE NOCASE))";

	// SQL de creación del trigger de foreign key de categoría
	private static final String CREATE_TRIGGER_FK_CATEGORIA = "CREATE TRIGGER "
			+ TRIGGER_FK_CATEGORIA + " BEFORE INSERT ON " + TABLE_EMPRESAS
			+ " FOR EACH ROW BEGIN SELECT CASE WHEN ((SELECT " + _ID + " FROM "
			+ TABLE_CATEGORIAS + " WHERE " + _ID + "=NEW." + ID_CATEGORIA
			+ ") IS NULL) THEN RAISE (ABORT,'Foreing Key Violation') END; END";

	// SQL de creación de vista de empresas
	private static final String CREATE_VIEW_EMPRESAS = "CREATE VIEW "
			+ VIEW_EMPRESAS + " AS SELECT " + TABLE_EMPRESAS + "." + _ID + ", "
			+ TABLE_EMPRESAS + "." + NOMBRE + ", " + TABLE_EMPRESAS + "."
			+ DIRECCION + ", " + TABLE_EMPRESAS + "." + EMAIL + ", "
			+ TABLE_EMPRESAS + "." + WEB + ", " + TABLE_CATEGORIAS + "." + _ID
			+ " AS " + ID_CATEGORIA + ", " + TABLE_CATEGORIAS + "." + NOMBRE
			+ " AS " + CATEGORIA + " FROM " + TABLE_EMPRESAS + " JOIN "
			+ TABLE_CATEGORIAS + " ON " + TABLE_EMPRESAS + "." + ID_CATEGORIA
			+ "=" + TABLE_CATEGORIAS + "." + _ID;

	// Valores de operacion
	private static final int INSERT = 0;
	private static final int DELETE = 1;

	protected GestorEmpresasDatabase(Context contexto) {
		mDatabaseOpenHelper = new DatabaseOpenHelper(contexto);
	}

	// Obtener valores de tabla Categoria
	private ContentValues obtenerValores(CategoriaBean categoria) {
		ContentValues valores = new ContentValues();
		valores.put(NOMBRE, categoria.getNombre());
		return valores;
	}

	// Obtener valores de tabla Empresa
	private ContentValues obtenerValores(EmpresaBean empresa) {
		ContentValues valores = new ContentValues();
		valores.put(NOMBRE, empresa.getNombre());
		valores.put(DIRECCION, empresa.getDireccion());
		valores.put(EMAIL, empresa.getEmail());
		valores.put(WEB, empresa.getWeb());
		valores.put(ID_CATEGORIA, empresa.getCategoria());
		return valores;
	}

	// Validación de campos con UNIQUE CONSTRAINT
	private boolean validarCampoUnico(String tabla, String campo, String valor)
			throws CampoUnicoException {
		String selection = "UPPER(" + campo + ") =?";
		String[] selectionArgs = new String[] { valor.toUpperCase() };

		Cursor cursor = mDatabaseOpenHelper.getReadableDatabase().query(tabla,
				new String[] { campo }, selection, selectionArgs, null, null,
				null);

		if (cursor != null && cursor.getCount() > 0) {
			throw new CampoUnicoException(tabla, campo);
		}
		return true;
	}

	// Validación de campos con FOREIGN KEY
	private boolean validarClaveForanea(int operacion, String tabla,
			String campo, String valor) throws ClaveForaneaException {
		String selection = campo + "=?";
		String[] selectionArgs = new String[] { valor };

		Cursor cursor = mDatabaseOpenHelper.getReadableDatabase().query(tabla,
				new String[] { campo }, selection, selectionArgs, null, null,
				null);

		boolean lanzarExcepcion = (cursor != null && cursor.getCount() > 0);

		switch (operacion) {
		case INSERT:
			lanzarExcepcion = !lanzarExcepcion;
			break;
		case DELETE:
			break;
		default:
			lanzarExcepcion = false;
			break;
		}

		if (lanzarExcepcion) {
			throw new ClaveForaneaException(tabla, campo);
		}
		return true;
	}

	// Consultar categorias
	protected Cursor queryCategorias(String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy) {
		return mDatabaseOpenHelper.getReadableDatabase().query(
				TABLE_CATEGORIAS, columns, selection, selectionArgs, groupBy,
				having, orderBy);
	}

	// Consultar empresas
	protected Cursor queryEmpresas(String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy) {
		return mDatabaseOpenHelper.getReadableDatabase().query(VIEW_EMPRESAS,
				columns, selection, selectionArgs, groupBy, having, orderBy);
	}

	// Añadir categoria
	protected long insertCategoria(CategoriaBean categoria)
			throws CampoUnicoException {
		validarCampoUnico(TABLE_CATEGORIAS, NOMBRE, categoria.getNombre());
		return mDatabaseOpenHelper.getWritableDatabase().insert(
				TABLE_CATEGORIAS, null, obtenerValores(categoria));
	}

	// Añadir empresa
	protected long insertEmpresa(EmpresaBean empresa)
			throws CampoUnicoException, ClaveForaneaException {
		validarCampoUnico(TABLE_EMPRESAS, NOMBRE, empresa.getNombre());
		validarClaveForanea(INSERT, TABLE_CATEGORIAS, _ID,
				String.valueOf(empresa.getCategoria()));
		return mDatabaseOpenHelper.getWritableDatabase().insert(TABLE_EMPRESAS,
				null, obtenerValores(empresa));
	}

	// Eliminar categoria
	protected int deleteCategoria(long id) throws ClaveForaneaException {
		validarClaveForanea(DELETE, TABLE_EMPRESAS, ID_CATEGORIA,
				String.valueOf(id));
		return mDatabaseOpenHelper.getWritableDatabase().delete(
				TABLE_CATEGORIAS, _ID + "=?",
				new String[] { Long.toString(id) });
	}

	// Eliminar empresa
	protected int deleteEmpresa(long id) {
		return mDatabaseOpenHelper.getWritableDatabase().delete(TABLE_EMPRESAS,
				_ID + "=?", new String[] { Long.toString(id) });
	}

	private static class DatabaseOpenHelper extends SQLiteOpenHelper {

		public DatabaseOpenHelper(Context contexto) {
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
			db.execSQL(CREATE_TABLE_CATEGORIAS);
			db.execSQL(CREATE_TABLE_EMPRESAS);
			db.execSQL(CREATE_TRIGGER_FK_CATEGORIA);
			db.execSQL(CREATE_VIEW_EMPRESAS);
		}

		private void eliminarTablas(SQLiteDatabase db) {
			db.execSQL("DROP VIEW IF EXISTS " + VIEW_EMPRESAS);
			db.execSQL("DROP TRIGGER IF EXISTS " + TRIGGER_FK_CATEGORIA);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_EMPRESAS);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIAS);
		}

	}

}
