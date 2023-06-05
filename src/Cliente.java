
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import javax.swing.Timer;

/**
 * Se encarga de la lógica del programa para recibir y enviar datos al servidor.
 * Se comunica con el servidor mediante el uso de sockets con puertos y el envío
 * de paquetes, más específicamente utilizando los objetos "DatagramSocket" y
 * "DatagramPacket".
 */
public class Cliente extends Thread {

    private DatagramSocket entradaParaEnviarDatos;
    private DatagramSocket entradaParaRecibirDatos;
    private int puertoServidor = 4213;
    private int puerto = 3198;
    private int puertoEntradaAuxiliarServidor = 5431;
    private LinkedList<String> nombresJarsEnCarpetaOculta;
    private EscucharGuardadoJar guardadoEsc;
    private EscucharChequeo chequeoEsc;
    private EstadoConexion ultimaActlzEstado = EstadoConexion.SERVER_ERROR;
    private Timer tareaDeConexion;
    private DatagramPacket envioDatosServidor;
    private DatagramPacket recepcionDatosServidor;

    /**
     * Inicia un nuevo hilo, inicializa los sockets y paquetes que va a usar el
     * programa, y un objeto de tipo "Timer" para verificar la conexion en un
     * periodo.a
     */
    @Override
    public void run() {
        super.run();
        try {
            envioDatosServidor = new DatagramPacket(new byte[1], 1, InetAddress.getLocalHost(), puertoServidor);
            entradaParaEnviarDatos = new DatagramSocket();
            entradaParaEnviarDatos.setReuseAddress(true);

            recepcionDatosServidor = new DatagramPacket(new byte[1], 1, InetAddress.getLocalHost(), puerto);

            entradaParaRecibirDatos = new DatagramSocket(puerto);
            entradaParaRecibirDatos.setReuseAddress(true);

            iniciarConexion();

            tareaDeConexion = new Timer(5000, new ActionListener() {
                private boolean errorAvisado = false;

                @Override
                public void actionPerformed(ActionEvent e) {
                    ultimaActlzEstado = comprobacionDeFuncionamientoPeriodica();
                    if (ultimaActlzEstado != EstadoConexion.CONECTADO && !errorAvisado) {
                        if (ultimaActlzEstado == EstadoConexion.CLIENTE_ERROR) {
                            DialogoInformativo.mostar(
                                    "Se ha producido un error en el cliente. Este programa se comunica con puertos de la Red LAN. Es posible que el puerto utilizado por el cliente ya esté en uso o que los puertos de la Red LAN estén cerrados. Por favor, revise su configuración de red e intente nuevamente. Si el problema continua, no dudes en contactarme.",
                                    "Error de conexion");
                        } else {
                            DialogoInformativo.mostar(
                                    "Se ha detectado un error en el servidor. Este programa se comunica a través de los puertos de tu Red LAN. Es posible que el servidor no esté en ejecución, o que el puerto que utiliza el servidor esté siendo utilizado por otra aplicación o servicio. También puede ser que los puertos de tu Red LAN estén cerrados, impidiendo que el programa se conecte correctamente. Por favor, revise la configuración del servidor y de la Red LAN para solucionar este problema.",
                                    "Error de conexion");
                        }
                        errorAvisado = true;
                    } else if (ultimaActlzEstado == EstadoConexion.CONECTADO) {
                        errorAvisado = false;
                    }
                    if (chequeoEsc != null) {
                        chequeoEsc.performanceDeChequeo(ultimaActlzEstado);
                    }
                }
            });
            tareaDeConexion.setInitialDelay(0);
            tareaDeConexion.setRepeats(true);
            tareaDeConexion.start();
        } catch (IOException o) {
            o.printStackTrace();
            tareaDeConexion.stop();
            entradaParaRecibirDatos.close();
        }
    }

    public Cliente() {
    }

    /**
     * Envia un mensaje al servidor para asi obtener una lista de los jars guardados
     * en el mismo, y asi iniciar el enlace de el cliente y el servidor.
     */
    public void iniciarConexion() {
        try {
            enviarMensajeAServidor("Establecer conexion");
        } catch (IOException e) {
            DialogoInformativo.mostar(
                    "Ha ocurrido un error al intentar establecer la conexion.\nEl codigo de error es: "
                            + e.toString(),
                    "Error de conexion");
        }
        obtenerJarsGuardadosEnCarpetaOculta();
    }

