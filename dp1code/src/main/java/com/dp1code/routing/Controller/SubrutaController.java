package com.dp1code.routing.Controller;

import com.dp1code.routing.dto.SubrutaDTO;
import com.dp1code.routing.Service.SubrutaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
@CrossOrigin(origins = {
    "https://h982equipo7g.duckdns.org",
    "http://localhost:3000"
})
@RestController
@RequestMapping("/api/subrutas")
public class SubrutaController {

    @Autowired
    private SubrutaService subRutaService;

    @PostMapping("/registrar")
    public String registrar(@RequestBody SubrutaDTO dto) {
        subRutaService.registrarSubRuta(dto);
        return "SubRuta registrada correctamente.";
    }
}
