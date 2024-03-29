package redes;

import javax.swing.JOptionPane;

/**
 * Thread utilizado para enviar mensajes de capa de transporte a capa de
 * aplicación. Procesa el mensaje y termina su ejecución.
 *
 * @author marccio
 */
public class DataSend extends Thread {

    /**
     * Se construye el thread y se determina qué acción tomar de acuerdo al
     * mensaje
     *
     * @param msj Mensaje a comunicar a la interfaz
     */
    public DataSend(String msj) {
        System.out.println("Msj recibido: " + msj);
        Cliente instance = Cliente.getInstance();
        if (msj.contains("LOGIN_OK")) {
            instance.comunicarOK();
        } else if (msj.contains("LOGIN_FAILED")) {
            JOptionPane.showMessageDialog(
                    instance,
                    "Ya existe un usuario con el apodo " + instance.getApodo()
                    + "\nPor favor seleccione otro apodo.",
                    "Cliente",
                    JOptionPane.INFORMATION_MESSAGE);
            instance.comunicarNoOk();
        } else if (msj.contains("GOODBYE")) {
            instance.terminarConexion();
        } else if (msj.contains("CONNECTED")) {
            instance.comunicarConectados(msj);
        } else if (msj.contains("ALIVE")) {
            instance.comunicarAlive();
        } else if (msj.contains("RELAYED_MESSAGE")) {
            instance.comunicarMensaje(msj);
        } else if (msj.contains("PRIVATE_MESSAGE")) {
            instance.comunicarMensajePrivado(msj);
        } else if (msj.contains("MP_FAILED")) {
            instance.comunicarMensajePrivadoFailed(msj);
        } else {
            instance.updateChat(msj, true, false);
        }
    }

}
