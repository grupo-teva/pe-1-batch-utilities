package es.mc.batch.batchlet;

import es.mc.domain.util.Constantes;
import es.mc.util.business.services.DatosInforme;
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

@Named
@Dependent
@Log4j2
public class EliminarLogsFirmaBatchlet extends AbstractBatchlet {

	@EJB(beanName = "DatosInformeImpl")
	private DatosInforme datosInformeSrv;

	public EliminarLogsFirmaBatchlet() {
	}

	@Override
	public String process() {

		// Registro inicio del proceso en log
		long startTime = System.currentTimeMillis();
		Date horaInicio = new Date();
		log.info("--------------------------------------------");
		log.info("INICIO DEL PROCESO DE ELIMINAR LOGS DE FIRMA");
		log.info("--------------------------------------------");
		log.info("HORA DE INICIO => " + horaInicio);

		try {

			final File folder = new File(datosInformeSrv.recuperarConstante(Constantes.RUTA_LOGS_BATCH));
			FilenameFilter fileFilter = new FilenameFilter() {
				@SneakyThrows
				@Override
				public boolean accept(File dir, String name) {
					return name.toLowerCase().startsWith(datosInformeSrv.recuperarConstante(Constantes.FICHEROS_ELIMINAR_REGEX));
				}
			};

			for (final File fileEntry : folder.listFiles(fileFilter)) {
				if (System.currentTimeMillis() - fileEntry.lastModified() > 2592000000L) //Miliseconds to days = 30 days
					log.info("Se elimina => " + fileEntry + ".");
					fileEntry.delete();
			}

			log.info("HORA DE FIN => " + horaInicio + " <-> " + new Date());
			long endTime = System.currentTimeMillis();
			long elapsedTime = endTime - startTime;
			double seconds = elapsedTime / 1.0E09;
			log.info("Tiempo transcurrido = " + seconds + " segundos");
			log.info("-------------------------------------------");
			log.info("FIN CORRECTO DEL PROCESO DE ELIMINAR LOGS DE FIRMA");
			log.info("-------------------------------------------");
			datosInformeSrv.insertarAuditoriaBatch("BATCHELIMINARLOGS", new Timestamp (startTime), new Timestamp (endTime), 1, null);
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
			log.info("FIN ERRONEO DEL PROCESO DE ELIMINAR LOGS DE FIRMA");
			log.info("-------------------------------------------");
			datosInformeSrv.insertarAuditoriaBatch("BATCHELIMINARLOGS", new Timestamp (startTime), new Timestamp (endTime), 2, e.getLocalizedMessage());
			return "KO";
		}
	}// FIN process
}// FIN EliminarLogsFirmaBatchlet
