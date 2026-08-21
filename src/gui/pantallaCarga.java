package gui;

import java.awt.*;
import javax.swing.*;

public class pantallaCarga {

    public void iniciarCarga() {
        SwingUtilities.invokeLater(() -> {
            JWindow splash = new JWindow();

            // Cargar el GIF
            ImageIcon iconOriginal = new ImageIcon(pantallaCarga.class.getResource("/gui/cargador/VideoCarga1.gif"));

            // Definir dimensiones (450x300)
            int anchoDeseado = 450;
            int altoDeseado = 300;

            // Crear el JLabel que redimensiona el GIF
            JLabel labelGIF = new JLabel(iconOriginal) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    // Activar el suavizado de la imagen
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    
                    // Dibujar el GIF
                    g2d.drawImage(iconOriginal.getImage(), 0, 0, anchoDeseado, altoDeseado, this);
                    g2d.dispose();
                }
            };

            labelGIF.setPreferredSize(new Dimension(anchoDeseado, altoDeseado));

            // Configuración de la ventana
            splash.getContentPane().add(labelGIF);
            splash.pack();
            splash.setLocationRelativeTo(null); // Centrar en la pantalla
            splash.setVisible(true);

            // Temporizador de 9 segundos (9000 ms)
            Timer timer = new Timer(10000, e -> {
                splash.dispose(); // Cierra el splash

                // Abrir la pantalla de Login
                pantallaLogin login = new pantallaLogin();
                login.setVisible(true);
            });
            
            timer.setRepeats(false);
            timer.start();
        });
    }
}