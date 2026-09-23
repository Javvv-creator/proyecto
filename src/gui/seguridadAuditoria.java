package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.net.URL;

/**
 * Pantalla de Seguridad y auditoría - GIT & EAT!
 * Muestra el diseño completo (sidebar, header, filtros, tarjetas de alerta y tabla).
 * La lógica real (filtrado, carga de datos, cierre de sesión) queda marcada con TODO
 * para que otro compañero la implemente conectando la base de datos / backend.
 */
public class seguridadAuditoria extends JFrame {

    // Paleta de colores (idéntica al resto de la aplicación)
    private static final Color COLOR_BG = new Color(231, 221, 202);
    private static final Color COLOR_SIDEBAR = new Color(139, 94, 52);
    private static final Color COLOR_HEADER = new Color(139, 94, 52);
    private static final Color COLOR_TEXT_BROWN = new Color(92, 53, 22);
    private static final Color COLOR_TABLE_HEADER = new Color(92, 53, 22);
    private static final Color COLOR_TABLE_GRID = new Color(222, 210, 191);
    private static final Color COLOR_SIDEBAR_HOVER = new Color(160, 110, 65);
    private static final Color COLOR_SIDEBAR_ACTIVE = new Color(110, 72, 38);
    private static final Color COLOR_BTN_NUEVO = new Color(243, 205, 59);
    private static final Color COLOR_CERRAR_SESION = new Color(211, 53, 58);

    // Colores de los campos con borde (Desde, Hasta, Usuarios, Tipo de acción)
    private static final Color COLOR_FIELD_BG = new Color(222, 210, 191);
    private static final Color COLOR_FIELD_BORDER = new Color(139, 94, 52);

    // Colores de las tarjetas de alerta
    private static final Color COLOR_CARD_CANCELACIONES = new Color(211, 53, 58);
    private static final Color COLOR_CARD_DIFERENCIAS = new Color(243, 205, 59);
    private static final Color COLOR_CARD_CAMBIOS = new Color(106, 161, 46);

    private int selectedMenuIndex = 6; // "Seguridad y auditoría" seleccionado por defecto en esta vista
    private JPanel[] menuButtons;

