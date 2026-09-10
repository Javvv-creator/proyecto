package gui;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import main.Crud.crud;

/**
 * Diálogo Emergente para Restablecer Contraseña
 *
 * Permite solicitar: - Contraseña actual - Nueva contraseña
 */
public class restablecerContraseñaDialog extends JDialog {

    // Paleta de colores exacta compartida con el sistema
    private static final Color COLOR_TEXT_BROWN = new Color(92, 53, 22);
    private static final Color COLOR_TEXT_MUTED = new Color(130, 110, 95);
    private static final Color COLOR_TEXT_RED = new Color(211, 53, 58);
    private static final Color COLOR_SEPARATOR = new Color(215, 200, 185);
    private static final Color COLOR_CARD_BG = Color.WHITE;
    private static final Color COLOR_FIELD_BG = new Color(250, 248, 245);
    private static final Color COLOR_BORDER = new Color(220, 210, 195);
    private static final Color COLOR_PRIMARY_BTN = new Color(92, 53, 22);
    private static final Color COLOR_PRIMARY_BTN_HOVER = new Color(120, 68, 28);
    private static final Color COLOR_CANCEL_BTN = new Color(235, 230, 222);
    private static final Color COLOR_CANCEL_BTN_HOVER = new Color(220, 212, 200);

    // Constante para el radio de las esquinas del cuadro emergente
    private static final int CARD_RADIUS = 22;

    // Componentes de formulario
    private JPasswordField txtContrasenaActual;
    private JPasswordField txtNuevaContrasena;
    private JLabel lblMensajeError;

    // Estado del resultado
    // Estado del resultado
    private boolean confirmado = false;
    private String contrasenaActual = "";
    private String nuevaContrasena = "";
    private final int idUsuario;

