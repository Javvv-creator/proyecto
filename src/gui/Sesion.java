package gui;

/**
 * Guarda los datos del usuario que inició sesión.
 * Se llena en pantallaLogin y se lee desde cualquier pantalla.
 */
public final class Sesion {

    private static int idUsuario = 0;
    private static String codigoEmpleado = "";
    private static String nombre = "";
    private static String apellido = "";
    private static String rol = "";
    private static String turno = "";

    private Sesion() {
    }

    public static synchronized void iniciar(int id, String codigo, String nom, String ape, String rolUsuario,
            String turnoUsuario) {
        idUsuario = id;
        codigoEmpleado = codigo == null ? "" : codigo.trim();
        nombre = nom == null ? "" : nom.trim();
        apellido = ape == null ? "" : ape.trim();
        rol = rolUsuario == null ? "" : rolUsuario.trim();
        turno = turnoUsuario == null ? "" : turnoUsuario.trim();
    }

    public static synchronized void cerrar() {
        idUsuario = 0;
        codigoEmpleado = nombre = apellido = rol = turno = "";
    }

    public static synchronized boolean estaActiva() {
        return idUsuario > 0;
    }

    public static synchronized int getIdUsuario() {
        return idUsuario;
    }

    public static synchronized String getCodigoEmpleado() {
        return codigoEmpleado;
    }

    public static synchronized String getNombreCompleto() {
        String n = (nombre + " " + apellido).trim();
        return n.isEmpty() ? "Sin sesión" : n;
    }

    /** "CAJERO" -> "Cajero" */
    public static synchronized String getRol() {
        if (rol.isEmpty())
            return "";
        return rol.substring(0, 1).toUpperCase() + rol.substring(1).toLowerCase();
    }

    /** "Mañana", "Tarde" o "Noche" (columna usuario.turno) */
    public static synchronized String getTurno() {
        return turno;
    }
}