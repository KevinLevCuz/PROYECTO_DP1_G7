package com.dp1code.routing.Service;

import com.dp1code.routing.Model.Nodo;
import com.dp1code.routing.Model.Planta;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class PlantaService {

    public ArrayList<Planta> obtenerTodas() {
        ArrayList<Planta> plantas = new ArrayList<>();
        String sql = "SELECT id, tipo, ubicacion_id, capacidadMaxima, glpDisponible, siguienteRecarga, intervaloRecarga FROM Planta";

        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Nodo nodo = new Nodo();
                NodoService serviceNodo = new NodoService();
                int ubicacion_id = rs.getInt("ubicacion_id");
                
                /*nodo.setPosX(rs.getInt("ubicacion_id")); // simulado
                nodo.setPosY(0);*/

                Planta planta = new Planta();
                planta.setId(rs.getInt("id"));
                planta.setTipo(rs.getString("tipo"));
                planta.setUbicacion(nodo);
                planta.setCapacidadMaxima(rs.getDouble("capacidadMaxima"));
                planta.setGlpDisponible(rs.getDouble("glpDisponible"));
                planta.setSiguienteRecarga(rs.getTimestamp("siguienteRecarga").toLocalDateTime());
                planta.setIntervaloRecarga(rs.getTimestamp("intervaloRecarga").toLocalDateTime());
                nodo = serviceNodo.getNodoPorId(ubicacion_id);
                planta.setUbicacion(nodo);
                plantas.add(planta);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return plantas;
    }
    public boolean actualizarPlantasBatchDiaDia(List<Planta> plantas, Connection conn) {
        String sql = "UPDATE prueba_camiones_diario.Planta SET glpDisponible = ? WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Planta planta : plantas) {
                ps.setDouble(1, planta.getGlpDisponible());
                ps.setInt(2, planta.getId());
                ps.addBatch();
            }
            ps.executeBatch();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public ArrayList<Planta> obtenerTodasDiaDia() {
        ArrayList<Planta> plantas = new ArrayList<>();
        String sql = "SELECT id, tipo, ubicacion_id, capacidadMaxima, glpDisponible, siguienteRecarga, intervaloRecarga FROM prueba_camiones_diario.Planta";

        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Nodo nodo = new Nodo();
                NodoService serviceNodo = new NodoService();
                int ubicacion_id = rs.getInt("ubicacion_id");

                /*
                 * nodo.setPosX(rs.getInt("ubicacion_id")); // simulado
                 * nodo.setPosY(0);
                 */

                Planta planta = new Planta();
                planta.setId(rs.getInt("id"));
                planta.setTipo(rs.getString("tipo"));
                planta.setUbicacion(nodo);
                planta.setCapacidadMaxima(rs.getDouble("capacidadMaxima"));
                planta.setGlpDisponible(rs.getDouble("glpDisponible"));
                planta.setSiguienteRecarga(rs.getTimestamp("siguienteRecarga").toLocalDateTime());
                planta.setIntervaloRecarga(rs.getTimestamp("intervaloRecarga").toLocalDateTime());
                nodo = serviceNodo.getNodoPorId(ubicacion_id);
                planta.setUbicacion(nodo);
                plantas.add(planta);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return plantas;
    }
    public boolean actualizarTodasLasPlantasDiaDia() {
        String sql = "UPDATE prueba_camiones_diario.Planta SET glpDisponible=capacidadMaxima";

        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar la ubicación del camión", e);
        }
    }

    public boolean actualizarPlantasBatch(List<Planta> plantas, Connection conn) {
        String sql = "UPDATE prueba_camiones.Planta SET glpDisponible = ? WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Planta planta : plantas) {
                ps.setDouble(1, planta.getGlpDisponible());
                ps.setInt(2, planta.getId());
                ps.addBatch();
            }
            ps.executeBatch();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarTodasLasPlantas() {
        String sql = "UPDATE Planta SET glpDisponible=capacidadMaxima";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar la ubicación del camión", e);
        }
    }

    public boolean actualizarGlpPlanta(double glpActual, int id) {
        String sql = "UPDATE Planta SET glpDisponible = ? WHERE id = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, glpActual);
            ps.setInt(2, id);

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar la ubicación del camión", e);
        }
    }
    public void actualizarPlantasJson(List<Planta> plantas) {
        String sql = "CALL prueba_camiones.actualizar_plantas_json(?)";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(plantas);

            ps.setString(1, json);
            ps.execute();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar plantas por JSON", e);
        }
    }

}
