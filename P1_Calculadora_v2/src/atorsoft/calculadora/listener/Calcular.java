package atorsoft.calculadora.listener;

import java.math.BigDecimal;
import java.math.MathContext;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import atorsoft.calculadora.activity.Calculadora;
import atorsoft.calculadora.activity.Resultado;

import atorsoft.calculadora.R;

public class Calcular implements OnClickListener {
	
	private static final BigDecimal CERO = new BigDecimal(0);
	private Calculadora calculadora;

	public Calcular(Calculadora calculadora) {
		this.calculadora = calculadora;
	}

	@Override
	public void onClick(View v) {
		
		String sNum2 = calculadora.getNumero2();
		String sNum1 = calculadora.getNumero1();
		
		if (sNum1 != null && sNum2 != null) {

			BigDecimal num1 = new BigDecimal(sNum1);
			BigDecimal num2 = new BigDecimal(sNum2);
		
			String simbolo = null;
			BigDecimal resultado = null;
			switch (v.getId()) {
			case R.id.bSumar:
				simbolo = calculadora.getResources().getString(R.string.suma);
				resultado = sumar(num1,num2);
				break;
			case R.id.bRestar:
				simbolo = calculadora.getResources().getString(R.string.resta);
				resultado = restar(num1,num2);
				break;
			case R.id.bMultiplicar:
				simbolo = calculadora.getResources().getString(R.string.multiplicacion);
				resultado = multiplicar(num1,num2);
				break;
			case R.id.bDividir:
				simbolo = calculadora.getResources().getString(R.string.division);
				resultado = dividir(num1,num2);
				break;
			}
			String textoResultado = "";
			if (simbolo != null && resultado != null) {
				textoResultado = formatearResultado(num1, num2,	simbolo, resultado);
			} else if (simbolo == null){
				textoResultado = calculadora.getResources().getString(R.string.noOperacion);
			} else if (resultado == null){
				textoResultado = calculadora.getResources().getString(R.string.noResultado);
			}
			mostrarResultado(textoResultado);
		}
	}

	private BigDecimal sumar(BigDecimal num1, BigDecimal num2) {
		return (num1).add(num2);
	}
	
	private BigDecimal restar(BigDecimal num1, BigDecimal num2) {
		return (num1).subtract(num2);
	}

	private BigDecimal multiplicar(BigDecimal num1, BigDecimal num2) {
		return (num1).multiply(num2);
	}

	private BigDecimal dividir(BigDecimal num1, BigDecimal num2) {
		if (!num2.equals(CERO)) {
			return (num1).divide(num2,MathContext.DECIMAL64);	
		} else {
			return null;
		}
	}
	
	private String formatearResultado(BigDecimal num1, BigDecimal num2, String simbolo,	BigDecimal resultado) {
		String textoResultado = num1 + " " + simbolo;
		if (num2.compareTo(CERO) >= 0) {
			textoResultado += " " + num2;
		} else {
			textoResultado += " (" + num2 + ")";
		}
		textoResultado += " = " + resultado;
		return textoResultado;
	}
	
	private void mostrarResultado(String textoResultado) {
		Intent intent = new Intent(calculadora, Resultado.class);
		intent.putExtra("textoResultado", textoResultado);
		calculadora.startActivity(intent);
	}
	
}
