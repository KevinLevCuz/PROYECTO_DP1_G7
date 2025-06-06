package com.dp1code.routing;

//import java.io.BufferedReader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import com.dp1code.routing.Model.Pedido;
import com.dp1code.routing.Model.Camion;
import com.dp1code.routing.Model.Bloqueo;
import com.dp1code.routing.Model.Mantenimiento;
import com.dp1code.routing.Model.Planta;
import com.dp1code.routing.Model.SimulatedAnnealing;
import com.dp1code.routing.Model.Solucion;
import com.dp1code.routing.Model.Nodo;
import com.dp1code.routing.Model.PlanCamion;
import com.dp1code.routing.Model.SubRuta;


@SpringBootApplication
public class RoutingApplication {
    // private static Nodo ubicacionInicial = new Nodo(0,0);
    public static void main(String[] args) throws IOException {
        SpringApplication.run(RoutingApplication.class, args);
        
        LocalDateTime ahora = LocalDateTime.now()
                .withDayOfMonth(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        ArrayList<Pedido> pedidos = cargarPedidos("data/pedidos.txt");
        ArrayList<Camion> camiones = cargarCamiones("data/camiones.txt");
        ArrayList<Bloqueo> bloqueos = cargarBloqueos("data/bloqueos.txt");
        ArrayList<Mantenimiento> mantenimientos = cargarMantenimientos("data/mantenimiento.txt");

        ArrayList<Planta> plantas = obtenerPlantas();

        SimulatedAnnealing sa = new SimulatedAnnealing(
                5000, 0.005, 100,
                plantas, bloqueos, mantenimientos);
        long t0 = System.nanoTime();
        Solucion mejor = sa.optimize(pedidos, camiones, plantas, bloqueos, mantenimientos, ahora);
        long t1 = System.nanoTime();

        double elapsedSec = (t1 - t0) / 1e9;
        
        System.out.printf("Optimize() tardó %.3f segundos%n", elapsedSec);

        double costeTotal = sa.cost(mejor);
        System.out.printf("Función objetivo (coste total): %.3f%n", costeTotal);

        int totalPedidos = 0;
        for (PlanCamion plan : mejor.getPlanesCamion()) {
            for (SubRuta sr : plan.getSubRutas()) {
                if (sr.getPedido() != null)
                    totalPedidos++;
            }
        }

        // 3A) Fitness promedio = coste medio por pedido
        double costeMedio = (totalPedidos > 0 ? costeTotal / totalPedidos : 0);
        System.out.printf("Coste medio por pedido: %.3f%n", costeMedio);

        double sumaFitness = 0;
        for (PlanCamion plan : mejor.getPlanesCamion()) {
            for (SubRuta sr : plan.getSubRutas()) {
                if (sr.getPedido() != null) {
                    // Calcula sólo el coste de esa subruta:
                    double cPedido = 0;
                    // (reutilizas tu lógica de cost() pero solo para este sr…
                    // distancia+consumo+penalizaciones…)
                    // por simplicidad: aproximamos cPedido = (costeTotal/totalPedidos)
                    cPedido = costeTotal / totalPedidos;
                    // fitness individual:
                    sumaFitness += 1.0 / (1.0 + cPedido);
                }
            }
        }
        double fitnessMedio = (totalPedidos > 0 ? sumaFitness / totalPedidos : 0);
        System.out.printf("Fitness promedio por pedido: %.3f%n%n%n%n", fitnessMedio);

        

        //System.out.println("Se llegó a dar solución\n");
        //mejor.imprimirRutas();
    }

    public static ArrayList<Planta> obtenerPlantas() throws IOException {
        ArrayList<Planta> plantas = new ArrayList<>();

        Planta plantaPrincipal = new Planta(1,"PRINCIPAL", new Nodo(12, 8));
        Planta plantaSecundaria1 = new Planta(2,"SECUNDARIA", new Nodo(42, 42));
        Planta plantaSecundaria2 = new Planta(3,"SECUNDARIA", new Nodo(63, 3));

        plantas.add(plantaPrincipal);
        plantas.add(plantaSecundaria1);
        plantas.add(plantaSecundaria2);
        return plantas;
    }

    public static ArrayList<Pedido> cargarPedidos(String filePath) throws IOException {
        ArrayList<Pedido> pedidos = new ArrayList<>();
        Path path = Paths.get(filePath);
        // Base del mes de simulación: primer día a las 00:00
        LocalDateTime base = LocalDateTime.now()
                .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        int i=0;
        for (String line : Files.readAllLines(path)) {
            i++;
            if (line.isBlank())
                continue;
            String[] parts = line.trim().split(":");
            // Tiempo de llegada
            String[] ts = parts[0].split("[dhm]");
            int d = Integer.parseInt(ts[0]);
            int h = Integer.parseInt(ts[1]);
            int m = Integer.parseInt(ts[2]);
            LocalDateTime horaPedido = base.plusDays(d).plusHours(h).plusMinutes(m);
            // Datos restantes
            String[] vals = parts[1].trim().split(",");
            int x = Integer.parseInt(vals[0]);
            int y = Integer.parseInt(vals[1]);
            String idCliente = vals[2].trim();
            int m3 = Integer.parseInt(vals[3].replace("m3", ""));
            int hLim = Integer.parseInt(vals[4].replace("h", ""));
            LocalDateTime plazoMax = horaPedido.plusHours(hLim);
            Pedido p = new Pedido(String.valueOf(i),new Nodo(x, y), idCliente, m3, horaPedido, plazoMax);
            pedidos.add(p);
        }
        return pedidos;
    }

    /**
     * Carga camiones desde archivo con formato:
     * TT,tara,capacidadGLP,_,_
     * Genera códigos TTNN según apariciones.
     */
    public static ArrayList<Camion> cargarCamiones(String filePath) throws IOException {
        ArrayList<Camion> camiones = new ArrayList<>();
        Map<String, Integer> count = new HashMap<>();
        Path path = Paths.get(filePath);
        LocalDateTime now = LocalDateTime.now();
        for (String line : Files.readAllLines(path)) {
            if (line.isBlank())
                continue;
            String[] parts = line.split(",");
            String tipo = parts[0];
            double capacidadGLP = Double.parseDouble(parts[2]);
            // Contador para código
            int idx = count.getOrDefault(tipo, 0) + 1;
            count.put(tipo, idx);
            String codigo = String.format("%s%02d", tipo, idx);
            // Ubicación por defecto (0,0)
            Nodo ubic = new Nodo(12, 8);
            Camion c = new Camion(codigo, ubic, capacidadGLP, capacidadGLP,
                    false, now);
            camiones.add(c);
        }
        return camiones;
    }

    /**
     * Carga bloqueos desde archivo con formato:
     * dd'd'HH'h'MM'm'-dd'd'HH'h'MM'm':x1,y1,x2,y2,...
     * Nombre de archivo: yyyyMM.bloqueadas para determinar año y mes.
     */
    public static ArrayList<Bloqueo> cargarBloqueos(String filePath) throws IOException {
        ArrayList<Bloqueo> bloqueos = new ArrayList<>();
        Path path = Paths.get(filePath);
        // Base temporal: primer día del mes actual a las 00:00
        LocalDateTime base = LocalDateTime.now()
                .withDayOfMonth(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        for (String line : Files.readAllLines(path)) {
            if (line.isBlank())
                continue;
            String[] parts = line.split(":");
            // Cada rango es "dd'd'HH'h'MM'm'-dd'd'HH'h'MM'm'"
            String[] span = parts[0].split("-");
            LocalDateTime start = parsearFechaHora(span[0]);
            LocalDateTime end = parsearFechaHora(span[1]);
            String[] coords = parts[1].split(",");
            ArrayList<Nodo> nodos = new ArrayList<>();
            for (int i = 0; i < coords.length; i += 2) {
                int x = Integer.parseInt(coords[i]);
                int y = Integer.parseInt(coords[i + 1]);
                nodos.add(new Nodo(x, y));
            }
            bloqueos.add(new Bloqueo(nodos, start, end));
        }
        return bloqueos;
    }

    private static LocalDateTime parseOffset(String ym, String offset) {
        int year = Integer.parseInt(ym.substring(0, 4));
        int month = Integer.parseInt(ym.substring(4, 6));
        // offset "dd'd'HH'h'MM'm'"
        String[] ts = offset.split("[dhm]");
        int d = Integer.parseInt(ts[0]);
        int h = Integer.parseInt(ts[1]);
        int m = Integer.parseInt(ts[2]);
        return LocalDateTime.of(year, month, d, h, m);
    }

    /**
     * Carga mantenimientos desde archivo con formato:
     * yyyyMMdd:TTNN
     * Ventana de 24h.
     */
    public static ArrayList<Mantenimiento> cargarMantenimientos(String filePath) throws IOException {
        ArrayList<Mantenimiento> list = new ArrayList<>();
        Path path = Paths.get(filePath);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd");
        for (String line : Files.readAllLines(path)) {
            if (line.isBlank())
                continue;
            String[] parts = line.split(":");
            LocalDate date = LocalDate.parse(parts[0], fmt);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = start.plusHours(24);
            String codigo = parts[1];
            Mantenimiento m = new Mantenimiento(start, end, codigo, "preventivo");
            list.add(m);
        }
        return list;
    }

    private static LocalDateTime parsearFechaHora(String texto) {
        texto = texto.trim();

        String[] partes = texto.split("[dhm]");
        if (partes.length < 3) {
            throw new IllegalArgumentException("Formato de fecha inválido: " + texto);
        }

        int dia = Integer.parseInt(partes[0]);
        int hora = Integer.parseInt(partes[1]);
        int minuto = Integer.parseInt(partes[2]);

        return LocalDateTime.of(2025, Month.MAY, dia, hora, minuto, 0, 0);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:3000")
                    .allowedMethods("*")
                    .allowCredentials(true);
            }
        };
    }


}