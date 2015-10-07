package redes;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
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

    Listener() {
    }

    public void terminarConexion() {
        if (socketMulticast != null && !socketMulticast.isClosed()) {
            socketMulticast.close();
        }
    }

    private boolean is_not_ACK(DatagramPacket rcvpkt) {
        //devuelve true si el paquete no es un acknowledge
        //matcheo el cabezal y el resto no interesa
        Pattern pattern = Pattern.compile("/RDT/(\\d)-\\d/RDT/.*");
        String strMensaje = new String(rcvpkt.getData(), 0, rcvpkt.getLength());
        Matcher matcher = pattern.matcher(strMensaje);
        if (matcher.matches()) {
            String ack = matcher.group(1);
            return (ack.equals("0"));
        } else {
            throw new IllegalArgumentException("El paquete no tenía un cabezal RDT correcto");
        }
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

    private boolean has_seq(DatagramPacket rcvpkt, int num) {
        //devuelve true si el numero de secuencia del paquete coincide con num
        Pattern pattern = Pattern.compile("/RDT/\\d-(\\d)/RDT/.*");
        String strMensaje = new String(rcvpkt.getData(), 0, rcvpkt.getLength());
        Matcher matcher = pattern.matcher(strMensaje);
        if (matcher.matches()) {
            String seqNum = matcher.group(1);
            return (seqNum.equals(String.valueOf(num)));
        } else {
            throw new IllegalArgumentException("El paquete no tenía un cabezal RDT correcto");
        }
    }

    private DatagramPacket makepkt(boolean is_ACK, int seqNum) {
        //armar paquete de acknowledge con numero de secuencia igual
        //a seqNum
        //TODO
        //el string va a ser /RDT/ackbit-seqnum/RDT/, donde ackbit y seqnum son 0 o 1
        String ackString;
        if (is_ACK) {
            ackString = "1";
        } else {
            ackString = "0";
        }
        String mensajeString = "/RDT/" + ackString + "-" + String.valueOf(seqNum) + "/RDT/";
        byte[] mensaje = mensajeString.getBytes();
        return new DatagramPacket(mensaje, mensaje.length);
    }

    private void rdt_rcv(DatagramPacket rcvpkt) {
        boolean salir = false;
        while (!salir) {
            //hasta que no me llegue algo válido para pasarle a la
            //"capa de aplicación", me quedo acá
            try {
                socketMulticast.receive(rcvpkt);
                if (estado == Estado.ESPERO_0) {
                    if (is_not_ACK(rcvpkt) && has_seq(rcvpkt, 1)
                            && paso == 1) {
                        //reenvio el mensaje de ACK anterior
                        //únicamente se reenvian acknowledges desde el cliente
                        socketMulticast.send(sndpkt);
                    } else if (is_not_ACK(rcvpkt) && has_seq(rcvpkt, 0)) {
                        //me llega lo que estoy esperando del servidor
                        sndpkt = makepkt(true, 0);
                        //broadcasteo el acknowledge
                        //si le llega a otro usuario, simplemente lo ignora
                        socketMulticast.send(sndpkt);
                        paso = 1;
                        salir = true;
                    }
                } else if (estado == Estado.ESPERO_1) {
                    if (is_not_ACK(rcvpkt) && has_seq(rcvpkt, 0)) {
                        //broadcasteo el paquete anterior
                        socketMulticast.send(sndpkt);
                    } else if (is_not_ACK(rcvpkt) && has_seq(rcvpkt, 1)) {
                        sndpkt = makepkt(true, 1);
                        socketMulticast.send(sndpkt);
                        salir = true;
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
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
                //recibo mensaje de capa de transporte artificial (con confiabilidad)
                rdt_rcv(paquete);
                String strMensaje = new String(paquete.getData(), 0, paquete.getLength());
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