    public restablecerContraseñaDialog(Frame parent, int idUsuario) {
        super(parent, "Restablecer Contraseña", true);
        this.idUsuario = idUsuario;
        setUndecorated(true);
        setSize(380, 420);
        // ... el resto del constructor sigue exactamente igual ...

        // Centrado matemático exacto sobre la ventana principal o pantalla
        centrarDialogo(parent);

        // Cierre automático con la tecla ESC
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Fondo transparente para visualizar las esquinas redondeadas del cuadro
        setBackground(new Color(0, 0, 0, 0));

        RoundedPanel mainContainer = new RoundedPanel(CARD_RADIUS, COLOR_CARD_BG);
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBorder(new EmptyBorder(20, 22, 20, 22));

        // 1. Encabezado con ícono de candado y título
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));

        ActionIcon lockIcon = new ActionIcon(ActionIcon.Type.LOCK, COLOR_TEXT_BROWN, 26);
        JLabel lblTitle = new JLabel("Restablecer Contraseña");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTitle.setForeground(COLOR_TEXT_BROWN);

        headerPanel.add(lockIcon);
        headerPanel.add(lblTitle);

        JLabel lblSubTitle = new JLabel("<html><center>Ingrese su contraseña actual y la nueva contraseña para actualizar su acceso.</center></html>");
        lblSubTitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSubTitle.setForeground(COLOR_TEXT_MUTED);
        lblSubTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setForeground(COLOR_SEPARATOR);
        separator.setBackground(COLOR_SEPARATOR);
        separator.setMaximumSize(new Dimension(340, 1));

        // 2. Campo: Contraseña Actual
        JPanel fieldGroupActual = createPasswordFieldGroup("Contraseña actual:", false);
        txtContrasenaActual = (JPasswordField) fieldGroupActual.getClientProperty("field");

        // 3. Campo: Nueva Contraseña
        JPanel fieldGroupNueva = createPasswordFieldGroup("Nueva contraseña:", true);
        txtNuevaContrasena = (JPasswordField) fieldGroupNueva.getClientProperty("field");

        // Label de retroalimentación de error
        lblMensajeError = new JLabel(" ");
        lblMensajeError.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblMensajeError.setForeground(COLOR_TEXT_RED);
        lblMensajeError.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 4. Panel de botones (Cancelar / Guardar)
        JPanel actionButtonsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        actionButtonsPanel.setOpaque(false);
        actionButtonsPanel.setMaximumSize(new Dimension(340, 42));

        JButton btnCancelar = createRoundedButton("Cancelar", COLOR_CANCEL_BTN, COLOR_CANCEL_BTN_HOVER, COLOR_TEXT_BROWN);
        btnCancelar.addActionListener(e -> dispose());

        JButton btnGuardar = createRoundedButton("Guardar", COLOR_PRIMARY_BTN, COLOR_PRIMARY_BTN_HOVER, Color.WHITE);
        btnGuardar.addActionListener(e -> procesarGuardado());

        actionButtonsPanel.add(btnCancelar);
        actionButtonsPanel.add(btnGuardar);

        // Permitir guardar al presionar ENTER en el campo de nueva contraseña
        txtNuevaContrasena.addActionListener(e -> procesarGuardado());

        mainContainer.add(headerPanel);
        mainContainer.add(Box.createRigidArea(new Dimension(0, 6)));
        mainContainer.add(lblSubTitle);
        mainContainer.add(Box.createRigidArea(new Dimension(0, 12)));
        mainContainer.add(separator);
        mainContainer.add(Box.createRigidArea(new Dimension(0, 14)));
        mainContainer.add(fieldGroupActual);
        mainContainer.add(Box.createRigidArea(new Dimension(0, 10)));
        mainContainer.add(fieldGroupNueva);
        mainContainer.add(Box.createRigidArea(new Dimension(0, 6)));
        mainContainer.add(lblMensajeError);
        mainContainer.add(Box.createRigidArea(new Dimension(0, 10)));
        mainContainer.add(actionButtonsPanel);

        add(mainContainer);
    }

    /**
     * Valida los campos ingresados y confirma la acción si todo está correcto.
     */
    private void procesarGuardado() {
        String actual = new String(txtContrasenaActual.getPassword()).trim();
        String nueva = new String(txtNuevaContrasena.getPassword()).trim();

        if (actual.isEmpty()) {
            lblMensajeError.setText("Ingrese la contraseña actual.");
            txtContrasenaActual.requestFocus();
            return;
        }

        if (nueva.isEmpty()) {
            lblMensajeError.setText("Ingrese la nueva contraseña.");
            txtNuevaContrasena.requestFocus();
            return;
        }

        if (nueva.length() < 4) {
            lblMensajeError.setText("La nueva contraseña debe tener al menos 4 caracteres.");
            txtNuevaContrasena.requestFocus();
            return;
        }

        if (actual.equals(nueva)) {
            lblMensajeError.setText("La nueva contraseña no puede ser igual a la actual.");
            txtNuevaContrasena.requestFocus();
            return;
        }

        if (idUsuario <= 0) {
            lblMensajeError.setText("No se pudo identificar al usuario.");
            return;
        }

        // Validar la contraseña actual contra la base de datos
        String contrasenaGuardada = crud.obtenerContrasena(idUsuario);

        if (contrasenaGuardada == null) {
            lblMensajeError.setText("No se encontró el usuario en la base de datos.");
            return;
        }

        if (!contrasenaGuardada.equals(actual)) {
            lblMensajeError.setText("La contraseña actual es incorrecta.");
            txtContrasenaActual.requestFocus();
            return;
        }

        boolean actualizado = crud.actualizarContrasena(idUsuario, nueva);

        if (!actualizado) {
            lblMensajeError.setText("No se pudo actualizar la contraseña. Intente de nuevo.");
            return;
        }

        this.contrasenaActual = actual;
        this.nuevaContrasena = nueva;
        this.confirmado = true;
        dispose();
        ajustesEmpleadoDialog ventana = new ajustesEmpleadoDialog(null);
        ventana.dispose();
    }

    /**
     * Genera un grupo etiquetado con input de contraseña estilizado y botón
     * para alternar visibilidad.
     */
    private JPanel createPasswordFieldGroup(String labelText, boolean requestFocusOnFocus) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setOpaque(false);
        group.setAlignmentX(Component.CENTER_ALIGNMENT);
        group.setMaximumSize(new Dimension(340, 68));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setForeground(COLOR_TEXT_BROWN);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Panel redondeado contenedor del input
        RoundedPanel fieldPanel = new RoundedPanel(12, COLOR_FIELD_BG);
        fieldPanel.setLayout(new BorderLayout(5, 0));
        fieldPanel.setMaximumSize(new Dimension(340, 40));
        fieldPanel.setPreferredSize(new Dimension(340, 40));
        fieldPanel.setBorder(new EmptyBorder(4, 10, 4, 8));

        JPasswordField pf = new JPasswordField();
        pf.setFont(new Font("SansSerif", Font.PLAIN, 15));
        pf.setForeground(COLOR_TEXT_BROWN);
        pf.setBackground(COLOR_FIELD_BG);
        pf.setBorder(null);
        pf.setCaretColor(COLOR_TEXT_BROWN);

        // Botón ojo para mostrar/ocultar contraseña
        ToggleButtonEye btnToggleEye = new ToggleButtonEye();
        btnToggleEye.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                btnToggleEye.toggleSelected();
                if (btnToggleEye.isSelected()) {
                    pf.setEchoChar((char) 0); // Mostrar texto
                } else {
                    pf.setEchoChar('•'); // Ocultar texto
                }
            }
        });

        fieldPanel.add(pf, BorderLayout.CENTER);
        fieldPanel.add(btnToggleEye, BorderLayout.EAST);

        group.add(lbl);
        group.add(Box.createRigidArea(new Dimension(0, 4)));
        group.add(fieldPanel);

        group.putClientProperty("field", pf);
        return group;
    }

    /**
     * Construye botones redondeados estilizados con eventos hover.
     */
    private JButton createRoundedButton(String text, Color bgNormal, Color bgHover, Color textFg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(bgHover.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bgHover);
                } else {
                    g2.setColor(bgNormal);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setForeground(textFg);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Centra el diálogo en relación al padre o pantalla principal.
     */
    private void centrarDialogo(Frame parent) {
        if (parent != null && parent.isShowing()) {
            try {
                Point parentLocation = parent.getLocationOnScreen();
                int x = parentLocation.x + (parent.getWidth() - getWidth()) / 2;
                int y = parentLocation.y + (parent.getHeight() - getHeight()) / 2;
                setLocation(x, y);
                return;
            } catch (Exception ignored) {
            }
        }
        setLocationRelativeTo(null);
    }

    public boolean isConfirmado() {
        return confirmado;
    }

    public String getContrasenaActual() {
        return contrasenaActual;
    }

    public String getNuevaContrasena() {
        return nuevaContrasena;
    }

    /**
     * Componente gráfico para dibujar íconos vectoriales en Java 2D con
     * antialiasing.
     */
    private static class ActionIcon extends JComponent {

        public enum Type {
            LOCK
        }

        private final Type type;
        private final Color color;

        public ActionIcon(Type type, Color color, int size) {
            this.type = type;
            this.color = color;
            setPreferredSize(new Dimension(size, size));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            g2.setColor(color);

            float scale = Math.min(w, h) / 32.0f;
            g2.scale(scale, scale);

            if (type == Type.LOCK) {
                g2.setStroke(new BasicStroke(2.2f));
                // Arco superior del candado
                g2.drawArc(10, 4, 12, 14, 0, 180);
                // Cuerpo del candado
                g2.fillRoundRect(8, 12, 16, 14, 4, 4);
                // Ojo de cerradura
                g2.setColor(Color.WHITE);
                g2.fillOval(14, 17, 4, 4);
            }

            g2.dispose();
        }
    }

    /**
     * Botón con ícono vectorial de Ojo en Java 2D para alternar mostrar/ocultar
     * contraseña.
     */
    private static class ToggleButtonEye extends JComponent {

        private boolean selected = false;

        public ToggleButtonEye() {
            setPreferredSize(new Dimension(24, 24));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setToolTipText("Mostrar/Ocultar contraseña");
        }

        public boolean isSelected() {
            return selected;
        }

        public void toggleSelected() {
            this.selected = !this.selected;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(COLOR_TEXT_MUTED);
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Silueta externa del ojo
            Path2D eye = new Path2D.Double();
            eye.moveTo(3, 12);
            eye.quadTo(12, 4, 21, 12);
            eye.quadTo(12, 20, 3, 12);
            eye.closePath();
            g2.draw(eye);

            // Pupila central
            g2.fillOval(9, 9, 6, 6);

            // Si está tachado (oculto)
            if (!selected) {
                g2.setColor(COLOR_TEXT_RED);
                g2.drawLine(4, 20, 20, 4);
            }

            g2.dispose();
        }
    }

    /**
     * Panel contenedor con esquinas redondeadas y borde continuo tenue.
     */
    private static class RoundedPanel extends JPanel {

        private final int cornerRadius;
        private final Color backgroundColor;

        public RoundedPanel(int radius, Color bgColor) {
            this.cornerRadius = radius;
            this.backgroundColor = bgColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Relleno de fondo redondeado
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

            // Borde tenue de contorno
            g2.setColor(COLOR_BORDER);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

            g2.dispose();
        }
    }

    // Método main para previsualizar y probar el diálogo de forma independiente
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            restablecerContraseñaDialog dialog = new restablecerContraseñaDialog(null, 1);
            dialog.setVisible(true);

            if (dialog.isConfirmado()) {
                System.out.println("--- Contraseña Restablecida Exitosamente ---");
                System.out.println("Contraseña Actual: " + dialog.getContrasenaActual());
                System.out.println("Nueva Contraseña: " + dialog.getNuevaContrasena());
            } else {
                System.out.println("Operación cancelada por el usuario.");
            }
            System.exit(0);
        });
    }
}
