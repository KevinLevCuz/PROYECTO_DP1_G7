package com.dp1code.routing.Service;

import com.dp1code.routing.dto.CamionResumenDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class CamionResumenService {

    public List<CamionResumenDTO> obtenerResumenCamiones() {
        List<CamionResumenDTO> resumen = new ArrayList<>();
        String sql = """
            SELECT 
                codigo, 
                num_pedidos_entregados, 
                combustible_consumido, 
                CASE 
                    WHEN num_pedidos_entregados = 0 THEN 0 
                    ELSE combustible_consumido / num_pedidos_entregados 
                END AS promedioGLPPedido
            FROM prueba_camiones.Camion;
        """;

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                CamionResumenDTO dto = new CamionResumenDTO();
                dto.setCodigo(rs.getString("codigo"));
                dto.setPedidosAtendidos(rs.getInt("num_pedidos_entregados"));
                dto.setGlpConsumido(rs.getDouble("combustible_consumido"));
                dto.setPromedioGlpPorPedido(rs.getDouble("promedioGLPPedido"));
                resumen.add(dto);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener resumen de camiones:");
            e.printStackTrace();
        }

        return resumen;
    }
}
