package proceso_carga;

import java.util.Properties;

import org.hibernate.Session;

public interface LoadServiceI {

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
	public Properties loadPropertiesFile(String filePath, ConnectionDB con);



	/**
	 * Método que devuelve la ruta al directorio que se desea monitorizar
	 * 
	 * @return filePath  Ruta al directorio que se desea monitorizar
	 */
	public String getFilePath();


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
	public void readCSV(Session session, ConnectionDB con);

	/**
	 * Método que devuelve un logger
	 * 
	 * @return logger
	 */
	public org.apache.logging.log4j.Logger getLogger();
}
