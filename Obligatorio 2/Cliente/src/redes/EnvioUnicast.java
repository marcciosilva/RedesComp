package redes;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
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
public class EnvioUnicast extends Thread {

    String msj;
    DatagramPacket out_pck;
    boolean interrumpido = false; //marca si tengo que terminar ejecución del thread
    Cliente cliente = Cliente.getInstance();
    InetAddress serverIP;
    int serverPort;
    boolean confiabilidad;
    public final static int TIMEOUT = 5000;
    public final static int PACKETSIZE = 65536;
    protected DatagramSocket socketUnicast;

    /**
     * Se construye el thread con el mensaje a enviar y la información del
     * servidor
     *
     * @param confiabilidad Determina el uso o no de rdt
     * @param msj Mensaje a enviar
     * @param serverIP Dirección IP del servidor
     * @param serverPort Puerto donde escucha el servidor
     */
    public EnvioUnicast(boolean confiabilidad, String msj, InetAddress serverIP, int serverPort) {
        this.confiabilidad = confiabilidad;
        this.msj = msj;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        try {
            socketUnicast = cliente.getUnicastSocket();
            rdt_send(msj);
        } catch (IOException ex) {
            Logger.getLogger(EnvioUnicast.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //la única situación en la que se interrumpe al thread es si llegó un ACK
    @Override
    public void interrupt() {
        super.interrupt();
        interrumpido = true;
    }

    /**
     * Envía un mensaje (String) aplicando confiabilidad
     *
     * @param msj Mensaje a enviar
     * @throws java.io.IOException
     */
    public void rdt_send(String msj) throws IOException {
        if (!confiabilidad) {
            byte[] data = msj.getBytes();
            DatagramPacket paquete = new DatagramPacket(data, data.length, serverIP, serverPort);
            try {
                socketUnicast.send(paquete); //es thread safe, no hay que sincronizarlo
            } catch (IOException ex) {
                Logger.getLogger(EnvioUnicast.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println(ex.toString());
            }
        } else {
            while (!interrumpido) {
                if (cliente.estadoSender == Cliente.EstadoSender.ESPERO_DATA_0) {
                    try {
                        byte[] data = msj.getBytes();
                        DatagramPacket paquete = new DatagramPacket(data, data.length, serverIP, serverPort);
                        out_pck = UtilsConfiabilidad.makeDatapkt(false, 0, paquete);
                        socketUnicast.send(out_pck);
                        cliente.estadoSender = Cliente.EstadoSender.ESPERO_ACK_0;
                        synchronized (this) {
                            wait(TIMEOUT);
                        }
                    } catch (InterruptedException ex) {
                        //si es interrumpido es porque llegó el ACK0
                        interrumpido = true;
                        Logger.getLogger(EnvioUnicast.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else if (cliente.estadoSender == Cliente.EstadoSender.ESPERO_DATA_1) {
                    try {
                        byte[] data = msj.getBytes();
                        DatagramPacket paquete = new DatagramPacket(data, data.length, serverIP, serverPort);
                        out_pck = UtilsConfiabilidad.makeDatapkt(false, 1, paquete);
                        socketUnicast.send(out_pck);
                        cliente.estadoSender = Cliente.EstadoSender.ESPERO_ACK_1;
                        synchronized (this) {
                            wait(TIMEOUT);
                        }
                    } catch (InterruptedException ex) {
                        //si es interrumpido es porque llegó el ACK1
                        interrumpido = true;
                        Logger.getLogger(EnvioUnicast.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (cliente.estadoSender == Cliente.EstadoSender.ESPERO_ACK_0
                            || cliente.estadoSender == Cliente.EstadoSender.ESPERO_ACK_1) {
                        //si saltó el timer entro por acá, sea que espere ACK0 o ACK1
                        //reenvíos
                        socketUnicast.send(out_pck);
                        synchronized (this) {
                            try {
                                wait(TIMEOUT);
                            } catch (InterruptedException ex) {
                                interrumpido = true;
                                Logger.getLogger(EnvioUnicast.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
            }

        }
    }
}
