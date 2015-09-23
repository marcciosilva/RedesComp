package redes;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import static redes.Cliente.PACKETSIZE;

public class Listener implements Runnable {

    private final int multicastPort = 6789;
    private InetAddress multicastIP;
    private MulticastSocket multicastSocket;
    private JTextArea jTextAreaChat;

    Listener(JTextArea jTextAreaChat) {
        this.jTextAreaChat = jTextAreaChat;
    }

    @Override
    public void run() {
		// Este es el thread que va a escuchar por nuevos mensajes y mostrarlos en el area del chat.

        // Fijo la dirección ip de donde voy a escuchar los mensajes 225.5.4.<nro_grupo>
        try {
			multicastIP = InetAddress.getByName("225.5.4.3");
            multicastSocket = new MulticastSocket(multicastPort);
            multicastSocket.joinGroup(multicastIP);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }

        while (true) {
            try {
				
                byte[] mensajes = new byte[PACKETSIZE];
                DatagramPacket paquete = new DatagramPacket(mensajes, mensajes.length);
                multicastSocket.receive(paquete);
				System.out.println("YAY !!!!!!!!!!!!!!!");
                String strMensaje = new String(paquete.getData(), 0, paquete.getLength());

                // Se debe parsear el datagrama para obtener el apodo y el mensaje por separado
                // para mostrarlos en el area de chat.
                synchronized (jTextAreaChat) {
                    jTextAreaChat.append(strMensaje + "\n");
                    jTextAreaChat.updateUI();
                }
            } catch (IOException ex) {
                Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
