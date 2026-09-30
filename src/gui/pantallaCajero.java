package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.*;
import java.util.List;
import main.Conexion.Conexion;
import main.Crud.crud;

/**
 * MenuPOS - Interfaz de punto de venta para restaurante de hamburguesas.
 * Barra de categorías, grilla de productos y panel de orden actual.
 *
 * - El menú se lee de la base de datos GITEAT según el turno vigente (Mañana / Tarde).
 * - Las hamburguesas se pueden personalizar (ingredientes, extras, nota).
 * - Al pagar (efectivo, tarjeta o mixto, con cupones) la venta se guarda con VentaDAO
 *   y se registra automáticamente en la caja (gestionCaja).
 */
public class pantallaCajero extends JFrame {

    // ---------- Paleta de colores ----------
    static final Color BG_BEIGE = new Color(0xE8, 0xD3, 0xAE);
    static final Color BG_BEIGE_LIGHT = new Color(0xEE, 0xDD, 0xC0);
    static final Color CARD_BG = new Color(0xF1, 0xE9, 0xDC);
    static final Color CARD_IMG_BG = new Color(0xF7, 0xF2, 0xE9);
    static final Color TAB_INACTIVE = new Color(0xF4, 0xEE, 0xE3);
    static final Color TAB_ACTIVE = new Color(0xE0, 0x1C, 0x4D);
    static final Color BROWN_DARK = new Color(0x4A, 0x2E, 0x1C);
    static final Color BROWN_TEXT = new Color(0x5A, 0x38, 0x22);
    static final Color YELLOW_TEXT = new Color(0xE7, 0xA6, 0x2E);
    static final Color YELLOW_BRIGHT = new Color(0xF2, 0xC9, 0x4C);
    static final Color PANEL_DARK = new Color(0x3B, 0x24, 0x16);
    static final Color WHITE_TEXT = new Color(0xF7, 0xF2, 0xE9);

    private final CardsGridPanel gridPanel;
    private final OrderPanel orderPanel;
    private final JLabel sectionTitle;
    private final Map<String, JButton> tabButtons = new LinkedHashMap<>();

    // ---------- Cajero que tiene la sesión abierta ----------
    private final int idUsuarioCajero;
    private final String nombreCajero;
    private final String turnoCajero;

    // ---------- Menú por horario ----------
    private int turnoActual = -1;                 // id_turno del menú que se está mostrando
    private String categoriaActual = "Todos";     // pestaña seleccionada
    private final JLabel menuLabel = new JLabel(" ");

    // ---------- Datos ----------
    static class MenuItem {

        final int idProducto; // -1 para las tarjetas de encabezado de sección (no son productos reales)
        final String name;
        final double price;
        final String category;
        final String emoji;
        final String imagePath;

        /** Producto real, cargado desde la base de datos. */
        MenuItem(int idProducto, String name, double price, String category, String imagenArchivo) {
            this.idProducto = idProducto;
            this.name = name;
            this.price = price;
            this.category = category;
            this.emoji = "Bebidas".equals(category) ? "🥤" : "🍔";
            if (idProducto < 0) {
                this.imagePath = null; // los encabezados no llevan imagen
            } else if (imagenArchivo != null && !imagenArchivo.isBlank()) {
                this.imagePath = "/gui/images/" + imagenArchivo;
            } else {
                this.imagePath = rutaDesdeNombre(name); // sin imagen en BD: se genera desde el nombre
            }
        }

        /** Tarjeta de encabezado de sección (ej. "Desayunos", "Sodas"); no es un producto comprable. */
        static MenuItem encabezado(String texto, String category) {
            return new MenuItem(-1, texto, 0.0, category, null);
        }

        boolean esEncabezado() {
            return idProducto < 0;
        }

        private static String rutaDesdeNombre(String name) {
            return "/gui/images/" + name.toLowerCase()
                    .replace(" ", "_")
                    .replace("á", "a").replace("é", "e").replace("í", "i")
                    .replace("ó", "o").replace("ú", "u").replace("ñ", "n")
                    + ".png";
        }
    }

    private final Map<String, List<MenuItem>> catalog = new LinkedHashMap<>();

    /** Abre la pantalla usando un cajero específico (por ejemplo, tras un login real). */
    public pantallaCajero(int idUsuarioCajero, String nombreCajero, String turnoCajero) {
        super("Pantalla - Cajero");
        this.idUsuarioCajero = idUsuarioCajero;
        this.nombreCajero = nombreCajero;
        this.turnoCajero = turnoCajero == null ? "" : turnoCajero;

        buildCatalog();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true); // pantalla completa sin bordes de ventana
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel root = new BackgroundPanel();
        root.setLayout(new BorderLayout());
        setContentPane(root);

        // ----- Panel izquierdo (categorías + grilla) -----
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        leftPanel.setBorder(new EmptyBorder(30, 40, 30, 20));

        JPanel tabsBar = buildTabsBar();

        // Etiqueta que indica qué menú está activo (Mañana / Tarde)
        RoundedPanel menuPill = new RoundedPanel(PANEL_DARK, 30);
        menuPill.setLayout(new BorderLayout());
        menuPill.setBorder(new EmptyBorder(12, 22, 12, 22));
        menuLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        menuLabel.setForeground(YELLOW_BRIGHT);
        menuPill.add(menuLabel, BorderLayout.CENTER);
        JPanel pillWrap = new JPanel(new GridBagLayout());
        pillWrap.setOpaque(false);
        pillWrap.add(menuPill);
        pillWrap.setBorder(new EmptyBorder(0, 20, 0, 0));

        leftPanel.add(tabsBar, BorderLayout.NORTH);

        JPanel centerArea = new JPanel(new BorderLayout());
        centerArea.setOpaque(false);
        centerArea.setBorder(new EmptyBorder(25, 0, 0, 0));

        sectionTitle = new JLabel("Todos los productos");
        sectionTitle.setFont(new Font("SansSerif", Font.BOLD, 34));
        sectionTitle.setForeground(BROWN_DARK);

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.add(sectionTitle, BorderLayout.WEST);
        JSeparator sep = new JSeparator();
        sep.setForeground(BROWN_TEXT);
        JPanel sepWrap = new JPanel(new BorderLayout());
        sepWrap.setOpaque(false);
        sepWrap.setBorder(new EmptyBorder(20, 20, 0, 0));
        sepWrap.add(sep, BorderLayout.CENTER);
        titleRow.add(sepWrap, BorderLayout.CENTER);
        titleRow.add(pillWrap, BorderLayout.EAST);   // etiqueta del menú activo

        centerArea.add(titleRow, BorderLayout.NORTH);

        gridPanel = new CardsGridPanel();
        JScrollPane scroll = new JScrollPane(gridPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        centerArea.add(scroll, BorderLayout.CENTER);

        leftPanel.add(centerArea, BorderLayout.CENTER);
        root.add(leftPanel, BorderLayout.CENTER);

        // ----- Panel derecho (orden actual) -----
        orderPanel = new OrderPanel();
        orderPanel.setPreferredSize(new Dimension(470, 100));
        root.add(orderPanel, BorderLayout.EAST);

        showCategory("Todos");

        // Revisa cada minuto si ya cambió el turno del menú (ej. 12:00 o 2:00 am)
        javax.swing.Timer relojMenu = new javax.swing.Timer(60_000, e -> revisarCambioDeTurno());
        relojMenu.start();

        // ESC: regresa a la pantalla anterior
        root.registerKeyboardAction(e -> regresar(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    /** Abre la pantalla con el primer cajero activo registrado en la base de datos (uso sin login). */
    public pantallaCajero() {
        this(cajeroPorDefectoId(), cajeroPorDefectoNombre(), cajeroPorDefectoTurno());
    }

    private static Object[] cajeroPorDefecto;

    private static Object[] resolverCajeroPorDefecto() {
        if (cajeroPorDefecto == null) {
            cajeroPorDefecto = crud.obtenerCajeroPredeterminado();
            if (cajeroPorDefecto == null) {
                // No hay ningún cajero activo en la tabla usuario; se usa un valor de reserva
                // para que la pantalla siga siendo utilizable. Los pedidos no podrán guardarse
                // hasta que exista un usuario válido en la base de datos.
                cajeroPorDefecto = new Object[]{-1, "Sin cajero asignado", "N/A"};
            }
        }
        return cajeroPorDefecto;
    }

    private static int cajeroPorDefectoId() {
        return (Integer) resolverCajeroPorDefecto()[0];
    }

    private static String cajeroPorDefectoNombre() {
        return (String) resolverCajeroPorDefecto()[1];
    }

    private static String cajeroPorDefectoTurno() {
        return (String) resolverCajeroPorDefecto()[2];
    }

    // Vuelve a la pantalla anterior (login / gestión de caja)
    private void regresar() {
        new pantallaLogin().setVisible(true);
        dispose();
    }

    // ---------- Turno del menú según la hora ----------
    // Devuelve {id_turno, nombre, hora_inicio, hora_fin} del turno vigente, o null si no hay.
    // Soporta turnos que cruzan la medianoche (ej. 12:00 a 02:00).
    private String[] buscarTurnoVigente(java.sql.Connection con) throws java.sql.SQLException {
        String sql = "SELECT id_turno, nombre, "
                + "DATE_FORMAT(hora_inicio, '%H:%i') AS ini, DATE_FORMAT(hora_fin, '%H:%i') AS fin "
                + "FROM turno_menu "
                + "WHERE estado = 1 AND ( "
                + "   (hora_inicio < hora_fin AND ? >= hora_inicio AND ? < hora_fin) "      // turno normal
                + "OR (hora_inicio > hora_fin AND (? >= hora_inicio OR ? < hora_fin)) "     // cruza medianoche
                + ") ORDER BY id_turno LIMIT 1";
        java.sql.Time ahora = java.sql.Time.valueOf(java.time.LocalTime.now().withNano(0));
        try (java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 1; i <= 4; i++) {
                ps.setTime(i, ahora);
            }
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[]{rs.getString("id_turno"), rs.getString("nombre"),
                        rs.getString("ini"), rs.getString("fin")};
                }
            }
        }
        return null;
    }

    // Se llama cada minuto: si cambió el turno, recarga el menú sin cerrar la app
    private void revisarCambioDeTurno() {
        try (java.sql.Connection con = new Conexion().getConnection()) {
            if (con == null) {
                return; // sin conexión: se deja el menú como está
            }
            String[] turno = buscarTurnoVigente(con);
            int id = turno == null ? 0 : Integer.parseInt(turno[0]);
            if (id != turnoActual) {
                buildCatalog();
                showCategory(categoriaActual);
            }
        } catch (java.sql.SQLException ex) {
            System.out.println("No se pudo revisar el turno: " + ex.getMessage());
        }
    }

