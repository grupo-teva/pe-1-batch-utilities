package es.mc.batch.util;

import es.mc.domain.util.Constantes;
import es.mc.email.xsd.AttachmentType;
import es.mc.util.business.beans.DatosContactoBean;
import es.mc.util.business.common.ConexionBDHandler;
import es.mc.util.business.dto.*;
import es.mc.util.business.services.PermisoAcceso;
import es.mc.util.business.services.impl.RegistradorBBDD;
import es.mc.util.business.util.Entidad;
import es.mc.util.business.util.Util;

import javax.naming.NamingException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.Month;
import java.time.DayOfWeek;
import java.time.Year;
import java.time.format.TextStyle;
import java.util.Locale;

public class Comunicaciones {

    private static final RegistradorBBDD registrador = new RegistradorBBDD();

    public static void envioComunicacionAdjunto(File excel, String nombreCsv, LocalDate fechaBatch) throws Exception {
        String[] destinatarios = registrador.recogerValorConstante(Constantes.ENVIO_EMAIL_PMR_ACTIVOS, false).split(";");
        AttachmentType adjunto = new AttachmentType();

        adjunto.setFilename(nombreCsv);
        adjunto.setContent(Files.readAllBytes(excel.toPath()));

        String mailAsunto = Constantes.ASUNTO_EMAIL_PMR_ACTIVOS + fechaBatch;
        String mailBody = String.format(Constantes.CUERPO_EMAIL_PMR_ACTIVOS, fechaBatch);

        Util.sendComunicacionMultipleAdjunto(mailAsunto, mailBody, destinatarios, adjunto);

    }

    public static void envioComunicacionPermisoProximaExpiracion(PermisoAccesoDTO permiso) throws Exception {
        DatosContactoBean datosContacto = getDatoContactoPersona(permiso);
        String nombreUsuario = registrador.recogerNombreUsuario(permiso.getCodigoPersona());
        String diasAExpirar = registrador.recogerValorConstante(Constantes.DIAS_EXPIRAR_CORREO_AVISO, false);
        ComunicacionDTO comunicacion = new ComunicacionDTO();
        ComunicacioDTO notificacion = new ComunicacioDTO();
        RegistroDTO registro = new RegistroDTO();
        EntidadDTO entidad = new EntidadDTO();
        PlantillaComunicacionDTO plantilla = new PlantillaComunicacionDTO();
        String mailAsunto;
        String mailBody;

        if(datosContacto.getCodCanalCom() == Constantes.SMS_CODIGO){
            comunicacion.setDestinatario(datosContacto.getTxtTelefonoMovil());
            mailAsunto = String.format(Constantes.ASUNTO_SMS_PERMISOS_A_EXPIRAR,
                    permiso.getVehiculo().getMatricula());
            mailBody = String.format(Constantes.CUERPO_SMS_PERMISOS_A_EXPIRAR,
                    getFechaFormatoCorreo(),
                    permiso.getDescripcion(),
                    permiso.getVehiculo().getMatricula(),
                    diasAExpirar);
        } else {
            comunicacion.setDestinatario(datosContacto.getTxtEmail());
            mailAsunto = String.format(Constantes.ASUNTO_EMAIL_PERMISOS_A_EXPIRAR,
                    permiso.getVehiculo().getMatricula());
            mailBody = String.format(Constantes.CUERPO_EMAIL_PERMISOS_A_EXPIRAR,
                    getFechaFormatoCorreo(),
                    nombreUsuario,
                    permiso.getDescripcion(),
                    permiso.getVehiculo().getMatricula(),
                    diasAExpirar,
                    getFechaFormatoCorreo());
        }

        mailBody = replaceAcutesHTML(mailBody);
        comunicacion.setMensaje(mailBody);
        entidad.setCodigoEntidad(Entidad.PERMISO_ACCESO.getCodigoEntidad());
        entidad.setNombreEntidad(Entidad.PERMISO_ACCESO.getNombreEntidad());
        registro.setCodigoRegistro(permiso.getIdPermiso());
        notificacion.setEntidad(entidad);
        notificacion.setRegistro(registro);
        plantilla.setCodigoPlantillaCommunicacion(Constantes.PLANTILLA_COMUNICACION_PERMISO_PROXIMA_EXPIRACION);
        notificacion.setPlantillaComunicacion(plantilla);

        Util.sendComunicacion(mailBody, mailAsunto, mailBody, datosContacto);
        agregarComunicacion(notificacion, comunicacion, datosContacto);
    }

