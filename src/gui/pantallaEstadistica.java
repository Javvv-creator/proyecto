package gui;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Git & Eat! - Pantalla "Reportes y estadísticas".
 * Misma estructura y estilo que gestionEmpleados / gestionProductos
 * (sidebar, header con reloj, cuerpo con tarjetas redondeadas).
 */
public class pantallaEstadistica extends JFrame {

    // Paleta de colores (idéntica a gestionEmpleados)
    private static final Color COLOR_BG = new Color(231, 221, 202);
    private static final Color COLOR_SIDEBAR = new Color(139, 94, 52);
    private static final Color COLOR_HEADER = new Color(139, 94, 52);
    private static final Color COLOR_SEARCH = new Color(106, 161, 46);
    private static final Color COLOR_COMBO = new Color(230, 115, 45);
    private static final Color COLOR_BTN_NUEVO = new Color(243, 205, 59);
    private static final Color COLOR_TABLE_HEADER = new Color(92, 53, 22);
    private static final Color COLOR_TABLE_GRID = new Color(222, 210, 191);
    private static final Color COLOR_TEXT_RED = new Color(211, 53, 58);
    private static final Color COLOR_SIDEBAR_HOVER = new Color(160, 110, 65);
    private static final Color COLOR_SIDEBAR_ACTIVE = new Color(110, 72, 38);
    private static final Color COLOR_CERRAR_SESION = new Color(211, 53, 58);

    // Colores propios de esta pantalla
    private static final Color COLOR_FILTERS_BG = new Color(242, 227, 211);
    private static final Color COLOR_TAB = new Color(227, 205, 176);

    // Índice de "Reportes y estadísticas" en la barra lateral
    private static final int INDICE_ACTUAL = 3;

    // Pestañas de reporte y columnas que muestra cada una
    private static final String[] TAB_TITLES = {
            "Ventas por fecha", "Ventas por cajero", "Productos más vendidos", "Método de pago"
    };
    private static final String[][] TAB_COLUMNS = {
            { "Fecha", "Transacciones", "Total vendido" },
            { "Cajero", "Transacciones", "Total vendido" },
            { "Producto", "Unidades vendidas", "Total vendido" },
            { "Método de pago", "Transacciones", "Total pagado" }
    };

    private int selectedMenuIndex = INDICE_ACTUAL;
    private int selectedTab = 3;
    private JPanel[] menuButtons;
    private JLabel lblClock;
    private JLabel lblDate;
    private Timer clockTimer;

    // Componentes de la pantalla
    private PillButton[] tabs;
    private JComboBox<String> cmbCategoria;
    private JComboBox<String> cmbCajero;
    private DefaultTableModel model;
    private BarChartPanel barChart;
    private PieChartPanel turnoChart;
    private PieChartPanel comboChart;

