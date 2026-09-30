package gui;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
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
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import main.Crud.crud;

// Ventana administrativa para consultar y administrar usuarios del sistema.
// Permite filtrar la lista, abrir acciones de empleado y ver sus totales.
public class gestionEmpleados extends JFrame {

    // Colores usados para mantener el mismo estilo en toda la pantalla.
    private static final Color COLOR_BG = new Color(231, 221, 202);
    private static final Color COLOR_SIDEBAR = new Color(139, 94, 52);
    private static final Color COLOR_HEADER = new Color(139, 94, 52);
    private static final Color COLOR_SEARCH = new Color(106, 161, 46);
    private static final Color COLOR_COMBO = new Color(230, 115, 45);
    private static final Color COLOR_BTN_NUEVO = new Color(243, 205, 59);
    private static final Color COLOR_TABLE_HEADER = new Color(92, 53, 22);
    private static final Color COLOR_TABLE_GRID = new Color(222, 210, 191);
    private static final Color COLOR_TEXT_GREEN = new Color(106, 161, 46);
    private static final Color COLOR_TEXT_RED = new Color(211, 53, 58);
    private static final Color COLOR_SIDEBAR_HOVER = new Color(160, 110, 65);
    private static final Color COLOR_SIDEBAR_ACTIVE = new Color(110, 72, 38);
    private static final Color COLOR_CERRAR_SESION = new Color(211, 53, 58);

    // Colores de las tarjetas de resumen inferiores
    private static final Color COLOR_CARD_ADMIN = new Color(243, 205, 59);
    private static final Color COLOR_CARD_CAJEROS = new Color(230, 115, 45);
    private static final Color COLOR_CARD_ACTIVOS = new Color(106, 161, 46);
    private static final Color COLOR_CARD_INACTIVOS = new Color(211, 53, 58);

    // Guarda qué sección está activa y los botones que forman el menú lateral.
    private int selectedMenuIndex = 0;
    private JPanel[] menuButtons;
    private JLabel lblClock;
    private JLabel lblDate;

    // Componentes que permiten buscar, filtrar y ordenar la tabla de empleados.
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTextField txtSearch;
    private JComboBox<String> cbRoles;
    private static final String PLACEHOLDER_TEXT = "Buscar por nombre o código";

    // Etiquetas actualizadas con los totales calculados desde los empleados cargados.
    private JLabel lblValAdmin;
    private JLabel lblValCajeros;
    private JLabel lblValActivos;
    private JLabel lblValInactivos;

