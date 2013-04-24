package atorsoft.ahorcado.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import atorsoft.ahorcado.activity.Ahorcado;

/**
 * Clase para la gestión de conexiones Bluetooth con otros dispositivos. Realiza
 * la gestión mediante 3 clases de thread: - AcceptThread: Parte servidor para
 * gestión de conexiones entrantes. - ConnectThread: Parte cliente para conexión
 * con dispositivos remotos. - ConexionThread: Thread para la transmisión de
 * datos entre dispositivos conectados.
 */
public class AhorcadoService {
	// Depuración
	private static final String TAG = "AhorcadoService";
	private static final boolean D = false;

	// Nombre del SDP para la creación de BluetoothServerSocket
	private static final String NAME_SECURE = "AhorcadoSecure";
	private static final String NAME_INSECURE = "AhorcadoInsecure";

	// UUID único para la aplicación
	private static final UUID UUID_SECURE = UUID
			.fromString("139dc531-22b1-42c5-a7ab-a5278ee66a74");
	private static final UUID UUID_INSECURE = UUID
			.fromString("4ba6b73f-dc9e-4401-8298-c54d7f14540a");

	// Adaptador local de Bluetooth
	private final BluetoothAdapter mBtAdapter;

	// Handler para la recepción de mensajes
	private final Handler mHandler;

	// Threads de gestión de conexiones
	private AcceptThread mSecureAcceptThread;
	private AcceptThread mInsecureAcceptThread;
	private ConnectThread mConnectThread;
	private ConexionThread mConexionThread;

	// Estado de la conexión
	private int mEstado;

	// Constantes para el estado de la conexión
	public static final int ESTADO_PARADA = 0; // Detenido
	public static final int ESTADO_EJECUCION = 1; // Escuchando conexiones
													// entrantes
	public static final int ESTADO_CONECTANDO = 2; // Iniciando conexión con
													// dispositivo remoto
	public static final int ESTADO_CONECTADO = 3; // Conectado a dispositivo
													// remoto

	/**
	 * Constructor. Prepara la sesión de Ahorcado.
	 * 
	 * @param context
	 *            Actividad principal
	 * @param handler
	 *            Handler para el envío de mensajes a la actividad principal
	 */
	public AhorcadoService(Context context, Handler handler) {
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		mEstado = ESTADO_PARADA;
		mHandler = handler;
	}

	/**
	 * Establece el estado actual de la conexión
	 * 
	 * @param estado
	 *            Entero que define el estado de la conexión
	 */
	private synchronized void setEstado(int estado) {
		if (D)
			Log.d(TAG, "setEstado() " + mEstado + " -> " + estado);
		mEstado = estado;

		// Envía el estado a la actividad principal
		mHandler.obtainMessage(Ahorcado.MENSAJE_CAMBIO_ESTADO, estado, -1)
				.sendToTarget();
	}

	/**
	 * Devuelve el estado actual de la conexión.
	 */
	public synchronized int getEstado() {
		return mEstado;
	}

	/**
	 * Inicia el servicio. Ejecuta el AcceptThread para comenzar la escucha de
	 * conexiones entrantes. Este método se llama desde el método onResume() de
	 * la actividad principal
	 */
	public synchronized void iniciar() {
		if (D)
			Log.d(TAG, "Iniciar");

		// Cancela cualquier thread que intente establecer conexión
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// Cancela cualquier thread que estuviera conectado
		if (mConexionThread != null) {
			mConexionThread.cancel();
			mConexionThread = null;
		}

		setEstado(ESTADO_EJECUCION);

		// Ejecución de AcceptThread (modo seguro y modo inseguro)
		if (mSecureAcceptThread == null) {
			mSecureAcceptThread = new AcceptThread(true);
			mSecureAcceptThread.start();
		}
		if (mInsecureAcceptThread == null) {
			mInsecureAcceptThread = new AcceptThread(false);
			mInsecureAcceptThread.start();
		}
	}

