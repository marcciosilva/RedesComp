package redes;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread para mensajes que no involucran login o escucha de mensajes privados.
 * Por ejemplo MESSAGE, PRIVATE_MESSAGE, LOGOUT. Se usa para mensajes
 * individuales, o sea que comienza y termina.
 *
 * @author marccio
 */
public class EnvioUnicast extends Thread {

	String msj;
	DatagramPacket out_pck;
	boolean interrumpido = false; //marca si tengo que terminar ejecución del thread
	Cliente cliente = Cliente.getInstance();
	InetAddress serverIP;
	int serverPort;
	boolean confiabilidad;
	public final static int TIMEOUT = 5000;
	public final static int PACKETSIZE = 65536;
	protected DatagramSocket socketUnicast;
	BlockingQueue<String> buffer;

	/**
	 * Se construye el thread con el mensaje a enviar y la información del
	 * servidor
	 *
	 * @param confiabilidad Determina el uso o no de rdt
	 * @param serverIP Dirección IP del servidor
	 * @param b
	 * @param serverPort Puerto donde escucha el servidor
	 */
	public EnvioUnicast(boolean confiabilidad, InetAddress serverIP, int serverPort, BlockingQueue b) {
		this.buffer = b;
		this.confiabilidad = confiabilidad;
		this.serverIP = serverIP;
		this.serverPort = serverPort;
	}

	@Override
	public void run() {
		while (true) {
			try {
				msj = buffer.take();
				socketUnicast = cliente.getUnicastSocket();
				Cliente.cant_mensajes++;
				System.out.println("Mensaje nro: " + Cliente.cant_mensajes);
				rdt_send(msj);
				synchronized (this) {
					wait();
				}
			} catch (IOException ex) {
				Logger.getLogger(EnvioUnicast.class.getName()).log(Level.SEVERE, null, ex);
			} catch (InterruptedException ex) {
				Logger.getLogger(EnvioUnicast.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	/**
	 * Envía un mensaje (String) aplicando confiabilidad
	 *
	 * @param msj Mensaje a enviar
	 * @throws java.io.IOException
	 */
	public void rdt_send(String msj) throws IOException {
		if (!confiabilidad) {
			byte[] data = msj.getBytes();
			DatagramPacket paquete = new DatagramPacket(data, data.length, serverIP, serverPort);
			try {
				socketUnicast.send(paquete); //es thread safe, no hay que sincronizarlo

			} catch (IOException ex) {
				Logger.getLogger(EnvioUnicast.class
						.getName()).log(Level.SEVERE, null, ex);
				System.err.println(ex.toString());
			}
		} else {
			System.out.println("Entro al loop de rdt_send para enviar: " + msj);
			if (cliente.estadoSender == Cliente.EstadoSender.ESPERO_DATA_0) {
				byte[] data = msj.getBytes();
				DatagramPacket paquete = new DatagramPacket(data, data.length, serverIP, serverPort);
				out_pck = UtilsConfiabilidad.makepkt(false, 0, data, serverIP, serverPort);
				Cliente.ultimo_pkt = out_pck;
				cliente.estadoSender = Cliente.EstadoSender.ESPERO_ACK_0;
				if (!(Cliente.cant_mensajes > 1 && (Cliente.cant_mensajes % 4 == 0))) {
					socketUnicast.send(out_pck);
				} else {
					System.out.println("777777 Skipped 7777777");
				}
				Cliente.tiempo_enviado = Date.from(Instant.now());

			} else if (cliente.estadoSender == Cliente.EstadoSender.ESPERO_DATA_1) {
				byte[] data = msj.getBytes();
				DatagramPacket paquete = new DatagramPacket(data, data.length, serverIP, serverPort);
				out_pck = UtilsConfiabilidad.makepkt(false, 1, data, serverIP, serverPort);
				Cliente.ultimo_pkt = out_pck;
				cliente.estadoSender = Cliente.EstadoSender.ESPERO_ACK_1;
				if (!(Cliente.cant_mensajes > 1 && (Cliente.cant_mensajes % 4 == 0))) {
					socketUnicast.send(out_pck);
				} else {
					System.out.println("777777 Skipped 7777777");
				}
				Cliente.tiempo_enviado = Date.from(Instant.now());

			}
		}
	}
}
