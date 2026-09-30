package gui;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

// Sistema Git & Eat! - Pantalla de Reportes y Estadísticas
// Muestra las métricas de ventas en gráficas y tablas conectadas a MySQL
public class pantallaEstadistica extends JFrame {

    // Paleta de colores principal de la interfaz
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

    // Colores específicos para los filtros y pestañas
    private static final Color COLOR_FILTERS_BG = new Color(242, 227, 211);
    private static final Color COLOR_TAB = new Color(227, 205, 176);

    // Posición de esta pantalla en el menú lateral
    private static final int INDICE_ACTUAL = 3;

    // Títulos de las pestañas de reportes
    private static final String[] TAB_TITLES = {
            "Ventas por fecha", "Ventas por cajero", "Productos más vendidos", "Método de pago"
    };
    
    // Columnas de la tabla para cada pestaña respectivamente
    private static final String[][] TAB_COLUMNS = {
            { "Fecha", "Transacciones", "Total vendido" },
            { "Cajero", "Transacciones", "Total vendido" },
            { "Producto", "Unidades vendidas", "Total vendido" },
            { "Método de pago", "Transacciones", "Total pagado" }
    };

    // Variables de estado de la interfaz
    private int selectedMenuIndex = INDICE_ACTUAL;
    private int selectedTab = 0; 
    private JPanel[] menuButtons;

    // Elementos gráficos interactivos
    private PillButton[] tabs;
    private JComboBox<ComboItem> cmbCategoria;
    private JComboBox<ComboItem> cmbCajero;
    private DefaultTableModel model;
    
    // Componentes de las gráficas
    private BarChartPanel barChart;
    private PieChartPanel turnoChart;
    private PieChartPanel comboChart;

    // Constructor principal que arma la ventana
    public pantallaEstadistica() {
        setTitle("GIT & EAT! - Reportes y estadísticas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1280, 720));

        // Contenedor base de toda la pantalla
        JPanel mainContainer = new JPanel(new BorderLayout(15, 15));
        mainContainer.setBackground(COLOR_BG);
        mainContainer.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Se añade la barra lateral a la izquierda
        mainContainer.add(createSidebarPanel(), BorderLayout.WEST);

        // Se agrupa el encabezado y el cuerpo en el centro
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setOpaque(false);
        contentPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        contentPanel.add(createMainBody(), BorderLayout.CENTER);

        mainContainer.add(contentPanel, BorderLayout.CENTER);
        add(mainContainer);

        // Permite presionar la tecla ESCAPE para regresar al menú principal
        getRootPane().registerKeyboardAction(
                e -> irADashboard(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Carga los datos de la base de datos al abrir la pantalla
        cargarFiltrosDB();
        selectTab(selectedTab);
        generarReporte();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                generarReporte();
            }
        });
    }

