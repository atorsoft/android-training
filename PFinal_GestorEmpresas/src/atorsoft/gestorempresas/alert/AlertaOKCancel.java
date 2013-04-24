package atorsoft.gestorempresas.alert;

import android.app.AlertDialog;
import android.content.Context;

public class AlertaOKCancel {

	public static void mostrar(Context contexto, String titulo, String mensaje,
			AlertDialog.OnClickListener clickOK,
			AlertDialog.OnClickListener clickCancel) {
		AlertDialog.Builder alerta = new AlertDialog.Builder(contexto);
		if (titulo != null) {
			alerta.setTitle(titulo);
		}
		alerta.setMessage(mensaje);
		alerta.setPositiveButton(android.R.string.ok, clickOK);
		alerta.setNegativeButton(android.R.string.cancel, clickCancel);
		alerta.show();
	}

}
