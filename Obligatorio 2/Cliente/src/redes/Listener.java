package redes;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static redes.Cliente.PACKETSIZE;
import static redes.Cliente.updateChat;

public class Listener implements Runnable {

	private InetAddress multicastIP;
	private int multicastPort = 6789; // No cambiar! Debe ser el mismo en el servidor.
	private byte[] mensaje = new byte[PACKETSIZE];
	private String strMulticastIP = "225.5.4.3";
	private MulticastSocket socketMulticast;

	Listener() {
	}

	public void terminarConexion() {
		if (socketMulticast != null && !socketMulticast.isClosed()) {
			socketMulticast.close();
		}
	}

	@Override
	public void run() {
		// Este es el thread que va a escuchar por nuevos mensajes y mostrarlos en el area del chat.

		// Fijo la dirección ip y el puerto de donde voy a escuchar los mensajes. IP 225.5.4.<nro_grupo> puerto 6789
		try {
			multicastIP = InetAddress.getByName(strMulticastIP);
			socketMulticast = new MulticastSocket(multicastPort);
			socketMulticast.joinGroup(multicastIP);
			// Loop
			while (true) {
				try {
					DatagramPacket paquete = new DatagramPacket(mensaje, mensaje.length);
					socketMulticast.receive(paquete);
					String strMensaje = new String(paquete.getData(), 0, paquete.getLength());

					// Se debe parsear el datagrama para obtener el apodo y el mensaje por separado
					// para mostrarlos en el area de chat.
					updateChat(strMensaje, true, false);
				} catch (SocketException ex) {
					Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		} catch (UnknownHostException ex) {
			Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
