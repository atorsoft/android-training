package atorsoft.libreria.listener;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import atorsoft.libreria.activity.Libreria;
import atorsoft.libreria.activity.Libro;
import atorsoft.libreria.javabean.LibroBean;

import atorsoft.libreria.R;

public class GuardarDatos implements OnClickListener {

	private Libro actvLibro;
	private Long idLibro;

	public GuardarDatos(Libro actvLibro) {
		this.actvLibro = actvLibro;
		this.idLibro = actvLibro.getIdLibro();
	}

	@Override
	public void onClick(View v) {
		if (guardarDatos()) {
			if (idLibro == null) {
				// Añadir libro: Cambiamos a Listado y limpiamos Libro
				Libreria.setCargaLista(true);
				((Libreria) actvLibro.getParent()).getTabHost()
						.setCurrentTab(0);
				actvLibro.limpiarDatos();
			} else {
				// Editar libro: Volvemos a Listado y cerramos Libro
				actvLibro.setResult(Activity.RESULT_OK);
				actvLibro.finish();
			}
		}
	}

	private boolean guardarDatos() {
		boolean resultado = false;
		try {
			String titulo = actvLibro.getTitulo();
			String autor = actvLibro.getAutor();
			String isbn = actvLibro.getISBN();
			float precio = actvLibro.getPrecio();

			LibroBean libro = new LibroBean(isbn, titulo, autor, precio);

			String mensaje;
			if (idLibro == null) {
				Libreria.getDBHelper().insertLibro(libro);
				mensaje = actvLibro.getString(R.string.libro_anadido);
			} else {
				Libreria.getDBHelper().updateLibro(idLibro, libro);
				mensaje = actvLibro.getString(R.string.libro_actualizado);
			}
			resultado = true;
			Toast.makeText(actvLibro, mensaje, Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			mostrarAlerta(e.getMessage());
		}
		return resultado;
	}

	private void mostrarAlerta(String mensaje) {
		AlertDialog.Builder alerta = new AlertDialog.Builder(actvLibro);
		alerta.setMessage(mensaje);
		alerta.setPositiveButton(android.R.string.ok, null);
		alerta.show();
	}

}
