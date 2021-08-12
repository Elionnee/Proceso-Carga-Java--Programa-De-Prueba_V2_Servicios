package proceso_carga;

public interface WatcherI {

	/**
	 * Crear un observador para el directorio indicado
	 * Indicar los tipos de eventos que se deben observar en este directorio
	 * Observar el directorio consatantemente hasta que se produzca uno de estos eventos y notificar del mismo cuando se produzca
	 * Notificar de cualquier error
	 */

	/**
	 * M�todo que se encarga de monitorizar e informar de cambios en el directorio
	 * 
	 * @throws InterruptedException Se lanza cuando el m�todo sufre una interrupci�n inesperada
	 */
	public void watchService(ConnectionDB con) throws InterruptedException;


}
