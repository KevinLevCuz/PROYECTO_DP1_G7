package com.dp1code.routing.Controller;

import com.dp1code.routing.dto.CamionResumenDTO;
import com.dp1code.routing.Service.CamionResumenService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {
    "https://h982equipo7g.duckdns.org",
    "http://localhost:3000"
})
@RestController
@RequestMapping("/api/camionesR")
public class CamionResumenController {

    @Autowired
    private CamionResumenService camionResumenService;

    @GetMapping("/resumen")
    public ResponseEntity<List<CamionResumenDTO>> obtenerResumenCamiones() {
        List<CamionResumenDTO> resumen = camionResumenService.obtenerResumenCamiones();

        if (resumen.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204 No Content si está vacío
        }

        return ResponseEntity.ok(resumen); // 200 OK con la lista
    }
}