	/**
	 * Ejecución de ConnectThread para iniciar conexión con dispositivo remoto.
	 * 
	 * @param dispositivo
	 *            Dispositivo al que conectarse
	 * @param seguridad
	 *            Tipo de socket - Secure (true) , Insecure (false)
	 */
	public synchronized void conectar(BluetoothDevice dispositivo,
			boolean seguridad) {
		if (D)
			Log.d(TAG, "Conectar a: " + dispositivo.getName());

		// Cancela cualquier thread que intente establecer conexión
		if (mEstado == ESTADO_CONECTANDO) {
			if (mConnectThread != null) {
				mConnectThread.cancel();
				mConnectThread = null;
			}
		}

		// Cancela cualquier thread que estuviera conectado
		if (mConexionThread != null) {
			mConexionThread.cancel();
			mConexionThread = null;
		}

		// Ejecución del ConnectThread para establecer la conexión con el
		// dispositivo remoto
		mConnectThread = new ConnectThread(dispositivo, seguridad);
		mConnectThread.start();
		setEstado(ESTADO_CONECTANDO);
	}

	/**
	 * Ejecución de ConexionThread para el intercambio de datos
	 * 
	 * @param socket
	 *            BluetoothSocket que representa la conexión
	 * @param dispositivo
	 *            BluetoothDevice con el que se ha conectado
	 * @param tipoSocket
	 *            Tipo de socket: Secure - Insecure
	 */
	public synchronized void establecerConexion(BluetoothSocket socket,
			BluetoothDevice dispositivo, final String tipoSocket) {
		if (D)
			Log.d(TAG, "EstablecerConexion [" + tipoSocket + "]");

		// Cancela el thread que ha realizado la conexión
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// Cancela cualquier thread que estuviera conectado
		if (mConexionThread != null) {
			mConexionThread.cancel();
			mConexionThread = null;
		}

		// Cancela los AcceptThread (sólo habrá conexión con un dispositivo)
		if (mSecureAcceptThread != null) {
			mSecureAcceptThread.cancel();
			mSecureAcceptThread = null;
		}
		if (mInsecureAcceptThread != null) {
			mInsecureAcceptThread.cancel();
			mInsecureAcceptThread = null;
		}

		// Ejecución de ConnectedThread para el intercambio de datos
		mConexionThread = new ConexionThread(socket, tipoSocket);
		mConexionThread.start();

		// Envío del nombre del dispositivo conectado a la actividad principal
		Message msg = mHandler
				.obtainMessage(Ahorcado.MENSAJE_NOMBRE_DISPOSITIVO);
		Bundle bundle = new Bundle();
		bundle.putString(Ahorcado.NOMBRE_DISPOSITIVO, dispositivo.getName());
		msg.setData(bundle);
		mHandler.sendMessage(msg);

		setEstado(ESTADO_CONECTADO);
	}

	/**
	 * Parada de todos los threads
	 */
	public synchronized void parar() {
		if (D)
			Log.d(TAG, "Parar");

		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		if (mConexionThread != null) {
			mConexionThread.cancel();
			mConexionThread = null;
		}

		if (mSecureAcceptThread != null) {
			mSecureAcceptThread.cancel();
			mSecureAcceptThread = null;
		}

		if (mInsecureAcceptThread != null) {
			mInsecureAcceptThread.cancel();
			mInsecureAcceptThread = null;
		}
		setEstado(ESTADO_PARADA);
	}

	/**
	 * Escritura en ConexionThread en modo asíncrono
	 * 
	 * @param out
	 *            Datos a escribir
	 * @see ConexionThread#escribir(byte[])
	 */
	public void escribir(byte[] out) {
		// ConnectedThread temporal
		ConexionThread r;
		// Obtiene de forma sincronizada una copia de ConexionThread
		synchronized (this) {
			if (mEstado != ESTADO_CONECTADO)
				return;
			r = mConexionThread;
		}
		// Escritura asíncrona
		r.escribir(out);
	}

