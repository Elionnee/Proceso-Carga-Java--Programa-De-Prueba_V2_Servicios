package proceso_carga;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.apache.logging.log4j.LogManager;
import org.hibernate.Session;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

interface StringFunction {

	String run(String str);

}


/**
 * Clase que se encarga de leer los contenidos del directorio, seleccionar los .csv y procesarlos 
 * @author snc
 *
 */
public class LoadService implements LoadServiceI{

	// Directorio que se desea monitorizar
	private String filePath;

	// Archivos pendientes de leer
	private Queue<File> filesOnQueue = new LinkedList<>();

	// Estado actual del thread, cambia si es interrumpido
	private Boolean threadState = true;

	// Objeto logger que registra el estado del programa por consola
	private org.apache.logging.log4j.Logger logger = null;
	// Objeto properties que nos permite acceder a los contenidos del archivo pc.properties correspondiente
	private Properties prop = null;

	private static LoadService load;
	private String posCatch;



	// Lambda que permite dar un formato específico a la string que se le pasa como parámetro de entrada
	StringFunction deleteSymbols = new StringFunction() {
		@Override
		public String run(String n) {

			String result = ""; 
			result = n.replaceAll("[^a-zA-Z0-9.]", "");  

			return result;

		}
	};


	/**
	 * Método que devuelve un logger
	 * 
	 * @return logger
	 */
	public org.apache.logging.log4j.Logger getLogger() {
		return logger;
	}



	/**
	 * Constructor de la clase que se encarga de buscar el archivo .properties y de leer que archivos 
	 * csv hay en el directorio que este archivo especifica
	 * 
	 * @param con Conexión con la base de datos para administrarla
	 */
	private LoadService(ConnectionDB con) {

		con.cleanMensajesPend();
		// Genera la conexión con el archivo .properties indicado
		prop = this.loadPropertiesFile("pc.properties", con);

		// Crea el logger y lo configura a partir de las indicaciones del fichero log4j2.xml correspondiente
		logger = LogManager.getLogger(this.getClass());
		PropertyConfigurator.configure(getClass().getResource("log4j2.xml"));

		// Comprueba si el logger creado funciona correctamente
		try {
			con.addMensajesPend("Logger funcionando correctamente");
		} catch(Exception e) {
			con.addMensajesPend("Error con el log");
		}

		// Limpia la cola de archivos pendientes de leer
		filesOnQueue.clear();

		// Setea el directorio indicado en el properties como directorio a observar
		setFilePath(this.prop, con);
	}

	

	/**
	 * Método que permite crear una única instancia de la clase, para cumplir con el patrón Singleton
	 * @return con Instancia de la clase 
	 */
	public static LoadService getSingletonInstance(ConnectionDB con) {
		
		if(load == null) {
			load =  new LoadService(con);
		}
		
		return load;
	}

	

	/** 
	 * Método que permite clonar un objeto pero que hemos sobreescrito para que no permita crear más objetos y así
	 * cumplamos con el patrón Singleton
	 */
	@Override
	public LoadService clone(){
	    try {
	        throw new CloneNotSupportedException();
	    } catch (CloneNotSupportedException ex) {
	       posCatch = "No se puede clonar un objeto de la clase LoadService";
	    }
	    return null; 
	}
	
	

	/**
	 * Por cada thread disponible, comenzar a ejecutar la función run() :
	 * 		Obtener archivo de la cola de pendientes
	 * 		Avisar en caso de que haya una interrupción inesperada y detener el thread
	 */

