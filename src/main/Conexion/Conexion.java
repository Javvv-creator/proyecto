package main.Conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {
    private static final String USER = "root";
    private static final String PASSWORD = "123456789";
    private static final String URL = "jdbc:mysql://localhost:3306/GITEAT"
            + "?useSSL=false"
            + "&allowPublicKeyRetrieval=true"
            + "&serverTimezone=America/Guatemala";

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