    /**
     * Obtiene del servidor un objeto de tipo "LinkedList" con los nombres de todos
     * los jars guardados en el servidor.
     */
    private void obtenerJarsGuardadosEnCarpetaOculta() {
        recepcionDatosServidor.setData(new byte[5000]);
        try {
            entradaParaRecibirDatos.setSoTimeout(7000);
            entradaParaRecibirDatos.receive(recepcionDatosServidor);

            ByteArrayInputStream bais = new ByteArrayInputStream(recepcionDatosServidor.getData());
            ObjectInputStream ois = new ObjectInputStream(bais);
            nombresJarsEnCarpetaOculta = (LinkedList<String>) ois.readObject();
            if (!nombresJarsEnCarpetaOculta.isEmpty()) {
                guardadoEsc.performanceDeGuardado(nombresJarsEnCarpetaOculta);
            }
        } catch (IOException | ClassNotFoundException e) {
            DialogoInformativo.mostar(
                    "Ha ocurrido un error al intentar obtener informacion del servidor.\nLa causa del error es: "
                            + e.toString(),
                    "Error de recepcion");
        }
    }

    /**
     * Envía objetos de tipo "String" al servidor, como indicación de cómo debe
     * proceder.
     * 
     * @param mensaje orden a enviar al servidor
     * 
     * @throws IOException si no se envia el mensaje
     */
    private void enviarMensajeAServidor(String mensaje) throws IOException {
        envioDatosServidor.setData(mensaje.getBytes());
        entradaParaEnviarDatos.send(envioDatosServidor);
    }

    /**
     * Envia un jar al servidor.
     * 
     * @param ubicacionArchivo el archivo que se desea enviar
     */
    public void enviarJar(String ubicacionArchivo) {
        try {
            File file = new File(ubicacionArchivo);
            enviarMensajeAServidor(file.getName() + "&" + file.length() + ":");
            try {
                byte[] jarEnBytes = new byte[(int) file.length()];
                FileInputStream archivoEnBytes = new FileInputStream(file);
                archivoEnBytes.read(jarEnBytes);
                archivoEnBytes.close();
                try {
                    envioDatosServidor.setData(jarEnBytes);
                    entradaParaEnviarDatos.send(envioDatosServidor);
                } catch (IOException e) {
                    DialogoInformativo.mostar(
                            "Ha ocurrido un error al intentar enviar el archivo al servidor.\nLa causa del error es: "
                                    + e.toString(),
                            "Error de envio");
                }
            } catch (IOException e) {
                DialogoInformativo.mostar(
                        "Se ha detectado un problema con el archivo especificado.\nLa causa del error es: "
                                + e.toString(),

                        "Error de archivo");
            }
        } catch (IOException e) {
            DialogoInformativo.mostar(
                    "Ha ocurrido un error al intentar enviar el archivo al servidor.\nLa causa del error es: "
                            + e.toString(),
                    "Error de envio");

        }
        obtenerJarsGuardadosEnCarpetaOculta();
        guardadoEsc.performanceDeGuardado(nombresJarsEnCarpetaOculta);
    }

    /**
     * Envía una orden al servidor para indicarle que ejecute un JAR.
     * 
     * @param nombreJar jar que va a ser ejecutado en el servidor
     */
    public void ejecutarJar(String nombreJar) {
        try {
            enviarMensajeAServidor(nombreJar + "|");
        } catch (IOException e) {
            DialogoInformativo.mostar(
                    "Ha ocurrido un error al intentar comunicar al servidor la ejecucion de ese archivo.\nLa causante del error es: "
                            + e.toString(),
                    "Error de comunicacion");
        }
    }

    /**
     * Envía una orden al servidor para indicarle que se reinicie.
     */
    public void reiniciarServidor() {
        try {
            int portaux = envioDatosServidor.getPort();
            envioDatosServidor.setPort(puertoEntradaAuxiliarServidor);
            enviarMensajeAServidor("Reiniciar");
            envioDatosServidor.setPort(portaux);
        } catch (IOException e) {
            DialogoInformativo.mostar(
                    "Ha ocurrido un error al intentar comunicar al servidor su reinicio.\nLa causante del error es: "
                            + e.toString(),
                    "Error de comunicacion");
        }
    }

