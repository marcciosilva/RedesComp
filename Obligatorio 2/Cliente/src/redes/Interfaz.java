package redes;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
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
		buttonGroup1.setSelected(buttonPublico.getModel(), true);
		textFieldDestinatario.setVisible(false);
		labelDestinatario.setVisible(false);
	}

	private boolean okIP(String ip) {
		InetAddressValidator validator = InetAddressValidator.getInstance();
		return validator.isValidInet4Address(ip) || ip.equals("localhost");
	}

	private boolean strSinEspacios(String s) {
		return !(s.matches(".*(\\s+).*") || s.matches(""));
	}

	/**
	 * Termina la conexión del cliente con el servidor, terminando los threads, cerrando los sockets correspondientes y limpiando el área de chat
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
			jTextAreaChat.updateUI();
		}
	}

	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
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
        buttonPrivado = new javax.swing.JRadioButton();
        buttonPublico = new javax.swing.JRadioButton();
        labelTipoMensaje = new javax.swing.JLabel();
        textFieldDestinatario = new javax.swing.JTextField();
        labelDestinatario = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Cliente de Chat - Redes 2015");
        setMinimumSize(new java.awt.Dimension(665, 460));
        setPreferredSize(new java.awt.Dimension(665, 460));
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
        getContentPane().add(jLabelHostIP, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 40, 70, -1));

        jLabelPort.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelPort.setText("Port:");
        getContentPane().add(jLabelPort, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 80, 70, -1));

        jLabelApodo.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelApodo.setText("Apodo:");
        getContentPane().add(jLabelApodo, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 120, 70, -1));
        getContentPane().add(jTextFieldHostIP, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 40, 120, -1));
        jTextFieldHostIP.setText("127.0.0.1");
        getContentPane().add(jTextFieldPort, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 80, 60, -1));
        jTextFieldPort.setText("54321");
        getContentPane().add(jTextFieldApodo, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 120, 120, -1));
        jTextFieldApodo.setText("anonimo");

        jButtonConectar.setText("Conectar");
        jButtonConectar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonConectarActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonConectar, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 220, -1, -1));

        jButtonDesconectar.setText("Desconectar");
        jButtonDesconectar.setEnabled(false);
        jButtonDesconectar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDesconectarActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonDesconectar, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 220, -1, -1));

        jTextAreaChat.setColumns(16);
        jTextAreaChat.setLineWrap(true);
        jTextAreaChat.setRows(5);
        jTextAreaChat.setWrapStyleWord(true);
        jTextAreaChat.setEnabled(false);
        jTextAreaChat.setFocusable(false);
        jScrollPane1.setViewportView(jTextAreaChat);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 30, 310, 260));

        jLabelStatus.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelStatus.setText(strDesconectado);
        getContentPane().add(jLabelStatus, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 260, 100, 20));

        jTextFieldMensaje.setText("Ingrese su mensaje aquí");
        jTextFieldMensaje.setEnabled(false);
        getContentPane().add(jTextFieldMensaje, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 370, 400, 20));

        jButtonEnviar.setText("Enviar");
        jButtonEnviar.setEnabled(false);
        jButtonEnviar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEnviarActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonEnviar, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 400, -1, -1));

        jButtonListarConectados.setText("Listar usuarios conectados");
        jButtonListarConectados.setEnabled(false);
        jButtonListarConectados.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonListarConectadosActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonListarConectados, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 170, 190, -1));

        buttonGroup1.add(buttonPrivado);
        buttonPrivado.setText("Privado");
        buttonPrivado.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                buttonPrivadoStateChanged(evt);
            }
        });
        buttonPrivado.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPrivadoActionPerformed(evt);
            }
        });
        getContentPane().add(buttonPrivado, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 350, -1, -1));

        buttonGroup1.add(buttonPublico);
        buttonPublico.setText("Público");
        buttonPublico.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPublicoActionPerformed(evt);
            }
        });
        getContentPane().add(buttonPublico, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 370, -1, -1));

        labelTipoMensaje.setText("Tipo de mensaje");
        getContentPane().add(labelTipoMensaje, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 330, -1, -1));

        textFieldDestinatario.setText("Ingrese el destinatario aquí");
        textFieldDestinatario.setEnabled(false);
        getContentPane().add(textFieldDestinatario, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 330, 290, -1));

        labelDestinatario.setText("Destinatario:");
        getContentPane().add(labelDestinatario, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 330, -1, -1));

        pack();
    }// </editor-fold>//GEN-END:initComponents

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

			// Habilito chat
			updateChat("Usted está en línea!", true, false);
		} catch (UnknownHostException ex) {
			Logger.getLogger(Interfaz.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(Interfaz.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Devuelve referencia al socketUnicast
	 *
	 * @return Referencia al socketUnicast
	 */
	public DatagramSocket getUnicastSocket() {
		return socketUnicast;
	}

	public MulticastSocket getMulticastSocket() {
		return socketMulticast;
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
				//inicializo socket de login
				socketUnicast = new DatagramSocket();
                //inicializo el listener unicast para ya poder recibir
				//respuesta del LOGIN
				listenerUnicast = new LectorUnicast(aplicarConfiabilidad, serverIP, serverPort);
				listenerUnicast.start();
				String msj = "LOGIN " + apodo + "\0";
				(new EnvioUnicast(false, msj, serverIP, serverPort)).start();
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
		String msj = "LOGOUT\0";
		(new EnvioUnicast(false, msj, serverIP, serverPort)).start();
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
					(new EnvioUnicast(aplicarConfiabilidad, msj, serverIP, serverPort)).start();
				} else {
					JOptionPane.showMessageDialog(this, "Debe ingresar un nombre de destinatario", "Error", JOptionPane.ERROR_MESSAGE);
				}
			} else {
				String msj = "MESSAGE ";
				msj = msj.concat(contenidoMsj);
				msj = msj.concat("\0");
				(new EnvioUnicast(aplicarConfiabilidad, msj, serverIP, serverPort)).start();
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
		(new EnvioUnicast(aplicarConfiabilidad, msj, serverIP, serverPort)).start();
    }//GEN-LAST:event_jButtonListarConectadosActionPerformed

	/**
	 * Comunica a la interfaz la información de usuarios conectados.
	 *
	 * @param conectados Información de usuarios conectados
	 */
	public void comunicarConectados(String conectados) {
		updateChat(conectados, true, false);
	}

    private void cerrandoVentanaEvent(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_cerrandoVentanaEvent
		if (conectado) {
			terminarConexion();
		}
		this.dispose();
    }//GEN-LAST:event_cerrandoVentanaEvent

    private void buttonPrivadoStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_buttonPrivadoStateChanged
    }//GEN-LAST:event_buttonPrivadoStateChanged

    private void buttonPrivadoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPrivadoActionPerformed
		textFieldDestinatario.setVisible(true);
		labelDestinatario.setVisible(false);
    }//GEN-LAST:event_buttonPrivadoActionPerformed

    private void buttonPublicoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPublicoActionPerformed
		textFieldDestinatario.setVisible(false);
		labelDestinatario.setVisible(false);
    }//GEN-LAST:event_buttonPublicoActionPerformed

	/**
	 * Devuelve el apodo del cliente, determinado en la interfaz
	 *
	 * @return Apodo del cliente
	 */
	public String getApodo() {
		return apodo;
	}

	private int multicastPort = 6789; // No cambiar! Debe ser el mismo en el servidor.
	private InetAddress multicastIP;
	private String strMulticastIP = "225.5.4.3";
	public final static int PACKETSIZE = 65536;
	private int serverPort; // El puerto donde corre el servidor. Se lee desde la interfaz
	private InetAddress serverIP; // La IP donde corre el servidor. Se lee desde la interfaz
	public MulticastSocket socketMulticast;
	public DatagramSocket socketUnicast; // El socket para recibir y enviar mansajes unicast.
	private final String strDesconectado = "<html><font color='red'>Desconectado</font></html>";
	private final String strEnLinea = "<html><font color='green'>En línea</font></html>";
	private byte[] data = new byte[PACKETSIZE]; // El mansaje se manda como byte[], esto es lo que incluye en el paquete
	private boolean conectado = false;
	private String apodo;
	private boolean aplicarConfiabilidad = false;
	private LectorMulticast multicastThread;
	private LectorUnicast listenerUnicast;
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
