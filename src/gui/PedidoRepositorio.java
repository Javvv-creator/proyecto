package gui;

import main.Crud.crud;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Puente entre las pantallas y la base de datos (usa la clase crud).
 * pantallaCajero registra aquí; gestionPedidos lee de aquí.
 */
public final class PedidoRepositorio {

    private static final List<Runnable> OYENTES = new ArrayList<>();

    private PedidoRepositorio() {
    }

    /** Guarda el pedido en la BD (orden + detalle_orden + pago_orden). Devuelve null si falla. */
    public static Pedido registrar(int idUsuario, String cajero, String turno, List<Pedido.Linea> lineas) {
        List<Object[]> filas = new ArrayList<>();
        for (Pedido.Linea l : lineas) {
            filas.add(new Object[] { l.getIdProducto(), l.getPrecioUnitario(), l.getCantidad(), l.getNota() });
        }
        // TODO: cuando exista la pantalla de cobro, pasar aquí el método de pago real
        int idOrden = crud.registrarOrden(idUsuario, filas, "Efectivo");
        if (idOrden < 0) {
            return null;
        }
        notificarCambio();
        return new Pedido(idOrden, new Date(), cajero, turno, lineas, "En cocina");
    }

    /** Lee todas las órdenes de la BD (de la más antigua a la más reciente). */
    public static List<Pedido> listar() {
        List<Pedido> resultado = new ArrayList<>();
        for (Object[] o : crud.listarOrdenes()) {
            int id = (Integer) o[0];
            List<Pedido.Linea> lineas = new ArrayList<>();
            for (Object[] d : crud.listarDetalleOrden(id)) {
                // listarDetalleOrden no expone id_producto (solo se necesita para mostrar el detalle,
                // no para volver a guardarlo), así que se usa -1 como marcador.
                lineas.add(new Pedido.Linea(-1, (String) d[0], (Double) d[2], (Integer) d[1], (String) d[3]));
            }
            resultado.add(new Pedido(id, (Date) o[1], (String) o[2], (String) o[3], lineas, (String) o[4]));
        }
        return resultado;
    }

    public static void cambiarEstado(Pedido p, String nuevoEstado) {
        if (crud.actualizarEstadoOrden(p.getNumero(), nuevoEstado)) {
            p.setEstado(nuevoEstado);
            notificarCambio();
        }
    }

    public static synchronized void agregarOyente(Runnable r) {
        OYENTES.add(r);
    }

    public static synchronized void quitarOyente(Runnable r) {
        OYENTES.remove(r);
    }

    private static synchronized void notificarCambio() {
        for (Runnable r : new ArrayList<>(OYENTES)) {
            SwingUtilities.invokeLater(r);
        }
    }
}