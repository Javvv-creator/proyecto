package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.*;
import java.util.List;

/**
 * MenuPOS - Interfaz de punto de venta para restaurante de hamburguesas.
 * Replica el diseño: barra de categorías, grilla de productos y panel de orden
 * actual.
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

    // ---------- Datos ----------
    // ---------- Datos ----------
    static class MenuItem {

        String name;
        double price;
        String category;
        String emoji;
        String imagePath;

        // Constructor original (usa el nombre para generar la ruta automáticamente)
        MenuItem(String name, double price, String category, String emoji) {
            this.name = name;
            this.price = price;
            this.category = category;
            this.emoji = emoji;
            // Genera la ruta a partir del nombre
            this.imagePath = "/gui/images/" + name.toLowerCase()
                    .replace(" ", "_")
                    .replace("á", "a").replace("é", "e").replace("í", "i")
                    .replace("ó", "o").replace("ú", "u").replace("ñ", "n")
                    + ".png";
        }

        // Nuevo constructor (acepta directamente el nombre del archivo de imagen)
        MenuItem(String name, double price, String category, String emoji, String imageFile) {
            this.name = name;
            this.price = price;
            this.category = category;
            this.emoji = emoji;
            // Usa directamente el archivo que le pases
            this.imagePath = "/gui/images/" + imageFile;
        }
    }

    private final Map<String, List<MenuItem>> catalog = new LinkedHashMap<>();

    public pantallaCajero() {
        super("Pantalla - Cajero");

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
        leftPanel.add(tabsBar, BorderLayout.NORTH);

        JPanel centerArea = new JPanel(new BorderLayout());
        centerArea.setOpaque(false);
        centerArea.setBorder(new EmptyBorder(25, 0, 0, 0));

        sectionTitle = new JLabel("Hamburguesas");
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
        orderPanel.setPreferredSize(new Dimension(430, 100));
        root.add(orderPanel, BorderLayout.EAST);

        showCategory("Todos");

        // Salir con ESC (ya que la ventana no tiene decoración)
        root.registerKeyboardAction(e -> System.exit(0),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void buildCatalog() {
        // ----- Hamburguesas (todo junto aquí) -----
        List<MenuItem> hamburguesas = new ArrayList<>();

// Mensajito de Desayunos
        hamburguesas.add(new MenuItem("🍳 Desayunos", 0.00, "Hamburguesas", "📌"));
// Desayunos
        hamburguesas.add(new MenuItem("McMuffin Cheddar McMelt", 28.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("McMuffin Tocino Doble Huevo", 30.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("McMuffin Salchicha y doble huevo", 30.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("Egg McMuffin Doble Huevo", 32.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("McMuffin de Salchicha y Huevo", 28.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("McMuffin de Salchicha", 26.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("McMuffin Chapín Con Salchicha", 30.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("Egg McMuffin", 26.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("McMuffin Super Chapín Con Salchicha", 32.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("Egg McMuffin Doble", 30.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("McMuffin de Tocino y Huevo", 28.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("McMuffin Super Chapín Con Jamón", 32.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("McMuffin de Salchicha Doble y Huevo", 32.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("McMuffin Tocino Doble y Huevo", 32.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("McMuffin Huevo y Frijol", 25.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("McMuffin Huevo y Queso", 26.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("McMuffin Chapín Con Jamón", 28.00, "Hamburguesas", "🍔"));

// Mensajito de Hamburguesas
        hamburguesas.add(new MenuItem("🍔 Hamburguesas", 0.00, "Hamburguesas", "📌"));
// Hamburguesas
        hamburguesas.add(new MenuItem("Bacon Cheddar McMelt", 38.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("McCrispy Bacon Cheddar", 40.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("Git Mac Doble", 41.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("Cuarto de Libra con Queso", 39.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("Cuarto de Libra Doble con Queso", 44.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("Cuarto de Libra Deluxe con Queso", 42.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("Cuarto de Libra Deluxe Doble con Queso", 46.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("Cuarto de Libra Bacon con Queso", 42.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("Cuarto de Libra Bacon Doble con Queso", 46.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("Big Tasty", 48.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("Big Tasty Doble", 55.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("Big Tasty Bacon", 50.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("Big Tasty Bacon Doble", 58.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("Triple Bacon", 60.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("Quesoburguesa", 32.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("Quesoburguesa Doble", 44.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("Quesoburguesa Triple", 46.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("Hamburguesa", 20.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("Hamburguesa Jr.", 18.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("GitNífica de Res", 42.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("GitNífica de Res Doble", 48.00, "Hamburguesas", "🍔"));

// Mensajito de Pollo
        hamburguesas.add(new MenuItem("🐔 Pollo", 0.00, "Hamburguesas", "📌"));
// Pollo
        hamburguesas.add(new MenuItem("McCrispy Chicken Bacon Ranch", 42.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("McCrispy Chicken Deluxe", 40.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("Big Tasty de Pollo", 48.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("Sándwich McPollo Doble", 49.00, "Hamburguesas", "🍔"));

// Mensajito de Gourmet
        hamburguesas.add(new MenuItem("🥩 Creaciones Gourmet", 0.00, "Hamburguesas", "📌"));
// Gourmet
        hamburguesas.add(new MenuItem("Smoke Tocino Gourmet de Res", 50.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("Smoke Tocino Gourmet doble", 58.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("Clásica Gourmet Res", 48.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("Clásica Gourmet Res doble", 55.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("Pico Guacamol Gourmet Res", 50.00, "Hamburguesas", "🍔"));
        hamburguesas.add(new MenuItem("Pico Guacamol Gourmet doble", 58.00, "Hamburguesas", "🍔"));

        // ----- Otras categorías vacías -----
        // ----- Bebidas -----
        List<MenuItem> bebidas = new ArrayList<>();

        // Mensajito de Bebidas Frías
        bebidas.add(new MenuItem("🥤 Bebidas Frías", 0.00, "Bebidas", "📌"));
        bebidas.add(new MenuItem("Horchata", 15.00, "Bebidas", "🥤", "horchata.png"));
        bebidas.add(new MenuItem("Iced Coffee Horchata", 22.00, "Bebidas", "🥤", "iced_coffee_horchata.png"));
        bebidas.add(new MenuItem("GITFizz Pink", 20.00, "Bebidas", "🥤", "mc_fizzpink.png"));
        bebidas.add(new MenuItem("GITFizz Manzana Verde", 20.00, "Bebidas", "🥤", "mc_fizz_manzana_verde.png"));
        bebidas.add(new MenuItem("GITFizz Blue", 20.00, "Bebidas", "🥤", "mc_fizz_blue.png"));
        bebidas.add(new MenuItem("Frappé Oreo", 32.00, "Bebidas", "🥤", "frappe_oreo.png"));
        bebidas.add(new MenuItem("Frappé Original", 28.00, "Bebidas", "🥤", "frappe_original.png"));
        bebidas.add(new MenuItem("Frappé Vainilla", 30.00, "Bebidas", "🥤", "frappe_vainilla.png"));
        bebidas.add(new MenuItem("Frappé Chocolate", 30.00, "Bebidas", "🥤", "frappe_chocolate.png"));
        bebidas.add(new MenuItem("Frappé Caramelo", 30.00, "Bebidas", "🥤", "frappe_caramelo.png"));
        bebidas.add(new MenuItem("Frappé Vainilla Light", 28.00, "Bebidas", "🥤", "frappe_vainilla_light.png"));
        bebidas.add(new MenuItem("Iced Coffee Original", 22.00, "Bebidas", "🥤", "iced_coffee_original.png"));
        bebidas.add(new MenuItem("Iced Coffee Vainilla", 24.00, "Bebidas", "🥤", "iced_coffee_vainilla.png"));
        bebidas.add(new MenuItem("Iced Coffee Vainilla Light", 24.00, "Bebidas", "🥤", "iced_coffee_vainilla_light.png"));
        bebidas.add(new MenuItem("Iced Coffee Chocolate", 24.00, "Bebidas", "🥤", "iced_coffee_chocolate.png"));
        bebidas.add(new MenuItem("Iced Coffee Caramelo", 24.00, "Bebidas", "🥤", "iced_coffee_caramelo.png"));
        bebidas.add(new MenuItem("Té Chai Frappé Té Verde", 28.00, "Bebidas", "🥤", "te_chai_frappe_te_verde.png"));
        bebidas.add(new MenuItem("Té Chai Frappé Original", 28.00, "Bebidas", "🥤", "te_chai_frappe_original.png"));
        bebidas.add(new MenuItem("Té Chai Frappé Vainilla", 30.00, "Bebidas", "🥤", "te_chai_frappe_vainilla.png"));
        bebidas.add(new MenuItem("Té Chai Frappé Vainilla Light", 28.00, "Bebidas", "🥤", "te_chai_frappe_vainilla_light.png"));
        bebidas.add(new MenuItem("Smoothie de Berries", 35.00, "Bebidas", "🥤", "smoothie_de_berries.png"));
        bebidas.add(new MenuItem("Smoothie de Mango", 35.00, "Bebidas", "🥤", "smoothie_de_mango.png"));
        bebidas.add(new MenuItem("GITFizz A.M.", 22.00, "Bebidas", "🥤", "mc_fizz_am.png"));

        // Mensajito de Bebidas Calientes
        bebidas.add(new MenuItem("☕ Bebidas Calientes", 0.00, "Bebidas", "📌"));
        bebidas.add(new MenuItem("Té Chai Original", 18.00, "Bebidas", "☕", "te_chai_original.png"));
        bebidas.add(new MenuItem("Té Chai Té Verde", 18.00, "Bebidas", "☕", "te_chai_te_verde.png"));
        bebidas.add(new MenuItem("Té Chai Vainilla", 20.00, "Bebidas", "☕", "te_chai_vainilla.png"));
        bebidas.add(new MenuItem("Té Chai Vainilla Light", 20.00, "Bebidas", "☕", "te_chai_vainilla_light.png"));
        bebidas.add(new MenuItem("Cappuccino", 22.00, "Bebidas", "☕", "cappuccino.png"));
        bebidas.add(new MenuItem("Latte", 22.00, "Bebidas", "☕", "latte.png"));
        bebidas.add(new MenuItem("Café Guatemalteco", 18.00, "Bebidas", "☕", "cafe_guatemalteco.png"));
        bebidas.add(new MenuItem("Té Guatemalteco Manzanilla Relax", 15.00, "Bebidas", "☕", "te_guatemalteco_manzanilla_relax.png"));
        bebidas.add(new MenuItem("Té Guatemalteco Melocotón Mix", 15.00, "Bebidas", "☕", "te_guatemalteco_melocoton_mix.png"));
        bebidas.add(new MenuItem("Té Guatemalteco Bora Bora", 15.00, "Bebidas", "☕", "te_guatemalteco_borabora.png"));
        bebidas.add(new MenuItem("Té Guatemalteco Menta Fusión", 15.00, "Bebidas", "☕", "te_guatemalteco_menta_fusion.png"));
        bebidas.add(new MenuItem("Chocolate caliente", 20.00, "Bebidas", "☕", "chocolate_caliente.png"));

        // Mensajito de Café en Bolsa
        bebidas.add(new MenuItem("🛍️ Café en Bolsa", 0.00, "Bebidas", "📌"));
        bebidas.add(new MenuItem("Blend Molido", 45.00, "Bebidas", "☕", "blend_molido.png"));
        bebidas.add(new MenuItem("Blend Grano", 45.00, "Bebidas", "☕", "blend_grano.png"));

        // Mensajito de Sodas
        bebidas.add(new MenuItem("🥤 Sodas", 0.00, "Bebidas", "📌"));
        bebidas.add(new MenuItem("Sprite", 10.00, "Bebidas", "🥤", "sprite.png"));
        bebidas.add(new MenuItem("Coca-Cola", 10.00, "Bebidas", "🥤", "coca_cola.png"));
        bebidas.add(new MenuItem("Coca Cola Zero", 10.00, "Bebidas", "🥤", "coca_cola_zero.png"));
        bebidas.add(new MenuItem("Fanta", 10.00, "Bebidas", "🥤", "fanta.png"));

        // Mensajito de Naturales
        bebidas.add(new MenuItem("🍹 Naturales", 0.00, "Bebidas", "📌"));
        bebidas.add(new MenuItem("Jugo de Naranja", 12.00, "Bebidas", "🥤", "jugo_de_naranja.png"));
        bebidas.add(new MenuItem("Té Lipton", 12.00, "Bebidas", "🥤", "te_lipton.png"));
        bebidas.add(new MenuItem("Rosa de Jamaica", 12.00, "Bebidas", "🥤", "rosa_de_jamaica.png"));
        bebidas.add(new MenuItem("Agua Pura", 8.00, "Bebidas", "🥤", "agua_pura.png"));
        bebidas.add(new MenuItem("Jugo de Manzana", 12.00, "Bebidas", "🥤", "jugo_de_manzana.png"));

        // Mensajito de Calientes
        bebidas.add(new MenuItem("🔥 Calientes", 0.00, "Bebidas", "📌"));
        bebidas.add(new MenuItem("Café", 15.00, "Bebidas", "☕", "cafe.png"));
        bebidas.add(new MenuItem("Café Con Leche", 18.00, "Bebidas", "☕", "cafe_con_leche.png"));
        bebidas.add(new MenuItem("Chocolate", 18.00, "Bebidas", "☕", "chocolate.png"));
        bebidas.add(new MenuItem("Té Caliente", 15.00, "Bebidas", "☕", "te_caliente.png"));

        List<MenuItem> postres = new ArrayList<>();
        List<MenuItem> combos = new ArrayList<>();

// ----- Guardar en catálogo -----
        catalog.put("Hamburguesas", hamburguesas);
        catalog.put("Bebidas", bebidas);
        catalog.put("Postres", new ArrayList<>());
        catalog.put("Combos", new ArrayList<>());

// ----- Todos -----
        List<MenuItem> todos = new ArrayList<>();
        for (List<MenuItem> l : catalog.values()) {
            todos.addAll(l);
        }
        catalog.put("Todos", todos);

    }

    private JPanel buildTabsBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        bar.setOpaque(false);
        String[] cats = {"Todos", "Hamburguesas", "Bebidas", "Postres", "Combos"};
        for (String cat : cats) {
            JButton btn = new PillButton(cat);
            btn.addActionListener(e -> showCategory(cat));
            tabButtons.put(cat, btn);
            bar.add(btn);
        }
        return bar;
    }

    private void showCategory(String category) {
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
            for (int i = 0; i < items.size(); i++) {
                gc.gridx = i % cols;
                gc.gridy = i / cols;
                add(new ItemCard(items.get(i)), gc);
            }
            revalidate();
            repaint();
        }
    }

    // ---------- Tarjeta de producto ----------
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
            java.net.URL imgURL = getClass().getResource(item.imagePath);
            if (imgURL != null) {
                ImageIcon rawIcon = new ImageIcon(imgURL);
                Image scaled = rawIcon.getImage().getScaledInstance(140, 110, Image.SCALE_SMOOTH);
                pictureLabel.setIcon(new ImageIcon(scaled));
            } else {
                // Si la imagen no existe en la ruta dada, muestra el emoji de respaldo
                pictureLabel.setText(item.emoji);
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

            JLabel priceLabel = new JLabel(String.format("Q %.2f", item.price), SwingConstants.CENTER);
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

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    orderPanel.addItem(item);
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
            title.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel cashierRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            cashierRow.setOpaque(false);
            cashierRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel l1 = new JLabel("Cajero: ");
            l1.setForeground(WHITE_TEXT);
            l1.setFont(new Font("SansSerif", Font.PLAIN, 15));
            JLabel l2 = new JLabel("Luis Muñoz");
            l2.setForeground(YELLOW_BRIGHT);
            l2.setFont(new Font("SansSerif", Font.BOLD, 15));
            JLabel l3 = new JLabel("  -   Turno Tarde");
            l3.setForeground(WHITE_TEXT);
            l3.setFont(new Font("SansSerif", Font.PLAIN, 15));
            cashierRow.add(l1);
            cashierRow.add(l2);
            cashierRow.add(l3);

            JSeparator sep = new JSeparator();
            sep.setForeground(new Color(0x6B, 0x4A, 0x30));
            sep.setBackground(new Color(0x6B, 0x4A, 0x30));
            sep.setAlignmentX(Component.LEFT_ALIGNMENT);

            header.add(title);
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
            list.setToolTipText("Doble clic: agregar nota  |  Clic derecho: más opciones");
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
                JOptionPane.showMessageDialog(this,
                        "Cobro realizado: " + totalLabel.getText(),
                        "Ir al pago", JOptionPane.INFORMATION_MESSAGE);
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
            // Los "mensajitos" de sección (📌) no se agregan a la orden
            if ("📌".equals(item.emoji)) {
                return;
            }
            for (OrderLine line : lines) {
                if (line.item.name.equals(item.name) && line.notes.isEmpty()) {
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

        private void removeLine(OrderLine line) {
            lines.remove(line);
            model.removeElement(line);
            refresh();
        }

        private void editNote(OrderLine line) {
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
            JMenuItem note = new JMenuItem("Editar nota...");
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
                total += l.item.price * l.qty;
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

        OrderLine(MenuItem item) {
            this.item = item;
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

            priceLbl.setText(String.format("Q %.2f", value.item.price * value.qty));
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

    // ---------- Botón redondeado genérico (el verde) ----------
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
