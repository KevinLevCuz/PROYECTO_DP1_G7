package com.dp1code.routing.Service;

import com.dp1code.routing.dto.PedidoArchivoDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Service
public class PedidoArchivoService {

    public void procesarArchivoPedidos(MultipartFile archivo) throws Exception {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(archivo.getInputStream(), StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;

                PedidoArchivoDTO dto = parseLinea(linea);
                insertarPedido(dto);
            }
        }
    }

    private PedidoArchivoDTO parseLinea(String linea) {
        String[] partes = linea.split(":");
        String tiempoStr = partes[0];
        String[] datos = partes[1].split(",");

        int posX = Integer.parseInt(datos[0]);
        int posY = Integer.parseInt(datos[1]);
        String idCliente = datos[2];
        double cantidadGlp = Double.parseDouble(datos[3].replace("m3", ""));
        int plazoHoras = Integer.parseInt(datos[4].replace("h", ""));

        PedidoArchivoDTO dto = new PedidoArchivoDTO();
        dto.setTiempoSimulacion(tiempoStr);
        dto.setPosX(posX);
        dto.setPosY(posY);
        dto.setIdCliente(idCliente);
        dto.setCantidadGlp(cantidadGlp);
        dto.setPlazoHoras(plazoHoras);

        return dto;
    }

    private void insertarPedido(PedidoArchivoDTO dto) {
        String sql = "INSERT INTO Pedido (id, destino_id, cantidadGlp, horaPedido, plazoMaximoEntrega, tiempoDescarga, entregado, idCliente) " +
                "VALUES (?, ?, ?, ?, ?, ? ,?, ?)";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int nuevoId = obtenerSiguienteId();
            ps.setInt(1, nuevoId);

            // 1️⃣ destino_id según posX,posY
            ps.setInt(2, calcularDestinoId(dto.getPosX(), dto.getPosY()));

            // 2️⃣ cantidadGLP
            ps.setDouble(3, dto.getCantidadGlp());

            // 3️⃣ horaPedido convertido desde "01d00h24m"
            String cadenaTiempo = dto.getTiempoSimulacion();
            Timestamp horaPedido = convertirAFecha(cadenaTiempo);
            ps.setTimestamp(4, horaPedido);

            // 4️⃣ plazoMaximoEntrega en horas
            LocalDateTime plazo = horaPedido.toLocalDateTime().plusHours(dto.getPlazoHoras());
            ps.setTimestamp(5, Timestamp.valueOf(plazo));

            LocalDateTime tiempoDescarga = plazo.plusMinutes(15);
            ps.setTimestamp(6, Timestamp.valueOf(tiempoDescarga));

            ps.setBoolean(7, false);

            // 5️⃣ idCliente
            ps.setString(8, dto.getIdCliente());

            int filas = ps.executeUpdate();
            if (filas == 1) {
                System.out.println("✅ Pedido insertado desde archivo: " + dto);
            } else {
                System.err.println("⚠️ No se insertó el pedido: " + dto);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al insertar pedido desde archivo:");
            e.printStackTrace();
        }
    }

    private int calcularDestinoId(int posX, int posY) {
        String sql = "SELECT id FROM Nodo WHERE posX = ? AND posY = ? LIMIT 1";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, posX);
            ps.setInt(2, posY);

            var rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                System.err.println("⚠️ No se encontró Nodo para posX=" + posX + ", posY=" + posY);
                return -1;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al obtener destino_id", e);
        }
    }

    private Timestamp convertirAFecha(String tiempo) {
        // Ejemplo: "01d00h24m"
        int dia = Integer.parseInt(tiempo.substring(0, 2));
        int hora = Integer.parseInt(tiempo.substring(3, 5));
        int minuto = Integer.parseInt(tiempo.substring(6, 8));

        // Año y mes fijos según tu lógica
        LocalDateTime ldt = LocalDateTime.of(2026, 7, dia, hora, minuto, 0);
        return Timestamp.valueOf(ldt);
    }

    private int obtenerSiguienteId() {
        String sql = "SELECT MAX(CAST(id AS UNSIGNED))+1 AS siguiente FROM Pedido";
        try (Connection conn = DatabaseService.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            var rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("siguiente");
            }
            return 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener siguiente ID", e);
        }
    }

}
