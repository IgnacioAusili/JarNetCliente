
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Locale;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.LineBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.LinkedHashSet;
import javax.swing.BoxLayout;

/*
 * Ventana única y principal del programa contiene todos los componentes de la interfaz de usuario y es donde se inicia el cliente.
 */
public class JarNetUI extends JFrame {

    private JTextField ubicacionJarVisual = new JTextField() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (super.getText().equals("")) {
                g.setColor(Color.GRAY);
                g.drawChars(" Ingresa la ubicacion del jar a enviar".toCharArray(), 0, 38, 0, 20);
            }
        }

    };
    private JButton buscadorJarVisual = new JButton(
            new ImageIcon(JarNetUI.class.getResource("/explorador.png"))),
            enviarJarVisual = new JButton("Enviar"), ejecutarJarVisual = new JButton("Ejecutar");
    private JFileChooser buscadorJar = new JFileChooser();
    private Cliente micliente = new Cliente();
    private JLabel etiquetaError = new JLabel(), etiquetaEstadoConexion = new JLabel("Conexion:") {
        protected void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
            if (EstadoConexion.CONECTADO == micliente.obtenerUltimaActlzEstado()) {
                g.setColor(Color.GREEN);
                etiquetaEstadoConexion.setToolTipText("Conectado");
            } else {
                g.setColor(Color.RED);
                etiquetaEstadoConexion.setToolTipText("Desconectado");
            }
            g.fillOval(etiquetaEstadoConexion.getWidth() - 30, 5, 10, 10);
        };
    }, etiquetaCreador = new JLabel("Creado por: Ignacio Inzerilli."),
            etiquetaSolicitud = new JLabel("<html>Estado solicitud:<br>...</br></html>"),
            etiquetaReinicio = new JLabel("Reiniciar servidor"),
            contenedorImagenDecorativa = new JLabel(new ImageIcon(JarNetUI.class.getResource("/imgdecorativa.png")));
    private JListPersonalizado listaJarsVisual = new JListPersonalizado();
    private Timer actualizarEtiquetaSol = new Timer(5000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            etiquetaSolicitud.setText("<html>Estado solicitud:<br>...</br></html>");
        }
    });

    /**
     * Se crea una nueva ventana de tipo JFrame y se configuran sus dimensiones,
     * título y opciones de cierre. Luego, se inicia un nuevo cliente que se conecta
     * al servidor especificado mediante un objeto Socket y se establecen los flujos
     * de entrada y salida necesarios para la comunicación.
     */
    public JarNetUI() {
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 300);
        setLocationRelativeTo(null);
        setLayout(null);
        setTitle("JarNet");
        setIconImage(new ImageIcon(getClass().getResource("/JarNetIcono.png")).getImage());
        setFocusable(true);

        iniciarComponentes();
        iniciarEventos();
        setVisible(true);
        toFront();

        micliente.start();
        requestFocus();
    }

    /**
     * Inicializa los componentes de la interfaz gráfica de usuario. Es importante
     * que sea llamado al inicio del programa para asegurar que todos los
     * componentes estén configurados correctamente.
     */
    private void iniciarComponentes() {
        configUbicacionJarVisual();
        configBuscJarVisual();
        configExplorador();
        configEnviarJarVisual();
        configListaJarsEnServVisual();
        configEjecutarJarVisual();
        configEtiquetaError();
        configEtiquetaEstadoConexion();
        configListaJarsEnServVisual();
        configEtiquetaCreador();
        configEtiquetaSolicitud();
        configEtiquetaReinicio();
        configContenedorImagenDeco();
        configSeparador();
        repaint();
    }

    /**
     * Inicializa los eventos de la aplicación, es decir, establece
     * los listeners para los componentes interactivos de la interfaz gráfica de
     * usuario. Es importante que sea llamado al inicio del programa para asegurar
     * que todos los componentes estén configurados correctamente.
     */
    private void iniciarEventos() {
        new Eventos();
    }

    /**
     * Configura la apariencia del JListPersonalizado que muestra
     * los jar existentes en el servidor en forma de lista en la UI.
     */
    private void configListaJarsEnServVisual() {
        listaJarsVisual.setSize(140, 137);
        listaJarsVisual.setLocation(buscadorJarVisual.getX() + buscadorJarVisual.getWidth() + 25,
                23);
        listaJarsVisual.setBorder(new LineBorder(Color.gray));
        listaJarsVisual.setForeground(Color.GRAY);
        listaJarsVisual.setText("<html><font color=\"gray\">  ➔ Jars en servidor</font></html>");
        add(listaJarsVisual);
    }

    /**
     * Configura la apariencia del JTextField que muestra la ubicación del jar
     * seleccionado por el usuario en la interfaz gráfica de usuario.
     */
    private void configUbicacionJarVisual() {
        ubicacionJarVisual.setBounds(10, 130, 270, 30);
        ubicacionJarVisual.setFocusable(true);
        add(ubicacionJarVisual);
    }

    /**
     * Configura la apariencia del JButton que abre el explorador
     * de archivos para seleccionar un archivo jar.
     */
    private void configBuscJarVisual() {
        buscadorJarVisual.setSize(18, 18);
        buscadorJarVisual.setLocation(ubicacionJarVisual.getWidth() + buscadorJarVisual.getWidth() - 3,
                (ubicacionJarVisual.getY() + ubicacionJarVisual.getHeight() / 2) - buscadorJarVisual.getHeight() / 2);
        buscadorJarVisual.setBorder(null);
        buscadorJarVisual.setContentAreaFilled(false);
        buscadorJarVisual.setFocusable(false);
        buscadorJarVisual.setCursor(new Cursor(Cursor.HAND_CURSOR));
        add(buscadorJarVisual);
    }

    /**
     * Configura la parte lógica del explorador de archivos para permitir que el
     * usuario solo seleccione archivos con la extensión ".jar" y establezca su
     * ubicación.
     */
    private void configExplorador() {
        buscadorJar.setFileSelectionMode(JFileChooser.FILES_ONLY);
        buscadorJar.setAcceptAllFileFilterUsed(false);
        buscadorJar.setFileFilter(new FileNameExtensionFilter("Archivos Jar (*.jar)", "jar"));
        buscadorJar.setLocale(new Locale("es", "ES"));
        buscadorJar.setMultiSelectionEnabled(false);
        buscadorJar.setCurrentDirectory(new File(System.getProperty("user.home") + "/Desktop"));
    }

    /**
     * Configura la apariencia del "JButton" que envia los jars al
     * servidor.
     */
    private void configEnviarJarVisual() {
        enviarJarVisual.setSize(90, 30);
        enviarJarVisual.setLocation(
                (ubicacionJarVisual.getX() + ubicacionJarVisual.getWidth() / 2) - enviarJarVisual.getWidth() / 2,
                ubicacionJarVisual.getY() + ubicacionJarVisual.getHeight() + 12);
        enviarJarVisual.setFocusable(false);
        enviarJarVisual.setEnabled(false);
        add(enviarJarVisual);
    }

    /**
     * Crea y configura un componente "JSeparator", para divir en dos secciones la
     * ventana.
     */
    private void configSeparador() {
        JSeparator lineaSeparadora = new JSeparator(SwingConstants.VERTICAL);
        lineaSeparadora.setBounds(310, 13, 1,
                getHeight() - 103);
        lineaSeparadora.setForeground(Color.BLACK);
        add(lineaSeparadora);
    }

    /**
     * Configura la apariencia del "JButton" que ejecuta los jars que están en el
     * servidor.
     */
    private void configEjecutarJarVisual() {
        ejecutarJarVisual.setSize(enviarJarVisual.getWidth(), enviarJarVisual.getHeight());
        ejecutarJarVisual.setLocation(
                (listaJarsVisual.getX() + listaJarsVisual.getWidth() / 2) - ejecutarJarVisual.getWidth() / 2,
                listaJarsVisual.getHeight() + listaJarsVisual.getY() + 12);
        ejecutarJarVisual.setEnabled(false);
        add(ejecutarJarVisual);
    }

    /**
     * Configura la apariencia del "JLabel" que muestra el nombre del creador del
     * programa. (NO MODIFICAR).
     */
    private void configEtiquetaCreador() {
        etiquetaCreador.setSize(160, 23);
        etiquetaCreador.setLocation(2, 0);
        etiquetaCreador.setFont(new Font("Arial", Font.ITALIC, 12));
        add(etiquetaCreador);
    }

    /**
     * Configura la apariencia del "JLabel" que muestra mensajes de error
     * relacionados con la ubicación del archivo ".jar" seleccionado por el usuario.
     */
    private void configEtiquetaError() {
        etiquetaError.setSize(ubicacionJarVisual.getWidth(), 10);
        etiquetaError.setLocation(ubicacionJarVisual.getX(),
                ubicacionJarVisual.getY() + ubicacionJarVisual.getHeight());
        etiquetaError.setForeground(Color.RED);
        etiquetaError.setFont(new Font("Roboto", 0, 10));
        add(etiquetaError);
    }

    /**
     * Configura la apariencia del "JLabel" que muestra el estado de la conexión del
     * cliente con el servidor.
     */
    private void configEtiquetaEstadoConexion() {
        etiquetaEstadoConexion.setSize(90, 20);
        etiquetaEstadoConexion.setLocation(getWidth() - etiquetaEstadoConexion.getWidth() - 3, 0);
        add(etiquetaEstadoConexion);
    }

    /**
     * Configura la apariencia del "JLabel" en el que aparecen los mensajes del
     * estado de las solicitudes realizadas por el usuario.
     */
    private void configEtiquetaSolicitud() {
        etiquetaSolicitud.setSize(250, 46);
        etiquetaSolicitud.setLocation(230, (getHeight() - etiquetaSolicitud.getHeight()) - 50);
        actualizarEtiquetaSol.setRepeats(false);
        add(etiquetaSolicitud);
    }

    /**
     * 
     * Configura la apariencia del "JLabel" en la que aparece el texto "Reiniciar
     * servidor".
     */
    private void configEtiquetaReinicio() {
        etiquetaReinicio.setSize(100, 20);
        etiquetaReinicio.setLocation(5, getHeight() - etiquetaReinicio.getHeight() - 40);
        etiquetaReinicio.setFont(new Font("Arial", Font.HANGING_BASELINE, 10));
        add(etiquetaReinicio);
    }

    /**
     * Configura la apariencia del "JLabel" en la que aparece una imagen decorativa.
     */
    private void configContenedorImagenDeco() {
        contenedorImagenDecorativa.setSize(294, 100);
        contenedorImagenDecorativa.setLocation(8, 23);
        contenedorImagenDecorativa.setOpaque(true);
        contenedorImagenDecorativa.setBackground(Color.BLACK);
        contenedorImagenDecorativa.setToolTipText("Autor: IA Lexica Aperture v3");
        add(contenedorImagenDecorativa);
    }

    /**
     * Muestra un mensaje de estado en la ventana de la aplicación con el objetivo
     * de proporcionar una forma sencilla de mostrar información al usuario sobre el
     * estado de las solicitudes o mensajes de error.
     * 
     * @param mensaje el mensaje de estado que se va a mostrar en la ventana de la
     *                aplicación.
     */
    public void ponerMensajeEstado(String mensaje) {
        if (mensaje.substring( // Mensaje de error
                0, 0).equals("#")) {
            etiquetaSolicitud.setText(
                    "<html>Estado solicitud:<br><font color=\"red\">" + mensaje.replaceFirst("#", "")
                            + "</font></br></html>");
        } else { // Mensaje de exito
            etiquetaSolicitud
                    .setText("<html>Estado solicitud:<br><font color=\"#44f814\">" + mensaje + "</font></br></html>");
        }
        actualizarEtiquetaSol.start();
    }

    /**
     * Este componente muestra los elementos que se le pasan en formato de "String",
     * en forma de lista y los ordena por el orden en el que fueron agregados al
     * componente.
     */
    private class JListPersonalizado extends JScrollPane {

        private JLabel contenedor = new JLabel();
        private LinkedHashSet<String> etiquetas = new LinkedHashSet<>();
        private JLabel etiquetaSeleccionada;

        /**
         * Construye un componente y configura su comportamiento de manera que los
         * elementos que se agreguen a él se ordenen uno debajo del otro en formato de
         * lista.
         */
        public JListPersonalizado() {
            setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            setViewportView(contenedor);
            contenedor.setHorizontalTextPosition(SwingConstants.CENTER);
            contenedor.setVerticalTextPosition(SwingConstants.CENTER);
            contenedor.setLayout(new BoxLayout(contenedor, BoxLayout.Y_AXIS));
            contenedor.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (etiquetaSeleccionada != null) {
                        etiquetaSeleccionada.setBackground(getBackground());
                        etiquetaSeleccionada = null;
                        repaint();
                        ejecutarJarVisual.setEnabled(false);
                    }
                }
            });
        }

        /**
         * 
         * Permite agregar elementos a la lista.
         * 
         * Al pasarle un String, se genera un JLabel con el mismo incorporado, se
         * configura y se añade al panel.
         * 
         * @param nombre nombre que va a tener el elemento.
         */
        public void agregarElemento(String nombre) {
            contenedor.setText("");
            if (!etiquetas.contains(nombre)) {
                JLabel etiqueta = new JLabel(nombre, SwingConstants.CENTER) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        if (etiquetaSeleccionada != null && etiquetaSeleccionada.getText().equals(super.getText())) {
                            g.setColor(new Color(44, 110, 171, 78));
                            g.fillRect(0, 0, super.getWidth(), super.getHeight());
                        } else {
                            super.setBackground(JListPersonalizado.this.getBackground());
                        }
                        super.paintComponent(g);
                    }
                };
                etiqueta.setName(nombre);
                etiqueta.setPreferredSize(new Dimension(JListPersonalizado.this.getWidth(), 20));
                etiqueta.setMaximumSize(etiqueta.getPreferredSize());
                etiqueta.setMinimumSize(etiqueta.getPreferredSize());
                etiqueta.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if (etiquetaSeleccionada == etiqueta) {
                            etiquetaSeleccionada = null;
                            etiqueta.repaint();
                            ejecutarJarVisual.setEnabled(false);
                        } else {
                            ejecutarJarVisual.setEnabled(true);
                            etiquetaSeleccionada = etiqueta;
                            JListPersonalizado.this.repaint();
                        }
                    }
                });
                etiquetas.add(nombre);
                contenedor.add(etiqueta);
                contenedor.repaint();
                Dimension preferredSize = contenedor.getPreferredSize();
                preferredSize.height += etiqueta.getPreferredSize().height;
                contenedor.setPreferredSize(preferredSize);
                revalidate();
                repaint();
            }
        }

        /**
         * Elimina todos los elementos del componente.
         */
        public void eliminarTodosLosElementos() {
            Component et[] = contenedor.getComponents();
            for (Component etiqueta : et) {
                if (etiquetas.contains(etiqueta.getName())) {
                    etiquetas.remove(etiqueta.getName());
                    contenedor.remove(etiqueta);
                }
            }
            repaint();
        }

        /**
         * Agrega todos los elementos que estén en el "LinkedList".
         * 
         * @param elementos lista con los elementos a agregar
         */
        public void agregarTodosLosElementos(LinkedList<String> elementos) {
            for (String nombreJar : elementos) {
                agregarElemento(nombreJar);
            }
        }

        /**
         * Devuelve si existe el nombre del elemento seleccionado. Si el elemento no
         * existe, devuelve ("").
         * 
         * @return String
         */
        public String obtenerElementoSeleccionado() {
            return (etiquetaSeleccionada != null ? etiquetaSeleccionada.getText() : "");
        }

        /**
         * Muestra un mensaje de texto en el contenedor.
         */
        public void setText(String mensaje) {
            contenedor.setText(mensaje);
        }

        public boolean siExisteElElemento(String elementoABuscar) {
            return etiquetas.contains(elementoABuscar);
        }

        /**
         * Devuelve si hay elementos o no en el componente.
         * 
         * @return boolean
         */
        public boolean siHayElementos() {
            return !etiquetas.isEmpty();
        }
    }

    /**
     * Establece todos los eventos de los componentes que aparecen en la ventana del
     * programa.
     */
    private class Eventos {
        /**
         * Inicializa los eventos de los componentes.
         */
        protected Eventos() {
            abirExplorador();
            envioJarAServidor();
            comprobarExistenciaArchivo();
            actualizarDatosLista();
            ejecutarJarEnServidor();
            actualizarVisualEstadoConexion();
            actualizarTextoUbicJarVisual();
            actualizarFocoVentana();
            accionesEtiquetaReinicioServidor();
        }

        /**
         * Agrega al objeto "micliente" un listener llamado "EscucharChequeo" para
         * obtener el cambio de conexión y actualizar el componente
         * "etiquetaEstadoConexion" para mostrar el nuevo estado.
         */
        private void actualizarVisualEstadoConexion() {
            micliente.agregarOyenteChequeoConexion(new EscucharChequeo() {
                @Override
                public void performanceDeChequeo(EstadoConexion actualEstadoDeConexion) {
                    etiquetaEstadoConexion.repaint();
                    if (actualEstadoDeConexion == EstadoConexion.CONECTADO) {
                        enviarJarVisual.setEnabled(
                                !ubicacionJarVisual.getText().equals("") && etiquetaError.getText().equals(""));
                    } else {
                        enviarJarVisual.setEnabled(false);
                        listaJarsVisual.eliminarTodosLosElementos();
                    }
                }
            });
        }

        /**
         * Agrega un "MouseListener" a la ventana del programa, para obtener el foco de
         * la misma al hacer click en ella."
         */
        private void actualizarFocoVentana() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    requestFocus();
                }
            });
        }

        /**
         * Agrega un "ActionListener" al componente "ejecutarJarVisual", para llevar a
         * cabo la acción de ejecutar el jar seleccionado.
         */
        private void ejecutarJarEnServidor() {
            ejecutarJarVisual.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    micliente.ejecutarJar(listaJarsVisual.obtenerElementoSeleccionado());
                }
            });
        }

        /**
         * Agrega un "ActionListener" al componente "buscadorJarVisual", para llevar a
         * cabo la acción de abrir el explorador de archivos.
         */
        private void abirExplorador() {
            buscadorJarVisual.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (buscadorJar.showOpenDialog(buscadorJarVisual) == JFileChooser.APPROVE_OPTION) {
                        ubicacionJarVisual.setText(buscadorJar.getSelectedFile().getAbsolutePath());
                        enviarJarVisual.setEnabled(EstadoConexion.CONECTADO == micliente.obtenerUltimaActlzEstado());
                        ubicacionJarVisual.setForeground(Color.BLACK);
                    }
                }
            });
        };

        /*
         * Agrega un "FocusListener" al componente llamado "ubicacionJarVisual" para
         * permitir la actualización del texto que se mostrará o no al usuario al
         * interactuar con dicho componente."
         */
        private void actualizarTextoUbicJarVisual() {
            ubicacionJarVisual.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    repaint();
                }
            });
        }

        /*
         * Agrega un "CaretListener" al componente "ubicacionJarVisual", para comprobar
         * la validez de la ubicación del archivo jar ingresada por el usuario.
         */
        private void comprobarExistenciaArchivo() {
            ubicacionJarVisual.addCaretListener(new CaretListener() {
                @Override
                public void caretUpdate(CaretEvent e) {
                    if (ubicacionJarVisual.getText().equals("")) {
                        etiquetaError.setText("");
                        etiquetaError.repaint();
                    } else if (!ubicacionJarVisual.getText().endsWith(".jar") || ubicacionJarVisual.getText().isBlank()
                            || !Files.exists(Path.of(ubicacionJarVisual.getText()))) {
                        enviarJarVisual.setEnabled(false);
                        etiquetaError.setText("Ubicacion invalida.");
                    } else {
                        enviarJarVisual.setEnabled(!etiquetaEstadoConexion.getToolTipText().equals("Desconectado"));
                        etiquetaError.setText("");
                    }
                }

            });
        }

        /**
         * Agrega un "ActionListener" al componente "enviarJarVisual" para enviar el
         * archivo JAR seleccionado por el usuario al servidor.
         */
        private void envioJarAServidor() {
            enviarJarVisual.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        String ubicDelJar = ubicacionJarVisual.getText();
                        if (listaJarsVisual
                                .siExisteElElemento(ubicDelJar.substring(ubicDelJar.lastIndexOf(File.separator) + 1))) {
                            if (JOptionPane.showConfirmDialog(null,
                                    "Estas seguro que deseas enviar este archibo?\nYa existe un jar con ese nombre en el servidor, si lo envias, se sobreescribita ese jar",
                                    "Estas seguro?", JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                                micliente.enviarJar(ubicacionJarVisual.getText());
                            }
                        } else {
                            micliente.enviarJar(ubicacionJarVisual.getText());
                        }
                    } catch (Exception er) {
                        er.printStackTrace();
                    }
                }
            });
        }

        /**
         * Agrega un listener "MouseListener" al componente "etiquetaReinicio" para
         * llevar a cabo la acción de reiniciar el servidor y actualizar la apariencia
         * del componente.
         */
        private void accionesEtiquetaReinicioServidor() {
            etiquetaReinicio.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    etiquetaReinicio.setText("<html><u><font color=\"#1ec6ff\">Reiniciar servidor</font></u></html>");
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    etiquetaReinicio.setText("Reiniciar servidor");
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (etiquetaReinicio.getMousePosition() != null && 1 == JOptionPane.showConfirmDialog(null,
                            "Estas seguro que quieres reiniciar el servidor?\nPodria no solucionar el problema.",
                            "Dialogo de confirmacion", JOptionPane.YES_NO_OPTION)) {
                        micliente.reiniciarServidor();
                    }
                }
            });
        }

        /**
         * Agrega un listener "escucharGuardadoJar" al objeto "micliente", para que
         * cuando se detecte un nuevo archivo .jar guardado en el servidor, se muestre
         * en la lista correspondiente en la interfaz gráfica de usuario.
         */
        private void actualizarDatosLista() {
            micliente.agregarOyenteGuardado(new EscucharGuardadoJar() {
                @Override
                public void performanceDeGuardado(LinkedList<String> nombresJarsGuardadosEnServidor) {
                    if (listaJarsVisual.siHayElementos()) {
                        listaJarsVisual.agregarElemento(nombresJarsGuardadosEnServidor.getLast());
                    } else {
                        listaJarsVisual.agregarTodosLosElementos(nombresJarsGuardadosEnServidor);
                    }
                    repaint();
                }
            });
        }
    }
}