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

    public PedidoResumenDTO obtenerResumenPedidos(String fechaInicio, String fechaFin) {
        PedidoResumenDTO resumen = new PedidoResumenDTO();
        String sql = "CALL sp_indicadores_pedidos(?, ?)";

        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, fechaInicio);
            ps.setString(2, fechaFin);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    resumen.setTotalPedidos(rs.getInt("TotalPedidos"));
                    resumen.setEntregados(rs.getInt("Entregados"));
                    resumen.setPendientes(rs.getInt("Pendientes"));
                    resumen.setPromedioGlpPorPedido(rs.getDouble("PromedioGlpPorPedido"));
                    resumen.setPorcentajeCumplimiento(rs.getDouble("PorcentajeCumplimiento"));
                    resumen.setPromedioTiempoEntregaMin(rs.getDouble("PromedioTiempoEntregaMin"));
                    resumen.setTotalGlpEntregado(rs.getDouble("TotalGlpEntregado"));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener resumen de pedidos:");
            e.printStackTrace();
        }

        return resumen;
    }
}
