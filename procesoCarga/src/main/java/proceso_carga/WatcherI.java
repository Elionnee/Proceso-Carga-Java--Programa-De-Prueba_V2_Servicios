package proceso_carga;

/**
 * Interfaz de la clase observador que monitoriza el directorio indicado e informa de cualquier cambio que pueda ocurrir
 * @author snc
 *
 */
public interface WatcherI {

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
	public void watchService(ConnectionDB con) throws InterruptedException;


}
