package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import main.Conexion.Conexion;

/**
 * Permite consultar pedidos, buscar por criterios, ver sus detalles y cambiar
 * su estado.
 */
public class gestionPedidos extends JFrame {

    // Paleta de colores (idéntica al resto de la aplicación)
    private static final Color COLOR_BG = new Color(231, 221, 202);
    private static final Color COLOR_SIDEBAR = new Color(139, 94, 52);
    private static final Color COLOR_HEADER = new Color(139, 94, 52);
    private static final Color COLOR_TEXT_BROWN = new Color(92, 53, 22);
    private static final Color COLOR_SEARCH_CHIP = new Color(106, 161, 46);
    private static final Color COLOR_BTN_BUSCAR = new Color(243, 205, 59);
    private static final Color COLOR_LIMPIAR = new Color(230, 115, 45);
    private static final Color COLOR_TABLE_HEADER = new Color(92, 53, 22);
    private static final Color COLOR_TABLE_GRID = new Color(222, 210, 191);
    private static final Color COLOR_TABLE_ROW = new Color(222, 210, 191);
    private static final Color COLOR_TEXT_GREEN = new Color(106, 161, 46);
    private static final Color COLOR_TEXT_RED = new Color(211, 53, 58);
    private static final Color COLOR_SIDEBAR_HOVER = new Color(160, 110, 65);
    private static final Color COLOR_SIDEBAR_ACTIVE = new Color(110, 72, 38);
    private static final Color COLOR_CERRAR_SESION = new Color(211, 53, 58);
    private static final Color COLOR_TAB_INACTIVE = Color.WHITE;
    private static final Color COLOR_TAB_ACTIVE = new Color(92, 53, 22);

    private int selectedMenuIndex = 2; // "Gestión de pedidos" seleccionado por defecto en esta vista
    private JPanel[] menuButtons;

    // Estado del toggle Tiempo real / Historial (0 = Tiempo real, 1 = Historial)
    private int selectedTab = 1;
    private RoundedPanel tabTiempoReal;
    private RoundedPanel tabHistorial;
    private JLabel lblTabTiempoReal;
    private JLabel lblTabHistorial;
    private DefaultTableModel ordersModel;
    private JTable ordersTable;
    private SearchCriterion selectedCriterion = SearchCriterion.NONE;
    private final Map<SearchCriterion, RoundedPanel> searchChips = new EnumMap<>(SearchCriterion.class);
    private CardLayout searchInputLayout;
    private JPanel searchInputPanel;
    private JTextField txtSearchValue;
    private JLabel lblSearchHint;
    private JComboBox<String> cbSearchState;

    private enum SearchCriterion {
        NONE, ORDER_ID, DATE, CASHIER, STATE
    }

    private static final String[] ORDER_STATES = {
        "En cocina", "Preparando", "Listo", "Entregado", "Cancelado", "Completada"
    };

