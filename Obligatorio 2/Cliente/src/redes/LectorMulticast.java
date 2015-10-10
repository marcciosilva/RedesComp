package redes;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import static redes.Cliente.PACKETSIZE;

/**
 * Thread que está suscrito al canal de multicast y espera permanentemente mensajes del servidor.
 *
 * @author marccio
 */
public class LectorMulticast extends Thread {

	private MulticastSocket socketMulticast;
	private Estado estado;
	int paso;
	DatagramPacket sndpkt; //ultimo paquete enviado
	private boolean confiabilidad;

	private enum Estado {

		ESPERO_0, ESPERO_1
	}

	/**
	 * Se crea el thread para escuchar en multicast
	 *
	 * @param confiabilidad Si se pasa true, se aplica rdt para multicast
	 */
	public LectorMulticast(boolean confiabilidad) {
		this.confiabilidad = confiabilidad;
        // Este es el thread que va a escuchar por nuevos mensajes y mostrarlos en el area del chat.
		//inicializo máquina de estados
		//espero numero de secuencia 0 de la capa inferior
		estado = Estado.ESPERO_0;
		paso = 0;
	}

	private boolean is_not_ACK(DatagramPacket rcvpkt) {
		//devuelve true si el paquete no es un acknowledge
		byte[] bytes = rcvpkt.getData();
		byte header = bytes[0];
		int isAck = (int) header & 0x80; //mask 10000000
		//devuelvo true si el bit estaba apagado
		return isAck == 0;
	}

	private boolean has_seq1(DatagramPacket rcvpkt) {
		//devuelve true si el numero de secuencia del paquete coincide con 1
		byte[] bytes = rcvpkt.getData();
		byte header = bytes[0];
		int seqNum = (int) header & 0x7f;
		return seqNum == 1;
	}

	private boolean has_seq0(DatagramPacket rcvpkt) {
		//devuelve true si el numero de secuencia del paquete coincide con 0
		byte[] bytes = rcvpkt.getData();
		byte header = bytes[0];
		int seqNum = (int) header & 0x7f;
		return seqNum == 0;
	}

	private DatagramPacket makepkt(boolean is_ACK, int seqNum) {
        //armar paquete de acknowledge con numero de secuencia igual
		//a seqNum
		byte header = (byte) seqNum;
		if (is_ACK) {
			header = (byte) (header & 0xff);
		} else {
			header = (byte) (header & 0x7f);
		}
		byte bytes[] = {header};
		return new DatagramPacket(bytes, bytes.length);
	}

	private void rdt_rcv(DatagramPacket rcvpkt) {
		boolean salir = false;
		String strMensaje = null;
		while (!salir) {
            //hasta que no me llegue algo válido para pasarle a la
			//"capa de aplicación", me quedo acá
			try {
				socketMulticast.receive(rcvpkt);

				byte[] bytes = rcvpkt.getData();
                //extraigo el header para manipularlo bitwise
				//extraigo la data y creo un string a partir de ella
				byte[] data = Arrays.copyOfRange(bytes, 1, bytes.length - 1);
				strMensaje = new String(data, 0, data.length);
				if (estado == Estado.ESPERO_0) {
					if (is_not_ACK(rcvpkt) && has_seq1(rcvpkt)
							&& paso == 1) {
                        //reenvio el mensaje de ACK anterior
						//únicamente se reenvian acknowledges desde el cliente
						socketMulticast.send(sndpkt);
					} else if (is_not_ACK(rcvpkt) && has_seq0(rcvpkt)) {
						//me llega lo que estoy esperando del servidor
						sndpkt = makepkt(true, 0);
                        //broadcasteo el acknowledge
						//si le llega a otro usuario, simplemente lo ignora
						socketMulticast.send(sndpkt);
						paso = 1;
						salir = true;
						estado = Estado.ESPERO_1;
					}
				} else if (estado == Estado.ESPERO_1) {
					if (is_not_ACK(rcvpkt) && has_seq0(rcvpkt)) {
						//broadcasteo el paquete ACK anterior
						socketMulticast.send(sndpkt);
					} else if (is_not_ACK(rcvpkt) && has_seq1(rcvpkt)) {
						sndpkt = makepkt(true, 1);
						socketMulticast.send(sndpkt);
						salir = true;
						estado = Estado.ESPERO_0;
					}
				}
			} catch (IOException ex) {
				Logger.getLogger(LectorMulticast.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
        //genero un thread de DataSend con el mensaje que extraje del paquete
		//DataSend a su vez va a determinar qué tipo de mensaje es y va a actuar en consecuencia
		(new DataSend(strMensaje)).start();
	}

	@Override
	public void run() {
		socketMulticast = Cliente.getInstance().getMulticastSocket();
		while (true) {
			byte[] buffer = new byte[PACKETSIZE];
			DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
			//variable booleana para determinar si usar rdt o no
			if (confiabilidad) {
				//recibo mensaje de capa de transporte artificial (con confiabilidad)
				rdt_rcv(paquete);
			} else {
				try {
					String strMensaje;
					socketMulticast.receive(paquete);
					strMensaje = new String(paquete.getData(), 0, paquete.getLength());
					System.out.println("Multicast: " + strMensaje);
					(new DataSend(strMensaje)).start();
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
}
