package es.mc.batch.util;

import java.io.IOException;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 * Clase encargada de establecer conexion y ejecutar comandos SFTP.
 */
public class SFTPConnector {
 
    /**
     * Sesion SFTP establecida.
     */
    private Session session;
	private int grabCount;
	private static final Log LOG = LogFactory.getLog(SFTPConnector.class);
 
    /**
     * Establece una conexion SFTP.
     *
     * @param username Nombre de usuario.
     * @param password Contrasena.
     * @param host     Host a conectar.
     * @param port     Puerto del Host.
     *
     * @throws JSchException          Cualquier error al establecer
     *                                conexión SFTP.
     * @throws IllegalAccessException Indica que ya existe una conexion
     *                                SFTP establecida.
     */
    public void connect(String username, String password, String host, int port)
        throws JSchException, IllegalAccessException {
        if (this.session == null || !this.session.isConnected()) {
            JSch jsch = new JSch();
 
            this.session = jsch.getSession(username, host, port);
            this.session.setPassword(password);
 
            // Parametro para no validar key de conexion.
            this.session.setConfig("StrictHostKeyChecking", "no");
 
            this.session.connect();
            LOG.info("Conexión establecida.");
        } else {
            throw new IllegalAccessException("Sesion SFTP ya iniciada.");
        }
    }
 
    /**
     * Añade un archivo al directorio FTP usando el protocolo SFTP.
     *
     * @param ftpPath  Path del FTP donde se agregará el archivo.
     * @param filePath Directorio donde se encuentra el archivo a subir en
     *                 disco.
     * @param fileName Nombre que tendra el archivo en el destino.
     *
     * @throws IllegalAccessException Excepción lanzada cuando no hay
     *                                conexión establecida.
     * @throws JSchException          Excepción lanzada por algún
     *                                error en la ejecución del comando
     *                                SFTP.
     * @throws SftpException          Error al utilizar comandos SFTP.
     * @throws IOException            Excepción al leer el texto arrojado
     *                                luego de la ejecución del comando
     *                                SFTP.
     */
    public final boolean addFile(String ftpPath, String filePath,
        String fileName) throws IllegalAccessException, IOException,
        SftpException, JSchException {
        if (this.session != null && this.session.isConnected()) {
 
            // Abrimos un canal SFTP. Es como abrir una consola.
            ChannelSftp channelSftp = (ChannelSftp) this.session.
                openChannel("sftp");
 
            // Nos ubicamos en el directorio del FTP.
            
            channelSftp.connect();
            channelSftp.cd(ftpPath);
 
            LOG.info(String.format("Creando archivo %s en el " +
                "directorio %s", fileName, ftpPath));
            channelSftp.put(filePath, fileName);
 
            LOG.info("Archivo subido exitosamente");
 
            channelSftp.exit();
            channelSftp.disconnect();
            return true;
        } else {
            throw new IllegalAccessException("No existe sesion SFTP iniciada.");
           
        }
    }
    
    /**
     * Coge archivos del directorio FTP usando el protocolo SFTP.
     *
     * @param ftpPath  Path del FTP donde se cogerá el archivo.

     * 
     * @throws IllegalAccessException Excepción lanzada cuando no hay
     *                                conexión establecida.
     * @throws JSchException          Excepción lanzada por algún
     *                                error en la ejecución del comando
     *                                SFTP.
     * @throws SftpException          Error al utilizar comandos SFTP.
     * @throws IOException            Excepción al leer el texto arrojado
     *                                luego de la ejecución del comando
     *                                SFTP.
     */
    public final void getFile(String ftpPath) throws IllegalAccessException, IOException,
        SftpException, JSchException {
        if (this.session != null && this.session.isConnected()) {
 
            // Abrimos un canal SFTP. Es como abrir una consola.
            ChannelSftp channelSftp = (ChannelSftp) this.session.
                openChannel("sftp");
 
            // Nos ubicamos en el directorio del FTP.
            
            channelSftp.connect();
            channelSftp.cd(ftpPath);
            
            //Obtenemos lista de archivos
            @SuppressWarnings("unchecked")
			Vector<ChannelSftp.LsEntry> list = channelSftp.ls(ftpPath); 
            LOG.info("ls .");
 
            // iterate through objects in list, identifying specific file names
            for (ChannelSftp.LsEntry oListItem : list) {
                // output each item from directory listing for logs
                LOG.info(oListItem.toString()); 

                // If it is a file (not a directory)
                if (!oListItem.getAttrs().isDir()) {
                    // Grab the remote file ([remote filename], [local path/filename to write file to])

                    LOG.info("get " + oListItem.getFilename());
                    channelSftp.get(ftpPath + "/" + oListItem.getFilename());  

                    grabCount++; 

                    // Delete remote file
                    //c.rm(oListItem.getFilename());  // Note for SFTP grabs from this remote host, deleting the file is unnecessary, 
                                                      //   as their system automatically moves an item to the 'downloaded' subfolder
                                                      //   after it has been grabbed.  For other target hosts, un comment this line to remove any downloaded files from the inbox.
                }
            }

            // Report files grabbed to log
            if (grabCount == 0) { 
            	LOG.info("Found no new files to grab.");
            } else {
            	LOG.info("Retrieved " + grabCount + " new files.");
            }                           
            
            channelSftp.exit();
            channelSftp.disconnect();
        } else {
            throw new IllegalAccessException("No existe sesion SFTP iniciada.");
        }
    }
    
    /**
     * Cierra la sesion SFTP.
     * @throws JSchException 
     */
    public final ChannelSftp open() throws JSchException {
    	 ChannelSftp channelSftp = (ChannelSftp) this.session.openChannel("sftp");
  
             // Nos ubicamos en el directorio del FTP.
             
             channelSftp.connect();
             
		return channelSftp;
    }
 
    /**
     * Cierra la sesion SFTP.
     */
    public final void disconnect() {
    	LOG.info("Desconectamos SFTP");
        this.session.disconnect();
    }
}