	/**
	 * Método que crea los threads y su función de ejecución para leer los archivos CSV
	 * 
	 * @param session Sesión establecida con la base de datos correspondientes
	 * 
	 * @param threadPool Conjunto de threads disponibles
	 * 
	 * @param con Conexión con la base de datos para su correcta administración
	 * 
	 * @return true si el thread ha terminado su ejecución correctamente o false si se ha visto interrumpido
	 * 
	 */
	private Boolean threadReadCSVExecution(final Session session, ExecutorService threadPool, ConnectionDB con) {
		
		if(posCatch != null) {
			con.addMensajesPend(posCatch);
		}

		threadPool.execute(new Runnable() {

			public void run() {

				try {
					threadGetFileFromQueue();
				} catch (Exception e) {
					notifyThreadInt();
				}

			}

			private void threadGetFileFromQueue() {

				File file;

				file = getFileFromFileQueue();

				if(file != null) {

					readFile(session, file, con);
					setThreadState();
				} else {
					notifyThreadInt();
					Thread.currentThread().interrupt();
				}

			}

		});

		if(Boolean.FALSE.equals(threadState)) {
			setThreadState();
			return false;
		} else {
			return true;
		}

	}


	/**
	 * Método que actualiza la variable threadState para indicar que se ha interrumpido el thread
	 */
	private synchronized void notifyThreadInt() {
		threadState = false;
	}


	/**
	 * Método que actualiza la variable threadState para indicar el correcto funcionamiento del thread
	 */
	private synchronized void setThreadState() {
		threadState = true;
	}



	/**
	 * Método que extrae y devuelve el primer archivo de la cola de archivos pendientes por leer
	 * 
	 * @return filesOnQueue.poll() Primer archivo pendiente por leer o null si no quedan archivos pendientes por leer
	 */
	private synchronized File getFileFromFileQueue() {
		if(!filesOnQueue.isEmpty()) {
			return filesOnQueue.poll();
		}
		return null;
	}



	/**
	 * Crea un objeto 'properties'
	 * Busca el archivo .properties en el directorio indicado y trata de leerlo. En caso de no poder, lo notifica
	 * Guarda la referencia al contenido del archivo en el objeto 'properties'
	 * Devuelve el objeto 'properties'
	 */

	/**
	 * Método que lee los contenidos del archivo .properties
	 * 
	 * @param filePath Ruta en la que se encuentra el archivo pc.properties
	 *
	 * @param con Conexión con la base de datos para su correcta administración
	 * 
	 * @return prop Devuelve un objeto del tipo Properties que permite extraer los parametros del archivo .properties
	 */
	public Properties loadPropertiesFile(String filePath, ConnectionDB con) {

		// Crea el objeto .properties
		prop = new Properties();

		// Carga el archivo .properties de su directorio de origen, mediante un path relativo a la carpeta de recursos
		try (InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(filePath)) {

			// Carga el contenido del fichero .properties en el objeto indicado
			prop.load(resourceAsStream);

		} catch (IOException e) {

			con.addMensajesPend("No se pudo leer el archivo .properties : " + filePath);

		}

		return prop;

	}



	/**
	 * Método que devuelve la ruta al directorio que se desea monitorizar
	 * 
	 * @return filePath  Ruta al directorio que se desea monitorizar
	 */
	public String getFilePath() {

		return filePath;

	}



	/**
	 * Buscamos el valor de la etiqueta 'dir' en la referencia al archivo .properties
	 * En caso de haber un error, notificamos del mismo
	 */

	/**
	 * Método que busca la dirección del directorio a monitorizar del archivo .properties
	 * 
	 * @param prop Objeto que contiene los datos del archivo .properties
	 * 
	 * @param con Conexión con la base de datos para su correcta administración
	 */
	private void setFilePath(Properties prop, ConnectionDB con) {

		// Extraemos el filepath del directorio que se desea monitorizar del fichero .properties
		try {

			filePath = prop.getProperty("dir");

		} catch (NullPointerException e) {

			con.addMensajesPend("Error al leer el directorio objetivo en el .properties. No se ha encontrado.");

		}

	}




	/**
	 * Buscar el directorio a monitorizar
	 * Obtener una lista con todos los archivos que contiene en ese momento
	 * Guardar en una lista solo aquellos archivos con extensión .CSV en una lista de 'pendientes por procesar'
	 */

