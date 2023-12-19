package es.mc.batch.batchlet;

import es.mc.domain.util.Constantes;
import es.mc.util.business.dto.PermisoAccesoDTO;
import es.mc.util.business.services.DatosInforme;
import es.mc.util.business.services.PermisoAcceso;
import es.mc.util.business.services.impl.RegistradorBBDD;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import javax.batch.api.AbstractBatchlet;
import javax.ejb.EJB;
import javax.enterprise.context.Dependent;
import javax.inject.Named;
import java.io.File;
import java.io.FilenameFilter;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import static es.mc.batch.util.Comunicaciones.envioComunicacionPermisoExpirado;
import static es.mc.batch.util.Comunicaciones.envioComunicacionPermisoProximaExpiracion;

@Named
@Dependent
@Log4j2
public class ComunicarExpiracionPermisoBatchlet extends AbstractBatchlet {

	@EJB(beanName = "DatosInformeImpl")
	private DatosInforme datosInformeSrv;

	@EJB(beanName = "PermisoAccesoImpl")
	private PermisoAcceso permisoAccesoSrv;

	public ComunicarExpiracionPermisoBatchlet() {
	}

	private static final RegistradorBBDD registrador = new RegistradorBBDD();
	@Override
	public String process() {

		// Registro inicio del proceso en log
		long startTime = System.currentTimeMillis();
		Date horaInicio = new Date();
		log.info("--------------------------------------------");
		log.info("INICIO DEL PROCESO DE COMUNICAR EXPIRACION DE PERMISOS");
		log.info("--------------------------------------------");
		log.info("HORA DE INICIO => " + horaInicio);

		List<PermisoAccesoDTO> permisosExpirados;
		List<PermisoAccesoDTO> permisosProxExpirados;

		try {
			//Los permisos que se ven afectados por esta comunicacion son los siguientes:
			//TEPMR y permanentes, siempre que tengan fecha de fin. Se exluye a los puntuales.

			//Obtener los permisos a comunicar expiracion cuya fecha_fin - 1 = current_date
			//Comunicar con la Plantilla 1 de aviso de expiración
			Integer diasAExpirar = Integer.parseInt(registrador.recogerValorConstante(Constantes.DIAS_EXPIRAR_CORREO_AVISO, false));

			permisosProxExpirados = permisoAccesoSrv.consultarPermisosProximosExpiracion(diasAExpirar);
			notificarProximaExpiracion(permisosProxExpirados);

			//Obtener los permisos a comunicar expiracion cuya fecha_fin + 1 = current_date
			//Comunicar con la plantilla 2 de permiso expirado

			permisosExpirados = permisoAccesoSrv.consultarPermisosRecienteExpiracion();
			notificarExpiracion(permisosExpirados);

			log.info("HORA DE FIN => " + horaInicio + " <-> " + new Date());
			long endTime = System.currentTimeMillis();
			long elapsedTime = endTime - startTime;
			double seconds = elapsedTime / 1.0E09;
			log.info("Tiempo transcurrido = " + seconds + " segundos");
			log.info("-------------------------------------------");
			log.info("FIN CORRECTO DEL PROCESO DE COMUNICAR EXPIRACION DE PERMISOS");
			log.info("-------------------------------------------");
			datosInformeSrv.insertarAuditoriaBatch("BATCHCOMUNICAREXPIRACIONPERMISO", new Timestamp (startTime), new Timestamp (endTime), 1, null);
			return "OK";

		} catch (Exception e) {
			e.printStackTrace();
			log.info("HORA DE FIN => " + horaInicio + " <-> " + new Date());
			long endTime = System.currentTimeMillis();
			long elapsedTime = endTime - startTime;
			double seconds = elapsedTime / 1.0E09;
			log.info("Tiempo transcurrido = " + seconds + " segundos");
			log.info("ERROR => " + e.getLocalizedMessage());
			log.info("-------------------------------------------");
			log.info("FIN CORRECTO DEL PROCESO DE COMUNICAR EXPIRACION DE PERMISOS");
			log.info("-------------------------------------------");
			datosInformeSrv.insertarAuditoriaBatch("BATCHCOMUNICAREXPIRACIONPERMISO", new Timestamp (startTime), new Timestamp (endTime), 2, e.getLocalizedMessage());
			return "KO";
		}
	}

	private void notificarProximaExpiracion(List<PermisoAccesoDTO> permisos){
		for(PermisoAccesoDTO permiso : permisos) {
			try {
				envioComunicacionPermisoProximaExpiracion(permiso);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void notificarExpiracion(List<PermisoAccesoDTO> permisos){
		for(PermisoAccesoDTO permiso : permisos) {
			try {
				envioComunicacionPermisoExpirado(permiso);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
