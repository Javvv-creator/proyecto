package gui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Pedido cobrado en pantallaCajero. */
public class Pedido {

    /** Una línea del pedido (producto, cantidad, nota). */
    public static class Linea {
        private final int idProducto;
        private final String nombre;
        private final double precioUnitario;
        private final int cantidad;
        private final String nota;

        public Linea(int idProducto, String nombre, double precioUnitario, int cantidad, String nota) {
            this.idProducto = idProducto;
            this.nombre = nombre;
            this.precioUnitario = precioUnitario;
            this.cantidad = cantidad;
            this.nota = nota == null ? "" : nota;
        }

        public int getIdProducto() { return idProducto; }
        public String getNombre() { return nombre; }
        public double getPrecioUnitario() { return precioUnitario; }
        public int getCantidad() { return cantidad; }
        public String getNota() { return nota; }
        public double getSubtotal() { return precioUnitario * cantidad; }
    }

    private final int numero;
    private final Date fecha;
    private final String cajero;
    private final String turno;
    private final List<Linea> lineas;
    private String estado = "En cocina";

    public Pedido(int numero, Date fecha, String cajero, String turno, List<Linea> lineas) {
        this.numero = numero;
        this.fecha = fecha;
        this.cajero = cajero;
        this.turno = turno;
        this.lineas = new ArrayList<>(lineas);
    }

    public Pedido(int numero, Date fecha, String cajero, String turno, List<Linea> lineas, String estado) {
        this(numero, fecha, cajero, turno, lineas);
        this.estado = estado;
    }

    public int getNumero() { return numero; }
    public Date getFecha() { return fecha; }
    public String getCajero() { return cajero; }
    public String getTurno() { return turno; }
    public List<Linea> getLineas() { return lineas; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public double getTotal() {
        double t = 0;
        for (Linea l : lineas) t += l.getSubtotal();
        return t;
    }

    public int getTotalUnidades() {
        int u = 0;
        for (Linea l : lineas) u += l.getCantidad();
        return u;
    }

    /** Ej: "2x Big Tasty, 1x Horchata" */
    public String resumenProductos() {
        StringBuilder sb = new StringBuilder();
        for (Linea l : lineas) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(l.getCantidad()).append("x ").append(l.getNombre());
        }
        return sb.toString();
    }
}