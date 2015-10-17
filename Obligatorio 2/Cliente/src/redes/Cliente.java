package redes;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.text.DefaultCaret;
import org.apache.commons.validator.routines.InetAddressValidator;

/**
 * Cliente de chat, que además controla threads y sockets
 *
 * @author marccio (y yogui? y candela? y la moto?)
 */
public class Cliente extends javax.swing.JFrame {

    private static Cliente instance = null;

    /**
     * Devuelve instancia de la interfaz
     *
     * @return Instancia de la interfaz
     */
    public static Cliente getInstance() {
        if (instance == null) {
            instance = new Cliente();
        }
        return instance;
    }

    private Cliente() {
        initComponents();
        // Botón 'Enviar' como predeterminado al apretar 'Enter'
        getRootPane().setDefaultButton(jButtonEnviar);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabelHostIP = new javax.swing.JLabel();
        jTextFieldHostIP = new javax.swing.JTextField();
        jLabelPort = new javax.swing.JLabel();
        jTextFieldPort = new javax.swing.JTextField();
        jLabelApodo = new javax.swing.JLabel();
        jTextFieldApodo = new javax.swing.JTextField();
        jButtonListarConectados = new javax.swing.JButton();
        jButtonConectar = new javax.swing.JButton();
        jButtonDesconectar = new javax.swing.JButton();
        jLabelStatus = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextAreaChat = new javax.swing.JTextArea();
        labelTipoMensaje = new javax.swing.JLabel();
        buttonPrivado = new javax.swing.JRadioButton();
        buttonPublico = new javax.swing.JRadioButton();
        labelDestinatario = new javax.swing.JLabel();
        textFieldDestinatario = new javax.swing.JTextField();
        jTextFieldMensaje = new javax.swing.JTextField();
        jButtonEnviar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Cliente de Chat - Redes 2015");
        setMinimumSize(new java.awt.Dimension(640, 490));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                cerrandoVentanaEvent(evt);
            }
        });
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabelHostIP.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelHostIP.setText("Host IP:");
        getContentPane().add(jLabelHostIP, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 40, 70, -1));

        jTextFieldHostIP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldHostIPActionPerformed(evt);
            }
        });
        getContentPane().add(jTextFieldHostIP, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 40, 120, -1));
        jTextFieldHostIP.setText("127.0.0.1");
        //jTextFieldHostIP.setText("25.0.32.206");

        jLabelPort.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelPort.setText("Port:");
        getContentPane().add(jLabelPort, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 80, 70, -1));
        getContentPane().add(jTextFieldPort, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 80, 60, -1));
        jTextFieldPort.setText("54321");

        jLabelApodo.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelApodo.setText("Apodo:");
        getContentPane().add(jLabelApodo, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 120, 70, -1));
        getContentPane().add(jTextFieldApodo, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 120, 120, -1));
        jTextFieldApodo.setText("anonimo");

        jButtonListarConectados.setText("Listar usuarios conectados");
        jButtonListarConectados.setEnabled(false);
        jButtonListarConectados.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonListarConectadosActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonListarConectados, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 170, 200, -1));

        jButtonConectar.setText("Conectar");
        jButtonConectar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonConectarActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonConectar, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 220, 100, 40));

        jButtonDesconectar.setText("Desconectar");
        jButtonDesconectar.setEnabled(false);
        jButtonDesconectar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDesconectarActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonDesconectar, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 220, 100, 40));

        jLabelStatus.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelStatus.setText(strDesconectado);
        jLabelStatus.setFocusable(false);
        jLabelStatus.setFont(new Font(jLabelStatus.getFont().getFontName(), Font.BOLD, 14));
        jLabelStatus.setForeground(Color.RED);
        getContentPane().add(jLabelStatus, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 280, 200, 40));

        jTextAreaChat.setColumns(16);
        jTextAreaChat.setLineWrap(true);
        jTextAreaChat.setRows(5);
        jTextAreaChat.setWrapStyleWord(true);
        jTextAreaChat.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        jTextAreaChat.setEnabled(false);
        jTextAreaChat.setFocusable(false);
        DefaultCaret caret = (DefaultCaret) jTextAreaChat.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        jScrollPane1.setViewportView(jTextAreaChat);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 30, 310, 290));

        labelTipoMensaje.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelTipoMensaje.setText("Tipo de mensaje");
        labelTipoMensaje.setFocusable(false);
        getContentPane().add(labelTipoMensaje, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 340, 100, -1));

        buttonGroup1.add(buttonPrivado);
        buttonPrivado.setText("Privado");
        buttonPrivado.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPrivadoActionPerformed(evt);
            }
        });
        getContentPane().add(buttonPrivado, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 340, -1, -1));

        buttonGroup1.add(buttonPublico);
        buttonPublico.setSelected(true);
        buttonPublico.setText("Público");
        buttonPublico.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPublicoActionPerformed(evt);
            }
        });
        getContentPane().add(buttonPublico, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 340, -1, -1));

        labelDestinatario.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelDestinatario.setText("Destinatario:");
        labelDestinatario.setVisible(false);
        labelDestinatario.setFocusable(false);
        getContentPane().add(labelDestinatario, new org.netbeans.lib.awtextra.AbsoluteConstraints(33, 380, 100, -1));

        textFieldDestinatario.setText("Ingrese el destinatario aquí");
        textFieldDestinatario.setEnabled(false);
        textFieldDestinatario.setVisible(false);
        getContentPane().add(textFieldDestinatario, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 380, 290, -1));

        jTextFieldMensaje.setText("Ingrese su mensaje aquí");
        jTextFieldMensaje.setEnabled(false);
        getContentPane().add(jTextFieldMensaje, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 420, 400, 20));

        jButtonEnviar.setText("Enviar");
        jButtonEnviar.setEnabled(false);
        jButtonEnviar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEnviarActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonEnviar, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 390, 100, 40));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonConectarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonConectarActionPerformed
        String strHostIP = jTextFieldHostIP.getText();
        String strPort = jTextFieldPort.getText();
        apodo = jTextFieldApodo.getText();

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
                //inicializo socket de login
                socketUnicast = new DatagramSocket();
                //inicializo el listener unicast para ya poder recibir
                //respuesta del LOGIN
                listenerUnicast = new LectorUnicast(aplicarConfiabilidad, serverIP, serverPort);
                listenerUnicast.start();
                String msj = "LOGIN " + apodo + "\0";
                enviarMensaje(msj);
            } catch (SocketException ex) {
                Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
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
        String msj = "LOGOUT\0";
        enviarMensaje(msj);
    }//GEN-LAST:event_jButtonDesconectarActionPerformed

    private void jButtonEnviarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEnviarActionPerformed
        // Obtengo el chatMsj a ser enviado
        String contenidoMsj = jTextFieldMensaje.getText();
        // Me fijo si ingresó texto
        if (!contenidoMsj.isEmpty()) {
            if (buttonPrivado.isSelected()) {
                String destinatario = textFieldDestinatario.getText();
                if (!destinatario.equals("")) {
                    String msj = "PRIVATE_MESSAGE ";
                    msj = msj.concat(destinatario + " ");
                    msj = msj.concat(contenidoMsj);
                    msj = msj.concat("\0");
                    //muestro mi propio mensaje en el chat antes de enviarlo
                    //para poder seguir el hilo de la conversación
                    updateChat(apodo + " > " + destinatario + ": " + contenidoMsj, true, false);
                    enviarMensaje(msj);
                } else {
                    JOptionPane.showMessageDialog(this, "Debe ingresar un nombre de destinatario", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                String msj = "MESSAGE ";
                msj = msj.concat(contenidoMsj);
                msj = msj.concat("\0");
                enviarMensaje(msj);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Debe escribir un mensaje antes de tocar Enviar", "Error", JOptionPane.ERROR_MESSAGE);
        }
        // Limpio la línea de chatMsj pero no la de destinatario, por si se
        //quiere seguir la comunicación privada
        jTextFieldMensaje.setText(null);
    }//GEN-LAST:event_jButtonEnviarActionPerformed

    private void jButtonListarConectadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonListarConectadosActionPerformed
        String msj = "GET_CONNECTED\0";
        enviarMensaje(msj);
    }//GEN-LAST:event_jButtonListarConectadosActionPerformed

    private void cerrandoVentanaEvent(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_cerrandoVentanaEvent
        if (conectado) {
            terminarConexion();
        }
        this.dispose();
    }//GEN-LAST:event_cerrandoVentanaEvent

    private void buttonPrivadoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPrivadoActionPerformed
        textFieldDestinatario.setVisible(true);
        labelDestinatario.setVisible(true);
    }//GEN-LAST:event_buttonPrivadoActionPerformed

    private void buttonPublicoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPublicoActionPerformed
        textFieldDestinatario.setVisible(false);
        labelDestinatario.setVisible(false);
    }//GEN-LAST:event_buttonPublicoActionPerformed

    private void jTextFieldHostIPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldHostIPActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldHostIPActionPerformed

    private void enviarMensaje(String msj) {
        deshabilitarEnvios();
        threadEnvioUnicastActual = new EnvioUnicast(aplicarConfiabilidad, msj, serverIP, serverPort);
        threadEnvioUnicastActual.start();
    }

    private boolean okIP(String ip) {
        InetAddressValidator validator = InetAddressValidator.getInstance();
        return validator.isValidInet4Address(ip) || ip.equals("localhost");
    }

    private boolean strSinEspacios(String s) {
        return !(s.matches(".*(\\s+).*") || s.matches(""));
    }

    /**
     * Devuelve el apodo del cliente, determinado en la interfaz
     *
     * @return Apodo del cliente
     */
    public String getApodo() {
        return apodo;
    }

    /**
     * Devuelve referencia al socketUnicast
     *
     * @return Referencia al socketUnicast
     */
    public DatagramSocket getUnicastSocket() {
        //es thread-safe, no hay que sincronizar su uso
        return socketUnicast;
    }

    public MulticastSocket getMulticastSocket() {
        //es thread-safe, no hay que sincronizar su uso
        return socketMulticast;
    }

    /**
     * Comunica a la interfaz la información de usuarios conectados.
     *
     * @param conectados Información de usuarios conectados
     */
    public void comunicarConectados(String conectados) {
        String usuarios[] = conectados.split(" ", 2);
        String aviso = "Usuarios conectados: " + usuarios[1];
        updateChat(aviso, true, false);
    }

    public void comunicarMensaje(String msj) {
        String remitente_y_mensaje[] = msj.split(" ", 2)[1].split(" ", 2);
        String aviso = remitente_y_mensaje[0] + ": " + remitente_y_mensaje[1];
        updateChat(aviso, true, false);
    }

    public void comunicarAlive() {
        //hay que enviar un IS_ALIVE al server por unicast
        String msj = "IS_ALIVE\0";
        enviarMensaje(msj);
    }

    public void comunicarMensajePrivado(String msj) {
        String remitente_y_mensaje[] = msj.split(" ", 2)[1].split(" ", 2);
        String aviso = remitente_y_mensaje[0] + " > " + apodo + ": " + remitente_y_mensaje[1];
        updateChat(aviso, true, false);
    }

    void comunicarMensajePrivadoFailed(String msj) {
        String mensaje[] = msj.split(" ", 2);
        updateChat(mensaje[1], true, false);
    }

    /**
     * Termina la conexión del cliente con el servidor, terminando los threads,
     * cerrando los sockets correspondientes y limpiando el área de chat
     */
    public void terminarConexion() {
        // cierro el socket unicast
        socketUnicast.close();

        // cierro el socket multicast
        socketMulticast.close();

        // Deshabilito
        textFieldDestinatario.setText("Ingrese el destinatario aquí");
        textFieldDestinatario.setEnabled(false);
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
        jLabelStatus.setForeground(Color.RED);
        conectado = false;
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
        }
    }

    public void comunicarNoOk() {
        socketUnicast.close();
    }

    /**
     * Le comunica a la interfaz que hubo un login correcto
     */
    public void comunicarOK() {
        try {
            // Corro el listener
            //inicializo socket multicast
            // Fijo la dirección ip y el puerto de donde voy a escuchar los mensajes. IP 225.5.4.<nro_grupo> puerto 6789
            multicastIP = InetAddress.getByName(strMulticastIP);
            socketMulticast = new MulticastSocket(multicastPort);
            socketMulticast.joinGroup(multicastIP);
            multicastThread = new LectorMulticast(aplicarConfiabilidad);
            multicastThread.start();

            conectado = true;
            // Deshabilito
            jButtonConectar.setEnabled(false);
            jTextFieldHostIP.setEditable(false);
            jTextFieldPort.setEditable(false);
            jTextFieldApodo.setEditable(false);

            // Habilito
            jButtonDesconectar.setEnabled(true);
            textFieldDestinatario.setText(null);
            textFieldDestinatario.setEnabled(true);
            jTextFieldMensaje.setText(null);
            jTextFieldMensaje.setEnabled(true);
            jButtonEnviar.setEnabled(true);
            jButtonListarConectados.setEnabled(true);

            // Actualizo estado
            jLabelStatus.setText(strEnLinea);
            jLabelStatus.setForeground(Color.getHSBColor(0.36f, 0.75f, 0.7f));

            // Habilito chat
            updateChat("Usted está en línea!", true, false);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void envioFinalizado() {
        System.out.println("Envío finalizado correctamente");
        //cambio estado de la máquina del sender
        if (estadoSender == EstadoSender.ESPERO_ACK_0) {
            estadoSender = EstadoSender.ESPERO_DATA_1;
        } else if (estadoSender == EstadoSender.ESPERO_ACK_1) {
            estadoSender = EstadoSender.ESPERO_DATA_0;
        }
        habilitarEnvios();
    }

    private void deshabilitarEnvios() {
        jButtonDesconectar.setEnabled(false);
        jButtonListarConectados.setEnabled(false);
        jButtonEnviar.setEnabled(false); //deshabilito enviar
    }

    private void habilitarEnvios() {
        jButtonDesconectar.setEnabled(true);
        jButtonListarConectados.setEnabled(true);
        jButtonEnviar.setEnabled(true); //deshabilito enviar
    }

    public static void main(String args[]) {
        // Set look and Feel
        try {
            javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
        }

        // Creo jFrame
        Cliente v = Cliente.getInstance();
        v.setLocationRelativeTo(null);
        v.setVisible(true);
		
		timeoutChecker = new Timeout_checker();
        timeoutChecker.start();
    }

    EnvioUnicast threadEnvioUnicastActual;

    //Máquina de estados del emisor
    public static enum EstadoSender {

        ESPERO_DATA_0, ESPERO_ACK_0, ESPERO_DATA_1, ESPERO_ACK_1
    }
    public static EstadoSender estadoSender = EstadoSender.ESPERO_DATA_0;

    public static int multicastPort = 6789;
    private InetAddress multicastIP;
    private String strMulticastIP = "225.5.4.3";
    public final static int PACKETSIZE = 65507;
	public static Date tiempo_enviado = null;
	public static String ultimo_msj;
	public static boolean espero_ack = false;
    // El puerto donde corre el servidor. Se lee desde la interfaz
    private int serverPort;
    // La IP donde corre el servidor. Se lee desde la interfaz
    public static InetAddress serverIP;
    public MulticastSocket socketMulticast;
    // El socket para recibir y enviar mansajes unicast.
    public DatagramSocket socketUnicast;
    private final String strDesconectado = "Desconectado";
    private final String strEnLinea = "En línea";
    private boolean conectado = false;
    private String apodo;
    private boolean aplicarConfiabilidad = true;
    private LectorMulticast multicastThread;
    private LectorUnicast listenerUnicast;
    private static Timeout_checker timeoutChecker;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JRadioButton buttonPrivado;
    private javax.swing.JRadioButton buttonPublico;
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
    private javax.swing.JLabel labelDestinatario;
    private javax.swing.JLabel labelTipoMensaje;
    private javax.swing.JTextField textFieldDestinatario;
    // End of variables declaration//GEN-END:variables
}
