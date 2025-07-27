package com.dp1code.routing.Controller;

import com.dp1code.routing.dto.BloqueoDTO;
import com.dp1code.routing.Service.BloqueoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
@CrossOrigin(origins = {
    "https://h982equipo7g.duckdns.org",
    "http://localhost:3000"
})

@RestController
@RequestMapping("/api/bloqueos")
public class BloqueoController {

    @Autowired
    private BloqueoService bloqueoService;

    @PostMapping("/registrar")
    public String registrar(@RequestBody BloqueoDTO bloqueo) {
        bloqueoService.registrarBloqueo(bloqueo);
        return "Bloqueo registrado correctamente.";
    }
}
