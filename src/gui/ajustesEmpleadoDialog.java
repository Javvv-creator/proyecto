package gui;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Diálogo Emergente (Modal/Popup) para Ajustes de Empleado - GIT & EAT!
 * Incluye opciones interactivas con íconos vectoriales HD dibujados en Java 2D:
 * - Editar datos
 * - Restablecer contraseña
 * - Ver historial de órdenes
 * - Desactivar empleado
 */
public class ajustesEmpleadoDialog extends JDialog {

    // Paleta de colores exacta
    private static final Color COLOR_TEXT_BROWN = new Color(92, 53, 22);
    private static final Color COLOR_TEXT_RED = new Color(211, 53, 58);
    private static final Color COLOR_SEPARATOR = new Color(215, 200, 185);
    private static final Color COLOR_HOVER_BG = new Color(248, 244, 237);
    private static final Color COLOR_CARD_BG = Color.WHITE;

    // Constante para el radio de las esquinas del cuadro emergente
    private static final int CARD_RADIUS = 22;

    // Identificador de la opción seleccionada
    public enum AccionSeleccionada {
        NINGUNA,
        EDITAR_DATOS,
        RESTABLECER_CONTRASENA,
        VER_HISTORIAL,
        DESACTIVAR_EMPLEADO
    }

    private AccionSeleccionada accionEfectuada = AccionSeleccionada.NINGUNA;

    public ajustesEmpleadoDialog(Frame parent) {
        super(parent, "Ajustes de Empleado", true);
        setUndecorated(true);
        setSize(360, 260);

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
        mainContainer.setBorder(new EmptyBorder(15, 15, 15, 15));

        // 1. Botón: Editar datos
        JPanel btnEditar = createMenuItem(
            ActionIcon.Type.EDIT,
            "Editar datos",
            COLOR_TEXT_BROWN,
            () -> seleccionarAccion(AccionSeleccionada.EDITAR_DATOS)
        );

        // 2. Botón: Restablecer contraseña
        JPanel btnRestablecer = createMenuItem(
            ActionIcon.Type.LOCK,
            "Restablecer contraseña",
            COLOR_TEXT_BROWN,
            () -> seleccionarAccion(AccionSeleccionada.RESTABLECER_CONTRASENA)
        );

        // 3. Botón: Ver historial de órdenes
        JPanel btnHistorial = createMenuItem(
            ActionIcon.Type.HISTORY,
            "Ver historial de órdenes",
            COLOR_TEXT_BROWN,
            () -> seleccionarAccion(AccionSeleccionada.VER_HISTORIAL)
        );

        // Separador sutil horizontal
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setForeground(COLOR_SEPARATOR);
        separator.setBackground(COLOR_SEPARATOR);
        separator.setMaximumSize(new Dimension(330, 1));
        separator.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 4. Botón: Desactivar empleado (Rojo)
        JPanel btnDesactivar = createMenuItem(
            ActionIcon.Type.DISABLE_USER,
            "Desactivar empleado",
            COLOR_TEXT_RED,
            () -> seleccionarAccion(AccionSeleccionada.DESACTIVAR_EMPLEADO)
        );

        // Ensamblado vertical con espaciado uniforme
        mainContainer.add(btnEditar);
        mainContainer.add(Box.createRigidArea(new Dimension(0, 4)));
        mainContainer.add(btnRestablecer);
        mainContainer.add(Box.createRigidArea(new Dimension(0, 4)));
        mainContainer.add(btnHistorial);
        mainContainer.add(Box.createRigidArea(new Dimension(0, 10)));
        mainContainer.add(separator);
        mainContainer.add(Box.createRigidArea(new Dimension(0, 10)));
        mainContainer.add(btnDesactivar);

        add(mainContainer);
    }

