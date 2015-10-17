package redes;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Timeout_checker extends Thread {

	@Override
	public void run() {
		while (true) {
			Date now = Date.from(Instant.now());
			try {
				if ((Cliente.estadoSender == Cliente.EstadoSender.ESPERO_ACK_0 || Cliente.estadoSender == Cliente.EstadoSender.ESPERO_ACK_1) && ((now.getTime() - Cliente.tiempo_enviado.getTime()) > 500)) {
					Cliente.tiempo_enviado = Date.from(Instant.now());
					System.out.println("### reenvío ###");
					Cliente.socketUnicast.send(Cliente.ultimo_pkt);
				}
				Thread.sleep(200); // 500ms
			} catch (InterruptedException | IOException ex) {
				Logger.getLogger(Timeout_checker.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
}
