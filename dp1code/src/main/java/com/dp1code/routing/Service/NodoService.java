package com.dp1code.routing.Service;

import com.dp1code.routing.Model.Nodo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.Optional;

@Service
public class NodoService {

    public Optional<Nodo> getNodoPorCoordenadas(int posX, int posY) {
        String sql = "SELECT posX, posY, bloqueado FROM Nodo WHERE posX = ? AND posY = ?";
        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, posX);
            ps.setInt(2, posY);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Nodo nodo = new Nodo();
                nodo.setPosX(rs.getInt("posX"));
                nodo.setPosY(rs.getInt("posY"));
                nodo.setBloqueado(rs.getBoolean("bloqueado"));
                return Optional.of(nodo);
            }

        } catch (SQLException e) {
            System.err.println("Error al consultar nodo:");
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public void registrarNodo(Nodo nodo) {
        String sql = "INSERT INTO Nodo (posX, posY, bloqueado) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, nodo.getPosX());
            ps.setInt(2, nodo.getPosY());
            ps.setBoolean(3, nodo.isBloqueado());

            int filas = ps.executeUpdate();
            if (filas == 1) {
                System.out.println("Nodo registrado: (" + nodo.getPosX() + "," + nodo.getPosY() + ")");
            } else {
                System.err.println("No se insertó ningún nodo.");
            }

        } catch (SQLException e) {
            System.err.println("Error al registrar nodo:");
            e.printStackTrace();
        }
    }
    public static Nodo getNodoPorIdDiaDia(int id) {
        String sql = "SELECT posX, posY, bloqueado FROM prueba_camiones_diario.Nodo WHERE id = ?";

        try (
                Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
                    System.out.println("INSTANCIA 1");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
                    System.out.println("INSTANCIA 2");
            if (rs.next()) {
                Nodo nodo = new Nodo();
                nodo.setPosX(rs.getInt("posX"));
                nodo.setPosY(rs.getInt("posY"));
                nodo.setBloqueado(rs.getBoolean("bloqueado"));
                System.out.println("INSTANCIA 3");
                return nodo;
                
            } else {
                throw new RuntimeException("Nodo no encontrado con id: " + id);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar nodo con id: " + id, e);
        }
    }
    public Nodo getNodoPorId(int id) {
        String sql = "SELECT posX, posY, bloqueado FROM prueba_camiones_exper.Nodo WHERE id = ?";

        try (
                Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
                    System.out.println("INSTANCIA 1");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
                    System.out.println("INSTANCIA 2");
            if (rs.next()) {
                Nodo nodo = new Nodo();
                nodo.setPosX(rs.getInt("posX"));
                nodo.setPosY(rs.getInt("posY"));
                nodo.setBloqueado(rs.getBoolean("bloqueado"));
                System.out.println("INSTANCIA 3");
                return nodo;
                
            } else {
                throw new RuntimeException("Nodo no encontrado con id: " + id);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar nodo con id: " + id, e);
        }
    }
}
