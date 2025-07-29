package com.dp1code.routing.Service;

import com.dp1code.routing.dto.PedidoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dp1code.routing.Model.Nodo;
import com.dp1code.routing.Model.Pedido;
import com.dp1code.routing.Service.DatabaseService;
import java.time.LocalDateTime;

@Service
public class PedidoService {

    @Autowired
    private DatabaseService databaseService;

    public boolean actualizarTodosPedidosANoEntregados() {
        String sql = "UPDATE prueba_camiones_exper.Pedido SET entregado=0 WHERE entregado=1";

        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar la ubicación del camión", e);
        }
    }

    public ArrayList<Pedido> obtenerPedidosConSiguienteEnRango(LocalDateTime inicio, LocalDateTime fin) {
        ArrayList<Pedido> pedidos = new ArrayList<>();

        String sql = "WITH pedidos_con_siguiente AS ( " +
                "  SELECT  " +
                "    ped.id,  " +
                "    ped.destino_id,  " +
                "    ped.cantidadGlp,  " +
                "    ped.horaPedido,  " +
                "    ped.plazoMaximoEntrega,  " +
                "    ped.tiempoDescarga,  " +
                "    ped.idCliente, "+
                "    ped.entregado,  " +
                "    LEAD(ped.horaPedido) OVER (ORDER BY ped.horaPedido) AS siguienteHoraPedido, " +
                "    LEAD(ped.id) OVER (ORDER BY ped.horaPedido) AS siguienteId  " +
                "  FROM  " +
                "    prueba_camiones_diario.Pedido ped " +
                ") " +
                "SELECT ps.*, n.posX, n.posY " +
                "FROM pedidos_con_siguiente ps " +
                "INNER JOIN prueba_camiones_diario.Nodo n ON ps.destino_id = n.id " +
                "WHERE horaPedido >= ? AND horaPedido <= ?";

        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(inicio));
            ps.setTimestamp(2, Timestamp.valueOf(fin));

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Pedido pedido = new Pedido();
                Nodo destino = new Nodo();
                destino.setId(rs.getString("destino_id"));
                destino.setPosX(rs.getInt("posX"));
                destino.setPosY(rs.getInt("posY"));
                pedido.setId(String.valueOf(rs.getInt("id")));
                pedido.setCantidadGlp(rs.getDouble("cantidadGlp"));
                pedido.setHoraPedido(rs.getTimestamp("horaPedido").toLocalDateTime());
                pedido.setPlazoMaximoEntrega(rs.getTimestamp("plazoMaximoEntrega").toLocalDateTime());
                pedido.setTiempoDescarga(rs.getTimestamp("tiempoDescarga").toLocalDateTime());
                pedido.setIdCliente(rs.getString("idCliente"));
                pedido.setEntregado(rs.getBoolean("entregado"));
                pedido.setHoraSiguientePedido(fin);
                pedido.setSigId(String.valueOf(rs.getInt("siguienteId")));
                pedido.setDestino(destino);
                pedidos.add(pedido);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return pedidos;
    }

    public boolean insertarPedido(PedidoDTO input) {
        try (Connection conn = DatabaseService.getConnection()) {

            // Obtener el Nodo por coordenadas
            String sqlNodo = "SELECT id FROM prueba_camiones_diario.Nodo WHERE posX = ? AND posY = ?";
            int nodoId = -1;

            try (PreparedStatement psNodo = conn.prepareStatement(sqlNodo)) {
                psNodo.setInt(1, input.getPosX());
                psNodo.setInt(2, input.getPosY());
                ResultSet rsNodo = psNodo.executeQuery();
                if (rsNodo.next()) {
                    nodoId = rsNodo.getInt("id");
                } else {
                    System.out.println("No se encontró nodo en (" + input.getPosX() + "," + input.getPosY() + ")");
                    return false;
                }
            }

            // Insertar pedido
            String sqlPedido = "INSERT INTO prueba_camiones_diario.Pedido (destino_id, cantidadGlp, horaPedido, plazoMaximoEntrega, tiempoDescarga, entregado, idCliente, id) "
                    +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            String id = "2000" + String.format("%04d", new Random().nextInt(10000));

            try (PreparedStatement ps = conn.prepareStatement(sqlPedido)) {
                LocalDateTime horaPedido = input.getHoraPedido();
                LocalDateTime plazoMax = input.getPlazoMaximoEntrega();
                LocalDateTime tiempoDescarga = plazoMax.plusMinutes(15);

                ps.setInt(1, nodoId);
                ps.setDouble(2, input.getCantidadGlp());
                ps.setTimestamp(3, Timestamp.valueOf(horaPedido));
                ps.setTimestamp(4, Timestamp.valueOf(plazoMax));
                ps.setTimestamp(5, Timestamp.valueOf(tiempoDescarga));
                ps.setBoolean(6, false);
                ps.setString(7, input.getIdCliente());
                ps.setString(8, id);

                int rows = ps.executeUpdate();
                return rows > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarEstadoEntregadoPositivoDiaDia(String idPedido, LocalDateTime tiempoEntrega) {
        String sql = "UPDATE prueba_camiones_diario.Pedido SET entregado = ?, hora_entrega = ? WHERE id = ?";

        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, true);
            ps.setDate(2, java.sql.Date.valueOf(tiempoEntrega.toLocalDate()));
            ps.setString(3, idPedido);

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar estado 'entregado' del pedido con id: " + idPedido, e);
        }
    }

    public boolean actualizarTodosPedidosANoEntregadosDiaDia() {
        String sql = "UPDATE prueba_camiones_diario.Pedido SET entregado=0 WHERE entregado=1";

        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar la ubicación del camión", e);
        }
    }

    public void actualizarPedidosJson(List<Pedido> pedidos) {
        String sql = "CALL prueba_camiones_exper.actualizar_pedidos_json(?)";

        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ObjectMapper objectMapper = new ObjectMapper();

            // Mapear destino.getId() como destinoId en el JSON
            List<Map<String, Object>> pedidosMap = pedidos.stream().map(pedido -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", pedido.getId());
                map.put("cantidadGlp", pedido.getCantidadGlp());
                map.put("horaPedido", pedido.getHoraPedido());
                map.put("plazoMaximoEntrega", pedido.getPlazoMaximoEntrega());
                map.put("tiempoDescarga", pedido.getTiempoDescarga());
                map.put("entregado", pedido.isEntregado());
                return map;
            }).toList();

            String json = objectMapper.writeValueAsString(pedidosMap);
            ps.setString(1, json);
            ps.execute();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar pedidos por JSON", e);
        }
    }

    public ArrayList<Pedido> obtenerPedidosAnteriores(LocalDateTime fechaInput, LocalDateTime fin) {
        System.out.println("WAAAAAAAAAAAAA  FECHA INPUT: " + fechaInput + " FECHA FINAL: " + fin);
        ArrayList<Pedido> pedidos = new ArrayList<>();

        String sql = "SELECT ped.id, ped.destino_id, ped.cantidadGlp, ped.horaPedido, ped.plazoMaximoEntrega, ped.tiempoDescarga, ped.entregado, ped.idCliente, n.id as NodoID,n.* FROM Pedido ped INNER JOIN prueba_camiones_exper.Nodo n ON ped.destino_id = n.id WHERE horaPedido <= ? AND horaPedido >= ? AND entregado = 0";

        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(fin));
            ps.setTimestamp(2, Timestamp.valueOf(fechaInput));

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                // NodoService serviceNodo = new NodoService();
                // int id_destino = rs.getInt("destino_id");
                // Nodo destino = serviceNodo.getNodoPorId(id_destino); // Asegúrate que este
                // método no sea estático o usa
                // una instancia
                Nodo destino = new Nodo();
                destino.setId(rs.getString("NodoID"));
                destino.setPosX(rs.getInt("posX"));
                destino.setPosY(rs.getInt("posY"));
                destino.setBloqueado(rs.getBoolean("bloqueado"));

                Pedido pedido = new Pedido();
                pedido.setId(String.valueOf(rs.getInt("id")));
                pedido.setDestino(destino);
                pedido.setCantidadGlp(rs.getDouble("cantidadGlp"));
            
                pedido.setHoraPedido(rs.getTimestamp("horaPedido").toLocalDateTime());
                pedido.setPlazoMaximoEntrega(rs.getTimestamp("plazoMaximoEntrega").toLocalDateTime());
                pedido.setTiempoDescarga(rs.getTimestamp("tiempoDescarga").toLocalDateTime());
                pedido.setEntregado(rs.getBoolean("entregado"));
                pedido.setIdCliente(rs.getString("idCliente"));

                pedidos.add(pedido);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return pedidos;
    }
    public ArrayList<Pedido> obtenerPedidosAnteriores2(LocalDateTime fechaInput, LocalDateTime fin) {
        System.out.println("WAAAAAAAAAAAAA  FECHA INPUT: " + fechaInput + " FECHA FINAL: " + fin);
        ArrayList<Pedido> pedidos = new ArrayList<>();

        String sql = "SELECT ped.id, ped.destino_id, ped.cantidadGlp, ped.horaPedido, ped.plazoMaximoEntrega, ped.tiempoDescarga, ped.entregado, n.id as NodoID,n.* FROM Pedido ped INNER JOIN prueba_camiones_exper.Nodo n ON ped.destino_id = n.id WHERE horaPedido <= ? AND horaPedido >= ?";

        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(fin));
            ps.setTimestamp(2, Timestamp.valueOf(fechaInput));

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                // NodoService serviceNodo = new NodoService();
                // int id_destino = rs.getInt("destino_id");
                // Nodo destino = serviceNodo.getNodoPorId(id_destino); // Asegúrate que este
                // método no sea estático o usa
                // una instancia
                Nodo destino = new Nodo();
                destino.setId(rs.getString("NodoID"));
                destino.setPosX(rs.getInt("posX"));
                destino.setPosY(rs.getInt("posY"));
                destino.setBloqueado(rs.getBoolean("bloqueado"));

                Pedido pedido = new Pedido();
                pedido.setId(String.valueOf(rs.getInt("id")));
                pedido.setDestino(destino);
                pedido.setCantidadGlp(rs.getDouble("cantidadGlp"));
                pedido.setHoraPedido(rs.getTimestamp("horaPedido").toLocalDateTime());
                pedido.setPlazoMaximoEntrega(rs.getTimestamp("plazoMaximoEntrega").toLocalDateTime());
                pedido.setTiempoDescarga(rs.getTimestamp("tiempoDescarga").toLocalDateTime());
                pedido.setEntregado(rs.getBoolean("entregado"));

                pedidos.add(pedido);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return pedidos;
    }

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

        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPedido);
            ps.setString(2, codigo);
            ps.setDouble(3, cantidadGlp);
            ps.setTimestamp(4, java.sql.Timestamp.valueOf(horaPedido));
            ps.setTimestamp(5, java.sql.Timestamp.valueOf(plazoMaximoEntrega));
            ps.setTimestamp(6, java.sql.Timestamp.valueOf(tiempoDescarga));
            ps.setString(7, codCliente);
            ps.setInt(8, destinoNodoId);

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

    public ArrayList<Pedido> obtenerPedidosEntreTiempos(LocalDateTime inicio, LocalDateTime fin) {
        ArrayList<Pedido> pedidos = new ArrayList<>();

        String sql = "SELECT ped.id, ped.destino_id, ped.cantidadGlp, ped.horaPedido, ped.plazoMaximoEntrega, ped.tiempoDescarga, ped.entregado, n.id as NodoID,n.* FROM Pedido ped INNER JOIN prueba_camiones_exper.Nodo n ON ped.destino_id = n.id WHERE horaPedido BETWEEN ? AND ?";

        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(inicio));
            ps.setTimestamp(2, Timestamp.valueOf(fin));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                // NodoService serviceNodo = new NodoService();
                // int id_destino = rs.getInt("destino_id");
                // Nodo destino = serviceNodo.getNodoPorId(id_destino); // Asegúrate que este
                // método no sea estático o usa
                // una instancia
                Nodo destino = new Nodo();
                destino.setPosX(rs.getInt("posX"));
                destino.setPosY(rs.getInt("posY"));
                destino.setBloqueado(rs.getBoolean("bloqueado"));

                Pedido pedido = new Pedido();
                pedido.setId(String.valueOf(rs.getInt("id")));
                pedido.setDestino(destino);
                pedido.setCantidadGlp(rs.getDouble("cantidadGlp"));
                pedido.setHoraPedido(rs.getTimestamp("horaPedido").toLocalDateTime());
                pedido.setPlazoMaximoEntrega(rs.getTimestamp("plazoMaximoEntrega").toLocalDateTime());
                pedido.setTiempoDescarga(rs.getTimestamp("tiempoDescarga").toLocalDateTime());
                pedido.setEntregado(rs.getBoolean("entregado"));

                pedidos.add(pedido);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return pedidos;
    }

    public boolean actualizarEstadoEntregadoPositivo(String idPedido, LocalDateTime tiempoEntrega) {
        String sql = "UPDATE Pedido SET entregado = ?, hora_entrega = ? WHERE id = ?";

        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, true);
            ps.setDate(2, java.sql.Date.valueOf(tiempoEntrega.toLocalDate()));
            ps.setString(3, idPedido);

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar estado 'entregado' del pedido con id: " + idPedido, e);
        }
    }

    public boolean actualizarEstadoEntregadoNegativo(String idPedido) {
        String sql = "UPDATE Pedido SET entregado = ? WHERE id = ?";

        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, false);
            ps.setString(2, idPedido);

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar estado 'entregado' del pedido con id: " + idPedido, e);
        }
    }
}
