package atorsoft.ahorcado.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import atorsoft.ahorcado.gestor.GestorPartida;
import atorsoft.ahorcado.service.AhorcadoService;

import atorsoft.ahorcado.R;

public class Ahorcado extends Activity {
	// Depuración
	private static final String TAG = "Ahorcado";
	private static final boolean D = false;

	// Códigos de Intent
	private static final int REQUEST_HABILITAR_BLUETOOTH = 1;
	private static final int REQUEST_CONECTAR_DISPOSITIVO_SEGURO = 2;
	private static final int REQUEST_CONECTAR_DISPOSITIVO_INSEGURO = 3;

	// Constantes que representan los tipos de mensaje que envía el servicio a
	// la actividad principal.
	// Se gestionan en el Handler
	public static final int MENSAJE_CAMBIO_ESTADO = 1;
	public static final int MENSAJE_NOMBRE_DISPOSITIVO = 2;
	public static final int MENSAJE_LECTURA = 3;
	public static final int MENSAJE_TOAST = 4;

	// Claves recibidas desde el Handler
	public static final String NOMBRE_DISPOSITIVO = "nombre_dispositivo";
	public static final String TOAST = "toast";

	// Constantes para el envío y procesado de mensajes
	private static final String SEPARADOR_TAG = "#";
	private static final int TAG_INICIO = 1;
	private static final int TAG_LETRA = 2;
	private static final int TAG_OK = 3;
	private static final int TAG_ERROR = 4;

	// Modos de juego
	private static final int JUGADOR_1 = 1;
	private static final int JUGADOR_2 = 2;

	// Componentes del layout
	private TextView mTitulo;
	private LinearLayout mLayoutInicio;
	private TextView mTvIncognita;
	private TextView mTvLetraJug1;
	private LinearLayout mLayoutLetraJug2;
	private TextView mTvIntentos;
	private TextView mTvSolucion;
	private Button mNuevaPartida;

	// Gestor de partida
	private GestorPartida mGestorPartida;

	// Nombre del dispositivo conectado
	private String mNombreDispositivoConectado = null;

	// Adaptador local de Bluetooth
	private BluetoothAdapter mBtAdapter = null;

