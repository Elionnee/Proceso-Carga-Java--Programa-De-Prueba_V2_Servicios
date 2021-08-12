package proceso_carga;

import java.util.ArrayList;

import org.hibernate.Session;

public interface ConnectionDBI {

	/**
	 * M�todo que se encarga de introducir los datos a la base de datos
	 * 
	 * @param session Sesi�n establecida con la base de datos correspondientes
	 * 
	 * @param semana Nombre de la tabla a la que se deben a�adir los datos
	 * 
	 * @param prod Datos a a�adir
	 */
	public void connectToDBIntroduceData(Session session, String semana, ProductoEntity prod);





	/**
	 * Se crea una query para crear la tabla con el nombre indicado, utilizando la conexi�n ya establecida con la base de datos
	 * Se ejecuta la query
	 */

	/**
	 * M�todo que crea una nueva tabla en la base de datos
	 * 
	 * @param semana Nombre de la tabla que se debe crear
	 * 
	 * @param session Conexi�n abierta con la base de datos
	 */
	public void connectToDBCreateTable(String semana, Session session);



	/**
	 * M�todo que crea una nueva tabla de logs en la base de datos
	 * 
	 * @param session Conexi�n abierta con la base de datos
	 * @param session Conexi�n abierta con la base de datos
	 */
	public void connectToDBCreateTableLogs(Session session);



	/**
	 * M�todo que se encarga de introducir los logs a la base de datos
	 * 
	 * @param session Sesi�n establecida con la base de datos correspondientes
	 * 
	 * @param semana Nombre de la tabla a la que se deben a�adir los datos
	 * 
	 * @param prod Datos a a�adir
	 */
	public void connectToDBIntroduceLogs(Session session, String semana, ProductoEntity prod, String info);



	/**
	 * M�todo que se encarga de devolver un array que contiene todos los mensajes pendientes
	 * 
	 * @return mensajesPend Lista de mensajes pendientes
	 */
	public ArrayList<String> getMensajesPend();

	/**
	 * M�todo que se encarga de vaciar el array de mensajes pendientes
	 */
	public void cleanMensajesPend();

	/**
	 * M�todo que se encarga de a�adir un nuevo mensaje al array de pendientes
	 * 
	 * @param mes Mensaje que se desea a�adir a la cola de pendientes
	 */
	public void addMensajesPend(String mes);
}