    // Fuerza a la ventana a mostrarse maximizada
    public void mostrarPantallaCompleta() {
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    // Obtiene la conexión centralizada desde tu clase Conexion
    private Connection getConnection() {
        return new main.Conexion.Conexion().getConnection();
    }

    // Busca categorías y cajeros activos en la base de datos para los filtros
    private void cargarFiltrosDB() {
        // Se añade la opción por defecto en caso de no querer filtrar
        cmbCategoria.addItem(new ComboItem(0, "Todas las categorías"));
        cmbCajero.addItem(new ComboItem(0, "Todos los cajeros"));

        Connection conn = getConnection();
        if (conn == null) {
            System.err.println("No se pudo conectar a la base de datos para cargar los filtros.");
            return;
        }

        try {
            // Carga las categorías disponibles
            String sqlCat = "SELECT id_categoria, nombre FROM categoria WHERE estado = 1";
            try (PreparedStatement ps = conn.prepareStatement(sqlCat);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cmbCategoria.addItem(new ComboItem(rs.getInt("id_categoria"), rs.getString("nombre")));
                }
            }

            // Carga los cajeros registrados en el sistema
            String sqlCaj = "SELECT id_usuario, CONCAT(nombre, ' ', apellido) AS nombre_completo FROM usuario WHERE rol = 'CAJERO' AND estado = 1";
            try (PreparedStatement ps = conn.prepareStatement(sqlCaj);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cmbCajero.addItem(new ComboItem(rs.getInt("id_usuario"), rs.getString("nombre_completo")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error cargando filtros: " + e.getMessage());
        } finally {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    // Abre la pantalla correspondiente al botón seleccionado en el menú
    private void navegarA(int indice) {
        if (indice == INDICE_ACTUAL) return;
        
        switch (indice) {
            case 0: new gestionEmpleados().setVisible(true); dispose(); return;
            case 1: new gestionProductos().setVisible(true); dispose(); return;
            case 2: new gestionPedidos().setVisible(true); dispose(); return;
            case 4: new gestionCaja().setVisible(true); dispose(); return;
            case 5: new seguridadAuditoria().setVisible(true); dispose(); return;
        }
    }

    // Cierra esta ventana y abre el panel principal del administrador
    private void irADashboard() {
        dashboardAdmin app = new dashboardAdmin();
        app.setVisible(true);
        dispose();
    }

    // Dibuja todo el menú lateral izquierdo
    private JPanel createSidebarPanel() {
        RoundedPanel sidebar = new RoundedPanel(25, COLOR_SIDEBAR);
        sidebar.setLayout(new BorderLayout(0, 15));
        sidebar.setPreferredSize(new Dimension(320, 0));
        sidebar.setBorder(new EmptyBorder(15, 15, 20, 15));

        // Tarjeta blanca para el logo
        RoundedPanel logoCard = new RoundedPanel(20, Color.WHITE);
        logoCard.setPreferredSize(new Dimension(290, 140));
        logoCard.setLayout(new GridBagLayout());
        logoCard.add(createLogoLabel());
        logoCard.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Al hacer clic en el logo regresa al inicio
        logoCard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { irADashboard(); }
        });
        sidebar.add(logoCard, BorderLayout.NORTH);

        // Contenedor para los botones de navegación
        JPanel menuPanel = new JPanel(new GridLayout(7, 1, 0, 8));
        menuPanel.setOpaque(false);

        // Lista de opciones del menú
        Object[][] items = {
                { SidebarVectorIcon.IconType.EMPLOYEES, "gui/images/employees.png", "<html>Gestión de<br>empleados (cajeros)</html>" },
                { SidebarVectorIcon.IconType.MENU, "gui/images/menu.png", "<html>Gestión de menú /<br>productos</html>" },
                { SidebarVectorIcon.IconType.ORDERS, "gui/images/orders.png", "Gestión de pedidos" },
                { SidebarVectorIcon.IconType.REPORTS, "gui/images/reports.png", "Reportes y estadísticas" },
                { SidebarVectorIcon.IconType.CASH, "gui/images/cash.png", "Gestión de caja" },
                { SidebarVectorIcon.IconType.SECURITY, "gui/images/security.png", "Seguridad y auditoría" }
        };

        menuButtons = new JPanel[items.length];
        
        // Crea visualmente cada botón del menú
        for (int i = 0; i < items.length; i++) {
            final int index = i;
            SidebarVectorIcon.IconType iconType = (SidebarVectorIcon.IconType) items[i][0];
            String textHtml = (String) items[i][2];

            RoundedPanel btnPanel = new RoundedPanel(15, i == selectedMenuIndex ? COLOR_SIDEBAR_ACTIVE : COLOR_SIDEBAR);
            btnPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 8));
            btnPanel.setPreferredSize(new Dimension(0, 60));

            JLabel iconLbl = createSidebarIconLabel(iconType, (String) items[i][1]);
            JLabel textLbl = new JLabel(textHtml);
            textLbl.setFont(new Font("SansSerif", Font.BOLD, 17));
            textLbl.setForeground(Color.WHITE);

            btnPanel.add(iconLbl);
            btnPanel.add(textLbl);
            btnPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Colores al pasar el mouse por encima
            btnPanel.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { if (selectedMenuIndex != index) { btnPanel.setBackgroundColor(COLOR_SIDEBAR_HOVER); btnPanel.repaint(); } }
                @Override public void mouseExited(MouseEvent e) { if (selectedMenuIndex != index) { btnPanel.setBackgroundColor(COLOR_SIDEBAR); btnPanel.repaint(); } }
                @Override public void mouseClicked(MouseEvent e) { navegarA(index); }
            });

            menuButtons[i] = btnPanel;
            menuPanel.add(btnPanel);
        }

