package atorsoft.gestorempresas.provider.exception;

public class CampoUnicoException extends Exception {

	private static final long serialVersionUID = 1L;

	private String tabla;
	private String campo;

	public CampoUnicoException(String tabla, String campo) {
		this(tabla, campo, "Excepción de campo único: " + tabla.toUpperCase()
				+ "." + campo.toUpperCase());
	}

	private CampoUnicoException(String tabla, String campo, String mensaje) {
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
