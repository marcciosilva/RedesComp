package redes;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.apache.commons.validator.routines.InetAddressValidator;

public class Cliente extends javax.swing.JFrame {

	public Cliente() {
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

	private void terminarConexion() {
		// Cierro el socket
		if (socketCliente != null && !socketCliente.isClosed()) {
			socketCliente.close();
		}

		// Mato al listener
		try {
			if (listener != null && listener.isAlive()) {
				listener.join();
			}
		} catch (InterruptedException ex) {
			Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
		}

		// Deshabilito
		jButtonDesconectar.setEnabled(false);
		jTextAreaChat.setEnabled(false);
		jTextFieldMensaje.setEnabled(false);
		jTextFieldMensaje.setText("Ingrese su mensaje");
		jButtonEnviar.setEnabled(false);
		jButtonListarConectados.setEnabled(false);

		// Habilito
		jButtonConectar.setEnabled(true);
		jTextFieldHostIP.setEditable(true);
		jTextFieldPort.setEditable(true);
		jTextFieldApodo.setEditable(true);

		// Actualizo estado	en UI
		jLabelStatus.setText(strDesconectado);
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

    private void jButtonConectarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonConectarActionPerformed
		String strHostIP = jTextFieldHostIP.getText();
		String strPort = jTextFieldPort.getText();
		apodo = jTextFieldApodo.getText();

		// Verificar IP
		serverIP = null;
		try {
			serverIP = InetAddress.getByName(strHostIP);
		} catch (UnknownHostException ex) {
			Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
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

			// Intento abrir un DatagramSocket
			try {
				socketCliente = new DatagramSocket();
			} catch (SocketException ex) {
				Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
				JOptionPane.showMessageDialog(this, "Error! No se pudo abrir un puerto para la conección", "Cliente", JOptionPane.INFORMATION_MESSAGE);
			}

			// Construyo el paquete y lo envío
			// El número de puerto del cliente y la IP van incluídos en el datagrama por defecto.
			dataOut = ("LOGIN " + apodo + "\n").getBytes();
			DatagramPacket paquete = new DatagramPacket(dataOut, dataOut.length, serverIP, serverPort);
			try {
				socketCliente.send(paquete);
			} catch (IOException ex) {
				Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
				System.err.println(ex.toString());
				return;
			}

			// Espero por una respuesta con timeout de 2 segundos
			try {
				paquete.setData(new byte[PACKETSIZE]);
				socketCliente.setSoTimeout(2000);
				socketCliente.receive(paquete);
			} catch (IOException e) {
				System.err.println(e.toString());
				JOptionPane.showMessageDialog(this, "No se ha recibido una respuesta del servidor", "Cliente", JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			// Convierto el byte [] de la respuesta en un String
			String respuesta = new String(paquete.getData()).split("\0")[0];

			// Proceso la respuesta "OK"/"NOK"
			if (respuesta.equals("OK")) {
				// Deshabilito
				jButtonConectar.setEnabled(false);
				jTextFieldHostIP.setEditable(false);
				jTextFieldPort.setEditable(false);
				jTextFieldApodo.setEditable(false);

				// Habilito
				jButtonDesconectar.setEnabled(true);
				jTextAreaChat.setEnabled(true);
				jTextFieldMensaje.setText(null);
				jTextFieldMensaje.setEnabled(true);
				jButtonEnviar.setEnabled(true);
				jButtonListarConectados.setEnabled(true);

				// Actualizo estado
				jLabelStatus.setText(strEnLinea);

				// Corro el listener
				Listener listenerObj = new Listener(this.jTextAreaChat);
				Thread listenerThread = new Thread(listenerObj);
				listenerThread.start();
				jTextAreaChat.append("Usted está en linea!");
			} else if (respuesta.equals("NOK")) {
				JOptionPane.showMessageDialog(this, "Ya existe un usuario con el apodo " + apodo + "\nPor favor seleccione otro apodo.", "Cliente", JOptionPane.INFORMATION_MESSAGE);
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
		// Envio datagrama al servidor para comunicar la desconexión
		dataOut = "LOGOUT\n".getBytes();
		DatagramPacket paquete = new DatagramPacket(dataOut, dataOut.length, serverIP, serverPort);
		paquete.setData(dataOut);
		try {
			socketCliente.send(paquete);
		} catch (IOException ex) {
			System.err.println(ex.toString());
			return;
		}

		// Espero por una respuesta con timeout de 2 segundos
		try {
			paquete.setData(new byte[PACKETSIZE]);
			socketCliente.setSoTimeout(2000);
			socketCliente.receive(paquete);
		} catch (IOException e) {
			System.err.println(e.toString());
			JOptionPane.showMessageDialog(this, "No se ha recibido una respuesta del servidor", "Cliente", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		// Convierto el byte [] de la respuesta en un String
		String respuesta = new String(paquete.getData()).split("\0")[0];

		if (respuesta.equals("GOODBYE\n")) {
			terminarConexion();
		} else {
			System.err.println("Respuesta del servidor: " + respuesta);
		}

    }//GEN-LAST:event_jButtonDesconectarActionPerformed

    private void jButtonEnviarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEnviarActionPerformed
		// Obtengo el chatMsj a ser enviado
		String chatMsj = jTextFieldMensaje.getText();

		// Me fijo si ingresó texto
		if (!chatMsj.isEmpty()) {
			// Creo y envío el datagrama
			dataOut = ("MESSAGE" + chatMsj + "\n").getBytes();
			DatagramPacket paquete = new DatagramPacket(dataOut, dataOut.length, serverIP, serverPort);
			paquete.setData(dataOut);
			try {
				socketCliente.send(paquete);
			} catch (IOException ex) {
				Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		// Limpio la línea de chatMsj
		jTextFieldMensaje.setText(null);
    }//GEN-LAST:event_jButtonEnviarActionPerformed

    private void jButtonListarConectadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonListarConectadosActionPerformed
		if (!listadoUsuarios.isAlive()) {
			listadoUsuarios = new ListadoUsuarios(jTextAreaChat, serverIP, serverPort);
			listadoUsuarios.start();
		}
    }//GEN-LAST:event_jButtonListarConectadosActionPerformed

    private void cerrandoVentanaEvent(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_cerrandoVentanaEvent
		terminarConexion();
		this.dispose();
    }//GEN-LAST:event_cerrandoVentanaEvent

	public static void main(String args[]) {
		// Set look and Feel
		try {
			javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(Cliente.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}

		// Creo jFrame
		Cliente v = new Cliente();
		v.setLocationRelativeTo(null);
		v.setVisible(true);
	}

	private DatagramSocket socketCliente;
	public final static int PACKETSIZE = 1024;
	private byte[] dataOut = new byte[PACKETSIZE];
	private InetAddress serverIP;
	private int serverPort;
	private String apodo;
	private Thread listener;
	private ListadoUsuarios listadoUsuarios;
	private final String strDesconectado = "<html><font color='red'>Desconectado</font></html>";
	private final String strEnLinea = "<html><font color='green'>En línea</font></html>";
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
    private javax.swing.JTextArea jTextAreaChat;
    private javax.swing.JTextField jTextFieldApodo;
    private javax.swing.JTextField jTextFieldHostIP;
    private javax.swing.JTextField jTextFieldMensaje;
    private javax.swing.JTextField jTextFieldPort;
    // End of variables declaration//GEN-END:variables
}
