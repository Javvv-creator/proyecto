package gui;

import main.Conexion.Conexion;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class pantallaLogin {

    static final Color ROJO       = new Color(0xD8, 0x1E, 0x3B);
    static final Color VERDE      = new Color(0x6B, 0xA5, 0x39);
    static final Color AMARILLO   = new Color(0xF2, 0xC0, 0x2C);
    static final Color BEIGE      = new Color(0xEF, 0xEB, 0xE7);
    static final Color GRIS_TXT   = new Color(0x6B, 0x6B, 0x6B);
    static final Color GRIS_CAMPO = new Color(0xF0, 0xF0, 0xF0);
    static final Color GRIS_BORDE = new Color(0xDD, 0xDD, 0xDD);

    private JFrame frame;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new pantallaLogin().setVisible(true));
    }

    public pantallaLogin() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }

        frame = new JFrame("Git & Eat! - Iniciar sesión");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.setUndecorated(true);
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();

        if (gd.isFullScreenSupported()) {
            gd.setFullScreenWindow(frame);
        } else {
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        }

        frame.getRootPane().registerKeyboardAction(
            e -> System.exit(0),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        JPanel root = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;

        ImagePanel panelImagen = new ImagePanel(loadImage("/gui/images/imagen1.png"));
        gbc.gridx = 0;
        gbc.weightx = 0.46;
        gbc.weighty = 1.0;
        root.add(panelImagen, gbc);

        JPanel panelDerecho = new JPanel(new GridBagLayout());
        panelDerecho.setBackground(BEIGE);
        gbc.gridx = 1;
        gbc.weightx = 0.54;
        gbc.weighty = 1.0;
        root.add(panelDerecho, gbc);

        JPanel card = crearTarjetaLogin(frame);
        panelDerecho.add(card);

        frame.setContentPane(root);
    }

    public void setVisible(boolean visible) {
        if (frame != null) {
            frame.setVisible(visible);
        }
    }

    private static JPanel crearTarjetaLogin(JFrame frame) {
        RoundedPanel card = new RoundedPanel(36, Color.WHITE);
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(620, 740));
        card.setBorder(new EmptyBorder(40, 56, 40, 56));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;

        Image logo = loadImage("/gui/images/logo.png");
        JLabel logoLabel = new JLabel();
        if (logo != null) {
            int w = 290;
            int h = (int) (logo.getHeight(null) * (w / (double) logo.getWidth(null)));
            logoLabel.setIcon(new ImageIcon(logo.getScaledInstance(w, h, Image.SCALE_SMOOTH)));
        } else {
            logoLabel.setText("GIT & EAT!");
            logoLabel.setFont(new Font("SansSerif", Font.BOLD, 34));
        }
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gc.gridy = 0;
        gc.insets = new Insets(0, 0, 20, 0);
        card.add(logoLabel, gc);

        JLabel titulo = new JLabel("BIENVENIDO!", SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 30));
        titulo.setForeground(Color.BLACK);
        gc.gridy = 1;
        gc.insets = new Insets(0, 0, 8, 0);
        card.add(titulo, gc);

        JLabel subtitulo = new JLabel(
                "<html><div style='text-align:center;'>¡Listo para otro gran turno!<br>Inicia sesión y comencemos.</div></html>",
                SwingConstants.CENTER);
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 16));
        subtitulo.setForeground(GRIS_TXT);
        gc.gridy = 2;
        gc.insets = new Insets(0, 0, 28, 0);
        card.add(subtitulo, gc);

        PlaceholderField campoUsuario = new PlaceholderField("Código de empleado");
        RoundedFieldPanel panelUsuario = new RoundedFieldPanel(
                IconoFactory.icono("persona"), campoUsuario, null);
        gc.gridy = 3;
        gc.insets = new Insets(0, 0, 18, 0);
        card.add(panelUsuario, gc);

        PlaceholderPasswordField campoPin = new PlaceholderPasswordField("PIN");
        JButton toggleOjo = new JButton();
        toggleOjo.setIcon(IconoFactory.icono("ojo"));
        toggleOjo.setBorderPainted(false);
        toggleOjo.setContentAreaFilled(false);
        toggleOjo.setFocusPainted(false);
        toggleOjo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        final boolean[] visible = {false};
        toggleOjo.addActionListener(e -> {
            visible[0] = !visible[0];
            campoPin.setEchoVisible(visible[0]);
            toggleOjo.setIcon(IconoFactory.icono(visible[0] ? "ojo_cerrado" : "ojo"));
        });
        RoundedFieldPanel panelPin = new RoundedFieldPanel(
                IconoFactory.icono("candado"), campoPin, toggleOjo);
        gc.gridy = 4;
        gc.insets = new Insets(0, 0, 28, 0);
        card.add(panelPin, gc);

        RoundButton botonIngresar = new RoundButton("Ingresar", ROJO, Color.WHITE);
        botonIngresar.setPreferredSize(new Dimension(480, 56));
        botonIngresar.addActionListener(e -> {
            String usuario = campoUsuario.getRealText();
            String pin = new String(campoPin.getRealPassword()).trim();

            if (usuario.isEmpty() || pin.isEmpty()) {
                JOptionPane.showMessageDialog(frame,
                        "Ingresá tu código de empleado y tu PIN.",
                        "Datos incompletos", JOptionPane.WARNING_MESSAGE);
                return;
            }

            validarUsuario(frame, usuario, pin);
        });

        gc.gridy = 5;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(8, 0, 8, 0);
        card.add(botonIngresar, gc);

        return card;
    }

    private static void validarUsuario(JFrame frame, String codigoEmpleado, String pin) {
        String sql = "SELECT nombre, apellido, rol, estado FROM usuario WHERE codigo_empleado = ? AND contrasena = ?";
        Conexion conexionBD = new Conexion();

        try (Connection conn = conexionBD.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(frame,
                        "No se pudo establecer conexión con la base de datos.",
                        "Error de Conexión", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, codigoEmpleado);
                stmt.setString(2, pin);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        boolean estadoActivo = rs.getBoolean("estado");

                        if (!estadoActivo) {
                            JOptionPane.showMessageDialog(frame,
                                    "El usuario ingresado se encuentra inactivo.",
                                    "Acceso Denegado", JOptionPane.WARNING_MESSAGE);
                            return;
                        }

                        String nombre = rs.getString("nombre");
                        String apellido = rs.getString("apellido");
                        String rol = rs.getString("rol");

                        JOptionPane.showMessageDialog(frame,
                                "¡Autenticación exitosa!\n\nBienvenido, " + nombre + " " + apellido + "\nRol: " + rol,
                                "Git & Eat!", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(frame,
                                "Código de empleado o PIN incorrectos.",
                                "Error de Autenticación", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame,
                    "Error de SQL: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    static Image loadImage(String ruta) {
        try {
            URL resource = pantallaLogin.class.getResource(ruta);
            if (resource != null) {
                return ImageIO.read(resource);
            }
            InputStream is = pantallaLogin.class.getResourceAsStream(
                    ruta.startsWith("/") ? ruta : "/" + ruta);
            if (is != null) {
                return ImageIO.read(is);
            }
        } catch (Exception ignored) { }
        return null;
    }

    static class ImagePanel extends JPanel {
        private final Image imagen;

        ImagePanel(Image imagen) {
            this.imagen = imagen;
            setBackground(AMARILLO);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (imagen == null) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            
            int pw = getWidth(), ph = getHeight();
            int iw = imagen.getWidth(null), ih = imagen.getHeight(null);
            
            double scale = Math.max((double) pw / iw, (double) ph / ih);
            
            int sw = (int) (iw * scale), sh = (int) (ih * scale);
            int x = (pw - sw) / 2, y = (ph - sh) / 2;
            
            g2.drawImage(imagen, x, y, sw, sh, this);
            g2.dispose();
        }
    }

    static class RoundedPanel extends JPanel {
        private final int radio;
        private final Color colorFondo;

        RoundedPanel(int radio, Color colorFondo) {
            this.radio = radio;
            this.colorFondo = colorFondo;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 20));
            g2.fill(new RoundRectangle2D.Double(3, 5, getWidth() - 6, getHeight() - 6, radio, radio));
            g2.setColor(colorFondo);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 4, getHeight() - 4, radio, radio));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class RoundedFieldPanel extends JPanel {
        RoundedFieldPanel(Icon icono, JTextField campo, JButton extra) {
            setOpaque(false);
            setLayout(new BorderLayout(12, 0));
            setBorder(new EmptyBorder(12, 20, 12, 20));
            setPreferredSize(new Dimension(480, 56));

            JLabel iconoLabel = new JLabel(icono);
            add(iconoLabel, BorderLayout.WEST);

            campo.setOpaque(false);
            campo.setBorder(null);
            campo.setFont(new Font("SansSerif", Font.PLAIN, 16));
            add(campo, BorderLayout.CENTER);

            if (extra != null) {
                add(extra, BorderLayout.EAST);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(GRIS_CAMPO);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
            g2.setColor(GRIS_BORDE);
            g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, getHeight(), getHeight()));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class PlaceholderField extends JTextField {
        private final String placeholder;

        PlaceholderField(String placeholder) {
            this.placeholder = placeholder;
        }

        String getRealText() {
            return getText().trim();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(GRIS_TXT);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(placeholder, 2, y);
                g2.dispose();
            }
        }
    }

    static class PlaceholderPasswordField extends JPasswordField {
        private final String placeholder;
        private final char echoOculto = '•';

        PlaceholderPasswordField(String placeholder) {
            this.placeholder = placeholder;
            setEchoChar(echoOculto);
        }

        void setEchoVisible(boolean visible) {
            setEchoChar(visible ? (char) 0 : echoOculto);
        }

        char[] getRealPassword() {
            return getPassword();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getPassword().length == 0) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(GRIS_TXT);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(placeholder, 2, y);
                g2.dispose();
            }
        }
    }

    static class RoundButton extends JButton {
        private final Color colorFondo;
        private boolean hover = false;

        RoundButton(String texto, Color colorFondo, Color colorTexto) {
            super(texto);
            this.colorFondo = colorFondo;
            setForeground(colorTexto);
            setFont(new Font("SansSerif", Font.BOLD, 18));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color c = hover ? colorFondo.darker() : colorFondo;
            g2.setColor(c);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class IconoFactory {
        static Icon icono(String tipo) {
            return new Icon() {
                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.translate(x, y);
                    g2.setColor(GRIS_TXT);
                    g2.setStroke(new BasicStroke(1.8f));
                    switch (tipo) {
                        case "persona":
                            g2.drawOval(7, 2, 10, 10);
                            g2.drawArc(2, 14, 20, 12, 0, 180);
                            break;
                        case "candado":
                            g2.drawRoundRect(4, 11, 16, 12, 4, 4);
                            g2.drawArc(7, 3, 10, 12, 0, 180);
                            break;
                        case "ojo":
                            g2.drawArc(2, 5, 20, 14, 0, 180);
                            g2.drawArc(2, -4, 20, 14, 180, 180);
                            g2.drawOval(10, 9, 4, 4);
                            break;
                        case "ojo_cerrado":
                            g2.drawLine(3, 12, 21, 12);
                            g2.drawLine(7, 8, 7, 16);
                            g2.drawLine(12, 7, 12, 17);
                            g2.drawLine(17, 8, 17, 16);
                            break;
                    }
                    g2.dispose();
                }
                @Override public int getIconWidth() { return 24; }
                @Override public int getIconHeight() { return 24; }
            };
        }
    }
}