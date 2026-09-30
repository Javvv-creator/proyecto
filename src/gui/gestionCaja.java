package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Pantalla de Gestión de caja - GIT & EAT!
 * Versión funcional: abrir/cerrar caja, registrar ventas, contar efectivo,
 * calcular diferencias e historial de cajas.
 *
 * El estado de la caja vive en {@link EstadoCaja} (estático) para que no se
 * pierda al navegar entre pantallas (cada pantalla se crea y se destruye).
 */
public class gestionCaja extends JFrame {

    // ------------------------------------------------------------------
    // MODELO (estado compartido entre pantallas)
    // ------------------------------------------------------------------
    private static class Registro {
        final String apertura, cierre, cajero;
        final double inicial, ventas, esperado, contado, diferencia;

        Registro(String apertura, String cierre, String cajero, double inicial,
                double ventas, double esperado, double contado, double diferencia) {
            this.apertura = apertura;
            this.cierre = cierre;
            this.cajero = cajero;
            this.inicial = inicial;
            this.ventas = ventas;
            this.esperado = esperado;
            this.contado = contado;
            this.diferencia = diferencia;
        }
    }

    private static class EstadoCaja {
        static String cajero = "Sin sesión iniciada";
        static String turno = "";

        static boolean abierta = true;
        static String horaApertura = now();

        static double inicial = 500.00;
        static double ventasEfectivo = 0;
        static double ventasTarjeta = 0;
        static double ventasRegalo = 0; // pagos hechos con tarjeta de regalo (canjes)
        static int regalosCanjeados = 0;
        static double regalosCanjeadosMonto = 0;
        static int regalosVendidos = 0;
        static double regalosVendidosMonto = 0; // se asume cobro en efectivo
        static Double efectivoContado = null; // null = aún no contado

        static final List<Registro> historial = new ArrayList<>();

        static String now() {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }

        static double ventasTotales() {
            return ventasEfectivo + ventasTarjeta + ventasRegalo;
        }

        static double efectivoEsperado() {
            return inicial + ventasEfectivo + regalosVendidosMonto;
        }

        static void abrir(double montoInicial) {
            abierta = true;
            horaApertura = now();
            inicial = montoInicial;
            ventasEfectivo = ventasTarjeta = ventasRegalo = 0;
            regalosCanjeados = regalosVendidos = 0;
            regalosCanjeadosMonto = regalosVendidosMonto = 0;
            efectivoContado = null;
        }
    }

    // ------------------------------------------------------------------
    // API PÚBLICA (para ser usada desde el login y desde pantallaCajero)
    // ------------------------------------------------------------------
    public enum MetodoPago {
        EFECTIVO, TARJETA, TARJETA_REGALO
    }

    /** Instancia visible actualmente (para refrescar en vivo si está abierta). */
    private static gestionCaja instanciaActual;

    /**
     * Debe llamarse desde el login al iniciar sesión.
     * Ej: gestionCaja.setUsuarioActual("Samantha Martinez", "Cajera - Turno tarde");
     */
    public static synchronized void setUsuarioActual(String nombre, String rolOTurno) {
        EstadoCaja.cajero = (nombre == null || nombre.trim().isEmpty()) ? "Sin sesión iniciada" : nombre.trim();
        EstadoCaja.turno = rolOTurno == null ? "" : rolOTurno.trim();
        refrescarSiVisible();
    }

    public static void setUsuarioActual(String nombre) {
        setUsuarioActual(nombre, "");
    }

    /** Cierra la sesión del usuario (para llamar desde el botón de cerrar sesión). */
    public static synchronized void limpiarUsuarioActual() {
        setUsuarioActual(null, "");
    }

    public static boolean isCajaAbierta() {
        return EstadoCaja.abierta;
    }