    public static void envioComunicacionPermisoExpirado(PermisoAccesoDTO permiso) throws Exception {
        DatosContactoBean datosContacto = getDatoContactoPersona(permiso);
        String nombreUsuario = registrador.recogerNombreUsuario(permiso.getCodigoPersona());
        ComunicacionDTO comunicacion = new ComunicacionDTO();
        ComunicacioDTO notificacion = new ComunicacioDTO();
        RegistroDTO registro = new RegistroDTO();
        EntidadDTO entidad = new EntidadDTO();
        PlantillaComunicacionDTO plantilla = new PlantillaComunicacionDTO();
        String mailAsunto;
        String mailBody;

        if(datosContacto.getCodCanalCom() == Constantes.SMS_CODIGO){
            comunicacion.setDestinatario(datosContacto.getTxtTelefonoMovil());
            mailAsunto = String.format(Constantes.ASUNTO_SMS_PERMISOS_EXPIRADOS,
                    permiso.getVehiculo().getMatricula());
            mailBody = String.format(Constantes.CUERPO_SMS_PERMISOS_EXPIRADOS,
                    getFechaFormatoCorreo(),
                    permiso.getDescripcion(),
                    permiso.getVehiculo().getMatricula(),
                    getDiaSiguiente());
        } else {
            comunicacion.setDestinatario(datosContacto.getTxtEmail());
            mailAsunto = String.format(Constantes.ASUNTO_EMAIL_PERMISOS_EXPIRADOS,
                    permiso.getVehiculo().getMatricula());
            mailBody = String.format(Constantes.CUERPO_EMAIL_PERMISOS_EXPIRADOS,
                    getFechaFormatoCorreo(),
                    nombreUsuario,
                    permiso.getDescripcion(),
                    permiso.getVehiculo().getMatricula(),
                    getDiaSiguiente(),
                    getFechaFormatoCorreo());
        }

        mailBody = replaceAcutesHTML(mailBody);
        comunicacion.setMensaje(mailBody);
        entidad.setCodigoEntidad(Entidad.PERMISO_ACCESO.getCodigoEntidad());
        entidad.setNombreEntidad(Entidad.PERMISO_ACCESO.getNombreEntidad());
        registro.setCodigoRegistro(permiso.getIdPermiso());
        notificacion.setEntidad(entidad);
        notificacion.setRegistro(registro);
        plantilla.setCodigoPlantillaCommunicacion(Constantes.PLANTILLA_COMUNICACION_PERMISO_EXPIRADO);
        notificacion.setPlantillaComunicacion(plantilla);

        Util.sendComunicacion(mailBody, mailAsunto, mailBody, datosContacto);
        agregarComunicacion(notificacion, comunicacion, datosContacto);
    }

    private static DatosContactoBean getDatoContactoPersona(PermisoAccesoDTO permiso) {
        DatosContactoPersonaFisicaDTO datos_contacto = registrador.obtenerDatosContacto(permiso.getCodigoPersona());
        DatosContactoBean contactoBean = new DatosContactoBean();
        contactoBean.setTxtEmail(datos_contacto.getEmail());
        contactoBean.setTxtTelefonoMovil(datos_contacto.getTelMovil());
        contactoBean.setNomCanalCom(datos_contacto.getViaComunicacion().getNombre());
        contactoBean.setCodCanalCom((short) datos_contacto.getViaComunicacion().getCodigo());
        contactoBean.setIndDeseaCom(datos_contacto.getDeseaRecibir());

        return contactoBean;
    }

    private static String getFechaFormatoCorreo(){
        Integer day = LocalDate.now().getDayOfMonth();
        Month mes = LocalDate.now().getMonth();
        Year year = Year.of(LocalDate.now().getYear());

        return day + "/" + mes.getValue() + "/" + year.getValue();
    }

    private static String getDiaSiguiente(){
        LocalDate.now().plusDays(1);
        Integer day = LocalDate.now().plusDays(1).getDayOfMonth();
        Month mes = LocalDate.now().plusDays(1).getMonth();
        Year year = Year.of(LocalDate.now().plusDays(1).getYear());

        return day + "/" + mes.getValue() + "/" + year.getValue();
    }

    public static String replaceAcutesHTML(String str) {

        str = str.replaceAll("á","&aacute;");
        str = str.replaceAll("é","&eacute;");
        str = str.replaceAll("í","&iacute;");
        str = str.replaceAll("ó","&oacute;");
        str = str.replaceAll("ú","&uacute;");
        str = str.replaceAll("Á","&Aacute;");
        str = str.replaceAll("É","&Eacute;");
        str = str.replaceAll("Í","&Iacute;");
        str = str.replaceAll("Ó","&Oacute;");
        str = str.replaceAll("Ú","&Uacute;");
        str = str.replaceAll("ñ","&ntilde;");
        str = str.replaceAll("Ñ","&Ntilde;");

        return str;
    }

    public static Integer agregarComunicacion(ComunicacioDTO notificacion, ComunicacionDTO comunicacion, DatosContactoBean datosContacto) {
        // AGREGAR EN T_COMUNICACION
        try {
            CanalComunicacionDTO canalComunicacion = new CanalComunicacionDTO();
            canalComunicacion.setCodigoCanalComunicacion(datosContacto.getCodCanalCom());
            canalComunicacion.setDescripcionCanalComunicacion(datosContacto.getNomCanalCom());
            canalComunicacion.setNombreCanalComunicacion(datosContacto.getNomCanalCom());
            comunicacion.setCanalComunicacion(canalComunicacion);

            comunicacion.setEntidad(notificacion.getEntidad());
            comunicacion.setPlantilla(notificacion.getPlantillaComunicacion());
            comunicacion.setRegistro(notificacion.getRegistro());
            return agregarComunicacion(comunicacion);

        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static Integer agregarComunicacion(ComunicacionDTO comunicacion) throws SQLException, NamingException {

        Integer codComunicacion = null;
        try (Connection conn = ConexionBDHandler.getConexion();
             CallableStatement stored = conn.prepareCall(" { ? = call IComunicacion( ?, ?, ?, ?, ?, ? ) } ");) {

            stored.registerOutParameter(1, Types.INTEGER);
            stored.setInt(2, comunicacion.getCanalComunicacion().getCodigoCanalComunicacion());
            stored.setInt(3, comunicacion.getEntidad().getCodigoEntidad());
            stored.setInt(4, comunicacion.getRegistro().getCodigoRegistro());
            stored.setInt(5, comunicacion.getPlantilla().getCodigoPlantillaCommunicacion());
            stored.setString(6, comunicacion.getDestinatario());
            stored.setString(7, comunicacion.getMensaje());
            stored.execute();

            codComunicacion = stored.getInt(1);

            if (codComunicacion > 0) {
                return codComunicacion;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return codComunicacion;

    }
}