	/**
	 * Notifica fallo de conexión a la actividad principal.
	 */
	private void conexionFallida() {
		// Envío de mensaje de fallo de conexión a la actividad principal
		Message msg = mHandler.obtainMessage(Ahorcado.MENSAJE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(Ahorcado.TOAST,
				"Imposible conectar con el dispositivo");
		msg.setData(bundle);
		mHandler.sendMessage(msg);

		// Reinicia el servicio
		AhorcadoService.this.iniciar();
	}

	/**
	 * Notifica pérdida de conexión a la actividad principal.
	 */
	private void conexionPerdida() {
		// Envío de mensaje de pérdida de conexión a la actividad principal
		Message msg = mHandler.obtainMessage(Ahorcado.MENSAJE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(Ahorcado.TOAST, "Conexión con dispositivo perdida");
		msg.setData(bundle);
		mHandler.sendMessage(msg);

		// Reinicia el servicio
		AhorcadoService.this.iniciar();
	}

	/**
	 * Thread para la gestión de conexiones entrantes. Representa la parte
	 * servidor de la aplicación. Se ejecuta hasta que la conexión es aceptada
	 * (o cancelada).
	 */
	private class AcceptThread extends Thread {
		// ServerSocket local
		private final BluetoothServerSocket mServerSocket;
		private String mDescripcionTipoSocket;

		public AcceptThread(boolean seguro) {
			BluetoothServerSocket tmpServerSocket = null;
			mDescripcionTipoSocket = seguro ? "Secure" : "Insecure";

			// Creación de ServerSocket
			try {
				if (seguro) {
					tmpServerSocket = mBtAdapter
							.listenUsingRfcommWithServiceRecord(NAME_SECURE,
									UUID_SECURE);
				} else {
					tmpServerSocket = mBtAdapter
							.listenUsingInsecureRfcommWithServiceRecord(
									NAME_INSECURE, UUID_INSECURE);
				}
			} catch (IOException e) {
				Log.e(TAG, "Error en la creación de ServerSocket ["
						+ mDescripcionTipoSocket + "]", e);
			}
			mServerSocket = tmpServerSocket;
		}

		public void run() {
			if (D)
				Log.d(TAG, "INICIO mAcceptThread [" + mDescripcionTipoSocket
						+ "]");

			setName("AcceptThread" + mDescripcionTipoSocket);

			BluetoothSocket socket = null;

			// Escucha mientras no haya conexión establecida
			while (mEstado != ESTADO_CONECTADO) {
				try {
					// Llamada bloqueante (sólo se detiene con una conexión
					// válida o una excepción)
					socket = mServerSocket.accept();
				} catch (IOException e) {
					Log.e(TAG, "Error en Accept() [" + mDescripcionTipoSocket
							+ "]", e);
					break;
				}

				// Conexión entrante
				if (socket != null) {
					synchronized (AhorcadoService.this) {
						switch (mEstado) {
						case ESTADO_EJECUCION:
						case ESTADO_CONECTANDO:
							// Ejecución de ConnectedThread para el intercambio
							// de datos
							establecerConexion(socket,
									socket.getRemoteDevice(),
									mDescripcionTipoSocket);
							break;
						case ESTADO_PARADA:
						case ESTADO_CONECTADO:
							// No disponible o ya conectado: cierre de socket
							try {
								socket.close();
							} catch (IOException e) {
								Log.e(TAG, "Error en cierre de socket", e);
							}
							break;
						}
					}
				}
			}
			if (D)
				Log.d(TAG, "FIN mAcceptThread [" + mDescripcionTipoSocket + "]");

		}

		public void cancel() {
			if (D)
				Log.d(TAG, "Cancel [" + mDescripcionTipoSocket + "]");
			try {
				mServerSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "Error en cierre de ServerSocket ["
						+ mDescripcionTipoSocket + "]", e);
			}
		}
	}

	/**
	 * Thread para la conexión con dispositivos remotos. Representa la parte
	 * cliente de la aplicación. Se ejecuta hasta que la conexión se establece o
	 * falla.
	 */
	private class ConnectThread extends Thread {
		private final BluetoothSocket mSocket;
		private final BluetoothDevice mDispositivoRemoto;
		private String mDescripcionTipoSocket;

		public ConnectThread(BluetoothDevice device, boolean secure) {
			mDispositivoRemoto = device;
			BluetoothSocket tmpSocket = null;
			mDescripcionTipoSocket = secure ? "Secure" : "Insecure";

			// Obtención de socket para la conexión con el dispositivo remoto
			try {
				if (secure) {
					tmpSocket = device
							.createRfcommSocketToServiceRecord(UUID_SECURE);
				} else {
					tmpSocket = device
							.createInsecureRfcommSocketToServiceRecord(UUID_INSECURE);
				}
			} catch (IOException e) {
				Log.e(TAG, "Error en la creación de Socket ["
						+ mDescripcionTipoSocket + "]", e);
			}
			mSocket = tmpSocket;
		}

		public void run() {
			Log.i(TAG, "INICIO mConnectThread [" + mDescripcionTipoSocket + "]");

			setName("ConnectThread" + mDescripcionTipoSocket);

			// Cancelación de la búsqueda de dispositivos para mejora de
			// rendimiento
			mBtAdapter.cancelDiscovery();

			// Conexión con el BluetoothSocket
			try {
				// Llamada bloqueante (sólo se detiene con una conexión
				// válida o una excepción)
				mSocket.connect();
			} catch (IOException e) {
				// Cierre de socket
				try {
					mSocket.close();
				} catch (IOException e2) {
					Log.e(TAG,
							"Error en cierre de socket durante un fallo de conexión ["
									+ mDescripcionTipoSocket + "]", e2);
				}
				conexionFallida();
				return;
			}

			// Reset de ConnectThread porque ya se ha establecido la conexión
			synchronized (AhorcadoService.this) {
				mConnectThread = null;
			}

			// Ejecución de ConexionThread para el intercambio de datos
			establecerConexion(mSocket, mDispositivoRemoto,
					mDescripcionTipoSocket);
		}

		public void cancel() {
			try {
				mSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "Error en cierre de socket ["
						+ mDescripcionTipoSocket + "]", e);
			}
		}
	}

