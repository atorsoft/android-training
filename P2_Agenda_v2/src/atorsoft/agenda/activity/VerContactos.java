package atorsoft.agenda.activity;

import java.util.ArrayList;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import atorsoft.agenda.R;

public class VerContactos extends ListActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ver_contactos);

		mostrarContactos(getIntent().getStringArrayListExtra("listaContactos"));
	}

	private void mostrarContactos(ArrayList<String> listaContactos) {
		setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, listaContactos));
	}
}
