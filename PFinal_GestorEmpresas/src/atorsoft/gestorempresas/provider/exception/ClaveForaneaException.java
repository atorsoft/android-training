package atorsoft.gestorempresas.provider.exception;

public class ClaveForaneaException extends Exception {

	private static final long serialVersionUID = 1L;

	private String tabla;
	private String campo;

	public ClaveForaneaException(String tabla, String campo) {
		this(tabla, campo, "Excepci�n de clave for�nea: " + tabla.toUpperCase()
				+ "." + campo.toUpperCase());
	}

	private ClaveForaneaException(String tabla, String campo, String mensaje) {
		super(mensaje);
		this.tabla = tabla;
		this.campo = campo;
	}

	public String getTabla() {
		return tabla;
	}

	public String getCampo() {
		return campo;
	}

}
