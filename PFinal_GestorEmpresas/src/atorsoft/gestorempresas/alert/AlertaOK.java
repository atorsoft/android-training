package atorsoft.gestorempresas.alert;

import android.app.AlertDialog;
import android.content.Context;

public class AlertaOK {

	public static void mostrar(Context contexto, String titulo, String mensaje) {
		AlertDialog.Builder alerta = new AlertDialog.Builder(contexto);
		if (titulo != null) {
			alerta.setTitle(titulo);
		}
		alerta.setMessage(mensaje);
		alerta.setPositiveButton(android.R.string.ok, null);
		alerta.show();
	}

}
