package proceso_carga;

import java.util.ArrayList;
import java.util.UUID;

import org.hibernate.Session;
import org.hibernate.query.Query;

public class ConnectionDB implements ConnectionDBI {

	private static ConnectionDB con = null;
	
	private ArrayList<String> mensajesPend = new ArrayList<>();
	
	private ConnectionDB() {}
	
	public static ConnectionDB getSingletonInstance() {
		if (con == null) {
			con = new ConnectionDB();
		} else {
			System.out.println("No se puede crear el objeto, ya que ya existe una instancia del mismo  en esta clase");

		}
		return con;
	}

	
	@Override
	public ConnectionDB clone(){
	    try {
	        throw new CloneNotSupportedException();
	    } catch (CloneNotSupportedException ex) {
	        System.out.println("No se puede clonar un objeto de la clase ConnectionDB");
	    }
	    return null; 
	}
	
	
	
	/**
	 * Método que se encarga de introducir los datos a la base de datos
	 * 
	 * @param semana Nombre de la tabla a la que se deben añadir los datos
	 * 
	 * @param prod Datos a añadir
	 */
	public synchronized void connectToDBIntroduceData(Session session, String semana, ProductoEntity prod) {

		// Creamos un query que nos permite insertar valores en la base de datos
		String query = "INSERT INTO "+ semana + " (Id, Nombre, Precio, Cantidad, Id_Producto)\r\n"
				+ "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE Cantidad = VALUES(Cantidad) ;";


		// Inicio de la transacción con la bas
		session.getTransaction().begin();

		// Creamos un objeto query con la string que contiene el query de tipo insert
		@SuppressWarnings("rawtypes")
		Query query2 = session.createNativeQuery(query);

		// Rellenamos los parámetros necesarios para realizar el query de tipo insert
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
	public synchronized void connectToDBCreateTable(String semana, Session session) {

		// Creamos un query del tipo create que nos permitirá crear una tabala con el nombre indicado
		String queryTable = "CREATE TABLE " + semana + " ("
				+ "    Id varchar(80) PRIMARY KEY,\n"
				+ "    Nombre text,\n"
				+ "    Precio double,\n"
				+ "    Cantidad int,\n"
				+ "    Id_Producto text\n"
				+ ");";

		// Comienza la transacción para actualizar la base de datos y crear una tabla usando la query
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





	public synchronized void connectToDBCreateTableLogs(Session session) {

		// Creamos un query del tipo create que nos permitirá crear una tabala con el nombre indicado
		String queryTable = "CREATE TABLE logs (    Id_Log varchar(80) PRIMARY KEY,    Id_Transacción text,   Info text);";

		// Comienza la transacción para actualizar la base de datos y crear una tabla usando la query
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








	public synchronized void connectToDBIntroduceLogs(Session session, String semana, ProductoEntity prod, String info) {

		// Creamos un query que nos permite insertar valores en la base de datos
		String query = "INSERT INTO  logs (Id_Log, Id_Transacción, Info)\r\n"
				+ "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE Info = VALUES(Info) ;";


		// Inicio de la transacción con la bas
		session.getTransaction().begin();

		// Creamos un objeto query con la string que contiene el query de tipo insert
		@SuppressWarnings("rawtypes")
		Query query2 = session.createNativeQuery(query);

		// Rellenamos los parámetros necesarios para realizar el query de tipo insert
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



	public ArrayList<String> getMensajesPend() {
		return mensajesPend;
	}

	public void cleanMensajesPend() {
		mensajesPend.clear();
	}

	public synchronized void addMensajesPend(String mes) {
		mensajesPend.add(mes);
	}

}
