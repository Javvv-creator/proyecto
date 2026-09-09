package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class gestionProductos extends JFrame {

    // Paleta de colores exacta
    private static final Color COLOR_BG = new Color(231, 221, 202);
    private static final Color COLOR_SIDEBAR = new Color(139, 94, 52);
    private static final Color COLOR_HEADER = new Color(139, 94, 52);
    private static final Color COLOR_SEARCH = new Color(106, 161, 46);
    private static final Color COLOR_BTN_NUEVO = new Color(243, 205, 59);
    private static final Color COLOR_TABLE_HEADER = new Color(92, 53, 22);
    private static final Color COLOR_TABLE_GRID = new Color(222, 210, 191);
    private static final Color COLOR_TEXT_GREEN = new Color(106, 161, 46);
    private static final Color COLOR_TEXT_RED = new Color(211, 53, 58);
    private static final Color COLOR_SIDEBAR_HOVER = new Color(160, 110, 65);
    private static final Color COLOR_SIDEBAR_ACTIVE = new Color(110, 72, 38);

    // Colores de las tarjetas de resumen inferiores
    private static final Color COLOR_CARD_PRODUCTOS = new Color(243, 205, 59);
    private static final Color COLOR_CARD_ACTIVOS = new Color(230, 115, 45);
    private static final Color COLOR_CARD_CATEGORIAS = new Color(106, 161, 46);
    private static final Color COLOR_CARD_COMBOS = new Color(211, 53, 58);

    private int selectedMenuIndex = 1; // "Gestión de menú / productos" seleccionado por defecto
    private JPanel[] menuButtons;
    private JLabel lblClock;
    private JLabel lblDate;

    // Componentes para la funcionalidad dinámica
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTextField txtSearch;
    private JComboBox<String> cbCategoria;
    private JComboBox<String> cbTurno;
    private JComboBox<String> cbEstado;
    private static final String PLACEHOLDER_TEXT = "Buscar producto";

    // Labels de las tarjetas de métricas
    private JLabel lblValProductos;
    private JLabel lblValActivos;
    private JLabel lblValCategorias;
    private JLabel lblValCombos;

    public gestionProductos() {
        setTitle("GIT & EAT! - Gestión de Menú / Productos");
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

        startLiveClock();
        cargarDatosEjemplo();
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
        logoCard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
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
                { SidebarVectorIcon.IconType.SETTINGS, "gui/images/settings.png",
                        "<html>Configuración<br>general</html>" },
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
                    } else if (index == 1) {
                        selectedMenuIndex = index;
                        updateSidebarSelection();
                        return;
                    }
                    JOptionPane.showMessageDialog(gestionProductos.this, "Sección en desarrollo.", "Información",
                            JOptionPane.INFORMATION_MESSAGE);
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
            btn.setBackgroundColor(i == selectedMenuIndex ? COLOR_SIDEBAR_ACTIVE : COLOR_SIDEBAR);
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

    private JPanel createHeaderPanel() {
        RoundedPanel header = new RoundedPanel(20, COLOR_HEADER);
        header.setLayout(new BorderLayout());
        header.setPreferredSize(new Dimension(0, 125));
        header.setBorder(new EmptyBorder(20, 35, 20, 35));

        // Panel Izquierdo: Icono + Título
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        titlePanel.setOpaque(false);

        JLabel iconHeader = createSidebarIconLabel(SidebarVectorIcon.IconType.MENU, "gui/images/menu.png");

        JLabel title = new JLabel("Gestión de menú / productos");
        title.setFont(new Font("SansSerif", Font.BOLD, 40));
        title.setForeground(Color.WHITE);

        titlePanel.add(iconHeader);
        titlePanel.add(title);
        header.add(titlePanel, BorderLayout.WEST);

        // Panel Derecho: Hora + Botón Refrescar
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

    private JPanel createMainBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(8, 6, 8, 6);

        // Fila 1: Barra de búsqueda unificada + Botón
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.08;
        body.add(createTopBar(), gbc);

        // Fila 2: Tabla de Productos
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.62;
        body.add(createTableCard(), gbc);

        // Fila 3: Tarjetas resumen inferiores
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.30;
        body.add(createSummaryCardsRow(), gbc);

        return body;
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new GridBagLayout());
        bar.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 15);
        gbc.weighty = 1.0;

        // 1. Barra Integrada Verde (Buscar | Categoría | Turno | Estado)
        RoundedPanel searchPill = new RoundedPanel(20, COLOR_SEARCH);
        searchPill.setLayout(new GridBagLayout());
        searchPill.setBorder(new EmptyBorder(4, 15, 4, 15));

        GridBagConstraints pGbc = new GridBagConstraints();
        pGbc.fill = GridBagConstraints.BOTH;
        pGbc.anchor = GridBagConstraints.CENTER; // Corregido: 'anchor' en lugar de 'centerY'

        // Ícono de Lupa
        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        searchIcon.setForeground(Color.WHITE);
        pGbc.gridx = 0;
        pGbc.weightx = 0.0;
        pGbc.insets = new Insets(0, 0, 0, 8);
        searchPill.add(searchIcon, pGbc);

        // TextField Búsqueda
        txtSearch = new JTextField(PLACEHOLDER_TEXT);
        txtSearch.setOpaque(false);
        txtSearch.setBorder(null);
        txtSearch.setForeground(new Color(230, 230, 230));
        txtSearch.setFont(new Font("SansSerif", Font.BOLD, 17));
        txtSearch.setCaretColor(Color.WHITE);

        txtSearch.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtSearch.getText().equals(PLACEHOLDER_TEXT)) {
                    txtSearch.setText("");
                    txtSearch.setForeground(Color.WHITE);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (txtSearch.getText().trim().isEmpty()) {
                    txtSearch.setText(PLACEHOLDER_TEXT);
                    txtSearch.setForeground(new Color(230, 230, 230));
                }
            }
        });

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilters();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilters();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilters();
            }
        });

        pGbc.gridx = 1;
        pGbc.weightx = 0.35;
        searchPill.add(txtSearch, pGbc);

        // Separador 1 "|"
        pGbc.gridx = 2;
        pGbc.weightx = 0.0;
        pGbc.insets = new Insets(0, 8, 0, 8);
        searchPill.add(createPipeLabel(), pGbc);

        // Combobox Categoría
        cbCategoria = createPillComboBox(new String[] { "Categoría", "Hamburguesas", "Combos", "Acompañantes" });
        pGbc.gridx = 3;
        pGbc.weightx = 0.20;
        pGbc.insets = new Insets(0, 0, 0, 0);
        searchPill.add(cbCategoria, pGbc);

        // Separador 2 "|"
        pGbc.gridx = 4;
        pGbc.weightx = 0.0;
        pGbc.insets = new Insets(0, 8, 0, 8);
        searchPill.add(createPipeLabel(), pGbc);

        // Combobox Turno
        cbTurno = createPillComboBox(new String[] { "Turno", "Mañana", "Tarde", "Ambos" });
        pGbc.gridx = 5;
        pGbc.weightx = 0.20;
        pGbc.insets = new Insets(0, 0, 0, 0);
        searchPill.add(cbTurno, pGbc);

        // Separador 3 "|"
        pGbc.gridx = 6;
        pGbc.weightx = 0.0;
        pGbc.insets = new Insets(0, 8, 0, 8);
        searchPill.add(createPipeLabel(), pGbc);

        // Combobox Estado
        cbEstado = createPillComboBox(new String[] { "Estado", "Activo", "Inactivo" });
        pGbc.gridx = 7;
        pGbc.weightx = 0.20;
        pGbc.insets = new Insets(0, 0, 0, 0);
        searchPill.add(cbEstado, pGbc);

        gbc.gridx = 0;
        gbc.weightx = 0.78;
        bar.add(searchPill, gbc);

        // 2. Botón + Nuevo producto (Amarillo)
        RoundedPanel btnPill = new RoundedPanel(20, COLOR_BTN_NUEVO);
        btnPill.setLayout(new GridBagLayout());
        btnPill.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblNuevo = new JLabel("+ Nuevo producto");
        lblNuevo.setForeground(Color.WHITE);
        lblNuevo.setFont(new Font("SansSerif", Font.BOLD, 18));
        btnPill.add(lblNuevo);

        btnPill.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(gestionProductos.this, "Abrir modal de nuevo producto.", "Información",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        gbc.gridx = 1;
        gbc.weightx = 0.22;
        gbc.insets = new Insets(0, 0, 0, 0);
        bar.add(btnPill, gbc);

        return bar;
    }

    private JLabel createPipeLabel() {
        JLabel lbl = new JLabel("|");
        lbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        lbl.setForeground(new Color(255, 255, 255, 180));
        return lbl;
    }

    private JComboBox<String> createPillComboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setOpaque(false);
        combo.setBackground(COLOR_SEARCH);
        combo.setForeground(Color.WHITE);
        combo.setFont(new Font("SansSerif", Font.BOLD, 17));
        combo.setBorder(BorderFactory.createEmptyBorder());
        combo.setFocusable(false);
        combo.addActionListener(e -> applyFilters());
        return combo;
    }

    private JPanel createTableCard() {
        RoundedPanel card = new RoundedPanel(20, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] columns = { "Producto", "Categoría", "Precio", "Turno", "Tipo", "Cantidad", "Estado" };

        model = new DefaultTableModel(columns, 0) {
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

        rowSorter = new TableRowSorter<>(model);
        table.setRowSorter(rowSorter);

        // Header de la Tabla
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

        for (int i = 0; i < 6; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Renderizado especial Columna Estado (Texto Verde / Rojo)
        table.getColumnModel().getColumn(6).setCellRenderer((t, val, isS, hasF, row, col) -> {
            String estado = (String) val;
            JLabel lbl = new JLabel(estado, SwingConstants.CENTER);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 16));
            if ("Activo".equalsIgnoreCase(estado)) {
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

    private void applyFilters() {
        String searchText = txtSearch.getText().trim();
        String selectedCat = (String) cbCategoria.getSelectedItem();
        String selectedTurno = (String) cbTurno.getSelectedItem();
        String selectedEstado = (String) cbEstado.getSelectedItem();

        List<RowFilter<DefaultTableModel, Object>> filters = new ArrayList<>();

        if (!searchText.isEmpty() && !searchText.equals(PLACEHOLDER_TEXT)) {
            filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(searchText), 0));
        }

        if (selectedCat != null && !selectedCat.equals("Categoría")) {
            filters.add(RowFilter.regexFilter("^" + Pattern.quote(selectedCat) + "$", 1));
        }

        if (selectedTurno != null && !selectedTurno.equals("Turno")) {
            filters.add(RowFilter.regexFilter("^" + Pattern.quote(selectedTurno) + "$", 3));
        }

        if (selectedEstado != null && !selectedEstado.equals("Estado")) {
            filters.add(RowFilter.regexFilter("^" + Pattern.quote(selectedEstado) + "$", 6));
        }

        if (filters.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    private JPanel createSummaryCardsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 15, 0));
        row.setOpaque(false);

        lblValProductos = new JLabel("2", SwingConstants.CENTER);
        lblValActivos = new JLabel("15", SwingConstants.CENTER);
        lblValCategorias = new JLabel("11", SwingConstants.CENTER);
        lblValCombos = new JLabel("4", SwingConstants.CENTER);

        row.add(createMetricCard("Productos", lblValProductos, COLOR_CARD_PRODUCTOS, Color.WHITE));
        row.add(createMetricCard("Activos", lblValActivos, COLOR_CARD_ACTIVOS, Color.WHITE));
        row.add(createMetricCard("Categorías", lblValCategorias, COLOR_CARD_CATEGORIAS, Color.WHITE));
        row.add(createMetricCard("Combos", lblValCombos, COLOR_CARD_COMBOS, Color.WHITE));

        return row;
    }

    private JPanel createMetricCard(String titleText, JLabel valLabel, Color bg, Color textColor) {
        RoundedPanel card = new RoundedPanel(20, bg);
        card.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 8, 0);

        JLabel title = new JLabel(titleText, SwingConstants.CENTER);
        title.setForeground(textColor);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        card.add(title, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        valLabel.setForeground(textColor);
        valLabel.setFont(new Font("SansSerif", Font.BOLD, 48));
        card.add(valLabel, gbc);

        return card;
    }

    private void cargarDatosEjemplo() {
        model.setRowCount(0);
        model.addRow(new Object[] { "Hamburguesa", "Hamburguesas", "Q35.00", "Mañana", "Producto", "500", "Activo" });
        model.addRow(new Object[] { "Combo Clásico", "Combos", "Q49", "Tarde", "Combo", "50", "Activo" });
        model.addRow(new Object[] { "Papas fritas", "Acompañantes", "Q18.00", "Ambos", "Producto", "89", "Activo" });
    }

    // --- CLASE DE ICONOS VECTORIALES ---
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
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            gestionProductos app = new gestionProductos();
            app.setVisible(true);
        });
    }
}