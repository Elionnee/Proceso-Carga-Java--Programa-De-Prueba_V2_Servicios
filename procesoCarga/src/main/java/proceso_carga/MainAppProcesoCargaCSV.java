package proceso_carga;

import java.io.File;

import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;

public class MainAppProcesoCargaCSV {

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
					log.debug("¡ Cambio detectado !");
					loadS.readCSV(session, con);
				} catch(Exception e) {
					con.addMensajesPend("Extepción detectada al intentar leer los archivos .csv del directorio");
				}
				log.debug("Monitorizando el directorio...");
				watch.watchService(con);
			} 
		} finally {
			session.close();
		}
	}

}
