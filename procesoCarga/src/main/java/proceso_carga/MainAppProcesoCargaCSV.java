package proceso_carga;

import java.io.File;

import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
/**
 * Clase principal del programa que actua como main y permite su ejecuci�n
 * @author snc
 *
 */
public class MainAppProcesoCargaCSV {
	
	
	/**
	 * Creamos una "conexi�n" (actua m�s como administrador) con la base de datos
	 * Declaramos un objeto que nos permite cargar y leer los archivos .csv
	 * Declaramos un servicio observador para el directorio indicado por el fichero pc.properties correspondiente 
	 * Creamos un logger para poder almacenar entradas de log en la base de datos
	 * 
	 * Creamos y abrimos una sesi�n con la base de datos y observamos el directorio
	 * Esperamos a que se realicen cambios en el directorio
	 * Cuando detectamos cambios en el directorio, leemos el contenido del directorio y
	 * seleccionamos los archivos .csv.
	 * Introducimos los contenidos de los mismos en la base de datos
	 * Continuamos observando el directorio constantemente
	 * 
	 * @param args
	 * @throws InterruptedException
	 */
	
	public static void main(String[] args) throws InterruptedException {

		ConnectionDB con =  ConnectionDB.getSingletonInstance();
		LoadService loadS = LoadService.getSingletonInstance(con);
		Watcher watch = Watcher.getSingletonInstance(loadS.getFilePath());
		Logger log = loadS.getLogger();
		
		int i = -1;
		Session session;
		org.hibernate.SessionFactory sessions;
		sessions = new Configuration().configure(new File("src/main/resources/META-INF/hibernate.cfg.xml")).buildSessionFactory();
		session = sessions.openSession();

		try {
			while(i < 0) {
				try {
					log.debug("� Cambio detectado !");
					loadS.readCSV(session, con);
				} catch(Exception e) {
					con.addMensajesPend("Extepci�n detectada al intentar leer los archivos .csv del directorio");
				}
				log.debug("Monitorizando el directorio...");
				watch.watchService(con);
			} 
		} finally {
			session.close();
		}
	}

}
