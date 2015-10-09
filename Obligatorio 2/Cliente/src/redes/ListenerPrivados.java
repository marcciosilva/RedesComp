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
public class ListenerPrivados extends ConfiabilidadUnicast {

    boolean conexionAbierta = false;
    //para comunicación entre threads
    public BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    public ListenerPrivados(InetAddress serverIP, int serverPort) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        socketUnicast = Interfaz.getInstance().getUnicastSocket();
        conexionAbierta = true;
        while (conexionAbierta) {
            String msg;
            synchronized (socketUnicast) {
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
}
