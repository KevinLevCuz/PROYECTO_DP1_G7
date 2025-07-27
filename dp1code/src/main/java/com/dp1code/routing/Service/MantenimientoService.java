package com.dp1code.routing.Service;

import com.dp1code.routing.Model.Mantenimiento;
import com.dp1code.routing.Model.TimeRange;

import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MantenimientoService {

    public void registrarMantenimiento(Mantenimiento mantenimiento) {
        String sql = """
                    INSERT INTO Mantenimiento (inicio, fin, codigoCamion, tipo)
                    VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(mantenimiento.getInicio()));
            ps.setTimestamp(2, Timestamp.valueOf(mantenimiento.getFin()));
            ps.setString(3, mantenimiento.getCodigoCamion());
            ps.setString(4, mantenimiento.getTipo());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar mantenimiento", e);
        }
    }

    public List<Mantenimiento> listarMantenimientos() {
        List<Mantenimiento> lista = new ArrayList<>();
        String sql = "SELECT * FROM Mantenimiento";

        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Mantenimiento m = new Mantenimiento(
                        rs.getTimestamp("inicio").toLocalDateTime(),
                        rs.getTimestamp("fin").toLocalDateTime(),
                        rs.getString("codigoCamion"),
                        rs.getString("tipo"));
                lista.add(m);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al listar mantenimientos", e);
        }

        return lista;
    }

    public List<TimeRange> obtenerRangosPorCodigoCamion(String codigoCamion) {
        List<TimeRange> rangos = new ArrayList<>();
        String sql = "SELECT inicio, fin FROM Mantenimiento WHERE codigoCamion = ?";

        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, codigoCamion);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LocalDateTime inicio = rs.getTimestamp("inicio").toLocalDateTime();
                LocalDateTime fin = rs.getTimestamp("fin").toLocalDateTime();
                rangos.add(new TimeRange(inicio, fin));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar mantenimientos por códigoCamion", e);
        }

        return rangos;
    }

}