    public pantallaEstadistica() {
        setTitle("GIT & EAT! - Reportes y estadísticas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1280, 720));

        JPanel mainContainer = new JPanel(new BorderLayout(15, 15));
        mainContainer.setBackground(COLOR_BG);
        mainContainer.setBorder(new EmptyBorder(15, 15, 15, 15));

        // 1. BARRA LATERAL
        mainContainer.add(createSidebarPanel(), BorderLayout.WEST);

        // 2. PANEL CENTRAL (HEADER + CONTENIDO)
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setOpaque(false);
        contentPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        contentPanel.add(createMainBody(), BorderLayout.CENTER);

        mainContainer.add(contentPanel, BorderLayout.CENTER);
        add(mainContainer);

        // Al presionar ESC, regresa al Dashboard del Administrador
        getRootPane().registerKeyboardAction(
                e -> irADashboard(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        selectTab(selectedTab);
        startLiveClock();
    }

    /** Se mantiene por compatibilidad con código que ya la llamaba. */
    public void mostrarPantallaCompleta() {
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    // =====================================================================
    // NAVEGACIÓN
    // =====================================================================

    private void navegarA(int indice) {
        switch (indice) {
            case INDICE_ACTUAL:
                return; // ya estamos en esta pantalla
            case 0:
                new gestionEmpleados().setVisible(true);
                dispose();
                return;
            case 1:
                new gestionProductos().setVisible(true);
                dispose();
                return;
            default:
                JOptionPane.showMessageDialog(this, "Sección en desarrollo.", "Información",
                        JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void irADashboard() {
        dashboardAdmin app = new dashboardAdmin();
        app.setVisible(true);
        dispose();
    }

    /** Pide confirmación, cierra todas las ventanas abiertas y vuelve al login. */
    private void cerrarSesion() {
        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Deseas cerrar sesión?", "Cerrar Sesión",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }

        // 1. Detener el reloj de esta pantalla
        if (clockTimer != null) {
            clockTimer.stop();
        }

        // 2. Limpiar los datos de la sesión actual (usuario logueado, etc.)
        // TODO: por ejemplo Sesion.cerrar(); si tienes una clase que guarde el usuario actual

        // 3. Cerrar todas las ventanas abiertas de la aplicación
        for (Window w : Window.getWindows()) {
            w.dispose();
        }

        // 4. Abrir la ventana de login
        // TODO: reemplaza "login" por el nombre real de tu clase de inicio de sesión
        // new login().setVisible(true);
    }

    // =====================================================================
    // BARRA LATERAL
    // =====================================================================

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
        logoCard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                irADashboard();
            }
        });
        sidebar.add(logoCard, BorderLayout.NORTH);

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
            btnPanel.setPreferredSize(new Dimension(0, 60));

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
                    navegarA(index);
                }
            });

            menuButtons[i] = btnPanel;
            menuPanel.add(btnPanel);
        }

        // El menú queda pegado arriba con su altura natural (no ocupa toda la barra)
        JPanel menuWrapper = new JPanel(new BorderLayout());
        menuWrapper.setOpaque(false);
        menuWrapper.add(menuPanel, BorderLayout.NORTH);
        sidebar.add(menuWrapper, BorderLayout.CENTER);

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
        sidebar.add(btnCerrarSesion, BorderLayout.SOUTH);

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

    // =====================================================================
    // HEADER
    // =====================================================================

    private JPanel createHeaderPanel() {
        RoundedPanel header = new RoundedPanel(20, COLOR_HEADER);
        header.setLayout(new BorderLayout());
        header.setPreferredSize(new Dimension(0, 125));
        header.setBorder(new EmptyBorder(20, 35, 20, 35));

        // Título con icono de barras
        JPanel titlePanel = new JPanel(new GridBagLayout());
        titlePanel.setOpaque(false);
        GridBagConstraints tgbc = new GridBagConstraints();
        tgbc.gridy = 0;
        tgbc.insets = new Insets(0, 0, 0, 18);

        JLabel titleIcon = new JLabel(new SidebarVectorIcon(SidebarVectorIcon.IconType.REPORTS, 44));
        titlePanel.add(titleIcon, tgbc);

        JLabel title = new JLabel("Reportes y estadísticas");
        title.setFont(new Font("SansSerif", Font.BOLD, 40));
        title.setForeground(Color.WHITE);
        tgbc.insets = new Insets(0, 0, 0, 0);
        titlePanel.add(title, tgbc);

        header.add(titlePanel, BorderLayout.WEST);

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

        JButton btnRefresh = new JButton("🔄");
        btnRefresh.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setContentAreaFilled(false);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.setToolTipText("Actualizar datos");
        btnRefresh.addActionListener(e -> generarReporte());
        rightHeader.add(btnRefresh);

        header.add(rightHeader, BorderLayout.EAST);
        return header;
    }

    private void startLiveClock() {
        clockTimer = new Timer(1000, e -> {
            Date now = new Date();
            SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm:ss a");
            SimpleDateFormat sdfDate = new SimpleDateFormat("EEEE, d 'de' MMMM", Locale.of("es", "ES"));
            lblClock.setText(sdfTime.format(now));
            lblDate.setText(sdfDate.format(now));
        });
        clockTimer.setInitialDelay(0);
        clockTimer.start();
    }

    // =====================================================================
    // CUERPO
    // =====================================================================

    private JPanel createMainBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(8, 6, 8, 6);
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        // Fila 1: gráficas
        gbc.gridy = 0;
        gbc.weighty = 0.35;
        body.add(createChartsRow(), gbc);

