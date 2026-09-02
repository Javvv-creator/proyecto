package main.Crud;

import main.Conexion.Conexion;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class crud {

    // Instancia de la clase Conexion
    private static final Conexion conexionDB = new Conexion();

    // ==========================================
    // 1. CRUD CATEGORIA (CREAR, VER, EDITAR Y ELIMINAR)
    // ==========================================

    // CREAR CATEGORIA
    public static boolean crearCategoria(String nombre, String descripcion, int estado) {
        String sql = "INSERT INTO categoria (nombre, descripcion, estado) VALUES (?, ?, ?)";

        try (Connection conn = conexionDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombre);
            stmt.setString(2, descripcion);
            stmt.setInt(3, estado);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al crear categoría: " + e.getMessage());
            return false;
        }
    }

    // VER CATEGORIAS
    public static void verCategorias() {
        String sql = "SELECT id_categoria, nombre, descripcion, estado FROM categoria";

        try (Connection conn = conexionDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            System.out.println("\n--- LISTA DE CATEGORÍAS ---");

            while (rs.next()) {
                System.out.printf(
                        "ID: %d | Nombre: %s | Desc: %s | Estado: %d%n",
                        rs.getInt("id_categoria"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getInt("estado"));
            }

        } catch (SQLException e) {
            System.err.println("Error al ver categorías: " + e.getMessage());
        }
    }

    // EDITAR CATEGORIA
    public static boolean editarCategoria(
            int id,
            String nombre,
            String descripcion,
            int estado) {

        String sql = "UPDATE categoria SET "
                + "nombre = ?, "
                + "descripcion = ?, "
                + "estado = ? "
                + "WHERE id_categoria = ?";

        try (Connection conn = conexionDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombre);
            stmt.setString(2, descripcion);
            stmt.setInt(3, estado);
            stmt.setInt(4, id);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al editar categoría: " + e.getMessage());
            return false;
        }
    }

    // ELIMINAR CATEGORIA
    public static boolean eliminarCategoria(int id) {
        String sql = "DELETE FROM categoria WHERE id_categoria = ?";

        try (Connection conn = conexionDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar categoría: " + e.getMessage());
            return false;
        }
    }

    // ==========================================
    // 2. CRUD PRODUCTO (CREAR, VER, EDITAR Y ELIMINAR)
    // ==========================================

    // CREAR PRODUCTO
    public static boolean crearProducto(
            String nombre,
            BigDecimal precioBase,
            boolean esCombo,
            int estado,
            Integer idCategoria,
            Integer idTurno) {

        String sql = "INSERT INTO producto "
                + "(nombre, precio_base, es_combo, estado, id_categoria, id_turno) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = conexionDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombre);
            stmt.setBigDecimal(2, precioBase);
            stmt.setBoolean(3, esCombo);
            stmt.setInt(4, estado);

            if (idCategoria != null) {
                stmt.setInt(5, idCategoria);
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            if (idTurno != null) {
                stmt.setInt(6, idTurno);
            } else {
                stmt.setNull(6, Types.INTEGER);
            }

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al crear producto: " + e.getMessage());
            return false;
        }
    }

    // VER PRODUCTOS
    public static void verProductos() {
        String sql = "SELECT p.id_producto, p.nombre, p.precio_base, p.es_combo, p.estado, " +
                "c.nombre AS categoria, t.nombre AS turno " +
                "FROM producto p " +
                "LEFT JOIN categoria c ON p.id_categoria = c.id_categoria " +
                "LEFT JOIN turno_menu t ON p.id_turno = t.id_turno";

        try (Connection conn = conexionDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            System.out.println("\n--- LISTA DE PRODUCTOS ---");

            while (rs.next()) {
                System.out.printf(
                        "ID: %d | Nombre: %s | Precio: Q%.2f | Combo: %b | Estado: %d | Cat: %s | Turno: %s%n",
                        rs.getInt("id_producto"),
                        rs.getString("nombre"),
                        rs.getBigDecimal("precio_base"),
                        rs.getBoolean("es_combo"),
                        rs.getInt("estado"),
                        rs.getString("categoria"),
                        rs.getString("turno"));
            }

        } catch (SQLException e) {
            System.err.println("Error al ver productos: " + e.getMessage());
        }
    }

    // EDITAR PRODUCTO
    public static boolean editarProducto(
            int id,
            String nombre,
            BigDecimal precioBase,
            boolean esCombo,
            int estado,
            Integer idCategoria,
            Integer idTurno) {

        String sql = "UPDATE producto SET "
                + "nombre = ?, "
                + "precio_base = ?, "
                + "es_combo = ?, "
                + "estado = ?, "
                + "id_categoria = ?, "
                + "id_turno = ? "
                + "WHERE id_producto = ?";

        try (Connection conn = conexionDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombre);
            stmt.setBigDecimal(2, precioBase);
            stmt.setBoolean(3, esCombo);
            stmt.setInt(4, estado);

            if (idCategoria != null) {
                stmt.setInt(5, idCategoria);
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            if (idTurno != null) {
                stmt.setInt(6, idTurno);
            } else {
                stmt.setNull(6, Types.INTEGER);
            }

            stmt.setInt(7, id);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al editar producto: " + e.getMessage());
            return false;
        }
    }

    // ELIMINAR PRODUCTO
    public static boolean eliminarProducto(int id) {
        String sql = "DELETE FROM producto WHERE id_producto = ?";

        try (Connection conn = conexionDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
            return false;
        }
    }

    // ==========================================
    // 3. CRUD USUARIO (CREAR, VER, EDITAR Y ELIMINAR)
    // ==========================================

    // Crear Usuario
    public static boolean crearUsuario(
            String nombre,
            String apellido,
            String codigoEmpleado,
            String contrasena,
            String rol,
            int estado,
            String turno) {

        String sql = "INSERT INTO usuario "
                + "(nombre, apellido, codigo_empleado, contrasena, rol, estado, turno) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = conexionDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombre);
            stmt.setString(2, apellido);
            stmt.setString(3, codigoEmpleado);
            stmt.setString(4, contrasena);
            stmt.setString(5, rol);
            stmt.setInt(6, estado);
            stmt.setString(7, turno);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al crear usuario: " + e.getMessage());
            return false;
        }
    }

    // VER USUARIOS
    public static void verUsuarios() {
        String sql = "SELECT id_usuario, nombre, apellido, codigo_empleado, rol, estado FROM usuario";

        try (Connection conn = conexionDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            System.out.println("\n--- LISTA DE USUARIOS ---");

            while (rs.next()) {
                System.out.printf(
                        "ID: %d | Empleado: %s %s | Código: %s | Rol: %s | Estado: %d%n",
                        rs.getInt("id_usuario"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("codigo_empleado"),
                        rs.getString("rol"),
                        rs.getInt("estado"));
            }

        } catch (SQLException e) {
            System.err.println("Error al ver usuarios: " + e.getMessage());
        }
    }

    // EDITAR USUARIO
    public static boolean editarUsuario(
            int id,
            String nombre,
            String apellido,
            String codigoEmpleado,
            String rol,
            int estado) {

        String sql = "UPDATE usuario SET "
                + "nombre = ?, "
                + "apellido = ?, "
                + "codigo_empleado = ?, "
                + "rol = ?, "
                + "estado = ? "
                + "WHERE id_usuario = ?";

        try (Connection conn = conexionDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombre);
            stmt.setString(2, apellido);
            stmt.setString(3, codigoEmpleado);
            stmt.setString(4, rol);
            stmt.setInt(5, estado);
            stmt.setInt(6, id);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al editar usuario: " + e.getMessage());
            return false;
        }
    }

    // ELIMINAR USUARIO
    public static boolean eliminarUsuario(int id) {
        String sql = "DELETE FROM usuario WHERE id_usuario = ?";

        try (Connection conn = conexionDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
            return false;
        }
    }

    // LISTAR USUARIOS PARA LA TABLA (GUI)
    public static List<Object[]> listarUsuariosTabla() {
        List<Object[]> filas = new ArrayList<>();
        String sql = "SELECT nombre, apellido, codigo_empleado, rol, estado FROM usuario ORDER BY id_usuario";

        try (Connection conn = conexionDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String nombreCompleto = rs.getString("nombre") + " " + rs.getString("apellido");
                String codigo = rs.getString("codigo_empleado");
                String rolFormateado = "ADMINISTRADOR".equalsIgnoreCase(rs.getString("rol")) ? "Administrador"
                        : "Cajero";
                String estado = rs.getInt("estado") == 1 ? "Activo" : "Inactivo";
                filas.add(new Object[] { nombreCompleto, codigo, rolFormateado, estado, "" });
            }

        } catch (SQLException e) {
            System.err.println("Error al listar usuarios: " + e.getMessage());
        }
        return filas;
    }
}