package main.Crud;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import main.Conexion.Conexion;

public class crud {

    // Instancia de la clase Conexion
    private static final Conexion conexionDB = new Conexion();

    // ==========================================
    // 1. CRUD CATEGORIA (CREAR, VER, EDITAR Y ELIMINAR)
    // ==========================================
    // CREAR CATEGORIA
    public static boolean crearCategoria(String nombre, String descripcion, int estado) {
        String sql = "INSERT INTO categoria (nombre, descripcion, estado) VALUES (?, ?, ?)";

        try (Connection conn = conexionDB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

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

        try (Connection conn = conexionDB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

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

        try (Connection conn = conexionDB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar categoría: " + e.getMessage());
            return false;
        }
    }

    // LISTAR NOMBRES DE CATEGORÍAS ACTIVAS (para combos y pestañas de la GUI)
    public static List<String> listarCategoriasActivas() {
        List<String> nombres = new ArrayList<>();
        String sql = "SELECT nombre FROM categoria WHERE estado = 1 ORDER BY id_categoria";

        try (Connection conn = conexionDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                nombres.add(rs.getString("nombre"));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar categorías activas: " + e.getMessage());
        }
        return nombres;
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

        try (Connection conn = conexionDB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

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
        String sql = "SELECT p.id_producto, p.nombre, p.precio_base, p.es_combo, p.estado, "
                + "c.nombre AS categoria, t.nombre AS turno "
                + "FROM producto p "
                + "LEFT JOIN categoria c ON p.id_categoria = c.id_categoria "
                + "LEFT JOIN turno_menu t ON p.id_turno = t.id_turno";

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

        try (Connection conn = conexionDB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

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

        try (Connection conn = conexionDB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
            return false;
        }
    }

    // LISTAR PRODUCTOS PARA LA TABLA DE gestionProductos
    // Cada fila: {id_producto, nombre, categoria, precio_base(BigDecimal), turno, tipo("Combo"/"Producto"), estado("Activo"/"Inactivo")}
    public static List<Object[]> listarProductosTabla() {
        List<Object[]> filas = new ArrayList<>();
        String sql = "SELECT p.id_producto, p.nombre, c.nombre AS categoria, p.precio_base, "
                + "t.nombre AS turno, p.es_combo, p.estado "
                + "FROM producto p "
                + "LEFT JOIN categoria c ON p.id_categoria = c.id_categoria "
                + "LEFT JOIN turno_menu t ON p.id_turno = t.id_turno "
                + "ORDER BY c.nombre, p.orden_menu";

        try (Connection conn = conexionDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String categoria = rs.getString("categoria");
                String turno = rs.getString("turno");
                filas.add(new Object[] {
                        rs.getInt("id_producto"),
                        rs.getString("nombre"),
                        categoria == null ? "Sin categoría" : categoria,
                        rs.getBigDecimal("precio_base"),
                        turno == null ? "Ambos" : turno,
                        rs.getBoolean("es_combo") ? "Combo" : "Producto",
                        rs.getInt("estado") == 1 ? "Activo" : "Inactivo"
                });
            }

        } catch (SQLException e) {
            System.err.println("Error al listar productos: " + e.getMessage());
        }
        return filas;
    }

    // CATÁLOGO PARA pantallaCajero (solo productos activos)
    // Cada fila: {id_producto, nombre, precio_base(BigDecimal), categoria, seccion, imagen}
    public static List<Object[]> listarCatalogoMenu() {
        List<Object[]> filas = new ArrayList<>();
        String sql = "SELECT p.id_producto, p.nombre, p.precio_base, c.nombre AS categoria, "
                + "p.seccion, p.imagen "
                + "FROM producto p "
                + "LEFT JOIN categoria c ON p.id_categoria = c.id_categoria "
                + "WHERE p.estado = 1 "
                + "ORDER BY c.id_categoria, p.orden_menu";

        try (Connection conn = conexionDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                filas.add(new Object[] {
                        rs.getInt("id_producto"),
                        rs.getString("nombre"),
                        rs.getBigDecimal("precio_base"),
                        rs.getString("categoria"),
                        rs.getString("seccion"),
                        rs.getString("imagen")
                });
            }

        } catch (SQLException e) {
            System.err.println("Error al listar catálogo del menú: " + e.getMessage());
        }
        return filas;
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

        try (Connection conn = conexionDB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

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

        try (Connection conn = conexionDB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

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

        try (Connection conn = conexionDB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

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
        String sql = "SELECT id_usuario, nombre, apellido, codigo_empleado, rol, estado FROM usuario ORDER BY id_usuario";

        try (Connection conn = conexionDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String nombreCompleto = rs.getString("nombre") + " " + rs.getString("apellido");
                String codigo = rs.getString("codigo_empleado");
                String rolFormateado = "ADMINISTRADOR".equalsIgnoreCase(rs.getString("rol")) ? "Administrador"
                        : "Cajero";
                String estado = rs.getInt("estado") == 1 ? "Activo" : "Inactivo";
                int idUsuario = rs.getInt("id_usuario");

                // El 6to elemento (idUsuario) NO se muestra como columna visible,
                // pero queda guardado en la fila del modelo para poder identificarla después.
                filas.add(new Object[] { nombreCompleto, codigo, rolFormateado, estado, "", idUsuario });
            }

        } catch (SQLException e) {
            System.err.println("Error al listar usuarios: " + e.getMessage());
        }
        return filas;
    }

    // LISTAR EMPLEADOS ACTIVOS AGRUPADOS/ORDENADOS POR TURNO (Dashboard)
    public static List<Object[]> listarEmpleadosPorTurno() {
        List<Object[]> filas = new ArrayList<>();
        String sql = "SELECT nombre, apellido, rol, turno FROM usuario WHERE estado = 1 AND rol = 'Cajero' ORDER BY turno, nombre";

        try (Connection conn = conexionDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String nombreCompleto = rs.getString("nombre") + " " + rs.getString("apellido");
                String rolFormateado = "ADMINISTRADOR".equalsIgnoreCase(rs.getString("rol")) ? "Administrador"
                        : "Cajero";
                String turno = rs.getString("turno");
                filas.add(new Object[] { nombreCompleto, rolFormateado, turno });
            }

        } catch (SQLException e) {
            System.err.println("Error al listar empleados por turno: " + e.getMessage());
        }
        return filas;
    }

    // OBTENER LA CONTRASEÑA ACTUAL DE UN USUARIO (para validar el cambio)
    public static String obtenerContrasena(int idUsuario) {
        String sql = "SELECT contrasena FROM usuario WHERE id_usuario = ?";

        try (Connection conn = conexionDB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("contrasena");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener contraseña: " + e.getMessage());
        }
        return null;
    }

    // OBTENER UN USUARIO POR ID (para precargar el formulario de edición)
    public static Object[] obtenerUsuario(int idUsuario) {
        String sql = "SELECT nombre, apellido, codigo_empleado, rol, estado FROM usuario WHERE id_usuario = ?";

        try (Connection conn = conexionDB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Object[] {
                            rs.getString("nombre"),
                            rs.getString("apellido"),
                            rs.getString("codigo_empleado"),
                            rs.getString("rol"),
                            rs.getInt("estado")
                    };
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener usuario: " + e.getMessage());
        }
        return null;
    }

    // ACTUALIZAR LA CONTRASEÑA DE UN USUARIO
    public static boolean actualizarContrasena(int idUsuario, String nuevaContrasena) {
        String sql = "UPDATE usuario SET contrasena = ? WHERE id_usuario = ?";

        try (Connection conn = conexionDB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nuevaContrasena);
            stmt.setInt(2, idUsuario);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar contraseña: " + e.getMessage());
            return false;
        }
    }

    // OBTIENE UN CAJERO ACTIVO POR DEFECTO (para abrir pantallaCajero sin login)
    // Devuelve {id_usuario, "Nombre Apellido", turno} o null si no hay ninguno.
    public static Object[] obtenerCajeroPredeterminado() {
        String sql = "SELECT id_usuario, nombre, apellido, turno FROM usuario "
                + "WHERE estado = 1 AND rol = 'CAJERO' ORDER BY id_usuario LIMIT 1";

        try (Connection conn = conexionDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return new Object[] {
                        rs.getInt("id_usuario"),
                        rs.getString("nombre") + " " + rs.getString("apellido"),
                        rs.getString("turno")
                };
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener cajero predeterminado: " + e.getMessage());
        }
        return null;
    }

    // ==========================================
    // 4. ÓRDENES / PEDIDOS
    // ==========================================

    // REGISTRA UNA ORDEN COMPLETA (orden + detalle_orden + pago_orden) en una sola transacción.
    // lineas: cada elemento es {id_producto(Integer), precio_unitario(Double), cantidad(Integer), nota(String)}
    // Devuelve el id_orden generado, o -1 si falló.
    public static int registrarOrden(int idUsuario, List<Object[]> lineas, String metodoPago) {
        if (lineas == null || lineas.isEmpty()) {
            return -1;
        }

        double total = 0;
        for (Object[] l : lineas) {
            double precio = (Double) l[1];
            int cantidad = (Integer) l[2];
            total += precio * cantidad;
        }

        String sqlOrden = "INSERT INTO orden (fecha, hora, estado, total, id_usuario) "
                + "VALUES (CURDATE(), CURTIME(), 'En cocina', ?, ?)";
        String sqlDetalle = "INSERT INTO detalle_orden "
                + "(cantidad, precio_unitario, subtotal, id_orden, id_producto, notas) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        String sqlPago = "INSERT INTO pago_orden (monto, metodo_pago, id_orden) VALUES (?, ?, ?)";

        try (Connection conn = conexionDB.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int idOrden;
                try (PreparedStatement stOrden = conn.prepareStatement(sqlOrden, Statement.RETURN_GENERATED_KEYS)) {
                    stOrden.setBigDecimal(1, BigDecimal.valueOf(total));
                    stOrden.setInt(2, idUsuario);
                    stOrden.executeUpdate();
                    try (ResultSet keys = stOrden.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("No se pudo obtener el id de la orden generada");
                        }
                        idOrden = keys.getInt(1);
                    }
                }

                try (PreparedStatement stDetalle = conn.prepareStatement(sqlDetalle)) {
                    for (Object[] l : lineas) {
                        int idProducto = (Integer) l[0];
                        double precio = (Double) l[1];
                        int cantidad = (Integer) l[2];
                        String nota = (String) l[3];

                        stDetalle.setInt(1, cantidad);
                        stDetalle.setBigDecimal(2, BigDecimal.valueOf(precio));
                        stDetalle.setBigDecimal(3, BigDecimal.valueOf(precio * cantidad));
                        stDetalle.setInt(4, idOrden);
                        stDetalle.setInt(5, idProducto);
                        if (nota == null || nota.isEmpty()) {
                            stDetalle.setNull(6, Types.VARCHAR);
                        } else {
                            stDetalle.setString(6, nota);
                        }
                        stDetalle.addBatch();
                    }
                    stDetalle.executeBatch();
                }

                try (PreparedStatement stPago = conn.prepareStatement(sqlPago)) {
                    stPago.setBigDecimal(1, BigDecimal.valueOf(total));
                    stPago.setString(2, metodoPago);
                    stPago.setInt(3, idOrden);
                    stPago.executeUpdate();
                }

                conn.commit();
                return idOrden;

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Error al registrar orden, se revirtió la transacción: " + e.getMessage());
                return -1;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.err.println("Error de conexión al registrar orden: " + e.getMessage());
            return -1;
        }
    }

    // LISTA TODAS LAS ÓRDENES (para gestionPedidos)
    // Cada fila: {id_orden, fecha+hora(java.util.Date), cajero, turno, estado}
    public static List<Object[]> listarOrdenes() {
        List<Object[]> filas = new ArrayList<>();
        String sql = "SELECT o.id_orden, o.fecha, o.hora, o.estado, "
                + "u.nombre, u.apellido, u.turno "
                + "FROM orden o "
                + "JOIN usuario u ON o.id_usuario = u.id_usuario "
                + "ORDER BY o.id_orden";

        try (Connection conn = conexionDB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                java.sql.Date fecha = rs.getDate("fecha");
                java.sql.Time hora = rs.getTime("hora");

                Calendar cal = Calendar.getInstance();
                cal.setTime(fecha);
                if (hora != null) {
                    Calendar horaCal = Calendar.getInstance();
                    horaCal.setTime(hora);
                    cal.set(Calendar.HOUR_OF_DAY, horaCal.get(Calendar.HOUR_OF_DAY));
                    cal.set(Calendar.MINUTE, horaCal.get(Calendar.MINUTE));
                    cal.set(Calendar.SECOND, horaCal.get(Calendar.SECOND));
                }

                String cajero = rs.getString("nombre") + " " + rs.getString("apellido");

                filas.add(new Object[] {
                        rs.getInt("id_orden"),
                        cal.getTime(),
                        cajero,
                        rs.getString("turno"),
                        rs.getString("estado")
                });
            }

        } catch (SQLException e) {
            System.err.println("Error al listar órdenes: " + e.getMessage());
        }
        return filas;
    }