	/**
	 * Método que se encarga de obtener una lista que contenga todos los csv disponibles 
	 * en el directorio indicado como parametro de entrada.
	 * 	 
	 * @param con Conexión con la base de datos para su correcta administración
	 */
	private void getFiles(ConnectionDB con) {

		// Obtiene la referencia al directorio y extrae todos los archivos presentes en el mismo
		File folder = new File(filePath);
		File[] filesPresent = folder.listFiles();

		// Comprueba que en verdad cintiene archivos
		if(filesPresent.length==0) {

			con.addMensajesPend(Thread.currentThread().getId() + " : No hay archivos CSV pendientes de leer.");

		} else {

			// Comprueba que los archivos presentes son .CSV y que no habian sido leidos previamente durante esta ejecución
			for(File fileName : filesPresent) { 

				if(fileName.toString().toLowerCase().endsWith(".csv") && (fileName.isFile())) {

					filesOnQueue.add(fileName);

				} else {

					con.addMensajesPend(Thread.currentThread().getName() + " : No hay archivos CSV pendientes de leer.");

				}

			}

		}

	}




	/**
	 * Comprobar que los archivos introducidos y el directorio de destino no son nulos
	 * Tratar de mover el archivo al directorio destino
	 * En caso de que el archivo ya exista, reemplazar el mismo con el archivo nuevo y borrar este último de su localización anterior
	 * Notificar de cualquier error
	 */

	/**
	 * Mueve el archivo indicado al directorio indicado
	 * 
	 * @param orFilePath Nombre del archivo original que se va a mover
	 * 
	 * @param cpFilePath Nombre del archivo una vez movido o del archivo a sustituir
	 * 
	 * @param destDir Directorio al que se va a mover
	 * 
	 * @param con Conexión con la base de datos para su correcta administración
	 */
	private void moveFile(String orFilePath, String cpFilePath, String destDir, ConnectionDB con) {

		try {

			if(StringUtils.isNoneBlank(orFilePath) && StringUtils.isNoneBlank(cpFilePath) && StringUtils.isNoneBlank(destDir)) {

				File orFile = new File(orFilePath);
				File cpFile = new File(cpFilePath);

				replaceFile(orFile, cpFile, con);
				con.addMensajesPend("Archivo trasladado correctramente a la carpeta : " + destDir);

			}

		} catch(Exception ex) { 
			con.addMensajesPend("MoveFiles no encuentra el archivo");

		}

	}





	/**
	 * Mover el archivo indicado al directorio indicado
	 * En caso de no poder, sobreescribir el archivo ya existente con el mismo nombre
	 * Borrar el archivo de su directorio original
	 */

	/**
	 * Sobreescribe el archivo indicado con el archivo introducido
	 * 
	 * @param orFile Archivo que se desea mover
	 * 
	 * @param cpFile Archivo que se desea sobreescribir
	 * 
	 * @param con Conexión con la base de datos para su correcta administración
	 * 
	 * @throws IOException Cuando no consigue reemplazar el archivo o cuando no existe
	 */
	private void replaceFile(File orFile, File cpFile, ConnectionDB con) throws IOException {

		try {

			FileUtils.moveFile(orFile, cpFile);

		} catch(FileExistsException e) {

			FileUtils.copyFile(orFile, cpFile);
			FileUtils.forceDelete(orFile);

		} catch(FileNotFoundException ex) {
			con.addMensajesPend("Replace File : "+ orFile +" . Archivo movido previamente.");
		}

	}





	/**
	 * Crear un lector para archivos CSV que lea el archivo indicado como parametro de entrada
	 * Saltar la primera línea del archivo, ya que no contiene datos
	 * Comenzar a leer el archivo línea por línea
	 * Añadir el archivo a la base de datos línea por línea
	 * Si se añade correctamente -> Mover a la carpeta OK
	 * Si no -> Mover a la carpeta KO
	 */