	// Objeto del servicio
	private AhorcadoService mAhorcadoService = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D)
			Log.d(TAG, "+++ ON CREATE +++");

		// Layout
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.ahorcado);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.titulo_personalizado);

		// Título personalizado (muestra a la izquierda el nombre de la
		// aplicación y a la derecha el estado de la sesión de chat)
		mTitulo = (TextView) findViewById(R.id.titulo_izquierda);
		mTitulo.setText(R.string.app_name);
		mTitulo = (TextView) findViewById(R.id.titulo_derecha);

		// Obtención de BluetoothAdapter
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();

		// Si el adaptador es nulo el dispositivo no soporta Bluetooth
		if (mBtAdapter == null) {
			Toast.makeText(this, "Bluetooth no disponible", Toast.LENGTH_LONG)
					.show();
			finish();
			return;
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.d(TAG, "++ ON START ++");

		// Si el Bluetooth no está habilitado, se solicita al usuario la
		// activación mediante una actividad Android.
		// iniciarAhorcado() se llamará desde el método onActivityResult
		if (!mBtAdapter.isEnabled()) {
			Intent intentRequestEnable = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(intentRequestEnable,
					REQUEST_HABILITAR_BLUETOOTH);
			// Configuración de la sesión de chat
		} else {
			if (mAhorcadoService == null)
				iniciarAhorcado();
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (D)
			Log.d(TAG, "+ ON RESUME +");

		// Se realiza esta comprobación en onResume() para cubrir el caso en el
		// que el Bluetooth no estaba habilitado en onStart().
		// onResume() se llamará cuando la actividad de ACTION_REQUEST_ENABLE
		// devuelva el resultado.
		if (mAhorcadoService != null) {
			// Si el estado es ESTADO_PARADA se inicia el servicio
			if (mAhorcadoService.getEstado() == AhorcadoService.ESTADO_PARADA) {
				mAhorcadoService.iniciar();
			}
		}
	}

	private void iniciarAhorcado() {
		if (D)
			Log.d(TAG, "iniciarAhorcado()");

		// Componentes del layout
		mLayoutInicio = (LinearLayout) findViewById(R.id.layoutInicio);
		mTvIncognita = (TextView) findViewById(R.id.tvIncognita);
		mTvLetraJug1 = (TextView) findViewById(R.id.tvLetraJug1);
		mLayoutLetraJug2 = (LinearLayout) findViewById(R.id.layoutLetraJug2);
		mTvIntentos = (TextView) findViewById(R.id.tvIntentos);
		mTvSolucion = (TextView) findViewById(R.id.tvSolucion);
		mNuevaPartida = (Button) findViewById(R.id.bNuevaPartida);

		// Listener del botón de comunicar
		Button botonComunicar = (Button) findViewById(R.id.bComunicar);
		botonComunicar.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				iniciarPartida();
			}
		});

		// Inicialización de AhorcadoService
		mAhorcadoService = new AhorcadoService(this, mHandler);
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		if (D)
			Log.d(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D)
			Log.d(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Parar servicio
		if (mAhorcadoService != null)
			mAhorcadoService.parar();
		if (D)
			Log.d(TAG, "--- ON DESTROY ---");
	}

	private void hacerDispositivoVisible() {
		if (D)
			Log.d(TAG, "HacerDispositivoVisible");

		if (mBtAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent intentRequestDiscoverable = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			intentRequestDiscoverable.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(intentRequestDiscoverable);
		}
	}

	/**
	 * Comprobación de conexión.
	 */
	private boolean hayConexion() {
		if (mAhorcadoService.getEstado() != AhorcadoService.ESTADO_CONECTADO) {
			Toast.makeText(this, R.string.no_conectado, Toast.LENGTH_SHORT)
					.show();
			return false;
		}
		return true;
	}

	/**
	 * Inicio de la partida.
	 */
	private void iniciarPartida() {
		// Comprobamos que hay conexión
		if (!hayConexion())
			return;

		// Comprobamos que el campo de frase no esté vacío
		String frase = ((EditText) findViewById(R.id.etFrase)).getText()
				.toString().trim();
		if ("".equals(frase)) {
			Toast.makeText(this, R.string.no_frase, Toast.LENGTH_SHORT).show();
			return;
		}

		mGestorPartida = new GestorPartida(frase);
		String incognita = mGestorPartida.getIncognita();

		if (enviarMensaje(TAG_INICIO, incognita))
			configurarPartida(incognita, JUGADOR_1);
	}

	/**
	 * Configuración de la partida.
	 * 
	 * @param incognita
	 *            Frase a adivinar.
	 * @param modo
	 *            Modo de juego (Jugador 1 - Jugador 2).
	 */
	private void configurarPartida(String incognita, int modo) {
		mLayoutInicio.setVisibility(View.GONE);
		mNuevaPartida.setVisibility(View.GONE);

		setIncognita(incognita);
		mTvIncognita.setVisibility(View.VISIBLE);
		setIntentos(String.valueOf(GestorPartida.MAX_INTENTOS));
		mTvIntentos.setVisibility(View.VISIBLE);

		switch (modo) {
		case JUGADOR_1:
			mTvLetraJug1.setText("");
			mTvLetraJug1.setVisibility(View.VISIBLE);
			mLayoutLetraJug2.setVisibility(View.GONE);
			setSolucion(mGestorPartida.getSolucion());
			mTvSolucion.setVisibility(View.VISIBLE);
			break;
		case JUGADOR_2:
			mTvLetraJug1.setVisibility(View.GONE);
			mLayoutLetraJug2.setVisibility(View.VISIBLE);
			mTvSolucion.setVisibility(View.GONE);
			// Listener del botón de enviar
			Button bEnviar = (Button) findViewById(R.id.bEnviar);
			bEnviar.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// Comprobamos que el campo de letra no esté vacío
					EditText etLetra = (EditText) findViewById(R.id.etLetra);
					String letra = etLetra.getText().toString().trim();
					if (!"".equals(letra)) {
						if (enviarMensaje(TAG_LETRA, letra))
							etLetra.setText("");
					} else {
						Toast.makeText(v.getContext(), R.string.no_letra,
								Toast.LENGTH_SHORT).show();
					}
				}
			});
			break;
		}
	}

	private void setIncognita(String valor) {
		mTvIncognita.setText(valor);
	}

	private void setIntentos(String valor) {
		String intentos = getResources().getString(R.string.intentos) + " "
				+ valor;
		mTvIntentos.setText(intentos);
	}

	private void setLetraJug1(String valor, String resultado) {
		String letra = getResources().getString(R.string.letra) + " " + valor
				+ " >> " + resultado;
		mTvLetraJug1.setText(letra);
	}

	private void setSolucion(String valor) {
		String solucion = getResources().getString(R.string.solucion) + " "
				+ valor;
		mTvSolucion.setText(solucion);
	}

	/**
	 * Envío de mensaje.
	 * 
	 * @param tag
	 *            Tipo de mensaje a enviar.
	 * @param texto
	 *            Texto a enviar.
	 */
	private boolean enviarMensaje(int tag, String texto) {

		String mensaje = tag + SEPARADOR_TAG + texto;

		if (D)
			Log.i(TAG, "ENVIAR MENSAJE: " + mensaje);

		// Comprobamos que realmente hay conexión y que hay algo que enviar
		if (hayConexion() && (texto.length() > 0)) {
			// Obtención de bytes del mensaje y envío mediante el servicio
			byte[] bytesMensaje = mensaje.getBytes();
			mAhorcadoService.escribir(bytesMensaje);
			return true;
		}
		return false;
	}

	/**
	 * Procesar mensaje recibido.
	 * 
	 * @param mensaje
	 *            Mensaje recibido.
	 */
	private void procesarMensaje(String mensaje) {
		if (D)
			Log.i(TAG, "PROCESAR MENSAJE: " + mensaje);

		int tag = -1;
		String texto = "";
		try {
			String[] cadenas = mensaje.split(SEPARADOR_TAG);
			tag = Integer.parseInt(cadenas[0]);
			texto = cadenas[1].toUpperCase();
		} catch (Exception e) {
			Log.e(TAG, "Error en mensaje recibido: " + mensaje);
			return;
		}

		switch (tag) {
		case TAG_LETRA:
			if (mGestorPartida != null) {
				String resultado = "";
				if (mGestorPartida.comprobarLetra(texto)) {
					resultado = mGestorPartida.getIncognita();
					if (enviarMensaje(TAG_OK, resultado)) {
						setLetraJug1(texto,
								getResources().getString(R.string.acierto));
						setIncognita(resultado);
						comprobarFinPartida(TAG_OK, resultado);
					}
				} else {
					resultado = String.valueOf(mGestorPartida.getIntentos());
					if (enviarMensaje(TAG_ERROR, resultado)) {
						setLetraJug1(texto,
								getResources().getString(R.string.fallo));
						setIntentos(resultado);
						comprobarFinPartida(TAG_ERROR, resultado);
					}
				}
			}
			break;
		case TAG_OK:
			setIncognita(texto);
			comprobarFinPartida(tag, texto);
			break;
		case TAG_ERROR:
			setIntentos(texto);
			comprobarFinPartida(tag, texto);
			break;
		case TAG_INICIO:
			configurarPartida(texto, JUGADOR_2);
			break;
		}
	}

	/**
	 * Comprobación de fin de partida.
	 * 
	 * @param tag
	 *            Tipo de fin de partida.
	 * @param resultado
	 *            Resultado de la partida.
	 */
	private void comprobarFinPartida(int tag, String resultado) {
		boolean fin = false;
		String mensaje = "";

		switch (tag) {
		case TAG_OK:
			if (!resultado.contains(GestorPartida.X)) {
				mensaje = getResources()
						.getString(R.string.fin_partida_acierto);
				fin = true;
			}
			break;
		case TAG_ERROR:
			if (Integer.parseInt(resultado) == 0) {
				mensaje = getResources().getString(R.string.fin_partida_fallo);
				fin = true;
			}
			break;
		}

		if (fin) {
			mLayoutLetraJug2.setVisibility(View.INVISIBLE);
			Toast.makeText(
					this,
					getResources().getString(R.string.fin_partida) + " "
							+ mensaje, Toast.LENGTH_SHORT).show();

			// Botón de nueva partida
			mNuevaPartida.setVisibility(View.VISIBLE);
			mNuevaPartida.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					nuevaPartida();
				}
			});
		}
	}

	/**
	 * Prepara la actividad para una nueva partida.
	 */
	private void nuevaPartida() {
		mTvIncognita.setVisibility(View.INVISIBLE);
		mTvLetraJug1.setVisibility(View.INVISIBLE);
		mLayoutLetraJug2.setVisibility(View.INVISIBLE);
		mTvIntentos.setVisibility(View.INVISIBLE);
		mTvSolucion.setVisibility(View.INVISIBLE);
		mNuevaPartida.setVisibility(View.INVISIBLE);

		((EditText) findViewById(R.id.etFrase)).setText("");
		mLayoutInicio.setVisibility(View.VISIBLE);
	}

	// Handler que gestiona la información de AhorcadoService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message mensaje) {
			switch (mensaje.what) {
			case MENSAJE_CAMBIO_ESTADO:
				if (D)
					Log.i(TAG, "MENSAJE_CAMBIO_ESTADO: " + mensaje.arg1);
				switch (mensaje.arg1) {
				case AhorcadoService.ESTADO_CONECTADO:
					mTitulo.setText(R.string.estado_conectado);
					mTitulo.append(mNombreDispositivoConectado);
					break;
				case AhorcadoService.ESTADO_CONECTANDO:
					mTitulo.setText(R.string.estado_conectando);
					break;
				case AhorcadoService.ESTADO_EJECUCION:
				case AhorcadoService.ESTADO_PARADA:
					mTitulo.setText(R.string.estado_no_conectado);
					break;
				}
				break;
			case MENSAJE_LECTURA:
				byte[] bytesLectura = (byte[]) mensaje.obj;
				// Construcción de String a partir del array de bytes
				String mensajeLectura = new String(bytesLectura, 0,
						mensaje.arg1);
				procesarMensaje(mensajeLectura);
				break;
			case MENSAJE_NOMBRE_DISPOSITIVO:
				// Guardar el nombre del dispositivo conectado
				mNombreDispositivoConectado = mensaje.getData().getString(
						NOMBRE_DISPOSITIVO);
				Toast.makeText(getApplicationContext(),
						"Conectado a " + mNombreDispositivoConectado,
						Toast.LENGTH_SHORT).show();
				break;
			case MENSAJE_TOAST:
				Toast.makeText(getApplicationContext(),
						mensaje.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);

		switch (requestCode) {
		case REQUEST_CONECTAR_DISPOSITIVO_SEGURO:
			// Si ListaDispositivos devuelve el dispositivo seleccionado
			// (Seguro)
			if (resultCode == Activity.RESULT_OK) {
				conectarDispositivo(intent, true);
			}
			break;
		case REQUEST_CONECTAR_DISPOSITIVO_INSEGURO:
			// Si ListaDispositivos devuelve el dispositivo seleccionado
			// (Inseguro)
			if (resultCode == Activity.RESULT_OK) {
				conectarDispositivo(intent, false);
			}
			break;
		case REQUEST_HABILITAR_BLUETOOTH:
			// Retorno de la solicitud de activación de Bluetooth
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth habilitado: iniciar Ahorcado
				iniciarAhorcado();
			} else {
				// El usuario no ha habilitado Bluetooth o se ha producido algún
				// error
				Log.w(TAG, "Bluetooth no habilitado");
				Toast.makeText(this, R.string.bt_no_habilitado,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	private void conectarDispositivo(Intent intent, boolean seguridad) {
		// Obtención de la dirección MAC del dispositivo
		String direccionMAC = intent.getExtras().getString(
				ListaDispositivos.EXTRA_DIRECCION_MAC);
		// Obtención de objeto BluetoothDevice
		BluetoothDevice dispositivo = mBtAdapter.getRemoteDevice(direccionMAC);
		// Conexión con el dispositivo
		mAhorcadoService.conectar(dispositivo, seguridad);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_opciones, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent serverIntent = null;
		switch (item.getItemId()) {
		case R.id.conectar_dispositivo_seguro:
			// Lanza la actividad ListaDispositivos para mostrar los
			// dispositivos y realizar la búsqueda (modo Seguro)
			serverIntent = new Intent(this, ListaDispositivos.class);
			startActivityForResult(serverIntent,
					REQUEST_CONECTAR_DISPOSITIVO_SEGURO);
			return true;
		case R.id.conectar_dispositivo_inseguro:
			// Lanza la actividad ListaDispositivos para mostrar los
			// dispositivos y realizar la búsqueda (modo Inseguro)
			serverIntent = new Intent(this, ListaDispositivos.class);
			startActivityForResult(serverIntent,
					REQUEST_CONECTAR_DISPOSITIVO_INSEGURO);
			return true;
		case R.id.hacer_dispositivo_visible:
			hacerDispositivoVisible();
			return true;
		}
		return false;
	}

}
