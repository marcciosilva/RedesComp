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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
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
    public final static int PACKETSIZE = 65536; 
    private byte[] ack = new byte[PACKETSIZE];
    private byte[] data = new byte[PACKETSIZE];
    //private InetAddress serverIP;
    //private int serverPort;
    private DatagramSocket socketUnicast;
    DatagramPacket paquete;
    DatagramPacket sndpkt;
    Queue<DatagramPacket> buffer = new LinkedList<DatagramPacket>();
    
    //Emisor
     private enum EstadoSender {

        ESPERO_DATA_0, ESPERO_ACK_0, ESPERO_DATA_1, ESPERO_ACK_1
    }
    private EstadoSender estadoS = EstadoSender.ESPERO_DATA_0;
    int pasoSender = 0;
    boolean timeout0Sender = false;
    boolean timeout1Sender = false;
    
    //Receptor
    
    private enum EstadoReceiver {

        ESPERO_DATA_0, ESPERO_DATA_1
    }
    private EstadoReceiver estadoR = EstadoReceiver.ESPERO_DATA_0;
    int pasoReceiver = 0;
    
    private boolean is_not_ACK(DatagramPacket rcvpkt) {
        //devuelve true si el paquete no es un acknowledge
        byte[] bytes = rcvpkt.getData();
        byte header = bytes[0];
        int isAck = (int) header & 0x80; //mask 10000000
        //devuelvo true si el bit estaba apagado
        return isAck == 0;
    }
    private boolean is_ACK(DatagramPacket rcvpkt) {
        //devuelve true si el paquete es un acknowledge
        byte[] bytes = rcvpkt.getData();
        byte header = bytes[0];
        int isAck = (int) header & 0x80; //mask 10000000
        //devuelvo true si el bit estaba prendido
        return isAck == 1;
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

    private DatagramPacket makeDatapkt(boolean is_ACK, int seqNum, byte[] data, InetAddress serverIP, int serverPort) {
        //armar paquete a enviar
        byte header = (byte) seqNum;
        if (is_ACK) {
            header = (byte) (header & 0xff);
        } else {
            header = (byte) (header & 0x7f);
        }
        byte bytes[] = {header};
        //junto el cabezal con el msj
        byte[] pktbytes = new byte[bytes.length + data.length];
        System.arraycopy(bytes, 0, pktbytes, 0, bytes.length);
        System.arraycopy(data, 0, pktbytes, bytes.length, data.length);
        return new DatagramPacket(bytes, bytes.length, serverIP, serverPort);
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
    
    public void rdt_send(String msj, InetAddress serverIP, int serverPort) {
                // hay que ver que se hace si me llega un mensaje privado mientras
                // estoy aca -lo guardo en un buffer?
                // y en el rdt_recieve antes de hacer nada que se fije si tiene cosas en ese buffer?
        data = msj.getBytes();
        try{
            boolean salir = false;
            while (!salir) {

                if (estadoS == EstadoSender.ESPERO_DATA_0){
                    // creo paquete para enviar a partir del string con numero de secuencia 0
                    paquete = makeDatapkt(false, 0, data, serverIP, serverPort);
                    socketUnicast.send(paquete);
                    //start timer de 0
                    estadoS = EstadoSender.ESPERO_ACK_0;

                }else if (estadoS == EstadoSender.ESPERO_ACK_0){
                    DatagramPacket in_pck = new DatagramPacket(ack, ack.length);
                    socketUnicast.receive(in_pck);
                    if (timeout0Sender){
                        // reenvio el paquete
                        socketUnicast.send(paquete);
                        //start timer de 0
                        
                    }else if (is_ACK(in_pck) && (has_seq0(in_pck))){
                        // stop timer de 0
                        estadoS = EstadoSender.ESPERO_DATA_1;  
                        salir = true;
                        pasoSender = 1;
                        
                    }else if(is_not_ACK(in_pck)){ //me llego un mensaje privado del servidor, lo mando al buche
                        buffer.add(in_pck);
                    } 
                    
                }else if (estadoS == EstadoSender.ESPERO_DATA_1){
                    // creo paquete para enviar a partir del string con numero de secuencia 1
                    paquete = makeDatapkt(false, 1, data, serverIP, serverPort);
                    socketUnicast.send(paquete);
                    //start timer de 1
                    estadoS = EstadoSender.ESPERO_ACK_1;
                    
                }else if (estadoS == EstadoSender.ESPERO_ACK_1){
                    DatagramPacket in_pck = new DatagramPacket(ack, ack.length);
                    socketUnicast.receive(in_pck);
                    if (timeout1Sender){
                        // reenvio el paquete
                        socketUnicast.send(paquete);
                        //start timer de 1
                        
                    }else if (is_ACK(in_pck) && (has_seq1(in_pck))){
                        // stop timer de 1
                        estadoS = EstadoSender.ESPERO_DATA_1;  
                        salir = true;
                        pasoSender = 0;
                        
                    }else if(is_not_ACK(in_pck)){ //me llego un mensaje privado del servidor, lo mando al buche
                        buffer.add(in_pck);
                    } 
                }           
            }
            
        }
        catch (IOException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    }
    
    private String rdt_rcv(DatagramPacket rcvpkt) {

        boolean salir = false;
        String strMensaje = null;
        while (!salir) {
            try {
                if (buffer.isEmpty()){ //no tengo ningun mensaje privado guardado sin procesar
                    socketUnicast.receive(rcvpkt);
                }else{
                    rcvpkt = buffer.remove();
                }
                byte[] bytes = rcvpkt.getData();
                //extraigo el header para manipularlo bitwise
                byte header = bytes[0];
                //extraigo la data y creo un string a partir de ella
                byte[] data = Arrays.copyOfRange(bytes, 1, bytes.length - 1);
                strMensaje = new String(data, 0, data.length);
                if (estadoR == EstadoReceiver.ESPERO_DATA_0) {
                    if (is_not_ACK(rcvpkt) && has_seq1(rcvpkt)) {
                        sndpkt = makepkt(true, 1);
                        socketUnicast.send(sndpkt);
                    } else if (is_not_ACK(rcvpkt) && has_seq0(rcvpkt)) {
                        //me llega lo que estoy esperando del servidor
                        sndpkt = makepkt(true, 0);
                            //broadcasteo el acknowledge
                        //si le llega a otro usuario, simplemente lo ignora
                        socketUnicast.send(sndpkt);
                        pasoReceiver = 1;
                        salir = true;
                        estadoR = EstadoReceiver.ESPERO_DATA_1;
                    }
                } else if (estadoR == EstadoReceiver.ESPERO_DATA_1) {
                    if (buffer.contains(rcvpkt)) {
                        pasoReceiver = 0;
                        salir = true;
                        estadoR = EstadoReceiver.ESPERO_DATA_0;

                    } else if (is_not_ACK(rcvpkt) && has_seq0(rcvpkt)) {
                        sndpkt = makepkt(true, 0);
                        socketUnicast.send(sndpkt);
                    } else if (is_not_ACK(rcvpkt) && has_seq1(rcvpkt)) {
                        //me llega lo que estoy esperando del servidor
                        sndpkt = makepkt(true, 0);
                            //broadcasteo el acknowledge
                        //si le llega a otro usuario, simplemente lo ignora
                        socketUnicast.send(sndpkt);
                        pasoReceiver = 1;
                        salir = true;
                        estadoR = EstadoReceiver.ESPERO_DATA_0;
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Multicast.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return strMensaje;
    }
}