	/**
	 * Método que se encarga de leer el archivo csv que se le indica como parametro de entrada y que 
	 * almacena los contenidos del mismo en varios hashmap cuyas key son los id de los productos
	 * 
	 * @param file Archivo que se desea leer
	 * 
	 * @param con Conexión con la base de datos para su correcta administración
	 */
	private void readFile(Session session, File file, ConnectionDB con) {

		CSVReader csv = null;
		String semana = file.getName();
		String[] next = null;

		try {

			Reader reader = Files.newBufferedReader(Paths.get(file.getAbsolutePath()));
			csv = new CSVReaderBuilder(reader).withSkipLines(1).build();

		} catch (IOException e) {

			con.connectToDBIntroduceLogs(session, "Error", null , "Error al leer el fichero CSV.");

		}

		ProductoEntity p = null;
		
		if(csv != null) {

			semana = semana.replace(".csv", "");


			con.connectToDBIntroduceLogs(session, semana, null, "Conectado satisfactoriamente con la base de datos");
			con.connectToDBCreateTable(semana, session);
			con.connectToDBCreateTable("productoentity", session);

			try {

				// Mientras haya una línea de CSV por leer, continua
				while((next = csv.readNext()) != null) {

					// Crea una entidad nueva (Un producto nuevo)
					p = new ProductoEntity(next[0] + "_" + semana, next[1], Double.parseDouble(deleteSymbols.run(next[2])), Integer.parseInt(deleteSymbols.run(next[3])), next[0]);

					// Introduce la nueva entidad en su tabla correspondiente y en la tabla general
					con.connectToDBIntroduceData(session, semana, p);
					con.connectToDBIntroduceData(session, "productoentity", p);

				}

				// En caso de salir bien, mueve el archivo a la 
				moveFile(file.getAbsolutePath(), (prop.getProperty("ok") + "\\" + file.getName()), prop.getProperty("ok"), con);

			} catch (CsvValidationException e) {

				con.connectToDBIntroduceLogs(session, semana, p, "CSV es null");
				moveFile(file.getAbsolutePath(), prop.getProperty("ko") + "\\" + file.getName(), prop.getProperty("ko"), con);

			} catch (IOException e) {

				con.connectToDBIntroduceLogs(session, semana, p, "Error al leer el CSV");
				moveFile(file.getAbsolutePath(), prop.getProperty("ko") + "\\" + file.getName(), prop.getProperty("ko"), con);

			} catch (NullPointerException e) {

				con.connectToDBIntroduceLogs(session, semana, p, "CSV es nulo");
				moveFile(file.getAbsolutePath(), prop.getProperty("ko") + "\\" + file.getName(), prop.getProperty("ko"), con);

			}
		}
	}







	/**
	 * Obtener la lista de archivos pendientes de leer
	 * Mientras queden archivos pendientes por leer, asignar a los threads disponibles un archivo a leer
	 * Interrumpir el thread en caso de error y notificar del mismo
	 */

	/**
	 * Recoge todos los archivos nuevos que se encuentran actualmente en el directorio y los lee
	 * 
	 * @param con Conexión con la base de datos para su correcta administración
	 * 
	 * @throws InterruptedException Se lanza cuando un thread sufre una interrupción inesperada
	 */
	public void readCSV(Session session, ConnectionDB con) {

		Boolean pendiente = false;

		ArrayList<String> tempPend;

		con.connectToDBCreateTableLogs(session);		

		tempPend = con.getMensajesPend();
		con.cleanMensajesPend();
		for (String m : tempPend) {
			con.connectToDBIntroduceLogs(session, "Inicio", null,  m);
		}
		tempPend.clear();

		con.connectToDBIntroduceLogs(session, "Inicio", null, "Comienzo de transferencia de archivos .CSV a la base de datos.");

		getFiles(con);

		if (!filesOnQueue.isEmpty()) {

			ExecutorService threadPool = Executors.newFixedThreadPool(Integer.parseInt(prop.getProperty("numThreads")));
			pendiente = true;

			while(Boolean.TRUE.equals(pendiente)) {

				pendiente = threadReadCSVExecution(session, threadPool, con);

			}
		}

		if(posCatch != null) {
			con.addMensajesPend(posCatch);
		}


		tempPend = con.getMensajesPend();
		con.cleanMensajesPend();
		for (String m : tempPend) {
			con.connectToDBIntroduceLogs(session, "Fin", null,  m);
		}
		tempPend.clear();

	}




}
