package redes;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static redes.Cliente.PACKETSIZE;

/**
 * Thread que está suscrito al canal de multicast y espera permanentemente
 * mensajes del servidor.
 *
 * @author marccio
 */
public class LectorMulticast extends Thread {

    private MulticastSocket socketMulticastRecepcion;
    private DatagramSocket socketMulticastEnvio;
    final int puerto = 6789;
    InetAddress multicastAddress;
    private Estado estado;
    int paso;
    DatagramPacket out_pck; //ultimo paquete enviado
    private boolean confiabilidad;
    boolean interrumpido = false;

    private enum Estado {

        ESPERO_0, ESPERO_1
    }

    /**
     * Se crea el thread para escuchar en multicast
     *
     * @param confiabilidad Si se pasa true, se aplica rdt para multicast
     */
    public LectorMulticast(boolean confiabilidad) {
//        this.confiabilidad = confiabilidad;
        this.confiabilidad = false;
        // Este es el thread que va a escuchar por nuevos mensajes y mostrarlos en el area del chat.
        //inicializo máquina de estados
        //espero numero de secuencia 0 de la capa inferior
        estado = Estado.ESPERO_0;
        paso = 0;
    }

    //la única situación en la que se interrumpe al thread es si llegó un ACK
//    @Override
//    public void interrupt() {
//        super.interrupt();
//        interrumpido = true;
//    }
    private void rdt_rcv(DatagramPacket rcvpkt) throws IOException {
        //hasta que no me llegue algo válido para pasarle a la
        //"capa de aplicación", me quedo acá
        if (estado == Estado.ESPERO_0) {
            if (paso == 1
                    && UtilsConfiabilidad.is_not_ACK(rcvpkt)
                    && UtilsConfiabilidad.has_seq1(rcvpkt)) {
                //reenvio el mensaje de ACK anterior
                //únicamente se reenvian acknowledges desde el cliente
                socketMulticastEnvio.send(out_pck);
            } else if (UtilsConfiabilidad.is_not_ACK(rcvpkt)
                    && UtilsConfiabilidad.has_seq0(rcvpkt)) {
                String msj = UtilsConfiabilidad.extract(rcvpkt);
                UtilsConfiabilidad.deliver_msj(msj);
                //me llega lo que estoy esperando del servidor
                out_pck = UtilsConfiabilidad.makepkt(true, 0, null, multicastAddress, puerto);
                //broadcasteo el acknowledge
                //si le llega a otro usuario, simplemente lo ignora
                socketMulticastEnvio.send(out_pck);
                paso = 1;
                estado = Estado.ESPERO_1;
            }
        } else if (estado == Estado.ESPERO_1) {
            if (UtilsConfiabilidad.is_not_ACK(rcvpkt)
                    && UtilsConfiabilidad.has_seq0(rcvpkt)) {
                //broadcasteo el paquete ACK anterior
                socketMulticastEnvio.send(out_pck);
            } else if (UtilsConfiabilidad.is_not_ACK(rcvpkt)
                    && UtilsConfiabilidad.has_seq1(rcvpkt)) {
                try {
                    String msj = UtilsConfiabilidad.extract(rcvpkt);
                    UtilsConfiabilidad.deliver_msj(msj);
                    out_pck = UtilsConfiabilidad.makepkt(true, 1, null, multicastAddress, puerto);
                    socketMulticastEnvio.send(out_pck);
                    estado = Estado.ESPERO_0;
                } catch (IOException ex) {
                    Logger.getLogger(LectorMulticast.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public void run() {
        try {
            socketMulticastRecepcion = Cliente.getInstance().getMulticastSocket();
            if (confiabilidad) {
                socketMulticastEnvio = new DatagramSocket();
                multicastAddress = InetAddress.getByName(Cliente.strMulticastIP);
            }
//        while (!interrumpido) {
            while (true) { //no necesita ser interrumpido, se cierra al cerrar el socket
                byte[] buffer = new byte[PACKETSIZE];
                DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
                //variable booleana para determinar si usar rdt o no
                if (confiabilidad) {
                    try {
                        //recibo mensaje de capa de transporte artificial (con confiabilidad)
                        socketMulticastRecepcion.receive(paquete);
                        rdt_rcv(paquete);
                    } catch (IOException ex) {
                        Logger.getLogger(LectorMulticast.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    try {
                        String strMensaje;
                        socketMulticastRecepcion.receive(paquete);
                        strMensaje = new String(paquete.getData(), 0, paquete.getLength());
                        System.out.println("Multicast: " + strMensaje);
                        UtilsConfiabilidad.deliver_msj(strMensaje);
                    } catch (IOException ex) {
                        try {
                            this.join();
                        } catch (InterruptedException ex1) {
                            Logger.getLogger(LectorMulticast.class.getName()).log(Level.SEVERE, null, ex1);
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            Logger.getLogger(LectorMulticast.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            Logger.getLogger(LectorMulticast.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
