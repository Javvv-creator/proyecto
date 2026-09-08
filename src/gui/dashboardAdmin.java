package gui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import main.Crud.crud;

/**
 * Dashboard Administrador - GIT & EAT! Aplicación Java Swing con diseño
 * moderno, interactividad, reloj en vivo e íconos HD.
 */
public class dashboardAdmin extends JFrame {

    // Paleta de colores principal
    private static final Color COLOR_BG = new Color(231, 221, 202);
    private static final Color COLOR_SIDEBAR = new Color(139, 94, 52);
    private static final Color COLOR_HEADER = new Color(139, 94, 52);
    private static final Color COLOR_CARD_BG = Color.WHITE;
    private static final Color COLOR_GREEN = new Color(106, 161, 46);
    private static final Color COLOR_YELLOW = new Color(243, 205, 59);
    private static final Color COLOR_RED = new Color(211, 53, 58);
    private static final Color COLOR_PREPARANDO = new Color(214, 201, 168);
    private static final Color COLOR_TABLE_GRID = new Color(222, 210, 191);
    private static final Color COLOR_SIDEBAR_HOVER = new Color(160, 110, 65);
    private static final Color COLOR_SIDEBAR_ACTIVE = new Color(110, 72, 38);

    private JLabel lblClock;
    private JLabel lblDate;
    private int selectedMenuIndex = 0;
    private JPanel[] menuButtons;

