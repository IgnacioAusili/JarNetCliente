
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

import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Muestra facilmente un "pop-up" para mostrar un mensaje.
 */
public class DialogoInformativo {

    private static ConfigDialogo dialogo = new ConfigDialogo();

    /**
     * Muestra en pantalla un diálogo con el mensaje y el título establecidos.
     *
     * @param mensaje el mensaje que se mostrará en el diálogo
     * @param titulo  el título que tendrá el diálogo
     */
    public static void mostar(String mensaje, String titulo) {
        dialogo.mostrarDialogoInput(titulo, mensaje);
    }

    /**
     * Crea y configura el diálogo informativo.
     */
    static class ConfigDialogo extends JDialog {
        private JPanel panel = new JPanel(new BorderLayout());
        private JTextArea contenedorDelMensaje = new JTextArea();
        private JScrollPane barra = new JScrollPane(contenedorDelMensaje, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        private JLabel iconoError = new JLabel(new ImageIcon(DialogoInformativo.class.getResource("/error.png")));

        /**
         * Construye un nuevo diálogo informativo, establece sus valores predeterminados
         * y sus componentes.
         */
        public ConfigDialogo() {
            setResizable(true);
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            setSize(400, 210);
            setLayout(null);
            setResizable(false);
            setLocationRelativeTo(null);

            configTextArea();
            configBarraPanel();
            configIconoError();
        }

        /**
         * Configura la apariencia del "JTextArea" que muestra el mensaje establecido
         * por el usuario.
         */
        private void configTextArea() {
            contenedorDelMensaje.setFont(new Font("roboto", 0, 15));
            contenedorDelMensaje.setLineWrap(true);
            contenedorDelMensaje.setWrapStyleWord(true);
            contenedorDelMensaje.setBackground(getBackground());
            contenedorDelMensaje.setEditable(false);
        }

        /**
         * Configura la apariencia del "JScrollPane" que permite desplazarse a través
         * del mensaje.
         */
        private void configBarraPanel() {
            barra.setBorder(null);
            panel.add(BorderLayout.CENTER, barra);
            panel.setBounds(71, 15, 300, 144);
            add(panel);
        }

        /**
         * Configura la apariencia del "JLabel" en el que se muestra el icono de error.
         */
        private void configIconoError() {
            iconoError.setBounds(15, 5, 40, 40);
            add(iconoError);
        }

        /**
         * Se establece en el diálogo el mensaje y el título recibido, y luego se
         * muestra en pantalla.
         * 
         * @param titulo  titulo del dialogo
         * @param mensaje mensaje del dialogo
         */
        protected void mostrarDialogoInput(String titulo, String mensaje) {
            contenedorDelMensaje.setText(mensaje);
            contenedorDelMensaje.setCaretPosition(0);
            setTitle(titulo);
            setVisible(true);
            requestFocus();
        }
    }
}
