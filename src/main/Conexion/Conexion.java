package main.Conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Centraliza la configuración y apertura de conexiones JDBC a MySQL.
public class Conexion {
    // Estos valores se usan para construir la URL de conexión del driver.
    private static final String USER = "root";
    private static final String PASSWORD = "123456789";
    private static final String URL = "jdbc:mysql://localhost:3306/GITEAT"
            + "?useSSL=false"
            + "&allowPublicKeyRetrieval=true"
            + "&serverTimezone=America/Guatemala";

    // Devuelve una conexión abierta o null si el servidor rechaza la conexión.
    public Connection getConnection() {
        Connection conx = null;
        try {
            conx = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexión Establecida");
        } catch (SQLException e) {
            System.out.println("Error en la Conexión: " + e.getMessage());
        }
        return conx;
    }
}