package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.net.URL;

/**
 * Pantalla de Gestión de caja - GIT & EAT!
 * Interfaz adaptada fielmente al mockup, manteniendo la consistencia de
 * estilos.
 * Se utilizan Layouts que evitan el estiramiento vertical (anclando al NORTE).
 */
public class gestionCaja extends JFrame {

    // Paleta de colores principal
    private static final Color COLOR_BG = new Color(231, 221, 202);
    private static final Color COLOR_SIDEBAR = new Color(139, 94, 52);
    private static final Color COLOR_HEADER = new Color(139, 94, 52);
    private static final Color COLOR_TEXT_BROWN = new Color(92, 53, 22);
    private static final Color COLOR_SIDEBAR_HOVER = new Color(160, 110, 65);
    private static final Color COLOR_SIDEBAR_ACTIVE = new Color(110, 72, 38);
    private static final Color COLOR_CERRAR_SESION = new Color(211, 53, 58);

    // Colores específicos de la vista de caja
    private static final Color COLOR_CARD_GREEN = new Color(104, 159, 56);
    private static final Color COLOR_CARD_RED = new Color(196, 40, 53);
    private static final Color COLOR_CARD_ORANGE = new Color(222, 131, 77);
    private static final Color COLOR_BTN_YELLOW = new Color(243, 205, 59);
    private static final Color COLOR_BREAKDOWN_BG = new Color(245, 240, 230);
    private static final Color COLOR_STATUS_BG = new Color(76, 92, 70);
    private static final Color COLOR_STATUS_INDICATOR = new Color(56, 142, 60);

    private int selectedMenuIndex = 4; // "Gestión de caja" seleccionado
    private JPanel[] menuButtons;