    public dashboardAdmin() {
        setTitle("GIT & EAT! - Dashboard Administrador");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1280, 720));

        JPanel mainContainer = new JPanel(new BorderLayout(15, 15));
        mainContainer.setBackground(COLOR_BG);
        mainContainer.setBorder(new EmptyBorder(15, 15, 15, 15));

        // 1. BARRA LATERAL (SIDEBAR)
        mainContainer.add(createSidebarPanel(), BorderLayout.WEST);

        // 2. PANEL CENTRAL (HEADER + DASHBOARD)
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setOpaque(false);

        contentPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        contentPanel.add(createDashboardBody(), BorderLayout.CENTER);

        mainContainer.add(contentPanel, BorderLayout.CENTER);
        add(mainContainer);

        startLiveClock();
    }

    // --- BARRA LATERAL ---
    private JPanel createSidebarPanel() {
        RoundedPanel sidebar = new RoundedPanel(25, COLOR_SIDEBAR);
        sidebar.setLayout(new BorderLayout(0, 15));
        sidebar.setPreferredSize(new Dimension(320, 0));
        sidebar.setBorder(new EmptyBorder(15, 15, 20, 15));

        // Tarjeta contenedora blanca para el logo
        RoundedPanel logoCard = new RoundedPanel(20, Color.WHITE);
        logoCard.setPreferredSize(new Dimension(290, 140));
        logoCard.setLayout(new GridBagLayout());
        logoCard.add(createLogoLabel());
        sidebar.add(logoCard, BorderLayout.NORTH);

        // Menú de navegación con íconos vectoriales HD estilizados
        JPanel menuPanel = new JPanel(new GridLayout(7, 1, 0, 8));
        menuPanel.setOpaque(false);

        Object[][] items = {
            {SidebarVectorIcon.IconType.EMPLOYEES, "gui/images/employees.png", "<html>Gestión de<br>empleados (cajeros)</html>"},
            {SidebarVectorIcon.IconType.MENU, "gui/images/menu.png", "<html>Gestión de menú /<br>productos</html>"},
            {SidebarVectorIcon.IconType.ORDERS, "gui/images/orders.png", "Gestión de pedidos"},
            {SidebarVectorIcon.IconType.REPORTS, "gui/images/reports.png", "Reportes y estadísticas"},
            {SidebarVectorIcon.IconType.CASH, "gui/images/cash.png", "Gestión de caja"},
            {SidebarVectorIcon.IconType.SETTINGS, "gui/images/settings.png", "<html>Configuración<br>general</html>"},
            {SidebarVectorIcon.IconType.SECURITY, "gui/images/security.png", "Seguridad y auditoría"}
        };

        menuButtons = new JPanel[items.length];

        for (int i = 0; i < items.length; i++) {
            final int index = i;
            SidebarVectorIcon.IconType iconType = (SidebarVectorIcon.IconType) items[i][0];
            String iconPath = (String) items[i][1];
            String textHtml = (String) items[i][2];

            RoundedPanel btnPanel = new RoundedPanel(15, i == selectedMenuIndex ? COLOR_SIDEBAR_ACTIVE : COLOR_SIDEBAR);
            btnPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 8));

            JLabel iconLbl = createSidebarIconLabel(iconType, iconPath);

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
                        // Navegar a la ventana de Gestión de Empleados
                        gestionEmpleados app = new gestionEmpleados();
                        app.setVisible(true);
                        dispose();
                        return;
                    }
                    // Las demás ventanas todavía no existen
                    JOptionPane.showMessageDialog(
                            dashboardAdmin.this,
                            "aun no  JAJAJAJAJA.",
                            "........",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            });

            menuButtons[i] = btnPanel;
            menuPanel.add(btnPanel);
        }

        sidebar.add(menuPanel, BorderLayout.CENTER);
        return sidebar;
    }

    private JLabel createSidebarIconLabel(SidebarVectorIcon.IconType iconType, String resourcePath) {
        JLabel lbl = new JLabel();
        URL imgUrl = getClass().getResource("/" + resourcePath);
        if (imgUrl == null) {
            imgUrl = getClass().getResource("/" + resourcePath.replace("gui/", ""));
        }
        if (imgUrl != null) {
            ImageIcon icon = new ImageIcon(imgUrl);
            Image img = icon.getImage();
            lbl.setIcon(new ImageIcon(img.getScaledInstance(28, 28, Image.SCALE_SMOOTH)));
        } else {
            lbl.setIcon(new SidebarVectorIcon(iconType, 28));
        }
        return lbl;
    }

    private void updateSidebarSelection() {
        for (int i = 0; i < menuButtons.length; i++) {
            RoundedPanel btn = (RoundedPanel) menuButtons[i];
            if (i == selectedMenuIndex) {
                btn.setBackgroundColor(COLOR_SIDEBAR_ACTIVE);
            } else {
                btn.setBackgroundColor(COLOR_SIDEBAR);
            }
            btn.repaint();
        }
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
            lblLogo.setText("<html><center><font size='7' color='#6EA32E'><b>&lt; 🍴 &gt;</b></font><br>"
                    + "<font size='5'><b color='#6EA32E'>GIT & </b><b color='#D13941'>EAT!</b></font></center></html>");
        }
        return lblLogo;
    }

    // --- HEADER ---
    private JPanel createHeaderPanel() {
        RoundedPanel header = new RoundedPanel(20, COLOR_HEADER);
        header.setLayout(new BorderLayout());
        header.setPreferredSize(new Dimension(0, 125));
        header.setBorder(new EmptyBorder(20, 35, 20, 35));

        JLabel title = new JLabel("Dashboard Administrador");
        title.setFont(new Font("SansSerif", Font.BOLD, 40));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        // Panel Derecho: Hora en vivo + Controles
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        rightHeader.setOpaque(false);

        JPanel timePanel = new JPanel(new GridLayout(2, 1));
        timePanel.setOpaque(false);

        lblClock = new JLabel("--:--:--", SwingConstants.RIGHT);
        lblClock.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblClock.setForeground(Color.WHITE);

        lblDate = new JLabel("Cargando fecha...", SwingConstants.RIGHT);
        lblDate.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblDate.setForeground(new Color(230, 230, 230));

        timePanel.add(lblClock);
        timePanel.add(lblDate);
        rightHeader.add(timePanel);

        // Botón de actualización rápida
        JButton btnRefresh = new JButton("🔄");
        btnRefresh.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setContentAreaFilled(false);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.setToolTipText("Actualizar datos");
        btnRefresh.addActionListener(e -> repaint());
        rightHeader.add(btnRefresh);

        header.add(rightHeader, BorderLayout.EAST);

        return header;
    }

    private void startLiveClock() {
        Timer timer = new Timer(1000, e -> {
            Date now = new Date();
            SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm:ss a");
            SimpleDateFormat sdfDate = new SimpleDateFormat("EEEE, d 'de' MMMM", new Locale("es", "ES"));
            lblClock.setText(sdfTime.format(now));
            lblDate.setText(sdfDate.format(now));
        });
        timer.start();
    }

    // --- CUERPO DEL DASHBOARD ---
    private JPanel createDashboardBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(6, 6, 6, 6);

        // Fila 1: Empleados de hoy + Ventas Semanales
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0.55;
        gbc.weighty = 0.30;
        body.add(createEmpleadosCard(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.45;
        body.add(createVentasSemanalesCard(), gbc);

        // Fila 2: Pedidos Recientes
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.14;
        body.add(createPedidosRecientesCard(), gbc);

        // Fila 3: Tarjetas Inferiores
        JPanel bottomRow = new JPanel(new GridLayout(1, 3, 12, 0));
        bottomRow.setOpaque(false);
        bottomRow.add(createRendimientoCard());
        bottomRow.add(createTotalVentasCard());
        bottomRow.add(createPedidosAbiertosCard());

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weighty = 0.56;
        body.add(bottomRow, gbc);

        return body;
    }

    // --- CARDS ESPECÍFICAS ---
    private JPanel createEmpleadosCard() {
        RoundedPanel panel = createBaseCard("Empleados de hoy", null);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);
        listPanel.setBorder(new EmptyBorder(6, 15, 10, 15));

        List<Object[]> empleados = crud.listarEmpleadosPorTurno();

        if (empleados.isEmpty()) {
            JLabel lblVacio = new JLabel("No hay empleados registrados.");
            lblVacio.setFont(new Font("SansSerif", Font.PLAIN, 13));
            lblVacio.setForeground(Color.GRAY);
            listPanel.add(lblVacio);
        } else {
            for (int i = 0; i < empleados.size(); i++) {
                Object[] empleado = empleados.get(i);
                String nombreCompleto = (String) empleado[0];
                String rol = (String) empleado[1];
                String turno = (String) empleado[2];

                String etiqueta = nombreCompleto + " - " + rol;
                listPanel.add(createEmployeePill(etiqueta, turno, colorPorTurno(turno)));

                if (i < empleados.size() - 1) {
                    listPanel.add(Box.createVerticalStrut(8));
                }
            }
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

// Asigna un color según el turno del empleado (Mañana/Tarde/Noche)
    private Color colorPorTurno(String turno) {
        if (turno == null) {
            return COLOR_SIDEBAR;
        }
        switch (turno.trim().toLowerCase()) {
            case "mañana":
                return COLOR_GREEN;
            case "tarde":
                return COLOR_YELLOW;
            case "noche":
                return COLOR_RED;
            default:
                return COLOR_SIDEBAR;
        }
    }

    private JPanel createEmployeePill(String name, String shift, Color bg) {
        RoundedPanel pill = new RoundedPanel(12, bg);
        pill.setLayout(new BorderLayout());
        pill.setBorder(new EmptyBorder(6, 15, 6, 15));

        JLabel lblName = new JLabel(name);
        lblName.setForeground(Color.WHITE);
        lblName.setFont(new Font("SansSerif", Font.BOLD, 14));

        JLabel lblShift = new JLabel(shift);
        lblShift.setForeground(Color.WHITE);
        lblShift.setFont(new Font("SansSerif", Font.BOLD, 14));

        pill.add(lblName, BorderLayout.WEST);
        pill.add(lblShift, BorderLayout.EAST);
        return pill;
    }

    private JPanel createVentasSemanalesCard() {
        RoundedPanel panel = createBaseCard("Ventas Semanales", null);
        panel.add(new AreaChartPanel(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPedidosRecientesCard() {
        RoundedPanel panel = createBaseCard("Pedidos Recientes", null);

        String[] columns = {"Pedido", "Mesa", "Hora", "Total", "Estado"};
        Object[][] data = {
            {"#1035", "Mesa 5", "12:54", "Q45.80", "Preparando"},
            {"#1034", "Mesa 6", "12:56", "Q45.80", "Preparando"},
            {"#1030", "Mesa 8", "13:05", "Q45.80", "Preparando"}
        };

        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(32);
        table.setTableHeader(null);
        table.setShowGrid(true);
        table.setGridColor(COLOR_TABLE_GRID);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setFont(new Font("SansSerif", Font.BOLD, 15));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 0; i < 4; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        table.getColumnModel().getColumn(4).setCellRenderer((t, val, isS, hasF, row, col) -> {
            JPanel container = new JPanel(new GridBagLayout());
            container.setBackground(Color.WHITE);

            RoundedPanel pill = new RoundedPanel(18, COLOR_PREPARANDO);
            pill.setPreferredSize(new Dimension(185, 24));
            pill.setLayout(new GridBagLayout());

            JLabel lbl = new JLabel("Preparando");
            lbl.setForeground(Color.WHITE);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
            pill.add(lbl);

            container.add(pill);
            return container;
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(2, 15, 6, 15),
                BorderFactory.createLineBorder(COLOR_TABLE_GRID, 1)
        ));
        scroll.getViewport().setBackground(Color.WHITE);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createRendimientoCard() {
        RoundedPanel panel = createBaseCard("Rendimiento", "Ventas por mesero");
        panel.add(new BarChartPanel(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTotalVentasCard() {
        RoundedPanel panel = new RoundedPanel(20, COLOR_RED);
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 15, 0);

        JLabel title = new JLabel("Total de ventas hoy", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        panel.add(title, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        JLabel val = new JLabel("Q5,005.67", SwingConstants.CENTER);
        val.setForeground(Color.WHITE);
        val.setFont(new Font("SansSerif", Font.BOLD, 52));
        panel.add(val, gbc);

        return panel;
    }

    private JPanel createPedidosAbiertosCard() {
        RoundedPanel panel = new RoundedPanel(20, COLOR_YELLOW);
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 15, 0);

        JLabel title = new JLabel("Pedidos abiertos", SwingConstants.CENTER);
        title.setForeground(Color.BLACK);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        panel.add(title, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        JLabel val = new JLabel("49", SwingConstants.CENTER);
        val.setForeground(Color.BLACK);
        val.setFont(new Font("SansSerif", Font.BOLD, 64));
        panel.add(val, gbc);

        return panel;
    }

    private RoundedPanel createBaseCard(String titleText, String subtitleText) {
        RoundedPanel panel = new RoundedPanel(20, COLOR_CARD_BG);
        panel.setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(new EmptyBorder(10, 16, 2, 16));

        JLabel title = new JLabel(titleText);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(Color.BLACK);
        headerPanel.add(title);

        if (subtitleText != null && !subtitleText.isEmpty()) {
            JLabel subtitle = new JLabel(subtitleText);
            subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
            subtitle.setForeground(new Color(60, 60, 60));
            headerPanel.add(subtitle);
        }

        panel.add(headerPanel, BorderLayout.NORTH);
        return panel;
    }

    // --- GRÁFICAS DIBUJADAS A MEDIDA ---
    private static class AreaChartPanel extends JPanel {

        public AreaChartPanel() {
            setOpaque(false);
            setBorder(new EmptyBorder(5, 12, 8, 12));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int leftMargin = 28, bottomMargin = 18;
            int chartW = w - leftMargin - 10, chartH = h - bottomMargin - 10;

            // Cuadrícula y etiquetas Y
            g2.setFont(new Font("SansSerif", Font.BOLD, 9));
            String[] yLabels = {"200", "150", "100", "50", "0"};
            for (int i = 0; i < 5; i++) {
                int y = 10 + i * (chartH / 4);
                g2.setColor(new Color(240, 240, 240));
                g2.drawLine(leftMargin, y, w - 10, y);
                g2.setColor(Color.BLACK);
                g2.drawString(yLabels[i], 2, y + 3);
            }

            // Días X
            String[] days = {"Lun", "Mar", "Mie", "Jue", "Vie", "Sab", "Dom"};
            int stepX = chartW / (days.length - 1);
            for (int i = 0; i < days.length; i++) {
                int x = leftMargin + i * stepX;
                g2.drawString(days[i], x - 6, h - 2);
            }

            // Puntos de datos y área rellenada con degradado
            double[] values = {0.25, 0.74, 0.83, 0.28, 0.54, 0.48, 0.94};
            Path2D path = new Path2D.Double();

            int startX = leftMargin;
            int startY = 10 + (int) ((1.0 - values[0]) * chartH);
            path.moveTo(startX, startY);

            for (int i = 1; i < values.length; i++) {
                int x = leftMargin + i * stepX;
                int y = 10 + (int) ((1.0 - values[i]) * chartH);
                path.lineTo(x, y);
            }

            path.lineTo(leftMargin + (values.length - 1) * stepX, 10 + chartH);
            path.lineTo(startX, 10 + chartH);
            path.closePath();

            // Aplicar degradado verde dinámico
            GradientPaint gradient = new GradientPaint(
                    0, 10, COLOR_GREEN,
                    0, 10 + chartH, new Color(106, 161, 46, 100)
            );
            g2.setPaint(gradient);
            g2.fill(path);

            // Dibujar marcadores de punto de datos
            g2.setColor(COLOR_GREEN.darker());
            for (int i = 0; i < values.length; i++) {
                int x = leftMargin + i * stepX;
                int y = 10 + (int) ((1.0 - values[i]) * chartH);
                g2.fillOval(x - 3, y - 3, 6, 6);
            }
        }
    }

    private static class BarChartPanel extends JPanel {

        public BarChartPanel() {
            setOpaque(false);
            setBorder(new EmptyBorder(5, 12, 8, 12));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int leftMargin = 28, bottomMargin = 18;
            int chartW = w - leftMargin - 10, chartH = h - bottomMargin - 10;

            g2.setFont(new Font("SansSerif", Font.BOLD, 9));
            g2.setColor(Color.BLACK);
            String[] yLabels = {"100", "80", "60", "40", "20", "0"};
            for (int i = 0; i < yLabels.length; i++) {
                int y = 10 + i * (chartH / 5);
                g2.drawString(yLabels[i], 2, y + 3);
            }

            String[] names = {"Ana", "Luis", "Carlos"};
            double[] values = {0.79, 0.45, 0.84};

            int numBars = names.length;
            int gap = 16;
            int barWidth = (chartW - (numBars + 1) * gap) / numBars;

            for (int i = 0; i < numBars; i++) {
                int x = leftMargin + gap + i * (barWidth + gap);
                int barH = (int) (values[i] * chartH);
                int y = 10 + (chartH - barH);

                g2.setColor(COLOR_GREEN);
                g2.fillRoundRect(x, y, barWidth, barH, 10, 10);

                g2.setColor(Color.BLACK);
                FontMetrics fm = g2.getFontMetrics();
                int textW = fm.stringWidth(names[i]);
                g2.drawString(names[i], x + (barWidth - textW) / 2, h - 2);
            }
        }
    }

    // --- CLASE DE ICONOS VECTORIALES PARA LA BARRA LATERAL ---
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
                case EMPLOYEES: // Empleados (Icono de usuarios/avatars)
                    g2.fillOval(10, 3, 12, 12);
                    g2.fillArc(4, 16, 24, 18, 0, 180);
                    g2.setColor(new Color(255, 255, 255, 180));
                    g2.fillOval(20, 6, 8, 8);
                    g2.fillArc(17, 15, 14, 12, 0, 180);
                    break;

                case MENU: // Productos / Menú (Icono de Hamburguesa estilizada)
                    g2.fillArc(3, 5, 26, 14, 0, 180);
                    g2.fillRoundRect(2, 14, 28, 4, 2, 2);
                    g2.fillRoundRect(4, 20, 24, 6, 3, 3);
                    break;

                case ORDERS: // Pedidos (Icono de Portapapeles / Lista)
                    g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawRoundRect(5, 5, 22, 24, 4, 4);
                    g2.fillRoundRect(11, 3, 10, 4, 2, 2);
                    g2.drawLine(9, 12, 23, 12);
                    g2.drawLine(9, 17, 23, 17);
                    g2.drawLine(9, 22, 18, 22);
                    break;

                case REPORTS: // Reportes (Gráfico de Barras con Tendencia)
                    g2.fillRoundRect(4, 18, 6, 10, 2, 2);
                    g2.fillRoundRect(13, 12, 6, 16, 2, 2);
                    g2.fillRoundRect(22, 6, 6, 22, 2, 2);
                    break;

                case CASH: // Caja (Icono de Billete / Moneda con símbolo de Quetzal 'Q')
                    g2.setStroke(new BasicStroke(2.0f));
                    g2.drawRoundRect(3, 7, 26, 18, 4, 4);
                    g2.drawOval(11, 11, 10, 10);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString("Q", 16 - fm.stringWidth("Q") / 2, 19);
                    break;

                case SETTINGS: // Configuración (Engranaje)
                    g2.setStroke(new BasicStroke(2.5f));
                    g2.drawOval(10, 10, 12, 12);
                    for (int i = 0; i < 8; i++) {
                        double angle = Math.toRadians(i * 45);
                        int x1 = (int) (16 + 8 * Math.cos(angle));
                        int y1 = (int) (16 + 8 * Math.sin(angle));
                        int x2 = (int) (16 + 13 * Math.cos(angle));
                        int y2 = (int) (16 + 13 * Math.sin(angle));
                        g2.drawLine(x1, y1, x2, y2);
                    }
                    break;

                case SECURITY: // Seguridad (Escudo con Checkmark)
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
            }
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
            Graphics2D graphics = (Graphics2D) g;
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(backgroundColor);
            graphics.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            dashboardAdmin app = new dashboardAdmin();
            app.setVisible(true);
        });
    }
}
