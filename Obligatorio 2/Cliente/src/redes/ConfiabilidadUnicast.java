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
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase que implementa confiabilidad, para uso de los demás threads de unicast
 *
 * @author marccio
 */
public class ConfiabilidadUnicast extends Thread {

//    DatagramSocket socketUnicast;
    Cliente cliente = Cliente.getInstance();
    InetAddress serverIP;
    int serverPort;
    boolean confiabilidad;
    private final static int TIMEOUT = 2000;
    public final static int PACKETSIZE = 65536;
    private byte[] ack = new byte[PACKETSIZE];
    //private byte[] data = new byte[PACKETSIZE];
    //private InetAddress serverIP;
    //private int serverPort;
    protected DatagramSocket socketUnicast;
    DatagramPacket paqueteRDT;
    DatagramPacket sndpkt;
    Queue<DatagramPacket> buffer = new LinkedList<>();
    DatagramPacket in_pck;

    TimerTask timerTask0;
    //running timer task as daemon thread
    Timer timer0;

    //Emisor
    private enum EstadoSender {

        ESPERO_DATA_0, ESPERO_ACK_0, ESPERO_DATA_1, ESPERO_ACK_1
    }
    private EstadoSender estadoS = EstadoSender.ESPERO_DATA_0;
    int pasoSender = 0;
    boolean timeout0Sender = false;
    boolean timeout1Sender = false;

    //Receptor
    private enum EstadoReceiver {

