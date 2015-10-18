package redes;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread que se usa para escuchar mensajes privados
 *
 * @author marccio
 */
public class LectorUnicast extends Thread {

    boolean conexionAbierta;
    DatagramPacket sndpkt;
    Cliente cliente = Cliente.getInstance();
    InetAddress serverIP;
    int serverPort;
    boolean confiabilidad;
    public final static int TIMEOUT = 2000;
    public final static int PACKETSIZE = 65536;
    protected DatagramSocket socketUnicast;
    boolean interrumpido = false;
    //Receptor

    protected enum EstadoReceiver {

        ESPERO_DATA_0, ESPERO_DATA_1
    }
    protected EstadoReceiver estadoReceiver;

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

    //la única situación en la que se interrumpe al thread es si llegó un ACK
//    @Override
//    public void interrupt() {
//        super.interrupt();
//        interrumpido = true;
//    }
    @Override
    public void run() {
        estadoReceiver = EstadoReceiver.ESPERO_DATA_0;
        socketUnicast = cliente.getUnicastSocket();
        while (true) {
            try {
                byte[] data = new byte[PACKETSIZE];
                DatagramPacket paquete = new DatagramPacket(data, data.length, serverIP, serverPort);
                socketUnicast.receive(paquete);
                rdt_rcv(paquete);
            } catch (IOException ex) {
                try {
                    //cuando se termine la conexión y se cierre el socket
                    //va a salir por acá; en cualquier otra situación, se sale
                    //con un interrupt
                    this.join();
                } catch (InterruptedException ex1) {
                    Logger.getLogger(LectorMulticast.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        }
    }

    /**
     * Recibe un mensaje aplicando confiabilidad. El mensaje se comunica hacia
     * la interfaz mediante un thread DataSend.
     *
     * @param in_pck
     * @throws IOException
     */
    public void rdt_rcv(DatagramPacket in_pck) throws IOException {
        if (!confiabilidad) {
            String msj = new String(in_pck.getData()).split("\0")[0];
            UtilsConfiabilidad.deliver_msj(msj);
        } else {
            if (UtilsConfiabilidad.is_ACK(in_pck)) { //acknowledge para mensaje enviado por el sender
                System.out.println("Me llegó un ACK");
                synchronized (cliente.estadoSender) {
                    if (cliente.estadoSender == Cliente.EstadoSender.ESPERO_ACK_0
                            && UtilsConfiabilidad.has_seq0(in_pck)
                            || cliente.estadoSender == Cliente.EstadoSender.ESPERO_ACK_1
                            && UtilsConfiabilidad.has_seq1(in_pck)) {
                        cliente.envioFinalizado();
                    }
                }
            } else {
                if (estadoReceiver == EstadoReceiver.ESPERO_DATA_0) {
                    if (UtilsConfiabilidad.has_seq1(in_pck)) {
                        sndpkt = UtilsConfiabilidad.makepkt(true, 1, null, serverIP, serverPort);
                        socketUnicast.send(sndpkt);
                        System.out.println("Me llegó un mensaje con un seqNum que no esperaba");
                    } else if (UtilsConfiabilidad.has_seq0(in_pck)) {
                        String msj = UtilsConfiabilidad.extract(in_pck);
                        UtilsConfiabilidad.deliver_msj(msj);
                        sndpkt = UtilsConfiabilidad.makepkt(true, 0, null, serverIP, serverPort);
                        socketUnicast.send(sndpkt);
                        estadoReceiver = EstadoReceiver.ESPERO_DATA_1;
                        System.out.println("Me llegó un mensaje con un seqNum que esperaba (0) y es: " + msj);
                    }
                } else if (estadoReceiver == EstadoReceiver.ESPERO_DATA_1) {
                    if (UtilsConfiabilidad.has_seq0(in_pck)) {
                        sndpkt = UtilsConfiabilidad.makepkt(true, 0, null, serverIP, serverPort);
                        socketUnicast.send(sndpkt);
                        System.out.println("Me llegó un mensaje con un seqNum que no esperaba");
                    } else if (UtilsConfiabilidad.has_seq1(in_pck)) {
                        String msj = UtilsConfiabilidad.extract(in_pck);
                        UtilsConfiabilidad.deliver_msj(msj);
                        sndpkt = UtilsConfiabilidad.makepkt(true, 1, null, serverIP, serverPort);
                        socketUnicast.send(sndpkt);
                        estadoReceiver = EstadoReceiver.ESPERO_DATA_0;
                        System.out.println("Me llegó un mensaje con un seqNum que esperaba (1) y es: " + msj);
                    }
                }
            }
        }
    }

}
