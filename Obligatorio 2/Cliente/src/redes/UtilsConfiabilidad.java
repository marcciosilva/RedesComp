package redes;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;

/**
 * Clase que implementa confiabilidad, para uso de los demás threads de unicast
 *
 * @author marccio
 */
public class UtilsConfiabilidad {

    public static void deliver_msj(String msj) {
        //envía mensaje a la capa de aplicación generando un thread DataSend
        (new DataSend(msj)).start();
    }

    public static String extract(DatagramPacket pkt) {
        //extrae el string del mensaje sin contar la cabecera
        byte[] bytes = pkt.getData();
        byte[] data = Arrays.copyOfRange(bytes, 1, bytes.length - 1);
        System.out.println("Largo de la data sin el header : " + String.valueOf(data.length));
        byte fin_deLinea = 0x00;
        int i = 0;
        while (data[i] != fin_deLinea) {
            i++;
        }
        i++;
        return new String(data, 0, i);
    }

    public static boolean is_not_ACK(DatagramPacket rcvpkt) {
        //devuelve true si el paquete no es un acknowledge
        byte[] bytes = rcvpkt.getData();
        byte header = bytes[0];
        int isAck = (int) header & 0x80; //mask 10000000
        //devuelvo true si el bit estaba apagado
        return isAck == 0;
    }

    public static boolean is_ACK(DatagramPacket rcvpkt) {
        //devuelve true si el paquete es un acknowledge
        byte[] bytes = rcvpkt.getData();
        byte header = bytes[0];
        int isAck = (int) header & 0x80; //mask 10000000
        //devuelvo true si el bit estaba prendido
        return isAck == 128;
    }

    public static boolean has_seq1(DatagramPacket rcvpkt) {
        //devuelve true si el numero de secuencia del paquete coincide con 1
        byte[] bytes = rcvpkt.getData();
        byte header = bytes[0];
        int seqNum = (int) header & 0x01; //00000001
        return seqNum == 1;
    }

    public static boolean has_seq0(DatagramPacket rcvpkt) {
        //devuelve true si el numero de secuencia del paquete coincide con 0
        byte[] bytes = rcvpkt.getData();
        byte header = bytes[0];
        int seqNum = (int) header & 0x01;
        return seqNum == 0;
    }

    public static DatagramPacket makeDatapkt(boolean is_ACK, int seqNum, DatagramPacket paquete) {
        //armar paquete a enviar
        byte header = (byte) seqNum;
        if (is_ACK) {
            header = (byte) (header | 0x80);
        }
        byte bytes[] = {header};
        //junto el cabezal con el msj
        byte[] pktbytes = new byte[bytes.length + paquete.getData().length];
        System.arraycopy(bytes, 0, pktbytes, 0, bytes.length);
        System.arraycopy(paquete.getData(), 0, pktbytes, bytes.length, paquete.getData().length);
        return new DatagramPacket(pktbytes, pktbytes.length, paquete.getAddress(), paquete.getPort());
    }

    public static DatagramPacket makepkt_multicast(boolean is_ACK, int seqNum) {
        //armar paquete de acknowledge con numero de secuencia igual
        //a seqNum
        byte header = (byte) seqNum;
        if (is_ACK) {
            header = (byte) (header | 0x80);
        }
        byte bytes[] = {header};
        return new DatagramPacket(bytes, bytes.length);
    }

    public static DatagramPacket makepkt(boolean is_ACK, int seqNum, InetAddress address, int port) {
        //armar paquete de acknowledge con numero de secuencia igual
        //a seqNum
        byte header = (byte) seqNum;
        if (is_ACK) {
            header = (byte) (header | 0x80);
        }
        byte bytes[] = {header};
        return new DatagramPacket(bytes, bytes.length, address, port);
    }

}
