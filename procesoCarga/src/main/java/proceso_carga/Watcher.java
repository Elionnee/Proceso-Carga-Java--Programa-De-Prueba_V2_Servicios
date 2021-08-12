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
 * Clase obserdor que monitoriza el directorio indicado e informa de cualquier cambio que pueda ocurrir
 * @author snc
 *
 */
public class Watcher implements WatcherI {

	private File file =  null;
	private Path dir = null;

	private static Watcher watcher;

	/**
	 * Contructor de la clase
	 * 
	 * @param directory Directorio que se desea monitorizar
	 */
	private Watcher(String directory) {

		file =  new File(directory);
		dir = Paths.get(file.getAbsolutePath());

	}


	public static Watcher getSingletonInstance(String directory) {
		if(watcher == null) {
			watcher = new Watcher(directory);
		} else {
			System.out.println("No se puede crear el objeto, ya que ya existe una instancia del mismo  en esta clase");
		}
		return watcher;
	}


	@Override
	public Watcher clone(){
		try {
			throw new CloneNotSupportedException();
		} catch (CloneNotSupportedException ex) {
			System.out.println("No se puede clonar un objeto de la clase Watcher");
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
	 * Método que se encarga de monitorizar e informar de cambios en el directorio
	 * 
	 * @throws InterruptedException Se lanza cuando el método sufre una interrupción inesperada
	 */
	public void watchService(ConnectionDB con) throws InterruptedException {

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

			con.addMensajesPend("Extepción detectada durante la entrada y salida");
		} catch (InterruptedException e) {

			con.addMensajesPend("Thread Watcher Interrumpido");
			Thread.currentThread().interrupt();

		}

	}

}