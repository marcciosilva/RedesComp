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
    private int serverPort;
    private byte[] mensaje = new byte[PACKETSIZE];
    private InetAddress serverIP;
    private DatagramSocket socketUnicast;
    
     private enum Estado {

        ESPERO_DATA_0, ESPERO_ACK_0, ESPERO_DATA_1, ESPERO_ACK_1
    }
    private Estado estado = Estado.ESPERO_DATA_0;
    int paso = 0;
    boolean timeout0 = false;
    boolean timeout1 = false;
    
    
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

    public void rdt_send(DatagramPacket paquete) {
        //tal vez deberia llegarme solo un string con los datos y aca armar el DatagramPackeet  

                //serverIP = paquete.getAddress();
                //serverPort = paquete.getPort();
        try{
            boolean salir = false;
            while (!salir) {

                if (estado == Estado.ESPERO_DATA_0){
                    // creo paquete para enviar a partir del string con numero de secuencia 0
                    socketUnicast.send(paquete);
                    //start timer de 0
                    estado = Estado.ESPERO_ACK_0;

                }else if (estado == Estado.ESPERO_ACK_0){
                    DatagramPacket in_pck = new DatagramPacket(mensaje, mensaje.length);
                    socketUnicast.receive(in_pck);
                    if (timeout0){
                        // reenvio el paquete
                        socketUnicast.send(paquete);
                        //start timer de 0
                        
                    }else if (is_ACK(in_pck) && (has_seq0(in_pck))){
                        // stop timer de 0
                        estado = Estado.ESPERO_DATA_1;  
                        salir = true;
                    } 
                    
                }else if (estado == Estado.ESPERO_DATA_1){
                    // creo paquete para enviar a partir del string con numero de secuencia 1
                    socketUnicast.send(paquete);
                    //start timer de 1
                    estado = Estado.ESPERO_ACK_1;
                    
                }else if (estado == Estado.ESPERO_ACK_1){
                    DatagramPacket in_pck = new DatagramPacket(mensaje, mensaje.length);
                    socketUnicast.receive(in_pck);
                    if (timeout0){
                        // reenvio el paquete
                        socketUnicast.send(paquete);
                        //start timer de 1
                        
                    }else if (is_ACK(in_pck) && (has_seq1(in_pck))){
                        // stop timer de 1
                        estado = Estado.ESPERO_DATA_1;  
                        salir = true;
                    } 
                }           
            }
            
        }
        catch (IOException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    }
}
