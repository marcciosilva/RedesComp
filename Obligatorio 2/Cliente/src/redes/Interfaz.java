package redes;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.apache.commons.validator.routines.InetAddressValidator;

/**
 * Interfaz del cliente de chat, que además controla threads y sockets
 *
 * @author marccio
 */
public class Interfaz extends javax.swing.JFrame {

    private static Interfaz instance = null;

    /**
     * Devuelve instancia de la interfaz
     *
     * @return Instancia de la interfaz
     */
    public static Interfaz getInstance() {
        if (instance == null) {
            instance = new Interfaz();
        }
        return instance;
    }

    private Interfaz() {
        initComponents();
        // Botón 'Enviar' como predeterminado al apretar 'Enter'
        getRootPane().setDefaultButton(jButtonEnviar);

    }

    private boolean okIP(String ip) {
        InetAddressValidator validator = InetAddressValidator.getInstance();
        return validator.isValidInet4Address(ip) || ip.equals("localhost");
    }

    private boolean strSinEspacios(String s) {
        return !(s.matches(".*(\\s+).*") || s.matches(""));
    }

    /**
     * Termina la conexión del cliente con el servidor, terminando los threads,
     * cerrando los sockets correspondientes y limpiando el área de chat
     */
    public void terminarConexion() {

        try {

            // Cierro el socket multicast
            // Por alguna razón tira varias excepciones socketCLosed, aunque el
            // listenerThread ya no existe se supone. Así que no entiendo. Igual no genera ningún problema.
            if (multicastThread.isAlive()) {
                multicastThread.terminarConexion();
            }

            // Mato al listener
            try {
                if (multicastThread != null && multicastThread.isAlive()) {
                    multicastThread.join(0, 1); // Hay que matar al proceso así porque está bloqueado recibiendo
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Interfaz.class.getName()).log(Level.SEVERE, null, ex);
            }

            //mato thread que escucha privados y sirvió para el login
            if (listenerPrivados != null && listenerPrivados.isAlive()) {
                listenerPrivados.join(0, 1); // Hay que matar al proceso así porque está bloqueado recibiendo
            }

            //mato al socket de login
            socketUnicastLogin.close();

            //mato al socket de otros mensajes
            socketUnicastOtros.close();

            // Deshabilito
            jButtonDesconectar.setEnabled(false);
            jTextFieldMensaje.setEnabled(false);
            jTextFieldMensaje.setText("Ingrese su mensaje");
            jButtonEnviar.setEnabled(false);
            jButtonListarConectados.setEnabled(false);

            // Habilito
            jButtonConectar.setEnabled(true);
            jTextFieldHostIP.setEditable(true);
            jTextFieldPort.setEditable(true);
            jTextFieldApodo.setEditable(true);

            // Actualizo chat
            updateChat("", false, true);

            // Actualizo otros
            jLabelStatus.setText(strDesconectado);
            conectado = false;
        } catch (InterruptedException ex) {
            Logger.getLogger(Interfaz.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Actualiza el área de chat con mutuaexclusión
     *
     * @param msj Mensaje a mostrar
     * @param enable Indica si se debe deshabilitar el área de chat
     * @param clear Indica si se debe limpiar el área de chat
     */
    public synchronized void updateChat(String msj, boolean enable, boolean clear) {
        if (clear) {
            jTextAreaChat.setText("");
        }
        if (jTextAreaChat.isEnabled() != enable) {
            jTextAreaChat.setEnabled(enable);
        }
        if (jTextAreaChat.isEnabled() && !msj.equals("")) {
            jTextAreaChat.append(msj + "\n");
            jTextAreaChat.updateUI();
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelHostIP = new javax.swing.JLabel();
        jLabelPort = new javax.swing.JLabel();
        jLabelApodo = new javax.swing.JLabel();
        jTextFieldHostIP = new javax.swing.JTextField();
        jTextFieldPort = new javax.swing.JTextField();
        jTextFieldApodo = new javax.swing.JTextField();
        jButtonConectar = new javax.swing.JButton();
        jButtonDesconectar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextAreaChat = new javax.swing.JTextArea();
        jLabelStatus = new javax.swing.JLabel();
        jTextFieldMensaje = new javax.swing.JTextField();
        jButtonEnviar = new javax.swing.JButton();
        jButtonListarConectados = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Cliente de Chat - Redes 2015");
        setMinimumSize(new java.awt.Dimension(500, 380));
        setResizable(false);
        setSize(new java.awt.Dimension(500, 380));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                cerrandoVentanaEvent(evt);
            }
        });
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabelHostIP.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelHostIP.setText("Host IP:");
        getContentPane().add(jLabelHostIP, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 70, -1));

        jLabelPort.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelPort.setText("Port:");
        getContentPane().add(jLabelPort, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, 70, -1));

