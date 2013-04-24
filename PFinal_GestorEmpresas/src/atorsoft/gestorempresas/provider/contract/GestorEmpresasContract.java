package atorsoft.gestorempresas.provider.contract;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Definiciones para el proveedor de contenidos del gestor de empresas
 */
public class GestorEmpresasContract {

	public static final String AUTHORITY_CATEGORIAS = "atorsoft.gestorempresas.provider.categorias";
	public static final String AUTHORITY_EMPRESAS = "atorsoft.gestorempresas.provider.empresas";

	// Esta clase no puede instanciarse
	private GestorEmpresasContract() {
	}

	/**
	 * Tabla Categorias
	 */
	public static final class CategoriaColumnas implements BaseColumns {
		// Esta clase no puede instanciarse
		private CategoriaColumnas() {
		}

		/**
		 * URL de tipo content:// para la tabla Categorias
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY_CATEGORIAS + "/categorias");

		/**
		 * Tipo MIME de {@link #CONTENT_URI} para varios elementos
		 */
		public static final String CONTENT_DIR_MIME_TYPE = "vnd.android.cursor.dir/vnd.atorsoft.gestorempresas.provider.categoria";

		/**
		 * Tipo MIME de {@link #CONTENT_URI} para un único elemento
		 */
		public static final String CONTENT_ITEM_MIME_TYPE = "vnd.android.cursor.item/vnd.atorsoft.gestorempresas.provider.categoria";

		/**
		 * Columna de ordenación por defecto
		 */
		public static final String DEFAULT_SORT_ORDER = "nombre";

		/**
		 * Nombre de la categoria
		 * <P>
		 * Type: TEXT NOT NULL, UNIQUE
		 * </P>
		 */
		public static final String NOMBRE = "nombre";

	}

	/**
	 * Tabla Empresas
	 */
	public static final class EmpresaColumnas implements BaseColumns {
		// Esta clase no puede instanciarse
		private EmpresaColumnas() {
		}

		/**
		 * URL de tipo content:// para la tabla Empresas
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY_EMPRESAS + "/empresas");

		/**
		 * Tipo MIME de {@link #CONTENT_URI} para varios elementos
		 */
		public static final String CONTENT_DIR_MIME_TYPE = "vnd.android.cursor.dir/vnd.atorsoft.gestorempresas.provider.empresa";

		/**
		 * Tipo MIME de {@link #CONTENT_URI} para un único elemento
		 */
		public static final String CONTENT_ITEM_MIME_TYPE = "vnd.android.cursor.item/vnd.atorsoft.gestorempresas.provider.empresa";

		/**
		 * Columna de ordenación por defecto
		 */
		public static final String DEFAULT_SORT_ORDER = "nombre";

		/**
		 * Nombre de la empresa
		 * <P>
		 * Type: TEXT NOT NULL, UNIQUE
		 * </P>
		 */
		public static final String NOMBRE = "nombre";

		/**
		 * Dirección de la empresa
		 * <P>
		 * Type: TEXT NOT NULL
		 * </P>
		 */
		public static final String DIRECCION = "direccion";

		/**
		 * Email de la empresa
		 * <P>
		 * Type: TEXT NOT NULL
		 * </P>
		 */
		public static final String EMAIL = "email";

		/**
		 * Web de la empresa
		 * <P>
		 * Type: TEXT NOT NULL
		 * </P>
		 */
		public static final String WEB = "web";

		/**
		 * ID de la categoría a la que pertenece la empresa
		 * <P>
		 * Type: INTEGER NOT NULL, FOREIGN KEY
		 * </P>
		 */
		public static final String ID_CATEGORIA = "id_categoria";

		/**
		 * Nombre de la categoría a la que pertenece la empresa.</BR> Disponible
		 * únicamente en consultas.
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String CATEGORIA = "categoria";

	}

}
