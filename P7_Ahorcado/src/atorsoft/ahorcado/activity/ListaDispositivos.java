package atorsoft.ahorcado.activity;

import java.util.Set;

import atorsoft.ahorcado.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Esta actividad se muestra como ventana de di�logo. Muestra la lista de
 * dispositivos vinculados y dispositivos detectados en una b�squeda. Cuando el
 * usuario selecciona un dispositivo, la direcci�n MAC del dispositivo
 * seleccionado se env�a a la actividad principal mediante el intent de
 * resultado.
 */

public class ListaDispositivos extends Activity {
	// Depuraci�n
	private static final String TAG = "ListaDispositivos";
	private static final boolean D = false;

	// Extra del intent de resultado
	public static String EXTRA_DIRECCION_MAC = "mac_dispositivo";

	// Adaptador local de Bluetooth
	private BluetoothAdapter mBtAdapter;

	// Array de dispositivos vinculados
	private ArrayAdapter<String> mArrayAdapterDispositivosVinculados;

	// Array de nuevos dispositivos encontrados
	private ArrayAdapter<String> mArrayAdapterDispositivosNuevos;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Configuraci�n de ventana: Muestra el progreso de actividad en la
		// barra de t�tulo
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.lista_dispositivos);

		// Por defecto se establece resultado de CANCELACION por si el usuario
		// pulsa volver
		setResult(Activity.RESULT_CANCELED);

		// Bot�n de b�squeda
		Button botonBusqueda = (Button) findViewById(R.id.buscar_dispositivos);
		botonBusqueda.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				buscarDispositivos();
				v.setVisibility(View.GONE);
			}
		});

		// Inicializaci�n de los ArrayAdapter
		mArrayAdapterDispositivosVinculados = new ArrayAdapter<String>(this,
				R.layout.nombre_dispositivo);
		mArrayAdapterDispositivosNuevos = new ArrayAdapter<String>(this,
				R.layout.nombre_dispositivo);

		// ListView de dispositivos vinculados
		ListView listViewDispositivosVinculados = (ListView) findViewById(R.id.dispositivos_vinculados);
		listViewDispositivosVinculados
				.setAdapter(mArrayAdapterDispositivosVinculados);
		listViewDispositivosVinculados
				.setOnItemClickListener(mClickListenerDispositivo);

		// ListView de dispositivos nuevos encontrados
		ListView listViewDispositivosNuevos = (ListView) findViewById(R.id.dispositivos_nuevos);
		listViewDispositivosNuevos.setAdapter(mArrayAdapterDispositivosNuevos);
		listViewDispositivosNuevos
				.setOnItemClickListener(mClickListenerDispositivo);

		// Registro de BroadcastReceiver para la acci�n de nuevo dispositivo
		// encontrado
		IntentFilter intentFilter = new IntentFilter(
				BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(mBroadcastReceiver, intentFilter);

		// Registro de BroadcastReceiver para la acci�n de b�squeda finalizada
		intentFilter = new IntentFilter(
				BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mBroadcastReceiver, intentFilter);

		// Obtenci�n de BluetoothAdapter local
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();

		// Obtenci�n de set de dispositivos vinculados
		Set<BluetoothDevice> setDispositivosVinculados = mBtAdapter
				.getBondedDevices();

		// Si hay dispositivos vinculados, se a�aden al ArrayAdapter
		if (setDispositivosVinculados.size() > 0) {
			findViewById(R.id.titulo_dispositivos_vinculados).setVisibility(
					View.VISIBLE);
			for (BluetoothDevice dispositivo : setDispositivosVinculados) {
				mArrayAdapterDispositivosVinculados.add(dispositivo.getName()
						+ "\n" + dispositivo.getAddress());
			}
		} else {
			String noDispositivosVinculados = getResources().getText(
					R.string.no_dispositivos_vinculados).toString();
			mArrayAdapterDispositivosVinculados.add(noDispositivosVinculados);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// Cancelaci�n de b�squeda de dispositivos
		if (mBtAdapter != null) {
			mBtAdapter.cancelDiscovery();
		}

		// Eliminaci�n de registro de BroadcastReceiver
		this.unregisterReceiver(mBroadcastReceiver);
	}

	/**
	 * M�todo de b�squeda de dispositivos
	 */
	private void buscarDispositivos() {
		if (D)
			Log.d(TAG, "buscarDispositivos()");

		// Indica el t�tulo y el progreso de la b�squeda en la barra de t�tulo
		setProgressBarIndeterminateVisibility(true);
		setTitle(R.string.buscando);

		// Muestra el t�tulo del apartado de nuevos dispositivos
		findViewById(R.id.titulo_dispositivos_nuevos).setVisibility(
				View.VISIBLE);

		// Si se estaba realizando una b�squeda, dicha b�squeda se cancela
		if (mBtAdapter.isDiscovering()) {
			mBtAdapter.cancelDiscovery();
		}

		// Ejecuci�n de la b�squeda de dispositivos
		mBtAdapter.startDiscovery();
	}

	// OnClickListener para cada dispositivo de las listas
	private OnItemClickListener mClickListenerDispositivo = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// Cancelaci�n de la b�squeda para mejora del rendimiento
			mBtAdapter.cancelDiscovery();

			// Obtenci�n de la direcci�n MAC del dispositivo (�ltimos 17
			// caracteres del texto)
			String textoDispositivo = ((TextView) view).getText().toString();
			String direccionMAC = textoDispositivo.substring(textoDispositivo
					.length() - 17);

			// Intent de resultado: se a�ade el extra con la direcci�n MAC del
			// dispositivo seleccionado
			Intent intent = new Intent();
			intent.putExtra(EXTRA_DIRECCION_MAC, direccionMAC);

			// Resultado y finalizaci�n de la actividad
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
	};

	// BroadcastReceiver para la recepci�n de las siguientes acciones:
	// - Nuevo dispositivo encontrado
	// - Finalizaci�n de b�squeda de dispositivos
	private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// Nuevo dispositivo encontrado
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Obtenemos el objeto BluetoothDevice mediante el Intent
				BluetoothDevice dispositivoEncontrado = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// Si el dispositivo ya est� vinculado se ignora (aparecer� en
				// la lista de vinculados)
				// Si no est� vinculado, se a�ade al array de dispositivos
				// nuevos encontrados
				if (dispositivoEncontrado.getBondState() != BluetoothDevice.BOND_BONDED) {
					mArrayAdapterDispositivosNuevos.add(dispositivoEncontrado
							.getName()
							+ "\n"
							+ dispositivoEncontrado.getAddress());
				}
				// Finalizaci�n de b�squeda de dispositivos
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				setProgressBarIndeterminateVisibility(false);
				setTitle(R.string.seleccion_dispositivo);
				if (mArrayAdapterDispositivosNuevos.getCount() == 0) {
					String noDispositivosNuevos = getResources().getText(
							R.string.no_dispositivos_nuevos).toString();
					mArrayAdapterDispositivosNuevos.add(noDispositivosNuevos);
				}
			}
		}
	};

}
