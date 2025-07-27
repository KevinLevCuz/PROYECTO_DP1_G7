package com.dp1code.routing.Controller;

import com.dp1code.routing.dto.CamionDTO;
import com.dp1code.routing.dto.CamionResumenDTO;
import com.dp1code.routing.Model.Camion;
import com.dp1code.routing.Service.CamionResumenService;
import com.dp1code.routing.Service.CamionService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@CrossOrigin(origins = {
    "https://h982equipo7g.duckdns.org",
    "http://localhost:3000"
})
@RestController
@RequestMapping("/api/camiones")
public class CamionController {

    @Autowired
    private CamionService camionService;

    @PostMapping("/registrar")
    public String registrar(@RequestBody CamionDTO camionDTO) {
        camionService.registrarCamion(camionDTO);
        return "Camión registrado correctamente.";
    }

    @GetMapping("/resumen")
    public ResponseEntity<List<CamionResumenDTO>> obtenerResumenCamiones() {
        CamionResumenService camionResumenService = new CamionResumenService();
        List<CamionResumenDTO> resumen = camionResumenService.obtenerResumenCamiones();

        if (resumen.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204 No Content si está vacío
        }

        return ResponseEntity.ok(resumen); // 200 OK con la lista
    }

    @GetMapping("/obtenerCamiones")
    public List<Camion> obtenerCamiones() {
        return camionService.obtenerTodosLosCamiones();
    }

    @PutMapping("/actualizarEnrutaPositivo/{codigoCamion}")
    public ResponseEntity<String> actualizarEnRutaPositivo(
            @PathVariable String codigoCamion) {

        boolean actualizado = camionService.actualizarEstadoEnRutaPosotivo(codigoCamion);
        if (actualizado) {
            return ResponseEntity.ok("Estado 'enRuta' actualizado correctamente.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Camión no encontrado.");
        }
    }

    @PutMapping("/actualizarEnrutaNegativo/{codigoCamion}")
    public ResponseEntity<String> actualizarEnrutaNegativo(
            @PathVariable String codigoCamion) {

        boolean actualizado = camionService.actualizarEstadoEnRutaNegativo(codigoCamion);
        if (actualizado) {
            return ResponseEntity.ok("Estado 'enRuta' actualizado correctamente.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Camión no encontrado.");
        }
    }
}