	/**
	 * Thread para el intercambio de datos entre dispositivos conectados.
	 */
	private class ConexionThread extends Thread {
		private final BluetoothSocket mSocket;
		private final InputStream mInStream;
		private final OutputStream mOutStream;

		public ConexionThread(BluetoothSocket socket, String socketType) {
			Log.d(TAG, "Creación de ConexionThread [" + socketType + "]");
			mSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Obtención de InputStream y OutputStream de BluetoothSocket
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				Log.e(TAG,
						"Error en la obtención de inputStream/outputStream del socket de conexión",
						e);
			}

			mInStream = tmpIn;
			mOutStream = tmpOut;
		}

		public void run() {
			Log.i(TAG, "INICIO mConexionThread");
			byte[] buffer = new byte[1024];
			int bytes;

			// Lectura continua de InputStream durante la conexión
			while (true) {
				try {
					// Lectura de datos
					bytes = mInStream.read(buffer);

					// Envía los datos leídos a la actividad principal
					mHandler.obtainMessage(Ahorcado.MENSAJE_LECTURA, bytes, -1,
							buffer).sendToTarget();

				} catch (IOException e) {
					Log.e(TAG, "Desconectado", e);
					conexionPerdida();
					break;
				}
			}
		}

		/**
		 * Escritura en el OutputStream.
		 * 
		 * @param buffer
		 *            Bytes a escribir
		 */
		public void escribir(byte[] buffer) {
			try {
				mOutStream.write(buffer);
			} catch (IOException e) {
				Log.e(TAG, "Error en la escritura", e);
			}
		}

		public void cancel() {
			try {
				mSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "Error en cierre de socket", e);
			}
		}
	}
}
