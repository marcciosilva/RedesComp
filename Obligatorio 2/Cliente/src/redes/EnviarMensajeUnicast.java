/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package redes;

import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author marccio
 */
class EnviarMensajeUnicast extends Thread {

	private BlockingQueue buffer;
	private String msj;

	public EnviarMensajeUnicast(BlockingQueue b, String msj) {
		buffer = b;
		this.msj = msj;
	}

	public void run() {
		try {
			buffer.put(msj);
			System.out.println("Put -> Items en cola: " + String.valueOf(buffer.size()));
		} catch (InterruptedException ex) {
			Logger.getLogger(EnviarMensajeUnicast.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