    // Construye la ventana y distribuye el menú, el encabezado y el contenido.
    // Al final inicia el reloj y carga desde la base de datos la tabla y sus métricas.
    public gestionEmpleados() {
        setTitle("GIT & EAT! - Gestión de Empleados");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1280, 720));

        JPanel mainContainer = new JPanel(new BorderLayout(15, 15));
        mainContainer.setBackground(COLOR_BG);
        mainContainer.setBorder(new EmptyBorder(15, 15, 15, 15));

        // La barra lateral ocupa el lado izquierdo y el contenido el espacio restante.
        mainContainer.add(createSidebarPanel(), BorderLayout.WEST);

        // El panel central agrupa el encabezado y el cuerpo de la pantalla.
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setOpaque(false);

        contentPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        contentPanel.add(createMainBody(), BorderLayout.CENTER);

        mainContainer.add(contentPanel, BorderLayout.CENTER);
        add(mainContainer);

        // ESC abre una nueva instancia del dashboard y cierra esta ventana.
        getRootPane().registerKeyboardAction(
                e -> {
                    dashboardAdmin app = new dashboardAdmin();
                    app.setVisible(true);
                    dispose();
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        startLiveClock();
        cargarEmpleadosDesdeBD(); // Carga usuarios reales de la BD (ya calcula los totales)
    }

    // ==========================================
    // --- BARRA LATERAL (idéntica al resto + botón Cerrar Sesión) ---
    // ==========================================
    // Crea el menú lateral; cada opción abre su pantalla correspondiente.
    // El logo regresa al dashboard y el botón inferior permite cerrar la sesión.
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
                // El logo sirve como acceso rápido para volver al dashboard.
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

        // Cada fila define el icono, el recurso gráfico y el texto de una opción.
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
                    // El efecto hover no debe ocultar el color del botón seleccionado.
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

                    // Cada índice corresponde a una opción del arreglo items.
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

                    } else if (index == 2) {
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
                }
            });

            menuButtons[i] = btnPanel;
            menuPanel.add(btnPanel);
        }

        centerWrapper.add(menuPanel, BorderLayout.NORTH);

        // Pide confirmación antes de cerrar las ventanas y volver al login.
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
                    // Solo vuelve al login si el usuario confirma la salida.
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
        // Primero intenta cargar el icono desde las imágenes del proyecto.
        // Si el archivo no existe, usa una versión dibujada con Java.
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
        // Pinta la opción activa con otro color y deja el resto en su color normal.
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
        // Busca el logo en las rutas disponibles y lo escala sin deformarlo.
        // Si no encuentra la imagen, muestra un logotipo de texto como alternativa.
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
        // Crea la franja superior y coloca el título de la sección actual.
        RoundedPanel header = new RoundedPanel(20, COLOR_HEADER);
        header.setLayout(new BorderLayout());
        header.setPreferredSize(new Dimension(0, 125));
        header.setBorder(new EmptyBorder(20, 35, 20, 35));

        JLabel title = new JLabel("Gestión de empleados");
        title.setFont(new Font("SansSerif", Font.BOLD, 40));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        rightHeader.setOpaque(false);

        return header;
    }

    private void startLiveClock() {
        // El temporizador vuelve a calcular la hora y la fecha cada segundo.
        // Los valores se escriben en las etiquetas del encabezado.
        Timer timer = new Timer(1000, e -> {
            Date now = new Date();
            SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm:ss a");
            SimpleDateFormat sdfDate = new SimpleDateFormat("EEEE, d 'de' MMMM", Locale.of("es", "ES"));
            lblClock.setText(sdfTime.format(now));
            lblDate.setText(sdfDate.format(now));
        });
        timer.start();
    }

    private JPanel createMainBody() {
        // Divide el área principal en controles, tabla de empleados y métricas.
        // Los pesos indican cuánto espacio recibe cada fila al cambiar el tamaño.
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(8, 6, 8, 6);

        // Fila 1: Controles de búsqueda y filtros
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.08;
        body.add(createTopBar(), gbc);

        // Fila 2: Tabla de Empleados
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
        // Reúne la búsqueda por texto, el filtro por rol y el botón de alta.
        // Los cambios en búsqueda o rol se aplican a la tabla inmediatamente.
        JPanel bar = new JPanel(new GridBagLayout());
        bar.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 15);
        gbc.weighty = 1.0;

        // 1. Campo Búsqueda (Verde) con Placeholder Real
        RoundedPanel searchPill = new RoundedPanel(20, COLOR_SEARCH);
        searchPill.setLayout(new BorderLayout(10, 0));
        searchPill.setBorder(new EmptyBorder(8, 15, 8, 15));

        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        searchIcon.setForeground(Color.WHITE);

        txtSearch = new JTextField(PLACEHOLDER_TEXT);
        txtSearch.setOpaque(false);
        txtSearch.setBorder(null);
        txtSearch.setForeground(new Color(230, 230, 230)); // Tono traslúcido para placeholder
        txtSearch.setFont(new Font("SansSerif", Font.BOLD, 18));
        txtSearch.setCaretColor(Color.WHITE);

        // El texto de ayuda desaparece al entrar al campo y vuelve si queda vacío.
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

        // Cada cambio de texto actualiza los filtros sin necesitar otro botón.
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

        searchPill.add(searchIcon, BorderLayout.WEST);
        searchPill.add(txtSearch, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.weightx = 0.45;
        bar.add(searchPill, gbc);

        // El rol seleccionado se combina con el texto de búsqueda.
        RoundedPanel comboPill = new RoundedPanel(20, COLOR_COMBO);
        comboPill.setLayout(new BorderLayout());
        comboPill.setBorder(new EmptyBorder(5, 15, 5, 15));

        cbRoles = new JComboBox<>(new String[] { "Todos los roles", "Administrador", "Cajero" });
        cbRoles.setOpaque(false);
        cbRoles.setBackground(COLOR_COMBO);
        cbRoles.setForeground(Color.BLACK);
        cbRoles.setFont(new Font("SansSerif", Font.BOLD, 18));
        cbRoles.setBorder(BorderFactory.createEmptyBorder());
        cbRoles.setFocusable(false);
        cbRoles.addActionListener(e -> applyFilters());

        comboPill.add(cbRoles, BorderLayout.CENTER);

        gbc.gridx = 1;
        gbc.weightx = 0.30;
        bar.add(comboPill, gbc);

        // Abre el formulario de alta y recarga los datos cuando se guarda.
        RoundedPanel btnPill = new RoundedPanel(20, COLOR_BTN_NUEVO);
        btnPill.setLayout(new GridBagLayout());
        btnPill.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblNuevo = new JLabel("+ Nuevo empleado");
        lblNuevo.setForeground(Color.WHITE);
        lblNuevo.setFont(new Font("SansSerif", Font.BOLD, 18));
        btnPill.add(lblNuevo);

        btnPill.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                nuevoEmpleadoDialog modal = new nuevoEmpleadoDialog(gestionEmpleados.this);
                modal.setVisible(true);

                if (modal.isGuardado()) {
                    // El diálogo ya insertó el usuario en la BD.
                    // Solo refrescamos la tabla con los datos reales.
                    cargarEmpleadosDesdeBD();
                }
            }
        });

        gbc.gridx = 2;
        gbc.weightx = 0.25;
        gbc.insets = new Insets(0, 0, 0, 0);
        bar.add(btnPill, gbc);

        return bar;
    }

    private JPanel createTableCard() {
        // Presenta los empleados en una tabla ordenable y no editable directamente.
        // La columna ID se conserva en el modelo para identificar cada registro.
        RoundedPanel card = new RoundedPanel(20, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] columns = { "Empleado", "Código", "Rol", "Estado", "Acciones", "ID" };

        // El modelo guarda los datos; la vista solo muestra las columnas necesarias.
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

        // El sorter aplica ordenamiento y filtros sin cambiar el modelo original.
        rowSorter = new TableRowSorter<>(model);
        table.setRowSorter(rowSorter);

        // Personaliza el encabezado para distinguirlo de las filas de datos.
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

        for (int i = 0; i < 3; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Colorea el estado para distinguir rápidamente empleados activos e inactivos.
        table.getColumnModel().getColumn(3).setCellRenderer((t, val, isS, hasF, row, col) -> {
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

        // Esta columna dibuja el acceso visual al menú de acciones de cada fila.
        table.getColumnModel().getColumn(4).setCellRenderer((t, val, isS, hasF, row, col) -> {
            JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
            actionsPanel.setBackground(Color.WHITE);

            // Botón redondeado de opciones "⚙ Opciones ▾"
            RoundedPanel btnPill = new RoundedPanel(12, new Color(248, 244, 237));
            btnPill.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
            btnPill.setPreferredSize(new Dimension(130, 36));

            JLabel lblAccion = new JLabel("⚙ Opciones ▾");
            lblAccion.setFont(new Font("SansSerif", Font.BOLD, 14));
            lblAccion.setForeground(COLOR_TABLE_HEADER);

            btnPill.add(lblAccion);
            actionsPanel.add(btnPill);
            return actionsPanel;
        });

        // Oculta la columna ID de la vista (sigue existiendo en el modelo)
        table.removeColumn(table.getColumnModel().getColumn(5));

        // Un clic en "Acciones" abre las opciones del empleado seleccionado.
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int viewRow = table.rowAtPoint(e.getPoint());
                int viewCol = table.columnAtPoint(e.getPoint());

                if (viewRow < 0 || viewCol != 4) {
                    return; // clic fuera de la columna Acciones
                }

                // La fila visible puede estar filtrada; se convierte al índice del modelo
                // para obtener el ID correcto antes de abrir cualquier diálogo.
                int modelRow = table.convertRowIndexToModel(viewRow);
                Integer idUsuario = (Integer) model.getValueAt(modelRow, 5);
                if (idUsuario == null) {
                    return;
                }

                JPopupMenu menu = new JPopupMenu();

                // Editar abre el formulario y actualiza la tabla al guardar cambios.
                JMenuItem itemEditar = new JMenuItem("✏ Editar datos");
                itemEditar.addActionListener(ev -> {
                    editarEmpleadoDialog dialog = new editarEmpleadoDialog(gestionEmpleados.this, idUsuario);
                    dialog.setVisible(true);
                    if (dialog.isGuardado()) {
                        cargarEmpleadosDesdeBD(); // refresca tabla y métricas
                        JOptionPane.showMessageDialog(gestionEmpleados.this,
                                "Empleado actualizado correctamente.", "Éxito",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                });

                // Restablecer contraseña usa el mismo ID para identificar al empleado.
                JMenuItem itemPass = new JMenuItem("🔒 Restablecer contraseña");
                itemPass.addActionListener(ev -> {
                    restablecerContraseñaDialog dialog = new restablecerContraseñaDialog(gestionEmpleados.this,
                            idUsuario);
                    dialog.setVisible(true);
                    if (dialog.isConfirmado()) {
                        JOptionPane.showMessageDialog(gestionEmpleados.this,
                                "Contraseña actualizada correctamente.", "Éxito",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                });

                menu.add(itemEditar);
                menu.add(itemPass);
                menu.show(table, e.getX(), e.getY());
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_TABLE_GRID, 1));
        scroll.getViewport().setBackground(Color.WHITE);

        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // Método para aplicar los filtros combinados de búsqueda y rol
    private void applyFilters() {
        // El placeholder es solo texto de ayuda y no debe filtrar resultados.
        String searchText = txtSearch.getText().trim();
        String selectedRole = (String) cbRoles.getSelectedItem();

        List<RowFilter<DefaultTableModel, Object>> filters = new ArrayList<>();

        // Si hay texto de búsqueda, se compara con el nombre y el código del empleado.
        if (!searchText.isEmpty() && !searchText.equals(PLACEHOLDER_TEXT)) {
            // Pattern.quote trata los caracteres escritos como texto normal, no como regex.
            filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(searchText), 0, 1));
        }

        // El filtro de rol exige una coincidencia exacta en la columna correspondiente.
        if (selectedRole != null && !selectedRole.equals("Todos los roles")) {
            filters.add(RowFilter.regexFilter("^" + Pattern.quote(selectedRole) + "$", 2));
        }

        // Si no hay criterios, muestra todas las filas; de lo contrario exige que
        // coincidan tanto la búsqueda como el rol seleccionado.
        if (filters.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    private JPanel createSummaryCardsRow() {
        // Prepara una tarjeta para cada total: administradores, cajeros y estados.
        JPanel row = new JPanel(new GridLayout(1, 4, 15, 0));
        row.setOpaque(false);

        lblValAdmin = new JLabel("0", SwingConstants.CENTER);
        lblValCajeros = new JLabel("0", SwingConstants.CENTER);
        lblValActivos = new JLabel("0", SwingConstants.CENTER);
        lblValInactivos = new JLabel("0", SwingConstants.CENTER);

        row.add(createMetricCard("Administradores", lblValAdmin, COLOR_CARD_ADMIN, Color.WHITE));
        row.add(createMetricCard("Cajeros", lblValCajeros, COLOR_CARD_CAJEROS, Color.WHITE));
        row.add(createMetricCard("Cajeros Activos", lblValActivos, COLOR_CARD_ACTIVOS, Color.WHITE));
        row.add(createMetricCard("Cajeros Inactivos", lblValInactivos, COLOR_CARD_INACTIVOS, Color.WHITE));

        return row;
    }

    private JPanel createMetricCard(String titleText, JLabel valLabel, Color bg, Color textColor) {
        // Arma una tarjeta reutilizable con título y valor centrados.
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

    // Cuenta los roles y estados recorriendo las filas cargadas en el modelo.
    // Actualiza las etiquetas de las tarjetas con los nuevos totales.
    private void updateSummaryMetrics() {
        int admins = 0;
        int cajeros = 0;
        int cajerosActivos = 0;
        int cajerosInactivos = 0;

        // Se recorre el modelo completo; el filtro visual no cambia estos totales.
        for (int i = 0; i < model.getRowCount(); i++) {
            String rol = (String) model.getValueAt(i, 2);
            String estado = (String) model.getValueAt(i, 3);

            if ("Administrador".equalsIgnoreCase(rol)) {
                admins++;
            } else if ("Cajero".equalsIgnoreCase(rol)) {
                cajeros++;
                if ("Activo".equalsIgnoreCase(estado)) {
                    cajerosActivos++;
                } else {
                    cajerosInactivos++;
                }
            }
        }

        lblValAdmin.setText(String.valueOf(admins));
        lblValCajeros.setText(String.valueOf(cajeros));
        lblValActivos.setText(String.valueOf(cajerosActivos));
        lblValInactivos.setText(String.valueOf(cajerosInactivos));
    }

    // Pide los usuarios al CRUD, reemplaza las filas actuales y recalcula métricas.
    private void cargarEmpleadosDesdeBD() {
        // El CRUD devuelve filas listas para la tabla, incluido el ID oculto.
        List<Object[]> filas = crud.listarUsuariosTabla();
        model.setRowCount(0); // limpia cualquier dato previo (incluye datos de ejemplo)
        for (Object[] fila : filas) {
            model.addRow(fila);
        }
        updateSummaryMetrics();
    }

    // Dibuja iconos sencillos cuando no se encuentra la imagen correspondiente.
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
            // Ajusta el dibujo al tamaño solicitado y elige la forma por tipo de icono.
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            g2.setColor(Color.WHITE);

            float scale = size / 32.0f;
            g2.scale(scale, scale);

            // Cada caso dibuja una figura distinta para representar una sección del menú.
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

    // Panel reutilizable que pinta un fondo con esquinas redondeadas.
    private static class RoundedPanel extends JPanel {

        private final int cornerRadius;
        private Color backgroundColor;

        // Guarda el radio y color usados al dibujar este panel.
        public RoundedPanel(int radius, Color bgColor) {
            this.cornerRadius = radius;
            this.backgroundColor = bgColor;
            setOpaque(false);
        }

        public void setBackgroundColor(Color bgColor) {
            // Permite cambiar el color del panel, por ejemplo al pasar el cursor.
            this.backgroundColor = bgColor;
        }

        @Override
        protected void paintComponent(Graphics g) {
            // Dibuja el fondo antes de que Swing pinte los componentes internos.
            super.paintComponent(g);
            Graphics2D graphics = (Graphics2D) g;
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(backgroundColor);
            graphics.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
        }
    }

    // Punto de entrada para probar la pantalla de empleados de forma independiente.
    public static void main(String[] args) {
        try {
            // Usa el estilo visual del sistema operativo si está disponible.
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            // Swing crea y muestra la ventana en su hilo de eventos.
            gestionEmpleados app = new gestionEmpleados();
            app.setVisible(true);
        });
    }
}
