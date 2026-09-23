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

    // ---------- Panel derecho: orden actual ----------
    class OrderPanel extends JPanel {

        private final DefaultListModel<OrderLine> model = new DefaultListModel<>();
        private final JList<OrderLine> list = new JList<>(model);
        private final JLabel totalLabel = new JLabel("Q 0.00");
        private final List<OrderLine> lines = new ArrayList<>();

        OrderPanel() {
            setLayout(new BorderLayout());
            setBackground(PANEL_DARK);
            setBorder(new EmptyBorder(35, 30, 30, 30));

            JPanel header = new JPanel();
            header.setOpaque(false);
            header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

            JLabel title = new JLabel("Orden actual");
            title.setFont(new Font("SansSerif", Font.BOLD, 32));
            title.setForeground(WHITE_TEXT);
            title.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel cashierRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            cashierRow.setOpaque(false);
            cashierRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel l1 = new JLabel("Cajero: ");
            l1.setForeground(WHITE_TEXT);
            l1.setFont(new Font("SansSerif", Font.PLAIN, 16));
            JLabel l2 = new JLabel("Luis Muñoz");
            l2.setForeground(YELLOW_BRIGHT);
            l2.setFont(new Font("SansSerif", Font.BOLD, 16));
            JLabel l3 = new JLabel("  -   Turno Tarde");
            l3.setForeground(WHITE_TEXT);
            l3.setFont(new Font("SansSerif", Font.PLAIN, 16));
            cashierRow.add(l1);
            cashierRow.add(l2);
            cashierRow.add(l3);

            header.add(title);
            header.add(Box.createVerticalStrut(10));
            header.add(cashierRow);
            header.add(Box.createVerticalStrut(15));

            JSeparator sep = new JSeparator();
            sep.setForeground(new Color(0x6B, 0x4A, 0x30));
            sep.setAlignmentX(Component.LEFT_ALIGNMENT);
            header.add(sep);

            add(header, BorderLayout.NORTH);

            list.setOpaque(false);
            list.setCellRenderer(new OrderLineRenderer());
            list.setFixedCellHeight(-1);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            JScrollPane scroll = new JScrollPane(list);
            scroll.setBorder(null);
            scroll.getViewport().setOpaque(false);
            scroll.setOpaque(false);

            RoundedPanel listWrap = new RoundedPanel(CARD_IMG_BG, 24);
            listWrap.setLayout(new BorderLayout());
            listWrap.setBorder(new EmptyBorder(10, 10, 10, 10));
            listWrap.add(scroll, BorderLayout.CENTER);

            JPanel centerWrap = new JPanel(new BorderLayout());
            centerWrap.setOpaque(false);
            centerWrap.setBorder(new EmptyBorder(20, 0, 20, 0));
            centerWrap.add(listWrap, BorderLayout.CENTER);
            add(centerWrap, BorderLayout.CENTER);

            JPanel footer = new JPanel(new BorderLayout());
            footer.setOpaque(false);

            JPanel totalRow = new JPanel(new BorderLayout());
            totalRow.setOpaque(false);
            JLabel totalCaption = new JLabel("Total");
            totalCaption.setForeground(WHITE_TEXT);
            totalCaption.setFont(new Font("SansSerif", Font.PLAIN, 18));
            totalLabel.setForeground(YELLOW_BRIGHT);
            totalLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
            totalRow.add(totalCaption, BorderLayout.WEST);
            totalRow.add(totalLabel, BorderLayout.EAST);

            JButton chargeBtn = new JButton("Cobrar");
            chargeBtn.setFont(new Font("SansSerif", Font.BOLD, 20));
            chargeBtn.setForeground(Color.WHITE);
            chargeBtn.setBackground(TAB_ACTIVE);
            chargeBtn.setFocusPainted(false);
            chargeBtn.setBorder(new EmptyBorder(14, 0, 14, 0));
            chargeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            chargeBtn.addActionListener(e -> {
                if (!lines.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Cobro realizado: " + totalLabel.getText(),
                            "Cobrar", JOptionPane.INFORMATION_MESSAGE);
                    lines.clear();
                    model.clear();
                    updateTotal();
                }
            });

            footer.add(totalRow, BorderLayout.NORTH);
            JPanel btnWrap = new JPanel(new BorderLayout());
            btnWrap.setOpaque(false);
            btnWrap.setBorder(new EmptyBorder(15, 0, 0, 0));
            btnWrap.add(chargeBtn, BorderLayout.CENTER);
            footer.add(btnWrap, BorderLayout.SOUTH);

            add(footer, BorderLayout.SOUTH);
        }

        void addItem(MenuItem item) {
            for (OrderLine line : lines) {
                if (line.item.name.equals(item.name)) {
                    line.qty++;
                    list.repaint();
                    updateTotal();
                    return;
                }
            }
            OrderLine line = new OrderLine(item);
            lines.add(line);
            model.addElement(line);
            updateTotal();
        }

        void updateTotal() {
            double total = 0;
            for (OrderLine l : lines) {
                total += l.item.price * l.qty;
            }
            totalLabel.setText(String.format("Q %.2f", total));
        }
    }

    static class OrderLine {

        MenuItem item;
        int qty = 1;

        OrderLine(MenuItem item) {
            this.item = item;
        }
    }

    static class OrderLineRenderer extends JPanel implements ListCellRenderer<OrderLine> {

        private final JLabel nameLbl = new JLabel();
        private final JLabel qtyLbl = new JLabel();
        private final JLabel priceLbl = new JLabel();

        OrderLineRenderer() {
            setLayout(new BorderLayout());
            setOpaque(false);
            setBorder(new EmptyBorder(10, 8, 10, 8));
            nameLbl.setFont(new Font("SansSerif", Font.BOLD, 16));
            nameLbl.setForeground(BROWN_DARK);
            qtyLbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
            qtyLbl.setForeground(BROWN_TEXT);
            priceLbl.setFont(new Font("SansSerif", Font.BOLD, 16));
            priceLbl.setForeground(BROWN_DARK);

            JPanel left = new JPanel();
            left.setOpaque(false);
            left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
            nameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            qtyLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            left.add(nameLbl);
            left.add(qtyLbl);

            add(left, BorderLayout.WEST);
            add(priceLbl, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends OrderLine> list, OrderLine value,
                int index, boolean isSelected, boolean cellHasFocus) {
            nameLbl.setText(value.item.name);
            qtyLbl.setText("x" + value.qty);
            priceLbl.setText(String.format("Q %.2f", value.item.price * value.qty));
            setBackground(isSelected ? new Color(0xE8, 0xD8, 0xBC) : CARD_IMG_BG);
            setOpaque(isSelected);
            return this;
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
