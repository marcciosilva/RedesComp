/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package redes;

import javax.swing.JOptionPane;

/**
 *
 * @author marccio
 */
public class DataSend extends Thread {

    public DataSend(String msj) {
        System.out.println("Datasend: " + msj);
        Interfaz instance = Interfaz.getInstance();
        if (msj.equals("OK")) {
            instance.comunicarOK();
        } else if (msj.equals("NOK")) {
            JOptionPane.showMessageDialog(instance, "Ya existe un usuario con el apodo " + instance.getApodo() + "\nPor favor seleccione otro apodo.", "Cliente", JOptionPane.INFORMATION_MESSAGE);
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
