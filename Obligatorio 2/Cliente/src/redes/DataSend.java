/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
        Interfaz instance = Interfaz.getInstance();
        if (msj.equals("OK")) {
            instance.comunicarOK();
        } else if (msj.equals("NOK")) {
            JOptionPane.showMessageDialog(
                    instance,
                    "Ya existe un usuario con el apodo " + instance.getApodo()
                    + "\nPor favor seleccione otro apodo.",
                    "Cliente",
                    JOptionPane.INFORMATION_MESSAGE);
            instance.limpiarAreaChat();
        } else if (msj.equals("GOODBYE")) {
            instance.terminarConexion();
        } else if (msj.contains("CONNECTED")) {
            instance.comunicarConectados(msj);
        } else {
            //si es multicast
            Interfaz.getInstance().updateChat(msj, true, false);
        }
    }

}
