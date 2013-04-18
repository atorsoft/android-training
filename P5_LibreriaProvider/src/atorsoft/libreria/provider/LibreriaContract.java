package atorsoft.libreria.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Definiciones para LibreriaProvider
 */
public final class LibreriaContract {

	public static final String AUTHORITY = "atorsoft.libreria.provider";

	// Esta clase no puede instanciarse
	private LibreriaContract() {
	}

	/**
	 * Tabla Libreria
	 */
	public static final class LibroColumnas implements BaseColumns {
		// Esta clase no puede instanciarse
		private LibroColumnas() {
		}

		/**
		 * URL de tipo content:// para la tabla
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/libros");

		/**
		 * Tipo MIME de {@link #CONTENT_URI} para varios elementos
		 */
		public static final String CONTENT_DIR_MIME_TYPE = "vnd.android.cursor.dir/vnd.atorsoft.libreria.provider.libro";

		/**
		 * Tipo MIME de {@link #CONTENT_URI} para un único elemento
		 */
		public static final String CONTENT_ITEM_MIME_TYPE = "vnd.android.cursor.item/vnd.atorsoft.libreria.provider.libro";

		/**
		 * Columna de ordenación por defecto
		 */
		public static final String DEFAULT_SORT_ORDER = "titulo";

		/**
		 * Título del libro
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String TITULO = "titulo";

		/**
		 * Autor del libro
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String AUTOR = "autor";

		/**
		 * ISBN del libro
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String ISBN = "isbn";

		/**
		 * Precio del libro
		 * <P>
		 * Type: FLOAT
		 * </P>
		 */
		public static final String PRECIO = "precio";

	}

}
