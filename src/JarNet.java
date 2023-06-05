
/**
 * JarNet - Este programa permite la transferencia de archivos JAR a un dispositivo remoto, así como su ejecución desde el dispositivo local. 
 * Copyright (C) 2023 Ignacio Inzerilli
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * For further information on how to apply and follow the GNU GPL, please
 * visit <https://www.gnu.org/licenses/>.
 * 
 * Contact information: soporteIgnacio@hotmail.com
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Este proyecto Java se basa en una conexión entre cliente y servidor, los
 * cuales se comunican entre sí mediante el uso de sockets que utilizan puertos
 * de tu red LAN para establecer un flujo de datos.
 * 
 * El cliente puede enviar archivos .jar al servidor y también puede solicitar
 * al servidor la ejecución de algún archivo .jar almacenado previamente en el
 * servidor.
 * 
 * Autor: Ignacio Inzerilli
 * Fecha de creación: 12/04/2023
 * 
 */
public class JarNet {
    /**
     * Crea e inicializa la UI del programa y una entrada para recibir información
     * del servidor, para luego mostrarla en la UI.
     * 
     * @param args
     */
    public static void main(String[] args) {
        try {
            DatagramSocket entradaMensajesServidor = new DatagramSocket(108);
            DatagramPacket mensaje = new DatagramPacket(new byte[1204], 0, 1024);

            JarNetUI visualizacionPrograma = new JarNetUI();
            try {
                while (true) {
                    entradaMensajesServidor.receive(mensaje);
                    visualizacionPrograma.ponerMensajeEstado(new String(mensaje.getData(), 0, mensaje.getLength()));
                }
            } catch (IOException e) {
                visualizacionPrograma.ponerMensajeEstado("Error: " + e.toString());
                entradaMensajesServidor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}