package redes;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import static redes.ConfiabilidadUnicast.PACKETSIZE;

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
        socketUnicast = cliente.getUnicastSocket();
        while (true) {
            try {
                byte[] data = new byte[PACKETSIZE];
                DatagramPacket paquete = new DatagramPacket(data, data.length, serverIP, serverPort);
                socketUnicast.receive(paquete);
                rdt_rcv(paquete);
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
