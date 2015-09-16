
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.apache.commons.validator.routines.InetAddressValidator;

public class Cliente extends javax.swing.JFrame implements Runnable {

	public Cliente() {
		initComponents();
	}

	@Override
	public void run() {
		// Este es el thread que va a escuchar por nuevos mensajes y mostrarlos en el area del chat.

		// Fijo la dirección ip de donde voy a escuchar los mensajes 225.5.4.<nro_grupo>
		try {
			multicastIP = InetAddress.getByName("225.5.4.3");
			multicastSocket = new MulticastSocket(6789);
			multicastSocket.joinGroup(multicastIP);
		} catch (UnknownHostException ex) {
			Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
		}

		while (true) {
			try {
				byte[] mensajes = new byte[PACKETSIZE];
				DatagramPacket paquete = new DatagramPacket(mensajes, mensajes.length, multicastIP, multicastPort);
				multicastSocket.receive(paquete);
				String strMensaje = new String(paquete.getData(), 0, paquete.getLength());

				// Se debe parsear el datagrama para obtener el apodo y el mensaje por separado
				// para mostrarlos en el area de chat.
				jTextAreaChat.append(strMensaje);
			} catch (IOException ex) {
				Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	private boolean okIP(String ip) {
		InetAddressValidator validator = InetAddressValidator.getInstance();
		return validator.isValidInet4Address(ip);
	}

	private boolean strSinEspacios(String s) {
		return !(s.matches(".*(\\s+).*") || s.matches(""));
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

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Cliente de Chat");
        setMinimumSize(new java.awt.Dimension(500, 380));
        setResizable(false);
        setSize(new java.awt.Dimension(500, 380));
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
        getContentPane().add(jTextFieldPort, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 70, 60, -1));
        getContentPane().add(jTextFieldApodo, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 110, 120, -1));

        jButtonConectar.setText("Conectar");
        jButtonConectar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonConectarActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonConectar, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 180, -1, -1));

        jButtonDesconectar.setText("Desconectar");
        jButtonDesconectar.setEnabled(false);
        jButtonDesconectar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDesconectarActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonDesconectar, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 180, -1, -1));

        jTextAreaChat.setColumns(16);
        jTextAreaChat.setRows(5);
        jTextAreaChat.setEnabled(false);
        jTextAreaChat.setFocusable(false);
        jScrollPane1.setViewportView(jTextAreaChat);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 30, 190, 240));

        jLabelStatus.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelStatus.setText(strDesconectado);
        getContentPane().add(jLabelStatus, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 250, 100, 20));

        jTextFieldMensaje.setText("Ingrese su mensaje");
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

		// Mandar datagrama y esperar por conección exitosa
		if (okIP && okPort && okApodo) {

			// Intento abrir un DatagramSocket
			try {
				socketCliente = new DatagramSocket();
			} catch (SocketException ex) {
				Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
				JOptionPane.showMessageDialog(this, "Error! No se pudo abrir un puerto para la conección", "Cliente", JOptionPane.INFORMATION_MESSAGE);
			}

			// Construyo el paquete y lo envío "vacío" porque es solo para poder establecer la conección. El número de puerto 
			// del cliente y la IP van incluídos en el datagrama por defecto.
			DatagramPacket paquete = new DatagramPacket(arrayDataOut, arrayDataOut.length, serverIP, serverPort);
			try {
				socketCliente.send(paquete);
			} catch (IOException ex) {
				Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
			}

			// Espero por una respuesta con timeout de 2 segundos
			try {
				paquete.setData(new byte[PACKETSIZE]);
				socketCliente.setSoTimeout(2000);
				socketCliente.receive(paquete);
			} catch (IOException ex) {
				Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
				JOptionPane.showMessageDialog(this, "Error! No se ha recibido una respuesta del servidor", "Cliente", JOptionPane.INFORMATION_MESSAGE);
			}

			String mensajeRecibido = new String(paquete.getData(), 0, paquete.getLength());

			// Proceso la respuesta "OK"/"Apodo en uso"
			if (mensajeRecibido.equals("OK")) {
				// Deshabilito
				jButtonConectar.setEnabled(false);
				jTextFieldHostIP.setEditable(false);
				jTextFieldPort.setEditable(false);
				jTextFieldApodo.setEditable(false);

				// Habilito
				jButtonDesconectar.setEnabled(true);
				jTextAreaChat.setEnabled(true);
				jTextFieldMensaje.setText("");
				jTextFieldMensaje.setEnabled(true);
				jButtonEnviar.setEnabled(true);

				// Actualizo estado				
				jLabelStatus.setText(strEnLinea);

				// Corro el listener
				listener = new Thread(this, "Escucha mensajes");
				listener.start();

				JOptionPane.showMessageDialog(this, "Se ha conectado!", "Cliente", JOptionPane.INFORMATION_MESSAGE);
			} else if (mensajeRecibido.equals("Apodo en uso")) {
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
			JOptionPane.showMessageDialog(this, "Error! " + error, "Cliente", JOptionPane.INFORMATION_MESSAGE);
		}

    }//GEN-LAST:event_jButtonConectarActionPerformed

    private void jButtonDesconectarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDesconectarActionPerformed
		// Envio datagrama al servidor para comunicar la desconección
		arrayDataOut = "Bye".getBytes();
		DatagramPacket paquete = new DatagramPacket(arrayDataOut, arrayDataOut.length, serverIP, serverPort);
		paquete.setData(arrayDataOut);
		try {
			socketCliente.send(paquete);
		} catch (IOException ex) {
			Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
		}

		// Cierro el socket
		socketCliente.close();

		// Mato al listener
		try {
			listener.join();
		} catch (InterruptedException ex) {
			Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
		}

		// Deshabilito
		jButtonDesconectar.setEnabled(false);
		jTextAreaChat.setEnabled(false);
		jTextFieldMensaje.setEnabled(false);
		jTextFieldMensaje.setText("Ingrese su mensaje");
		jButtonEnviar.setEnabled(false);

		// Habilito
		jButtonConectar.setEnabled(true);
		jTextFieldHostIP.setEditable(true);
		jTextFieldPort.setEditable(true);
		jTextFieldApodo.setEditable(true);

		// Actualizo estado				
		jLabelStatus.setText(strDesconectado);
    }//GEN-LAST:event_jButtonDesconectarActionPerformed

    private void jButtonEnviarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEnviarActionPerformed
		// Obtengo el mensaje a ser enviado
		String mensaje = jTextFieldMensaje.getText();
		
		// Creo y envío el datagrama
		arrayDataOut = mensaje.getBytes();
		DatagramPacket paquete = new DatagramPacket(arrayDataOut, arrayDataOut.length, serverIP, serverPort);
		paquete.setData(arrayDataOut);
		try {
			socketCliente.send(paquete);
		} catch (IOException ex) {
			Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
		}		
    }//GEN-LAST:event_jButtonEnviarActionPerformed

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
	private MulticastSocket multicastSocket;
	private final static int PACKETSIZE = 1024;
	private byte[] arrayDataOut = new byte[PACKETSIZE];
	private InetAddress serverIP;
	private InetAddress multicastIP;
	private int serverPort;
	private final int multicastPort = 6789;
	private String apodo;
	private Thread listener;
	private static String strDesconectado = "<html><font color='red'>Desconectado</font></html>";
	private static String strEnLinea = "<html><font color='green'>En línea</font></html>";
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonConectar;
    private javax.swing.JButton jButtonDesconectar;
    private javax.swing.JButton jButtonEnviar;
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
