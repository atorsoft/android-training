package atorsoft.agenda.listener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import atorsoft.agenda.activity.NuevoContacto;
import atorsoft.agenda.activity.VerContactos;
import atorsoft.agenda.javabeans.ContactoBean;

import atorsoft.agenda.R;

public class GestorAgenda implements OnClickListener {

	private static final String FICHERO_AGENDA = "agenda.txt";

	private NuevoContacto iNuevoContacto;

	public GestorAgenda(NuevoContacto iNuevoContacto) {
		this.iNuevoContacto = iNuevoContacto;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.botonAnadir:
			Long telefono = iNuevoContacto.getTelefono();
			String email = iNuevoContacto.getEmail();
			String nombre = iNuevoContacto.getNombre();
			if (nombre != null && email != null && telefono != null) {
				anadirContacto(new ContactoBean(nombre, email, telefono));
			}
			break;
		case R.id.botonVerContactos:
			verContactos();
			break;
		default:
			break;
		}

	}

	private void anadirContacto(ContactoBean contacto) {
		OutputStreamWriter out;
		try {
			out = new OutputStreamWriter(iNuevoContacto.openFileOutput(
					FICHERO_AGENDA, Context.MODE_APPEND));
			out.write(contacto.toString() + "\n");
			out.flush();
			out.close();
			String mensaje = iNuevoContacto.getResources().getString(
					R.string.contacto_anadido)
					+ ":\n";
			mensaje += contacto.toString();
			iNuevoContacto.mostrarAlerta(mensaje);
			iNuevoContacto.limpiarCampos();
		} catch (Throwable t) {
			iNuevoContacto.mostrarAlerta("Error: " + t.getLocalizedMessage());
		}

	}

	private void verContactos() {
		InputStreamReader in;
		try {
			in = new InputStreamReader(iNuevoContacto.openFileInput(FICHERO_AGENDA));
			BufferedReader buff = new BufferedReader(in);
			String strTmp = null;
			StringBuffer strBuff = new StringBuffer();
			while ((strTmp = buff.readLine()) != null) {
				strBuff.append(strTmp + "\n");
			}
			in.close();

			Intent intent = new Intent(iNuevoContacto, VerContactos.class);
			intent.putExtra("textoContactos", strBuff.toString());
			iNuevoContacto.startActivity(intent);
		} catch (Throwable t) {
			iNuevoContacto.mostrarAlerta("Error: " + t.getLocalizedMessage());
		}
	}
}
