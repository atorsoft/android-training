package atorsoft.calculadora.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import atorsoft.calculadora.R;

public class Resultado extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.resultado);

		setResultado(getIntent().getExtras().getString("textoResultado"));

	}

	private void setResultado(String texto) {
		((TextView) findViewById(R.id.tvResultado)).setText(texto);
	}

}
