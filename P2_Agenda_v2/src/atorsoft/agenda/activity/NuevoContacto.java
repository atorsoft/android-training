package atorsoft.agenda.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import atorsoft.agenda.listener.GestorAgenda;

import atorsoft.agenda.R;

public class NuevoContacto extends Activity {

	private EditText etNombre;
	private EditText etEmail;
	private EditText etTelefono;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nuevo_contacto);

		etNombre = (EditText) findViewById(R.id.etNombre);
		etEmail = (EditText) findViewById(R.id.etEmail);
		etTelefono = (EditText) findViewById(R.id.etTelefono);

		GestorAgenda gestor = new GestorAgenda(this);
		((Button) findViewById(R.id.botonAnadir)).setOnClickListener(gestor);
		((Button) findViewById(R.id.botonVerContactos))
				.setOnClickListener(gestor);

	}

	public String getNombre() {
		return comprobarCampoVacio(etNombre);
	}

	public String getEmail() {
		return comprobarCampoVacio(etEmail);
	}

	public Long getTelefono() {
		String valorTelefono = comprobarCampoVacio(etTelefono);
		if (valorTelefono != null) {
			try {
				return Long.parseLong(valorTelefono);
			} catch (NumberFormatException nfe) {
				mostrarAlerta(getResources().getString(
						R.string.telefonoNoNumerico));
				return null;
			}
		} else
			return null;
	}

	private String comprobarCampoVacio(EditText campo) {
		String valor = campo.getText().toString().trim();
		if (!"".equals(valor)) {
			return valor;
		} else {
			String mensaje = "";
			switch (campo.getId()) {
			case R.id.etNombre:
				mensaje += getResources().getString(R.string.nombre) + " ";
				break;
			case R.id.etEmail:
				mensaje += getResources().getString(R.string.email) + " ";
				break;
			case R.id.etTelefono:
				mensaje += getResources().getString(R.string.telefono) + " ";
				break;
			}

			mensaje += getResources().getString(R.string.noValor).toString();
			mostrarAlerta(mensaje);
			return null;
		}
	}

	public void mostrarAlerta(String mensaje) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setMessage(mensaje);
		alert.setPositiveButton(android.R.string.ok, null);
		alert.show();
	}

	public void limpiarCampos() {
		etNombre.setText("");
		etEmail.setText("");
		etTelefono.setText("");
	}

}
