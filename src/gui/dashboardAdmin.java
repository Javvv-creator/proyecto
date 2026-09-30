package gui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Path2D;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    private static final Color COLOR_CERRAR_SESION = new Color(211, 53, 58);

    private JLabel lblClock;
    private JLabel lblDate;
    private int selectedMenuIndex = 0;
    private JPanel[] menuButtons;
    private AreaChartPanel weeklySalesChart;
    private BarChartPanel cashierPerformanceChart;
    private DefaultTableModel recentOrdersModel;
    private JLabel totalSalesValue;
    private JLabel openOrdersValue;

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
        refreshDashboardData();
        Timer refreshTimer = new Timer(60_000, e -> refreshDashboardData());
        refreshTimer.start();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                refreshDashboardData();
            }
        });
    }

    // ==========================================
    // --- BARRA LATERAL (idéntica al resto + botón Cerrar Sesión) ---
    // ==========================================
    private JPanel createSidebarPanel() {
        RoundedPanel sidebar = new RoundedPanel(25, COLOR_SIDEBAR);
        sidebar.setLayout(new BorderLayout(0, 15));
        sidebar.setPreferredSize(new Dimension(320, 0));
        sidebar.setBorder(new EmptyBorder(15, 15, 20, 15));

        // Tarjeta contenedora blanca para el logo (clic = volver al Dashboard)
        RoundedPanel logoCard = new RoundedPanel(20, Color.WHITE);
        logoCard.setPreferredSize(new Dimension(290, 140));
        logoCard.setLayout(new GridBagLayout());
        logoCard.add(createLogoLabel());
        logoCard.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoCard.setToolTipText("Volver al Dashboard");
        logoCard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dashboardAdmin app = new dashboardAdmin();
                app.setVisible(true);
                dispose();
            }
        });
        sidebar.add(logoCard, BorderLayout.NORTH);

        // Panel central: menú de navegación + botón de Cerrar Sesión debajo
        JPanel centerWrapper = new JPanel(new BorderLayout(0, 15));
        centerWrapper.setOpaque(false);

        JPanel menuPanel = new JPanel(new GridLayout(7, 1, 0, 8));
        menuPanel.setOpaque(false);

        Object[][] items = {
                { SidebarVectorIcon.IconType.EMPLOYEES, "gui/images/employees.png",
                        "<html>Gestión de<br>empleados (cajeros)</html>" },
                { SidebarVectorIcon.IconType.MENU, "gui/images/menu.png",
                        "<html>Gestión de menú /<br>productos</html>" },
                { SidebarVectorIcon.IconType.ORDERS, "gui/images/orders.png", "Gestión de pedidos" },
                { SidebarVectorIcon.IconType.REPORTS, "gui/images/reports.png", "Reportes y estadísticas" },
                { SidebarVectorIcon.IconType.CASH, "gui/images/cash.png", "Gestión de caja" },
                { SidebarVectorIcon.IconType.SECURITY, "gui/images/security.png", "Seguridad y auditoría" }
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
                    } 
                    else if (index == 2) {
                        gestionPedidos app = new gestionPedidos(); 
                        app.setVisible(true);
                        dispose();
                        return;
                    }
                    else if (index == 3) {
                        pantallaEstadistica app = new pantallaEstadistica();
                        app.setVisible(true);
                        dispose();
                        return;
                    } else if (index == 4) {
                        gestionCaja app = new gestionCaja();
                        app.setVisible(true);
                        dispose();
                        return;
                    } else if (index == 5) {
                        seguridadAuditoria app = new seguridadAuditoria();
                        app.setVisible(true);
                        dispose();
                        return;
                    }
                    // Las demás ventanas todavía no existen
                    JOptionPane.showMessageDialog(
                            dashboardAdmin.this,
                            "Esta sección todavía está en desarrollo.",
                            "Próximamente",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            });

            menuButtons[i] = btnPanel;
            menuPanel.add(btnPanel);
        }

        centerWrapper.add(menuPanel, BorderLayout.NORTH);

        // Botón "Cerrar Sesión" (rojo) al final de la barra lateral
        RoundedPanel btnCerrarSesion = new RoundedPanel(15, COLOR_CERRAR_SESION);
        btnCerrarSesion.setLayout(new GridBagLayout());
        btnCerrarSesion.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrarSesion.setPreferredSize(new Dimension(0, 55));

        JLabel lblCerrarSesion = new JLabel("Cerrar Sesión");
        lblCerrarSesion.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblCerrarSesion.setForeground(Color.WHITE);
        btnCerrarSesion.add(lblCerrarSesion);

        btnCerrarSesion.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int confirmacion = JOptionPane.showConfirmDialog(
                        SwingUtilities.getWindowAncestor(btnCerrarSesion),
                        "¿Desea cerrar la sesión actual?",
                        "Confirmar Cierre de Sesión",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                if (confirmacion == JOptionPane.YES_OPTION) {
                    // Abrir la ventana de Login
                    SwingUtilities.invokeLater(() -> {
                        new pantallaLogin().setVisible(true);
                    });

                    // Cerrar todas las ventanas abiertas
                    for (Window window : Window.getWindows()) {
                        window.dispose();
                    }
                }
            }
        });

        JPanel logoutWrapper = new JPanel(new BorderLayout());
        logoutWrapper.setOpaque(false);
        logoutWrapper.add(btnCerrarSesion, BorderLayout.SOUTH);

        sidebar.add(centerWrapper, BorderLayout.CENTER);
        sidebar.add(logoutWrapper, BorderLayout.SOUTH);

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

    // dejenlo aqui
    /*
     * private void updateSidebarSelection() {
     * for (int i = 0; i < menuButtons.length; i++) {
     * RoundedPanel btn = (RoundedPanel) menuButtons[i];
     * if (i == selectedMenuIndex) {
     * btn.setBackgroundColor(COLOR_SIDEBAR_ACTIVE);
     * } else {
     * btn.setBackgroundColor(COLOR_SIDEBAR);
     * }
     * btn.repaint();
     * }
     * }
     */

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
            SimpleDateFormat sdfDate = new SimpleDateFormat("EEEE, d 'de' MMMM", Locale.of("es", "ES"));
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
        weeklySalesChart = new AreaChartPanel();
        panel.add(weeklySalesChart, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPedidosRecientesCard() {
        RoundedPanel panel = createBaseCard("Pedidos Recientes", null);

        String[] columns = { "Pedido", "Cajero", "Fecha / hora", "Total", "Estado" };

        recentOrdersModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(recentOrdersModel);
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
            String estado = val == null ? "" : val.toString();
            JPanel container = new JPanel(new GridBagLayout());
            container.setBackground(Color.WHITE);

            Color color = colorPorEstado(estado);
            RoundedPanel pill = new RoundedPanel(18, color);
            pill.setPreferredSize(new Dimension(185, 24));
            pill.setLayout(new GridBagLayout());

            JLabel lbl = new JLabel(estado);
            lbl.setForeground(color.equals(COLOR_YELLOW) ? Color.BLACK : Color.WHITE);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
            pill.add(lbl);

            container.add(pill);
            return container;
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(2, 15, 6, 15),
                BorderFactory.createLineBorder(COLOR_TABLE_GRID, 1)));
        scroll.getViewport().setBackground(Color.WHITE);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createRendimientoCard() {
        RoundedPanel panel = createBaseCard("Rendimiento", "Ventas por mesero");
        cashierPerformanceChart = new BarChartPanel();
        panel.add(cashierPerformanceChart, BorderLayout.CENTER);
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
        totalSalesValue = new JLabel("Q 0.00", SwingConstants.CENTER);
        totalSalesValue.setForeground(Color.WHITE);
        totalSalesValue.setFont(new Font("SansSerif", Font.BOLD, 52));
        panel.add(totalSalesValue, gbc);

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
        openOrdersValue = new JLabel("0", SwingConstants.CENTER);
        openOrdersValue.setForeground(Color.BLACK);
        openOrdersValue.setFont(new Font("SansSerif", Font.BOLD, 64));
        panel.add(openOrdersValue, gbc);

        return panel;
    }

    private Color colorPorEstado(String estado) {
        String normalized = estado.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("cancel")) return COLOR_RED;
        if (normalized.contains("entreg") || normalized.contains("complet")) return COLOR_GREEN;
        if (normalized.contains("listo")) return COLOR_YELLOW;
        return COLOR_PREPARANDO;
    }

    // Consulta los datos del dashboard y actualiza las gráficas, la tabla y los indicadores.
    private void refreshDashboardData() {
        try (Connection conn = new main.Conexion.Conexion().getConnection()) {
            if (conn == null) {
                System.err.println("No se pudo conectar para actualizar el dashboard.");
                return;
            }
            LocalDate today = getDatabaseDate(conn);
            cargarVentasSemanales(conn, today);
            cargarRendimientoCajeros(conn, today);
            cargarPedidosRecientes(conn);
            cargarIndicadores(conn);
        } catch (SQLException e) {
            System.err.println("Error actualizando dashboard: " + e.getMessage());
        }
    }

    // Usa la fecha del servidor MySQL para mantener coherentes los rangos de consulta.
    private LocalDate getDatabaseDate(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT CURDATE()");
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getDate(1).toLocalDate();
        }
    }

    // Llena la gráfica con los ingresos pagados de hoy y los seis días anteriores.
    private void cargarVentasSemanales(Connection conn, LocalDate today) throws SQLException {
        LocalDate firstDay = today.minusDays(6);
        Map<LocalDate, Double> salesByDay = new HashMap<>();
        String sql = "SELECT o.fecha, SUM(po.monto) AS ventas "
                + "FROM orden o JOIN pago_orden po ON po.id_orden = o.id_orden "
                + "WHERE o.fecha BETWEEN ? AND ? GROUP BY o.fecha ORDER BY o.fecha";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(firstDay));
            ps.setDate(2, java.sql.Date.valueOf(today));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    salesByDay.put(rs.getDate("fecha").toLocalDate(), rs.getDouble("ventas"));
                }
            }
        }

        DateTimeFormatter dayFormat = DateTimeFormatter.ofPattern("EEE", Locale.forLanguageTag("es-GT"));
        String[] labels = new String[7];
        double[] values = new double[7];
        for (int i = 0; i < 7; i++) {
            LocalDate day = firstDay.plusDays(i);
            String label = day.format(dayFormat);
            labels[i] = Character.toUpperCase(label.charAt(0)) + label.substring(1);
            values[i] = salesByDay.getOrDefault(day, 0.0);
        }
        weeklySalesChart.setData(labels, values);
    }

    // Compara las ventas del mes actual por cajero y entrega los cinco primeros al gráfico.
    private void cargarRendimientoCajeros(Connection conn, LocalDate today) throws SQLException {
        LocalDate firstDay = today.withDayOfMonth(1);
        String sql = "SELECT CONCAT(u.nombre, ' ', u.apellido) AS cajero, COALESCE(SUM(po.monto), 0) AS ventas "
                + "FROM usuario u "
                + "LEFT JOIN orden o ON o.id_usuario = u.id_usuario AND o.fecha BETWEEN ? AND ? "
                + "LEFT JOIN pago_orden po ON po.id_orden = o.id_orden "
                + "WHERE u.rol = 'CAJERO' AND u.estado = 1 "
                + "GROUP BY u.id_usuario, u.nombre, u.apellido ORDER BY ventas DESC, cajero LIMIT 5";
        List<String> names = new java.util.ArrayList<>();
        List<Double> values = new java.util.ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(firstDay));
            ps.setDate(2, java.sql.Date.valueOf(today));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    names.add(rs.getString("cajero"));
                    values.add(rs.getDouble("ventas"));
                }
            }
        }
        cashierPerformanceChart.setData(names.toArray(new String[0]),
                values.stream().mapToDouble(Double::doubleValue).toArray());
    }

    // Reemplaza la tabla de ejemplo con los cinco pedidos más recientes de la base de datos.
    private void cargarPedidosRecientes(Connection conn) throws SQLException {
        String sql = "SELECT o.id_orden, COALESCE(CONCAT(u.nombre, ' ', u.apellido), 'Sin asignar') AS cajero, "
                + "DATE_FORMAT(TIMESTAMP(o.fecha, o.hora), '%d/%m %H:%i') AS fecha_hora, o.total, o.estado "
                + "FROM orden o LEFT JOIN usuario u ON u.id_usuario = o.id_usuario "
                + "ORDER BY o.fecha DESC, o.hora DESC, o.id_orden DESC LIMIT 5";
        recentOrdersModel.setRowCount(0);
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                recentOrdersModel.addRow(new Object[]{
                    "#" + rs.getInt("id_orden"),
                    rs.getString("cajero"),
                    rs.getString("fecha_hora"),
                    String.format(Locale.US, "Q %,.2f", rs.getDouble("total")),
                    rs.getString("estado")
                });
            }
        }
    }

    // Actualiza el total cobrado hoy y cuenta pedidos que todavía no están cerrados.
    private void cargarIndicadores(Connection conn) throws SQLException {
        String sqlVentas = "SELECT COALESCE(SUM(po.monto), 0) FROM pago_orden po "
                + "JOIN orden o ON o.id_orden = po.id_orden WHERE o.fecha = CURDATE()";
        try (PreparedStatement ps = conn.prepareStatement(sqlVentas); ResultSet rs = ps.executeQuery()) {
            rs.next();
            totalSalesValue.setText(String.format(Locale.US, "Q %,.2f", rs.getDouble(1)));
        }

        String sqlAbiertos = "SELECT COUNT(*) FROM orden WHERE UPPER(TRIM(estado)) NOT IN "
                + "('COMPLETADA', 'COMPLETADO', 'ENTREGADO', 'CANCELADA', 'CANCELADO')";
        try (PreparedStatement ps = conn.prepareStatement(sqlAbiertos); ResultSet rs = ps.executeQuery()) {
            rs.next();
            openOrdersValue.setText(Integer.toString(rs.getInt(1)));
        }
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

        private String[] labels = new String[0];
        private double[] values = new double[0];

        public AreaChartPanel() {
            setOpaque(false);
            setBorder(new EmptyBorder(5, 12, 8, 12));
        }

        void setData(String[] labels, double[] values) {
            this.labels = labels;
            this.values = values;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int leftMargin = 42, bottomMargin = 18;
            int chartW = w - leftMargin - 10, chartH = h - bottomMargin - 10;
            if (chartW <= 0 || chartH <= 0 || labels.length == 0) return;

            double maxValue = 0;
            for (double value : values) maxValue = Math.max(maxValue, value);
            if (maxValue <= 0) maxValue = 1;

            g2.setFont(new Font("SansSerif", Font.BOLD, 9));
            for (int i = 0; i < 5; i++) {
                int y = 10 + i * (chartH / 4);
                g2.setColor(new Color(240, 240, 240));
                g2.drawLine(leftMargin, y, w - 10, y);
                g2.setColor(Color.BLACK);
                g2.drawString(String.format(Locale.US, "Q%.0f", maxValue * (4 - i) / 4), 2, y + 3);
            }

            int stepX = chartW / Math.max(1, labels.length - 1);
            for (int i = 0; i < labels.length; i++) {
                int x = leftMargin + i * stepX;
                g2.drawString(labels[i], x - 8, h - 2);
            }

            Path2D path = new Path2D.Double();
            int startX = leftMargin;
            int startY = 10 + (int) ((1.0 - values[0] / maxValue) * chartH);
            path.moveTo(startX, startY);

            for (int i = 1; i < values.length; i++) {
                int x = leftMargin + i * stepX;
                int y = 10 + (int) ((1.0 - values[i] / maxValue) * chartH);
                path.lineTo(x, y);
            }

            path.lineTo(leftMargin + (values.length - 1) * stepX, 10 + chartH);
            path.lineTo(startX, 10 + chartH);
            path.closePath();

            // Aplicar degradado verde dinámico
            GradientPaint gradient = new GradientPaint(
                    0, 10, COLOR_GREEN,
                    0, 10 + chartH, new Color(106, 161, 46, 100));
            g2.setPaint(gradient);
            g2.fill(path);

            // Dibujar marcadores de punto de datos
            g2.setColor(COLOR_GREEN.darker());
            for (int i = 0; i < values.length; i++) {
                int x = leftMargin + i * stepX;
                int y = 10 + (int) ((1.0 - values[i] / maxValue) * chartH);
                g2.fillOval(x - 3, y - 3, 6, 6);
            }
        }
    }

    private static class BarChartPanel extends JPanel {

        private String[] names = new String[0];
        private double[] values = new double[0];

        public BarChartPanel() {
            setOpaque(false);
            setBorder(new EmptyBorder(5, 12, 8, 12));
        }

        void setData(String[] names, double[] values) {
            this.names = names;
            this.values = values;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int leftMargin = 42, bottomMargin = 18;
            int chartW = w - leftMargin - 10, chartH = h - bottomMargin - 10;
            if (chartW <= 0 || chartH <= 0) return;

            g2.setFont(new Font("SansSerif", Font.BOLD, 9));
            g2.setColor(Color.BLACK);
            double maxValue = 0;
            for (double value : values) maxValue = Math.max(maxValue, value);
            if (maxValue <= 0) maxValue = 1;
            for (int i = 0; i <= 5; i++) {
                int y = 10 + i * (chartH / 5);
                g2.drawString(String.format(Locale.US, "Q%.0f", maxValue * (5 - i) / 5), 2, y + 3);
            }

            int numBars = Math.min(names.length, values.length);
            if (numBars == 0) {
                g2.drawString("Sin datos", leftMargin + 8, 28);
                return;
            }
            int gap = 12;
            int barWidth = Math.max(1, (chartW - (numBars + 1) * gap) / numBars);

            for (int i = 0; i < numBars; i++) {
                int x = leftMargin + gap + i * (barWidth + gap);
                int barH = (int) (values[i] / maxValue * chartH);
                int y = 10 + (chartH - barH);

                g2.setColor(COLOR_GREEN);
                g2.fillRoundRect(x, y, barWidth, barH, 10, 10);

                g2.setColor(Color.BLACK);
                FontMetrics fm = g2.getFontMetrics();
                String label = names[i];
                while (label.length() > 1 && fm.stringWidth(label) > barWidth) {
                    label = label.substring(0, label.length() - 1);
                }
                if (!label.equals(names[i])) label = label.substring(0, Math.max(1, label.length() - 1)) + ".";
                int textW = fm.stringWidth(label);
                g2.drawString(label, x + (barWidth - textW) / 2, h - 2);
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
