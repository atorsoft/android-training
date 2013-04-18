package atorsoft.agenda.javabeans;

public class ContactoBean {

	private String nombre;
	private String email;
	private long telefono;

	public ContactoBean(String nombre, String email, long telefono) {
		super();
		this.nombre = nombre;
		this.email = email;
		this.telefono = telefono;
	}

	public String getNombre() {
		return nombre;
	}

	public String getEmail() {
		return email;
	}

	public long getTelefono() {
		return telefono;
	}

	@Override
	public String toString() {
		return nombre + " (" + email + " - " + telefono + ")";
	}

}
