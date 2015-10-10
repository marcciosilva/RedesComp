/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package redes;
/**
 * Clase principal del proyecto, se encarga de obtener una instancia de la
 * interfaz y mostrarla
 * 
 * @author marccio
 */
public class Cliente {

//    Interfaz ventanaPrincipal = Interfaz.getInstance();
    /**
     * Inicializa una instancia de la interfaz
     *
     * @param args
     */
    public static void main(String args[]) {
        // Set look and Feel
        try {
            javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
        }

        // Creo jFrame
        Interfaz v = Interfaz.getInstance();
        v.setLocationRelativeTo(null);
        v.setVisible(true);
    }
}
