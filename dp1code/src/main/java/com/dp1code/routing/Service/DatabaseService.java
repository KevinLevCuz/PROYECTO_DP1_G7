package com.dp1code.routing.Service;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

@Service
public class DatabaseService {
    // JDBC URL apuntando a tu esquema SISTEMA_DE_CAMIONES en RDS
    private static final String URL =
    "jdbc:mysql://equipo7g.c0gnuf8617fk.us-east-1.rds.amazonaws.com:3306/prueba_camiones_diario"
  + "?useSSL=false&serverTimezone=America/Lima";
    private static final String USER     = "admin";
    private static final String PASSWORD = "Equipo7G*";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new SQLException("Driver MySQL no encontrado", e);
        }
    }

    /**
     * Inserta un nuevo pedido en la tabla SISTEMA_DE_CAMIONES.Pedido.
     */
    public static void registrarPedido(int idPedido,
                                       String codigo,
                                       double cantidadGlp,
                                       LocalDateTime horaPedido,
                                       LocalDateTime plazoMaximoEntrega,
                                       LocalDateTime tiempoDescarga,
                                       String codCliente,
                                       int destinoNodoId) {
        String sql = """
            INSERT INTO Pedido (
              idPedido,
              codigo,
              cantidadGlp,
              horaPedido,
              plazoMaximoEntrega,
              tiempoDescarga,
              codCliente,
              destino
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt      (1, idPedido);
            ps.setString   (2, codigo);
            ps.setDouble   (3, cantidadGlp);
            ps.setTimestamp(4, java.sql.Timestamp.valueOf(horaPedido));
            ps.setTimestamp(5, java.sql.Timestamp.valueOf(plazoMaximoEntrega));
            ps.setTimestamp(6, java.sql.Timestamp.valueOf(tiempoDescarga));
            ps.setString   (7, codCliente);
            ps.setInt      (8, destinoNodoId);

            int filas = ps.executeUpdate();
            if (filas == 1) {
                System.out.println("Pedido registrado exitosamente: id=" + idPedido);
            } else {
                System.err.println("No se insertó ningún registro.");
            }

        } catch (SQLException e) {
            System.err.println("Error al registrar el pedido:");
            e.printStackTrace();
        }
    }

    /**
     * Método main de prueba: crea un pedido ejemplo y lo inserta.
     */
    public static void main(String[] args) {
        // Objeto: Pedido (Atributos)
        // Funcion(objetio.atributo, objetivo.atrivutp2)
        // SpingBoot:
        registrarPedido(
            4,
            "PED-20230614-001",
            150.75,
            LocalDateTime.parse("2025-06-14T08:30:00"),
            LocalDateTime.parse("2025-06-16T08:30:00"),
            LocalDateTime.parse("2025-06-14T00:00:45"),
            "CLI-001",
            101
        );

        // GET: variable = FUNCUION()
        // variable.cliente = RESULTADO
    }
}
