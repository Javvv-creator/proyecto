package gui;

import main.Conexion.Conexion;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Guarda una venta en las tablas orden, detalle_orden y pago_orden.
 * Todo va en una transacción: o se guarda todo o no se guarda nada.
 */
public final class VentaDAO {

    private VentaDAO() {
    }

    /** @return id_orden generado */
    public static int guardar(int idUsuario, pantallaCajero.PaymentResult pago,
            List<pantallaCajero.OrderLine> lineas) throws SQLException {

        if (idUsuario <= 0) {
            throw new SQLException("No hay una sesión iniciada. Inicie sesión desde el login.");
        }

        final String sqlOrden = "INSERT INTO orden (fecha, hora, estado, total, descuento, cupon, id_usuario) "
                + "VALUES (?,?,?,?,?,?,?)";
        final String sqlDetalle = "INSERT INTO detalle_orden (cantidad, precio_unitario, subtotal, es_agrandado, "
                + "id_orden, id_producto, notas) VALUES (?,?,?,?,?,?,?)";
        final String sqlPago = "INSERT INTO pago_orden (monto, metodo_pago, id_orden) VALUES (?,?,?)";

        for (pantallaCajero.OrderLine l : lineas) {
            if (l.item.idProducto <= 0) {
                throw new SQLException("El producto \"" + l.item.name + "\" no tiene id en la base de datos.");
            }
        }

        try (Connection con = new Conexion().getConnection()) {
            if (con == null) {
                throw new SQLException("No se pudo conectar a la base de datos.");
            }
            con.setAutoCommit(false);
            try {
                int idOrden;
                try (PreparedStatement ps = con.prepareStatement(sqlOrden, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setDate(1, Date.valueOf(LocalDate.now()));
                    ps.setTime(2, Time.valueOf(LocalTime.now().withNano(0)));
                    ps.setString(3, "En cocina");
                    ps.setDouble(4, pago.total);
                    ps.setDouble(5, pago.discount);
                    ps.setString(6, pago.couponCode); // puede ser null
                    ps.setInt(7, idUsuario);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next())
                            throw new SQLException("No se obtuvo el id de la orden.");
                        idOrden = keys.getInt(1);
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(sqlDetalle)) {
                    for (pantallaCajero.OrderLine l : lineas) {
                        ps.setInt(1, l.qty);
                        ps.setDouble(2, l.unitPrice);
                        ps.setDouble(3, Math.round(l.unitPrice * l.qty * 100.0) / 100.0);
                        ps.setInt(4, 0);
                        ps.setInt(5, idOrden);
                        ps.setInt(6, l.item.idProducto);
                        ps.setString(7, (l.notes == null || l.notes.isEmpty()) ? null : l.notes);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                try (PreparedStatement ps = con.prepareStatement(sqlPago)) {
                    if (pago.cash > 0) {
                        ps.setDouble(1, pago.cash); // efectivo cobrado (sin el cambio)
                        ps.setString(2, "Efectivo");
                        ps.setInt(3, idOrden);
                        ps.addBatch();
                    }
                    if (pago.card > 0) {
                        ps.setDouble(1, pago.card);
                        ps.setString(2, "Tarjeta de Crédito"); // el diálogo no distingue crédito/débito
                        ps.setInt(3, idOrden);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                con.commit();
                return idOrden;
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }
}