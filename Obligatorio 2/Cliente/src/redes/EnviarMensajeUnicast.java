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
class EnviarMensajeUnicast extends Thread {

    private BufferMensajesEnviar buffer;
    private String msj;

    public EnviarMensajeUnicast(BufferMensajesEnviar b, String msj) {
        buffer = b;
        this.msj = msj;
    }

    public void run() {
        buffer.put(msj);
    }
}
