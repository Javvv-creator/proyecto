package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Calendario emergente (sin librerías externas) con la paleta de GIT & EAT!.
 *
 * Uso:
 *   CalendarPopup.mostrar(componenteAncla, fechaInicial, fecha -> campo.setText(...));
 */
public class CalendarPopup extends JPopupMenu {

    private static final Color BG = new Color(231, 221, 202);
    private static final Color BROWN = new Color(139, 94, 52);
    private static final Color DARK_BROWN = new Color(92, 53, 22);
    private static final Color YELLOW = new Color(243, 205, 59);
    private static final Color HOVER = new Color(222, 210, 191);
    private static final Locale ES = new Locale("es", "GT");
    private static final String[] DIAS = {"L", "M", "M", "J", "V", "S", "D"};

    private YearMonth mes;
    private final LocalDate seleccionada;
    private final LocalDate minimo; // null = sin límite; los días anteriores quedan deshabilitados
    private final Consumer<LocalDate> alElegir;
    private final JLabel lblMes = new JLabel("", SwingConstants.CENTER);
    private final JPanel grilla = new JPanel(new GridLayout(6, 7, 2, 2));

    public static void mostrar(Component ancla, LocalDate inicial, Consumer<LocalDate> alElegir) {
        mostrar(ancla, inicial, null, alElegir);
    }

    /** @param minimo fecha mínima elegible (null = sin límite) */
    public static void mostrar(Component ancla, LocalDate inicial, LocalDate minimo, Consumer<LocalDate> alElegir) {
        CalendarPopup popup = new CalendarPopup(inicial, minimo, alElegir);
        popup.show(ancla, 0, ancla.getHeight() + 4);
    }

    private CalendarPopup(LocalDate inicial, LocalDate minimo, Consumer<LocalDate> alElegir) {
        this.minimo = minimo;
        this.seleccionada = inicial != null ? inicial : LocalDate.now();
        this.mes = YearMonth.from(this.seleccionada);
        this.alElegir = alElegir;

        setBorder(BorderFactory.createLineBorder(BROWN, 2));
        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(10, 10, 10, 10));
        root.setPreferredSize(new Dimension(300, 310));

        // Encabezado: « ‹ Mes Año › »
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        izq.setOpaque(false);
        izq.add(flecha("«", -12));
        izq.add(flecha("‹", -1));
        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        der.setOpaque(false);
        der.add(flecha("›", 1));
        der.add(flecha("»", 12));
        lblMes.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblMes.setForeground(DARK_BROWN);
        top.add(izq, BorderLayout.WEST);
        top.add(lblMes, BorderLayout.CENTER);
        top.add(der, BorderLayout.EAST);

        // Nombres de los días + grilla
        JPanel centro = new JPanel(new BorderLayout(0, 4));
        centro.setOpaque(false);
        JPanel nombres = new JPanel(new GridLayout(1, 7, 2, 2));
        nombres.setOpaque(false);
        for (String d : DIAS) {
            JLabel l = new JLabel(d, SwingConstants.CENTER);
            l.setFont(new Font("SansSerif", Font.BOLD, 13));
            l.setForeground(BROWN);
            nombres.add(l);
        }
        grilla.setOpaque(false);
        centro.add(nombres, BorderLayout.NORTH);
        centro.add(grilla, BorderLayout.CENTER);

        // Pie: botón "Hoy"
        JLabel hoy = new JLabel("Hoy", SwingConstants.CENTER);
        hoy.setOpaque(true);
        hoy.setBackground(YELLOW);
        hoy.setForeground(Color.WHITE);
        hoy.setFont(new Font("SansSerif", Font.BOLD, 14));
        hoy.setBorder(new EmptyBorder(7, 0, 7, 0));
        boolean hoyHabilitado = minimo == null || !LocalDate.now().isBefore(minimo);
        if (hoyHabilitado) {
            hoy.setCursor(new Cursor(Cursor.HAND_CURSOR));
            hoy.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    elegir(LocalDate.now());
                }
            });
        } else {
            hoy.setBackground(new Color(200, 190, 170));
        }

        root.add(top, BorderLayout.NORTH);
        root.add(centro, BorderLayout.CENTER);
        root.add(hoy, BorderLayout.SOUTH);
        add(root);
        refrescar();
    }

    private JLabel flecha(String texto, int deltaMeses) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("SansSerif", Font.BOLD, 20));
        l.setForeground(DARK_BROWN);
        l.setCursor(new Cursor(Cursor.HAND_CURSOR));
        l.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mes = mes.plusMonths(deltaMeses);
                refrescar();
            }
        });
        return l;
    }

    private void elegir(LocalDate fecha) {
        setVisible(false);
        alElegir.accept(fecha);
    }

    private void refrescar() {
        String nombreMes = mes.getMonth().getDisplayName(TextStyle.FULL, ES);
        lblMes.setText(Character.toUpperCase(nombreMes.charAt(0)) + nombreMes.substring(1) + " " + mes.getYear());

        grilla.removeAll();
        LocalDate primero = mes.atDay(1);
        int desfase = primero.getDayOfWeek().getValue() - 1; // lunes = 0
        LocalDate hoy = LocalDate.now();

        for (int i = 0; i < 42; i++) {
            LocalDate fecha = primero.plusDays(i - desfase);
            grilla.add(new CeldaDia(fecha, fecha.getMonth() == mes.getMonth(),
                    fecha.equals(seleccionada), fecha.equals(hoy),
                    minimo == null || !fecha.isBefore(minimo)));
        }
        grilla.revalidate();
        grilla.repaint();
    }

    private class CeldaDia extends JLabel {

        private final boolean elegido;
        private final boolean esHoy;
        private boolean encima = false;

        CeldaDia(LocalDate fecha, boolean delMes, boolean elegido, boolean esHoy, boolean habilitado) {
            super(String.valueOf(fecha.getDayOfMonth()), SwingConstants.CENTER);
            this.elegido = elegido;
            this.esHoy = esHoy;
            setFont(new Font("SansSerif", elegido ? Font.BOLD : Font.PLAIN, 14));
            setForeground(elegido ? Color.WHITE : (delMes ? DARK_BROWN : new Color(160, 140, 115)));
            if (!habilitado) {
                // Día no permitido: gris tachado, sin hover ni clic
                setForeground(new Color(190, 178, 158));
                setFont(getFont().deriveFont(java.util.Map.of(
                        java.awt.font.TextAttribute.STRIKETHROUGH, java.awt.font.TextAttribute.STRIKETHROUGH_ON)));
                return;
            }
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    encima = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    encima = false;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    elegir(fecha);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int d = Math.min(getWidth(), getHeight()) - 2;
            int x = (getWidth() - d) / 2;
            int y = (getHeight() - d) / 2;
            if (elegido) {
                g2.setColor(BROWN);
                g2.fillOval(x, y, d, d);
            } else if (encima) {
                g2.setColor(HOVER);
                g2.fillOval(x, y, d, d);
            }
            if (esHoy && !elegido) {
                g2.setColor(YELLOW);
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(x, y, d, d);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}