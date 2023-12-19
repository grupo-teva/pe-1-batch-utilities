package es.mc.batch.dto;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DtoWrapperRest implements Serializable{
	
	private static final long serialVersionUID = 127152836605904620L;
	
	private String resultado;
	private String descripcion;
	
	Object datos;
	
	public Object getDatos() {
		return datos;
	}

	public void setDatos(Object datos) {
		this.datos = datos;
	}

	public String getResultado() {
		return resultado;
	}
	
	public void setResultado(String resultado) {
		this.resultado = resultado;
	}
	
	public String getDescripcion() {
		return descripcion;
	}
	
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	
	public DtoWrapperRest(String resultado, String descripcion) {
		super();
		this.resultado = resultado;
		this.descripcion = descripcion;
	}
	
	public DtoWrapperRest(String resultado, String descripcion, Object object) {
		super();
		this.resultado = resultado;
		this.descripcion = descripcion;
		this.datos = object;
	}
	
}//FIN DtoWrapperRest
