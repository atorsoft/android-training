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
 * Esta actividad se muestra como ventana de diálogo. Muestra la lista de
 * dispositivos vinculados y dispositivos detectados en una búsqueda. Cuando el
 * usuario selecciona un dispositivo, la dirección MAC del dispositivo
 * seleccionado se envía a la actividad principal mediante el intent de
 * resultado.
 */

public class ListaDispositivos extends Activity {
	// Depuración
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

		// Configuración de ventana: Muestra el progreso de actividad en la
		// barra de título
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.lista_dispositivos);

		// Por defecto se establece resultado de CANCELACION por si el usuario
		// pulsa volver
		setResult(Activity.RESULT_CANCELED);

		// Botón de búsqueda
		Button botonBusqueda = (Button) findViewById(R.id.buscar_dispositivos);
		botonBusqueda.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				buscarDispositivos();
				v.setVisibility(View.GONE);
			}
		});

		// Inicialización de los ArrayAdapter
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

		// Registro de BroadcastReceiver para la acción de nuevo dispositivo
		// encontrado
		IntentFilter intentFilter = new IntentFilter(
				BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(mBroadcastReceiver, intentFilter);

		// Registro de BroadcastReceiver para la acción de búsqueda finalizada
		intentFilter = new IntentFilter(
				BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mBroadcastReceiver, intentFilter);

		// Obtención de BluetoothAdapter local
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();

		// Obtención de set de dispositivos vinculados
		Set<BluetoothDevice> setDispositivosVinculados = mBtAdapter
				.getBondedDevices();

		// Si hay dispositivos vinculados, se añaden al ArrayAdapter
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

		// Cancelación de búsqueda de dispositivos
		if (mBtAdapter != null) {
			mBtAdapter.cancelDiscovery();
		}

		// Eliminación de registro de BroadcastReceiver
		this.unregisterReceiver(mBroadcastReceiver);
	}

	/**
	 * Método de búsqueda de dispositivos
	 */
	private void buscarDispositivos() {
		if (D)
			Log.d(TAG, "buscarDispositivos()");

		// Indica el título y el progreso de la búsqueda en la barra de título
		setProgressBarIndeterminateVisibility(true);
		setTitle(R.string.buscando);

		// Muestra el título del apartado de nuevos dispositivos
		findViewById(R.id.titulo_dispositivos_nuevos).setVisibility(
				View.VISIBLE);

		// Si se estaba realizando una búsqueda, dicha búsqueda se cancela
		if (mBtAdapter.isDiscovering()) {
			mBtAdapter.cancelDiscovery();
		}

		// Ejecución de la búsqueda de dispositivos
		mBtAdapter.startDiscovery();
	}

	// OnClickListener para cada dispositivo de las listas
	private OnItemClickListener mClickListenerDispositivo = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// Cancelación de la búsqueda para mejora del rendimiento
			mBtAdapter.cancelDiscovery();

			// Obtención de la dirección MAC del dispositivo (últimos 17
			// caracteres del texto)
			String textoDispositivo = ((TextView) view).getText().toString();
			String direccionMAC = textoDispositivo.substring(textoDispositivo
					.length() - 17);

			// Intent de resultado: se añade el extra con la dirección MAC del
			// dispositivo seleccionado
			Intent intent = new Intent();
			intent.putExtra(EXTRA_DIRECCION_MAC, direccionMAC);

			// Resultado y finalización de la actividad
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
	};

	// BroadcastReceiver para la recepción de las siguientes acciones:
	// - Nuevo dispositivo encontrado
	// - Finalización de búsqueda de dispositivos
	private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// Nuevo dispositivo encontrado
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Obtenemos el objeto BluetoothDevice mediante el Intent
				BluetoothDevice dispositivoEncontrado = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// Si el dispositivo ya está vinculado se ignora (aparecerá en
				// la lista de vinculados)
				// Si no está vinculado, se añade al array de dispositivos
				// nuevos encontrados
				if (dispositivoEncontrado.getBondState() != BluetoothDevice.BOND_BONDED) {
					mArrayAdapterDispositivosNuevos.add(dispositivoEncontrado
							.getName()
							+ "\n"
							+ dispositivoEncontrado.getAddress());
				}
				// Finalización de búsqueda de dispositivos
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
