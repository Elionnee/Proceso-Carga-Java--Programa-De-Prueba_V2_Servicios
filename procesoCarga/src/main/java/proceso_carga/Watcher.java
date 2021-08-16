package proceso_carga;

import java.util.List;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;



/**
 * Clase observador que monitoriza el directorio indicado e informa de cualquier cambio que pueda ocurrir
 * @author snc
 *
 */
public class Watcher implements WatcherI {

	private File file =  null;
	private Path dir = null;
	private String posCatch;
	private static Watcher watch;

	/**
	 * Contructor de la clase
	 * 
	 * @param directory Directorio que se desea monitorizar
	 */
	private Watcher(String directory) {

		file =  new File(directory);
		dir = Paths.get(file.getAbsolutePath());
		

	}


	/**
	 * M�todo que permite crear una �nica instancia de la clase, para cumplir con el patr�n Singleton
	 * 
	 * @param directory Directorio que se desea observar
	 * 
	 * @return watch Instancia de la clase 
	 */
	public static Watcher getSingletonInstance(String directory) {

		if(watch == null) {

			watch = new Watcher(directory);

		} 

		return watch;

	}



	/** 
	 * M�todo que permite clonar un objeto pero que hemos sobreescrito para que no permita crear m�s objetos y as�
	 * cumplamos con el patr�n Singleton
	 * 
	 * @return null No permite que se clone el objeto
	 */
	@Override
	public Watcher clone() {

		try {

			throw new CloneNotSupportedException();

		} catch (CloneNotSupportedException ex) {

			posCatch = "No se puede clonar un objeto de la clase Watcher";
		}

		return null; 

	}



	/**
	 * Crear un observador para el directorio indicado
	 * Indicar los tipos de eventos que se deben observar en este directorio
	 * Observar el directorio consatantemente hasta que se produzca uno de estos eventos y notificar del mismo cuando se produzca
	 * Notificar de cualquier error
	 */

	/**
	 * M�todo que se encarga de monitorizar e informar de cambios en el directorio
	 * 
	 * @param con Conexi�n con la base de datos para administrarla
	 * 
	 * @throws InterruptedException Se lanza cuando el m�todo sufre una interrupci�n inesperada
	 */
	public void watchService(ConnectionDB con) throws InterruptedException {
		
		if(posCatch != null) {
			
			con.addMensajesPend(posCatch);
		
		}

		try {

			WatchService watcher = dir.getFileSystem().newWatchService();
			dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
					StandardWatchEventKinds.ENTRY_MODIFY);

			con.addMensajesPend("Monitorizando eventos en el directorio...");

			WatchKey watchKey = watcher.take();

			List<WatchEvent<?>> events = watchKey.pollEvents();

			for (WatchEvent<?> event : events) {

				if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {

					con.addMensajesPend("Se ha creado un nuevo archivo o directorio: " + event.context().toString());

				}
				
				if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {

					con.addMensajesPend("Se ha borrado un archivo o directorio: " + event.context().toString());

				}

				if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {

					con.addMensajesPend("Se ha modificado un archivo o directorio: " + event.context().toString());

				}

			}

		} catch (IOException e) {

			con.addMensajesPend("Extepci�n detectada durante la entrada y salida");
			
		} catch (InterruptedException e) {

			con.addMensajesPend("Thread Watcher Interrumpido");
			Thread.currentThread().interrupt();

		}

	}

}