    public gestionCaja() {
        setTitle("GIT & EAT! - Gestión de caja");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1280, 720));

        JPanel mainContainer = new JPanel(new BorderLayout(15, 15));
        mainContainer.setBackground(COLOR_BG);
        mainContainer.setBorder(new EmptyBorder(15, 15, 15, 15));

        // 1. BARRA LATERAL
        mainContainer.add(createSidebarPanel(), BorderLayout.WEST);

        // 2. PANEL CENTRAL
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setOpaque(false);

        contentPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        // El cuerpo se añade dentro de otro panel con BorderLayout.NORTH
        // para que todo el contenido se empuje hacia arriba y no se estire.
        JPanel bodyWrapper = new JPanel(new BorderLayout());
        bodyWrapper.setOpaque(false);
        bodyWrapper.add(createMainBody(), BorderLayout.NORTH);

        contentPanel.add(bodyWrapper, BorderLayout.CENTER);

        mainContainer.add(contentPanel, BorderLayout.CENTER);
        add(mainContainer);
    }

    private JPanel createSidebarPanel() {
        RoundedPanel sidebar = new RoundedPanel(25, COLOR_SIDEBAR);
        sidebar.setLayout(new BorderLayout(0, 15));
        sidebar.setPreferredSize(new Dimension(320, 0));
        sidebar.setBorder(new EmptyBorder(15, 15, 20, 15));

        RoundedPanel logoCard = new RoundedPanel(20, Color.WHITE);
        logoCard.setPreferredSize(new Dimension(290, 140));
        logoCard.setLayout(new GridBagLayout());
        logoCard.add(createLogoLabel());
        logoCard.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoCard.setToolTipText("Volver al Dashboard");

        sidebar.add(logoCard, BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new BorderLayout(0, 15));
        centerWrapper.setOpaque(false);

        JPanel menuPanel = new JPanel(new GridLayout(6, 1, 0, 8));
        menuPanel.setOpaque(false);

        Object[][] items = {
                { SidebarVectorIcon.IconType.EMPLOYEES, "Gestión de empleados (cajeros)" },
                { SidebarVectorIcon.IconType.MENU, "Gestión de menú / productos" },
                { SidebarVectorIcon.IconType.ORDERS, "Gestión de pedidos" },
                { SidebarVectorIcon.IconType.REPORTS, "Reportes y estadísticas" },
                { SidebarVectorIcon.IconType.CASH, "Gestión de caja" },
                { SidebarVectorIcon.IconType.SECURITY, "Seguridad y auditoría" }
        };

        menuButtons = new JPanel[items.length];

        for (int i = 0; i < items.length; i++) {
            final int index = i;
            SidebarVectorIcon.IconType iconType = (SidebarVectorIcon.IconType) items[i][0];
            String textHtml = (String) items[i][1];

            // Formateo de saltos de línea para que coincida visualmente
            if (i == 0)
                textHtml = "<html>Gestión de<br>empleados (cajeros)</html>";
            if (i == 1)
                textHtml = "<html>Gestión de menú /<br>productos</html>";

            RoundedPanel btnPanel = new RoundedPanel(15, i == selectedMenuIndex ? COLOR_SIDEBAR_ACTIVE : COLOR_SIDEBAR);
            btnPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 8));

            JLabel iconLbl = new JLabel(new SidebarVectorIcon(iconType, 28));
            JLabel textLbl = new JLabel(textHtml);
            textLbl.setFont(new Font("SansSerif", Font.BOLD, 17));
            textLbl.setForeground(Color.WHITE);

            btnPanel.add(iconLbl);
            btnPanel.add(textLbl);
            btnPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

            btnPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (selectedMenuIndex != index) {
                        btnPanel.setBackgroundColor(COLOR_SIDEBAR_HOVER);
                        btnPanel.repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (selectedMenuIndex != index) {
                        btnPanel.setBackgroundColor(COLOR_SIDEBAR);
                        btnPanel.repaint();
                    }
                }

                @Override
                public void mouseClicked(MouseEvent e) {

                    if (index == 0) {
                        gestionEmpleados app = new gestionEmpleados();
                        app.setVisible(true);
                        dispose();
                        return;
                    }
                    if (index == 1) {
                        gestionProductos app = new gestionProductos();
                        app.setVisible(true);
                        dispose();
                        return;
                    } else if (index == 3) {
                        pantallaEstadistica app = new pantallaEstadistica();
                        app.setVisible(true);
                        dispose();
                        return;
                    }
                    else if (index == 4) {
                        gestionCaja app = new gestionCaja();
                        app.setVisible(true);
                        dispose();
                        return;
                    }
                     else
                    if (index == 5) {
                        seguridadAuditoria app = new seguridadAuditoria();
                        app.setVisible(true);
                        dispose();
                        return;
                    }
                    // Las demás ventanas todavía no existen
                    JOptionPane.showMessageDialog(
                            gestionCaja.this,
                            "Esta sección todavía está en desarrollo.",
                            "Próximamente",
                            JOptionPane.INFORMATION_MESSAGE);
        }});

            menuButtons[i] = btnPanel;
            menuPanel.add(btnPanel);
        }

        centerWrapper.add(menuPanel, BorderLayout.NORTH);

        RoundedPanel btnCerrarSesion = new RoundedPanel(15, COLOR_CERRAR_SESION);
        btnCerrarSesion.setLayout(new GridBagLayout());
        btnCerrarSesion.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrarSesion.setPreferredSize(new Dimension(0, 55));

        JLabel lblCerrarSesion = new JLabel("Cerrar Sesión");
        lblCerrarSesion.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblCerrarSesion.setForeground(Color.WHITE);
        btnCerrarSesion.add(lblCerrarSesion);

        JPanel logoutWrapper = new JPanel(new BorderLayout());
        logoutWrapper.setOpaque(false);
        logoutWrapper.add(btnCerrarSesion, BorderLayout.SOUTH);

        sidebar.add(centerWrapper, BorderLayout.CENTER);
        sidebar.add(logoutWrapper, BorderLayout.SOUTH);

        return sidebar;
    }

    private JLabel createLogoLabel() {
        JLabel lblLogo = new JLabel();
        URL logoUrl = getClass().getResource("/gui/images/logo.png");
        if (logoUrl == null) {
            logoUrl = getClass().getResource("/images/logo.png");
        }

        if (logoUrl != null) {
            ImageIcon icon = new ImageIcon(logoUrl);
            Image img = icon.getImage();
            int w = img.getWidth(null);
            int h = img.getHeight(null);
            if (w > 0 && h > 0) {
                double scale = Math.min(270.0 / w, 120.0 / h);
                int targetW = (int) (w * scale);
                int targetH = (int) (h * scale);
                lblLogo.setIcon(new ImageIcon(img.getScaledInstance(targetW, targetH, Image.SCALE_SMOOTH)));
            } else {
                lblLogo.setIcon(icon);
            }
        } else {
            lblLogo.setText("<html><center><font size='7' color='#6EA32E'><b>&lt; 🍴 &gt;</b></font><br>" +
                    "<font size='5'><b color='#6EA32E'>GIT & </b><b color='#D13941'>EAT!</b></font></center></html>");
        }
        return lblLogo;
    }

    private JPanel createHeaderPanel() {
        RoundedPanel header = new RoundedPanel(20, COLOR_HEADER);
        header.setLayout(new BorderLayout());
        header.setPreferredSize(new Dimension(0, 125));
        header.setBorder(new EmptyBorder(20, 35, 20, 35));

        JLabel title = new JLabel("Gestión de caja");
        title.setFont(new Font("SansSerif", Font.BOLD, 40));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        rightHeader.setOpaque(false);

        return header;
    }

    private JPanel createMainBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(8, 6, 8, 6));

        // 1. Fila de Usuario y Estado
        body.add(createUserAndStatusRow());
        body.add(Box.createRigidArea(new Dimension(0, 20)));

        // 2. Fila de Resumen Principal (Verde y Rojo)
        body.add(createMainSummaryRow());
        body.add(Box.createRigidArea(new Dimension(0, 20)));

        // 3. Panel de Desglose de Pagos
        body.add(createBreakdownPanel());
        body.add(Box.createRigidArea(new Dimension(0, 20)));

        // 4. Fila de Detalles (Tarjetas regalo y Efectivo)
        body.add(createDetailsRow());
        body.add(Box.createRigidArea(new Dimension(0, 30)));

        // 5. Botones de Acción
        body.add(createActionButtonsRow());

        return body;
    }

    private JPanel createUserAndStatusRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60)); // Limita la altura

        // Izquierda: Usuario
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        userPanel.setOpaque(false);

        JLabel userIcon = new JLabel(new CustomIcon(CustomIcon.Type.USER_PROFILE, 50, COLOR_CARD_GREEN));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel nameLbl = new JLabel("Samantha martinez");
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        nameLbl.setForeground(COLOR_TEXT_BROWN);

        JLabel roleLbl = new JLabel("Cajera - Turno tarde");
        roleLbl.setFont(new Font("SansSerif", Font.PLAIN, 15));
        roleLbl.setForeground(Color.DARK_GRAY);

        textPanel.add(nameLbl);
        textPanel.add(roleLbl);

        userPanel.add(userIcon);
        userPanel.add(textPanel);

        // Derecha: Estado de Caja
        RoundedPanel statusBadge = new RoundedPanel(25, COLOR_STATUS_BG);
        statusBadge.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 12));
        statusBadge.setPreferredSize(new Dimension(200, 50));

        JLabel dotIcon = new JLabel(new CustomIcon(CustomIcon.Type.STATUS_DOT, 22, COLOR_STATUS_INDICATOR));
        JLabel statusText = new JLabel("Caja abierta");
        statusText.setFont(new Font("SansSerif", Font.BOLD, 17));
        statusText.setForeground(Color.WHITE);

        statusBadge.add(dotIcon);
        statusBadge.add(statusText);

        JPanel rightAlignPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightAlignPanel.setOpaque(false);
        rightAlignPanel.add(statusBadge);

        row.add(userPanel, BorderLayout.WEST);
        row.add(rightAlignPanel, BorderLayout.EAST);

        return row;
    }

    private JPanel createMainSummaryRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 20, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 110));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        row.add(createSummaryCard("Monto inicial:", "Q.500.00", COLOR_CARD_GREEN));
        row.add(createSummaryCard("Ventas del dia:", "Q.500.00", COLOR_CARD_RED));

        return row;
    }

    private JPanel createSummaryCard(String title, String amount, Color bgColor) {
        RoundedPanel card = new RoundedPanel(20, bgColor);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 19));
        titleLbl.setForeground(Color.BLACK);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel amountLbl = new JLabel(amount);
        amountLbl.setFont(new Font("SansSerif", Font.PLAIN, 34));
        amountLbl.setForeground(Color.BLACK);
        amountLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLbl);
        card.add(Box.createVerticalGlue());
        card.add(amountLbl);

        return card;
    }

    private JPanel createBreakdownPanel() {
        RoundedPanel panel = new RoundedPanel(20, COLOR_BREAKDOWN_BG);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(10, 25, 10, 25));

        // Usamos altura fija y evitar estiramientos
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        panel.add(createBreakdownRow(CustomIcon.Type.COIN, "Efectivo:", "Q.1000.00"));
        panel.add(new DashedSeparator());
        panel.add(createBreakdownRow(CustomIcon.Type.CREDIT_CARD, "Tarjeta :", "Q.850.00"));
        panel.add(new DashedSeparator());
        panel.add(createBreakdownRow(CustomIcon.Type.GIFT_CARD, "Tarjetas de regalo :", "Q.180.00"));

        return panel;
    }

    private JPanel createBreakdownRow(CustomIcon.Type iconType, String label, String amount) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(12, 0, 12, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);

        JLabel iconLbl = new JLabel(new CustomIcon(iconType, 32, Color.BLACK));
        JLabel textLbl = new JLabel(label);
        textLbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        textLbl.setForeground(Color.BLACK);

        leftPanel.add(iconLbl);
        leftPanel.add(textLbl);

        JLabel amountLbl = new JLabel(amount);
        amountLbl.setFont(new Font("SansSerif", Font.PLAIN, 22));
        amountLbl.setForeground(Color.BLACK);

        row.add(leftPanel, BorderLayout.WEST);
        row.add(amountLbl, BorderLayout.EAST);

        return row;
    }

    private JPanel createDetailsRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 20, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 140));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        // Tarjeta Naranja (Tarjetas de regalo)
        RoundedPanel orangeCard = new RoundedPanel(20, COLOR_CARD_ORANGE);
        orangeCard.setLayout(new BoxLayout(orangeCard, BoxLayout.Y_AXIS));
        orangeCard.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel titleOrange = new JLabel("Tarjetas de regalo");
        titleOrange.setFont(new Font("SansSerif", Font.BOLD, 19));
        titleOrange.setForeground(Color.WHITE);
        titleOrange.setAlignmentX(Component.LEFT_ALIGNMENT);

        orangeCard.add(titleOrange);
        orangeCard.add(Box.createRigidArea(new Dimension(0, 15)));
        orangeCard.add(createDetailSubRow("Canjeadas hoy (8)", "Q.150.00", Color.WHITE));
        orangeCard.add(Box.createRigidArea(new Dimension(0, 10)));
        orangeCard.add(createDetailSubRow("Vendidas hoy (3)", "Q.300.00", Color.WHITE));

        // Tarjeta Verde (Efectivo detalles)
        RoundedPanel greenCard = new RoundedPanel(20, COLOR_CARD_GREEN);
        greenCard.setLayout(new BoxLayout(greenCard, BoxLayout.Y_AXIS));
        greenCard.setBorder(new EmptyBorder(20, 25, 20, 25));

        greenCard.add(createDetailSubRow("Efectivo esperado:", "Q.850.00", Color.BLACK));
        greenCard.add(Box.createRigidArea(new Dimension(0, 10)));
        greenCard.add(createDetailSubRow("Efectivo contado:", "Q.850.00", Color.BLACK));
        greenCard.add(Box.createRigidArea(new Dimension(0, 15)));
        greenCard.add(createDetailSubRow("Diferencia:", "Q.0.00", Color.BLACK));

        row.add(orangeCard);
        row.add(greenCard);

        return row;
    }

    private JPanel createDetailSubRow(String label, String amount, Color textColor) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        JLabel lblLeft = new JLabel(label);
        lblLeft.setFont(new Font("SansSerif", Font.PLAIN, 18));
        lblLeft.setForeground(textColor);

        JLabel lblRight = new JLabel(amount);
        lblRight.setFont(new Font("SansSerif", Font.PLAIN, 18));
        lblRight.setForeground(textColor);

        row.add(lblLeft, BorderLayout.WEST);
        row.add(lblRight, BorderLayout.EAST);

        return row;
    }

    private JPanel createActionButtonsRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        RoundedPanel btnCerrar = createActionButton(CustomIcon.Type.CLOSE_X, "Cerrar caja");
        RoundedPanel btnHistorial = createActionButton(CustomIcon.Type.HISTORY, "Historial de cajas");

        row.add(btnCerrar);
        row.add(btnHistorial);

        return row;
    }

    private RoundedPanel createActionButton(CustomIcon.Type iconType, String text) {
        RoundedPanel btn = new RoundedPanel(20, COLOR_BTN_YELLOW);
        btn.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 12));
        btn.setPreferredSize(new Dimension(240, 50));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel iconLbl = new JLabel(new CustomIcon(iconType, 22, COLOR_TEXT_BROWN));
        JLabel textLbl = new JLabel(text);
        textLbl.setFont(new Font("SansSerif", Font.BOLD, 17));
        textLbl.setForeground(COLOR_TEXT_BROWN);

        btn.add(iconLbl);
        btn.add(textLbl);

        // Hover Effect
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackgroundColor(COLOR_BTN_YELLOW.darker());
                btn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackgroundColor(COLOR_BTN_YELLOW);
                btn.repaint();
            }
        });

        return btn;
    }

    private static class DashedSeparator extends JPanel {
        DashedSeparator() {
            setOpaque(false);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            float[] dash = { 6.0f, 6.0f };
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
            g2.setColor(new Color(210, 200, 185)); // Marrón muy claro
            g2.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
            g2.dispose();
        }
    }

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
            Graphics2D graphics = (Graphics2D) g.create();
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(backgroundColor);
            graphics.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
            graphics.dispose();
        }
    }

    private static class SidebarVectorIcon implements Icon {
        public enum IconType {
            EMPLOYEES, MENU, ORDERS, REPORTS, CASH, SETTINGS, SECURITY
        }

        private final IconType type;
        private final int size;

        public SidebarVectorIcon(IconType type, int size) {
            this.type = type;
            this.size = size;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            g2.setColor(Color.WHITE);
            float scale = size / 32.0f;
            g2.scale(scale, scale);

            switch (type) {
                case EMPLOYEES:
                    g2.fillOval(10, 3, 12, 12);
                    g2.fillArc(4, 16, 24, 18, 0, 180);
                    g2.setColor(new Color(255, 255, 255, 180));
                    g2.fillOval(20, 6, 8, 8);
                    g2.fillArc(17, 15, 14, 12, 0, 180);
                    break;
                case MENU:
                    g2.fillArc(3, 5, 26, 14, 0, 180);
                    g2.fillRoundRect(2, 14, 28, 4, 2, 2);
                    g2.fillRoundRect(4, 20, 24, 6, 3, 3);
                    break;
                case ORDERS:
                    g2.setStroke(new BasicStroke(2.2f));
                    g2.drawRoundRect(5, 5, 22, 24, 4, 4);
                    g2.fillRoundRect(11, 3, 10, 4, 2, 2);
                    g2.drawLine(9, 12, 23, 12);
                    g2.drawLine(9, 17, 23, 17);
                    g2.drawLine(9, 22, 18, 22);
                    break;
                case REPORTS:
                    g2.fillRoundRect(4, 18, 6, 10, 2, 2);
                    g2.fillRoundRect(13, 12, 6, 16, 2, 2);
                    g2.fillRoundRect(22, 6, 6, 22, 2, 2);
                    break;
                case CASH:
                    g2.setStroke(new BasicStroke(2.0f));
                    g2.drawRoundRect(3, 7, 26, 18, 4, 4);
                    g2.drawOval(11, 11, 10, 10);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                    g2.drawString("Q", 16 - g2.getFontMetrics().stringWidth("Q") / 2, 19);
                    break;
                case SECURITY:
                    Path2D shield = new Path2D.Double();
                    shield.moveTo(16, 3);
                    shield.curveTo(24, 3, 27, 5, 27, 13);
                    shield.curveTo(27, 22, 18, 27, 16, 29);
                    shield.curveTo(14, 27, 5, 22, 5, 13);
                    shield.curveTo(5, 5, 8, 3, 16, 3);
                    shield.closePath();
                    g2.setStroke(new BasicStroke(2.2f));
                    g2.draw(shield);
                    Path2D check = new Path2D.Double();
                    check.moveTo(11, 15);
                    check.lineTo(14, 18);
                    check.lineTo(21, 11);
                    g2.draw(check);
                    break;
                default:
                    break;
            }
            g2.dispose();
        }
    }

    private static class CustomIcon implements Icon {
        public enum Type {
            MONEY_BAG, USER_PROFILE, STATUS_DOT, COIN, CREDIT_CARD, GIFT_CARD, CLOSE_X, HISTORY
        }

        private final Type type;
        private final int size;
        private final Color color;

        public CustomIcon(Type type, int size, Color color) {
            this.type = type;
            this.size = size;
            this.color = color;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            g2.setColor(color);
            float scale = size / 32.0f;
            g2.scale(scale, scale);

            switch (type) {
                case MONEY_BAG:
                    // Dibujamos la bolsa de dinero blanca
                    Path2D bag = new Path2D.Double();
                    bag.moveTo(12, 8);
                    bag.lineTo(20, 8);
                    bag.lineTo(18, 4);
                    bag.lineTo(14, 4);
                    bag.closePath();
                    g2.fill(bag);
                    g2.fillOval(5, 8, 22, 22);
                    // Dibujamos el signo $ en medio
                    g2.setColor(COLOR_HEADER); // Marrón de fondo
                    g2.setFont(new Font("SansSerif", Font.BOLD, 15));
                    g2.drawString("$", 16 - g2.getFontMetrics().stringWidth("$") / 2, 24);
                    break;
                case USER_PROFILE:
                    g2.fillOval(0, 0, 32, 32); // Círculo exterior
                    g2.setColor(Color.WHITE); // Figura interna blanca
                    g2.fillOval(10, 6, 12, 12); // Cabeza
                    g2.fillArc(4, 20, 24, 18, 0, 180); // Hombros
                    break;
                case STATUS_DOT:
                    g2.fillOval(4, 4, 24, 24);
                    break;
                case COIN:
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawOval(4, 4, 24, 24);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 15));
                    g2.drawString("$", 16 - g2.getFontMetrics().stringWidth("$") / 2, 21);
                    break;
                case CREDIT_CARD:
                    g2.fillRoundRect(2, 6, 28, 20, 4, 4);
                    g2.setColor(COLOR_BREAKDOWN_BG); // Mismo que el fondo para "cortar"
                    g2.fillRect(2, 10, 28, 4); // Banda magnética
                    g2.fillRect(6, 18, 6, 3); // Chip
                    break;
                case GIFT_CARD:
                    g2.fillRoundRect(2, 6, 28, 20, 4, 4);
                    g2.setColor(COLOR_BREAKDOWN_BG); // "Cortar" para hacer el lazo
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawLine(10, 6, 10, 26); // Lazo vertical
                    g2.drawOval(6, 2, 5, 5); // Oreja del moño izq
                    g2.drawOval(10, 2, 5, 5); // Oreja del moño der
                    break;
                case CLOSE_X:
                    g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(6, 6, 26, 26);
                    g2.drawLine(26, 6, 6, 26);
                    break;
                case HISTORY:
                    g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawOval(4, 4, 24, 24);
                    g2.drawLine(16, 10, 16, 16);
                    g2.drawLine(16, 16, 22, 16);
                    break;
            }
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> {
            gestionCaja app = new gestionCaja();
            app.setVisible(true);
        });
    }
}