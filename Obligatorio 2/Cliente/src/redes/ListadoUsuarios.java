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
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

/**
 *
 * @author marccio
 */
public class ListadoUsuarios extends Thread {

    private final JTextArea jTextAreaChat;
    private DatagramSocket socketCliente;
    private MulticastSocket multicastSocket;
    public final static int PACKETSIZE = 1024;
    private byte[] dataOut = new byte[PACKETSIZE];
    private final InetAddress serverIP;
    private final int serverPort;

    public ListadoUsuarios(JTextArea jTextAreaChat, InetAddress serverIP, int serverPort) {
        this.jTextAreaChat = jTextAreaChat;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        // Intento abrir un DatagramSocket
        try {
            socketCliente = new DatagramSocket();
        } catch (SocketException ex) {
            Logger.getLogger(ListadoUsuarios.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(jTextAreaChat.getParent(), "Error! No se pudo abrir un puerto para la conección", "Error", JOptionPane.INFORMATION_MESSAGE);
        }

        // Construyo el paquete y lo envío "vacío" porque es solo para poder establecer la conexión. El número de puerto
        // del cliente y la IP van incluídos en el datagrama por defecto.
        dataOut = ("GET_CONNECTED" + "\n").getBytes();
        DatagramPacket paquete = new DatagramPacket(dataOut, dataOut.length, serverIP, serverPort);
        try {
            socketCliente.send(paquete);
        } catch (IOException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Espero por una respuesta con timeout de 2 segundos
        try {
            paquete.setData(new byte[PACKETSIZE]);
            socketCliente.setSoTimeout(2000);
            socketCliente.receive(paquete);
        } catch (IOException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(jTextAreaChat.getParent(), "Error! No se ha recibido una respuesta del servidor", "Error", JOptionPane.INFORMATION_MESSAGE);
        }

        String mensajeRecibido = new String(paquete.getData(), 0, paquete.getLength());

        synchronized (jTextAreaChat) {
            jTextAreaChat.append(mensajeRecibido);
        }

    }

}
