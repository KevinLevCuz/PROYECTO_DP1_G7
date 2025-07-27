package com.dp1code.routing.Service;

import com.dp1code.routing.dto.CamionStatsDTO;
import com.dp1code.routing.dto.PedidoStatsDTO;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class EstadisticasService {

  public List<CamionStatsDTO> obtenerEstadisticasCamiones() {
    String sql = """
            SELECT
              codigo,
              SUM(num_pedidos_entregados) AS total_entregados,
              (SUM(num_pedidos_entregados) / total_filas) * 100 AS porcentaje_global
            FROM prueba_camiones.Camion,
              (SELECT COUNT(*) AS total_filas FROM prueba_camiones.Camion) AS total
            GROUP BY codigo;
        """;

    List<CamionStatsDTO> lista = new ArrayList<>();
    try (Connection conn = DatabaseService.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {

      while (rs.next()) {
        CamionStatsDTO dto = new CamionStatsDTO();
        dto.setCodigo(rs.getString("codigo"));
        dto.setTotalEntregados(rs.getInt("total_entregados"));
        dto.setPorcentajeGlobal(rs.getDouble("porcentaje_global"));
        lista.add(dto);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return lista;
  }

  public List<CamionStatsDTO> obtenerEstadisticasCamionesDiaDia() {
    String sql = """
            SELECT
              codigo,
              SUM(num_pedidos_entregados) AS total_entregados,
              (SUM(num_pedidos_entregados) / total_filas) * 100 AS porcentaje_global
            FROM prueba_camiones_diario.Camion,
              (SELECT COUNT(*) AS total_filas FROM prueba_camiones_diario.Camion) AS total
            GROUP BY codigo;
        """;

    List<CamionStatsDTO> lista = new ArrayList<>();
    try (Connection conn = DatabaseService.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {

      while (rs.next()) {
        CamionStatsDTO dto = new CamionStatsDTO();
        dto.setCodigo(rs.getString("codigo"));
        dto.setTotalEntregados(rs.getInt("total_entregados"));
        dto.setPorcentajeGlobal(rs.getDouble("porcentaje_global"));
        lista.add(dto);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return lista;
  }

  public List<PedidoStatsDTO> obtenerEstadisticasPedidos() {
    String sql = """
            SELECT
              id,
              horaPedido,
              plazoMaximoEntrega,
              hora_entrega,
              TIMESTAMPDIFF(MINUTE, horaPedido, plazoMaximoEntrega) AS tiempo_disponible,
              TIMESTAMPDIFF(MINUTE, horaPedido, hora_entrega) AS tiempo_real,
              ROUND(
                (
                  TIMESTAMPDIFF(MINUTE, horaPedido, hora_entrega) /
                  TIMESTAMPDIFF(MINUTE, horaPedido, plazoMaximoEntrega)
                ) * 100, 2
              ) AS porcentaje_utilizado
            FROM prueba_camiones.Pedido
            WHERE hora_entrega IS NOT NULL;
        """;

    List<PedidoStatsDTO> lista = new ArrayList<>();
    try (Connection conn = DatabaseService.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {

      while (rs.next()) {
        PedidoStatsDTO dto = new PedidoStatsDTO();
        dto.setId(rs.getInt("id"));
        dto.setHoraPedido(rs.getString("horaPedido"));
        dto.setPlazoMaximoEntrega(rs.getString("plazoMaximoEntrega"));
        dto.setHoraEntrega(rs.getString("hora_entrega"));
        dto.setTiempoDisponible(rs.getInt("tiempo_disponible"));
        dto.setTiempoReal(rs.getInt("tiempo_real"));
        dto.setPorcentajeUtilizado(rs.getDouble("porcentaje_utilizado"));
        lista.add(dto);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return lista;
  }

  public List<PedidoStatsDTO> obtenerEstadisticasPedidosDiaDia() {
    String sql = """
            SELECT
              id,
              horaPedido,
              plazoMaximoEntrega,
              hora_entrega,
              TIMESTAMPDIFF(MINUTE, horaPedido, plazoMaximoEntrega) AS tiempo_disponible,
              TIMESTAMPDIFF(MINUTE, horaPedido, hora_entrega) AS tiempo_real,
              ROUND(
                (
                  TIMESTAMPDIFF(MINUTE, horaPedido, hora_entrega) /
                  TIMESTAMPDIFF(MINUTE, horaPedido, plazoMaximoEntrega)
                ) * 100, 2
              ) AS porcentaje_utilizado
            FROM prueba_camiones_diario.Pedido
            WHERE hora_entrega IS NOT NULL;
        """;

    List<PedidoStatsDTO> lista = new ArrayList<>();
    try (Connection conn = DatabaseService.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {

      while (rs.next()) {
        PedidoStatsDTO dto = new PedidoStatsDTO();
        dto.setId(rs.getInt("id"));
        dto.setHoraPedido(rs.getString("horaPedido"));
        dto.setPlazoMaximoEntrega(rs.getString("plazoMaximoEntrega"));
        dto.setHoraEntrega(rs.getString("hora_entrega"));
        dto.setTiempoDisponible(rs.getInt("tiempo_disponible"));
        dto.setTiempoReal(rs.getInt("tiempo_real"));
        dto.setPorcentajeUtilizado(rs.getDouble("porcentaje_utilizado"));
        lista.add(dto);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return lista;
  }
}
