package atorsoft.gestorempresas.provider.javabean;

public class EmpresaBean {

	private String nombre;
	private String direccion;
	private String email;
	private String web;
	private Integer categoria;

	public EmpresaBean(String nombre, String direccion, String email,
			String web, Integer categoria) {
		super();
		this.nombre = nombre;
		this.direccion = direccion;
		this.email = email;
		this.web = web;
		this.categoria = categoria;
	}

	public String getNombre() {
		return nombre;
	}

	public String getDireccion() {
		return direccion;
	}

	public String getEmail() {
		return email;
	}

	public String getWeb() {
		return web;
	}

	public Integer getCategoria() {
		return categoria;
	}

}
