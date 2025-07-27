package com.dp1code.routing.Controller;

import com.dp1code.routing.Service.PedidoResumenService;
import com.dp1code.routing.dto.PedidoResumenDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {
    "https://h982equipo7g.duckdns.org",
    "http://localhost:3000"
})
@RestController
@RequestMapping("/api/estadisticasR")
public class PedidoResumenController {

    @Autowired
    private PedidoResumenService pedidoResumenService;

    @GetMapping("/resumenPedidos")
    public List<PedidoResumenDTO> obtenerResumenPedidos(
        @RequestParam("fechaInicio") String fechaInicio,
        @RequestParam("fechaFin") String fechaFin
    ) {
        return pedidoResumenService.obtenerResumenPedidos(fechaInicio, fechaFin);
    }
}
