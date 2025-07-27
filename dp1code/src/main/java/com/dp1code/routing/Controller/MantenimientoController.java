package com.dp1code.routing.Controller;

import com.dp1code.routing.Model.Mantenimiento;
import com.dp1code.routing.Model.TimeRange;
import com.dp1code.routing.Service.MantenimientoService;
import com.dp1code.routing.dto.MantenimientoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.CrossOrigin;
@CrossOrigin(origins = {
    "https://h982equipo7g.duckdns.org",
    "http://localhost:3000"
})
@RestController
@RequestMapping("/api/mantenimientos")
public class MantenimientoController {

    @Autowired
    private MantenimientoService mantenimientoService;

    @PostMapping("/registrar")
    public ResponseEntity<String> registrar(@RequestBody MantenimientoDTO dto) {
        Mantenimiento mantenimiento = new Mantenimiento(
                dto.getInicio(),
                dto.getFin(),
                dto.getCodigoCamion(),
                dto.getTipo());

        mantenimientoService.registrarMantenimiento(mantenimiento);
        return ResponseEntity.ok("Mantenimiento registrado correctamente.");
    }

    @GetMapping("/listar")
    public ResponseEntity<List<MantenimientoDTO>> listar() {
        List<MantenimientoDTO> resultado = mantenimientoService.listarMantenimientos()
                .stream()
                .map(m -> new MantenimientoDTO(
                        m.getInicio(),
                        m.getFin(),
                        m.getCodigoCamion(),
                        m.getTipo()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/rangos")
    public ResponseEntity<List<TimeRange>> obtenerRangos(@RequestParam String codigoCamion) {
        List<TimeRange> rangos = mantenimientoService.obtenerRangosPorCodigoCamion(codigoCamion);
        return ResponseEntity.ok(rangos);
    }

}
