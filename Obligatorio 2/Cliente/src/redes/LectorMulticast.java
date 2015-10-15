package redes;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.Arrays;
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

    private MulticastSocket socketMulticast;
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
        this.confiabilidad = confiabilidad;
        // Este es el thread que va a escuchar por nuevos mensajes y mostrarlos en el area del chat.
        //inicializo máquina de estados
        //espero numero de secuencia 0 de la capa inferior
        estado = Estado.ESPERO_0;
        paso = 0;
    }

    //la única situación en la que se interrumpe al thread es si llegó un ACK
    @Override
    public void interrupt() {
        super.interrupt();
        interrumpido = true;
    }

    private void rdt_rcv(DatagramPacket rcvpkt) throws IOException {
        //hasta que no me llegue algo válido para pasarle a la
        //"capa de aplicación", me quedo acá
        if (estado == Estado.ESPERO_0) {
            if (paso == 1
                    && UtilsConfiabilidad.is_not_ACK(rcvpkt)
                    && UtilsConfiabilidad.has_seq1(rcvpkt)) {
                //reenvio el mensaje de ACK anterior
                //únicamente se reenvian acknowledges desde el cliente
                socketMulticast.send(out_pck);
            } else if (UtilsConfiabilidad.is_not_ACK(rcvpkt)
                    && UtilsConfiabilidad.has_seq0(rcvpkt)) {
                String msj = UtilsConfiabilidad.extract(rcvpkt);
                UtilsConfiabilidad.deliver_msj(msj);
                //me llega lo que estoy esperando del servidor
                out_pck = UtilsConfiabilidad.makepkt(true, 0);
                //broadcasteo el acknowledge
                //si le llega a otro usuario, simplemente lo ignora
                socketMulticast.send(out_pck);
                paso = 1;
                estado = Estado.ESPERO_1;
            }
        } else if (estado == Estado.ESPERO_1) {
            if (UtilsConfiabilidad.is_not_ACK(rcvpkt)
                    && UtilsConfiabilidad.has_seq0(rcvpkt)) {
                //broadcasteo el paquete ACK anterior
                socketMulticast.send(out_pck);
            } else if (UtilsConfiabilidad.is_not_ACK(rcvpkt)
                    && UtilsConfiabilidad.has_seq1(rcvpkt)) {
                try {
                    String msj = UtilsConfiabilidad.extract(rcvpkt);
                    UtilsConfiabilidad.deliver_msj(msj);
                    out_pck = UtilsConfiabilidad.makepkt(true, 1);
                    socketMulticast.send(out_pck);
                    estado = Estado.ESPERO_0;
                } catch (IOException ex) {
                    Logger.getLogger(LectorMulticast.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public void run() {
        socketMulticast = Cliente.getInstance().getMulticastSocket();
        while (!interrumpido) {
            byte[] buffer = new byte[PACKETSIZE];
            DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
            //variable booleana para determinar si usar rdt o no
            if (confiabilidad) {
                try {
                    //recibo mensaje de capa de transporte artificial (con confiabilidad)
                    socketMulticast.receive(paquete);
                    rdt_rcv(paquete);
                } catch (IOException ex) {
                    Logger.getLogger(LectorMulticast.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                try {
                    String strMensaje;
                    socketMulticast.receive(paquete);
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
    }
}