    /**
     * Comprueba la conexión enviando una solicitud al servidor y recibiendo su
     * respuesta para verificar si se estableció correctamente o no. Va devolver
     * uno de estos dos estados:
     * 
     * Si la comprobación resultó exitosa: "return EstadoConexion.CONECTADO".
     * Si la comprobación resultó fallida: "return EstadoConexion.SERVER_ERROR".
     * 
     * @return {@link EstadoConexion}
     */
    private EstadoConexion siSeEstablecioConexion() {
        try {
            enviarMensajeAServidor("Estado conexion");
            recepcionDatosServidor.setData(new byte[1024], 0, 1024);
            entradaParaRecibirDatos.setSoTimeout(5000);
            entradaParaRecibirDatos.receive(recepcionDatosServidor);
            if (new String(recepcionDatosServidor.getData(), 0, recepcionDatosServidor.getLength())
                    .equals("recibido")) {
                return EstadoConexion.CONECTADO;
            } else {
                DialogoInformativo.mostar(
                        "Ha ocurrido un error al chequear la conexion.El servidor no envio la respuesta esperada, se recomienda reiniciar el servidor",
                        "Error de sincronizacion");
                return EstadoConexion.SERVER_ERROR;
            }
        } catch (IOException e1) {
            DialogoInformativo.mostar(
                    "Ha ocurrido un error al chequear la conexion,\nLa causa del error:" + e1.toString(),
                    "Error de conexion");
            return EstadoConexion.SERVER_ERROR;
        }
    }

    /**
     * Realiza una comprobación del correcto funcionamiento de ciertas tareas que
     * debe realizar el programa y la conexión del cliente con el servidor. Va
     * devolver uno de estos tres tipos de estados:
     * 
     * Si la comprobación resultó exitosa: "return EstadoConexion.CONECTADO;".
     * Si la comprobación resultó fallida por parte del servidor: "return
     * EstadoConexion.SERVER_ERROR".
     * Si la comprobación resultó fallida por parte del cliente: "return
     * EstadoConexion.CLIENTE_ERROR".
     * 
     * NOTA: Puede que la comprobación no sea 100% precisa.
     * 
     * @return {@link EstadoConexion}
     */
    private EstadoConexion comprobacionDeFuncionamientoPeriodica() {
        try {
            DatagramSocket serv = new DatagramSocket(puertoServidor);
            serv.close();
            return EstadoConexion.SERVER_ERROR;
        } catch (Exception e) {
            if (entradaParaRecibirDatos.isClosed()) {
                try {
                    entradaParaRecibirDatos.connect(new InetSocketAddress(InetAddress.getLocalHost(), puerto));
                } catch (SocketException e1) {
                    DialogoInformativo.mostar(
                            "Ha ocurrido un error al intentar conectar el cliente con el servidor.\nLa causante del error es: "
                                    + e.toString(),
                            "Error de conexion");
                    return EstadoConexion.CLIENTE_ERROR;
                } catch (UnknownHostException e1) {
                    DialogoInformativo.mostar(
                            "No se encuentra la ip local, conectate a una red Wi-Fi.\nLa causa del error es:"
                                    + e.toString(),
                            "Error de recepcion");
                    return EstadoConexion.CLIENTE_ERROR;
                }
            } else {
                if (ultimaActlzEstado != EstadoConexion.CONECTADO) {
                    iniciarConexion();
                }
                return siSeEstablecioConexion();
            }
        }
        return EstadoConexion.CONECTADO;
    }

    /**
     * Agrega un nuevo oyente para detectar cuando se guarda un archivo JAR en el
     * servidor, de manera que se pueda realizar una acción en respuesta a dicha
     * acción. Es importante tener en cuenta que si se ejecuta la acción pero el
     * archivo JAR no se guarda, el oyente no se ejecutará.
     * 
     * @param esc oyente a agregar
     */
    public void agregarOyenteGuardado(EscucharGuardadoJar esc) {
        guardadoEsc = esc;
    }

    /**
     * Agrega un nuevo oyente para detectar el actual estado de conexión.
     * 
     * @param esc oyente a agregar
     */
    public void agregarOyenteChequeoConexion(EscucharChequeo esc) {
        chequeoEsc = esc;
    }

    /**
     * Devuelve el último estado de conexión registrado.
     * 
     * NOTA: Puede llegar a no ser el actual estado de conexion.
     * 
     * @return {@link EstadoConexion}
     */
    public EstadoConexion obtenerUltimaActlzEstado() {
        return ultimaActlzEstado;
    }

}

interface EscucharGuardadoJar {
    void performanceDeGuardado(LinkedList<String> jarsEnServidor);
}

interface EscucharChequeo {
    void performanceDeChequeo(EstadoConexion actualEstadoDeConexion);
}

enum EstadoConexion {
    SERVER_ERROR,
    CLIENTE_ERROR,
    CONECTADO
}