        // Fila 2: pestañas y filtros (altura fija)
        gbc.gridy = 1;
        gbc.weighty = 0.0;
        body.add(createFiltersPanel(), gbc);

        // Fila 3: tabla de resultados
        gbc.gridy = 2;
        gbc.weighty = 0.65;
        body.add(createTableCard(), gbc);

        return body;
    }

    private JPanel createChartsRow() {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        // Altura base: sin esto el layout la aplasta porque la tabla pide más espacio
        row.setPreferredSize(new Dimension(0, 200));

        barChart = new BarChartPanel("Ventas por hora del día");
        turnoChart = new PieChartPanel("Turno: mañana / tarde",
                COLOR_BTN_NUEVO, "Mañana", COLOR_SEARCH, "Tarde", 40);
        comboChart = new PieChartPanel("Combos / individuales",
                COLOR_COMBO, "Combos", COLOR_TEXT_RED, "Individuales", 39);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        gbc.gridx = 0;
        gbc.weightx = 0.40;
        gbc.insets = new Insets(0, 0, 0, 15);
        row.add(barChart, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.30;
        row.add(turnoChart, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.30;
        gbc.insets = new Insets(0, 0, 0, 0);
        row.add(comboChart, gbc);

        return row;
    }

    private JPanel createFiltersPanel() {
        RoundedPanel panel = new RoundedPanel(20, COLOR_FILTERS_BG);
        panel.setLayout(new GridLayout(2, 1, 0, 10));
        panel.setBorder(new EmptyBorder(12, 20, 12, 20));
        panel.setPreferredSize(new Dimension(0, 135));

        // Fila de pestañas
        JPanel tabsRow = new JPanel(new GridLayout(1, TAB_TITLES.length, 12, 0));
        tabsRow.setOpaque(false);
        tabs = new PillButton[TAB_TITLES.length];
        for (int i = 0; i < TAB_TITLES.length; i++) {
            final int idx = i;
            tabs[i] = new PillButton(TAB_TITLES[i], 15, COLOR_TAB, COLOR_TABLE_HEADER, 16);
            tabs[i].onClick(() -> selectTab(idx));
            tabsRow.add(tabs[i]);
        }
        panel.add(tabsRow);

        // Fila de filtros + botón Generar
        JPanel controls = new JPanel(new GridBagLayout());
        controls.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        cmbCategoria = new JComboBox<>(new String[] { "Todas las categorías" });
        cmbCajero = new JComboBox<>(new String[] { "Todos" });

        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.insets = new Insets(0, 0, 0, 10);
        controls.add(createFilterLabel("Categoría:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.35;
        gbc.insets = new Insets(0, 0, 0, 30);
        controls.add(wrapCombo(cmbCategoria), gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.insets = new Insets(0, 0, 0, 10);
        controls.add(createFilterLabel("Cajero:"), gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.35;
        gbc.insets = new Insets(0, 0, 0, 30);
        controls.add(wrapCombo(cmbCajero), gbc);

        PillButton btnGenerar = new PillButton("Generar", 20, COLOR_BTN_NUEVO, Color.WHITE, 18);
        btnGenerar.setPreferredSize(new Dimension(170, 0));
        btnGenerar.onClick(this::generarReporte);
        gbc.gridx = 4;
        gbc.weightx = 0.15;
        gbc.insets = new Insets(0, 0, 0, 0);
        controls.add(btnGenerar, gbc);

        panel.add(controls);
        return panel;
    }

    private JLabel createFilterLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 17));
        lbl.setForeground(COLOR_TABLE_HEADER);
        return lbl;
    }

    private JPanel wrapCombo(JComboBox<String> combo) {
        RoundedPanel pill = new RoundedPanel(20, COLOR_COMBO);
        pill.setLayout(new BorderLayout());
        pill.setBorder(new EmptyBorder(5, 15, 5, 15));

        combo.setOpaque(false);
        combo.setBackground(COLOR_COMBO);
        combo.setForeground(Color.BLACK);
        combo.setFont(new Font("SansSerif", Font.BOLD, 16));
        combo.setBorder(BorderFactory.createEmptyBorder());
        combo.setFocusable(false);

        pill.add(combo, BorderLayout.CENTER);
        return pill;
    }

    private JPanel createTableCard() {
        RoundedPanel card = new RoundedPanel(20, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(12, 12, 12, 12));
        card.setPreferredSize(new Dimension(0, 150));

        model = new DefaultTableModel(TAB_COLUMNS[selectedTab], 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(50);
        table.setShowGrid(true);
        table.setGridColor(COLOR_TABLE_GRID);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setFont(new Font("SansSerif", Font.PLAIN, 16));
        table.setFillsViewportHeight(true);

        // Header de la tabla (mismo estilo que gestionEmpleados)
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 45));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel lbl = new JLabel(String.valueOf(value), SwingConstants.CENTER);
                lbl.setOpaque(true);
                lbl.setBackground(COLOR_TABLE_HEADER);
                lbl.setForeground(Color.WHITE);
                lbl.setFont(new Font("SansSerif", Font.BOLD, 17));
                return lbl;
            }
        });

        // Renderer centrado para todas las columnas (sobrevive al cambio de pestaña)
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_TABLE_GRID, 1));
        scroll.getViewport().setBackground(Color.WHITE);

        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // =====================================================================
    // LÓGICA
    // =====================================================================

    /** Resalta la pestaña elegida y ajusta las columnas de la tabla. */
    private void selectTab(int index) {
        selectedTab = index;
        if (tabs != null) {
            for (int i = 0; i < tabs.length; i++) {
                if (i == index) {
                    tabs[i].setColors(COLOR_TABLE_HEADER, Color.WHITE);
                } else {
                    tabs[i].setColors(COLOR_TAB, COLOR_TABLE_HEADER);
                }
            }
        }
        if (model != null) {
            model.setColumnIdentifiers(TAB_COLUMNS[index]);
            model.setRowCount(0);
        }
    }

    /**
     * Genera el reporte según la pestaña y filtros elegidos.
     * TODO: conectar con crud (consultas de ventas) y llenar la tabla y las gráficas:
     * model.addRow(new Object[] { ... });
     * barChart.setDatos(labels, valores, colores);
     * turnoChart.setPorcentaje(pctMañana);
     * comboChart.setPorcentaje(pctCombos);
     */
    private void generarReporte() {
        model.setRowCount(0);
    }

    // =====================================================================
    // COMPONENTES
    // =====================================================================

    private static void antialias(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    private static void drawTitle(Graphics2D g2, String text, int width) {
        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2.setColor(COLOR_TABLE_HEADER);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, (width - fm.stringWidth(text)) / 2, 30);
    }

    /** Gráfica de barras "Ventas por hora del día". */
    private static class BarChartPanel extends RoundedPanel {

        private final String title;
        private String[] labels = { "6am", "7am", "8am", "12pm", "1pm", "2pm", "5pm", "6pm", "7pm" };
        private double[] values = { 610, 450, 480, 610, 450, 480, 610, 450, 480 }; // datos de ejemplo
        private Color[] colors = {
                COLOR_TEXT_RED, COLOR_TEXT_RED, COLOR_TEXT_RED,
                COLOR_SEARCH, COLOR_SEARCH, COLOR_SEARCH,
                COLOR_COMBO, COLOR_COMBO, COLOR_COMBO };

        BarChartPanel(String title) {
            super(20, Color.WHITE);
            this.title = title;
        }

        void setDatos(String[] labels, double[] values, Color[] colors) {
            this.labels = labels;
            this.values = values;
            this.colors = colors;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            antialias(g2);

            int w = getWidth();
            int h = getHeight();
            drawTitle(g2, title, w);

            int left = 75, right = 25, top = 50, bottom = 40;
            int chartW = w - left - right;
            int chartH = h - top - bottom;
            if (chartW <= 0 || chartH <= 0 || labels.length == 0) {
                g2.dispose();
                return;
            }
            int baseY = top + chartH;

            double max = 0;
            for (double v : values) {
                max = Math.max(max, v);
            }
            if (max <= 0) {
                max = 1;
            }

            // Etiquetas del eje Y
            g2.setColor(COLOR_TABLE_HEADER);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2.drawString(String.format(Locale.US, "Q%,.0f", max), 12, top + 12);
            g2.drawString("Q0", 12, baseY);

            // Barras
            int slot = chartW / labels.length;
            int barW = Math.max(6, (int) (slot * 0.7));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
            FontMetrics fm = g2.getFontMetrics();
            for (int i = 0; i < labels.length; i++) {
                int barH = (int) (values[i] / max * (chartH - 10));
                int x = left + i * slot + (slot - barW) / 2;
                g2.setColor(colors[i % colors.length]);
                g2.fillRect(x, baseY - barH, barW, barH);

                g2.setColor(COLOR_TABLE_HEADER);
                g2.drawString(labels[i], x + (barW - fm.stringWidth(labels[i])) / 2, baseY + 24);
            }

            // Línea base
            g2.setColor(COLOR_BTN_NUEVO);
            g2.fillRoundRect(left - 6, baseY, chartW + 12, 6, 6, 6);

            g2.dispose();
        }
    }

    /** Gráfica de pastel con dos porciones y leyenda. */
    private static class PieChartPanel extends RoundedPanel {

        private final String title;
        private final Color color1, color2;
        private final String name1, name2;
        private double porcentaje;

        PieChartPanel(String title, Color color1, String name1, Color color2, String name2, double porcentaje) {
            super(20, Color.WHITE);
            this.title = title;
            this.color1 = color1;
            this.name1 = name1;
            this.color2 = color2;
            this.name2 = name2;
            this.porcentaje = porcentaje;
        }

        /** Porcentaje (0-100) de la primera porción. */
        void setPorcentaje(double porcentaje) {
            this.porcentaje = Math.max(0, Math.min(100, porcentaje));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            antialias(g2);

            int w = getWidth();
            int h = getHeight();
            drawTitle(g2, title, w);

            int top = 50, margin = 20;
            int availH = h - top - margin;
            int d = Math.min(availH, (int) (w * 0.48));
            if (d < 40) {
                g2.dispose();
                return;
            }
            int px = margin;
            int py = top + (availH - d) / 2;

            double ext = -360 * porcentaje / 100.0;
            slice(g2, px, py, d, 90, ext, color1);
            slice(g2, px, py, d, 90 + ext, -(360 + ext), color2);

            int lx = px + d + 25;
            legend(g2, color1, name1, String.format("%.0f%%", porcentaje), lx, py + (int) (d * 0.30));
            legend(g2, color2, name2, String.format("%.0f%%", 100 - porcentaje), lx, py + (int) (d * 0.72));

            g2.dispose();
        }

        private void slice(Graphics2D g2, int x, int y, int d, double start, double extent, Color c) {
            Arc2D arc = new Arc2D.Double(x, y, d, d, start, extent, Arc2D.PIE);
            g2.setColor(c);
            g2.fill(arc);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f));
            g2.draw(arc);
        }

        private void legend(Graphics2D g2, Color color, String name, String pct, int x, int yCenter) {
            g2.setColor(color);
            g2.fillOval(x, yCenter - 9, 18, 18);
            g2.setColor(COLOR_TABLE_HEADER);
            g2.setFont(new Font("SansSerif", Font.BOLD, 15));
            g2.drawString(name, x + 26, yCenter - 1);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 15));
            g2.drawString(pct, x + 26, yCenter + 17);
        }
    }

    /** Botón/pestaña redondeada con texto centrado. */
    private static class PillButton extends RoundedPanel {

        private final JLabel label;

        PillButton(String text, int radius, Color bg, Color fg, int fontSize) {
            super(radius, bg);
            setLayout(new GridBagLayout());
            label = new JLabel(text, SwingConstants.CENTER);
            label.setForeground(fg);
            label.setFont(new Font("SansSerif", Font.BOLD, fontSize));
            add(label);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        void setColors(Color bg, Color fg) {
            setBackgroundColor(bg);
            label.setForeground(fg);
            repaint();
        }

        void onClick(Runnable action) {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    action.run();
                }
            });
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
                    g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
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
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString("Q", 16 - fm.stringWidth("Q") / 2, 19);
                    break;
                case SETTINGS:
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
            }
            g2.dispose();
        }
    }

    // --- COMPONENTE DE PANEL REDONDEADO ---
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
            pantallaEstadistica app = new pantallaEstadistica();
            app.setVisible(true);
        });
    }
}