package redes;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static redes.Cliente.PACKETSIZE;
import static redes.Cliente.updateChat;

public class Listener implements Runnable {

    private InetAddress multicastIP;
    private int multicastPort = 6789; // No cambiar! Debe ser el mismo en el servidor.
    private byte[] mensaje = new byte[PACKETSIZE];
    private String strMulticastIP = "225.5.4.3";
    private MulticastSocket socketMulticast;

    private enum Estado {

        ESPERO_0, ESPERO_1
    }
    private Estado estado;
    int paso;
    DatagramPacket sndpkt; //ultimo paquete enviado
    private boolean confiabilidad = false;

    Listener() {
    }

    public void terminarConexion() {
        if (socketMulticast != null && !socketMulticast.isClosed()) {
            socketMulticast.close();
        }
    }

    private boolean is_not_ACK(DatagramPacket rcvpkt) {
        //devuelve true si el paquete no es un acknowledge
        byte[] bytes = rcvpkt.getData();
        byte header = bytes[0];
        int isAck = (int) header & 0x80; //mask 10000000
        //devuelvo true si el bit estaba apagado
        return isAck == 0;
    }

    private boolean is_ACK(DatagramPacket rcvpkt, int num) {
        //devuelve true si el paquete es un acknowledge
        //con numero de secuencia num
        //matcheo el cabezal y el resto no interesa
        Pattern pattern = Pattern.compile("/RDT/(\\d)-(\\d)/RDT/.*");
        String strMensaje = new String(rcvpkt.getData(), 0, rcvpkt.getLength());
        Matcher matcher = pattern.matcher(strMensaje);
        if (matcher.matches()) {
            String ack = matcher.group(1);
            String seqNum = matcher.group(2);
            return (ack.equals("1") && seqNum.equals(String.valueOf(num)));
        } else {
            throw new IllegalArgumentException("El paquete no tenía un cabezal RDT correcto");
        }
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

    private String rdt_rcv(DatagramPacket rcvpkt) {
        boolean salir = false;
        String strMensaje = null;
        while (!salir) {
            //hasta que no me llegue algo válido para pasarle a la
            //"capa de aplicación", me quedo acá
            try {
                socketMulticast.receive(rcvpkt);

                byte[] bytes = rcvpkt.getData();
                //extraigo el header para manipularlo bitwise
                byte header = bytes[0];
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
                    }
                } else if (estado == Estado.ESPERO_1) {
                    if (is_not_ACK(rcvpkt) && has_seq0(rcvpkt)) {
                        //broadcasteo el paquete ACK anterior
                        socketMulticast.send(sndpkt);
                    } else if (is_not_ACK(rcvpkt) && has_seq1(rcvpkt)) {
                        sndpkt = makepkt(true, 1);
                        socketMulticast.send(sndpkt);
                        salir = true;
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return strMensaje;
    }

    @Override
    public void run() {
		// Este es el thread que va a escuchar por nuevos mensajes y mostrarlos en el area del chat.

        // Fijo la dirección ip y el puerto de donde voy a escuchar los mensajes. IP 225.5.4.<nro_grupo> puerto 6789
        try {
            //inicializo máquina de estados
            //espero numero de secuencia 0 de la capa inferior
            estado = Estado.ESPERO_0;
            paso = 0;
            //inicializo socket multicast
            multicastIP = InetAddress.getByName(strMulticastIP);
            socketMulticast = new MulticastSocket(multicastPort);
            socketMulticast.joinGroup(multicastIP);
            // Loop
            while (true) {
                DatagramPacket paquete = new DatagramPacket(mensaje, mensaje.length);
                String strMensaje;
                //variable booleana para determinar si usar rdt o no
                if (confiabilidad) {
                    //recibo mensaje de capa de transporte artificial (con confiabilidad)
                    strMensaje = rdt_rcv(paquete);
                } else {
                    socketMulticast.receive(paquete);
                    strMensaje = new String(paquete.getData(), 0, paquete.getLength());
                }
                // Se debe parsear el datagrama para obtener el apodo y el mensaje por separado
                // para mostrarlos en el area de chat.
                updateChat(strMensaje, true, false);
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
