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
 * Thread para mensajes que no involucran login o escucha de mensajes privados.
 * Por ejemplo MESSAGE, PRIVATE_MESSAGE, LOGOUT. Se usa para mensajes
 * individuales, o sea que comienza y termina.
 *
 * @author marccio
 */
public class LoginLogout extends ConfiabilidadUnicast {

    String msj;

    /**
     * Se construye el thread con el mensaje a enviar y la información del
     * servidor
     *
     * @param confiabilidad Determina el uso o no de rdt
     * @param msj Mensaje a enviar
     * @param serverIP Dirección IP del servidor
     * @param serverPort Puerto donde escucha el servidor
     */
    public LoginLogout(boolean confiabilidad, String msj, InetAddress serverIP, int serverPort) {
        this.confiabilidad = confiabilidad;
        this.msj = msj;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        socketUnicast = Interfaz.getInstance().getUnicastSocket();
        synchronized (socketUnicast) {
            rdt_send(msj);
            if (msj.contains("LOGIN") || msj.contains("LOGOUT")) {
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
