package com.dp1code.routing.Controller;

import com.dp1code.routing.Model.Solucion;
import com.dp1code.routing.Model.SubRuta;
import com.dp1code.routing.Model.Pedido;
import com.dp1code.routing.Model.PlanCamion;
import com.dp1code.routing.Model.Camion;
import com.dp1code.routing.Model.Bloqueo;
import com.dp1code.routing.Model.Planta;
import com.dp1code.routing.Model.Simulacion;
import com.dp1code.routing.Service.PedidoService;
import com.dp1code.routing.Service.PlantaService;
import com.dp1code.routing.Service.RoutingService;

// Los imports de Spring Web:
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = {
        "https://h982equipo7g.duckdns.org",
        "http://localhost:3000"
})

@RestController
@RequestMapping("/api/routing")
public class RoutingController {

    private final RoutingService routingService;
    private final SimpMessagingTemplate messagingTemplate;
    private boolean simulacionActiva = false;

    public RoutingController(RoutingService routingService, SimpMessagingTemplate messagingTemplate) {
        this.routingService = routingService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/IniciarSimulacionSemanal")
    public void iniciarSimulacion(Map<String, String> payload) throws IOException {
        String inicioStr = payload.get("ahora") + "Z"; // usamos el "ahora" inicial
        Instant inicio = Instant.parse(inicioStr);
        LocalDateTime fechaInicio = LocalDateTime.ofInstant(inicio, ZoneOffset.UTC).minusHours(5);
        routingService.iniciarSimulacionConWebSocket(fechaInicio, messagingTemplate);
    }

    @MessageMapping("/iniciarSimulacionDiaDia")
    public void iniciarSimulacionDiaDia(Map<String, String> payload) throws IOException {

        synchronized (this) {
            if (!simulacionActiva) {
                simulacionActiva = true;
                try {
                    System.out.println("🔄 Iniciando simulación día a día...");
                    String inicioStr = payload.get("ahora") + "Z";
                    Instant inicio = Instant.parse(inicioStr);
                    LocalDateTime fechaInicio = LocalDateTime.ofInstant(inicio, ZoneOffset.UTC).minusHours(5);
                    routingService.iniciarSimulacionConWebSocketDiaDia(fechaInicio, messagingTemplate);
                } catch (Exception e) {
                    simulacionActiva = false;
                    throw e; // o loguea el error
                }
            } else {
                System.out.println("⚠️ Simulación ya está activa, no se puede iniciar de nuevo.");
            }
        }

    }

    // @RequestBody OptimizeRequest request, LocalDateTime ahora =
    // LocalDateTime.parse(request.getAhora());
    @PostMapping("/simulacionSemanal")
    public Solucion simulacionSemanal(@RequestBody Map<String, String> payload) throws IOException {
        String ahoraStr = payload.get("ahora");
        String fechaVariableStr = payload.get("fechaVariable");

        LocalDateTime ahoraLocal = LocalDateTime.parse(ahoraStr);
        LocalDateTime fechaVariableLocal = LocalDateTime.parse(fechaVariableStr);

        System.out.println("Esta ingresando con la hora local de: " + ahoraLocal);

        Solucion solucion = routingService.simulacionSemanal(ahoraLocal, fechaVariableLocal);

        return solucion;
    }

    @PostMapping("/monitoreoDiario")
    public Solucion monitoreoDiario(@RequestBody Map<String, String> payload) throws IOException {
        String ahoraStr = payload.get("ahora") + "Z";
        String fechaVariableStr = payload.get("fechaVariable") + "Z";
        int cont = Integer.parseInt(payload.get("cont"));

        Instant ahora = Instant.parse(ahoraStr);
        Instant fechaVariable = Instant.parse(fechaVariableStr);
        LocalDateTime ahoraLocal = LocalDateTime.ofInstant(ahora, ZoneOffset.UTC).minusHours(5);
        LocalDateTime fechaVariableLocal = LocalDateTime.ofInstant(fechaVariable, ZoneOffset.UTC).minusHours(5);

        System.out.println("Esta ingresando con la hora local de: " + ahoraLocal);

        Solucion solucion = routingService.obtenerDiaDia(ahoraLocal, fechaVariableLocal, cont);

        /*
         * System.out.println("Se retorna: ");
         * for(Solucion s : simulacion.getSoluciones()) {
         * for(PlanCamion p: s.getPlanesCamion()) {
         * System.out.println("El camion: "+p.getCamion().getCodigo() +
         * " y el size de subRutas es: "+p.getSubRutas().size());
         * for(SubRuta sub: p.getSubRutas()) {
         * System.out.println(sub.getTrayectoria().get(0).getPosX()+", "+sub.
         * getTrayectoria().get(0).getPosY() +
         * " Fin: "+sub.getTrayectoria().get(sub.getTrayectoria().size()-1).getPosX()
         * +", "+sub.getTrayectoria().get(sub.getTrayectoria().size()-1).getPosY());
         * }
         * }
         * }
         */
        return solucion;
    }

    /*
     * @PostMapping("/optimize")
     * public Solucion optimize() throws IOException {
     * LocalDateTime ahora = LocalDateTime.now()
     * .withDayOfMonth(25)
     * .withHour(12)
     * .withMinute(53)
     * .withSecond(20)
     * .withNano(0);
     * return routingService.optimize(ahora);
     * }
     */

    @PostMapping("/obtenerPedidos")
    public ArrayList<Pedido> obtenerPedidos() throws IOException {
        LocalDateTime now = LocalDateTime.now()
                .withDayOfMonth(18)
                .withHour(12)
                .withMinute(53)
                .withSecond(20)
                .withNano(0);
        return routingService.cargarPedidosSegmentado("data/pedidos.txt", now);
    }

    @PostMapping("/obtenerPlantas")
    public ArrayList<Planta> obtenerPlantas() throws IOException {
        PlantaService plantaService = new PlantaService();
        return plantaService.obtenerTodas();
    }

    @PostMapping("/obtenerCamiones")
    public ArrayList<Camion> obtenerCamiones() throws IOException {
        LocalDateTime ahora = LocalDateTime.now()
                .withDayOfMonth(25)
                .withHour(12)
                .withMinute(53)
                .withSecond(20)
                .withNano(0);
        return routingService.cargarCamiones("data/camiones.txt", ahora);
    }

    @PostMapping("/obtenerBloqueos")
    public ArrayList<Bloqueo> obtenerBloqueos() throws IOException {
        return routingService.obtenerBloqueosTotales();
    }

    // Registrar un pedido:

    // DTO para recibir el POST
    public static class OptimizeRequest {
        private List<Pedido> pedidos;
        private List<Camion> camiones;
        private String ahora;

        // Jackson necesita el constructor vacío:
        public OptimizeRequest() {
        }

        // getters y setters:
        public List<Pedido> getPedidos() {
            return pedidos;
        }

        public void setPedidos(List<Pedido> pedidos) {
            this.pedidos = pedidos;
        }

        public List<Camion> getCamiones() {
            return camiones;
        }

        public void setCamiones(List<Camion> camiones) {
            this.camiones = camiones;
        }

        public String getAhora() {
            return ahora;
        }

        public void setAhora(String ahora) {
            this.ahora = ahora;
        }
    }
}