    /**
     * Calcula la posición X e Y exactas para que el cuadro quede centrado en el medio de la pantalla o ventana padre.
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
                // Si ocurre un error al calcular coordenadas en pantalla, usa el fallback predeterminado
            }
        }
        setLocationRelativeTo(null);
    }

    /**
     * Construye una fila tipo botón con ícono, texto y comportamiento táctil/hover.
     */
    private JPanel createMenuItem(ActionIcon.Type iconType, String titleText, Color textColor, Runnable onClickAction) {
        RoundedPanel itemPanel = new RoundedPanel(12, COLOR_CARD_BG);
        itemPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 8));
        itemPanel.setMaximumSize(new Dimension(330, 46));
        itemPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        itemPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Componente de ícono vectorial
        ActionIcon iconComp = new ActionIcon(iconType, textColor, 28);

        // Etiqueta del texto
        JLabel lblTitle = new JLabel(titleText);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTitle.setForeground(textColor);

        itemPanel.add(iconComp);
        itemPanel.add(lblTitle);

        // Eventos de ratón (hover e interacción al hacer clic)
        itemPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                itemPanel.setBackgroundColor(COLOR_HOVER_BG);
                itemPanel.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                itemPanel.setBackgroundColor(COLOR_CARD_BG);
                itemPanel.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (onClickAction != null) {
                    onClickAction.run();
                }
            }
        });

        return itemPanel;
    }

    private void seleccionarAccion(AccionSeleccionada accion) {
        this.accionEfectuada = accion;
        dispose();
    }

    public AccionSeleccionada getAccionEfectuada() {
        return accionEfectuada;
    }

    /**
     * Componente gráfico que renderiza los íconos vectoriales de la lista en Java 2D con suavizado.
     */
    private static class ActionIcon extends JComponent {
        public enum Type { EDIT, LOCK, HISTORY, DISABLE_USER }

        private final Type type;
        private final Color color;
        private final int iconSize;

        public ActionIcon(Type type, Color color, int size) {
            this.type = type;
            this.color = color;
            this.iconSize = size;
            setPreferredSize(new Dimension(size, size));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            int w = getWidth();
            int h = getHeight();
            g2.setColor(color);

            float scale = Math.min(w, h) / 32.0f;
            g2.scale(scale, scale);

            switch (type) {
                case EDIT: // Ícono de Cuadro con Lápiz
                    g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawRoundRect(4, 8, 20, 20, 4, 4);
                    
                    // Lápiz diagonal
                    Path2D pencil = new Path2D.Double();
                    pencil.moveTo(14, 18);
                    pencil.lineTo(26, 6);
                    pencil.lineTo(28, 8);
                    pencil.lineTo(16, 20);
                    pencil.closePath();
                    g2.fill(pencil);
                    break;

                case LOCK: // Ícono de Candado
                    g2.setStroke(new BasicStroke(2.2f));
                    // Arco superior
                    g2.drawArc(10, 4, 12, 14, 0, 180);
                    // Cuerpo del candado
                    g2.fillRoundRect(8, 12, 16, 14, 4, 4);
                    // Ojo de cerradura
                    g2.setColor(Color.WHITE);
                    g2.fillOval(14, 17, 4, 4);
                    break;

                case HISTORY: // Ícono de Hoja de Historial / Documento
                    g2.fillRoundRect(7, 4, 18, 24, 4, 4);
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(11, 10, 21, 10);
                    g2.drawLine(11, 15, 21, 15);
                    g2.drawLine(11, 20, 18, 20);
                    break;

                case DISABLE_USER: // Ícono de Usuario Desactivado / Círculo con Línea Diagonal
                    g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    // Círculo exterior
                    g2.drawOval(4, 4, 24, 24);
                    // Silueta de usuario adentro
                    g2.fillOval(13, 8, 6, 6);
                    g2.fillArc(9, 16, 14, 10, 0, 180);
                    // Línea diagonal que cruza
                    g2.drawLine(6, 26, 26, 6);
                    break;
            }

            g2.dispose();
        }
    }

    /**
     * Panel con esquinas redondeadas y borde sutil de contorno.
     */
    private static class RoundedPanel extends JPanel {
        private final int cornerRadius;
        private Color backgroundColor;

        public RoundedPanel(int radius, Color bgColor) {
            this.cornerRadius = radius;
            this.backgroundColor = bgColor;
            setOpaque(false);
        }

        public void setBackgroundColor(Color bgColor) {
            this.backgroundColor = bgColor;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Relleno de fondo redondeado
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

            // Borde tenue exterior
            g2.setColor(new Color(220, 210, 195));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

            g2.dispose();
        }
    }

    // Método main para ejecutar y previsualizar directamente este cuadro emergente
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            ajustesEmpleadoDialog dialog = new ajustesEmpleadoDialog(null);
            dialog.setVisible(true);

            // Mostrar el resultado de la opción elegida en consola
            System.out.println("Opción elegida: " + dialog.getAccionEfectuada());
        });
    }
}