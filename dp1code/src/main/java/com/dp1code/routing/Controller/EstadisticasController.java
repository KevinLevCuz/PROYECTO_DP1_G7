package com.dp1code.routing.Controller;

import com.dp1code.routing.Service.EstadisticasService;
import com.dp1code.routing.dto.CamionStatsDTO;
import com.dp1code.routing.dto.PedidoStatsDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.sql.SQLException;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
@CrossOrigin(origins = {
    "https://h982equipo7g.duckdns.org",
    "http://localhost:3000"
})


@RestController
@RequestMapping("/api/estadisticas")
public class EstadisticasController {

    @Autowired
    private EstadisticasService estadisticasService;

    @GetMapping("/camiones")
    public ResponseEntity<List<CamionStatsDTO>> obtenerCamiones() {
        try {
            return ResponseEntity.ok(estadisticasService.obtenerEstadisticasCamiones());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/camionesDiaDia")
    public ResponseEntity<List<CamionStatsDTO>> obtenerCamionesDiaDia() {
        try {
            return ResponseEntity.ok(estadisticasService.obtenerEstadisticasCamionesDiaDia());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/pedidos")
    public ResponseEntity<List<PedidoStatsDTO>> obtenerPedidos() {
        try {
            return ResponseEntity.ok(estadisticasService.obtenerEstadisticasPedidos());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/pedidosDiaDia")
    public ResponseEntity<List<PedidoStatsDTO>> obtenerPedidosDiaDia() {
        try {
            return ResponseEntity.ok(estadisticasService.obtenerEstadisticasPedidosDiaDia());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
