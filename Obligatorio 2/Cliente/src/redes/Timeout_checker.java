package redes;

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
				if ((Cliente.estadoSender == Cliente.EstadoSender.ESPERO_ACK_0 || Cliente.estadoSender == Cliente.EstadoSender.ESPERO_ACK_1) && now.getTime() - Cliente.tiempo_enviado.getTime() > 500) {
					Cliente.tiempo_enviado = Date.from(Instant.now());
					EnvioUnicast t1 = new EnvioUnicast(false, Cliente.ultimo_msj, Cliente.serverIP, Cliente.multicastPort);
					t1.start();
					t1.join();
				}
				Thread.sleep(500); // 500ms
			} catch (InterruptedException ex) {
				Logger.getLogger(Timeout_checker.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

}
