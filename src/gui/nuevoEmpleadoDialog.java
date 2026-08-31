package gui;

import main.Conexion.Conexion;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class nuevoEmpleadoDialog extends JDialog {

    // Paleta de colores ajustada al diseño
    private static final Color COLOR_TEXT_BROWN = new Color(92, 53, 22);
    private static final Color COLOR_INPUT_BG = new Color(240, 235, 227);
    private static final Color COLOR_BTN_CANCEL = new Color(240, 235, 227);
    private static final Color COLOR_BTN_SAVE = new Color(110, 65, 30);
    private static final Color COLOR_CHECKBOX = new Color(106, 161, 46);
    private static final Color COLOR_CHECKBOX_BORDER = new Color(200, 190, 170);

    // Radios de las esquinas redondeadas
    private static final int FIELD_RADIUS = 14;
    private static final int BTN_RADIUS = 20;
    private static final int CHECK_RADIUS = 6;

    // Componentes para obtener datos desde la vista padre
    private JTextField txtNombre;
    private JComboBox<String> cbRol;
    private JPasswordField txtContrasena;
    private JComboBox<String> cbTurno;
    private JCheckBox chkActivo;
    private JButton btnGuardar;
    private JButton btnCancelar;

    private boolean guardado = false;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            nuevoEmpleadoDialog dialog = new nuevoEmpleadoDialog(null);
            dialog.setVisible(true);
        });
    }

    public nuevoEmpleadoDialog(Frame parent) {
        super(parent, "Nuevo empleado", true);
        setUndecorated(true);
        setSize(420, 580);
        setLocationRelativeTo(parent);

        setBackground(new Color(0, 0, 0, 0));

        RoundedPanel mainPanel = new RoundedPanel(25, Color.WHITE);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        // 1. Título
        JLabel lblTitulo = new JLabel("Nuevo empleado");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblTitulo.setForeground(COLOR_TEXT_BROWN);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 2. Campo: Nombre completo
        JLabel lblNombre = createFieldLabel("Nombre completo");
        txtNombre = new RoundedTextField(FIELD_RADIUS);
        styleTextField(txtNombre);

        // 3. Campo: Rol (ComboBox)
        JLabel lblRol = createFieldLabel("Rol");
        cbRol = new RoundedComboBox<>(new String[]{"Seleccionar rol...", "Administrador", "Cajero"}, FIELD_RADIUS);
        styleComboBox(cbRol);

        // 4. Campo: Contraseña
        JLabel lblContrasena = createFieldLabel("PIN / Contraseña");
        txtContrasena = new RoundedPasswordField(FIELD_RADIUS);
        styleTextField(txtContrasena);

        JLabel lblSubtext = new JLabel("Se utilizará junto al código de empleado para iniciar sesión");
        lblSubtext.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblSubtext.setForeground(COLOR_TEXT_BROWN);
        lblSubtext.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 5. Campo: Turno (ComboBox)
        JLabel lblTurno = createFieldLabel("Turno");
        cbTurno = new RoundedComboBox<>(new String[]{"Seleccionar turno...", "Mañana", "Tarde"}, FIELD_RADIUS);
        styleComboBox(cbTurno);

        // 6. Checkbox: Empleado activo
        JPanel checkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        checkPanel.setOpaque(false);
        checkPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        chkActivo = new JCheckBox();
        chkActivo.setSelected(true);
        chkActivo.setOpaque(false);
        chkActivo.setFocusable(false);
        chkActivo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chkActivo.setIcon(new RoundedCheckIcon(22, false));
        chkActivo.setSelectedIcon(new RoundedCheckIcon(22, true));

        JLabel lblActivo = new JLabel("Empleado activo");
        lblActivo.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblActivo.setForeground(COLOR_TEXT_BROWN);

        checkPanel.add(chkActivo);
        checkPanel.add(lblActivo);

        // 7. Botones Cancelar y Guardar
        JPanel actionsPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        actionsPanel.setOpaque(false);
        actionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionsPanel.setMaximumSize(new Dimension(380, 48));

        btnCancelar = new RoundedButton("Cancelar", BTN_RADIUS);
        styleButton(btnCancelar, COLOR_BTN_CANCEL, COLOR_TEXT_BROWN);
        btnCancelar.addActionListener(e -> dispose());

        btnGuardar = new RoundedButton("Guardar empleado", BTN_RADIUS);
        styleButton(btnGuardar, COLOR_BTN_SAVE, Color.WHITE);
        btnGuardar.addActionListener(e -> procesarGuardado());

        actionsPanel.add(btnCancelar);
        actionsPanel.add(btnGuardar);

        // Ensamblado
        mainPanel.add(lblTitulo);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 18)));

        mainPanel.add(lblNombre);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(txtNombre);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        mainPanel.add(lblRol);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(cbRol);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        mainPanel.add(lblContrasena);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(txtContrasena);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        mainPanel.add(lblSubtext);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        mainPanel.add(lblTurno);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(cbTurno);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        mainPanel.add(checkPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        mainPanel.add(actionsPanel);

        add(mainPanel);
    }

    // --- Lógica de procesamiento e inserción en Base de Datos ---

    private void procesarGuardado() {
        String nombreCompleto = txtNombre.getText().trim();
        String contrasena = new String(txtContrasena.getPassword()).trim();
        int indexRol = cbRol.getSelectedIndex();
        int indexTurno = cbTurno.getSelectedIndex();

        // Validaciones de campos
        if (nombreCompleto.isEmpty()) {
            mostrarMensaje("Ingresa el nombre completo del empleado.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (indexRol <= 0) {
            mostrarMensaje("Selecciona un rol válido.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (contrasena.isEmpty()) {
            mostrarMensaje("Ingresa una contraseña o PIN.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (indexTurno <= 0) {
            mostrarMensaje("Selecciona un turno de trabajo.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Separar nombre y apellido
        String[] partesNombre = nombreCompleto.split("\\s+", 2);
        String nombre = partesNombre[0];
        String apellido = partesNombre.length > 1 ? partesNombre[1] : "-";

        String rol = ((String) cbRol.getSelectedItem()).toUpperCase(); // ADMINISTRADOR o CAJERO
        String turno = (String) cbTurno.getSelectedItem();
        boolean activo = chkActivo.isSelected();

        // Guardar en la Base de Datos
        Conexion conexionBD = new Conexion();
        try (Connection conn = conexionBD.getConnection()) {
            if (conn == null) {
                mostrarMensaje("No se pudo conectar a la base de datos.", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Generar código de empleado automático (ej: ADM01, CAJ02)
            String codigoEmpleado = generarCodigoEmpleado(conn, rol);

            String sql = "INSERT INTO usuario (nombre, apellido, codigo_empleado, contrasena, rol, estado, turno) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, nombre);
                stmt.setString(2, apellido);
                stmt.setString(3, codigoEmpleado);
                stmt.setString(4, contrasena);
                stmt.setString(5, rol);
                stmt.setBoolean(6, activo);
                stmt.setString(7, turno);

                int filasAfectadas = stmt.executeUpdate();
                if (filasAfectadas > 0) {
                    mostrarMensaje("¡Empleado registrado exitosamente!\n\nCódigo asignado: " + codigoEmpleado, JOptionPane.INFORMATION_MESSAGE);
                    this.guardado = true;
                    dispose();
                }
            }

        } catch (SQLException ex) {
            mostrarMensaje("Error al guardar en la base de datos: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private String generarCodigoEmpleado(Connection conn, String rol) throws SQLException {
        String prefijo = rol.equalsIgnoreCase("ADMINISTRADOR") ? "ADM" : "CAJ";
        String sql = "SELECT COUNT(*) FROM usuario WHERE rol = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rol);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt(1) + 1;
                    return String.format("%s%02d", prefijo, total);
                }
            }
        }
        return prefijo + "01";
    }

    private void mostrarMensaje(String mensaje, int tipo) {
        JOptionPane.showMessageDialog(this, mensaje, "Git & Eat!", tipo);
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 15));
        lbl.setForeground(COLOR_TEXT_BROWN);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void styleTextField(JTextField field) {
        field.setMaximumSize(new Dimension(380, 38));
        field.setPreferredSize(new Dimension(380, 38));
        field.setBackground(COLOR_INPUT_BG);
        field.setForeground(COLOR_TEXT_BROWN);
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setMaximumSize(new Dimension(380, 38));
        combo.setPreferredSize(new Dimension(380, 38));
        combo.setBackground(COLOR_INPUT_BG);
        combo.setForeground(COLOR_TEXT_BROWN);
        combo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        combo.setFocusable(false);
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public boolean isGuardado() { return guardado; }
    public String getNombre() { return txtNombre.getText().trim(); }
    public String getRol() { return (String) cbRol.getSelectedItem(); }
    public String getContrasena() { return new String(txtContrasena.getPassword()); }
    public String getTurno() { return (String) cbTurno.getSelectedItem(); }
    public boolean isActivo() { return chkActivo.isSelected(); }
    public JButton getBtnGuardar() { return btnGuardar; }

    private static void paintRoundedBackground(Graphics g, JComponent comp, int radius) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(comp.getBackground());
        g2.fillRoundRect(0, 0, comp.getWidth(), comp.getHeight(), radius, radius);
        g2.dispose();
    }

    private static class RoundedTextField extends JTextField {
        private final int radius;

        RoundedTextField(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            paintRoundedBackground(g, this, radius);
            super.paintComponent(g);
        }
    }

    private static class RoundedPasswordField extends JPasswordField {
        private final int radius;

        RoundedPasswordField(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            paintRoundedBackground(g, this, radius);
            super.paintComponent(g);
        }
    }

    private static class RoundedComboBox<E> extends JComboBox<E> {
        private final int radius;

        RoundedComboBox(E[] items, int radius) {
            super(items);
            this.radius = radius;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 8));

            setUI(new BasicComboBoxUI() {
                @Override
                protected JButton createArrowButton() {
                    JButton button = new JButton("▾");
                    button.setFont(new Font("SansSerif", Font.PLAIN, 12));
                    button.setForeground(COLOR_TEXT_BROWN);
                    button.setContentAreaFilled(false);
                    button.setBorderPainted(false);
                    button.setFocusPainted(false);
                    button.setOpaque(false);
                    return button;
                }

                @Override
                public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) { }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            paintRoundedBackground(g, this, radius);
            super.paintComponent(g);
        }
    }

    private static class RoundedButton extends JButton {
        private final int radius;

        RoundedButton(String text, int radius) {
            super(text);
            this.radius = radius;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            paintRoundedBackground(g, this, radius);
            super.paintComponent(g);
        }
    }

    private static class RoundedCheckIcon implements Icon {
        private final int size;
        private final boolean selected;

        RoundedCheckIcon(int size, boolean selected) {
            this.size = size;
            this.selected = selected;
        }

        @Override
        public int getIconWidth() { return size; }

        @Override
        public int getIconHeight() { return size; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);

            if (selected) {
                g2.setColor(COLOR_CHECKBOX);
                g2.fillRoundRect(0, 0, size, size, CHECK_RADIUS, CHECK_RADIUS);

                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int x1 = (int) (size * 0.22), y1 = (int) (size * 0.52);
                int x2 = (int) (size * 0.42), y2 = (int) (size * 0.72);
                int x3 = (int) (size * 0.80), y3 = (int) (size * 0.28);
                g2.drawLine(x1, y1, x2, y2);
                g2.drawLine(x2, y2, x3, y3);
            } else {
                g2.setColor(COLOR_INPUT_BG);
                g2.fillRoundRect(0, 0, size, size, CHECK_RADIUS, CHECK_RADIUS);
                g2.setColor(COLOR_CHECKBOX_BORDER);
                g2.drawRoundRect(0, 0, size - 1, size - 1, CHECK_RADIUS, CHECK_RADIUS);
            }

            g2.dispose();
        }
    }

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
            
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
            
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2.0f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
            
            g2.dispose();
        }
    }
}