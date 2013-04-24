package atorsoft.ahorcado.gestor;

public class GestorPartida {

	public static final int MAX_INTENTOS = 5;
	public static final String X = "_";

	private String solucion;
	private String[] incognita;
	private int intentos;

	public GestorPartida(String frase) {
		solucion = frase.toUpperCase();
		// Generación de incógnita
		int longitud = solucion.length();
		incognita = new String[longitud];
		for (int i = 0; i < longitud; i++) {
			if (solucion.charAt(i) == ' ') {
				setIncognita(i, "  ");
			} else {
				setIncognita(i, X + " ");
			}
		}
		intentos = MAX_INTENTOS;

	}

	private void setIncognita(int pos, String valor) {
		this.incognita[pos] = valor;
	}

	public String getSolucion() {
		return solucion;
	}

	public String getIncognita() {
		StringBuilder sbIncognita = new StringBuilder();
		for (int i = 0; i < incognita.length; i++) {
			sbIncognita.append(incognita[i]);
		}
		return sbIncognita.toString();
	}

	public int getIntentos() {
		return intentos;
	}

	public boolean comprobarLetra(String letra) {
		boolean acierto = false;
		char c = letra.toUpperCase().charAt(0);
		for (int i = 0; i < solucion.length(); i++) {
			if (solucion.charAt(i) == c) {
				setIncognita(i, c + " ");
				acierto = true;
			}
		}
		if (!acierto)
			intentos--;

		return acierto;
	}

}