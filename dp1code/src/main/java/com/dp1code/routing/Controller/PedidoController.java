package com.dp1code.routing.Controller;

import com.dp1code.routing.dto.PedidoDTO;
import com.dp1code.routing.dto.PedidoResumenDTO;
import com.dp1code.routing.Model.Pedido;
import com.dp1code.routing.Service.PedidoResumenService;
import com.dp1code.routing.Service.PedidoService;
import com.dp1code.routing.Service.PedidoArchivoService;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = {
        "https://h982equipo7g.duckdns.org",
        "http://localhost:3000"
})
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    //@Autowired
    private PedidoService pedidoService;
    private PedidoArchivoService pedidoArchivoService;

    //@Autowired
    public PedidoController(PedidoArchivoService pedidoArchivoService) {
        this.pedidoArchivoService = pedidoArchivoService;
    }

    @PostMapping("/registrarPedido")
    public ResponseEntity<String> insertarPedido(@RequestBody PedidoDTO input) {
        PedidoService pedidoService = new PedidoService();
        boolean resultado = pedidoService.insertarPedido(input);

        if (resultado) {
            return ResponseEntity.ok("Pedido insertado correctamente");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al insertar pedido");
        }
    }

    @GetMapping("/resumenPedidos")
    public PedidoResumenDTO obtenerResumenPedidos(
            @RequestParam("fechaInicio") String fechaInicio,
            @RequestParam("fechaFin") String fechaFin) {
        PedidoResumenService pedidoResumenService = new PedidoResumenService();
        return pedidoResumenService.obtenerResumenPedidos(fechaInicio, fechaFin);
    }

    @GetMapping("/rango")
    public List<Pedido> obtenerPedidosEnRango() {
        // Fechas hardcodeadas para pruebas
        LocalDateTime inicio = LocalDateTime.of(2025, 7, 1, 0, 0);
        LocalDateTime fin = LocalDateTime.of(2025, 7, 1, 05, 59);

        return pedidoService.obtenerPedidosEntreTiempos(inicio, fin);
    }

    @PutMapping("/actualizarEntregadoPositivo/{idPedido}/{estado}")
    public ResponseEntity<String> actualizarEntregadoPositivo(
            @PathVariable String idPedido,
            @PathVariable boolean estado, @PathVariable LocalDateTime tiempoEntrega) {

        boolean actualizado = pedidoService.actualizarEstadoEntregadoPositivo(idPedido, tiempoEntrega);
        if (actualizado) {
            return ResponseEntity.ok("Estado 'entregado' actualizado correctamente.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Pedido no encontrado.");
        }
    }

    @PutMapping("/actualizarEntregadoNegativo/{idPedido}/{estado}")
    public ResponseEntity<String> actualizarEntregadoNegativo(
            @PathVariable String idPedido,
            @PathVariable boolean estado) {

        boolean actualizado = pedidoService.actualizarEstadoEntregadoNegativo(idPedido);
        if (actualizado) {
            return ResponseEntity.ok("Estado 'entregado' actualizado correctamente.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Pedido no encontrado.");
        }
    }

    /*
     * @PostMapping("/cargarArchivo")
     * public ResponseEntity<String> cargarArchivo(@RequestParam("archivo")
     * MultipartFile archivo) {
     * try {
     * if (archivo.isEmpty()) {
     * return ResponseEntity.badRequest().body("El archivo está vacío.");
     * }
     * 
     * pedidoArchivoService.procesarArchivoPedidos(archivo);
     * return ResponseEntity.ok("Archivo procesado e insertado correctamente.");
     * 
     * } catch (Exception e) {
     * e.printStackTrace();
     * return ResponseEntity.status(500).body("Error procesando el archivo: " +
     * e.getMessage());
     * }
     * }
     */

    @PostMapping("/cargarArchivo")
    public ResponseEntity<String> cargarArchivo(@RequestParam("archivo") MultipartFile archivo) {
        try {
            if (archivo == null) {
                return ResponseEntity.badRequest().body("No se recibió ningún archivo");
            }

            if (archivo.isEmpty()) {
                return ResponseEntity.badRequest().body("El archivo está vacío.");
            }

            // Log para diagnóstico
            System.out.println("Recibido archivo: " + archivo.getOriginalFilename() +
                    ", tamaño: " + archivo.getSize() + " bytes");

            pedidoArchivoService.procesarArchivoPedidos(archivo);
            return ResponseEntity.ok("Archivo procesado correctamente");

        } catch (Exception e) {
            // Log más detallado
            System.err.println("ERROR al procesar archivo:");
            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body("Error procesando el archivo: " +
                            e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
