package proceso_carga;

import java.util.ArrayList;
import java.util.UUID;

import org.hibernate.Session;
import org.hibernate.query.Query;


/**
 * Clase que permite la introducci�n de datos y la gesti�n de la base de datos que tenemos a nuestra disposici�n
 * @author snc
 *
 */
public class ConnectionDB implements ConnectionDBI {

	private static ConnectionDB con = null;

	private ArrayList<String> mensajesPend = new ArrayList<>();



	/**
	 * Contructor de la clase
	 */
	private ConnectionDB() {}


	/**
	 * M�todo que permite crear una �nica instancia de la clase, para cumplir con el patr�n Singleton
	 * 
	 * @return con Instancia de la clase 
	 */
	public static ConnectionDB getSingletonInstance() {
		
		if (con == null) {
			
			con = new ConnectionDB();
		}
		
		return con;
		
	}



	/** 
	 * M�todo que permite clonar un objeto pero que hemos sobreescrito para que no permita crear m�s objetos y as�
	 * cumplamos con el patr�n Singleton
	 * 
	 * @return null No permite que se clone el objeto
	 */
	@Override
	public ConnectionDB clone() {
		
		try {
			
			throw new CloneNotSupportedException();
			
		} catch (CloneNotSupportedException ex) {
			
			addMensajesPend("No se puede clonar un objeto de la clase ConnectionDB");
		
		}
		
		return null; 
		
	}



	/**
	 * M�todo que se encarga de introducir los datos a la base de datos
	 * 
	 * @param session Sesi�n establecida con la base de datos correspondientes
	 * 
	 * @param semana Nombre de la tabla a la que se deben a�adir los datos
	 * 
	 * @param prod Datos a a�adir
	 */
	public synchronized void connectToDBIntroduceData(Session session, String semana, ProductoEntity prod) {

		// Creamos un query que nos permite insertar valores en la base de datos
		String query = "INSERT INTO "+ semana + " (Id, Nombre, Precio, Cantidad, Id_Producto)\r\n"
				+ "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE Cantidad = VALUES(Cantidad) ;";


		// Inicio de la transacci�n con la bas
		session.getTransaction().begin();

		// Creamos un objeto query con la string que contiene el query de tipo insert
		@SuppressWarnings("rawtypes")
		Query query2 = session.createNativeQuery(query);

		// Rellenamos los par�metros necesarios para realizar el query de tipo insert
		query2.setParameter(1, prod.getId());
		query2.setParameter(2, prod.getNombre());
		query2.setParameter(3, Double.toString(prod.getPrecio()));
		query2.setParameter(4, Integer.toString(prod.getCantidad()));
		query2.setParameter(5, prod.getTransactionId());

		// Intenta actualizar la base de datos ejecutando la query
		try {
			
			query2.executeUpdate();
			session.getTransaction().commit();
			
		} catch(Exception e) {
			
			session.getTransaction().rollback();
		
		}
	
	}




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
	public synchronized void connectToDBCreateTable(String semana, Session session) {

		// Creamos un query del tipo create que nos permitir� crear una tabala con el nombre indicado
		String queryTable = "CREATE TABLE " + semana + " ("
				+ "    Id varchar(80) PRIMARY KEY,\n"
				+ "    Nombre text,\n"
				+ "    Precio double,\n"
				+ "    Cantidad int,\n"
				+ "    Id_Producto text\n"
				+ ");";

		// Comienza la transacci�n para actualizar la base de datos y crear una tabla usando la query
		try {

			session.getTransaction().begin();

			// Crea el objeto query usando el string que contiene la query del tipo create 
			@SuppressWarnings("rawtypes")
			Query query = session.createNativeQuery(queryTable);
			query.executeUpdate();

			session.getTransaction().commit();

		} catch (Exception e) {

			mensajesPend.add("Fallo al crear la tabla. Ya existe");
			session.getTransaction().rollback();

		}
	}





	/**
	 * M�todo que crea una nueva tabla de logs en la base de datos
	 * 
	 * @param session Conexi�n abierta con la base de datos
	 */
	public synchronized void connectToDBCreateTableLogs(Session session) {

		// Creamos un query del tipo create que nos permitir� crear una tabala con el nombre indicado
		String queryTable = "CREATE TABLE logs (    Id_Log varchar(80) PRIMARY KEY,    Id_Transacci�n text,   Info text);";

		// Comienza la transacci�n para actualizar la base de datos y crear una tabla usando la query
		try {

			session.getTransaction().begin();

			// Crea el objeto query usando el string que contiene la query del tipo create 
			@SuppressWarnings("rawtypes")
			Query query = session.createNativeQuery(queryTable);
			query.executeUpdate();

			session.getTransaction().commit();

		} catch (Exception e) {

			mensajesPend.add("Fallo al crear la tabla de logs, ya existe.");
			session.getTransaction().rollback();

		}
	}






	/**
	 * M�todo que se encarga de introducir los logs a la base de datos
	 * 
	 * @param session Sesi�n establecida con la base de datos correspondientes
	 * 
	 * @param semana Nombre de la tabla a la que se deben a�adir los datos
	 * 
	 * @param prod Datos a a�adir
	 * 
	 * @param info Informaci�n que se desea a�adir en el log
	 */
	public synchronized void connectToDBIntroduceLogs(Session session, String semana, ProductoEntity prod, String info) {

		// Creamos un query que nos permite insertar valores en la base de datos
		String query = "INSERT INTO  logs (Id_Log, Id_Transacci�n, Info)\r\n"
				+ "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE Info = VALUES(Info) ;";


		// Inicio de la transacci�n con la bas
		session.getTransaction().begin();

		// Creamos un objeto query con la string que contiene el query de tipo insert
		@SuppressWarnings("rawtypes")
		Query query2 = session.createNativeQuery(query);

		// Rellenamos los par�metros necesarios para realizar el query de tipo insert
		query2.setParameter(1, UUID.randomUUID().toString());
		
		if(prod != null) {
			
			query2.setParameter(2, prod.getId() + "_" + semana);
		
		} else {
			
			query2.setParameter(2, "INFO_GENERAL_" + semana);
		
		}
		
		query2.setParameter(3, info);

		// Intenta actualizar la base de datos ejecutando la query
		try {
			
			query2.executeUpdate();
			session.getTransaction().commit();
			
		} catch(Exception e) {
			
			session.getTransaction().rollback();
			
		}
		
	}


	/**
	 * M�todo que se encarga de devolver un array que contiene todos los mensajes pendientes
	 * 
	 * @return mensajesPend Lista de mensajes pendientes
	 */
	public ArrayList<String> getMensajesPend() {
		return mensajesPend;
	}


	/**
	 * M�todo que se encarga de vaciar el array de mensajes pendientes
	 */
	public void cleanMensajesPend() {
		mensajesPend.clear();
	}

	/**
	 * M�todo que se encarga de a�adir un nuevo mensaje al array de pendientes
	 * 
	 * @param mes Mensaje que se desea a�adir a la cola de pendientes
	 */
	public synchronized void addMensajesPend(String mes) {
		
		mensajesPend.add(mes);
		
	}

}
