package proceso_carga;

import java.util.ArrayList;

import org.hibernate.Session;

public interface ConnectionDBI {

	/**
	 * Método que se encarga de introducir los datos a la base de datos
	 * 
	 * @param session Sesión establecida con la base de datos correspondientes
	 * 
	 * @param semana Nombre de la tabla a la que se deben añadir los datos
	 * 
	 * @param prod Datos a añadir
	 */
	public void connectToDBIntroduceData(Session session, String semana, ProductoEntity prod);





	/**
	 * Se crea una query para crear la tabla con el nombre indicado, utilizando la conexión ya establecida con la base de datos
	 * Se ejecuta la query
	 */

	/**
	 * Método que crea una nueva tabla en la base de datos
	 * 
	 * @param semana Nombre de la tabla que se debe crear
	 * 
	 * @param session Conexión abierta con la base de datos
	 */
	public void connectToDBCreateTable(String semana, Session session);



	/**
	 * Método que crea una nueva tabla de logs en la base de datos
	 * 
	 * @param session Conexión abierta con la base de datos
	 * @param session Conexión abierta con la base de datos
	 */
	public void connectToDBCreateTableLogs(Session session);



	/**
	 * Método que se encarga de introducir los logs a la base de datos
	 * 
	 * @param session Sesión establecida con la base de datos correspondientes
	 * 
	 * @param semana Nombre de la tabla a la que se deben añadir los datos
	 * 
	 * @param prod Datos a añadir
	 */
	public void connectToDBIntroduceLogs(Session session, String semana, ProductoEntity prod, String info);



	/**
	 * Método que se encarga de devolver un array que contiene todos los mensajes pendientes
	 * 
	 * @return mensajesPend Lista de mensajes pendientes
	 */
	public ArrayList<String> getMensajesPend();

	/**
	 * Método que se encarga de vaciar el array de mensajes pendientes
	 */
	public void cleanMensajesPend();

	/**
	 * Método que se encarga de añadir un nuevo mensaje al array de pendientes
	 * 
	 * @param mes Mensaje que se desea añadir a la cola de pendientes
	 */
	public void addMensajesPend(String mes);
}
