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
 * Clase para la gesti�n de conexiones Bluetooth con otros dispositivos. Realiza
 * la gesti�n mediante 3 clases de thread: - AcceptThread: Parte servidor para
 * gesti�n de conexiones entrantes. - ConnectThread: Parte cliente para conexi�n
 * con dispositivos remotos. - ConexionThread: Thread para la transmisi�n de
 * datos entre dispositivos conectados.
 */
public class AhorcadoService {
	// Depuraci�n
	private static final String TAG = "AhorcadoService";
	private static final boolean D = false;

	// Nombre del SDP para la creaci�n de BluetoothServerSocket
	private static final String NAME_SECURE = "AhorcadoSecure";
	private static final String NAME_INSECURE = "AhorcadoInsecure";

	// UUID �nico para la aplicaci�n
	private static final UUID UUID_SECURE = UUID
			.fromString("139dc531-22b1-42c5-a7ab-a5278ee66a74");
	private static final UUID UUID_INSECURE = UUID
			.fromString("4ba6b73f-dc9e-4401-8298-c54d7f14540a");

	// Adaptador local de Bluetooth
	private final BluetoothAdapter mBtAdapter;

	// Handler para la recepci�n de mensajes
	private final Handler mHandler;

	// Threads de gesti�n de conexiones
	private AcceptThread mSecureAcceptThread;
	private AcceptThread mInsecureAcceptThread;
	private ConnectThread mConnectThread;
	private ConexionThread mConexionThread;

	// Estado de la conexi�n
	private int mEstado;

	// Constantes para el estado de la conexi�n
	public static final int ESTADO_PARADA = 0; // Detenido
	public static final int ESTADO_EJECUCION = 1; // Escuchando conexiones
													// entrantes
	public static final int ESTADO_CONECTANDO = 2; // Iniciando conexi�n con
													// dispositivo remoto
	public static final int ESTADO_CONECTADO = 3; // Conectado a dispositivo
													// remoto

	/**
	 * Constructor. Prepara la sesi�n de Ahorcado.
	 * 
	 * @param context
	 *            Actividad principal
	 * @param handler
	 *            Handler para el env�o de mensajes a la actividad principal
	 */
	public AhorcadoService(Context context, Handler handler) {
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		mEstado = ESTADO_PARADA;
		mHandler = handler;
	}

	/**
	 * Establece el estado actual de la conexi�n
	 * 
	 * @param estado
	 *            Entero que define el estado de la conexi�n
	 */
	private synchronized void setEstado(int estado) {
		if (D)
			Log.d(TAG, "setEstado() " + mEstado + " -> " + estado);
		mEstado = estado;

		// Env�a el estado a la actividad principal
		mHandler.obtainMessage(Ahorcado.MENSAJE_CAMBIO_ESTADO, estado, -1)
				.sendToTarget();
	}

	/**
	 * Devuelve el estado actual de la conexi�n.
	 */
	public synchronized int getEstado() {
		return mEstado;
	}

	/**
	 * Inicia el servicio. Ejecuta el AcceptThread para comenzar la escucha de
	 * conexiones entrantes. Este m�todo se llama desde el m�todo onResume() de
	 * la actividad principal
	 */
	public synchronized void iniciar() {
		if (D)
			Log.d(TAG, "Iniciar");

		// Cancela cualquier thread que intente establecer conexi�n
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

		// Ejecuci�n de AcceptThread (modo seguro y modo inseguro)
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
	 * Ejecuci�n de ConnectThread para iniciar conexi�n con dispositivo remoto.
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

		// Cancela cualquier thread que intente establecer conexi�n
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

		// Ejecuci�n del ConnectThread para establecer la conexi�n con el
		// dispositivo remoto
		mConnectThread = new ConnectThread(dispositivo, seguridad);
		mConnectThread.start();
		setEstado(ESTADO_CONECTANDO);
	}

	/**
	 * Ejecuci�n de ConexionThread para el intercambio de datos
	 * 
	 * @param socket
	 *            BluetoothSocket que representa la conexi�n
	 * @param dispositivo
	 *            BluetoothDevice con el que se ha conectado
	 * @param tipoSocket
	 *            Tipo de socket: Secure - Insecure
	 */
	public synchronized void establecerConexion(BluetoothSocket socket,
			BluetoothDevice dispositivo, final String tipoSocket) {
		if (D)
			Log.d(TAG, "EstablecerConexion [" + tipoSocket + "]");

		// Cancela el thread que ha realizado la conexi�n
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// Cancela cualquier thread que estuviera conectado
		if (mConexionThread != null) {
			mConexionThread.cancel();
			mConexionThread = null;
		}

		// Cancela los AcceptThread (s�lo habr� conexi�n con un dispositivo)
		if (mSecureAcceptThread != null) {
			mSecureAcceptThread.cancel();
			mSecureAcceptThread = null;
		}
		if (mInsecureAcceptThread != null) {
			mInsecureAcceptThread.cancel();
			mInsecureAcceptThread = null;
		}

		// Ejecuci�n de ConnectedThread para el intercambio de datos
		mConexionThread = new ConexionThread(socket, tipoSocket);
		mConexionThread.start();

		// Env�o del nombre del dispositivo conectado a la actividad principal
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
	 * Escritura en ConexionThread en modo as�ncrono
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
		// Escritura as�ncrona
		r.escribir(out);
	}

