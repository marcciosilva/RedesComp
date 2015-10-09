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
 * Thread que se usa para hacer el login (mediante el uso del parámetro queue de
 * éste thread), y posteriormente se dedica a escuchar sobre un socket unicast
 * por mensajes privados. Se usa el mismo socket porque el servidor registra su
 * identificación al hacer el login, y con esas credenciales envía los mensajes
 * privados.
 *
 * @author marccio
 */
public class ListenerPrivados extends ConfiabilidadUnicast {

    boolean conexionAbierta;

    /**
     * Cola de mensajes para comunicarse con el thread
     */
//    public BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    /**
     * Construye el thread
     *
     * @param confiabilidad Determina el uso o no de rdt
     * @param serverIP IP del server
     * @param serverPort Puerto donde escucha el servidor
     */
    public ListenerPrivados(boolean confiabilidad, InetAddress serverIP, int serverPort) {
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
            String msg;
//                while ((msg = queue.poll()) != null) {
//                    if (msg.equals("login") || msg.equals("logout")) {
//                        //porque sé que si vino un rdt_send vino el mensaje después
//                        rdt_send(queue.poll());
//                        try {
//                            rdt_rcv();
//                        } catch (IOException ex) {
//                            Logger.getLogger(ListenerPrivados.class.getName()).log(Level.SEVERE, null, ex);
//                            JOptionPane.showMessageDialog(Interfaz.getInstance(), "Atención! No se ha recibido una respuesta del servidor", "Sin respuesta", JOptionPane.INFORMATION_MESSAGE);
//                        }
//                    }
//                }
            try {
                rdt_rcv();
            } catch (IOException ex) {
                Logger.getLogger(ListenerPrivados.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
