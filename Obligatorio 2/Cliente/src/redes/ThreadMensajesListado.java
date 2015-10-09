/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package redes;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author marccio
 */
public class ThreadMensajesListado extends ConfiabilidadUnicast {

    String msj;
    //para comunicación entre threads
    public BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    public ThreadMensajesListado(String msj, InetAddress serverIP, int serverPort) {
        this.msj = msj;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        socketUnicast = Interfaz.getInstance().getSocketOtros();
        synchronized (socketUnicast) {
            rdt_send(msj);
            if (msj.contains("GET_CONNECTED") || msj.contains("LOGOUT")) {
                try {
                    //voy a precisar recibir una respuesta también
                    rdt_rcv();
                } catch (IOException ex) {
                    Logger.getLogger(ThreadMensajesListado.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
