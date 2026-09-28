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

/**
 * Pantalla de Gestión de pedidos - GIT & EAT!
 * Mantiene el mismo sidebar/header/paleta del resto de la aplicación.
 * La lógica real (búsqueda, alternar Tiempo real / Historial, limpiar filtros)
 * queda marcada con TODO para que otro compañero la conecte al backend.
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
                // TODO (compañero): implementar el cierre de sesión real:
                //  - invalidar el token / sesión del usuario actual
                //  - limpiar cualquier dato sensible que se tenga en memoria
                //  - abrir la ventana de login y cerrar todas las ventanas abiertas
                System.out.println("Cerrar sesión presionado (pendiente de implementar)");
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
                // TODO (compañero): cambiar la fuente de datos de la tabla a los
                //  pedidos EN VIVO (por ejemplo, con un listener/polling al backend)
                //  en lugar del historial estático que se muestra ahora.
            }
        });

        tabHistorial.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedTab = 1;
                updateTabsSelection();
                // TODO (compañero): cambiar la fuente de datos de la tabla al
                //  HISTORIAL completo de pedidos (consulta con filtros de fecha, etc.)
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
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panel.setOpaque(false);

        TitledBorder titled = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_TEXT_BROWN, 2, true),
                "Buscar por:"
        );
        titled.setTitleFont(new Font("SansSerif", Font.BOLD, 16));
        titled.setTitleColor(COLOR_TEXT_BROWN);
        panel.setBorder(BorderFactory.createCompoundBorder(titled, new EmptyBorder(6, 15, 12, 15)));

        // TODO (compañero): al hacer clic en cada chip, mostrar el campo de entrada
        //  correspondiente (número de pedido, selector de fecha, selector de cajero
        //  o selector de estado) para capturar el valor de búsqueda.
        panel.add(createSearchChip("No. Pedido"));
        panel.add(createSearchChip("Fecha"));
        panel.add(createSearchChip("Cajero"));
        panel.add(createSearchChip("Estado"));

        RoundedPanel btnBuscar = new RoundedPanel(20, COLOR_BTN_BUSCAR);
        btnBuscar.setLayout(new GridBagLayout());
        btnBuscar.setPreferredSize(new Dimension(140, 50));
        btnBuscar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblBuscar = new JLabel("Buscar");
        lblBuscar.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblBuscar.setForeground(Color.WHITE);
        btnBuscar.add(lblBuscar);

        btnBuscar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // TODO (compañero): tomar los criterios seleccionados (chips activos +
                //  sus valores) y consultar los pedidos reales; luego repoblar el
                //  DefaultTableModel de la tabla con los resultados.
                System.out.println("Buscar presionado (pendiente de implementar)");
            }
        });
        panel.add(btnBuscar);

        JLabel lblLimpiar = new JLabel("Limpiar");
        lblLimpiar.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblLimpiar.setForeground(COLOR_LIMPIAR);
        lblLimpiar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblLimpiar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // TODO (compañero): limpiar los criterios de búsqueda seleccionados
                //  y volver a mostrar el listado completo (sin filtros).
                System.out.println("Limpiar presionado (pendiente de implementar)");
            }
        });
        panel.add(lblLimpiar);

        return panel;
    }

    private RoundedPanel createSearchChip(String text) {
        RoundedPanel chip = new RoundedPanel(18, COLOR_SEARCH_CHIP);
        chip.setLayout(new GridBagLayout());
        chip.setPreferredSize(new Dimension(140, 50));
        chip.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        lbl.setForeground(Color.WHITE);
        chip.add(lbl);

        chip.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // TODO (compañero): marcar este chip como criterio de búsqueda activo
                //  y mostrar el control de captura correspondiente (texto, fecha, etc.)
                System.out.println("Filtro '" + text + "' presionado (pendiente de implementar)");
            }
        });

        return chip;
    }

    // --- TABLA DE PEDIDOS ---
    private JPanel createTableCard() {
        RoundedPanel card = new RoundedPanel(20, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] columns = {"Pedido", "Fecha", "Hora", "Cajero", "Productos", "Total", "Estado", "Entrega"};

        Object[][] data = {
            {"#1040", "09/09/26", "15:20", "Sofía", "Combo clásico", "Q35.00", "Entregado", "15:36"},
            {"#1039", "09/09/26", "15:05", "Ana", "2x Hamburguesa", "Q50.00", "Entregado", "15:18"},
            {"#1038", "09/09/26", "14:52", "Sofía", "Nuggets + bebida", "Q28.00", "Cancelado", "-"},
            {"#1037", "09/09/26", "14:40", "Carlos", "Combo familiar", "Q65.00", "Entregado", "14:55"}
        };

        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(55);
        table.setShowGrid(true);
        table.setGridColor(COLOR_TABLE_GRID);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setFont(new Font("SansSerif", Font.PLAIN, 16));
        table.setBackground(Color.WHITE);

        JTableHeader header = table.getTableHeader();
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
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        table.getColumnModel().getColumn(6).setCellRenderer((t, val, isS, hasF, row, col) -> {
            String estado = (String) val;
            JLabel lbl = new JLabel(estado, SwingConstants.CENTER);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 16));
            if ("Entregado".equalsIgnoreCase(estado)) {
                lbl.setForeground(COLOR_TEXT_GREEN);
            } else {
                lbl.setForeground(COLOR_TEXT_RED);
            }
            return lbl;
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_TABLE_GRID, 1));
        scroll.getViewport().setBackground(Color.WHITE);

        card.add(scroll, BorderLayout.CENTER);
        return card;
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