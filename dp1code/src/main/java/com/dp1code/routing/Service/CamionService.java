package com.dp1code.routing.Service;

import com.dp1code.routing.Model.Camion;
import com.dp1code.routing.Model.Grid;
import com.dp1code.routing.Model.Nodo;
import com.dp1code.routing.dto.CamionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.sql.Timestamp;
import java.sql.Connection;

import org.springframework.stereotype.Service;

@Service
public class CamionService {

    public void registrarCamion(CamionDTO camion) {
        System.out.println("Camión recibido:");
        System.out.println("Código: " + camion.getCodigo());
        System.out.println("Tipo: " + camion.getTipo());
        // Agrega lógica de persistencia si lo deseas
    }

    public boolean actualizarCamionesBatch(List<Camion> camiones, Connection conn, LocalDateTime fechaSimulada) {
        String sql = "UPDATE prueba_camiones_exper.Camion c SET " +
                "glpTanque = ?, " +
                "glpActual = ?, " +
                "ubicacionActual_id = (SELECT id FROM prueba_camiones_diario.Nodo WHERE posX = ? AND posY = ?), " +
                "num_pedidos_entregados = ?, " +
                "estado = ( " +
                "  SELECT CASE " +
                "    WHEN EXISTS ( " +
                "      SELECT 1 FROM prueba_camiones_exper.Mantenimiento m " +
                "      WHERE m.codigoCamion = c.codigo " +
                "        AND ? BETWEEN m.inicio AND m.fin " +
                "    ) THEN 'ND' " +
                "    ELSE 'L' " +
                "  END " +
                ") " +
                "WHERE codigo = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Camion camion : camiones) {
                ps.setDouble(1, camion.getGlpTanque());
                ps.setDouble(2, camion.getGlpActual());
                ps.setInt(3, camion.getUbicacionActual().getPosX());
                ps.setInt(4, camion.getUbicacionActual().getPosY());
                ps.setInt(5, camion.getNumPedidosAtendidos());
                ps.setTimestamp(6, Timestamp.valueOf(fechaSimulada));
                ps.setString(7, camion.getCodigo());

                ps.addBatch();
            }
            ps.executeBatch();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarCamionesBatchDiaADia(List<Camion> camiones, Connection conn) {
        String sql = "UPDATE prueba_camiones_diario.Camion SET " +
                "glpTanque = ?, " +
                "glpActual = ?, " +
                "ubicacionActual_id = (SELECT id FROM prueba_camiones_diario.Nodo WHERE posX = ? AND posY = ?), " +
                "num_pedidos_entregados = ? " + // <- espacio agregado
                "WHERE codigo = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Camion camion : camiones) {
                ps.setDouble(1, camion.getGlpTanque());
                ps.setDouble(2, camion.getGlpActual());
                ps.setInt(3, camion.getUbicacionActual().getPosX());
                ps.setInt(4, camion.getUbicacionActual().getPosY());
                ps.setInt(5, camion.getNumPedidosAtendidos());
                ps.setString(6, camion.getCodigo());

                ps.addBatch();
            }
            ps.executeBatch();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarGlpCargaTodosCamionDiaDia() {
        String sql = "UPDATE prueba_camiones_diario.Camion SET glpActual = capacidadMaxima, num_pedidos_entregados = 0 ";

        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar la ubicación del camión", e);
        }
    }

    public boolean actualizarGlpTanqueTodosCamionDiaDia() {
        String sql = "UPDATE prueba_camiones_diario.Camion SET glpTanque = 25";

        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar la ubicación del camión", e);
        }
    }

    public ArrayList<Camion> obtenerTodosLosCamionesDiaDia(Grid grid) {
        ArrayList<Camion> camiones = new ArrayList<>();
        String sql = """
                SELECT c.codigo, c.tipo, c.pesoVacio, c.ubicacionActual_id, c.capacidadMaxima,
                       c.glpActual, c.glpTanque, c.enRuta, c.disponibleDesde, c.horaLibre, n.*
                FROM prueba_camiones_diario.Camion c INNER JOIN prueba_camiones_diario.Nodo n ON c.ubicacionActual_id = n.id
                """;

        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Camion camion = new Camion();
                camion.setCodigo(rs.getString("codigo"));
                camion.setTipo(rs.getString("tipo"));
                camion.setPesoVacio(rs.getDouble("pesoVacio"));
                camion.setCapacidadMaxima(rs.getDouble("capacidadMaxima"));
                camion.setGlpActual(rs.getDouble("glpActual"));
                camion.setGlpTanque(rs.getDouble("glpTanque"));
                camion.setEnRuta(rs.getBoolean("enRuta"));
                camion.setDisponibleDesde(rs.getTimestamp("disponibleDesde").toLocalDateTime());
                camion.setHoraLibre(rs.getTimestamp("horaLibre").toLocalDateTime());

                Nodo destino = new Nodo();
                destino.setId(rs.getString("ubicacionActual_id"));
                destino.setPosX(rs.getInt("posX"));
                destino.setPosY(rs.getInt("posY"));
                destino.setBloqueado(rs.getBoolean("bloqueado"));
                camion.setUbicacionActual(grid.getNodoAt(destino.getPosX(), destino.getPosY()));

                camiones.add(camion);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener camiones:");
            e.printStackTrace();
        }

        return camiones;
    }

    public boolean actualizarUbicacionTodosCamionDiaDia() {
        String sql = "UPDATE prueba_camiones_diario.Camion SET ubicacionActual_id = 581";

        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar la ubicación del camión", e);
        }
    }

    public boolean actualizarUbicacionCamion(int posX, int posY, String codigoCamion) {
        String sql = "UPDATE prueba_camiones_exper.Camion SET ubicacionActual_id = (SELECT id FROM prueba_camiones_exper.Nodo WHERE posX= ? AND posY=?) WHERE codigo=?";

        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, posX);
            ps.setInt(2, posY);
            ps.setString(3, codigoCamion);

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar la ubicación del camión", e);
        }
    }

    public boolean actualizarUbicacionTodosCamion() {
        String sql = "UPDATE prueba_camiones_exper.Camion SET ubicacionActual_id = 581";

        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar la ubicación del camión", e);
        }
    }

    public boolean actualizarGlpTanqueTodosCamion() {
        String sql = "UPDATE prueba_camiones_exper.Camion SET glpTanque = 25";

        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar la ubicación del camión", e);
        }
    }

    public boolean actualizarGlpCargaTodosCamion() {
        String sql = "UPDATE prueba_camiones_exper.Camion SET glpActual = capacidadMaxima, num_pedidos_entregados = 0, combustible_consumido = 0";

        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar la ubicación del camión", e);
        }
    }

    public boolean actualizarGlpTanqueCamion(double glpTanque, String codigoCamion) {
        String sql = "UPDATE Camion SET glpTanque = ? WHERE codigo = ?";

        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, glpTanque);
            ps.setString(2, codigoCamion);

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar la ubicación del camión", e);
        }
    }

    public boolean actualizarGlpCargaCamion(double glpActual, String codigoCamion) {
        String sql = "UPDATE Camion SET glpActual = ? WHERE codigo = ?";

        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, glpActual);
            ps.setString(2, codigoCamion);

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar la ubicación del camión", e);
        }
    }

    public ArrayList<Camion> obtenerTodosLosCamiones() {
        ArrayList<Camion> camiones = new ArrayList<>();
        String sql = """
                SELECT c.codigo, c.tipo, c.pesoVacio, c.ubicacionActual_id, c.capacidadMaxima,
                       c.glpActual, c.glpTanque, c.enRuta, c.disponibleDesde, c.horaLibre, c.num_pedidos_entregados, c.estado, n.*
                FROM Camion c INNER JOIN prueba_camiones_exper.Nodo n ON c.ubicacionActual_id = n.id
                """;

        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Camion camion = new Camion();
                camion.setCodigo(rs.getString("codigo"));
                camion.setTipo(rs.getString("tipo"));
                camion.setPesoVacio(rs.getDouble("pesoVacio"));
                camion.setCapacidadMaxima(rs.getDouble("capacidadMaxima"));
                camion.setGlpActual(rs.getDouble("glpActual"));
                camion.setGlpTanque(rs.getDouble("glpTanque"));
                camion.setEnRuta(rs.getBoolean("enRuta"));
                camion.setDisponibleDesde(rs.getTimestamp("disponibleDesde").toLocalDateTime());
                camion.setHoraLibre(rs.getTimestamp("horaLibre").toLocalDateTime());
                camion.setNumPedidosAtendidos(rs.getInt("num_pedidos_entregados"));
                camion.setEstado(rs.getString("estado"));
                Nodo destino = new Nodo();
                destino.setId(rs.getString("ubicacionActual_id"));
                destino.setPosX(rs.getInt("posX"));
                destino.setPosY(rs.getInt("posY"));
                destino.setBloqueado(rs.getBoolean("bloqueado"));
                camion.setUbicacionActual(destino);

                camiones.add(camion);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener camiones:");
            e.printStackTrace();
        }

        return camiones;
    }
    public boolean actualizarEstadoEnRutaPosotivo(String codigoCamion) {
        String sql = "UPDATE Camion SET enRuta = ? WHERE codigo = ?";

        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, true);
            ps.setString(2, codigoCamion);

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar estado enRuta del camión con código: " + codigoCamion, e);
        }
    }

    public boolean actualizarEstadoEnRutaNegativo(String codigoCamion) {
        String sql = "UPDATE Camion SET enRuta = ? WHERE codigo = ?";

        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, false);
            ps.setString(2, codigoCamion);

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar estado enRuta del camión con código: " + codigoCamion, e);
        }
    }

    public void actualizarCamionesJson(List<Camion> camiones) {
        String sql = "CALL prueba_camiones_exper.actualizar_camiones_json(?)";

        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(camiones);

            ps.setString(1, json);
            ps.execute();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar camiones por JSON", e);
        }
    }
}
