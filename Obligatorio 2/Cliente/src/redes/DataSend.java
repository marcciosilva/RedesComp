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
        System.out.println("Datasend: " + msj);
        Cliente instance = Cliente.getInstance();
        if (msj.equals("LOGIN_OK")) {
            instance.comunicarOK();
        } else if (msj.equals("LOGIN_FAILED")) {
            JOptionPane.showMessageDialog(
                    instance,
                    "Ya existe un usuario con el apodo " + instance.getApodo()
                    + "\nPor favor seleccione otro apodo.",
                    "Cliente",
                    JOptionPane.INFORMATION_MESSAGE);
            instance.comunicarNoOk();
            //limpio chat
            instance.updateChat(null, false, true);
        } else if (msj.equals("GOODBYE")) {
            instance.terminarConexion();
        } else if (msj.contains("CONNECTED")) {
            instance.comunicarConectados(msj);
        } else if (msj.equals("ALIVE")) {
            //ignorar
        } else {
            //si es multicast
            Cliente.getInstance().updateChat(msj, true, false);
        }
    }

}