    public seguridadAuditoria() {
        setTitle("GIT & EAT! - Seguridad y Auditoría");
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
                    if (index == 6) {
                        // Ya estamos en la ventana de Seguridad y auditoría
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
                    // Las demás ventanas todavía no existen
                    JOptionPane.showMessageDialog(
                            seguridadAuditoria.this,
                            "Esta sección todavía está en desarrollo.",
                            "Próximamente",
                            JOptionPane.INFORMATION_MESSAGE
                    );
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
                // implementar el cierre de sesión real:
                //  - invalidar el token / sesión del usuario actual
                //  - limpiar cualquier dato sensible que se tenga en memoria
                //  - abrir la ventana de login y cerrar todas las ventanas abiertas
                // Por ahora solo se deja este mensaje de referencia:
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
    // --- VISTA PRINCIPAL (SEGURIDAD Y AUDITORÍA) ---
    // ==========================================
    private JPanel createHeaderPanel() {
        RoundedPanel header = new RoundedPanel(20, COLOR_HEADER);
        header.setLayout(new FlowLayout(FlowLayout.LEFT, 25, 25));
        header.setPreferredSize(new Dimension(0, 110));

        JLabel iconHeader = new JLabel(new SidebarVectorIcon(SidebarVectorIcon.IconType.SECURITY, 42));

        JLabel title = new JLabel("Seguridad y auditoría");
        title.setFont(new Font("SansSerif", Font.BOLD, 38));
        title.setForeground(Color.WHITE);

        header.add(iconHeader);
        header.add(title);

        return header;
    }

    private JPanel createMainBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(8, 6, 8, 6);

        // Fila 1: Filtros (Desde / Hasta / Usuarios / Tipo de acción / Filtrar)
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.weighty = 0.20;
        body.add(createFiltersPanel(), gbc);

        // Fila 2: Tarjetas de alerta (Cancelaciones / Diferencias de caja / Cambios de precio)
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 1.0; gbc.weighty = 0.22;
        body.add(createAlertCardsRow(), gbc);

        // Fila 3: Tabla de auditoría
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 1.0; gbc.weighty = 0.58;
        body.add(createTableCard(), gbc);

        return body;
    }

    // --- FILTROS ---
    private JPanel createFiltersPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Fila 1: Desde / Hasta
        JPanel row1 = new JPanel(new GridBagLayout());
        row1.setOpaque(false);
        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.fill = GridBagConstraints.HORIZONTAL;
        gbc1.insets = new Insets(0, 0, 0, 20);
        gbc1.weighty = 1.0;

        JLabel lblDesdeTxt = new JLabel("01/02/2026");
        // TODO (compañero): reemplazar por un selector de fecha real (JDatePicker o similar)
        //  y conectar su valor a la consulta de auditoría.
        JComponent campoDesde = createOutlinedField("📅", lblDesdeTxt, false);
        JPanel desdeGroup = createLabeledField("Desde:", campoDesde);

        JLabel lblHastaTxt = new JLabel("01/08/2026");
        // TODO (compañero): reemplazar por un selector de fecha real y conectar su valor.
        JComponent campoHasta = createOutlinedField("📅", lblHastaTxt, false);
        JPanel hastaGroup = createLabeledField("Hasta:", campoHasta);

        gbc1.gridx = 0; gbc1.weightx = 0.5;
        row1.add(desdeGroup, gbc1);
        gbc1.gridx = 1; gbc1.weightx = 0.5; gbc1.insets = new Insets(0, 0, 0, 0);
        row1.add(hastaGroup, gbc1);

        // Fila 2: Usuarios / Tipo de acción / Filtrar
        JPanel row2 = new JPanel(new GridBagLayout());
        row2.setOpaque(false);
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.fill = GridBagConstraints.HORIZONTAL;
        gbc2.insets = new Insets(0, 0, 0, 20);
        gbc2.weighty = 1.0;

        // TODO (compañero): cargar la lista real de usuarios desde la base de datos
        JComboBox<String> cbUsuarios = new FlatComboBox<>(new String[]{"Todos", "Administrador", "Cajero"});
        cbUsuarios.setFont(new Font("SansSerif", Font.BOLD, 15));
        cbUsuarios.setForeground(COLOR_TEXT_BROWN);
        JComponent campoUsuarios = createOutlinedField("👤", cbUsuarios, true);
        JPanel usuariosGroup = createLabeledField("Usuarios:", campoUsuarios);

        // TODO (compañero): cargar la lista real de tipos de acción (login, cancelación,
        //  cambio de precio, diferencia de caja, etc.) desde la base de datos
        JComboBox<String> cbTipoAccion = new FlatComboBox<>(new String[]{"Todas", "Cancelaciones", "Cambios de precio", "Diferencias de caja"});
        cbTipoAccion.setFont(new Font("SansSerif", Font.BOLD, 15));
        cbTipoAccion.setForeground(COLOR_TEXT_BROWN);
        JComponent campoTipoAccion = createOutlinedField("🎚️", cbTipoAccion, true);
        JPanel tipoAccionGroup = createLabeledField("Tipo de accion:", campoTipoAccion);

        // Botón Filtrar
        RoundedPanel btnFiltrar = new RoundedPanel(18, COLOR_BTN_NUEVO);
        btnFiltrar.setLayout(new GridBagLayout());
        btnFiltrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnFiltrar.setPreferredSize(new Dimension(0, 46));

        JLabel lblFiltrar = new JLabel("🔍  Filtrar");
        lblFiltrar.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblFiltrar.setForeground(Color.WHITE);
        btnFiltrar.add(lblFiltrar);

        btnFiltrar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // TODO (compañero): implementar la lógica real de filtrado:
                //  1. Leer los valores de lblDesdeTxt / lblHastaTxt / cbUsuarios / cbTipoAccion
                //  2. Consultar el historial de auditoría con esos filtros
                //  3. Volver a poblar el DefaultTableModel de la tabla con los resultados
                //  4. Actualizar los números de las tarjetas (Cancelaciones, Diferencias de
                //     caja, Cambios de precio) según los resultados filtrados
                System.out.println("Filtrar presionado (pendiente de implementar)");
            }
        });

        // Se envuelve el botón para alinear su altura con los demás campos (con label vacío arriba)
        JPanel filtrarGroup = new JPanel();
        filtrarGroup.setOpaque(false);
        filtrarGroup.setLayout(new BoxLayout(filtrarGroup, BoxLayout.Y_AXIS));
        JLabel spacerLabel = new JLabel(" ");
        spacerLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        spacerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnFiltrar.setAlignmentX(Component.LEFT_ALIGNMENT);
        filtrarGroup.add(spacerLabel);
        filtrarGroup.add(Box.createRigidArea(new Dimension(0, 6)));
        filtrarGroup.add(btnFiltrar);

        gbc2.gridx = 0; gbc2.weightx = 0.38;
        row2.add(usuariosGroup, gbc2);
        gbc2.gridx = 1; gbc2.weightx = 0.38;
        row2.add(tipoAccionGroup, gbc2);
        gbc2.gridx = 2; gbc2.weightx = 0.24; gbc2.insets = new Insets(0, 0, 0, 0);
        row2.add(filtrarGroup, gbc2);

        panel.add(row1);
        panel.add(Box.createRigidArea(new Dimension(0, 16)));
        panel.add(row2);

        return panel;
    }

    private JPanel createLabeledField(String labelText, JComponent field) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        label.setForeground(COLOR_TEXT_BROWN);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        field.setPreferredSize(new Dimension(0, 46));

        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 6)));
        panel.add(field);

        return panel;
    }

    private JComponent createOutlinedField(String iconEmoji, JComponent centerComponent, boolean showChevron) {
        RoundedOutlinePanel wrapper = new RoundedOutlinePanel(18, COLOR_FIELD_BG, COLOR_FIELD_BORDER, 1.6f);
        wrapper.setLayout(new BorderLayout(10, 0));
        wrapper.setBorder(new EmptyBorder(6, 16, 6, 16));

        JLabel icon = new JLabel(iconEmoji);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        wrapper.add(icon, BorderLayout.WEST);

        if (centerComponent instanceof JLabel) {
            ((JLabel) centerComponent).setFont(new Font("SansSerif", Font.BOLD, 15));
            ((JLabel) centerComponent).setForeground(COLOR_TEXT_BROWN);
        }
        centerComponent.setOpaque(false);
        wrapper.add(centerComponent, BorderLayout.CENTER);

        if (showChevron) {
            JLabel chevron = new JLabel("▾");
            chevron.setFont(new Font("SansSerif", Font.BOLD, 14));
            chevron.setForeground(COLOR_TEXT_BROWN);
            wrapper.add(chevron, BorderLayout.EAST);
        }

        return wrapper;
    }

    // --- TARJETAS DE ALERTA ---
    private JPanel createAlertCardsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 15, 0));
        row.setOpaque(false);

        // TODO (compañero): reemplazar estos valores fijos por los conteos reales
        //  obtenidos de la consulta de auditoría (ver botón "Filtrar").
        row.add(createAlertCard("cancel", "2", "Cancelaciones", COLOR_CARD_CANCELACIONES));
        row.add(createAlertCard("warning", "3", "<html>Diferencias de<br>caja</html>", COLOR_CARD_DIFERENCIAS));
        row.add(createAlertCard("swap", "1", "<html>Cambios de<br>precio</html>", COLOR_CARD_CAMBIOS));

        return row;
    }

    private JPanel createAlertCard(String iconType, String number, String labelHtml, Color bg) {
        RoundedPanel card = new RoundedPanel(20, bg);
        card.setLayout(new BorderLayout(18, 0));
        card.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel iconLbl = new JLabel(new AlertIcon(iconType, 54));
        card.add(iconLbl, BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel numberLbl = new JLabel(number);
        numberLbl.setFont(new Font("SansSerif", Font.BOLD, 36));
        numberLbl.setForeground(Color.BLACK);
        numberLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel textLbl = new JLabel(labelHtml);
        textLbl.setFont(new Font("SansSerif", Font.BOLD, 16));
        textLbl.setForeground(Color.BLACK);
        textLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(numberLbl);
        textPanel.add(textLbl);

        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    // --- TABLA DE AUDITORÍA ---
    private JPanel createTableCard() {
        RoundedPanel card = new RoundedPanel(20, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(0, 0, 0, 0));

        String[] columns = {"Fecha y hora:", "Usuario:", "Accion:", "Detalle:"};

        // TODO (compañero): poblar este modelo con los registros reales de auditoría
        //  (por ejemplo, resultado de una consulta SELECT al historial de acciones).
        //  Por ahora se deja vacío para que el diseño se vea igual al mockup.
        Object[][] data = {};

        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(model);
        table.setRowHeight(45);
        table.setShowGrid(false);
        table.setFont(new Font("SansSerif", Font.PLAIN, 15));

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 48));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = new JLabel(value.toString(), SwingConstants.LEFT);
                lbl.setBorder(new EmptyBorder(0, 20, 0, 0));
                lbl.setOpaque(true);
                lbl.setBackground(COLOR_TABLE_HEADER);
                lbl.setForeground(Color.WHITE);
                lbl.setFont(new Font("SansSerif", Font.BOLD, 16));
                return lbl;
            }
        });

        // Panel vacío (cuerpo de la tabla) con marca de agua de lupa, igual al mockup
        JPanel emptyBody = new WatermarkPanel();
        emptyBody.setBackground(COLOR_BG.brighter());
        emptyBody.setOpaque(true);

        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.add(header, BorderLayout.NORTH);
        tableContainer.add(emptyBody, BorderLayout.CENTER);

        card.add(tableContainer, BorderLayout.CENTER);
        return card;
    }

    // Panel decorativo que dibuja una lupa tenue de fondo cuando la tabla está vacía
    private static class WatermarkPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 42));
            g2.setColor(new Color(0, 0, 0, 25));
            g2.drawString("🔍", 25, getHeight() - 25);
            g2.dispose();
        }
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

    // --- Ícono decorativo para las tarjetas de alerta (círculo translúcido + símbolo) ---
    private static class AlertIcon implements Icon {
        private final String type; // "cancel", "warning", "swap"
        private final int size;

        AlertIcon(String type, int size) {
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
            g2.translate(x, y);

            g2.setColor(new Color(255, 255, 255, 70));
            g2.fillOval(0, 0, size, size);

            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int pad = size / 4;
            switch (type) {
                case "cancel":
                    g2.drawLine(pad, pad, size - pad, size - pad);
                    g2.drawLine(size - pad, pad, pad, size - pad);
                    break;
                case "warning":
                    int[] xs = {size / 2, pad, size - pad};
                    int[] ys = {pad, size - pad, size - pad};
                    g2.drawPolygon(xs, ys, 3);
                    g2.drawLine(size / 2, size / 2 - 4, size / 2, size / 2 + 4);
                    g2.fillOval(size / 2 - 2, size / 2 + 8, 4, 4);
                    break;
                case "swap":
                    int midTop = size / 2 - 6;
                    int midBottom = size / 2 + 6;
                    g2.drawLine(pad, midTop, size - pad, midTop);
                    g2.drawLine(size - pad - 8, midTop - 6, size - pad, midTop);
                    g2.drawLine(size - pad - 8, midTop + 6, size - pad, midTop);

                    g2.drawLine(pad, midBottom, size - pad, midBottom);
                    g2.drawLine(pad + 8, midBottom - 6, pad, midBottom);
                    g2.drawLine(pad + 8, midBottom + 6, pad, midBottom);
                    break;
                default:
                    break;
            }
            g2.dispose();
        }
    }

    // --- ComboBox plano (sin fondo ni flecha nativa) para usarse dentro de RoundedOutlinePanel ---
    private static class FlatComboBox<E> extends JComboBox<E> {
        FlatComboBox(E[] items) {
            super(items);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder());
            setFocusable(false);

            setUI(new BasicComboBoxUI() {
                @Override
                protected JButton createArrowButton() {
                    JButton btn = new JButton();
                    btn.setPreferredSize(new Dimension(0, 0));
                    btn.setBorder(BorderFactory.createEmptyBorder());
                    btn.setContentAreaFilled(false);
                    return btn;
                }

                @Override
                public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                    // Sin fondo propio: el contenedor (RoundedOutlinePanel) ya pinta el borde y relleno
                }
            });
        }
    }

    // --- Panel con relleno y borde redondeado (para los campos con contorno del mockup) ---
    private static class RoundedOutlinePanel extends JPanel {
        private final int radius;
        private final Color fill;
        private final Color border;
        private final float strokeWidth;

        RoundedOutlinePanel(int radius, Color fill, Color border, float strokeWidth) {
            this.radius = radius;
            this.fill = fill;
            this.border = border;
            this.strokeWidth = strokeWidth;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.setStroke(new BasicStroke(strokeWidth));
            g2.setColor(border);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
        }
    }

    // --- COMPONENTE DE PANEL REDONDEADO (con fondo mutable) ---
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
            seguridadAuditoria app = new seguridadAuditoria();
            app.setVisible(true);
        });
    }
}