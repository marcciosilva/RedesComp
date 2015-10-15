package redes;

import java.net.DatagramPacket;

/**
 * Clase que implementa confiabilidad, para uso de los demás threads de unicast
 *
 * @author marccio
 */
public class UtilsConfiabilidad {

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
        return isAck == 1;
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
        int seqNum = (int) header & 0x02; //00000010 por ahora, aunque tendría que ser 00000000
        return seqNum == 0;
    }

    public static DatagramPacket makeDatapkt(boolean is_ACK, int seqNum, DatagramPacket paquete) {
        //armar paquete a enviar
        byte header;
        if (seqNum == 0) {
            header = (byte) 0x02; //provisorio
        } else {
            header = (byte) seqNum;
        }
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

    public static DatagramPacket makepkt(boolean is_ACK, int seqNum) {
        //armar paquete de acknowledge con numero de secuencia igual
        //a seqNum
        byte header;
        if (seqNum == 0) {
            header = (byte) 0x02; //provisorio
        } else {
            header = (byte) seqNum;
        }
        if (is_ACK) {
            header = (byte) (header & 0xff);
        } else {
            header = (byte) (header & 0x7f);
        }
        byte bytes[] = {header};
        return new DatagramPacket(bytes, bytes.length);
    }

}
