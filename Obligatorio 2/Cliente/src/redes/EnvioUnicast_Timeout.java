/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package redes;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author marccio
 */
public class EnvioUnicast_Timeout extends Thread {

    @Override
    public void run() {
        Cliente cliente = Cliente.getInstance();
        DatagramSocket socketUnicast = cliente.getUnicastSocket();
        Map<Integer, DatagramPacket> bufferEmisor = cliente.bufferEmisor;
        for (DatagramPacket paquete : bufferEmisor.values()) {
            try {
                //reenvio paquetes
                socketUnicast.send(paquete);
            } catch (IOException ex) {
                Logger.getLogger(EnvioUnicast_Timeout.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
