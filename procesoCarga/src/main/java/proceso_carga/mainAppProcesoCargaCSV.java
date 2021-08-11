package proceso_carga;

import java.io.File;

import org.hibernate.Session;
import org.hibernate.cfg.Configuration;

public class mainAppProcesoCargaCSV {

	public static void main(String[] args) throws InterruptedException {

		ConnectionDB con =  new ConnectionDB();
		LoadService loadS = new LoadService(con);
		Watcher watch = new Watcher(loadS.getFilePath());

		int i = -1;
		Session session;
		org.hibernate.SessionFactory sessions;
		sessions = new Configuration().configure(new File("src/main/resources/META-INF/hibernate.cfg.xml")).buildSessionFactory();
		session = sessions.openSession();
		while(i < 0) {
			try {
				loadS.readCSV(session, con);
			} catch(Exception e) {
				e.printStackTrace();
			}
			watch.watchService();
		}

		session.close();
	}

}
