package atorsoft.agenda.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import atorsoft.agenda.R;

public class VerContactos extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ver_contactos);

		mostrarContactos(getIntent().getExtras().getString("textoContactos"));
	}

	private void mostrarContactos(String textoContactos) {
		((TextView) findViewById(R.id.etContactos)).setText(textoContactos);
	}
}
