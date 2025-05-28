package com.dp1code.routing.Controller;

import com.dp1code.routing.Model.Solucion;
import com.dp1code.routing.Model.Pedido;
import com.dp1code.routing.Model.Camion;
import com.dp1code.routing.Service.RoutingService;

// Los imports de Spring Web:
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/routing")
public class RoutingController {

    private final RoutingService routingService;

    public RoutingController(RoutingService routingService) {
        this.routingService = routingService;
    }

    @PostMapping("/optimize")
    public Solucion optimize(
        @RequestBody OptimizeRequest req
    ) {
        LocalDateTime now = LocalDateTime.parse(req.getAhora());
        return routingService.optimize();//chequea esto
    }

    // DTO para recibir el POST
    public static class OptimizeRequest {
        private List<Pedido> pedidos;
        private List<Camion> camiones;
        private String ahora;

        // Jackson necesita el constructor vacío:
        public OptimizeRequest() {}

        // getters y setters:
        public List<Pedido> getPedidos() { return pedidos; }
        public void setPedidos(List<Pedido> pedidos) { this.pedidos = pedidos; }

        public List<Camion> getCamiones() { return camiones; }
        public void setCamiones(List<Camion> camiones) { this.camiones = camiones; }

        public String getAhora() { return ahora; }
        public void setAhora(String ahora) { this.ahora = ahora; }
    }
}