    public gestionPedidos() {
        setTitle("GIT & EAT! - Gestión de Pedidos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1280, 720));

        JPanel mainContainer = new JPanel(new BorderLayout(15, 15));
        mainContainer.setBackground(COLOR_BG);
        mainContainer.setBorder(new EmptyBorder(15, 15, 15, 15));

        // 1. BARRA LATERAL (idéntica al resto de la aplicación + botón Cerrar Sesión)
        mainContainer.add(createSidebarPanel(), BorderLayout.WEST);

        // 2. PANEL CENTRAL (HEADER + CONTENIDO)
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setOpaque(false);

        contentPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        contentPanel.add(createMainBody(), BorderLayout.CENTER);

        mainContainer.add(contentPanel, BorderLayout.CENTER);
        add(mainContainer);

        // Carga el historial inicial y consulta periódicamente los pedidos activos.
        loadOrders(true);
        Timer refreshTimer = new Timer(30_000, e -> {
            if (selectedTab == 0) loadOrders(false);
        });
        refreshTimer.start();
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
            {SidebarVectorIcon.IconType.EMPLOYEES, "gui/images/employees.png", "<html>Gestión de<br>empleados (cajeros)</html>"},
            {SidebarVectorIcon.IconType.MENU, "gui/images/menu.png", "<html>Gestión de menú /<br>productos</html>"},
            {SidebarVectorIcon.IconType.ORDERS, "gui/images/orders.png", "Gestión de pedidos"},
            {SidebarVectorIcon.IconType.REPORTS, "gui/images/reports.png", "Reportes y estadísticas"},
            {SidebarVectorIcon.IconType.CASH, "gui/images/cash.png", "Gestión de caja"},
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
                    if (index == 2) {
                        // Ya estamos en la ventana de Gestión de pedidos
                        selectedMenuIndex = index;
                        updateSidebarSelection();
                        return;
                    }
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
                    if (index == 3) {
                        pantallaEstadistica app = new pantallaEstadistica();
                        app.setVisible(true);
                        dispose();
                        return;
                    }
                    if (index == 4) {
                        gestionCaja app = new gestionCaja();
                        app.setVisible(true);
                        dispose();
                        return;
                    }
                    
                    if (index == 5) {
                        seguridadAuditoria app = new seguridadAuditoria();
                        app.setVisible(true);
                        dispose();
                        return;
                    }
                   
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
                        gestionPedidos.this,
                        "¿Desea cerrar la sesión actual?",
                        "Confirmar cierre de sesión",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (confirmacion == JOptionPane.YES_OPTION) {
                    SwingUtilities.invokeLater(() -> new pantallaLogin().setVisible(true));
                    for (Window window : Window.getWindows()) window.dispose();
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
            lblLogo.setText("<html><center><font size='7' color='#6EA32E'><b>&lt; 🍴 &gt;</b></font><br>" +
                    "<font size='5'><b color='#6EA32E'>GIT & </b><b color='#D13941'>EAT!</b></font></center></html>");
        }
        return lblLogo;
    }

    // ==========================================
    // --- VISTA PRINCIPAL (GESTIÓN DE PEDIDOS) ---
    // ==========================================
    private JPanel createHeaderPanel() {
        RoundedPanel header = new RoundedPanel(20, COLOR_HEADER);
        header.setLayout(new BorderLayout());
        header.setPreferredSize(new Dimension(0, 125));
        header.setBorder(new EmptyBorder(20, 35, 20, 35));

        JLabel title = new JLabel("Gestión de Pedidos");
        title.setFont(new Font("SansSerif", Font.BOLD, 40));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        rightHeader.setOpaque(false);

        return header;
    }

    private JPanel createMainBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(6, 6, 6, 6);

        // Fila 1: toggle Tiempo real / Historial (alineado a la derecha)
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.weighty = 0.06;
        body.add(createTabsRow(), gbc);

        // Fila 2: panel "Buscar por:" con los chips de filtro
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 1.0; gbc.weighty = 0.16;
        body.add(createSearchByPanel(), gbc);

        // Fila 3: Tabla de pedidos
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 1.0; gbc.weighty = 0.78;
        body.add(createTableCard(), gbc);

        return body;
    }

    // --- TOGGLE TIEMPO REAL / HISTORIAL ---
    private JPanel createTabsRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        row.setOpaque(false);

        tabTiempoReal = new RoundedPanel(18, selectedTab == 0 ? COLOR_TAB_ACTIVE : COLOR_TAB_INACTIVE);
        tabTiempoReal.setLayout(new GridBagLayout());
        tabTiempoReal.setPreferredSize(new Dimension(140, 40));
        tabTiempoReal.setCursor(new Cursor(Cursor.HAND_CURSOR));

        lblTabTiempoReal = new JLabel("Tiempo real");
        lblTabTiempoReal.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTabTiempoReal.setForeground(selectedTab == 0 ? Color.WHITE : COLOR_TEXT_BROWN);
        tabTiempoReal.add(lblTabTiempoReal);

        tabHistorial = new RoundedPanel(18, selectedTab == 1 ? COLOR_TAB_ACTIVE : COLOR_TAB_INACTIVE);
        tabHistorial.setLayout(new GridBagLayout());
        tabHistorial.setPreferredSize(new Dimension(120, 40));
        tabHistorial.setCursor(new Cursor(Cursor.HAND_CURSOR));