    // LISTA EL DETALLE (productos) DE UNA ORDEN
    // Cada fila: {nombre_producto, cantidad, precio_unitario(Double), nota}
    public static List<Object[]> listarDetalleOrden(int idOrden) {
        List<Object[]> filas = new ArrayList<>();
        String sql = "SELECT p.nombre, d.cantidad, d.precio_unitario, d.notas "
                + "FROM detalle_orden d "
                + "JOIN producto p ON d.id_producto = p.id_producto "
                + "WHERE d.id_orden = ? "
                + "ORDER BY d.id_detalle";

        try (Connection conn = conexionDB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idOrden);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    filas.add(new Object[] {
                            rs.getString("nombre"),
                            rs.getInt("cantidad"),
                            rs.getBigDecimal("precio_unitario").doubleValue(),
                            rs.getString("notas") == null ? "" : rs.getString("notas")
                    });
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al listar detalle de orden: " + e.getMessage());
        }
        return filas;
    }

    // ACTUALIZA EL ESTADO DE UNA ORDEN (En cocina / Entregado / Anulado)
    public static boolean actualizarEstadoOrden(int idOrden, String nuevoEstado) {
        String sql = "UPDATE orden SET estado = ? WHERE id_orden = ?";

        try (Connection conn = conexionDB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nuevoEstado);
            stmt.setInt(2, idOrden);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar estado de orden: " + e.getMessage());
            return false;
        }
    }
}