package main;

import gui.pantallaCarga;
import javax.swing.JOptionPane;
import main.Conexion.Conexion;

public class Main {

    public static void main(String[] args) {

        try {

            Conexion conectar = new Conexion();

            if (conectar.getConnection() == null) {
                throw new Exception(
                        "No se pudo establecer la conexión con la base de datos."
                );
            }

            // Iniciar pantalla de carga
            pantallaCarga carga = new pantallaCarga();
            carga.iniciarCarga();

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    null,
                    "Error al conectar con la base de datos\n"
                    + e.getMessage(),
                    "Error de conexión",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}