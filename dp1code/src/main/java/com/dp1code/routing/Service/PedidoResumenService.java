package com.dp1code.routing.Service;

import com.dp1code.routing.dto.PedidoResumenDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class PedidoResumenService {

    public List<PedidoResumenDTO> obtenerResumenPedidos(String fechaInicio, String fechaFin) {
        List<PedidoResumenDTO> resumen = new ArrayList<>();
        String sql = "CALL sp_indicadores_pedidos(?, ?)";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, fechaInicio);
            ps.setString(2, fechaFin);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PedidoResumenDTO dto = new PedidoResumenDTO();
                    dto.setTotalPedidos(rs.getInt("TotalPedidos"));
                    dto.setEntregados(rs.getInt("Entregados"));
                    dto.setPendientes(rs.getInt("Pendientes"));
                    dto.setPromedioGlpPorPedido(rs.getDouble("PromedioGlpPorPedido"));
                    dto.setPorcentajeCumplimiento(rs.getDouble("PorcentajeCumplimiento"));
                    dto.setPromedioTiempoEntregaMin(rs.getDouble("PromedioTiempoEntregaMin"));
                    dto.setTotalGlpEntregado(rs.getDouble("TotalGlpEntregado"));
                    resumen.add(dto);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener resumen de pedidos:");
            e.printStackTrace();
        }

        return resumen;
    }
}
