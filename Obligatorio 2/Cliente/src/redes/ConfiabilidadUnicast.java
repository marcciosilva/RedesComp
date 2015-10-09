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
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static redes.Interfaz.PACKETSIZE;

/**
 * Clase que implementa confiabilidad, para uso de los demás threads de unicast
 *
 * @author marccio
 */
public class ConfiabilidadUnicast extends Thread {

    DatagramSocket socketUnicast;
    InetAddress serverIP;
    int serverPort;
    boolean confiabilidad;

    /**
     * Envía un mensaje (String) aplicando confiabilidad
     *
     * @param msj Mensaje a enviar
     */
    public void rdt_send(String msj) {
        if (!confiabilidad) {
            byte[] data = msj.getBytes();
            DatagramPacket paquete = new DatagramPacket(data, data.length, serverIP, serverPort);
            try {
                synchronized (socketUnicast) {
                    socketUnicast.send(paquete);
                }
            } catch (IOException ex) {
                Logger.getLogger(Interfaz.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println(ex.toString());
            }
        } else {

        }
    }

    /**
     * Recibe un mensaje aplicando confiabilidad. El mensaje se comunica hacia
     * la interfaz mediante un thread DataSend.
     *
     * @throws IOException
     */
    public void rdt_rcv() throws IOException {
        if (!confiabilidad) {
            try {
                byte[] data = new byte[PACKETSIZE];
                DatagramPacket paquete = new DatagramPacket(data, data.length, serverIP, serverPort);
                // Espero por una respuesta con timeout de 2 segundos
                synchronized (socketUnicast) {
                    socketUnicast.setSoTimeout(2000);
                    socketUnicast.receive(paquete);
                }
                // Convierto el byte [] de la respuesta en un String y se lo paso
                // a DataSend para que vea que hacer con él
                String msj = new String(paquete.getData()).split("\0")[0];
                System.out.println("Unicast: " + msj);
                (new DataSend(msj)).start();
            } catch (SocketException ex) {
//                Logger.getLogger(ListenerPrivados.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                //tiro la excepcion hacia fuera para manejarla desde los threads,
                //por ejemplo cuando necesito tirar un popup porque no se recibió
                //mensaje de respuesta al LOGIN
                throw ex;
            }
        } else {
            //TODO
        }
    }

}