        JPanel menuWrapper = new JPanel(new BorderLayout());
        menuWrapper.setOpaque(false);
        menuWrapper.add(menuPanel, BorderLayout.NORTH);
        sidebar.add(menuWrapper, BorderLayout.CENTER);

        // Botón rojo inferior para cerrar la sesión
        RoundedPanel btnCerrarSesion = new RoundedPanel(15, COLOR_CERRAR_SESION);
        btnCerrarSesion.setLayout(new GridBagLayout());
        btnCerrarSesion.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrarSesion.setPreferredSize(new Dimension(0, 55));
        JLabel lblCerrarSesion = new JLabel("Cerrar Sesión");
        lblCerrarSesion.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblCerrarSesion.setForeground(Color.WHITE);
        btnCerrarSesion.add(lblCerrarSesion);
        
        // Confirmación antes de salir
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
                    SwingUtilities.invokeLater(() -> {
                        new pantallaLogin().setVisible(true);
                    });
                    for (Window window : Window.getWindows()) {
                        window.dispose();
                    }
                }
            }
        });
        sidebar.add(btnCerrarSesion, BorderLayout.SOUTH);

        return sidebar;
    }

    // Busca la imagen del icono, si no existe dibuja uno con código
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

    // Muestra el logo de la empresa redimensionado sin deformarse
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
            // Texto por defecto si no hay imagen de logo
            lblLogo.setText("<html><center><font size='7' color='#6EA32E'><b>&lt; 🍴 &gt;</b></font><br>"
                    + "<font size='5'><b color='#6EA32E'>GIT & </b><b color='#D13941'>EAT!</b></font></center></html>");
        }
        return lblLogo;
    }

    // Crea la barra superior donde dice "Reportes y estadísticas"
    private JPanel createHeaderPanel() {
        RoundedPanel header = new RoundedPanel(20, COLOR_HEADER);
        header.setLayout(new BorderLayout());
        header.setPreferredSize(new Dimension(0, 125));
        header.setBorder(new EmptyBorder(20, 35, 20, 35));

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
        return header;
    }

    // Organiza las gráficas, filtros y tabla de forma vertical
    private JPanel createMainBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(8, 6, 8, 6);
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        // Fila 1: Panel de gráficas
        gbc.gridy = 0; gbc.weighty = 0.35;
        body.add(createChartsRow(), gbc);

        // Fila 2: Filtros de búsqueda
        gbc.gridy = 1; gbc.weighty = 0.0;
        body.add(createFiltersPanel(), gbc);

        // Fila 3: Tabla inferior
        gbc.gridy = 2; gbc.weighty = 0.65;
        body.add(createTableCard(), gbc);

        return body;
    }

    // Ubica las tres gráficas visuales en la parte superior
    private JPanel createChartsRow() {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 200));

        barChart = new BarChartPanel("Ventas por hora del día");
        turnoChart = new PieChartPanel("Turno: mañana / tarde", COLOR_BTN_NUEVO, "Mañana", COLOR_SEARCH, "Tarde", 0);
        comboChart = new PieChartPanel("Combos / individuales", COLOR_COMBO, "Combos", COLOR_TEXT_RED, "Individuales", 0);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        gbc.gridx = 0; gbc.weightx = 0.40; gbc.insets = new Insets(0, 0, 0, 15);
        row.add(barChart, gbc);

        gbc.gridx = 1; gbc.weightx = 0.30;
        row.add(turnoChart, gbc);

        gbc.gridx = 2; gbc.weightx = 0.30; gbc.insets = new Insets(0, 0, 0, 0);
        row.add(comboChart, gbc);

        return row;
    }

    // Diseña la zona con pestañas y combobox para elegir qué consultar
    private JPanel createFiltersPanel() {
        RoundedPanel panel = new RoundedPanel(20, COLOR_FILTERS_BG);
        panel.setLayout(new GridLayout(2, 1, 0, 10));
        panel.setBorder(new EmptyBorder(12, 20, 12, 20));
        panel.setPreferredSize(new Dimension(0, 135));

        // Fila de las pestañas superiores (Ventas por fecha, etc.)
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

        // Fila con selectores y el botón Generar
        JPanel controls = new JPanel(new GridBagLayout());
        controls.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        cmbCategoria = new JComboBox<>();
        cmbCajero = new JComboBox<>();

        gbc.gridx = 0; gbc.weightx = 0; gbc.insets = new Insets(0, 0, 0, 10);
        controls.add(createFilterLabel("Categoría:"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.35; gbc.insets = new Insets(0, 0, 0, 30);
        controls.add(wrapCombo(cmbCategoria), gbc);

        gbc.gridx = 2; gbc.weightx = 0; gbc.insets = new Insets(0, 0, 0, 10);
        controls.add(createFilterLabel("Cajero:"), gbc);

        gbc.gridx = 3; gbc.weightx = 0.35; gbc.insets = new Insets(0, 0, 0, 30);
        controls.add(wrapCombo(cmbCajero), gbc);

        // Dispara la consulta SQL al presionarse
        PillButton btnGenerar = new PillButton("Generar", 20, COLOR_BTN_NUEVO, Color.WHITE, 18);
        btnGenerar.setPreferredSize(new Dimension(170, 0));
        btnGenerar.onClick(this::generarReporte);
        gbc.gridx = 4; gbc.weightx = 0.15; gbc.insets = new Insets(0, 0, 0, 0);
        controls.add(btnGenerar, gbc);

        panel.add(controls);
        return panel;
    }

    // Texto descriptivo para los filtros
    private JLabel createFilterLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 17));
        lbl.setForeground(COLOR_TABLE_HEADER);
        return lbl;
    }

    // Le da forma redondeada y color a los JComboBox
    private JPanel wrapCombo(JComboBox<?> combo) {
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

    // Tabla inferior donde se listan los resultados
    private JPanel createTableCard() {
        RoundedPanel card = new RoundedPanel(20, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(12, 12, 12, 12));
        card.setPreferredSize(new Dimension(0, 150));

        // Se bloquea la edición de las celdas
        model = new DefaultTableModel(TAB_COLUMNS[selectedTab], 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(model);
        table.setRowHeight(50);
        table.setShowGrid(true);
        table.setGridColor(COLOR_TABLE_GRID);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setFont(new Font("SansSerif", Font.PLAIN, 16));
        table.setFillsViewportHeight(true);

        // Estilo de los encabezados de la tabla
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 45));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = new JLabel(String.valueOf(value), SwingConstants.CENTER);
                lbl.setOpaque(true);
                lbl.setBackground(COLOR_TABLE_HEADER);
                lbl.setForeground(Color.WHITE);
                lbl.setFont(new Font("SansSerif", Font.BOLD, 17));
                return lbl;
            }
        });

        // Centra el contenido de las filas
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_TABLE_GRID, 1));
        scroll.getViewport().setBackground(Color.WHITE);

        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // Reacciona al cambio de pestaña repintando columnas y volviendo a consultar
    private void selectTab(int index) {
        selectedTab = index;
        if (tabs != null) {
            for (int i = 0; i < tabs.length; i++) {
                if (i == index) tabs[i].setColors(COLOR_TABLE_HEADER, Color.WHITE);
                else tabs[i].setColors(COLOR_TAB, COLOR_TABLE_HEADER);
            }
        }
        if (model != null) {
            model.setColumnIdentifiers(TAB_COLUMNS[index]);
            model.setRowCount(0);
        }
        generarReporte();
    }

    // Coordina la lectura de base de datos para gráficas y tabla
    private void generarReporte() {
        if (cmbCategoria.getSelectedItem() == null || cmbCajero.getSelectedItem() == null) return;

        // Recupera la ID real para las consultas SQL
        int idCategoria = ((ComboItem) cmbCategoria.getSelectedItem()).getId();
        int idCajero = ((ComboItem) cmbCajero.getSelectedItem()).getId();

        model.setRowCount(0);

        Connection conn = getConnection();
        if (conn == null) {
            System.err.println("No hay conexión con la base de datos.");
            return;
        }

        try {
            actualizarTablaReporte(conn, idCategoria, idCajero);
            actualizarGraficas(conn, idCategoria, idCajero);
        } catch (SQLException e) {
            System.err.println("Error ejecutando reporte: " + e.getMessage());
        } finally {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    // Elige el SQL correcto según la pestaña actual e inserta los resultados
    private void actualizarTablaReporte(Connection conn, int idCategoria, int idCajero) throws SQLException {
        String sql = "";
        
        // Cláusula compartida para enlazar las tablas de compras
        String baseJoins = "FROM orden o " +
                "JOIN detalle_orden d ON o.id_orden = d.id_orden " +
                "JOIN producto p ON d.id_producto = p.id_producto ";
        
        // Condición para filtrar por los combobox (0 = Todos)
        String whereClause = "WHERE (p.id_categoria = ? OR ? = 0) AND (o.id_usuario = ? OR ? = 0) ";

        // Consulta específica para cada pestaña
        switch (selectedTab) {
            case 0:
                sql = "SELECT o.fecha, COUNT(DISTINCT o.id_orden) AS transacciones, SUM(d.subtotal) AS total " +
                      baseJoins + whereClause + "GROUP BY o.fecha ORDER BY o.fecha DESC";
                break;
            case 1:
                sql = "SELECT CONCAT(u.nombre, ' ', u.apellido) AS cajero, COUNT(DISTINCT o.id_orden) AS transacciones, SUM(d.subtotal) AS total " +
                      baseJoins + "JOIN usuario u ON o.id_usuario = u.id_usuario " +
                      whereClause + "GROUP BY u.id_usuario";
                break;
            case 2:
                sql = "SELECT p.nombre, SUM(d.cantidad) AS unidades, SUM(d.subtotal) AS total " +
                      baseJoins + whereClause + "GROUP BY p.id_producto ORDER BY unidades DESC";
                break;
            case 3:
                sql = "SELECT po.metodo_pago, COUNT(po.id_pago) AS transacciones, SUM(po.monto) AS total " +
                      "FROM pago_orden po WHERE po.id_orden IN (" +
                      "SELECT o.id_orden " + baseJoins + whereClause + ") " +
                      "GROUP BY po.metodo_pago";
                break;
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCategoria); ps.setInt(2, idCategoria);
            ps.setInt(3, idCajero);    ps.setInt(4, idCajero);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                // Inserta una fila en la tabla visual dependiendo del número de columnas
                if (selectedTab == 0) {
                    model.addRow(new Object[]{ rs.getDate(1).toString(), rs.getInt(2), String.format("Q %.2f", rs.getDouble(3)) });
                } else if (selectedTab == 1 || selectedTab == 3) {
                    model.addRow(new Object[]{ rs.getString(1), rs.getInt(2), String.format("Q %.2f", rs.getDouble(3)) });
                } else if (selectedTab == 2) {
                    model.addRow(new Object[]{ rs.getString(1), rs.getInt(2), String.format("Q %.2f", rs.getDouble(3)) });
                }
            }
        }
    }

    // Lee la base de datos para extraer totales globales e inyectarlos a las gráficas
    private void actualizarGraficas(Connection conn, int idCategoria, int idCajero) throws SQLException {
        String baseJoins = "FROM orden o JOIN detalle_orden d ON o.id_orden = d.id_orden JOIN producto p ON d.id_producto = p.id_producto ";
        String whereClause = "WHERE (p.id_categoria = ? OR ? = 0) AND (o.id_usuario = ? OR ? = 0) ";

        // 1. Dibuja las barras dependiendo a qué hora vendieron más
        String sqlHora = "SELECT HOUR(o.hora) AS h, SUM(d.subtotal) AS total " + baseJoins + whereClause + "GROUP BY HOUR(o.hora) ORDER BY h";
        try (PreparedStatement ps = conn.prepareStatement(sqlHora)) {
            ps.setInt(1, idCategoria); ps.setInt(2, idCategoria); ps.setInt(3, idCajero); ps.setInt(4, idCajero);
            ResultSet rs = ps.executeQuery();
            ArrayList<String> labelsList = new ArrayList<>();
            ArrayList<Double> valuesList = new ArrayList<>();
            while (rs.next()) {
                labelsList.add(rs.getInt("h") + ":00");
                valuesList.add(rs.getDouble("total"));
            }
            if(!labelsList.isEmpty()){
                 String[] labels = labelsList.toArray(new String[0]);
                 double[] values = valuesList.stream().mapToDouble(Double::doubleValue).toArray();
                 Color[] colors = new Color[labels.length];
                 for(int i=0; i<colors.length; i++) colors[i] = COLOR_SEARCH;
                 barChart.setDatos(labels, values, colors);
            } else {
                 barChart.setDatos(new String[]{"Sin datos"}, new double[]{0}, new Color[]{Color.LIGHT_GRAY});
            }
        }

        // 2. Calcula la relación de ganancias antes y después de mediodía
        String sqlTurno = "SELECT " +
                "COALESCE(SUM(CASE WHEN o.hora <= '12:00:00' THEN d.subtotal ELSE 0 END), 0) AS manana, " +
                "COALESCE(SUM(CASE WHEN o.hora > '12:00:00' THEN d.subtotal ELSE 0 END), 0) AS tarde " +
                baseJoins + whereClause;
        try (PreparedStatement ps = conn.prepareStatement(sqlTurno)) {
            ps.setInt(1, idCategoria); ps.setInt(2, idCategoria); ps.setInt(3, idCajero); ps.setInt(4, idCajero);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double manana = rs.getDouble("manana");
                double tarde = rs.getDouble("tarde");
                double total = manana + tarde;
                if (total == 0) {
                    turnoChart.setSinDatos();
                } else {
                    turnoChart.setPorcentaje((manana / total) * 100.0);
                }
            }
        }

        // 3. Calcula la relación de combos frente a productos individuales
        String sqlCombo = "SELECT " +
                "COALESCE(SUM(CASE WHEN p.es_combo = 1 THEN d.subtotal ELSE 0 END), 0) AS combos, " +
                "COALESCE(SUM(CASE WHEN p.es_combo = 0 THEN d.subtotal ELSE 0 END), 0) AS ind " +
                baseJoins + whereClause;
        try (PreparedStatement ps = conn.prepareStatement(sqlCombo)) {
            ps.setInt(1, idCategoria); ps.setInt(2, idCategoria); ps.setInt(3, idCajero); ps.setInt(4, idCajero);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double combos = rs.getDouble("combos");
                double ind = rs.getDouble("ind");
                double total = combos + ind;
                if (total == 0) {
                    comboChart.setSinDatos();
                } else {
                    comboChart.setPorcentaje((combos / total) * 100.0);
                }
            }
        }
    }

    // Objeto utilizado para meter ID reales a los combobox pero mostrar un nombre amigable
    private static class ComboItem {
        private int id;
        private String label;

        public ComboItem(int id, String label) {
            this.id = id;
            this.label = label;
        }
        public int getId() { return id; }
        @Override public String toString() { return label; }
    }

    // Suaviza el dibujo de gráficas para que no se vean pixeladas
    private static void antialias(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    // Dibuja el título en la parte alta de las gráficas
    private static void drawTitle(Graphics2D g2, String text, int width) {
        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2.setColor(COLOR_TABLE_HEADER);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, (width - fm.stringWidth(text)) / 2, 30);
    }

    // Panel específico que dibuja barras horizontales en base a números
    private static class BarChartPanel extends RoundedPanel {
        private final String title;
        private String[] labels = { "Sin datos" };
        private double[] values = { 0 }; 
        private Color[] colors = { Color.LIGHT_GRAY };

        BarChartPanel(String title) {
            super(20, Color.WHITE);
            this.title = title;
        }

        // Actualiza la barra y fuerza la pintura
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

            int w = getWidth(); int h = getHeight();
            drawTitle(g2, title, w);

            int left = 75, right = 25, top = 50, bottom = 40;
            int chartW = w - left - right; int chartH = h - top - bottom;
            if (chartW <= 0 || chartH <= 0 || labels.length == 0) { g2.dispose(); return; }
            
            int baseY = top + chartH;
            double max = 0;
            for (double v : values) max = Math.max(max, v);
            if (max <= 0) max = 1;

            // Dibuja cantidades del eje Y
            g2.setColor(COLOR_TABLE_HEADER);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2.drawString(String.format(Locale.US, "Q%,.0f", max), 12, top + 12);
            g2.drawString("Q0", 12, baseY);

            int slot = chartW / labels.length;
            int barW = Math.max(6, (int) (slot * 0.7));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
            FontMetrics fm = g2.getFontMetrics();
            
            // Creación manual de cada barra vertical
            for (int i = 0; i < labels.length; i++) {
                int barH = (int) (values[i] / max * (chartH - 10));
                int x = left + i * slot + (slot - barW) / 2;
                g2.setColor(colors[i % colors.length]);
                g2.fillRect(x, baseY - barH, barW, barH);
                g2.setColor(COLOR_TABLE_HEADER);
                g2.drawString(labels[i], x + (barW - fm.stringWidth(labels[i])) / 2, baseY + 24);
            }

            // Piso de la gráfica
            g2.setColor(COLOR_BTN_NUEVO);
            g2.fillRoundRect(left - 6, baseY, chartW + 12, 6, 6, 6);
            g2.dispose();
        }
    }

    // Panel para gráficas circulares de dos porciones
    private static class PieChartPanel extends RoundedPanel {
        private final String title;
        private final Color color1, color2;
        private final String name1, name2;
        private double porcentaje;
        private boolean hasData;

        PieChartPanel(String title, Color color1, String name1, Color color2, String name2, double porcentaje) {
            super(20, Color.WHITE);
            this.title = title;
            this.color1 = color1; this.name1 = name1;
            this.color2 = color2; this.name2 = name2;
            this.porcentaje = porcentaje;
        }

        // Calcula el pedazo del pastel
        void setPorcentaje(double porcentaje) {
            hasData = true;
            this.porcentaje = Math.max(0, Math.min(100, porcentaje));
            repaint();
        }

        void setSinDatos() {
            hasData = false;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            antialias(g2);

            int w = getWidth(); int h = getHeight();
            drawTitle(g2, title, w);

            int top = 50, margin = 20;
            int availH = h - top - margin;
            int d = Math.min(availH, (int) (w * 0.48));
            if (d < 40) { g2.dispose(); return; }
            
            int px = margin; int py = top + (availH - d) / 2;
            
              if (!hasData) {
                 slice(g2, px, py, d, 90, -360, Color.LIGHT_GRAY);
                 int lx = px + d + 25;
                 legend(g2, color1, name1, "0%", lx, py + (int) (d * 0.30));
                 legend(g2, color2, name2, "0%", lx, py + (int) (d * 0.72));
            } else {
                 double ext = -360 * porcentaje / 100.0;
                  if (porcentaje > 0) slice(g2, px, py, d, 90, ext, color1);
                  if (porcentaje < 100) slice(g2, px, py, d, 90 + ext, -(360 + ext), color2);
                 int lx = px + d + 25;
                 legend(g2, color1, name1, String.format("%.0f%%", porcentaje), lx, py + (int) (d * 0.30));
                 legend(g2, color2, name2, String.format("%.0f%%", 100 - porcentaje), lx, py + (int) (d * 0.72));
            }

            g2.dispose();
        }

        // Dibuja una fracción del círculo
        private void slice(Graphics2D g2, int x, int y, int d, double start, double extent, Color c) {
            Arc2D arc = new Arc2D.Double(x, y, d, d, start, extent, Arc2D.PIE);
            g2.setColor(c);
            g2.fill(arc);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f));
            g2.draw(arc); 
        }

        // Agrega el nombre y color de la categoría a la derecha del pastel
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

    // Botones con apariencia curva para pestañas de filtros
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
                @Override public void mouseClicked(MouseEvent e) { action.run(); }
            });
        }
    }

    // Dibuja trazos y formas vectoriales para usar cuando falta la imagen de un icono
    private static class SidebarVectorIcon implements Icon {
        public enum IconType { EMPLOYEES, MENU, ORDERS, REPORTS, CASH, SETTINGS, SECURITY }
        private final IconType type;
        private final int size;

        public SidebarVectorIcon(IconType type, int size) {
            this.type = type;
            this.size = size;
        }
        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            g2.setColor(Color.WHITE);
            float scale = size / 32.0f;
            g2.scale(scale, scale);

            // Representaciones geométricas para cada tipo de botón
            switch (type) {
                case EMPLOYEES:
                    g2.fillOval(10, 3, 12, 12); g2.fillArc(4, 16, 24, 18, 0, 180);
                    g2.setColor(new Color(255, 255, 255, 180));
                    g2.fillOval(20, 6, 8, 8); g2.fillArc(17, 15, 14, 12, 0, 180);
                    break;
                case MENU:
                    g2.fillArc(3, 5, 26, 14, 0, 180); g2.fillRoundRect(2, 14, 28, 4, 2, 2); g2.fillRoundRect(4, 20, 24, 6, 3, 3);
                    break;
                case ORDERS:
                    g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawRoundRect(5, 5, 22, 24, 4, 4); g2.fillRoundRect(11, 3, 10, 4, 2, 2);
                    g2.drawLine(9, 12, 23, 12); g2.drawLine(9, 17, 23, 17); g2.drawLine(9, 22, 18, 22);
                    break;
                case REPORTS:
                    g2.fillRoundRect(4, 18, 6, 10, 2, 2); g2.fillRoundRect(13, 12, 6, 16, 2, 2); g2.fillRoundRect(22, 6, 6, 22, 2, 2);
                    break;
                case CASH:
                    g2.setStroke(new BasicStroke(2.0f)); g2.drawRoundRect(3, 7, 26, 18, 4, 4); g2.drawOval(11, 11, 10, 10);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 10)); g2.drawString("Q", 16 - g2.getFontMetrics().stringWidth("Q") / 2, 19);
                    break;
                case SECURITY:
                    Path2D shield = new Path2D.Double();
                    shield.moveTo(16, 3); shield.curveTo(24, 3, 27, 5, 27, 13); shield.curveTo(27, 22, 18, 27, 16, 29);
                    shield.curveTo(14, 27, 5, 22, 5, 13); shield.curveTo(5, 5, 8, 3, 16, 3); shield.closePath();
                    g2.setStroke(new BasicStroke(2.2f)); g2.draw(shield);
                    Path2D check = new Path2D.Double();
                    check.moveTo(11, 15); check.lineTo(14, 18); check.lineTo(21, 11); g2.draw(check);
                    break;
            }
            g2.dispose();
        }
    }

    // Panel con bordes redondos utilizado en gráficas y botones
    private static class RoundedPanel extends JPanel {
        private final int cornerRadius;
        private Color backgroundColor;

        public RoundedPanel(int radius, Color bgColor) {
            this.cornerRadius = radius;
            this.backgroundColor = bgColor;
            setOpaque(false);
        }
        public void setBackgroundColor(Color bgColor) { this.backgroundColor = bgColor; }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D graphics = (Graphics2D) g;
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(backgroundColor);
            graphics.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
        }
    }

    // Método temporal para lanzar solo esta ventana durante el diseño
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