    /**
     * Registra una venta de un pedido cobrado. Llamar desde pantallaCajero
     * cuando se confirme el pago. Devuelve false si la caja está cerrada
     * (en ese caso la venta NO se registra y pantallaCajero debería avisar).
     *
     * Ej: gestionCaja.registrarVenta(gestionCaja.MetodoPago.EFECTIVO, 85.50);
     */
    public static synchronized boolean registrarVenta(MetodoPago metodo, double monto) {
        if (!EstadoCaja.abierta || metodo == null || monto <= 0 || Double.isNaN(monto) || Double.isInfinite(monto))
            return false;
        monto = round2(monto);
        switch (metodo) {
            case EFECTIVO:
                EstadoCaja.ventasEfectivo += monto;
                break;
            case TARJETA:
                EstadoCaja.ventasTarjeta += monto;
                break;
            case TARJETA_REGALO:
                EstadoCaja.ventasRegalo += monto;
                EstadoCaja.regalosCanjeados++;
                EstadoCaja.regalosCanjeadosMonto += monto;
                break;
        }
        EstadoCaja.efectivoContado = null; // el conteo anterior ya no es válido
        refrescarSiVisible();
        return true;
    }

    /** Registra la venta de una tarjeta de regalo (se asume cobrada en efectivo). */
    public static synchronized boolean registrarVentaTarjetaRegalo(double monto) {
        if (!EstadoCaja.abierta || monto <= 0 || Double.isNaN(monto) || Double.isInfinite(monto))
            return false;
        EstadoCaja.regalosVendidos++;
        EstadoCaja.regalosVendidosMonto += round2(monto);
        EstadoCaja.efectivoContado = null;
        refrescarSiVisible();
        return true;
    }

    /** Nombre a mostrar: usa la Sesion del login; si no hay, el valor de setUsuarioActual. */
    private static String nombreUsuario() {
        return Sesion.estaActiva() ? Sesion.getNombreCompleto() : EstadoCaja.cajero;
    }

    private static String rolUsuario() {
        if (Sesion.estaActiva()) {
            String t = Sesion.getTurno().isEmpty() ? "" : " - Turno " + Sesion.getTurno();
            return Sesion.getRol() + t;
        }
        return EstadoCaja.turno;
    }

    private static void refrescarSiVisible() {
        final gestionCaja g = instanciaActual;
        if (g != null) {
            SwingUtilities.invokeLater(g::refresh);
        }
    }

    @Override
    public void dispose() {
        if (instanciaActual == this)
            instanciaActual = null;
        super.dispose();
    }

    // ------------------------------------------------------------------
    // COLORES
    // ------------------------------------------------------------------
    private static final Color COLOR_BG = new Color(231, 221, 202);
    private static final Color COLOR_SIDEBAR = new Color(139, 94, 52);
    private static final Color COLOR_HEADER = new Color(139, 94, 52);
    private static final Color COLOR_TEXT_BROWN = new Color(92, 53, 22);
    private static final Color COLOR_SIDEBAR_HOVER = new Color(160, 110, 65);
    private static final Color COLOR_SIDEBAR_ACTIVE = new Color(110, 72, 38);
    private static final Color COLOR_CERRAR_SESION = new Color(211, 53, 58);

    private static final Color COLOR_CARD_GREEN = new Color(104, 159, 56);
    private static final Color COLOR_CARD_RED = new Color(196, 40, 53);
    private static final Color COLOR_CARD_ORANGE = new Color(222, 131, 77);
    private static final Color COLOR_BTN_YELLOW = new Color(243, 205, 59);
    private static final Color COLOR_BTN_DISABLED = new Color(200, 190, 160);
    private static final Color COLOR_BREAKDOWN_BG = new Color(245, 240, 230);
    private static final Color COLOR_STATUS_BG = new Color(76, 92, 70);
    private static final Color COLOR_STATUS_CLOSED_BG = new Color(120, 70, 70);
    private static final Color COLOR_STATUS_INDICATOR = new Color(56, 142, 60);
    private static final Color COLOR_STATUS_INDICATOR_OFF = new Color(230, 90, 90);

    private int selectedMenuIndex = 4;
    private JPanel[] menuButtons;

    // Componentes que se actualizan dinámicamente
    private JLabel lblInicial, lblVentas, lblEfectivo, lblTarjeta, lblRegalo;
    private JLabel lblCanjeadasTxt, lblCanjeadasMonto, lblVendidasTxt, lblVendidasMonto;
    private JLabel lblEsperado, lblContado, lblDiferencia;
    private JLabel statusText, dotLbl, nameLbl, roleLbl;
    private RoundedPanel statusBadge;
    private ActionButton btnToggle, btnContar, btnHistorial;