        ESPERO_DATA_0, ESPERO_DATA_1
    }
    private EstadoReceiver estadoR = EstadoReceiver.ESPERO_DATA_0;
    int pasoReceiver = 0;

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
//                synchronized (socketUnicast) {
                socketUnicast.send(paquete);
//                }
            } catch (IOException ex) {
                Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println(ex.toString());
            }
        } else {
            int nextSeqNum = cliente.getNextSeqNumEmisor();
            int windowSize = cliente.getMaxSeqNum();
            int base = cliente.getBaseEmisor();
            Map<Integer, DatagramPacket> bufferEmisor = cliente.bufferEmisor;
            if (nextSeqNum < base + windowSize) {
                try {
                    byte[] data = msj.getBytes();
                    DatagramPacket paquete = new DatagramPacket(data, data.length, serverIP, serverPort);
                    bufferEmisor.put(nextSeqNum, makeDatapkt(false, nextSeqNum, paquete));
                    socketUnicast.send((DatagramPacket) bufferEmisor.get(nextSeqNum));
                    if (base == nextSeqNum) { //si es el primer paquete de la ventana
                        //seteo un timer para que tras 3 segundos ejecute el
                        //reenvio de todos los paquetes de la ventana
                        int seconds = 3; //segundos que espera el timer
                        cliente.timerEmisor = new Timer(true);
                        cliente.timerEmisor.scheduleAtFixedRate(
                                new TimerTask() {
                                    public void run() {
                                        for (DatagramPacket paquete : bufferEmisor.values()) {
                                            try {
                                                //reenvio paquetes
                                                socketUnicast.send(paquete);
                                            } catch (IOException ex) {
                                                Logger.getLogger(EnvioUnicast_Timeout.class.getName()).log(Level.SEVERE, null, ex);
                                            }
                                        }
                                    }
                                }, 0, seconds * 1000);
                    }
                    cliente.setNextSeqNumEmisor(nextSeqNum + 1);
                } catch (IOException ex) {
                    Logger.getLogger(ConfiabilidadUnicast.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                refuse_data(msj); //FALTA IMPLEMENTAR
            }
        }
    }

    private void refuse_data(String msj) {

    }

    /**
     * Recibe un mensaje aplicando confiabilidad. El mensaje se comunica hacia
     * la interfaz mediante un thread DataSend.
     *
     * @throws IOException
     */
    public void rdt_rcv(DatagramPacket rcvpkt) throws IOException {
        if (!confiabilidad) {
            String msj = new String(rcvpkt.getData()).split("\0")[0];
            (new DataSend(msj)).start();
        } else {
            int expectedSeqNumReceptor
                    = cliente.getExpectedSeqNumReceptor();
            if (hasSeqNum(rcvpkt, expectedSeqNumReceptor)) {
                sndpkt = makepkt(true, expectedSeqNumReceptor);
                cliente.setExpectedSeqNumReceptor(
                        expectedSeqNumReceptor
                        % cliente.getMaxSeqNum());
                String msj = new String(rcvpkt.getData()).split("\0")[0];
                (new DataSend(msj)).start();
            }
            //si el paquete es ack (independientemente de si tiene el numSeq
            //esperado o no), se actualiza el puntero base
            //de la ventana de emisión de acuerdo a su numero de seq
            //y si la base ahora coincide con nextSeqNum,
            //se detiene el timer porque no hay nada que mandar,
            //en caso contrario se reinicia
            if (is_ACK(rcvpkt)) {
                cliente.setBaseEmisor(getSeqNum(rcvpkt) + 1);
                if (cliente.getBaseEmisor() == cliente.getNextSeqNumEmisor()) {
                    cliente.timerEmisor.cancel();
                } else {
                    //reseteo timer
                    cliente.timerEmisor.cancel();
                    cliente.timerEmisor = new Timer();
                    int seconds = 3;
                    cliente.timerEmisor.scheduleAtFixedRate(
                            new TimerTask() {
                                public void run() {
                                    for (DatagramPacket paquete : cliente.bufferEmisor.values()) {
                                        try {
                                            //reenvio paquetes
                                            socketUnicast.send(paquete);
                                        } catch (IOException ex) {
                                            Logger.getLogger(EnvioUnicast_Timeout.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    }
                                }
                            }, 0, seconds * 1000);
                }
            }
            //reenvío ack anterior si me llega algo fuera de orden
            socketUnicast.send(sndpkt);
        }
    }

    private boolean is_ACK(DatagramPacket rcvpkt) {
        //devuelve true si el paquete es un acknowledge
        byte[] bytes = rcvpkt.getData();
        byte header = bytes[0];
        int isAck = (int) header & 0x80; //mask 10000000
        //devuelvo true si el bit estaba prendido
        return isAck == 1;
    }

    private int getSeqNum(DatagramPacket rcvpkt) {
        byte[] bytes = rcvpkt.getData();
        byte header = bytes[0];
        int seqNum = (int) header & 0x7f;
        return seqNum;
    }

    private boolean hasSeqNum(DatagramPacket rcvpkt, int expectedSeqNum) {
        //devuelve true si el numero de secuencia del paquete coincide con 1
        byte[] bytes = rcvpkt.getData();
        byte header = bytes[0];
        int seqNum = (int) header & 0x7f;
        return seqNum == expectedSeqNum;
    }

    private DatagramPacket makeDatapkt(boolean is_ACK, int seqNum, DatagramPacket paquete) {
        //armar paquete a enviar
        byte header = (byte) seqNum;
        if (is_ACK) {
            header = (byte) (header & 0xff);
        } else {
            header = (byte) (header & 0x7f);
        }
        byte bytes[] = {header};
        //junto el cabezal con el msj
        byte[] pktbytes = new byte[bytes.length + paquete.getData().length];
        System.arraycopy(bytes, 0, pktbytes, 0, bytes.length);
        System.arraycopy(paquete.getData(), 0, pktbytes, bytes.length, paquete.getData().length);
        return new DatagramPacket(pktbytes, pktbytes.length, paquete.getAddress(), paquete.getPort());
    }

    private DatagramPacket makepkt(boolean is_ACK, int seqNum) {
        //armar paquete de acknowledge con numero de secuencia igual
        //a seqNum
        byte header = (byte) seqNum;
        if (is_ACK) {
            header = (byte) (header & 0xff);
        } else {
            header = (byte) (header & 0x7f);
        }
        byte bytes[] = {header};
        return new DatagramPacket(bytes, bytes.length);
    }

}
