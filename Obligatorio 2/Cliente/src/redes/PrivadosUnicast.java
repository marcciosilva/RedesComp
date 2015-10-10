/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package redes;

import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread que se usa para escuchar mensajes privados
 *
 * @author marccio
 */
public class PrivadosUnicast extends ConfiabilidadUnicast {

    boolean conexionAbierta;

    /**
     * Construye el thread
     *
     * @param confiabilidad Determina el uso o no de rdt
     * @param serverIP IP del server
     * @param serverPort Puerto donde escucha el servidor
     */
    public PrivadosUnicast(boolean confiabilidad, InetAddress serverIP, int serverPort) {
        this.confiabilidad = confiabilidad;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        conexionAbierta = false;
    }

    @Override
    public void run() {
        socketUnicast = Interfaz.getInstance().getUnicastSocket();
        conexionAbierta = true;
        while (conexionAbierta) {
            try {
                rdt_rcv();
            } catch (IOException ex) {
                Logger.getLogger(PrivadosUnicast.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