    public gestionCaja() {
        setTitle("GIT & EAT! - Gestión de caja");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1280, 720));

        JPanel mainContainer = new JPanel(new BorderLayout(15, 15));
        mainContainer.setBackground(COLOR_BG);
        mainContainer.setBorder(new EmptyBorder(15, 15, 15, 15));

        mainContainer.add(createSidebarPanel(), BorderLayout.WEST);

        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setOpaque(false);
        contentPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel bodyWrapper = new JPanel(new BorderLayout());
        bodyWrapper.setOpaque(false);
        bodyWrapper.add(createMainBody(), BorderLayout.NORTH);
        contentPanel.add(bodyWrapper, BorderLayout.CENTER);

        mainContainer.add(contentPanel, BorderLayout.CENTER);
        add(mainContainer);

        // Tecla ESC: cierra el programa
        KeyStroke esc = KeyStroke.getKeyStroke("ESCAPE");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(esc, "cerrarPrograma");
        getRootPane().getActionMap().put("cerrarPrograma", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                System.exit(0);
            }
        });

        instanciaActual = this;
        refresh();
    }

    // ------------------------------------------------------------------
    // UTILIDADES
    // ------------------------------------------------------------------
    private static String q(double v) {
        return String.format(Locale.US, "Q.%,.2f", v);
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    /** Pide un monto. Devuelve null si el usuario cancela. */
    private Double pedirMonto(String mensaje, String titulo, boolean permitirCero) {
        String valor = "";
        while (true) {
            Object r = JOptionPane.showInputDialog(this, mensaje, titulo, JOptionPane.QUESTION_MESSAGE, null, null,
                    valor);
            if (r == null)
                return null;
            valor = r.toString().trim();
            try {
                String limpio = valor.replace("Q", "").replace("q", "").replace(",", "").replace(" ", "");
                if (limpio.startsWith("."))
                    limpio = limpio.substring(1); // permite "Q.500"
                double m = round2(Double.parseDouble(limpio));
                if (m < 0 || (m == 0 && !permitirCero) || Double.isNaN(m) || Double.isInfinite(m))
                    throw new NumberFormatException();
                return m;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        permitirCero ? "Ingrese un monto válido (0 o mayor)." : "Ingrese un monto válido mayor a 0.",
                        "Monto inválido", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    /** Abre otra pantalla por nombre de clase (para pantallas que no conozco). */
    private boolean abrirPantalla(String... nombres) {
        for (String n : nombres) {
            try {
                Object o = Class.forName(n).getDeclaredConstructor().newInstance();
                if (o instanceof JFrame) {
                    ((JFrame) o).setVisible(true);
                    dispose();
                    return true;
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    // ------------------------------------------------------------------
    // ACTUALIZACIÓN DE LA VISTA
    // ------------------------------------------------------------------
    private void refresh() {
        boolean abierta = EstadoCaja.abierta;

        nameLbl.setText(nombreUsuario());
        roleLbl.setText(rolUsuario());

        lblInicial.setText(q(EstadoCaja.inicial));
        lblVentas.setText(q(EstadoCaja.ventasTotales()));
        lblEfectivo.setText(q(EstadoCaja.ventasEfectivo));
        lblTarjeta.setText(q(EstadoCaja.ventasTarjeta));
        lblRegalo.setText(q(EstadoCaja.ventasRegalo));

        lblCanjeadasTxt.setText("Canjeadas hoy (" + EstadoCaja.regalosCanjeados + ")");
        lblCanjeadasMonto.setText(q(EstadoCaja.regalosCanjeadosMonto));
        lblVendidasTxt.setText("Vendidas hoy (" + EstadoCaja.regalosVendidos + ")");
        lblVendidasMonto.setText(q(EstadoCaja.regalosVendidosMonto));

        double esperado = EstadoCaja.efectivoEsperado();
        lblEsperado.setText(q(esperado));
        if (EstadoCaja.efectivoContado == null) {
            lblContado.setText("Sin contar");
            lblDiferencia.setText("--");
        } else {
            double dif = round2(EstadoCaja.efectivoContado - esperado);
            lblContado.setText(q(EstadoCaja.efectivoContado));
            lblDiferencia.setText((dif > 0 ? "+" : "") + q(dif));
        }

        statusText.setText(abierta ? "Caja abierta" : "Caja cerrada");
        statusBadge.setBackgroundColor(abierta ? COLOR_STATUS_BG : COLOR_STATUS_CLOSED_BG);
        dotLbl.setIcon(new CustomIcon(CustomIcon.Type.STATUS_DOT, 22,
                abierta ? COLOR_STATUS_INDICATOR : COLOR_STATUS_INDICATOR_OFF));
        statusBadge.repaint();

        btnToggle.setContent(abierta ? CustomIcon.Type.CLOSE_X : CustomIcon.Type.PLUS,
                abierta ? "Cerrar caja" : "Abrir caja");
        btnContar.setEnabledLook(abierta);
    }

    // ------------------------------------------------------------------
    // ACCIONES
    // ------------------------------------------------------------------
    private void accionToggleCaja() {
        if (EstadoCaja.abierta)
            cerrarCaja();
        else
            abrirCaja();
    }

    private void abrirCaja() {
        Double monto = pedirMonto("Ingrese el monto inicial de la caja:", "Abrir caja", true);
        if (monto == null)
            return;
        EstadoCaja.abrir(monto);
        refresh();
        JOptionPane.showMessageDialog(this, "Caja abierta con un monto inicial de " + q(monto) + ".",
                "Caja abierta", JOptionPane.INFORMATION_MESSAGE);
    }

    private void cerrarCaja() {
        // Si aún no se contó el efectivo, se solicita
        if (EstadoCaja.efectivoContado == null) {
            Double contado = pedirMonto("Antes de cerrar, ingrese el efectivo contado en caja:\n(Esperado: "
                    + q(EstadoCaja.efectivoEsperado()) + ")", "Cerrar caja", true);
            if (contado == null)
                return;
            EstadoCaja.efectivoContado = contado;
            refresh();
        }

        double esperado = EstadoCaja.efectivoEsperado();
        double contado = EstadoCaja.efectivoContado;
        double dif = round2(contado - esperado);

        String resumen = "Resumen del cierre:\n\n"
                + "Monto inicial: " + q(EstadoCaja.inicial) + "\n"
                + "Ventas del día: " + q(EstadoCaja.ventasTotales()) + "\n"
                + "Efectivo esperado: " + q(esperado) + "\n"
                + "Efectivo contado: " + q(contado) + "\n"
                + "Diferencia: " + q(dif)
                + (dif < 0 ? "  (FALTANTE)" : dif > 0 ? "  (SOBRANTE)" : "  (cuadrada)")
                + "\n\n¿Desea cerrar la caja?";

        int r = JOptionPane.showConfirmDialog(this, resumen, "Confirmar cierre de caja",
                JOptionPane.YES_NO_OPTION, dif == 0 ? JOptionPane.QUESTION_MESSAGE : JOptionPane.WARNING_MESSAGE);
        if (r != JOptionPane.YES_OPTION)
            return;

        EstadoCaja.historial.add(0, new Registro(EstadoCaja.horaApertura, EstadoCaja.now(), nombreUsuario(),
                EstadoCaja.inicial, EstadoCaja.ventasTotales(), esperado, contado, dif));
        EstadoCaja.abierta = false;
        refresh();
        JOptionPane.showMessageDialog(this, "Caja cerrada correctamente.", "Caja cerrada",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private boolean verificarAbierta() {
        if (!EstadoCaja.abierta) {
            JOptionPane.showMessageDialog(this, "La caja está cerrada. Ábrala primero.", "Caja cerrada",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void contarEfectivo() {
        if (!verificarAbierta())
            return;
        Double c = pedirMonto("Ingrese el efectivo contado en caja:\n(Esperado: " + q(EstadoCaja.efectivoEsperado())
                + ")", "Contar efectivo", true);
        if (c == null)
            return;
        EstadoCaja.efectivoContado = c;
        refresh();
    }

    private void mostrarHistorial() {
        if (EstadoCaja.historial.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Aún no hay cierres de caja registrados.", "Historial de cajas",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String[] cols = { "Apertura", "Cierre", "Cajero", "Inicial", "Ventas", "Esperado", "Contado", "Diferencia" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        for (Registro r : EstadoCaja.historial) {
            model.addRow(new Object[] { r.apertura, r.cierre, r.cajero, q(r.inicial), q(r.ventas), q(r.esperado),
                    q(r.contado), q(r.diferencia) });
        }
        JTable table = new JTable(model);
        table.setRowHeight(26);
        table.getTableHeader().setReorderingAllowed(false);
        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(900, 300));
        JOptionPane.showMessageDialog(this, sp, "Historial de cajas", JOptionPane.PLAIN_MESSAGE);
    }

    private void cerrarSesion() {
        String msg = EstadoCaja.abierta
                ? "La caja sigue abierta. ¿Cerrar sesión de todas formas?"
                : "¿Desea cerrar sesión?";
        int r = JOptionPane.showConfirmDialog(this, msg, "Cerrar sesión", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (r != JOptionPane.YES_OPTION)
            return;
        Sesion.cerrar();
        limpiarUsuarioActual();
        new pantallaLogin().setVisible(true);
        dispose();
    }

    private void volverDashboard() {
        new dashboardAdmin().setVisible(true);
        dispose();
    }

    // ------------------------------------------------------------------
    // SIDEBAR
    // ------------------------------------------------------------------
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
                volverDashboard();
            }
        });

        sidebar.add(logoCard, BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new BorderLayout(0, 15));
        centerWrapper.setOpaque(false);

        JPanel menuPanel = new JPanel(new GridLayout(6, 1, 0, 8));
        menuPanel.setOpaque(false);

        Object[][] items = {
                { SidebarVectorIcon.IconType.EMPLOYEES, "Gestión de empleados (cajeros)" },
                { SidebarVectorIcon.IconType.MENU, "Gestión de menú / productos" },
                { SidebarVectorIcon.IconType.ORDERS, "Gestión de pedidos" },
                { SidebarVectorIcon.IconType.REPORTS, "Reportes y estadísticas" },
                { SidebarVectorIcon.IconType.CASH, "Gestión de caja" },
                { SidebarVectorIcon.IconType.SECURITY, "Seguridad y auditoría" }
        };

        menuButtons = new JPanel[items.length];

        for (int i = 0; i < items.length; i++) {
            final int index = i;
            SidebarVectorIcon.IconType iconType = (SidebarVectorIcon.IconType) items[i][0];
            String textHtml = (String) items[i][1];

            if (i == 0)
                textHtml = "<html>Gestión de<br>empleados (cajeros)</html>";
            if (i == 1)
                textHtml = "<html>Gestión de menú /<br>productos</html>";

            RoundedPanel btnPanel = new RoundedPanel(15, i == selectedMenuIndex ? COLOR_SIDEBAR_ACTIVE : COLOR_SIDEBAR);
            btnPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 8));

            JLabel iconLbl = new JLabel(new SidebarVectorIcon(iconType, 28));
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
                    switch (index) {
                        case 0:
                            new gestionEmpleados().setVisible(true);
                            dispose();
                            break;
                        case 1:
                            new gestionProductos().setVisible(true);
                            dispose();
                            break;
                        case 2:
                            new gestionPedidos().setVisible(true);
                            dispose();
                            break;
                        case 3:
                            new pantallaEstadistica().setVisible(true);
                            dispose();
                            break;
                        case 4:
                            // Ya estamos en Gestión de caja: no se recrea la ventana
                            break;
                        case 5:
                            new seguridadAuditoria().setVisible(true);
                            dispose();
                            break;
                    }
                }
            });

            menuButtons[i] = btnPanel;
            menuPanel.add(btnPanel);
        }

        centerWrapper.add(menuPanel, BorderLayout.NORTH);

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
                cerrarSesion();
            }
        });

        JPanel logoutWrapper = new JPanel(new BorderLayout());
        logoutWrapper.setOpaque(false);
        logoutWrapper.add(btnCerrarSesion, BorderLayout.SOUTH);

        sidebar.add(centerWrapper, BorderLayout.CENTER);
        sidebar.add(logoutWrapper, BorderLayout.SOUTH);

        return sidebar;
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

        JLabel title = new JLabel("Gestión de caja");
        title.setFont(new Font("SansSerif", Font.BOLD, 40));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        return header;
    }

    // ------------------------------------------------------------------
    // CUERPO
    // ------------------------------------------------------------------
    private JPanel createMainBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(8, 6, 8, 6));

        body.add(createUserAndStatusRow());
        body.add(Box.createRigidArea(new Dimension(0, 20)));

        body.add(createMainSummaryRow());
        body.add(Box.createRigidArea(new Dimension(0, 20)));

        body.add(createBreakdownPanel());
        body.add(Box.createRigidArea(new Dimension(0, 20)));

        body.add(createDetailsRow());
        body.add(Box.createRigidArea(new Dimension(0, 30)));

        body.add(createActionButtonsRow());

        return body;
    }

    private JLabel newLabel(int style, int size, Color color) {
        JLabel l = new JLabel(" ");
        l.setFont(new Font("SansSerif", style, size));
        l.setForeground(color);
        return l;
    }

    private JPanel createUserAndStatusRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        userPanel.setOpaque(false);

        JLabel userIcon = new JLabel(new CustomIcon(CustomIcon.Type.USER_PROFILE, 50, COLOR_CARD_GREEN));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        nameLbl = new JLabel(nombreUsuario());
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        nameLbl.setForeground(COLOR_TEXT_BROWN);

        roleLbl = new JLabel(rolUsuario());
        roleLbl.setFont(new Font("SansSerif", Font.PLAIN, 15));
        roleLbl.setForeground(Color.DARK_GRAY);

        textPanel.add(nameLbl);
        textPanel.add(roleLbl);

        userPanel.add(userIcon);
        userPanel.add(textPanel);

        statusBadge = new RoundedPanel(25, COLOR_STATUS_BG);
        statusBadge.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 12));
        statusBadge.setPreferredSize(new Dimension(200, 50));

        dotLbl = new JLabel(new CustomIcon(CustomIcon.Type.STATUS_DOT, 22, COLOR_STATUS_INDICATOR));
        statusText = new JLabel("Caja abierta");
        statusText.setFont(new Font("SansSerif", Font.BOLD, 17));
        statusText.setForeground(Color.WHITE);

        statusBadge.add(dotLbl);
        statusBadge.add(statusText);

        JPanel rightAlignPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightAlignPanel.setOpaque(false);
        rightAlignPanel.add(statusBadge);

        row.add(userPanel, BorderLayout.WEST);
        row.add(rightAlignPanel, BorderLayout.EAST);

        return row;
    }

    private JPanel createMainSummaryRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 20, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 110));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        lblInicial = newLabel(Font.PLAIN, 34, Color.BLACK);
        lblVentas = newLabel(Font.PLAIN, 34, Color.BLACK);

        row.add(createSummaryCard("Monto inicial:", lblInicial, COLOR_CARD_GREEN));
        row.add(createSummaryCard("Ventas del dia:", lblVentas, COLOR_CARD_RED));

        return row;
    }

    private JPanel createSummaryCard(String title, JLabel amountLbl, Color bgColor) {
        RoundedPanel card = new RoundedPanel(20, bgColor);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 19));
        titleLbl.setForeground(Color.BLACK);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        amountLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLbl);
        card.add(Box.createVerticalGlue());
        card.add(amountLbl);

        return card;
    }

    private JPanel createBreakdownPanel() {
        RoundedPanel panel = new RoundedPanel(20, COLOR_BREAKDOWN_BG);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(10, 25, 10, 25));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        lblEfectivo = newLabel(Font.PLAIN, 22, Color.BLACK);
        lblTarjeta = newLabel(Font.PLAIN, 22, Color.BLACK);
        lblRegalo = newLabel(Font.PLAIN, 22, Color.BLACK);

        panel.add(createBreakdownRow(CustomIcon.Type.COIN, "Efectivo:", lblEfectivo));
        panel.add(new DashedSeparator());
        panel.add(createBreakdownRow(CustomIcon.Type.CREDIT_CARD, "Tarjeta :", lblTarjeta));
        panel.add(new DashedSeparator());
        panel.add(createBreakdownRow(CustomIcon.Type.GIFT_CARD, "Tarjetas de regalo :", lblRegalo));

        return panel;
    }

    private JPanel createBreakdownRow(CustomIcon.Type iconType, String label, JLabel amountLbl) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(12, 0, 12, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);

        JLabel iconLbl = new JLabel(new CustomIcon(iconType, 32, Color.BLACK));
        JLabel textLbl = new JLabel(label);
        textLbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        textLbl.setForeground(Color.BLACK);

        leftPanel.add(iconLbl);
        leftPanel.add(textLbl);

        row.add(leftPanel, BorderLayout.WEST);
        row.add(amountLbl, BorderLayout.EAST);

        return row;
    }

    private JPanel createDetailsRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 20, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 140));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        // Tarjeta naranja (tarjetas de regalo)
        RoundedPanel orangeCard = new RoundedPanel(20, COLOR_CARD_ORANGE);
        orangeCard.setLayout(new BoxLayout(orangeCard, BoxLayout.Y_AXIS));
        orangeCard.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel titleOrange = new JLabel("Tarjetas de regalo");
        titleOrange.setFont(new Font("SansSerif", Font.BOLD, 19));
        titleOrange.setForeground(Color.WHITE);
        titleOrange.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblCanjeadasTxt = newLabel(Font.PLAIN, 18, Color.WHITE);
        lblCanjeadasMonto = newLabel(Font.PLAIN, 18, Color.WHITE);
        lblVendidasTxt = newLabel(Font.PLAIN, 18, Color.WHITE);
        lblVendidasMonto = newLabel(Font.PLAIN, 18, Color.WHITE);

        orangeCard.add(titleOrange);
        orangeCard.add(Box.createRigidArea(new Dimension(0, 15)));
        orangeCard.add(createDetailSubRow(lblCanjeadasTxt, lblCanjeadasMonto));
        orangeCard.add(Box.createRigidArea(new Dimension(0, 10)));
        orangeCard.add(createDetailSubRow(lblVendidasTxt, lblVendidasMonto));

        // Tarjeta verde (detalle de efectivo)
        RoundedPanel greenCard = new RoundedPanel(20, COLOR_CARD_GREEN);
        greenCard.setLayout(new BoxLayout(greenCard, BoxLayout.Y_AXIS));
        greenCard.setBorder(new EmptyBorder(20, 25, 20, 25));

        lblEsperado = newLabel(Font.PLAIN, 18, Color.BLACK);
        lblContado = newLabel(Font.PLAIN, 18, Color.BLACK);
        lblDiferencia = newLabel(Font.PLAIN, 18, Color.BLACK);

        greenCard.add(createDetailSubRow(fixedLabel("Efectivo esperado:"), lblEsperado));
        greenCard.add(Box.createRigidArea(new Dimension(0, 10)));
        greenCard.add(createDetailSubRow(fixedLabel("Efectivo contado:"), lblContado));
        greenCard.add(Box.createRigidArea(new Dimension(0, 15)));
        greenCard.add(createDetailSubRow(fixedLabel("Diferencia:"), lblDiferencia));

        row.add(orangeCard);
        row.add(greenCard);

        return row;
    }

    private JLabel fixedLabel(String text) {
        JLabel l = newLabel(Font.PLAIN, 18, Color.BLACK);
        l.setText(text);
        return l;
    }

    private JPanel createDetailSubRow(JLabel left, JLabel right) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        row.add(left, BorderLayout.WEST);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    private JPanel createActionButtonsRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        btnToggle = new ActionButton(CustomIcon.Type.CLOSE_X, "Cerrar caja", this::accionToggleCaja);
        btnContar = new ActionButton(CustomIcon.Type.COIN, "Contar efectivo", this::contarEfectivo);
        btnHistorial = new ActionButton(CustomIcon.Type.HISTORY, "Historial de cajas", this::mostrarHistorial);

        row.add(btnToggle);
        row.add(btnContar);
        row.add(btnHistorial);

        return row;
    }

    // ------------------------------------------------------------------
    // COMPONENTES PERSONALIZADOS
    // ------------------------------------------------------------------
    private static class ActionButton extends RoundedPanel {
        private final JLabel iconLbl = new JLabel();
        private final JLabel textLbl = new JLabel();
        private boolean enabledLook = true;

        ActionButton(CustomIcon.Type iconType, String text, Runnable action) {
            super(20, COLOR_BTN_YELLOW);
            setLayout(new FlowLayout(FlowLayout.CENTER, 10, 12));
            setPreferredSize(new Dimension(205, 50));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            textLbl.setFont(new Font("SansSerif", Font.BOLD, 15));
            textLbl.setForeground(COLOR_TEXT_BROWN);
            add(iconLbl);
            add(textLbl);
            setContent(iconType, text);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (enabledLook) {
                        setBackgroundColor(COLOR_BTN_YELLOW.darker());
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setBackgroundColor(enabledLook ? COLOR_BTN_YELLOW : COLOR_BTN_DISABLED);
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    action.run();
                }
            });
        }

        void setContent(CustomIcon.Type iconType, String text) {
            iconLbl.setIcon(new CustomIcon(iconType, 22, COLOR_TEXT_BROWN));
            textLbl.setText(text);
        }

        /** Solo cambia el aspecto; el clic muestra un aviso desde la acción. */
        void setEnabledLook(boolean on) {
            enabledLook = on;
            setBackgroundColor(on ? COLOR_BTN_YELLOW : COLOR_BTN_DISABLED);
            repaint();
        }
    }

    private static class DashedSeparator extends JPanel {
        DashedSeparator() {
            setOpaque(false);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            float[] dash = { 6.0f, 6.0f };
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
            g2.setColor(new Color(210, 200, 185));
            g2.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
            g2.dispose();
        }
    }

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
            Graphics2D graphics = (Graphics2D) g.create();
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(backgroundColor);
            graphics.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
            graphics.dispose();
        }
    }

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
                    g2.setStroke(new BasicStroke(2.2f));
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
                    g2.drawString("Q", 16 - g2.getFontMetrics().stringWidth("Q") / 2, 19);
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
                default:
                    break;
            }
            g2.dispose();
        }
    }

    private static class CustomIcon implements Icon {
        public enum Type {
            MONEY_BAG, USER_PROFILE, STATUS_DOT, COIN, CREDIT_CARD, GIFT_CARD, CLOSE_X, HISTORY, PLUS
        }

        private final Type type;
        private final int size;
        private final Color color;

        public CustomIcon(Type type, int size, Color color) {
            this.type = type;
            this.size = size;
            this.color = color;
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
            g2.translate(x, y);
            g2.setColor(color);
            float scale = size / 32.0f;
            g2.scale(scale, scale);

            switch (type) {
                case MONEY_BAG:
                    Path2D bag = new Path2D.Double();
                    bag.moveTo(12, 8);
                    bag.lineTo(20, 8);
                    bag.lineTo(18, 4);
                    bag.lineTo(14, 4);
                    bag.closePath();
                    g2.fill(bag);
                    g2.fillOval(5, 8, 22, 22);
                    g2.setColor(COLOR_HEADER);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 15));
                    g2.drawString("$", 16 - g2.getFontMetrics().stringWidth("$") / 2, 24);
                    break;
                case USER_PROFILE:
                    g2.fillOval(0, 0, 32, 32);
                    g2.setColor(Color.WHITE);
                    g2.fillOval(10, 6, 12, 12);
                    g2.fillArc(4, 20, 24, 18, 0, 180);
                    break;
                case STATUS_DOT:
                    g2.fillOval(4, 4, 24, 24);
                    break;
                case COIN:
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawOval(4, 4, 24, 24);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 15));
                    g2.drawString("$", 16 - g2.getFontMetrics().stringWidth("$") / 2, 21);
                    break;
                case CREDIT_CARD:
                    g2.fillRoundRect(2, 6, 28, 20, 4, 4);
                    g2.setColor(COLOR_BREAKDOWN_BG);
                    g2.fillRect(2, 10, 28, 4);
                    g2.fillRect(6, 18, 6, 3);
                    break;
                case GIFT_CARD:
                    g2.fillRoundRect(2, 6, 28, 20, 4, 4);
                    g2.setColor(COLOR_BREAKDOWN_BG);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawLine(10, 6, 10, 26);
                    g2.drawOval(6, 2, 5, 5);
                    g2.drawOval(10, 2, 5, 5);
                    break;
                case CLOSE_X:
                    g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(6, 6, 26, 26);
                    g2.drawLine(26, 6, 6, 26);
                    break;
                case HISTORY:
                    g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawOval(4, 4, 24, 24);
                    g2.drawLine(16, 10, 16, 16);
                    g2.drawLine(16, 16, 22, 16);
                    break;
                case PLUS:
                    g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(16, 5, 16, 27);
                    g2.drawLine(5, 16, 27, 16);
                    break;
            }
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> {
            gestionCaja app = new gestionCaja();
            app.setVisible(true);
        });
    }
}