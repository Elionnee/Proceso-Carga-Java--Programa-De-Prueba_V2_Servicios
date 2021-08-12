package proceso_carga;

import java.util.ArrayList;

import org.hibernate.Session;

public interface ConnectionDBI {

	/**
	 * Método que se encarga de introducir los datos a la base de datos
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



	public void connectToDBCreateTableLogs(Session session);



	public void connectToDBIntroduceLogs(Session session, String semana, ProductoEntity prod, String info);


	public ArrayList<String> getMensajesPend();

	public void cleanMensajesPend();

	public void addMensajesPend(String mes);
}
