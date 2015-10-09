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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import static redes.Interfaz.PACKETSIZE;

/**
 *
 * @author marccio
 */
public class ListenerPrivados extends Thread {

    private DatagramSocket socketUnicast;
    InetAddress serverIP;
    int serverPort;
    boolean conexionAbierta = false;
    //para comunicación entre threads
    public BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    public ListenerPrivados(InetAddress serverIP, int serverPort) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    public void rdt_rcv() {
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
            Logger.getLogger(ListenerPrivados.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ListenerPrivados.class.getName()).log(Level.SEVERE, null, ex);
//            JOptionPane.showMessageDialog(Interfaz.getInstance(), "Atención! No se ha recibido una respuesta del servidor", "Interfaz", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void rdt_send(String msj) {
        synchronized (socketUnicast) {
            byte[] data = msj.getBytes();
            DatagramPacket paquete = new DatagramPacket(data, data.length, serverIP, serverPort);
            try {
                socketUnicast.send(paquete);
            } catch (IOException ex) {
                Logger.getLogger(Interfaz.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println(ex.toString());
            }
        }
    }

    @Override
    public void run() {
//        try {
//            socketUnicastLogin = new DatagramSocket();
        socketUnicast = Interfaz.getInstance().getUnicastSocket();
        conexionAbierta = true;
        while (conexionAbierta) {
            String msg;
            while ((msg = queue.poll()) != null) {
                if (msg.equals("login")) {
                    //porque sé que si vino un rdt_send vino el mensaje después
                    rdt_send(queue.poll());
                }
            }
            rdt_rcv();
        }
    }
}