	/**
	 * Notifica fallo de conexi�n a la actividad principal.
	 */
	private void conexionFallida() {
		// Env�o de mensaje de fallo de conexi�n a la actividad principal
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
	 * Notifica p�rdida de conexi�n a la actividad principal.
	 */
	private void conexionPerdida() {
		// Env�o de mensaje de p�rdida de conexi�n a la actividad principal
		Message msg = mHandler.obtainMessage(Ahorcado.MENSAJE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(Ahorcado.TOAST, "Conexi�n con dispositivo perdida");
		msg.setData(bundle);
		mHandler.sendMessage(msg);

		// Reinicia el servicio
		AhorcadoService.this.iniciar();
	}

	/**
	 * Thread para la gesti�n de conexiones entrantes. Representa la parte
	 * servidor de la aplicaci�n. Se ejecuta hasta que la conexi�n es aceptada
	 * (o cancelada).
	 */
	private class AcceptThread extends Thread {
		// ServerSocket local
		private final BluetoothServerSocket mServerSocket;
		private String mDescripcionTipoSocket;

		public AcceptThread(boolean seguro) {
			BluetoothServerSocket tmpServerSocket = null;
			mDescripcionTipoSocket = seguro ? "Secure" : "Insecure";

			// Creaci�n de ServerSocket
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
				Log.e(TAG, "Error en la creaci�n de ServerSocket ["
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

			// Escucha mientras no haya conexi�n establecida
			while (mEstado != ESTADO_CONECTADO) {
				try {
					// Llamada bloqueante (s�lo se detiene con una conexi�n
					// v�lida o una excepci�n)
					socket = mServerSocket.accept();
				} catch (IOException e) {
					Log.e(TAG, "Error en Accept() [" + mDescripcionTipoSocket
							+ "]", e);
					break;
				}

				// Conexi�n entrante
				if (socket != null) {
					synchronized (AhorcadoService.this) {
						switch (mEstado) {
						case ESTADO_EJECUCION:
						case ESTADO_CONECTANDO:
							// Ejecuci�n de ConnectedThread para el intercambio
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
	 * Thread para la conexi�n con dispositivos remotos. Representa la parte
	 * cliente de la aplicaci�n. Se ejecuta hasta que la conexi�n se establece o
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

			// Obtenci�n de socket para la conexi�n con el dispositivo remoto
			try {
				if (secure) {
					tmpSocket = device
							.createRfcommSocketToServiceRecord(UUID_SECURE);
				} else {
					tmpSocket = device
							.createInsecureRfcommSocketToServiceRecord(UUID_INSECURE);
				}
			} catch (IOException e) {
				Log.e(TAG, "Error en la creaci�n de Socket ["
						+ mDescripcionTipoSocket + "]", e);
			}
			mSocket = tmpSocket;
		}

		public void run() {
			Log.i(TAG, "INICIO mConnectThread [" + mDescripcionTipoSocket + "]");

			setName("ConnectThread" + mDescripcionTipoSocket);

			// Cancelaci�n de la b�squeda de dispositivos para mejora de
			// rendimiento
			mBtAdapter.cancelDiscovery();

			// Conexi�n con el BluetoothSocket
			try {
				// Llamada bloqueante (s�lo se detiene con una conexi�n
				// v�lida o una excepci�n)
				mSocket.connect();
			} catch (IOException e) {
				// Cierre de socket
				try {
					mSocket.close();
				} catch (IOException e2) {
					Log.e(TAG,
							"Error en cierre de socket durante un fallo de conexi�n ["
									+ mDescripcionTipoSocket + "]", e2);
				}
				conexionFallida();
				return;
			}

			// Reset de ConnectThread porque ya se ha establecido la conexi�n
			synchronized (AhorcadoService.this) {
				mConnectThread = null;
			}

			// Ejecuci�n de ConexionThread para el intercambio de datos
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
			Log.d(TAG, "Creaci�n de ConexionThread [" + socketType + "]");
			mSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Obtenci�n de InputStream y OutputStream de BluetoothSocket
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				Log.e(TAG,
						"Error en la obtenci�n de inputStream/outputStream del socket de conexi�n",
						e);
			}

			mInStream = tmpIn;
			mOutStream = tmpOut;
		}

		public void run() {
			Log.i(TAG, "INICIO mConexionThread");
			byte[] buffer = new byte[1024];
			int bytes;

			// Lectura continua de InputStream durante la conexi�n
			while (true) {
				try {
					// Lectura de datos
					bytes = mInStream.read(buffer);

					// Env�a los datos le�dos a la actividad principal
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