        jLabelApodo.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelApodo.setText("Apodo:");
        getContentPane().add(jLabelApodo, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, 70, -1));
        getContentPane().add(jTextFieldHostIP, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 30, 120, -1));
        jTextFieldHostIP.setText("127.0.0.1");
        getContentPane().add(jTextFieldPort, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 70, 60, -1));
        jTextFieldPort.setText("54321");
        getContentPane().add(jTextFieldApodo, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 110, 120, -1));
        jTextFieldApodo.setText("anonimo");

        jButtonConectar.setText("Conectar");
        jButtonConectar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonConectarActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonConectar, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 210, -1, -1));

        jButtonDesconectar.setText("Desconectar");
        jButtonDesconectar.setEnabled(false);
        jButtonDesconectar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDesconectarActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonDesconectar, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 210, -1, -1));

        jTextAreaChat.setColumns(16);
        jTextAreaChat.setRows(5);
        jTextAreaChat.setEnabled(false);
        jTextAreaChat.setFocusable(false);
        jScrollPane1.setViewportView(jTextAreaChat);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 30, 190, 240));

        jLabelStatus.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelStatus.setText(strDesconectado);
        getContentPane().add(jLabelStatus, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 250, 100, 20));

        jTextFieldMensaje.setText("Ingrese su mensaje aquí");
        jTextFieldMensaje.setEnabled(false);
        getContentPane().add(jTextFieldMensaje, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 310, 350, -1));

        jButtonEnviar.setText("Enviar");
        jButtonEnviar.setEnabled(false);
        jButtonEnviar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEnviarActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonEnviar, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 310, -1, -1));

        jButtonListarConectados.setText("Listar usuarios conectados");
        jButtonListarConectados.setEnabled(false);
        jButtonListarConectados.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonListarConectadosActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonListarConectados, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 160, 190, -1));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Le comunica a la interfaz que hubo un login correcto
     */
    public void comunicarOK() {
        conectado = true;
        // Deshabilito
        jButtonConectar.setEnabled(false);
        jTextFieldHostIP.setEditable(false);
        jTextFieldPort.setEditable(false);
        jTextFieldApodo.setEditable(false);

        // Habilito
        jButtonDesconectar.setEnabled(true);
        jTextFieldMensaje.setText(null);
        jTextFieldMensaje.setEnabled(true);
        jButtonEnviar.setEnabled(true);
        jButtonListarConectados.setEnabled(true);

        // Actualizo estado
        jLabelStatus.setText(strEnLinea);

        // Habilito chat
        updateChat("Usted está en línea!", true, false);
    }

    /**
     * Devuelve referencia al socketUnicastLogin
     *
     * @return Referencia al socketUnicastLogin
     */
    public DatagramSocket getUnicastSocket() {
        return socketUnicastLogin;
    }

    /**
     * Devuelve referencia al socketUnicastOtros
     *
     * @return Referencia al socketUnicastOtros
     */
    public DatagramSocket getSocketOtros() {
        return socketUnicastOtros;
    }

    private void jButtonConectarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonConectarActionPerformed
        String strHostIP = jTextFieldHostIP.getText();
        String strPort = jTextFieldPort.getText();
        apodo = jTextFieldApodo.getText();
        jTextAreaChat.setText(""); //vacio area de chat antes de arrancar
        // Verificar IP
        serverIP = null;
        try {
            serverIP = InetAddress.getByName(strHostIP);
        } catch (UnknownHostException ex) {
        }
        boolean okIP = okIP(strHostIP);
        // Verificar Puerto
        boolean okPort;
        try {
            serverPort = Integer.parseInt(strPort);
            okPort = serverPort >= 4000;
        } catch (NumberFormatException e) {
            okPort = false;
            System.err.println(e);
        }
        // Verificar Apodo
        boolean okApodo = strSinEspacios(apodo);

        // Mandar datagrama y esperar por conexión exitosa
        if (okIP && okPort && okApodo) {
            try {
                // Corro el listener
                multicastThread = new Multicast(aplicarConfiabilidad);
                multicastThread.start();
                //inicializo sockets
                socketUnicastLogin = new DatagramSocket();
                socketUnicastOtros = new DatagramSocket();
                //Corro el listener unicast
                //El listener va a intentar loguearse primero, y después va a escuchar mensajes privados
                listenerPrivados = new ListenerPrivados(aplicarConfiabilidad, serverIP, serverPort);
                listenerPrivados.start();
                String msj = "LOGIN " + apodo + "\0";
                listenerPrivados.queue.put("login");
                listenerPrivados.queue.put(msj);
            } catch (InterruptedException ex) {
                Logger.getLogger(Interfaz.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SocketException ex) {
                Logger.getLogger(Interfaz.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            String error = "";
            if (!okIP) {
                error = "La IP ingresada no es válida.";
            } else if (!okPort) {
                error = "El número de puerto ingresado no es válido.";
            } else if (!okApodo) {
                error = "El Apodo ingresado no es válido.\nEl mismo no debe contener espacios.";
            }
            JOptionPane.showMessageDialog(this, "Error! " + error, "Cliente", JOptionPane.ERROR_MESSAGE);
        }

    }//GEN-LAST:event_jButtonConectarActionPerformed

    private void jButtonDesconectarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDesconectarActionPerformed
        try {
            String msj = "LOGOUT\0";
            listenerPrivados.queue.put("logout");
            listenerPrivados.queue.put(msj);
        } catch (InterruptedException ex) {
            Logger.getLogger(Interfaz.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButtonDesconectarActionPerformed

    private void jButtonEnviarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEnviarActionPerformed
        // Obtengo el chatMsj a ser enviado
        String msj = jTextFieldMensaje.getText();
        // Me fijo si ingresó texto
        if (!msj.isEmpty()) {
            (new ThreadMensajesListado(aplicarConfiabilidad, msj, serverIP, serverPort)).start();
        }
        // Limpio la línea de chatMsj
        jTextFieldMensaje.setText(null);
    }//GEN-LAST:event_jButtonEnviarActionPerformed

    private void jButtonListarConectadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonListarConectadosActionPerformed
        String msj = "GET_CONNECTED\0";
        (new ThreadMensajesListado(aplicarConfiabilidad, msj, serverIP, serverPort)).start();
    }//GEN-LAST:event_jButtonListarConectadosActionPerformed

    /**
     * Comunica a la interfaz la información de usuarios conectados.
     *
     * @param conectados Información de usuarios conectados
     */
    public void comunicarConectados(String conectados) {
        updateChat(conectados, true, false);
    }

    /**
     * Vacía el área de chat de mensajes anteriores
     */
    public void limpiarAreaChat() {
        updateChat(null, true, true);
    }

    private void cerrandoVentanaEvent(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_cerrandoVentanaEvent
        if (conectado) {
            terminarConexion();
        }
        this.dispose();
    }//GEN-LAST:event_cerrandoVentanaEvent

    /**
     * Devuelve el apodo del cliente, determinado en la interfaz
     *
     * @return Apodo del cliente
     */
    public String getApodo() {
        return apodo;
    }

    /**
     * Tamaño fijo de paquete
     */
    public final static int PACKETSIZE = 65536;
    private int serverPort; // El puerto donde corre el servidor. Se lee desde la interfaz
    private InetAddress serverIP; // La IP donde corre el servidor. Se lee desde la interfaz
    /**
     * Socket utilizado para mensajes que no sean de login (logout, message,
     * private_message)
     */
    public DatagramSocket socketUnicastOtros;
    /**
     * Socket utilizado para login y escuchar mensajes privados
     */
    public DatagramSocket socketUnicastLogin; // El socket para recibir y enviar mansajes unicast.
    private final String strDesconectado = "<html><font color='red'>Desconectado</font></html>";
    private final String strEnLinea = "<html><font color='green'>En línea</font></html>";
    private byte[] data = new byte[PACKETSIZE]; // El mansaje se manda como byte[], esto es lo que incluye en el paquete
    private boolean conectado = false;
    private String apodo;
    private boolean aplicarConfiabilidad = false;
    private Multicast multicastThread;
    private ListenerPrivados listenerPrivados;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonConectar;
    private javax.swing.JButton jButtonDesconectar;
    private javax.swing.JButton jButtonEnviar;
    private javax.swing.JButton jButtonListarConectados;
    private javax.swing.JLabel jLabelApodo;
    private javax.swing.JLabel jLabelHostIP;
    private javax.swing.JLabel jLabelPort;
    private javax.swing.JLabel jLabelStatus;
    private javax.swing.JScrollPane jScrollPane1;
    private static javax.swing.JTextArea jTextAreaChat;
    private javax.swing.JTextField jTextFieldApodo;
    private javax.swing.JTextField jTextFieldHostIP;
    private javax.swing.JTextField jTextFieldMensaje;
    private javax.swing.JTextField jTextFieldPort;
    // End of variables declaration//GEN-END:variables
}
