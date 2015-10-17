/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package redes;

/**
 *
 * @author marccio
 */
public class BufferMensajesEnviar {

	private String msj;
	private boolean available = false;

	public synchronized String get() {
		//se obtiene un mensaje pero no se quita del buffer
		while (available == false) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
		available = false; //habilita a que los productores pongan
		notifyAll(); //los despierta
		System.out.println("Se sacó el mensaje: " + msj + "del buffer");
		return msj;
	}

//    public synchronized void remove() {
//        //se quita el mensaje del buffer
//        available = false; //hasta que un productor no ponga otro mensaje, no se va a poder volver a sacar
//        notifyAll();
//    }
	public synchronized void put(String msj) {
		while (available == true) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
		System.out.println("Se agregó el mensaje: " + msj + "al buffer");
		this.msj = msj;
		available = true;
		notifyAll(); //despierta al consumidor (EnvioUnicast)
	}
}
