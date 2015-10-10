package redes;

import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread que se usa para escuchar mensajes privados
 *
 * @author marccio
 */
public class LectorUnicast extends ConfiabilidadUnicast {

	boolean conexionAbierta;

	/**
	 * Construye el thread
	 *
	 * @param confiabilidad Determina el uso o no de rdt
	 * @param serverIP IP del server
	 * @param serverPort Puerto donde escucha el servidor
	 */
	public LectorUnicast(boolean confiabilidad, InetAddress serverIP, int serverPort) {
		this.confiabilidad = confiabilidad;
		this.serverIP = serverIP;
		this.serverPort = serverPort;
	}

	@Override
	public void run() {
		socketUnicast = Cliente.getInstance().getUnicastSocket();
		while (true) {
			try {
				rdt_rcv();
			} catch (IOException ex) {
				try {
					this.join();
				} catch (InterruptedException ex1) {
					Logger.getLogger(LectorMulticast.class.getName()).log(Level.SEVERE, null, ex1);
				}
			}
		}
	}
}