    // Carga categorías y productos desde la base de datos GITEAT
    // Solo trae los productos del turno actual + los que no tienen turno (bebidas)
    private void buildCatalog() {
        catalog.clear();
        String sqlCategorias = "SELECT nombre FROM categoria WHERE estado = 1 ORDER BY id_categoria";
        String sqlProductos = "SELECT p.id_producto, p.nombre, p.precio_base, p.imagen, p.seccion, c.nombre AS categoria "
                + "FROM producto p "
                + "JOIN categoria c ON c.id_categoria = p.id_categoria "
                + "WHERE p.estado = 1 AND c.estado = 1 "
                + "AND (p.id_turno IS NULL OR p.id_turno = ?) "
                + "ORDER BY c.id_categoria, p.orden_menu, p.id_producto";

        try (java.sql.Connection con = new Conexion().getConnection()) {
            if (con == null) {
                throw new java.sql.SQLException("Revisa que MySQL esté encendido y que el usuario y contraseña sean correctos.");
            }

            // ----- Turno vigente -----
            String[] turno = buscarTurnoVigente(con);
            turnoActual = turno == null ? 0 : Integer.parseInt(turno[0]);
            if (turno != null) {
                menuLabel.setText("Menú " + turno[1] + "  ·  " + turno[2] + " a " + turno[3]);
            } else {
                menuLabel.setText("Sin menú de horario activo");
            }

            // ----- Categorías (una lista vacía por cada una) -----
            try (java.sql.PreparedStatement ps = con.prepareStatement(sqlCategorias);
                    java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    catalog.put(rs.getString("nombre"), new ArrayList<>());
                }
            }

            // ----- Productos -----
            Map<String, String> ultimaSeccion = new HashMap<>();
            try (java.sql.PreparedStatement ps = con.prepareStatement(sqlProductos)) {
                ps.setInt(1, turnoActual);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int idProducto = rs.getInt("id_producto");
                        String nombre = rs.getString("nombre");
                        double precio = rs.getDouble("precio_base");
                        String imagen = rs.getString("imagen");
                        String seccion = rs.getString("seccion");
                        String categoria = rs.getString("categoria");

                        List<MenuItem> lista = catalog.computeIfAbsent(categoria, k -> new ArrayList<>());

                        // Cuando cambia la sección, se agrega el "mensajito" (Desayunos, Pollo, Sodas...)
                        if (seccion != null && !seccion.equals(ultimaSeccion.get(categoria))) {
                            lista.add(MenuItem.encabezado(seccion, categoria));
                            ultimaSeccion.put(categoria, seccion);
                        }

                        lista.add(new MenuItem(idProducto, nombre, precio, categoria, imagen));
                    }
                }
            }

        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(null,
                    "No se pudo cargar el menú desde la base de datos.\n\n" + ex.getMessage(),
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
        }

        // ----- Todos -----
        List<MenuItem> todos = new ArrayList<>();
        for (List<MenuItem> l : catalog.values()) {
            todos.addAll(l);
        }
        catalog.put("Todos", todos);
    }

    // Las pestañas salen de las categorías activas de la base de datos
    private JPanel buildTabsBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        bar.setOpaque(false);

        List<String> cats = new ArrayList<>();
        cats.add("Todos");
        cats.addAll(crud.listarCategoriasActivas());

        for (String cat : cats) {
            JButton btn = new PillButton(cat);
            btn.addActionListener(e -> showCategory(cat));
            tabButtons.put(cat, btn);
            bar.add(btn);
        }
        return bar;
    }

    private void showCategory(String category) {
        categoriaActual = category;
        for (Map.Entry<String, JButton> entry : tabButtons.entrySet()) {
            ((PillButton) entry.getValue()).setActive(entry.getKey().equals(category));
        }
        sectionTitle.setText(category.equals("Todos") ? "Todos los productos" : category);
        List<MenuItem> items = catalog.getOrDefault(category, new ArrayList<>());
        gridPanel.setItems(items);
    }

    // ---------- Panel de fondo con leve decoración ----------
    static class BackgroundPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, BG_BEIGE_LIGHT, 0, h, BG_BEIGE);
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
            // franja inferior color café, como en el diseño original
            g2.setColor(new Color(0x5B, 0x3A, 0x24));
            int stripeH = (int) (h * 0.10);
            g2.fillRect(0, h - stripeH, w, stripeH);
            g2.dispose();
        }
    }

    // ---------- Botón tipo "pill" para categorías ----------
    static class PillButton extends JButton {

        private boolean active = false;

        PillButton(String text) {
            super(text);
            setFont(new Font("SansSerif", Font.BOLD, 18));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setForeground(BROWN_TEXT);
            setBorder(new EmptyBorder(16, 30, 16, 30));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        void setActive(boolean active) {
            this.active = active;
            setForeground(active ? Color.WHITE : BROWN_TEXT);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(active ? TAB_ACTIVE : TAB_INACTIVE);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 30, 30));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ---------- Grilla de productos ----------
    class CardsGridPanel extends JPanel {

        CardsGridPanel() {
            setOpaque(false);
            setLayout(new GridBagLayout());
        }

        void setItems(List<MenuItem> items) {
            removeAll();
            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(14, 14, 14, 14);
            gc.fill = GridBagConstraints.NONE;
            int cols = 4;

            if (items.isEmpty()) {
                gc.gridx = 0;
                gc.gridy = 0;
                gc.gridwidth = cols;
                JLabel empty = new JLabel("No hay productos en esta categoría todavía.");
                empty.setFont(new Font("SansSerif", Font.PLAIN, 18));
                empty.setForeground(BROWN_TEXT);
                add(empty, gc);
            } else {
                for (int i = 0; i < items.size(); i++) {
                    gc.gridx = i % cols;
                    gc.gridy = i / cols;
                    add(new ItemCard(items.get(i)), gc);
                }
            }
            revalidate();
            repaint();
        }
    }

    // ---------- Tarjeta de producto ----------
    class ItemCard extends JPanel {

        ItemCard(MenuItem item) {
            setPreferredSize(new Dimension(230, 250));
            setOpaque(false);
            setLayout(new BorderLayout());
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            JPanel content = new RoundedPanel(CARD_BG, 22);
            content.setLayout(new BorderLayout());
            content.setBorder(new EmptyBorder(18, 12, 18, 12));

            JLabel pictureLabel = new JLabel("", SwingConstants.CENTER);

            // Cargar la imagen utilizando el recurso del proyecto (getResource)
            java.net.URL imgURL = item.imagePath == null ? null : getClass().getResource(item.imagePath);
            if (imgURL != null) {
                ImageIcon rawIcon = new ImageIcon(imgURL);
                Image scaled = rawIcon.getImage().getScaledInstance(140, 110, Image.SCALE_SMOOTH);
                pictureLabel.setIcon(new ImageIcon(scaled));
            } else {
                // Si la imagen no existe (o es una tarjeta de encabezado), muestra un emoji de respaldo
                pictureLabel.setText(item.esEncabezado() ? "📌" : item.emoji);
                pictureLabel.setFont(new Font("SansSerif", Font.PLAIN, 64));
            }

            JPanel imgWrap = new RoundedPanel(CARD_IMG_BG, 16);
            imgWrap.setLayout(new BorderLayout());
            imgWrap.add(pictureLabel, BorderLayout.CENTER);
            imgWrap.setPreferredSize(new Dimension(150, 120));

            JPanel imgOuter = new JPanel(new FlowLayout(FlowLayout.CENTER));
            imgOuter.setOpaque(false);
            imgOuter.add(imgWrap);
            content.add(imgOuter, BorderLayout.NORTH);

            JLabel nameLabel = new JLabel("<html><div style='text-align:center;'>" + item.name + "</div></html>", SwingConstants.CENTER);
            nameLabel.setFont(new Font("SansSerif", Font.BOLD, 19));
            nameLabel.setForeground(YELLOW_TEXT);
            nameLabel.setHorizontalAlignment(SwingConstants.CENTER);

            JLabel priceLabel = new JLabel(item.esEncabezado() ? " " : String.format("Q %.2f", item.price), SwingConstants.CENTER);
            priceLabel.setFont(new Font("SansSerif", Font.PLAIN, 17));
            priceLabel.setForeground(BROWN_TEXT);

            JPanel textPanel = new JPanel();
            textPanel.setOpaque(false);
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            textPanel.add(Box.createVerticalStrut(10));
            textPanel.add(nameLabel);
            textPanel.add(Box.createVerticalStrut(6));
            textPanel.add(priceLabel);

            content.add(textPanel, BorderLayout.CENTER);
            add(content, BorderLayout.CENTER);

            // Los encabezados de sección no son clicables
            if (!item.esEncabezado()) {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // Hamburguesas con receta: abrir ventana de ingredientes
                        Recipe recipe = RECIPES.get(item.name);
                        if ("Hamburguesas".equals(item.category) && recipe != null) {
                            content.setBorder(new EmptyBorder(18, 12, 18, 12));
                            Customization c = CustomizeDialog.open(pantallaCajero.this, item, recipe, null);
                            if (c != null) {
                                orderPanel.addCustom(item, c);
                            }
                        } else {
                            orderPanel.addItem(item);
                        }
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        content.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(YELLOW_TEXT, 2, true),
                                new EmptyBorder(16, 10, 16, 10)));
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        content.setBorder(new EmptyBorder(18, 12, 18, 12));
                        repaint();
                    }
                });
            }
        }
    }

    // ---------- Panel redondeado genérico ----------
    static class RoundedPanel extends JPanel {

        private final Color bg;
        private final int radius;

        RoundedPanel(Color bg, int radius) {
            this.bg = bg;
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), radius, radius));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // =====================================================================
    // ---------- Panel derecho: orden actual ----------
    // =====================================================================

    // Colores extra para el panel de orden
    static final Color RED_PAY = new Color(0xD7, 0x1F, 0x3A);
    static final Color RED_PAY_HOVER = new Color(0xB5, 0x15, 0x2D);
    static final Color GREEN_ADD = new Color(0x6A, 0x9E, 0x2F);
    static final Color GREEN_ADD_HOVER = new Color(0x58, 0x88, 0x24);
    static final Color TOTAL_BAR = new Color(0xF2, 0xCE, 0x4C);
    static final Color LINE_SEP = new Color(0xD9, 0xCB, 0xB5);
    static final Color NOTE_TEXT = new Color(0x7A, 0x5A, 0x42);
    static final Color ROW_SELECTED = new Color(0xEF, 0xE3, 0xCF);

    class OrderPanel extends JPanel {

        private final int M = 18; // margen exterior de la tarjeta café

        private final DefaultListModel<OrderLine> model = new DefaultListModel<>();
        private final JList<OrderLine> list = new JList<>(model);
        private final JLabel subtotalLabel = new JLabel("Q 0.00");
        private final JLabel totalLabel = new JLabel("Q 0.00");
        private final List<OrderLine> lines = new ArrayList<>();
        private final CardLayout listCards = new CardLayout();
        private final JPanel listHolder = new JPanel(listCards);

        OrderPanel() {
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createCompoundBorder(
                    new EmptyBorder(M, 4, M, M),        // margen fuera de la tarjeta
                    new EmptyBorder(28, 22, 22, 22)));  // relleno dentro de la tarjeta

            // ----- Encabezado -----
            JPanel header = new JPanel();
            header.setOpaque(false);
            header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

            JLabel title = new JLabel("Orden actual");
            title.setFont(new Font("SansSerif", Font.BOLD, 30));
            title.setForeground(WHITE_TEXT);

            // Botón Regresar
            RoundedButton backBtn = new RoundedButton("← Regresar",
                    new Color(0x5A, 0x3A, 0x24), new Color(0x74, 0x4C, 0x30), 15);
            backBtn.setPreferredSize(new Dimension(130, 40));
            backBtn.addActionListener(e -> regresar());

            JPanel titleRow = new JPanel(new BorderLayout());
            titleRow.setOpaque(false);
            titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            titleRow.add(title, BorderLayout.WEST);
            titleRow.add(backBtn, BorderLayout.EAST);
            titleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, titleRow.getPreferredSize().height));

            JPanel cashierRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            cashierRow.setOpaque(false);
            cashierRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel l1 = new JLabel("Cajero: ");
            l1.setForeground(WHITE_TEXT);
            l1.setFont(new Font("SansSerif", Font.PLAIN, 15));
            JLabel l2 = new JLabel(nombreCajero);
            l2.setForeground(YELLOW_BRIGHT);
            l2.setFont(new Font("SansSerif", Font.BOLD, 15));
            JLabel l3 = new JLabel(turnoCajero.isEmpty() ? "" : "  -   Turno " + turnoCajero);
            l3.setForeground(WHITE_TEXT);
            l3.setFont(new Font("SansSerif", Font.PLAIN, 15));
            cashierRow.add(l1);
            cashierRow.add(l2);
            cashierRow.add(l3);

            JSeparator sep = new JSeparator();
            sep.setForeground(new Color(0x6B, 0x4A, 0x30));
            sep.setBackground(new Color(0x6B, 0x4A, 0x30));
            sep.setAlignmentX(Component.LEFT_ALIGNMENT);

            header.add(titleRow);
            header.add(Box.createVerticalStrut(8));
            header.add(cashierRow);
            header.add(Box.createVerticalStrut(14));
            header.add(sep);
            add(header, BorderLayout.NORTH);

            // ----- Tarjeta clara interior -----
            RoundedPanel card = new RoundedPanel(CARD_IMG_BG, 28);
            card.setLayout(new BorderLayout());
            card.setBorder(new EmptyBorder(10, 14, 16, 14));

            // Lista de productos
            list.setOpaque(false);
            list.setCellRenderer(new OrderLineRenderer());
            list.setFixedCellHeight(-1);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.setToolTipText("Doble clic: editar ingredientes / nota  |  Clic derecho: más opciones");
            list.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int idx = list.locationToIndex(e.getPoint());
                    if (idx < 0 || !list.getCellBounds(idx, idx).contains(e.getPoint())) {
                        return;
                    }
                    list.setSelectedIndex(idx);
                    OrderLine line = model.get(idx);
                    if (SwingUtilities.isRightMouseButton(e)) {
                        showLineMenu(line, e);
                    } else if (e.getClickCount() == 2) {
                        editNote(line);
                    }
                }
            });

            JScrollPane scroll = new JScrollPane(list);
            scroll.setBorder(null);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
            scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

            // Mensaje cuando la orden está vacía
            JLabel empty = new JLabel("<html><div style='text-align:center;'>"
                    + "<b>Aún no hay productos</b><br><br>"
                    + "Toca un producto del menú<br>para agregarlo a la orden"
                    + "</div></html>", SwingConstants.CENTER);
            empty.setFont(new Font("SansSerif", Font.PLAIN, 15));
            empty.setForeground(NOTE_TEXT);

            listHolder.setOpaque(false);
            listHolder.add(empty, "empty");
            listHolder.add(scroll, "list");
            card.add(listHolder, BorderLayout.CENTER);

            // ----- Pie: subtotal, total y botones -----
            JPanel footer = new JPanel();
            footer.setOpaque(false);
            footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));

            JPanel subtotalRow = new JPanel(new BorderLayout());
            subtotalRow.setOpaque(false);
            subtotalRow.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 0, 0, LINE_SEP),
                    new EmptyBorder(12, 8, 8, 8)));
            JLabel subCaption = new JLabel("Subtotal");
            subCaption.setFont(new Font("SansSerif", Font.PLAIN, 14));
            subCaption.setForeground(BROWN_TEXT);
            subtotalLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
            subtotalLabel.setForeground(BROWN_TEXT);
            subtotalRow.add(subCaption, BorderLayout.WEST);
            subtotalRow.add(subtotalLabel, BorderLayout.EAST);

            RoundedPanel totalBar = new RoundedPanel(TOTAL_BAR, 14);
            totalBar.setLayout(new BorderLayout());
            totalBar.setBorder(new EmptyBorder(12, 14, 12, 14));
            JLabel totalCaption = new JLabel("Total");
            totalCaption.setFont(new Font("SansSerif", Font.BOLD, 22));
            totalCaption.setForeground(BROWN_DARK);
            totalLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
            totalLabel.setForeground(BROWN_DARK);
            totalBar.add(totalCaption, BorderLayout.WEST);
            totalBar.add(totalLabel, BorderLayout.EAST);

            PayButton payBtn = new PayButton();
            payBtn.addActionListener(e -> {
                if (lines.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "La orden está vacía.",
                            "Ir al pago", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (idUsuarioCajero < 0) {
                    JOptionPane.showMessageDialog(this,
                            "No hay ningún cajero activo registrado en la base de datos.\n"
                            + "No se puede guardar el pedido.",
                            "Ir al pago", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!gestionCaja.isCajaAbierta()) {
                    JOptionPane.showMessageDialog(this,
                            "La caja está cerrada. Pida al administrador que la abra.",
                            "Caja cerrada", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                double subtotal = 0;
                for (OrderLine l : lines) {
                    subtotal += l.unitPrice * l.qty;
                }
                // Ventana de pago: efectivo, tarjeta, mixto y cupones
                PaymentResult pago = PaymentDialog.open(this, subtotal);
                if (pago == null) {
                    return; // canceló el pago
                }

                // 1) Guardar en la base de datos (si falla, NO se limpia la orden)
                int idOrden;
                try {
                    idOrden = VentaDAO.guardar(idUsuarioCajero, pago, lines);
                } catch (java.sql.SQLException ex) {
                    JOptionPane.showMessageDialog(this,
                            "No se pudo guardar la venta:\n" + ex.getMessage(),
                            "Error al guardar", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 2) Reflejar la venta en la caja
                if (pago.cash > 0) {
                    gestionCaja.registrarVenta(gestionCaja.MetodoPago.EFECTIVO, pago.cash);
                }
                if (pago.card > 0) {
                    gestionCaja.registrarVenta(gestionCaja.MetodoPago.TARJETA, pago.card);
                }

                JOptionPane.showMessageDialog(this, "Orden #" + idOrden + "\n\n" + pago.summary(),
                        "Pago realizado", JOptionPane.INFORMATION_MESSAGE);
                lines.clear();
                model.clear();
                updateTotal();
            });

            RoundedButton addMoreBtn = new RoundedButton("¿Deseas agregar algo más?",
                    GREEN_ADD, GREEN_ADD_HOVER, 16);
            addMoreBtn.addActionListener(e -> showCategory("Todos"));

            footer.add(full(subtotalRow));
            footer.add(Box.createVerticalStrut(6));
            footer.add(full(totalBar));
            footer.add(Box.createVerticalStrut(18));
            footer.add(full(payBtn));
            footer.add(Box.createVerticalStrut(12));
            footer.add(full(addMoreBtn));
            card.add(footer, BorderLayout.SOUTH);

            JPanel centerWrap = new JPanel(new BorderLayout());
            centerWrap.setOpaque(false);
            centerWrap.setBorder(new EmptyBorder(18, 0, 0, 0));
            centerWrap.add(card, BorderLayout.CENTER);
            add(centerWrap, BorderLayout.CENTER);

            updateTotal();
        }

        // Hace que el componente ocupe todo el ancho en un BoxLayout vertical
        private <T extends JComponent> T full(T c) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
            return c;
        }

        // Tarjeta café con esquinas redondeadas
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(PANEL_DARK);
            g2.fill(new RoundRectangle2D.Double(4, M, getWidth() - 4 - M, getHeight() - 2 * M, 40, 40));
            g2.dispose();
        }

        void addItem(MenuItem item) {
            // Los encabezados de sección no se agregan a la orden
            if (item.esEncabezado()) {
                return;
            }
            for (OrderLine line : lines) {
                if (line.custom == null && line.item.idProducto == item.idProducto && line.notes.isEmpty()) {
                    line.qty++;
                    refresh();
                    return;
                }
            }
            OrderLine line = new OrderLine(item);
            lines.add(line);
            model.addElement(line);
            refresh();
            list.ensureIndexIsVisible(model.size() - 1);
        }

        // Agrega una hamburguesa personalizada (desde la ventana de ingredientes)
        void addCustom(MenuItem item, Customization c) {
            String notes = c.describe();
            double price = c.unitPrice(item);
            for (OrderLine line : lines) {
                if (line.item.idProducto == item.idProducto && line.notes.equals(notes)
                        && Math.abs(line.unitPrice - price) < 0.001) {
                    line.qty++;
                    refresh();
                    return;
                }
            }
            OrderLine line = new OrderLine(item);
            line.custom = c;
            line.notes = notes;
            line.unitPrice = price;
            lines.add(line);
            model.addElement(line);
            refresh();
            list.ensureIndexIsVisible(model.size() - 1);
        }

        private void removeLine(OrderLine line) {
            lines.remove(line);
            model.removeElement(line);
            refresh();
        }

        private void editNote(OrderLine line) {
            // Si es hamburguesa con receta, se reabre la ventana de ingredientes
            Recipe recipe = RECIPES.get(line.item.name);
            if (recipe != null) {
                Customization c = CustomizeDialog.open(this, line.item, recipe, line.custom);
                if (c != null) {
                    line.custom = c;
                    line.notes = c.describe();
                    line.unitPrice = c.unitPrice(line.item);
                    refresh();
                }
                return;
            }
            Object res = JOptionPane.showInputDialog(this,
                    "Nota para " + line.item.name + "\n(ej. Sin pepinillos, sin cebolla)",
                    "Personalizar producto", JOptionPane.PLAIN_MESSAGE, null, null, line.notes);
            if (res != null) {
                line.notes = res.toString().trim();
                refresh();
            }
        }

        private void showLineMenu(OrderLine line, MouseEvent e) {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem plus = new JMenuItem("Agregar uno (+1)");
            plus.addActionListener(a -> {
                line.qty++;
                refresh();
            });
            JMenuItem minus = new JMenuItem("Quitar uno (-1)");
            minus.addActionListener(a -> {
                line.qty--;
                if (line.qty <= 0) {
                    removeLine(line);
                } else {
                    refresh();
                }
            });
            JMenuItem note = new JMenuItem("Editar ingredientes / nota...");
            note.addActionListener(a -> editNote(line));
            JMenuItem del = new JMenuItem("Eliminar producto");
            del.addActionListener(a -> removeLine(line));
            menu.add(plus);
            menu.add(minus);
            menu.add(note);
            menu.addSeparator();
            menu.add(del);
            menu.show(list, e.getX(), e.getY());
        }

        // Recalcula alturas de fila (por las notas) y el total
        private void refresh() {
            list.setFixedCellHeight(10);
            list.setFixedCellHeight(-1);
            list.revalidate();
            list.repaint();
            updateTotal();
        }

        void updateTotal() {
            double total = 0;
            for (OrderLine l : lines) {
                total += l.unitPrice * l.qty;
            }
            subtotalLabel.setText(String.format("Q %.2f", total));
            totalLabel.setText(String.format("Q %.2f", total));
            listCards.show(listHolder, lines.isEmpty() ? "empty" : "list");
        }
    }

    static class OrderLine {

        MenuItem item;
        int qty = 1;
        String notes = "";
        double unitPrice;          // precio con extras incluidos
        Customization custom;      // ingredientes elegidos (solo hamburguesas)

        OrderLine(MenuItem item) {
            this.item = item;
            this.unitPrice = item.price;
        }
    }

    // ---------- Fila de la orden: imagen + burbuja roja + nombre/nota + precio ----------
    static class OrderLineRenderer extends JPanel implements ListCellRenderer<OrderLine> {

        private static final Map<String, ImageIcon> ICON_CACHE = new HashMap<>();

        private final Thumb thumb = new Thumb();
        private final JLabel nameLbl = new JLabel();
        private final JLabel noteLbl = new JLabel();
        private final JLabel priceLbl = new JLabel();
        private boolean selected;
        private boolean drawSeparator;

        OrderLineRenderer() {
            setLayout(new BorderLayout(10, 0));
            setOpaque(false);
            setBorder(new EmptyBorder(12, 4, 12, 6));

            nameLbl.setFont(new Font("SansSerif", Font.BOLD, 17));
            nameLbl.setForeground(BROWN_DARK);
            noteLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
            noteLbl.setForeground(NOTE_TEXT);
            priceLbl.setFont(new Font("SansSerif", Font.BOLD, 17));
            priceLbl.setForeground(BROWN_DARK);

            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            nameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            noteLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            text.add(nameLbl);
            text.add(Box.createVerticalStrut(3));
            text.add(noteLbl);

            // GridBagLayout centra verticalmente el bloque de texto
            JPanel textWrap = new JPanel(new GridBagLayout());
            textWrap.setOpaque(false);
            GridBagConstraints gc = new GridBagConstraints();
            gc.anchor = GridBagConstraints.WEST;
            gc.weightx = 1;
            gc.fill = GridBagConstraints.HORIZONTAL;
            textWrap.add(text, gc);

            add(thumb, BorderLayout.WEST);
            add(textWrap, BorderLayout.CENTER);
            add(priceLbl, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends OrderLine> list, OrderLine value,
                int index, boolean isSelected, boolean cellHasFocus) {
            thumb.set(loadIcon(value.item.imagePath), value.qty);

            int textW = Math.max(110, list.getWidth() - 210);
            nameLbl.setText("<html><div style='width:" + textW + "px'>"
                    + esc(value.item.name) + "</div></html>");

            if (value.notes.isEmpty()) {
                noteLbl.setVisible(false);
            } else {
                noteLbl.setVisible(true);
                noteLbl.setText("<html><div style='width:" + textW + "px'>"
                        + esc(value.notes) + "</div></html>");
            }

            priceLbl.setText(String.format("Q %.2f", value.unitPrice * value.qty));
            selected = isSelected;
            drawSeparator = index < list.getModel().getSize() - 1;
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (selected) {
                g2.setColor(ROW_SELECTED);
                g2.fill(new RoundRectangle2D.Double(0, 3, getWidth(), getHeight() - 6, 16, 16));
            }
            if (drawSeparator) {
                g2.setColor(LINE_SEP);
                g2.fillRect(4, getHeight() - 1, getWidth() - 8, 1);
            }
            g2.dispose();
            super.paintComponent(g);
        }

        private static String esc(String s) {
            return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        }

        // Carga y escala la imagen una sola vez (se guarda en caché)
        static ImageIcon loadIcon(String path) {
            if (path == null) {
                return null;
            }
            if (ICON_CACHE.containsKey(path)) {
                return ICON_CACHE.get(path);
            }
            ImageIcon result = null;
            java.net.URL url = OrderLineRenderer.class.getResource(path);
            if (url != null) {
                ImageIcon raw = new ImageIcon(url);
                int iw = raw.getIconWidth(), ih = raw.getIconHeight();
                if (iw > 0 && ih > 0) {
                    double s = Math.min(66.0 / iw, 52.0 / ih);
                    int w = Math.max(1, (int) (iw * s));
                    int h = Math.max(1, (int) (ih * s));
                    result = new ImageIcon(raw.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
                }
            }
            ICON_CACHE.put(path, result);
            return result;
        }
    }

    // ---------- Miniatura con la burbuja roja de cantidad (1x, 2x...) ----------
    static class Thumb extends JComponent {

        private ImageIcon icon;
        private int qty;

        Thumb() {
            setPreferredSize(new Dimension(92, 66));
        }

        void set(ImageIcon icon, int qty) {
            this.icon = icon;
            this.qty = qty;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int oy = Math.max(0, (getHeight() - 64) / 2); // centrar verticalmente

            // Fondo de la imagen
            g2.setColor(CARD_BG);
            g2.fillRoundRect(0, oy + 6, 74, 58, 14, 14);
            if (icon != null) {
                int x = (74 - icon.getIconWidth()) / 2;
                int y = oy + 6 + (58 - icon.getIconHeight()) / 2;
                icon.paintIcon(this, g2, x, y);
            }

            // Burbuja roja con la cantidad
            int d = 32;
            int bx = getWidth() - d - 2;
            int by = oy;
            g2.setColor(RED_PAY);
            g2.fillOval(bx, by, d, d);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(bx, by, d, d);

            String t = qty + "x";
            g2.setFont(new Font("SansSerif", Font.BOLD, qty >= 10 ? 11 : 13));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(t, bx + (d - fm.stringWidth(t)) / 2,
                    by + (d - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    // ---------- Botón rojo "Ir al pago" con ícono de tarjeta y flecha ----------
    static class PayButton extends JButton {

        private boolean hover;

        PayButton() {
            super("Ir al pago");
            setFont(new Font("SansSerif", Font.BOLD, 20));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(100, 58));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight(), cy = h / 2;

            g2.setColor(hover ? RED_PAY_HOVER : RED_PAY);
            g2.fill(new RoundRectangle2D.Double(0, 0, w, h, 20, 20));

            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int textW = fm.stringWidth(getText());
            int contentW = 32 + 14 + textW;
            int x = (w - contentW) / 2;

            // Ícono de tarjeta
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(x, cy - 11, 32, 22, 6, 6);
            g2.setColor(hover ? RED_PAY_HOVER : RED_PAY);
            g2.fillRect(x, cy - 6, 32, 4);
            g2.fillRect(x + 5, cy + 4, 9, 3);

            // Texto
            g2.setColor(Color.WHITE);
            g2.drawString(getText(), x + 46, cy - fm.getHeight() / 2 + fm.getAscent());

            // Flecha ">"
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(w - 32, cy - 7, w - 25, cy);
            g2.drawLine(w - 25, cy, w - 32, cy + 7);
            g2.dispose();
        }
    }

    // ---------- Botón redondeado genérico ----------
    static class RoundedButton extends JButton {

        private final Color base, hoverColor;
        private boolean hover;

        RoundedButton(String text, Color base, Color hoverColor, int fontSize) {
            super(text);
            this.base = base;
            this.hoverColor = hoverColor;
            setFont(new Font("SansSerif", Font.BOLD, fontSize));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(100, 48));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(hover ? hoverColor : base);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
            g2.setColor(getForeground());
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    // =====================================================================
    // ---------- RECETAS: ingredientes de cada hamburguesa ----------
    // =====================================================================

    // Tipo de hamburguesa: define qué "tipo de carne" se puede elegir y qué extras hay
    enum Kind {
        RES, POLLO_CRUJIENTE, POLLO_PLANCHA,
        DESAYUNO_SALCHICHA, DESAYUNO_JAMON, DESAYUNO_TOCINO, DESAYUNO_SIN_CARNE
    }

    // ---------- Opciones de cada tipo de ingrediente ----------
    // "Sin" siempre quita el ingrediente. Puedes agregar o cambiar opciones aquí.
    static final Map<String, String[]> ING_OPTIONS = new HashMap<>();

    static {
        String[] cantidad = {"Sin", "Normal", "Extra"};
        String[] salsa = {"Sin", "Menos", "Normal", "Extra"};

        ING_OPTIONS.put("Lechuga", cantidad);
        ING_OPTIONS.put("Tomate", cantidad);
        ING_OPTIONS.put("Pepinillos", cantidad);
        ING_OPTIONS.put("Frijol volteado", cantidad);
        ING_OPTIONS.put("Plátano frito", cantidad);
        ING_OPTIONS.put("Guacamol", cantidad);
        ING_OPTIONS.put("Pico de gallo", cantidad);
        ING_OPTIONS.put("Cebolla crujiente", cantidad);

        ING_OPTIONS.put("Cebolla", new String[]{"Sin", "Blanca", "Morada", "Caramelizada"});
        ING_OPTIONS.put("Queso", new String[]{"Sin", "Amarillo", "Cheddar", "Emmental", "Blanco"});
        ING_OPTIONS.put("Tocino", new String[]{"Sin", "Normal", "Bien tostado"});
        ING_OPTIONS.put("Huevo", new String[]{"Sin", "Estrellado", "Revuelto"});

        ING_OPTIONS.put("Ketchup", salsa);
        ING_OPTIONS.put("Mostaza", salsa);
        ING_OPTIONS.put("Mayonesa", salsa);
        ING_OPTIONS.put("Salsa especial", salsa);
        ING_OPTIONS.put("Salsa Big Tasty", salsa);
        ING_OPTIONS.put("Aderezo ranch", salsa);
        ING_OPTIONS.put("Salsa BBQ ahumada", salsa);
        ING_OPTIONS.put("Salsa blanca", salsa);
        ING_OPTIONS.put("Salsa de queso cheddar", salsa);

        // Preferencias generales (se agregan a TODAS las hamburguesas)
        ING_OPTIONS.put("Sal", new String[]{"Sin", "Con"});
        ING_OPTIONS.put("Pan", new String[]{"Normal", "Bien tostado", "Sin tostar"});
    }

    // Un ingrediente dentro de una receta
    static class Ing {

        final String label;     // lo que se muestra, ej. "Queso (x2)"
        final String type;      // tipo base, ej. "Queso"
        final String[] options;
        final String def;       // opción original de la receta

        // spec: "Lechuga"  o  "Cebolla:Blanca"  o  "Queso (x2):Amarillo"
        Ing(String spec) {
            String[] parts = spec.split(":", 2);
            this.label = parts[0];
            this.type = parts[0].replaceAll(" \\(x\\d\\)$", "");
            String[] opts = ING_OPTIONS.get(type);
            this.options = opts != null ? opts : new String[]{"Sin", "Normal"};
            if (parts.length > 1) {
                this.def = parts[1];
            } else {
                this.def = Arrays.asList(options).contains("Normal") ? "Normal" : options[options.length > 1 ? 1 : 0];
            }
        }
    }

    static class Extra {

        final String name;
        final double price;

        Extra(String name, double price) {
            this.name = name;
            this.price = price;
        }
    }

    // Extras con costo (puedes cambiar nombres y precios aquí)
    static final Extra[] EXTRAS_DESAYUNO = {
        new Extra("Huevo extra", 5.00),
        new Extra("Queso extra", 4.00),
        new Extra("Tocino extra", 7.00),
        new Extra("Salchicha extra", 7.00),
        new Extra("Aguacate", 6.00)
    };
    static final Extra[] EXTRAS_ALMUERZO = {
        new Extra("Queso extra", 5.00),
        new Extra("Tocino extra", 8.00),
        new Extra("Carne extra", 12.00),
        new Extra("Guacamol extra", 7.00),
        new Extra("Jalapeños", 4.00),
        new Extra("Huevo estrellado", 6.00)
    };

    static class Recipe {

        final Kind kind;
        final int portions;                    // cuántas carnes lleva
        final List<Ing> ingredients = new ArrayList<>();
        final List<Ing> general = new ArrayList<>();

        Recipe(Kind kind, int portions, String... specs) {
            this.kind = kind;
            this.portions = portions;
            for (String s : specs) {
                ingredients.add(new Ing(s));
            }
            general.add(new Ing("Sal:Con"));
            general.add(new Ing("Pan:Normal"));
        }

        List<Ing> all() {
            List<Ing> l = new ArrayList<>(ingredients);
            l.addAll(general);
            return l;
        }

        boolean isBreakfast() {
            return kind.name().startsWith("DESAYUNO");
        }

        // La primera opción es la original de la hamburguesa
        String[] proteins() {
            switch (kind) {
                case RES:
                    return new String[]{"Res", "Pollo crujiente", "Pollo a la plancha"};
                case POLLO_CRUJIENTE:
                    return new String[]{"Pollo crujiente", "Pollo a la plancha", "Res"};
                case POLLO_PLANCHA:
                    return new String[]{"Pollo a la plancha", "Pollo crujiente", "Res"};
                case DESAYUNO_SALCHICHA:
                    return new String[]{"Salchicha", "Jamón", "Tocino"};
                case DESAYUNO_JAMON:
                    return new String[]{"Jamón", "Salchicha", "Tocino"};
                case DESAYUNO_TOCINO:
                    return new String[]{"Tocino", "Salchicha", "Jamón"};
                default:
                    return new String[0];
            }
        }

        String defaultProtein() {
            String[] p = proteins();
            return p.length == 0 ? null : p[0];
        }

        Extra[] extras() {
            return isBreakfast() ? EXTRAS_DESAYUNO : EXTRAS_ALMUERZO;
        }

        double extraPrice(String name) {
            for (Extra x : extras()) {
                if (x.name.equals(name)) {
                    return x.price;
                }
            }
            return 0;
        }
    }

    static final Map<String, Recipe> RECIPES = buildRecipes();

    private static Map<String, Recipe> buildRecipes() {
        Map<String, Recipe> m = new HashMap<>();

        // ----- Desayunos (pan muffin inglés) -----
        m.put("McMuffin Cheddar McMelt", new Recipe(Kind.DESAYUNO_SALCHICHA, 1,
                "Huevo:Estrellado", "Tocino", "Salsa de queso cheddar"));
        m.put("McMuffin Tocino Doble Huevo", new Recipe(Kind.DESAYUNO_TOCINO, 1,
                "Huevo (x2):Estrellado", "Queso:Amarillo"));
        m.put("McMuffin Salchicha y doble huevo", new Recipe(Kind.DESAYUNO_SALCHICHA, 1,
                "Huevo (x2):Estrellado", "Queso:Amarillo"));
        m.put("Egg McMuffin Doble Huevo", new Recipe(Kind.DESAYUNO_JAMON, 1,
                "Huevo (x2):Estrellado", "Queso:Amarillo"));
        m.put("McMuffin de Salchicha y Huevo", new Recipe(Kind.DESAYUNO_SALCHICHA, 1,
                "Huevo:Estrellado", "Queso:Amarillo"));
        m.put("McMuffin de Salchicha", new Recipe(Kind.DESAYUNO_SALCHICHA, 1,
                "Queso:Amarillo"));
        m.put("McMuffin Chapín Con Salchicha", new Recipe(Kind.DESAYUNO_SALCHICHA, 1,
                "Huevo:Revuelto", "Frijol volteado", "Queso:Amarillo"));
        m.put("Egg McMuffin", new Recipe(Kind.DESAYUNO_JAMON, 1,
                "Huevo:Estrellado", "Queso:Amarillo"));
        m.put("McMuffin Super Chapín Con Salchicha", new Recipe(Kind.DESAYUNO_SALCHICHA, 1,
                "Huevo:Revuelto", "Plátano frito", "Frijol volteado", "Queso:Amarillo"));
        m.put("Egg McMuffin Doble", new Recipe(Kind.DESAYUNO_JAMON, 2,
                "Huevo:Estrellado", "Queso:Amarillo"));
        m.put("McMuffin de Tocino y Huevo", new Recipe(Kind.DESAYUNO_TOCINO, 1,
                "Huevo:Estrellado", "Queso:Amarillo"));
        m.put("McMuffin Super Chapín Con Jamón", new Recipe(Kind.DESAYUNO_JAMON, 1,
                "Huevo:Revuelto", "Plátano frito", "Frijol volteado", "Queso:Amarillo"));
        m.put("McMuffin de Salchicha Doble y Huevo", new Recipe(Kind.DESAYUNO_SALCHICHA, 2,
                "Huevo:Estrellado", "Queso:Amarillo"));
        m.put("McMuffin Tocino Doble y Huevo", new Recipe(Kind.DESAYUNO_TOCINO, 2,
                "Huevo:Estrellado", "Queso:Amarillo"));
        m.put("McMuffin Huevo y Frijol", new Recipe(Kind.DESAYUNO_SIN_CARNE, 0,
                "Huevo:Estrellado", "Frijol volteado"));
        m.put("McMuffin Huevo y Queso", new Recipe(Kind.DESAYUNO_SIN_CARNE, 0,
                "Huevo:Estrellado", "Queso:Amarillo"));
        m.put("McMuffin Chapín Con Jamón", new Recipe(Kind.DESAYUNO_JAMON, 1,
                "Huevo:Revuelto", "Frijol volteado", "Queso:Amarillo"));

        // ----- Hamburguesas de res -----
        m.put("Bacon Cheddar McMelt", new Recipe(Kind.RES, 2,
                "Tocino", "Queso:Cheddar", "Salsa de queso cheddar", "Cebolla:Blanca"));
        m.put("McCrispy Bacon Cheddar", new Recipe(Kind.POLLO_CRUJIENTE, 1,
                "Tocino", "Salsa de queso cheddar"));
        m.put("Git Mac", new Recipe(Kind.RES, 2,
                "Lechuga", "Queso:Amarillo", "Pepinillos", "Cebolla:Blanca", "Salsa especial"));
        m.put("Git Mac Doble", new Recipe(Kind.RES, 4,
                "Lechuga", "Queso:Amarillo", "Pepinillos", "Cebolla:Blanca", "Salsa especial"));
        m.put("Cuarto de Libra con Queso", new Recipe(Kind.RES, 1,
                "Queso (x2):Amarillo", "Cebolla:Blanca", "Pepinillos", "Ketchup", "Mostaza"));
        m.put("Cuarto de Libra Doble con Queso", new Recipe(Kind.RES, 2,
                "Queso (x2):Amarillo", "Cebolla:Blanca", "Pepinillos", "Ketchup", "Mostaza"));
        m.put("Cuarto de Libra Deluxe con Queso", new Recipe(Kind.RES, 1,
                "Queso:Amarillo", "Lechuga", "Tomate", "Cebolla:Blanca", "Pepinillos", "Ketchup", "Mostaza"));
        m.put("Cuarto de Libra Deluxe Doble con Queso", new Recipe(Kind.RES, 2,
                "Queso:Amarillo", "Lechuga", "Tomate", "Cebolla:Blanca", "Pepinillos", "Ketchup", "Mostaza"));
        m.put("Cuarto de Libra Bacon con Queso", new Recipe(Kind.RES, 1,
                "Tocino", "Queso:Amarillo", "Cebolla:Blanca", "Pepinillos", "Ketchup", "Mostaza"));
        m.put("Cuarto de Libra Bacon Doble con Queso", new Recipe(Kind.RES, 2,
                "Tocino", "Queso:Amarillo", "Cebolla:Blanca", "Pepinillos", "Ketchup", "Mostaza"));
        m.put("Big Tasty", new Recipe(Kind.RES, 1,
                "Queso:Emmental", "Lechuga", "Tomate", "Cebolla:Blanca", "Salsa Big Tasty"));
        m.put("Big Tasty Doble", new Recipe(Kind.RES, 2,
                "Queso:Emmental", "Lechuga", "Tomate", "Cebolla:Blanca", "Salsa Big Tasty"));
        m.put("Big Tasty Bacon", new Recipe(Kind.RES, 1,
                "Tocino", "Queso:Emmental", "Lechuga", "Tomate", "Cebolla:Blanca", "Salsa Big Tasty"));
        m.put("Big Tasty Bacon Doble", new Recipe(Kind.RES, 2,
                "Tocino", "Queso:Emmental", "Lechuga", "Tomate", "Cebolla:Blanca", "Salsa Big Tasty"));
        m.put("Triple Bacon", new Recipe(Kind.RES, 3,
                "Tocino", "Queso:Cheddar", "Cebolla:Blanca", "Pepinillos", "Ketchup", "Mostaza"));
        m.put("Quesoburguesa", new Recipe(Kind.RES, 1,
                "Queso:Amarillo", "Cebolla:Blanca", "Pepinillos", "Ketchup", "Mostaza"));
        m.put("Quesoburguesa Doble", new Recipe(Kind.RES, 2,
                "Queso (x2):Amarillo", "Cebolla:Blanca", "Pepinillos", "Ketchup", "Mostaza"));
        m.put("Quesoburguesa Triple", new Recipe(Kind.RES, 3,
                "Queso (x2):Amarillo", "Cebolla:Blanca", "Pepinillos", "Ketchup", "Mostaza"));
        m.put("Hamburguesa", new Recipe(Kind.RES, 1,
                "Cebolla:Blanca", "Pepinillos", "Ketchup", "Mostaza"));
        m.put("Hamburguesa Jr.", new Recipe(Kind.RES, 1,
                "Lechuga", "Tomate", "Mayonesa"));
        m.put("GitNífica de Res", new Recipe(Kind.RES, 1,
                "Queso:Amarillo", "Lechuga", "Tomate", "Cebolla:Blanca", "Ketchup", "Mostaza", "Mayonesa"));
        m.put("GitNífica de Res Doble", new Recipe(Kind.RES, 2,
                "Queso:Amarillo", "Lechuga", "Tomate", "Cebolla:Blanca", "Ketchup", "Mostaza", "Mayonesa"));

        // ----- Pollo -----
        m.put("McCrispy Chicken Bacon Ranch", new Recipe(Kind.POLLO_CRUJIENTE, 1,
                "Tocino", "Lechuga", "Tomate", "Aderezo ranch"));
        m.put("McCrispy Chicken Deluxe", new Recipe(Kind.POLLO_CRUJIENTE, 1,
                "Lechuga", "Tomate", "Mayonesa"));
        m.put("Big Tasty de Pollo", new Recipe(Kind.POLLO_PLANCHA, 1,
                "Queso:Emmental", "Lechuga", "Tomate", "Cebolla:Blanca", "Salsa Big Tasty"));
        m.put("Sándwich McPollo Doble", new Recipe(Kind.POLLO_CRUJIENTE, 2,
                "Lechuga", "Mayonesa"));

        // ----- Creaciones Gourmet (pan brioche) -----
        m.put("Smoke Tocino Gourmet de Res", new Recipe(Kind.RES, 1,
                "Tocino", "Queso:Blanco", "Cebolla crujiente", "Salsa BBQ ahumada"));
        m.put("Smoke Tocino Gourmet doble", new Recipe(Kind.RES, 2,
                "Tocino", "Queso:Blanco", "Cebolla crujiente", "Salsa BBQ ahumada"));
        m.put("Clásica Gourmet Res", new Recipe(Kind.RES, 1,
                "Queso:Blanco", "Lechuga", "Tomate", "Cebolla:Blanca", "Mayonesa"));
        m.put("Clásica Gourmet Res doble", new Recipe(Kind.RES, 2,
                "Queso:Blanco", "Lechuga", "Tomate", "Cebolla:Blanca", "Mayonesa"));
        m.put("Pico Guacamol Gourmet Res", new Recipe(Kind.RES, 1,
                "Guacamol", "Pico de gallo", "Lechuga", "Queso:Blanco", "Salsa blanca"));
        m.put("Pico Guacamol Gourmet doble", new Recipe(Kind.RES, 2,
                "Guacamol", "Pico de gallo", "Lechuga", "Queso:Blanco", "Salsa blanca"));
        return m;
    }

    static String lowerFirst(String s) {
        return s.isEmpty() ? s : Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    static String upperFirst(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // ---------- Lo que eligió el cliente para una hamburguesa ----------
    static class Customization {

        final Recipe recipe;
        String protein;
        final Map<String, String> choices = new HashMap<>();   // ingrediente -> opción elegida
        final Set<String> extras = new LinkedHashSet<>();
        String note = "";

        Customization(Recipe recipe) {
            this.recipe = recipe;
            this.protein = recipe.defaultProtein();
        }

        Customization copy() {
            Customization c = new Customization(recipe);
            c.protein = protein;
            c.choices.putAll(choices);
            c.extras.addAll(extras);
            c.note = note;
            return c;
        }

        String choiceFor(Ing ing) {
            return choices.getOrDefault(ing.label, ing.def);
        }

        // Texto que aparece debajo del nombre en la orden
        String describe() {
            List<String> parts = new ArrayList<>();
            if (protein != null && !protein.equals(recipe.defaultProtein())) {
                parts.add("Con " + lowerFirst(protein));
            }
            List<String> mods = new ArrayList<>();
            for (Ing ing : recipe.all()) {
                String sel = choiceFor(ing);
                if (sel.equals(ing.def)) {
                    continue;
                }
                String t = lowerFirst(ing.type);
                switch (sel) {
                    case "Sin":
                    case "Con":
                    case "Extra":
                    case "Menos":
                    case "Normal":
                        mods.add(lowerFirst(sel) + " " + t);        // "sin lechuga", "extra tomate"
                        break;
                    default:
                        mods.add(t + " " + lowerFirst(sel));        // "cebolla morada", "queso cheddar"
                }
            }
            if (!mods.isEmpty()) {
                parts.add(upperFirst(String.join(", ", mods)));
            }
            if (!extras.isEmpty()) {
                parts.add("+ " + String.join(", ", extras));
            }
            if (!note.isEmpty()) {
                parts.add(note);
            }
            return String.join(" · ", parts);
        }

        double unitPrice(MenuItem item) {
            double p = item.price;
            for (String x : extras) {
                p += recipe.extraPrice(x);
            }
            return p;
        }
    }

    // ---------- Botón tipo "chip" que se activa/desactiva ----------
    static class ChipToggle extends JToggleButton {

        private final String onText, offText;
        private final Color onBg, onFg, offBg, offFg, offBorder;

        ChipToggle(String onText, String offText, Color onBg, Color onFg,
                Color offBg, Color offFg, Color offBorder) {
            super(onText);
            this.onText = onText;
            this.offText = offText;
            this.onBg = onBg;
            this.onFg = onFg;
            this.offBg = offBg;
            this.offFg = offFg;
            this.offBorder = offBorder;
            setFont(new Font("SansSerif", Font.BOLD, 14));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(110, 40));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            boolean on = isSelected();
            int w = getWidth(), h = getHeight();

            g2.setColor(on ? onBg : offBg);
            g2.fill(new RoundRectangle2D.Double(1, 1, w - 2, h - 2, 22, 22));
            if (!on && offBorder != null) {
                g2.setColor(offBorder);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Double(1, 1, w - 3, h - 3, 22, 22));
            }

            // Achica la letra si el texto no cabe
            String text = on ? onText : offText;
            int size = 14;
            g2.setFont(getFont().deriveFont((float) size));
            while (size > 10 && g2.getFontMetrics().stringWidth(text) > w - 14) {
                size--;
                g2.setFont(getFont().deriveFont((float) size));
            }
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(on ? onFg : offFg);
            g2.drawString(text, (w - fm.stringWidth(text)) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    // =====================================================================
    // ---------- Utilidades compartidas por las ventanas emergentes ----------
    // =====================================================================
    static JPanel dialogRoot() {
        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_IMG_BG);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 30, 30));
                g2.setColor(PANEL_DARK);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 3, getHeight() - 3, 30, 30));
                g2.dispose();
            }
        };
        root.setOpaque(false);
        return root;
    }

    static JPanel dialogHeader() {
        JPanel header = new JPanel(new BorderLayout(18, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PANEL_DARK);
                // más alto que el panel para que solo se redondeen las esquinas de arriba
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight() + 30, 30, 30));
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(18, 22, 18, 18));
        return header;
    }

    static JPanel closeButton(JDialog d) {
        RoundedButton closeBtn = new RoundedButton("X", new Color(0x5A, 0x3A, 0x24), new Color(0x74, 0x4C, 0x30), 16);
        closeBtn.setPreferredSize(new Dimension(42, 42));
        closeBtn.addActionListener(e -> d.dispose());
        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        wrap.setOpaque(false);
        wrap.add(closeBtn);
        return wrap;
    }

    static JPanel sectionTitle(String title, String sub) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.BOLD, 19));
        t.setForeground(BROWN_DARK);
        JLabel s = new JLabel("    " + sub);
        s.setFont(new Font("SansSerif", Font.PLAIN, 13));
        s.setForeground(NOTE_TEXT);
        p.add(t);
        p.add(s);
        p.setBorder(new EmptyBorder(0, 0, 10, 0));
        return stretchX(p);
    }

    static <T extends JComponent> T stretchX(T c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        return c;
    }

    static JTextField styledField() {
        JTextField f = new JTextField();
        f.setFont(new Font("SansSerif", Font.PLAIN, 16));
        f.setForeground(BROWN_DARK);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE_SEP, 2, true),
                new EmptyBorder(8, 12, 8, 12)));
        return f;
    }

    static JScrollPane transparentScroll(JComponent body) {
        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    static void sizeDialog(JDialog d, Window owner, int width) {
        d.pack();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int h = Math.min(d.getHeight() + 10, (int) (screen.height * 0.90));
        d.setSize(width, h);
        d.setLocationRelativeTo(owner);
    }

    static Window windowOf(Component c) {
        return c instanceof Window ? (Window) c : SwingUtilities.getWindowAncestor(c);
    }

    static String money(double v) {
        return String.format("Q %.2f", v);
    }

    // =====================================================================
    // ---------- Ventana para elegir ingredientes ----------
    // =====================================================================
    static class CustomizeDialog extends JDialog {

        private Customization result;
        private final JLabel priceLbl = new JLabel();

        static Customization open(Component parent, MenuItem item, Recipe recipe, Customization existing) {
            CustomizeDialog d = new CustomizeDialog(windowOf(parent), item, recipe, existing);
            d.setVisible(true); // se queda esperando hasta que se cierre
            return d.result;
        }

        private CustomizeDialog(Window owner, MenuItem item, Recipe recipe, Customization existing) {
            super(owner, "Personalizar", Dialog.ModalityType.APPLICATION_MODAL);
            setUndecorated(true);
            try {
                setBackground(new Color(0, 0, 0, 0)); // esquinas redondeadas
            } catch (Exception ignored) {
            }

            final Customization work = existing != null ? existing.copy() : new Customization(recipe);

            JPanel root = dialogRoot();
            setContentPane(root);

            // ----- Encabezado café con imagen y nombre -----
            JPanel header = dialogHeader();
            JLabel img = new JLabel(loadBig(item.imagePath), SwingConstants.CENTER);
            img.setPreferredSize(new Dimension(120, 90));
            header.add(img, BorderLayout.WEST);

            JPanel titles = new JPanel();
            titles.setOpaque(false);
            titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
            JLabel nameLbl = new JLabel("<html><div style='width:420px'>" + item.name + "</div></html>");
            nameLbl.setFont(new Font("SansSerif", Font.BOLD, 24));
            nameLbl.setForeground(WHITE_TEXT);
            JLabel subLbl = new JLabel(money(item.price) + "  ·  Personaliza tu orden");
            subLbl.setFont(new Font("SansSerif", Font.BOLD, 15));
            subLbl.setForeground(YELLOW_BRIGHT);
            titles.add(Box.createVerticalGlue());
            titles.add(nameLbl);
            titles.add(Box.createVerticalStrut(6));
            titles.add(subLbl);
            titles.add(Box.createVerticalGlue());
            header.add(titles, BorderLayout.CENTER);
            header.add(closeButton(this), BorderLayout.EAST);

            // ----- Cuerpo con las opciones -----
            JPanel body = new JPanel();
            body.setOpaque(false);
            body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
            body.setBorder(new EmptyBorder(16, 22, 14, 22));

            // Tipo de carne
            String[] proteins = recipe.proteins();
            if (proteins.length > 0) {
                String sub = recipe.portions > 1
                        ? "Aplica a las " + recipe.portions + " porciones"
                        : "Elige una opción";
                body.add(sectionTitle("Tipo de carne", sub));
                body.add(optionRow("Carne", proteins, work.protein, recipe.defaultProtein(),
                        opt -> work.protein = opt));
                body.add(Box.createVerticalStrut(14));
            }

            // Ingredientes de la receta
            body.add(sectionTitle("Ingredientes", "La opción con punto amarillo es la receta original"));
            for (Ing ing : recipe.ingredients) {
                body.add(optionRow(ing.label, ing.options, work.choiceFor(ing), ing.def,
                        opt -> work.choices.put(ing.label, opt)));
            }
            body.add(Box.createVerticalStrut(14));

            // Preferencias generales (sal, pan)
            body.add(sectionTitle("Preferencias", "Aplica a toda la hamburguesa"));
            for (Ing ing : recipe.general) {
                body.add(optionRow(ing.label, ing.options, work.choiceFor(ing), ing.def,
                        opt -> work.choices.put(ing.label, opt)));
            }
            body.add(Box.createVerticalStrut(14));

            // Extras con costo
            body.add(sectionTitle("Extras", "Tienen costo adicional"));
            JPanel exGrid = new JPanel(new GridLayout(0, 3, 10, 10));
            exGrid.setOpaque(false);
            exGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
            for (Extra x : recipe.extras()) {
                String label = String.format("+ %s  Q%.2f", x.name, x.price);
                ChipToggle chip = new ChipToggle(label, label, GREEN_ADD, Color.WHITE, CARD_BG, BROWN_TEXT, null);
                chip.setPreferredSize(new Dimension(200, 42));
                chip.setSelected(work.extras.contains(x.name));
                chip.addActionListener(e -> {
                    if (chip.isSelected()) {
                        work.extras.add(x.name);
                    } else {
                        work.extras.remove(x.name);
                    }
                    priceLbl.setText(money(work.unitPrice(item)));
                });
                exGrid.add(chip);
            }
            body.add(exGrid);
            body.add(Box.createVerticalStrut(18));

            // Nota libre
            body.add(sectionTitle("Nota adicional", "Opcional"));
            JTextField noteField = styledField();
            noteField.setText(work.note);
            body.add(stretchX(noteField));

            JScrollPane scroll = transparentScroll(body);

            // ----- Pie con precio y botones -----
            JPanel footer = new JPanel(new BorderLayout());
            footer.setOpaque(false);
            footer.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 0, 0, LINE_SEP),
                    new EmptyBorder(14, 22, 18, 22)));

            JPanel totalBox = new JPanel();
            totalBox.setOpaque(false);
            totalBox.setLayout(new BoxLayout(totalBox, BoxLayout.Y_AXIS));
            JLabel totalCap = new JLabel("Precio");
            totalCap.setFont(new Font("SansSerif", Font.PLAIN, 13));
            totalCap.setForeground(NOTE_TEXT);
            priceLbl.setFont(new Font("SansSerif", Font.BOLD, 26));
            priceLbl.setForeground(BROWN_DARK);
            priceLbl.setText(money(work.unitPrice(item)));
            totalBox.add(totalCap);
            totalBox.add(priceLbl);
            footer.add(totalBox, BorderLayout.WEST);

            RoundedButton cancelBtn = new RoundedButton("Cancelar", new Color(0xA8, 0x93, 0x78), new Color(0x93, 0x7E, 0x64), 16);
            cancelBtn.setPreferredSize(new Dimension(130, 50));
            cancelBtn.addActionListener(e -> dispose());

            RoundedButton okBtn = new RoundedButton(existing != null ? "Guardar cambios" : "Agregar a la orden",
                    RED_PAY, RED_PAY_HOVER, 17);
            okBtn.setPreferredSize(new Dimension(220, 50));
            okBtn.addActionListener(e -> {
                work.note = noteField.getText().trim();
                result = work;
                dispose();
            });

            JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            btns.setOpaque(false);
            btns.add(cancelBtn);
            btns.add(okBtn);
            footer.add(btns, BorderLayout.EAST);

            root.add(header, BorderLayout.NORTH);
            root.add(scroll, BorderLayout.CENTER);
            root.add(footer, BorderLayout.SOUTH);

            root.registerKeyboardAction(e -> dispose(),
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                    JComponent.WHEN_IN_FOCUSED_WINDOW);

            sizeDialog(this, owner, 860);
        }

        // Fila: nombre del ingrediente + botones de opciones (solo se elige una)
        private static JPanel optionRow(String label, String[] options, String selected, String original,
                java.util.function.Consumer<String> onPick) {
            JPanel row = new JPanel(new BorderLayout(12, 0));
            row.setOpaque(false);
            row.setBorder(new EmptyBorder(0, 0, 8, 0));

            JLabel l = new JLabel(label);
            l.setFont(new Font("SansSerif", Font.BOLD, 15));
            l.setForeground(BROWN_DARK);
            l.setPreferredSize(new Dimension(190, 40));
            row.add(l, BorderLayout.WEST);

            JPanel chips = new JPanel(new GridLayout(1, 5, 8, 0));
            chips.setOpaque(false);
            ButtonGroup group = new ButtonGroup();
            for (String opt : options) {
                boolean isSin = opt.equals("Sin");
                String text = opt;
                ChipToggle chip = new ChipToggle(text, text,
                        isSin ? RED_PAY : BROWN_DARK, Color.WHITE,
                        CARD_BG, BROWN_TEXT, null) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        // punto amarillo = opción original de la receta
                        if (opt.equals(original)) {
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g2.setColor(YELLOW_BRIGHT);
                            g2.fillOval(getWidth() - 14, 6, 8, 8);
                            g2.dispose();
                        }
                    }
                };
                chip.setSelected(opt.equals(selected));
                chip.addActionListener(e -> onPick.accept(opt));
                group.add(chip);
                chips.add(chip);
            }
            for (int i = options.length; i < 5; i++) {
                JLabel filler = new JLabel();
                chips.add(filler);
            }
            row.add(chips, BorderLayout.CENTER);
            return stretchX(row);
        }

        private static ImageIcon loadBig(String path) {
            if (path == null) {
                return null;
            }
            java.net.URL url = CustomizeDialog.class.getResource(path);
            if (url == null) {
                return null;
            }
            ImageIcon raw = new ImageIcon(url);
            int iw = raw.getIconWidth(), ih = raw.getIconHeight();
            if (iw <= 0 || ih <= 0) {
                return null;
            }
            double s = Math.min(120.0 / iw, 90.0 / ih);
            return new ImageIcon(raw.getImage().getScaledInstance(
                    Math.max(1, (int) (iw * s)), Math.max(1, (int) (ih * s)), Image.SCALE_SMOOTH));
        }
    }

    // =====================================================================
    // ---------- PAGO: efectivo, tarjeta, mixto y cupones ----------
    // =====================================================================

    static class Coupon {

        final String code;
        final double percent;   // ej. 10 = 10%
        final double fixed;     // ej. 15 = Q15 de descuento
        final String description;

        Coupon(String code, double percent, double fixed, String description) {
            this.code = code;
            this.percent = percent;
            this.fixed = fixed;
            this.description = description;
        }

        double discountFor(double subtotal) {
            double d = subtotal * percent / 100.0 + fixed;
            return Math.min(round2(d), subtotal);
        }
    }

    // Cupones válidos (agrega o cambia los que quieras aquí; se escriben en MAYÚSCULAS)
    static final Map<String, Coupon> COUPONS = new HashMap<>();

    static {
        COUPONS.put("GIT10", new Coupon("GIT10", 10, 0, "10% de descuento"));
        COUPONS.put("GIT20", new Coupon("GIT20", 20, 0, "20% de descuento"));
        COUPONS.put("EMPLEADO", new Coupon("EMPLEADO", 25, 0, "25% descuento de empleado"));
        COUPONS.put("DESC15", new Coupon("DESC15", 0, 15, "Q15.00 de descuento"));
    }

    static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    // Convierte texto a número. Vacío = 0, inválido = -1
    static double parseMoney(String s) {
        s = s.replace("Q", "").replace(",", "").trim();
        if (s.isEmpty()) {
            return 0;
        }
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** Resultado del pago: total, descuento/cupón aplicados, cómo se dividió el pago y datos para el comprobante. */
    public static class PaymentResult {

        public String method = "Efectivo";
        public double subtotal, discount, total, card, cash, cashReceived, change;
        public String couponCode;
        public String cardLast4 = "";

        public PaymentResult() {
        }

        /** Constructor corto (total, descuento, cupón, efectivo, tarjeta). */
        public PaymentResult(double total, double discount, String couponCode, double cash, double card) {
            this.total = total;
            this.discount = discount;
            this.couponCode = couponCode;
            this.cash = cash;
            this.card = card;
            this.subtotal = total + discount;
            this.cashReceived = cash;
            if (card > 0 && cash > 0) {
                this.method = "Mixto";
            } else if (card > 0) {
                this.method = "Tarjeta";
            }
        }

        String summary() {
            StringBuilder sb = new StringBuilder();
            sb.append("Subtotal: ").append(money(subtotal)).append("\n");
            if (couponCode != null) {
                sb.append("Cupón ").append(couponCode).append(": -").append(money(discount)).append("\n");
            }
            sb.append("TOTAL PAGADO: ").append(money(total)).append("\n\n");
            sb.append("Método: ").append(method).append("\n");
            if (card > 0) {
                sb.append("   Tarjeta: ").append(money(card));
                if (!cardLast4.isEmpty()) {
                    sb.append("  (**** ").append(cardLast4).append(")");
                }
                sb.append("\n");
            }
            if (cash > 0) {
                sb.append("   Efectivo: ").append(money(cash)).append("\n");
                sb.append("   Recibido: ").append(money(cashReceived)).append("\n");
                sb.append("   Cambio: ").append(money(change)).append("\n");
            }
            return sb.toString();
        }
    }

    static class PaymentDialog extends JDialog {

        private PaymentResult result;

        private final double subtotal;
        private Coupon coupon;
        private String method = "Efectivo";

        private final JLabel discountLbl = new JLabel();
        private final JLabel totalLbl = new JLabel();
        private final JLabel footerTotal = new JLabel();
        private final JLabel couponStatus = new JLabel(" ");

        // Efectivo
        private final JTextField cashReceived = styledField();
        private final JLabel cashChange = new JLabel();
        // Tarjeta
        private final JTextField cardLast4 = styledField();
        private final JLabel cardInfo = new JLabel();
        // Mixto
        private final JTextField mixCard = styledField();
        private final JTextField mixCashReceived = styledField();
        private final JTextField mixLast4 = styledField();
        private final JLabel mixCashPart = new JLabel();
        private final JLabel mixChange = new JLabel();

        private final CardLayout methodCards = new CardLayout();
        private final JPanel methodPanel = new JPanel(methodCards);

        static PaymentResult open(Component parent, double subtotal) {
            PaymentDialog d = new PaymentDialog(windowOf(parent), subtotal);
            d.setVisible(true);
            return d.result;
        }

        private PaymentDialog(Window owner, double subtotal) {
            super(owner, "Pago", Dialog.ModalityType.APPLICATION_MODAL);
            this.subtotal = round2(subtotal);
            setUndecorated(true);
            try {
                setBackground(new Color(0, 0, 0, 0));
            } catch (Exception ignored) {
            }

            JPanel root = dialogRoot();
            setContentPane(root);

            // ----- Encabezado -----
            JPanel header = dialogHeader();
            JPanel titles = new JPanel();
            titles.setOpaque(false);
            titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
            JLabel t = new JLabel("Pago de la orden");
            t.setFont(new Font("SansSerif", Font.BOLD, 26));
            t.setForeground(WHITE_TEXT);
            JLabel s = new JLabel("Subtotal: " + money(this.subtotal));
            s.setFont(new Font("SansSerif", Font.BOLD, 15));
            s.setForeground(YELLOW_BRIGHT);
            titles.add(t);
            titles.add(Box.createVerticalStrut(6));
            titles.add(s);
            header.add(titles, BorderLayout.CENTER);
            header.add(closeButton(this), BorderLayout.EAST);

            // ----- Cuerpo -----
            JPanel body = new JPanel();
            body.setOpaque(false);
            body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
            body.setBorder(new EmptyBorder(16, 22, 14, 22));

            // Cupón
            body.add(sectionTitle("Cupón de descuento", "Opcional"));
            JTextField couponField = styledField();
            RoundedButton applyBtn = new RoundedButton("Aplicar", GREEN_ADD, GREEN_ADD_HOVER, 15);
            applyBtn.setPreferredSize(new Dimension(120, 42));
            RoundedButton removeBtn = new RoundedButton("Quitar", new Color(0xA8, 0x93, 0x78), new Color(0x93, 0x7E, 0x64), 15);
            removeBtn.setPreferredSize(new Dimension(100, 42));
            JPanel couponRow = new JPanel(new BorderLayout(10, 0));
            couponRow.setOpaque(false);
            couponRow.add(couponField, BorderLayout.CENTER);
            JPanel couponBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            couponBtns.setOpaque(false);
            couponBtns.add(applyBtn);
            couponBtns.add(removeBtn);
            couponRow.add(couponBtns, BorderLayout.EAST);
            body.add(stretchX(couponRow));
            couponStatus.setFont(new Font("SansSerif", Font.BOLD, 13));
            couponStatus.setBorder(new EmptyBorder(6, 4, 0, 0));
            body.add(stretchX(couponStatus));

            applyBtn.addActionListener(e -> {
                String code = couponField.getText().trim().toUpperCase();
                Coupon c = COUPONS.get(code);
                if (c == null) {
                    coupon = null;
                    couponStatus.setForeground(RED_PAY);
                    couponStatus.setText(code.isEmpty() ? "Escribe un código de cupón" : "El cupón \"" + code + "\" no es válido");
                } else {
                    coupon = c;
                    couponStatus.setForeground(GREEN_ADD);
                    couponStatus.setText("Cupón " + c.code + " aplicado: " + c.description);
                }
                recalc();
            });
            removeBtn.addActionListener(e -> {
                coupon = null;
                couponField.setText("");
                couponStatus.setText(" ");
                recalc();
            });
            body.add(Box.createVerticalStrut(14));

            // Resumen
            RoundedPanel summary = new RoundedPanel(CARD_BG, 18);
            summary.setLayout(new GridLayout(0, 2, 0, 6));
            summary.setBorder(new EmptyBorder(14, 18, 14, 18));
            summary.add(summaryLabel("Subtotal", false));
            JLabel subV = summaryLabel(money(this.subtotal), false);
            subV.setHorizontalAlignment(SwingConstants.RIGHT);
            summary.add(subV);
            summary.add(summaryLabel("Descuento", false));
            discountLbl.setFont(new Font("SansSerif", Font.PLAIN, 16));
            discountLbl.setForeground(GREEN_ADD);
            discountLbl.setHorizontalAlignment(SwingConstants.RIGHT);
            summary.add(discountLbl);
            summary.add(summaryLabel("Total a pagar", true));
            totalLbl.setFont(new Font("SansSerif", Font.BOLD, 22));
            totalLbl.setForeground(BROWN_DARK);
            totalLbl.setHorizontalAlignment(SwingConstants.RIGHT);
            summary.add(totalLbl);
            body.add(stretchX(summary));
            body.add(Box.createVerticalStrut(18));

            // Método de pago
            body.add(sectionTitle("Método de pago", "Elige cómo paga el cliente"));
            JPanel methods = new JPanel(new GridLayout(1, 3, 10, 0));
            methods.setOpaque(false);
            ButtonGroup mg = new ButtonGroup();
            String[] names = {"Efectivo", "Tarjeta", "Mixto (tarjeta + efectivo)"};
            for (String n : names) {
                ChipToggle chip = new ChipToggle(n, n, RED_PAY, Color.WHITE, CARD_BG, BROWN_TEXT, null);
                chip.setPreferredSize(new Dimension(180, 48));
                chip.setSelected(n.equals("Efectivo"));
                chip.addActionListener(e -> {
                    method = n.startsWith("Mixto") ? "Mixto" : n;
                    methodCards.show(methodPanel, method);
                    if (method.equals("Mixto") && mixCard.getText().trim().isEmpty()) {
                        mixCard.setText(String.format("%.2f", round2(total() / 2)));
                    }
                    recalc();
                });
                mg.add(chip);
                methods.add(chip);
            }
            body.add(stretchX(methods));
            body.add(Box.createVerticalStrut(14));

            methodPanel.setOpaque(false);
            methodPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            methodPanel.add(buildCashPanel(), "Efectivo");
            methodPanel.add(buildCardPanel(), "Tarjeta");
            methodPanel.add(buildMixPanel(), "Mixto");
            body.add(methodPanel);

            JScrollPane scroll = transparentScroll(body);

            // ----- Pie -----
            JPanel footer = new JPanel(new BorderLayout());
            footer.setOpaque(false);
            footer.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 0, 0, LINE_SEP),
                    new EmptyBorder(14, 22, 18, 22)));
            JPanel totalBox = new JPanel();
            totalBox.setOpaque(false);
            totalBox.setLayout(new BoxLayout(totalBox, BoxLayout.Y_AXIS));
            JLabel cap = new JLabel("Total");
            cap.setFont(new Font("SansSerif", Font.PLAIN, 13));
            cap.setForeground(NOTE_TEXT);
            footerTotal.setFont(new Font("SansSerif", Font.BOLD, 26));
            footerTotal.setForeground(BROWN_DARK);
            totalBox.add(cap);
            totalBox.add(footerTotal);
            footer.add(totalBox, BorderLayout.WEST);

            RoundedButton cancelBtn = new RoundedButton("Cancelar", new Color(0xA8, 0x93, 0x78), new Color(0x93, 0x7E, 0x64), 16);
            cancelBtn.setPreferredSize(new Dimension(130, 50));
            cancelBtn.addActionListener(e -> dispose());
            RoundedButton payBtn = new RoundedButton("Confirmar pago", RED_PAY, RED_PAY_HOVER, 17);
            payBtn.setPreferredSize(new Dimension(210, 50));
            payBtn.addActionListener(e -> confirm());
            JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            btns.setOpaque(false);
            btns.add(cancelBtn);
            btns.add(payBtn);
            footer.add(btns, BorderLayout.EAST);

            root.add(header, BorderLayout.NORTH);
            root.add(scroll, BorderLayout.CENTER);
            root.add(footer, BorderLayout.SOUTH);

            root.registerKeyboardAction(e -> dispose(),
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                    JComponent.WHEN_IN_FOCUSED_WINDOW);

            // Recalcular cada vez que se escribe en un campo
            javax.swing.event.DocumentListener dl = new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    recalc();
                }

                public void removeUpdate(javax.swing.event.DocumentEvent e) {
                    recalc();
                }

                public void changedUpdate(javax.swing.event.DocumentEvent e) {
                    recalc();
                }
            };
            cashReceived.getDocument().addDocumentListener(dl);
            mixCard.getDocument().addDocumentListener(dl);
            mixCashReceived.getDocument().addDocumentListener(dl);

            recalc();
            sizeDialog(this, owner, 700);
        }

        private JLabel summaryLabel(String text, boolean bold) {
            JLabel l = new JLabel(text);
            l.setFont(new Font("SansSerif", bold ? Font.BOLD : Font.PLAIN, bold ? 20 : 16));
            l.setForeground(BROWN_DARK);
            return l;
        }

        private JLabel fieldLabel(String text) {
            JLabel l = new JLabel(text);
            l.setFont(new Font("SansSerif", Font.BOLD, 15));
            l.setForeground(BROWN_DARK);
            l.setBorder(new EmptyBorder(8, 0, 6, 0));
            return stretchX(l);
        }

        private JLabel infoLabel(JLabel l) {
            l.setText("Texto de ejemplo"); // para que calcule bien la altura
            l.setFont(new Font("SansSerif", Font.BOLD, 17));
            l.setForeground(BROWN_DARK);
            l.setBorder(new EmptyBorder(10, 0, 0, 0));
            return stretchX(l);
        }

        private JPanel column() {
            JPanel p = new JPanel();
            p.setOpaque(false);
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            return p;
        }

        private JPanel buildCashPanel() {
            JPanel p = column();
            p.add(fieldLabel("Monto recibido en efectivo"));
            p.add(stretchX(cashReceived));
            p.add(Box.createVerticalStrut(10));

            // Botones rápidos
            JPanel quick = new JPanel(new GridLayout(1, 5, 8, 0));
            quick.setOpaque(false);
            String[] labels = {"Exacto", "Q 50", "Q 100", "Q 200", "Q 500"};
            for (String q : labels) {
                RoundedButton b = new RoundedButton(q, new Color(0x8A, 0x63, 0x42), new Color(0x74, 0x50, 0x33), 15);
                b.setPreferredSize(new Dimension(90, 40));
                b.addActionListener(e -> {
                    if (q.equals("Exacto")) {
                        cashReceived.setText(String.format("%.2f", total()));
                    } else {
                        cashReceived.setText(q.replace("Q ", "") + ".00");
                    }
                });
                quick.add(b);
            }
            p.add(stretchX(quick));
            p.add(infoLabel(cashChange));
            return p;
        }

        private JPanel buildCardPanel() {
            JPanel p = column();
            p.add(fieldLabel("Últimos 4 dígitos de la tarjeta (opcional)"));
            p.add(stretchX(cardLast4));
            p.add(infoLabel(cardInfo));
            return p;
        }

        private JPanel buildMixPanel() {
            JPanel p = column();

            JPanel cardRow = new JPanel(new BorderLayout(10, 0));
            cardRow.setOpaque(false);
            cardRow.add(mixCard, BorderLayout.CENTER);
            RoundedButton half = new RoundedButton("Mitad y mitad", new Color(0x8A, 0x63, 0x42), new Color(0x74, 0x50, 0x33), 15);
            half.setPreferredSize(new Dimension(160, 42));
            half.addActionListener(e -> mixCard.setText(String.format("%.2f", round2(total() / 2))));
            cardRow.add(half, BorderLayout.EAST);

            p.add(fieldLabel("Monto que se cobra con tarjeta"));
            p.add(stretchX(cardRow));
            p.add(infoLabel(mixCashPart));
            p.add(fieldLabel("Efectivo recibido"));
            p.add(stretchX(mixCashReceived));
            p.add(infoLabel(mixChange));
            p.add(fieldLabel("Últimos 4 dígitos de la tarjeta (opcional)"));
            p.add(stretchX(mixLast4));
            return p;
        }

        private double discount() {
            return coupon == null ? 0 : coupon.discountFor(subtotal);
        }

        private double total() {
            return round2(subtotal - discount());
        }

        private void recalc() {
            double total = total();
            discountLbl.setText(discount() > 0 ? "- " + money(discount()) : money(0));
            totalLbl.setText(money(total));
            footerTotal.setText(money(total));

            // Efectivo
            double rec = parseMoney(cashReceived.getText());
            if (rec < 0) {
                setInfo(cashChange, "Monto no válido", RED_PAY);
            } else if (rec == 0) {
                setInfo(cashChange, "Cambio: " + money(0), BROWN_DARK);
            } else if (rec < total) {
                setInfo(cashChange, "Faltan " + money(total - rec), RED_PAY);
            } else {
                setInfo(cashChange, "Cambio: " + money(rec - total), GREEN_ADD);
            }

            // Tarjeta
            setInfo(cardInfo, "Cobrar " + money(total) + " en la terminal (POS)", BROWN_DARK);

            // Mixto
            double card = parseMoney(mixCard.getText());
            if (card < 0 || card > total) {
                setInfo(mixCashPart, "El monto con tarjeta no es válido", RED_PAY);
                setInfo(mixChange, " ", BROWN_DARK);
            } else {
                double cashPart = round2(total - card);
                setInfo(mixCashPart, "Resta en efectivo: " + money(cashPart), BROWN_DARK);
                double mrec = parseMoney(mixCashReceived.getText());
                if (mrec < 0) {
                    setInfo(mixChange, "Monto no válido", RED_PAY);
                } else if (mrec < cashPart) {
                    setInfo(mixChange, "Faltan " + money(cashPart - mrec), RED_PAY);
                } else {
                    setInfo(mixChange, "Cambio: " + money(mrec - cashPart), GREEN_ADD);
                }
            }
        }

        private void setInfo(JLabel l, String text, Color c) {
            l.setText(text);
            l.setForeground(c);
        }

        private boolean validLast4(String s) {
            return s.isEmpty() || s.matches("\\d{4}");
        }

        private void warn(String msg) {
            JOptionPane.showMessageDialog(this, msg, "Revisa el pago", JOptionPane.WARNING_MESSAGE);
        }

        private void confirm() {
            double total = total();
            PaymentResult r = new PaymentResult();
            r.method = method;
            r.subtotal = subtotal;
            r.discount = discount();
            r.total = total;
            r.couponCode = coupon == null ? null : coupon.code;

            switch (method) {
                case "Efectivo": {
                    double rec = parseMoney(cashReceived.getText());
                    if (rec < total) {
                        warn("El efectivo recibido no alcanza para cubrir " + money(total) + ".");
                        return;
                    }
                    r.cash = total;
                    r.cashReceived = rec;
                    r.change = round2(rec - total);
                    break;
                }
                case "Tarjeta": {
                    String l4 = cardLast4.getText().trim();
                    if (!validLast4(l4)) {
                        warn("Los últimos dígitos deben ser 4 números.");
                        return;
                    }
                    r.card = total;
                    r.cardLast4 = l4;
                    break;
                }
                default: { // Mixto
                    double card = parseMoney(mixCard.getText());
                    if (card <= 0 || card >= total) {
                        warn("En pago mixto, el monto con tarjeta debe ser mayor a Q0 y menor al total.");
                        return;
                    }
                    double cashPart = round2(total - card);
                    double rec = parseMoney(mixCashReceived.getText());
                    if (rec < cashPart) {
                        warn("El efectivo recibido no alcanza para cubrir " + money(cashPart) + ".");
                        return;
                    }
                    String l4 = mixLast4.getText().trim();
                    if (!validLast4(l4)) {
                        warn("Los últimos dígitos deben ser 4 números.");
                        return;
                    }
                    r.card = round2(card);
                    r.cash = cashPart;
                    r.cashReceived = rec;
                    r.change = round2(rec - cashPart);
                    r.cardLast4 = l4;
                }
            }
            result = r;
            dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            pantallaCajero frame = new pantallaCajero();
            frame.setVisible(true);
        });
    }
}