        lblTabHistorial = new JLabel("Historial");
        lblTabHistorial.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTabHistorial.setForeground(selectedTab == 1 ? Color.WHITE : COLOR_TEXT_BROWN);
        tabHistorial.add(lblTabHistorial);

        tabTiempoReal.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedTab = 0;
                updateTabsSelection();
                loadOrders(true);
            }
        });

        tabHistorial.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedTab = 1;
                updateTabsSelection();
                loadOrders(true);
            }
        });

        row.add(tabTiempoReal);
        row.add(tabHistorial);

        return row;
    }

    private void updateTabsSelection() {
        tabTiempoReal.setBackgroundColor(selectedTab == 0 ? COLOR_TAB_ACTIVE : COLOR_TAB_INACTIVE);
        lblTabTiempoReal.setForeground(selectedTab == 0 ? Color.WHITE : COLOR_TEXT_BROWN);
        tabTiempoReal.repaint();

        tabHistorial.setBackgroundColor(selectedTab == 1 ? COLOR_TAB_ACTIVE : COLOR_TAB_INACTIVE);
        lblTabHistorial.setForeground(selectedTab == 1 ? Color.WHITE : COLOR_TEXT_BROWN);
        tabHistorial.repaint();
    }

    // --- PANEL "BUSCAR POR:" (usa TitledBorder para el efecto de "fieldset") ---
    private JPanel createSearchByPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 4));
        panel.setOpaque(false);

        TitledBorder titled = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_TEXT_BROWN, 2, true),
                "Buscar por:"
        );
        titled.setTitleFont(new Font("SansSerif", Font.BOLD, 16));
        titled.setTitleColor(COLOR_TEXT_BROWN);
        panel.setBorder(BorderFactory.createCompoundBorder(titled, new EmptyBorder(4, 12, 8, 12)));

        JPanel chipsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        chipsRow.setOpaque(false);
        chipsRow.add(createSearchChip("No. Pedido", SearchCriterion.ORDER_ID));
        chipsRow.add(createSearchChip("Fecha", SearchCriterion.DATE));
        chipsRow.add(createSearchChip("Cajero", SearchCriterion.CASHIER));
        chipsRow.add(createSearchChip("Estado", SearchCriterion.STATE));
        panel.add(chipsRow, BorderLayout.NORTH);

        JPanel controls = new JPanel(new BorderLayout(10, 0));
        controls.setOpaque(false);
        searchInputPanel = createSearchInputPanel();
        controls.add(searchInputPanel, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        actions.setOpaque(false);
        RoundedPanel btnBuscar = createSearchAction("Buscar", COLOR_BTN_BUSCAR);
        btnBuscar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                loadOrders(true);
            }
        });
        actions.add(btnBuscar);

        RoundedPanel btnLimpiar = createSearchAction("Limpiar", COLOR_LIMPIAR);
        btnLimpiar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionarCriterio(SearchCriterion.NONE);
                txtSearchValue.setText("");
                cbSearchState.setSelectedIndex(0);
                loadOrders(true);
            }
        });
        actions.add(btnLimpiar);
        controls.add(actions, BorderLayout.EAST);
        panel.add(controls, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSearchInputPanel() {
        searchInputLayout = new CardLayout();
        JPanel inputPanel = new JPanel(searchInputLayout);
        inputPanel.setOpaque(false);

        JPanel textPanel = new JPanel(new BorderLayout(8, 0));
        textPanel.setOpaque(false);
        lblSearchHint = new JLabel("Selecciona un filtro:");
        lblSearchHint.setForeground(COLOR_TEXT_BROWN);
        lblSearchHint.setFont(new Font("SansSerif", Font.BOLD, 13));
        txtSearchValue = new JTextField();
        txtSearchValue.setToolTipText("Escribe el valor y presiona Buscar");
        textPanel.add(lblSearchHint, BorderLayout.WEST);
        textPanel.add(txtSearchValue, BorderLayout.CENTER);

        JPanel statePanel = new JPanel(new BorderLayout(8, 0));
        statePanel.setOpaque(false);
        JLabel stateLabel = new JLabel("Estado:");
        stateLabel.setForeground(COLOR_TEXT_BROWN);
        stateLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        cbSearchState = new JComboBox<>(new String[]{
            "Todos", "En cocina", "Preparando", "Listo", "Entregado", "Cancelado", "Completada"
        });
        statePanel.add(stateLabel, BorderLayout.WEST);
        statePanel.add(cbSearchState, BorderLayout.CENTER);

        JLabel noFilter = new JLabel("Selecciona un criterio o presiona Buscar para actualizar.");
        noFilter.setForeground(COLOR_TEXT_BROWN);
        inputPanel.add(noFilter, "none");
        inputPanel.add(textPanel, "text");
        inputPanel.add(statePanel, "state");
        searchInputLayout.show(inputPanel, "none");
        return inputPanel;
    }

    private RoundedPanel createSearchAction(String text, Color background) {
        RoundedPanel button = new RoundedPanel(18, background);
        button.setLayout(new GridBagLayout());
        button.setPreferredSize(new Dimension(105, 42));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(Color.WHITE);
        button.add(label);
        return button;
    }

    private RoundedPanel createSearchChip(String text, SearchCriterion criterion) {
        RoundedPanel chip = new RoundedPanel(18, COLOR_SEARCH_CHIP);
        chip.setLayout(new GridBagLayout());
        chip.setPreferredSize(new Dimension(125, 38));
        chip.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chip.setToolTipText("Filtrar pedidos por " + text.toLowerCase(Locale.ROOT));
        searchChips.put(criterion, chip);

        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        lbl.setForeground(Color.WHITE);
        chip.add(lbl);

        chip.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionarCriterio(selectedCriterion == criterion ? SearchCriterion.NONE : criterion);
            }
        });

        return chip;
    }

    // Activa un filtro, resalta su chip y muestra el control de entrada adecuado.
    private void seleccionarCriterio(SearchCriterion criterion) {
        selectedCriterion = criterion;
        for (Map.Entry<SearchCriterion, RoundedPanel> entry : searchChips.entrySet()) {
            entry.getValue().setBackgroundColor(entry.getKey() == criterion ? COLOR_TAB_ACTIVE : COLOR_SEARCH_CHIP);
            entry.getValue().repaint();
        }

        if (criterion == SearchCriterion.NONE) {
            searchInputLayout.show(searchInputPanel, "none");
            return;
        }

        if (criterion == SearchCriterion.STATE) {
            searchInputLayout.show(searchInputPanel, "state");
            return;
        }

        lblSearchHint.setText(switch (criterion) {
            case ORDER_ID -> "No. de pedido:";
            case DATE -> "Fecha (dd/MM/aaaa):";
            case CASHIER -> "Cajero:";
            default -> "Buscar:";
        });
        txtSearchValue.setText("");
        searchInputLayout.show(searchInputPanel, "text");
        txtSearchValue.requestFocusInWindow();
    }

    // --- TABLA DE PEDIDOS ---
    private JPanel createTableCard() {
        RoundedPanel card = new RoundedPanel(20, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] columns = {"ID", "Pedido", "Fecha", "Hora", "Cajero", "Productos", "Total", "Estado", "Pago"};
        ordersModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        ordersTable = new JTable(ordersModel);
        ordersTable.setRowHeight(45);
        ordersTable.setShowGrid(true);
        ordersTable.setGridColor(COLOR_TABLE_GRID);
        ordersTable.setIntercellSpacing(new Dimension(1, 1));
        ordersTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        ordersTable.setBackground(Color.WHITE);
        ordersTable.setAutoCreateRowSorter(true);
        ordersTable.setFillsViewportHeight(true);

        JTableHeader header = ordersTable.getTableHeader();
        header.setPreferredSize(new Dimension(0, 45));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel lbl = new JLabel(value.toString(), SwingConstants.CENTER);
                lbl.setOpaque(true);
                lbl.setBackground(COLOR_TABLE_HEADER);
                lbl.setForeground(Color.WHITE);
                lbl.setFont(new Font("SansSerif", Font.BOLD, 17));
                return lbl;
            }
        });

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setBackground(Color.WHITE);
        centerRenderer.setForeground(COLOR_TEXT_BROWN);

        for (int i = 0; i < columns.length; i++) {
            ordersTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        ordersTable.getColumnModel().getColumn(7).setCellRenderer((t, val, isS, hasF, row, col) -> {
            String estado = val == null ? "" : val.toString();
            JLabel lbl = new JLabel(estado, SwingConstants.CENTER);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 16));
            if (estado.equalsIgnoreCase("Entregado") || estado.equalsIgnoreCase("Completada")) {
                lbl.setForeground(COLOR_TEXT_GREEN);
            } else if (estado.toLowerCase(Locale.ROOT).contains("cancel")) {
                lbl.setForeground(COLOR_TEXT_RED);
            } else {
                lbl.setForeground(COLOR_TEXT_BROWN);
            }
            return lbl;
        });

        // El ID queda en el modelo para ejecutar acciones, pero no ocupa espacio en pantalla.
        ordersTable.removeColumn(ordersTable.getColumnModel().getColumn(0));
        ordersTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        ordersTable.getColumnModel().getColumn(1).setPreferredWidth(105);
        ordersTable.getColumnModel().getColumn(2).setPreferredWidth(70);
        ordersTable.getColumnModel().getColumn(3).setPreferredWidth(160);
        ordersTable.getColumnModel().getColumn(4).setPreferredWidth(250);
        ordersTable.getColumnModel().getColumn(5).setPreferredWidth(105);
        ordersTable.getColumnModel().getColumn(6).setPreferredWidth(125);
        ordersTable.getColumnModel().getColumn(7).setPreferredWidth(150);

        ordersTable.addMouseListener(new MouseAdapter() {
            private void showPopup(MouseEvent e) {
                if (!e.isPopupTrigger()) return;
                int viewRow = ordersTable.rowAtPoint(e.getPoint());
                if (viewRow < 0) return;
                ordersTable.setRowSelectionInterval(viewRow, viewRow);
                int modelRow = ordersTable.convertRowIndexToModel(viewRow);
                JPopupMenu menu = new JPopupMenu();

                JMenuItem details = new JMenuItem("Ver detalle");
                details.addActionListener(event -> mostrarDetallePedido(modelRow));
                menu.add(details);

                JMenuItem changeState = new JMenuItem("Cambiar estado");
                changeState.addActionListener(event -> cambiarEstadoPedido(modelRow));
                menu.add(changeState);
                menu.show(ordersTable, e.getX(), e.getY());
            }

            @Override
            public void mousePressed(MouseEvent e) {
                showPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                showPopup(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && !e.isPopupTrigger()) {
                    int viewRow = ordersTable.rowAtPoint(e.getPoint());
                    if (viewRow >= 0) {
                        mostrarDetallePedido(ordersTable.convertRowIndexToModel(viewRow));
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(ordersTable);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_TABLE_GRID, 1));
        scroll.getViewport().setBackground(Color.WHITE);

        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // Consulta pedidos activos o del historial y añade el criterio seleccionado al SQL.
    // Los parámetros se enlazan con PreparedStatement para evitar concatenar valores.
    private void loadOrders(boolean showErrors) {
        StringBuilder sql = new StringBuilder(
                "SELECT o.id_orden, DATE_FORMAT(o.fecha, '%d/%m/%Y') AS fecha, "
                + "TIME_FORMAT(o.hora, '%H:%i') AS hora, "
                + "COALESCE(CONCAT(u.nombre, ' ', u.apellido), 'Sin asignar') AS cajero, "
                + "COALESCE(det.productos, 'Sin productos') AS productos, o.total, o.estado, "
                + "COALESCE(pay.metodos, 'Sin pago') AS metodos "
                + "FROM orden o LEFT JOIN usuario u ON u.id_usuario = o.id_usuario "
                + "LEFT JOIN (SELECT d.id_orden, GROUP_CONCAT(CONCAT(d.cantidad, 'x ', p.nombre) "
                + "ORDER BY p.nombre SEPARATOR ', ') AS productos FROM detalle_orden d "
                + "JOIN producto p ON p.id_producto = d.id_producto GROUP BY d.id_orden) det "
                + "ON det.id_orden = o.id_orden "
                + "LEFT JOIN (SELECT id_orden, GROUP_CONCAT(DISTINCT metodo_pago SEPARATOR ', ') AS metodos "
                + "FROM pago_orden GROUP BY id_orden) pay ON pay.id_orden = o.id_orden WHERE ");
        List<Object> parameters = new ArrayList<>();

        if (selectedTab == 0) {
            sql.append("UPPER(TRIM(o.estado)) NOT IN ('ENTREGADO', 'CANCELADO', 'CANCELADA', 'COMPLETADA', 'COMPLETADO') ");
        } else {
            sql.append("UPPER(TRIM(o.estado)) IN ('ENTREGADO', 'CANCELADO', 'CANCELADA', 'COMPLETADA', 'COMPLETADO') ");
        }

        if (selectedCriterion == SearchCriterion.ORDER_ID) {
            String orderIdText = txtSearchValue.getText().trim().replace("#", "");
            try {
                parameters.add(Integer.parseInt(orderIdText));
            } catch (NumberFormatException e) {
                mostrarError("Escribe un número de pedido válido.");
                return;
            }
            sql.append("AND o.id_orden = ? ");
        } else if (selectedCriterion == SearchCriterion.DATE) {
            try {
                LocalDate date = LocalDate.parse(txtSearchValue.getText().trim(),
                        DateTimeFormatter.ofPattern("dd/MM/uuuu"));
                parameters.add(java.sql.Date.valueOf(date));
            } catch (DateTimeParseException e) {
                mostrarError("Escribe la fecha con el formato dd/MM/aaaa.");
                return;
            }
            sql.append("AND o.fecha = ? ");
        } else if (selectedCriterion == SearchCriterion.CASHIER) {
            String cashier = txtSearchValue.getText().trim();
            if (cashier.isEmpty()) {
                mostrarError("Escribe el nombre del cajero.");
                return;
            }
            parameters.add("%" + cashier + "%");
            sql.append("AND CONCAT(u.nombre, ' ', u.apellido) LIKE ? ");
        } else if (selectedCriterion == SearchCriterion.STATE) {
            String state = (String) cbSearchState.getSelectedItem();
            if (state != null && !state.equals("Todos")) {
                parameters.add(state);
                sql.append("AND UPPER(TRIM(o.estado)) = UPPER(?) ");
            }
        }
        sql.append("ORDER BY o.fecha DESC, o.hora DESC, o.id_orden DESC");

        try (Connection conn = new Conexion().getConnection()) {
            if (conn == null) {
                if (showErrors) mostrarError("No hay conexión con la base de datos.");
                return;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < parameters.size(); i++) {
                    Object value = parameters.get(i);
                    if (value instanceof Integer) {
                        ps.setInt(i + 1, (Integer) value);
                    } else if (value instanceof java.sql.Date) {
                        ps.setDate(i + 1, (java.sql.Date) value);
                    } else {
                        ps.setString(i + 1, value.toString());
                    }
                }
                try (ResultSet rs = ps.executeQuery()) {
                    ordersModel.setRowCount(0);
                    while (rs.next()) {
                        ordersModel.addRow(new Object[]{
                            rs.getInt("id_orden"),
                            "#" + rs.getInt("id_orden"),
                            rs.getString("fecha"),
                            rs.getString("hora"),
                            rs.getString("cajero"),
                            rs.getString("productos"),
                            String.format(Locale.US, "Q %,.2f", rs.getDouble("total")),
                            rs.getString("estado"),
                            rs.getString("metodos")
                        });
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error cargando pedidos: " + e.getMessage());
            if (showErrors) mostrarError("No se pudieron cargar los pedidos.\n" + e.getMessage());
        }
    }

    // Recupera los productos del pedido seleccionado y presenta un resumen detallado.
    private void mostrarDetallePedido(int modelRow) {
        int orderId = (Integer) ordersModel.getValueAt(modelRow, 0);
        StringBuilder details = new StringBuilder();
        details.append("Pedido ").append(ordersModel.getValueAt(modelRow, 1)).append('\n')
                .append("Fecha: ").append(ordersModel.getValueAt(modelRow, 2)).append("  ")
                .append(ordersModel.getValueAt(modelRow, 3)).append('\n')
                .append("Cajero: ").append(ordersModel.getValueAt(modelRow, 4)).append('\n')
                .append("Estado: ").append(ordersModel.getValueAt(modelRow, 7)).append('\n')
                .append("Pago: ").append(ordersModel.getValueAt(modelRow, 8)).append("\n\nProductos:\n");

        String sql = "SELECT p.nombre, d.cantidad, d.precio_unitario, d.subtotal "
                + "FROM detalle_orden d JOIN producto p ON p.id_producto = d.id_producto "
                + "WHERE d.id_orden = ? ORDER BY d.id_detalle";
        try (Connection conn = new Conexion().getConnection()) {
            if (conn == null) {
                mostrarError("No hay conexión con la base de datos.");
                return;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    boolean hasProducts = false;
                    while (rs.next()) {
                        hasProducts = true;
                        details.append(rs.getInt("cantidad")).append(" x ")
                                .append(rs.getString("nombre")).append("  Q")
                                .append(String.format(Locale.US, "%.2f", rs.getDouble("subtotal")))
                                .append('\n');
                    }
                    if (!hasProducts) details.append("Sin productos registrados.\n");
                }
            }
        } catch (SQLException e) {
            mostrarError("No se pudo cargar el detalle del pedido.\n" + e.getMessage());
            return;
        }

        details.append("\nTotal: ").append(ordersModel.getValueAt(modelRow, 6));
        JTextArea textArea = new JTextArea(details.toString(), 12, 42);
        textArea.setEditable(false);
        textArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea),
                "Detalle del pedido", JOptionPane.INFORMATION_MESSAGE);
    }

    // Pide un estado nuevo, actualiza la fila en MySQL y vuelve a cargar la lista.
    private void cambiarEstadoPedido(int modelRow) {
        int orderId = (Integer) ordersModel.getValueAt(modelRow, 0);
        String currentState = ordersModel.getValueAt(modelRow, 7).toString();
        String newState = (String) JOptionPane.showInputDialog(
                this,
                "Selecciona el nuevo estado:",
                "Cambiar estado del pedido",
                JOptionPane.PLAIN_MESSAGE,
                null,
                ORDER_STATES,
                currentState);
        if (newState == null || newState.equals(currentState)) return;

        String sql = "UPDATE orden SET estado = ? WHERE id_orden = ?";
        try (Connection conn = new Conexion().getConnection()) {
            if (conn == null) {
                mostrarError("No hay conexión con la base de datos.");
                return;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, newState);
                ps.setInt(2, orderId);
                if (ps.executeUpdate() == 0) {
                    mostrarError("El pedido ya no existe o no se pudo actualizar.");
                    return;
                }
            }
            loadOrders(true);
        } catch (SQLException e) {
            mostrarError("No se pudo actualizar el estado.\n" + e.getMessage());
        }
    }

    // Centraliza los avisos de validación y los errores de la pantalla.
    private void mostrarError(String message) {
        JOptionPane.showMessageDialog(this, message, "Gestión de pedidos", JOptionPane.WARNING_MESSAGE);
    }

    // --- CLASE DE ICONOS VECTORIALES PARA LA BARRA LATERAL (idéntica al resto) ---
    private static class SidebarVectorIcon implements Icon {
        public enum IconType { EMPLOYEES, MENU, ORDERS, REPORTS, CASH, SETTINGS, SECURITY }

        private final IconType type;
        private final int size;

        public SidebarVectorIcon(IconType type, int size) {
            this.type = type;
            this.size = size;
        }

        @Override
        public int getIconWidth() { return size; }

        @Override
        public int getIconHeight() { return size; }

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

        // --- PANEL REDONDEADO ---
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
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            gestionPedidos app = new gestionPedidos();
            app.setVisible(true);
        });
    }

}