/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package redes;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static redes.Cliente.PACKETSIZE;

/**
 *
 * @author User
 */
public class rdtUnicast {
    
    private int serverPort;
    private byte[] mensaje = new byte[PACKETSIZE];
    private InetAddress serverIP;
    private DatagramSocket socketUnicast;
    
     private enum Estado {

        ESPERO_DATA_0, ESPERO_ACK_0, ESPERO_DATA_1, ESPERO_ACK_1
    }
    private Estado estado = Estado.ESPERO_DATA_0;
    int paso = 0;
    DatagramPacket sndpkt; //ultimo paquete enviado
    
    
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

    public void rdt_send(DatagramPacket paquete) {
        //tal vez deberia llegarme solo un string con los datos y aca armar el DatagramPackeet  

                //serverIP = paquete.getAddress();
                //serverPort = paquete.getPort();
        boolean salir = false;
        while (!salir) {
            
            if (estado == Estado.ESPERO_DATA_0){
                
            
            }
            
            
        }
    }
    
}
