package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.Path2D;
import java.net.URL;

public class dashboardAdmin extends JFrame {

    // Paleta de colores exacta extraída de la imagen
    private static final Color COLOR_BG = new Color(231, 221, 202);
    private static final Color COLOR_SIDEBAR = new Color(139, 94, 52);
    private static final Color COLOR_HEADER = new Color(139, 94, 52);
    private static final Color COLOR_CARD_BG = Color.WHITE;
    private static final Color COLOR_GREEN = new Color(106, 161, 46);
    private static final Color COLOR_YELLOW = new Color(243, 205, 59);
    private static final Color COLOR_RED = new Color(211, 53, 58);
    private static final Color COLOR_PREPARANDO = new Color(214, 201, 168);
    private static final Color COLOR_TABLE_GRID = new Color(222, 210, 191);

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

        // Menú de navegación con salto de línea en Configuración general
        JPanel menuPanel = new JPanel(new GridLayout(7, 1, 0, 8));
        menuPanel.setOpaque(false);

        Object[][] items = {
            {"👥", "<html>Gestión de<br>empleados (cajeros)</html>"},
            {"🍔", "<html>Gestión de menú /<br>productos</html>"},
            {"📋", "Gestión de pedidos"},
            {"📊", "Reportes y estadísticas"},
            {"💰", "Gestión de caja"},
            {"⚙️", "<html>Configuración<br>general</html>"}, // Salto de línea agregado
            {"🛡️", "Seguridad y auditoría"}
        };

        for (Object[] item : items) {
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
            btnPanel.setOpaque(false);

            JLabel iconLbl = new JLabel((String) item[0]);
            iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
            iconLbl.setForeground(Color.WHITE);

            JLabel textLbl = new JLabel((String) item[1]);
            textLbl.setFont(new Font("SansSerif", Font.BOLD, 18));
            textLbl.setForeground(Color.WHITE);

            btnPanel.add(iconLbl);
            btnPanel.add(textLbl);
            btnPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

            menuPanel.add(btnPanel);
        }

        sidebar.add(menuPanel, BorderLayout.CENTER);
        return sidebar;
    }

    private JLabel createLogoLabel() {
        JLabel lblLogo = new JLabel();
        URL logoUrl = getClass().getResource("/gui/images/logo.png");
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

    // --- HEADER ---
    private JPanel createHeaderPanel() {
        RoundedPanel header = new RoundedPanel(20, COLOR_HEADER);
        header.setLayout(new FlowLayout(FlowLayout.LEFT, 35, 30));
        header.setPreferredSize(new Dimension(0, 125));

        JLabel title = new JLabel("Dashboard Administrador");
        title.setFont(new Font("SansSerif", Font.BOLD, 42));
        title.setForeground(Color.WHITE);
        header.add(title);

        return header;
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
        gbc.gridx = 0; gbc.weightx = 0.55; gbc.weighty = 0.30;
        body.add(createEmpleadosCard(), gbc);

        gbc.gridx = 1; gbc.weightx = 0.45;
        body.add(createVentasSemanalesCard(), gbc);

        // Fila 2: Pedidos Recientes
        gbc.gridy = 1; gbc.gridx = 0;
        gbc.gridwidth = 2; gbc.weightx = 1.0; gbc.weighty = 0.14;
        body.add(createPedidosRecientesCard(), gbc);

        // Fila 3: Tarjetas Inferiores
        JPanel bottomRow = new JPanel(new GridLayout(1, 3, 12, 0));
        bottomRow.setOpaque(false);
        bottomRow.add(createRendimientoCard());
        bottomRow.add(createTotalVentasCard());
        bottomRow.add(createPedidosAbiertosCard());

        gbc.gridy = 2; gbc.gridx = 0;
        gbc.gridwidth = 2; gbc.weighty = 0.56;
        body.add(bottomRow, gbc);

        return body;
    }

    // --- CARDS ESPECÍFICAS ---

    private JPanel createEmpleadosCard() {
        RoundedPanel panel = createBaseCard("Empleados de hoy", null);

        JPanel listPanel = new JPanel(new GridLayout(3, 1, 0, 8));
        listPanel.setOpaque(false);
        listPanel.setBorder(new EmptyBorder(6, 15, 10, 15));

        listPanel.add(createEmployeePill("Karla Patal - Cajera", "Mañana", COLOR_GREEN));
        listPanel.add(createEmployeePill("Javier Top - Cajero", "Tarde", COLOR_YELLOW));
        listPanel.add(createEmployeePill("Luis Muñoz - Cajero", "Noche", COLOR_RED));

        panel.add(listPanel, BorderLayout.CENTER);
        return panel;
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
            {"#1035", "Mesa 5", "12:54", "Q45.8", "Preparando"},
            {"#1034", "Mesa 6", "12:56", "Q45.8", "Preparando"},
            {"#1030", "Mesa 8", "13:05", "Q45.8", "Preparando"}
        };

        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(model);
        table.setRowHeight(30); // Incrementado para acomodar texto más grande
        table.setTableHeader(null);
        table.setShowGrid(true);
        table.setGridColor(COLOR_TABLE_GRID);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setFont(new Font("SansSerif", Font.BOLD, 15)); // Texto de la tabla ampliado

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
            lbl.setFont(new Font("SansSerif", Font.BOLD, 13)); // Texto del botón ampliado
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
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 15, 0);

        JLabel title = new JLabel("Total de ventas hoy", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        panel.add(title, gbc);

        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 0, 0);
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
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 15, 0);

        JLabel title = new JLabel("Pedidos abiertos", SwingConstants.CENTER);
        title.setForeground(Color.BLACK);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        panel.add(title, gbc);

        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 0, 0);
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

            g2.setFont(new Font("SansSerif", Font.BOLD, 9));
            String[] yLabels = {"200", "150", "100", "50", "0"};
            for (int i = 0; i < 5; i++) {
                int y = 10 + i * (chartH / 4);
                g2.setColor(new Color(245, 245, 245));
                g2.drawLine(leftMargin, y, w - 10, y);
                g2.setColor(Color.BLACK);
                g2.drawString(yLabels[i], 2, y + 3);
            }

            String[] days = {"Lun", "Mar", "Mie", "Jue", "Vie", "Sab", "Dom"};
            int stepX = chartW / (days.length - 1);
            for (int i = 0; i < days.length; i++) {
                int x = leftMargin + i * stepX;
                g2.drawString(days[i], x - 6, h - 2);
            }

            double[] values = {0.25, 0.74, 0.83, 0.28, 0.54, 0.48, 0.94};
            Path2D path = new Path2D.Double();

            int startX = leftMargin;
            int startY = 10 + (int)((1.0 - values[0]) * chartH);
            path.moveTo(startX, startY);

            for (int i = 1; i < values.length; i++) {
                int x = leftMargin + i * stepX;
                int y = 10 + (int)((1.0 - values[i]) * chartH);
                path.lineTo(x, y);
            }

            path.lineTo(leftMargin + (values.length - 1) * stepX, 10 + chartH);
            path.lineTo(startX, 10 + chartH);
            path.closePath();

            g2.setColor(COLOR_GREEN);
            g2.fill(path);
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
                int barH = (int)(values[i] * chartH);
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
            Graphics2D graphics = (Graphics2D) g;
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(backgroundColor);
            graphics.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            dashboardAdmin app = new dashboardAdmin();
            app.setVisible(true);
        });
    }
}