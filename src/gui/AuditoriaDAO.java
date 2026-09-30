package gui;

import main.Conexion.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Acceso a datos de la bitácora de seguridad y auditoría de GIT & EAT!.
 *
 * La tabla "auditoria" se crea sola la primera vez que se usa (CREATE TABLE IF
 * NOT EXISTS),
 * así que no hace falta correr ningún script manualmente.
 *
 * Para registrar un evento desde cualquier pantalla:
 *
 * AuditoriaDAO.registrar(idUsuario, "Juan Pérez", "Cajero",
 * AuditoriaDAO.Tipo.CANCELACION, "Canceló la orden #57 (Q 84.00)");
 */
public class AuditoriaDAO {

    /** Tipos de evento que muestra la pantalla de seguridad y auditoría. */
    public enum Tipo {
        CANCELACION("Cancelación"),
        CAMBIO_PRECIO("Cambio de precio"),
        DIFERENCIA_CAJA("Diferencia de caja");

        public final String etiqueta;

        Tipo(String etiqueta) {
            this.etiqueta = etiqueta;
        }
    }

    /** Una fila de la bitácora. */
    public static class Registro {

        public final LocalDateTime fechaHora;
        public final String usuario;
        public final String rol;
        public final Tipo tipo;
        public final String detalle;

        public Registro(LocalDateTime fechaHora, String usuario, String rol, Tipo tipo, String detalle) {
            this.fechaHora = fechaHora;
            this.usuario = usuario;
            this.rol = rol;
            this.tipo = tipo;
            this.detalle = detalle;
        }
    }

    private static final String DDL = "CREATE TABLE IF NOT EXISTS auditoria ("
            + " id_auditoria INT AUTO_INCREMENT PRIMARY KEY,"
            + " fecha_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
            + " id_usuario INT NULL,"
            + " usuario VARCHAR(100) NOT NULL,"
            + " rol VARCHAR(30) NULL,"
            + " tipo VARCHAR(30) NOT NULL,"
            + " detalle VARCHAR(500) NOT NULL,"
            + " INDEX idx_auditoria_fecha (fecha_hora),"
            + " INDEX idx_auditoria_tipo (tipo)"
            + ")";

    private static boolean tablaLista = false;

    private AuditoriaDAO() {
    }

    private static Connection abrir() throws SQLException {
        Connection con = new Conexion().getConnection();
        if (con == null) {
            throw new SQLException("No hay conexión con la base de datos. "
                    + "Revisa que MySQL esté encendido y que el usuario y contraseña sean correctos.");
        }
        if (!tablaLista) {
            try (Statement st = con.createStatement()) {
                st.executeUpdate(DDL);
                tablaLista = true;
            } catch (SQLException ex) {
                con.close();
                throw ex;
            }
        }
        return con;
    }

    /**
     * Guarda un evento en la bitácora. No lanza excepción: si falla, lo imprime en
     * consola
     * para que un error de auditoría nunca bloquee la operación principal (venta,
     * caja, etc.).
     *
     * @return true si se guardó correctamente
     */
    public static boolean registrar(Integer idUsuario, String usuario, String rol, Tipo tipo, String detalle) {
        String sql = "INSERT INTO auditoria (id_usuario, usuario, rol, tipo, detalle) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = abrir(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (idUsuario == null) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, idUsuario);
            }
            ps.setString(2, usuario == null || usuario.isBlank() ? "Desconocido" : usuario);
            ps.setString(3, rol);
            ps.setString(4, tipo.name());
            ps.setString(5, detalle == null ? "" : (detalle.length() > 500 ? detalle.substring(0, 500) : detalle));
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            System.out.println("No se pudo registrar la auditoría: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Lista los eventos del rango de fechas (ambos días incluidos), más recientes
     * primero.
     *
     * @param usuario null = todos los usuarios
     * @param tipo    null = todos los tipos
     */
    public static List<Registro> listar(LocalDate desde, LocalDate hasta, String usuario, Tipo tipo)
            throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT fecha_hora, usuario, rol, tipo, detalle FROM auditoria "
                        + "WHERE fecha_hora >= ? AND fecha_hora < ?");
        if (usuario != null) {
            sql.append(" AND usuario = ?");
        }
        if (tipo != null) {
            sql.append(" AND tipo = ?");
        }
        sql.append(" ORDER BY fecha_hora DESC, id_auditoria DESC LIMIT 1000");

        List<Registro> lista = new ArrayList<>();
        try (Connection con = abrir(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int i = 1;
            ps.setTimestamp(i++, Timestamp.valueOf(desde.atStartOfDay()));
            ps.setTimestamp(i++, Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));
            if (usuario != null) {
                ps.setString(i++, usuario);
            }
            if (tipo != null) {
                ps.setString(i++, tipo.name());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Tipo t;
                    try {
                        t = Tipo.valueOf(rs.getString("tipo"));
                    } catch (IllegalArgumentException ex) {
                        continue; // tipo desconocido: se ignora
                    }
                    lista.add(new Registro(
                            rs.getTimestamp("fecha_hora").toLocalDateTime(),
                            rs.getString("usuario"),
                            rs.getString("rol"),
                            t,
                            rs.getString("detalle")));
                }
            }
        }
        return lista;
    }

    /**
     * Cuenta los eventos por tipo dentro del rango (para las tarjetas de arriba).
     */
    public static Map<Tipo, Integer> contar(LocalDate desde, LocalDate hasta, String usuario) throws SQLException {
        Map<Tipo, Integer> conteo = new EnumMap<>(Tipo.class);
        for (Tipo t : Tipo.values()) {
            conteo.put(t, 0);
        }
        StringBuilder sql = new StringBuilder(
                "SELECT tipo, COUNT(*) AS total FROM auditoria WHERE fecha_hora >= ? AND fecha_hora < ?");
        if (usuario != null) {
            sql.append(" AND usuario = ?");
        }
        sql.append(" GROUP BY tipo");

        try (Connection con = abrir(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int i = 1;
            ps.setTimestamp(i++, Timestamp.valueOf(desde.atStartOfDay()));
            ps.setTimestamp(i++, Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));
            if (usuario != null) {
                ps.setString(i++, usuario);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    try {
                        conteo.put(Tipo.valueOf(rs.getString("tipo")), rs.getInt("total"));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        }
        return conteo;
    }

    // Devuelve la lista de usuarios que han iniciado sesión o que tienen eventos en la bitácora.
    public static List<String> usuarios() throws SQLException {
        java.util.Set<String> nombres = new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        try (Connection con = abrir()) {
            // Usuarios registrados en el sistema
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT CONCAT(nombre, ' ', apellido) FROM usuario");
                    ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    nombres.add(rs.getString(1).trim());
                }
            }
            // Usuarios con eventos en la bitácora (por si ya no existen en la tabla
            // usuario)
            try (PreparedStatement ps = con.prepareStatement("SELECT DISTINCT usuario FROM auditoria");
                    ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    nombres.add(rs.getString(1));
                }
            }
        }
        return new ArrayList<>(nombres);
    }
}