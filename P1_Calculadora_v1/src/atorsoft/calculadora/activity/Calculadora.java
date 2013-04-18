package atorsoft.calculadora.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import atorsoft.calculadora.R;
import atorsoft.calculadora.listener.Calcular;

public class Calculadora extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calculadora);

		Calcular calcular = new Calcular(this);
		((Button) findViewById(R.id.bSumar)).setOnClickListener(calcular);
		((Button) findViewById(R.id.bRestar)).setOnClickListener(calcular);
		((Button) findViewById(R.id.bMultiplicar)).setOnClickListener(calcular);
		((Button) findViewById(R.id.bDividir)).setOnClickListener(calcular);

	}

	public String getNumero1() {
		return comprobarCampoVacio((EditText) findViewById(R.id.etNum1));
	}

	public String getNumero2() {
		return comprobarCampoVacio((EditText) findViewById(R.id.etNum2));
	}

	private String comprobarCampoVacio(EditText campo) {
		String valor = campo.getText().toString().trim();
		if (!"".equals(valor)) {
			return valor;
		} else {
			String mensaje = "";
			switch (campo.getId()) {
			case R.id.etNum1:
				mensaje += getResources().getString(R.string.num1) + " ";
				break;
			case R.id.etNum2:
				mensaje += getResources().getString(R.string.num2) + " ";
				break;
			}
			mensaje += getResources().getString(R.string.noValor).toString();
			mostrarAlerta(mensaje);
			return null;
		}
	}

	private void mostrarAlerta(String mensaje) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setMessage(mensaje);
		alert.setPositiveButton(android.R.string.ok, null);
		alert.show();
	}

	public void setResultado(String texto) {
		((TextView) findViewById(R.id.tvResultado)).setText(texto);
	}

}
