package com.dp1code.routing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dp1code.routing.Model.*;
import com.dp1code.routing.Service.*;;

@SpringBootApplication
public class RoutingApplication {
    static Grid grid = new Grid(71, 51);
    
    static int tiermpoSalto = 15;
    static ArrayList<Planta> plantas = new ArrayList<>();
    static ArrayList<Pedido> pedidos = new ArrayList<>();
    static ArrayList<Camion> camiones = new ArrayList<>();
    
    public static void main(String[] args) throws IOException {
        SpringApplication.run(RoutingApplication.class, args);
        LocalDateTime ahora = LocalDateTime.now()
                .withDayOfMonth(6)
                .withHour(12)
                .withMinute(13)
                .withSecond(0)
                .withNano(0);
/* 
        RoutingService routingService = new RoutingService();
        Simulacion simulacion = new Simulacion(routingService.simulacionSemanal(ahora));
        
        for(Solucion s : simulacion.getSoluciones()) {
            System.out.println("=================SOLUCION:===================");
            for(PlanCamion p : s.getPlanesCamion()){
                System.out.println("El camion: "+p.getCamion().getCodigo() + " y el size de subRutas es: "+p.getSubRutas().size());
                //double glpTanqueSolucion = 25;
                //double glpCargaSolucion = 25;
                for(SubRuta sub: p.getSubRutas()){
                    if(sub.getPedido()!=null){
                        System.out.println("El pedido es: "+ sub.getPedido().getId()+" y su capacidad es: "+ sub.getPedido().getCantidadGlp()+" y su ubi es: "+ sub.getPedido().getDestino().getPosX()+", "+ sub.getPedido().getDestino().getPosY());
                        //glpCargaSolucion = glpCargaSolucion - sub.getPedido().getCantidadGlp();
                    }
                    System.out.println("Inicio: "+sub.getTrayectoria().get(0).getPosX()+", "+sub.getTrayectoria().get(0).getPosY() + " Fin: "+sub.getTrayectoria().get(sub.getTrayectoria().size()-1).getPosX()+", "+sub.getTrayectoria().get(sub.getTrayectoria().size()-1).getPosY()+" y la trayectoria size es: "+sub.getTrayectoria().size());
                    //glpTanqueSolucion = glpTanqueSolucion - p.getCamion().calcularConsumo(sub.getTrayectoria().size()-1,glpCargaSolucion);

                }
            }
        }
        ArrayList<Solucion> soluciones = new ArrayList<>();
        LocalDateTime fechaSimulada = ahora;

        //Al inicio de la simulación.
        cargarBloqueos("data/bloqueos.txt");
        plantas = obtenerPlantas();
        ArrayList<Pedido> pedidosNoEntregados = new ArrayList<>();
        camiones = cargarCamiones("data/camiones.txt", ahora);
        pedidos = cargarPedidosCompletos("data/pedidos.txt", ahora);
        System.out.println("Al inicio de la simulación es: "+ fechaSimulada);
        while(!fechaSimulada.isAfter(ahora.plusHours(1))){
            //Esto varia
            pedidos = cargarPedidosParaPlanificar("data/pedidos.txt", fechaSimulada, pedidosNoEntregados);
            //System.out.println("COMIENZOOOOO EL WHILEEE CON: "+ fechaSimulada);
            

            ArrayList<Camion> camionesBackup = deepCopyCamiones(camiones);
            ArrayList<Pedido> pedidosBackup = deepCopyPedidos(pedidos);
            ArrayList<Planta> plantasBackup = deepCopyPlantas(plantas);

            System.out.println("--------- Los pedidos ingresados para ver esta solucion son:");
            for(Pedido p : pedidos){
                System.out.println("Su id es: "+p.getId() + "con cantidad de glp: "+p.getCantidadGlp());
            }
            System.out.println("BackUP:--------- Los pedidos ingresados para ver esta solucion son:");
            for(Pedido p : pedidosBackup){
                System.out.println("Su id es: "+p.getId() + "con cantidad de glp: "+p.getCantidadGlp());
            }
            
            SimulatedAnnealing sa = new SimulatedAnnealing( 5000, 0.005, 100, plantasBackup, camionesBackup, pedidosBackup, grid);
            Solucion mejor = sa.optimize(fechaSimulada);
            System.out.println("Una nueva solucion: ");
            for(PlanCamion p : mejor.getPlanesCamion()){
                System.out.println("El camion: "+p.getCamion());
                for(SubRuta sub: p.getSubRutas()){
                    if(sub.getPedido()!=null){
                        System.out.println("Existe pedido: "+sub.getPedido().getId());
                    }
                }
            }

            


            pedidosNoEntregados = actualizarDatos(mejor, fechaSimulada.plusMinutes(tiermpoSalto));

            soluciones.add(mejor);
            //System.out.println("Se agrego una nueva solucion al arreglo");

            fechaSimulada = fechaSimulada.plusMinutes(tiermpoSalto);
        }
        System.out.println("La fecha Simulada final es: "+ fechaSimulada+" y obtuvimos "+soluciones.size()+" soluciones");*/
    }

    public static ArrayList<Camion> deepCopyCamiones(ArrayList<Camion> original) {
        ArrayList<Camion> copia = new ArrayList<>();
        for (Camion c : original) {
            Camion nuevo = new Camion(
                c.getCodigo(), 
                c.getTipo(), 
                c.getUbicacionActual(), 
                c.isEnRuta(), 
                c.getDisponibleDesde(), 
                c.getGlpTanque(), 
                c.getGlpActual()
            );
            nuevo.setPesoVacio(c.getPesoVacio());
            nuevo.setCapacidadMaxima(c.getCapacidadMaxima());
            nuevo.setHoraLibre(c.getHoraLibre());
            
            if (c.getSubRutasExistentes() != null) {
                nuevo.setSubRutasExistentes(new ArrayList<>(c.getSubRutasExistentes()));
            }
            if (c.getMantenimientos() != null) {
                nuevo.setMantenimientos(new ArrayList<>(c.getMantenimientos()));
            }
            
            copia.add(nuevo);
        }
        return copia;
    }

    public static ArrayList<Pedido> deepCopyPedidos(ArrayList<Pedido> original) {
        ArrayList<Pedido> copia = new ArrayList<>();
        for (Pedido p : original) {
            Pedido nuevo = new Pedido(
                p.getId(), 
                p.getDestino(), 
                p.getIdCliente(), 
                p.getCantidadGlp(), 
                p.getHoraPedido(), 
                p.getPlazoMaximoEntrega()
            );
            nuevo.setTiempoDescarga(p.getTiempoDescarga());
            nuevo.setEntregado(p.isEntregado());
            copia.add(nuevo);
        }
        return copia;
    }

    public static ArrayList<Planta> deepCopyPlantas(ArrayList<Planta> original) {
        ArrayList<Planta> copia = new ArrayList<>();
        for (Planta p : original) {
            Planta nuevo = new Planta(p.getId(), p.getTipo(), p.getUbicacion());
            nuevo.setCapacidadMaxima(p.getCapacidadMaxima());
            nuevo.setGlpDisponible(p.getGlpDisponible());
            nuevo.setSiguienteRecarga(p.getSiguienteRecarga());
            nuevo.setIntervaloRecarga(p.getIntervaloRecarga());
            copia.add(nuevo);
        }
        return copia;
    }






    private static ArrayList<Pedido> actualizarDatos(Solucion solucion, LocalDateTime fechaSimulada) {
        ArrayList<Pedido> pedidosNoEntregados = new ArrayList<>();
        for(PlanCamion plan : solucion.getPlanesCamion()){
            if(plan.getSubRutas().size()!=0){
                Camion c = new Camion();
                for(Camion camion: camiones){
                    if(camion.getCodigo() == plan.getCamion().getCodigo()){
                        c = camion;
                    }
                }
                //Primero verifiquemos si ya termino la ultima subRuta de este plan.
                SubRuta last = plan.getSubRutas().get(plan.getSubRutas().size()-1);
                if(!last.getHoraFin().isAfter(fechaSimulada)){
                  //  System.out.println("Entro aqui 1");
                    c.setUbicacionActual(plantas.get(0).getUbicacion());
                    for(SubRuta sub: plan.getSubRutas()){
                        double glpConsumida = c.calcularConsumo(sub.getTrayectoria().size()-1);
                        c.setGlpTanque(c.getGlpTanque() - glpConsumida);
                        if(sub.getPedido()!=null){
                            double cargaEntregada = sub.getPedido().getCantidadGlp();
                            c.setGlpActual(c.getGlpActual() - cargaEntregada);

                            sub.getPedido().setEntregado(true);
                        }
                        if(Utilidades.esPlantaSecundaria(sub.getTrayectoria().get(0), plantas)){
                            Planta planta = Utilidades.obtenerPlanta(sub.getTrayectoria().get(0), plantas);
                            double glpFaltante = c.getCapacidadMaxima() - c.getGlpTanque();
                            planta.setGlpDisponible(planta.getGlpDisponible()-glpFaltante);
                        }
                        if(Utilidades.esPlantaPrincipal(sub.getTrayectoria().get(0), plantas)){
                            c.setGlpTanque(25);
                            c.setGlpActual(c.getCapacidadMaxima());
                        }
                        
                    }
                }else if(!plan.getSubRutas().get(0).getHoraInicio().isBefore(fechaSimulada)){ //Luego verifiquemos si aun no comienza.
                   // System.out.println("Entro aqui 2");
                    for(SubRuta sub: plan.getSubRutas()){
                        if(sub.getPedido()!=null){
                            pedidosNoEntregados.add(sub.getPedido());
                        }
                    }
                } else{
                    //Luego vemos si es que esta en ruta. Y buscamos en que subRuta esta, para ir checandolo desde ahí.
                for(SubRuta sub : plan.getSubRutas()){
                    if(sub.getTrayectoria().isEmpty()){
                        continue;
                    }
                    //System.out.println("Entro aqui 3");
                    //Si ya paso esa subRuta(A-> B).
                    if(!sub.getHoraFin().isAfter(fechaSimulada)){
                       // System.out.println("Entro aqui 3.1");
                        double glpConsumida = c.calcularConsumo(sub.getTrayectoria().size()-1);
                        c.setGlpTanque(c.getGlpTanque() - glpConsumida);
                        c.setUbicacionActual(sub.getFin());
                        if(sub.getPedido()!=null){
                            sub.getPedido().setEntregado(true);
                        }
                        if(Utilidades.esPlantaSecundaria(sub.getTrayectoria().get(0), plantas)){
                            Planta planta = Utilidades.obtenerPlanta(sub.getTrayectoria().get(0), plantas);
                            double glpFaltante = c.getCapacidadMaxima() - c.getGlpTanque();
                            planta.setGlpDisponible(planta.getGlpDisponible()-glpFaltante);
                        }
                        if(Utilidades.esPlantaPrincipal(sub.getTrayectoria().get(0), plantas)){
                            c.setGlpTanque(25);
                            c.setGlpActual(c.getCapacidadMaxima());
                        }
                    } else if (!sub.getHoraInicio().isBefore(fechaSimulada)){//Si aun no llego a esa SubRuta
                        if(sub.getPedido()!=null){
                            pedidosNoEntregados.add(sub.getPedido());
                        }
                    }else {//Si esta en plena SubRuta
                        //System.out.println("Entro aqui 3.2");
                        if(sub.getPedido()!=null){
                            pedidosNoEntregados.add(sub.getPedido());
                        }
                        for (int i = 0; i < sub.getTrayectoria().size() - 1; i++) {
                            LocalDateTime tiempoParcial = sub.getTiemposNodo().get(i);
                            LocalDateTime tiempoParcialSiguiente = sub.getTiemposNodo().get(i + 1);

                            if (fechaSimulada.isAfter(tiempoParcial) && fechaSimulada.isBefore(tiempoParcialSiguiente)) {
                                double distancia = i;
                                c.setUbicacionActual(sub.getTrayectoria().get(i));
                                c.setGlpTanque(c.getGlpTanque() - c.calcularConsumo(distancia));
                            }
                        }

                    }
                }
                }
            }
        }
        return pedidosNoEntregados;
    }

    public static ArrayList<Planta> obtenerPlantas() throws IOException {
        ArrayList<Planta> plantas = new ArrayList<>();
        plantas.add(new Planta(1, "PRINCIPAL", grid.getNodoAt(12, 8)));
        plantas.add(new Planta(2, "SECUNDARIA", grid.getNodoAt(42, 42)));
        plantas.add(new Planta(3, "SECUNDARIA", grid.getNodoAt(63, 8)));
        return plantas;
    }

    public static ArrayList<Pedido> cargarPedidosParaPlanificar(String filePath, LocalDateTime ahora, ArrayList<Pedido> pedidosNoEntregadosAnteriormente) throws IOException {
        ArrayList<Pedido> pedidos = obtenerPedidos(ahora);

        for (Pedido pedido : pedidos) {
            boolean yaExiste = false;
            
            for (Pedido pedidoPrevio : pedidosNoEntregadosAnteriormente) {
                if (pedido.getId() == pedidoPrevio.getId()) {
                    yaExiste = true;
                    break;  // Salimos del segundo bucle, ya sabemos que existe
                }
            }
            
            if (!yaExiste) {
                pedidosNoEntregadosAnteriormente.add(pedido);
            }
        }

        return pedidosNoEntregadosAnteriormente;
    }


    //El cargar pedidos segmentados me da la sección de pedidos nuevos.
    public static ArrayList<Pedido> cargarPedidosSegmentado(String filePath, LocalDateTime ahora) throws IOException {
        ArrayList<Pedido> pedidos = new ArrayList<>();
        Path path = Paths.get(filePath);
        LocalDateTime base = ahora.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        
        int i = 0;
        for (String line : Files.readAllLines(path)) {
            if (line.isBlank()) continue;
            
            String[] parts = line.split(":");

            String[] ts = parts[0].split("[dhm]");
            int d = Integer.parseInt(ts[0]);
            int h = Integer.parseInt(ts[1]);
            int m = Integer.parseInt(ts[2]);
            LocalDateTime horaPedido = base.plusDays(d-1).plusHours(h).plusMinutes(m);
            if (!horaPedido.isAfter(ahora) && !horaPedido.isBefore(ahora.minusMinutes(tiermpoSalto))) {
                i++;
                String[] vals = parts[1].split(",");
                int x = Integer.parseInt(vals[0]);
                int y = Integer.parseInt(vals[1]);
                String id = vals[2];
                int m3 = Integer.parseInt(vals[3].replace("m3", ""));
                int hLim = Integer.parseInt(vals[4].replace("h", ""));
                LocalDateTime plazoMax = horaPedido.plusHours(hLim);
                pedidos.add(new Pedido(String.valueOf(i), grid.getNodoAt(x, y), id, m3, horaPedido, plazoMax));
            }
        }
        return pedidos;
    }

    public static ArrayList<Pedido> obtenerPedidos(LocalDateTime ahora) throws IOException {
        ArrayList<Pedido> pedidosAObtener = new ArrayList<>();
        for(Pedido p: pedidos){
            if(!p.getHoraPedido().isAfter(ahora) && !p.getHoraPedido().isBefore(ahora.minusMinutes(tiermpoSalto))){
                pedidosAObtener.add(p);
            }
        }
        return pedidosAObtener;
    }

    public static ArrayList<Pedido> cargarPedidosCompletos(String filePath, LocalDateTime ahora) throws IOException {
        Path path = Paths.get(filePath);
        LocalDateTime base = ahora.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        
        int i = 0;
        for (String line : Files.readAllLines(path)) {
            if (line.isBlank()) continue;
            
            String[] parts = line.split(":");

            String[] ts = parts[0].split("[dhm]");
            int d = Integer.parseInt(ts[0]);
            int h = Integer.parseInt(ts[1]);
            int m = Integer.parseInt(ts[2]);
            LocalDateTime horaPedido = base.plusDays(d-1).plusHours(h).plusMinutes(m);
            String[] vals = parts[1].split(",");
            int x = Integer.parseInt(vals[0]);
            int y = Integer.parseInt(vals[1]);
            String id = vals[2];
            int m3 = Integer.parseInt(vals[3].replace("m3", ""));
            int hLim = Integer.parseInt(vals[4].replace("h", ""));
            LocalDateTime plazoMax = horaPedido.plusHours(hLim);
            i++;
            pedidos.add(new Pedido(String.valueOf(i), grid.getNodoAt(x, y), id, m3, horaPedido, plazoMax));
        }
        return pedidos;
    }

    public static ArrayList<Camion> cargarCamiones(String filePath, LocalDateTime ahora) throws IOException {
        ArrayList<Camion> camiones = new ArrayList<>();
        Map<String, Integer> count = new HashMap<>();
        Path path = Paths.get(filePath);
        
        for (String line : Files.readAllLines(path)) {
            if (line.isBlank()) continue;
            
            String[] parts = line.split(",");
            String tipo = parts[0];
            double tanqueActual = Double.parseDouble(parts[1]);
            double cargaActual = Double.parseDouble(parts[2]);
            int x = Integer.parseInt(parts[3]);
            int y = Integer.parseInt(parts[4]);
            
            int idx = count.getOrDefault(tipo, 0) + 1;
            count.put(tipo, idx);
            String codigo = String.format("%s%02d", tipo, idx);
            
            camiones.add(new Camion(codigo, tipo, grid.getNodoAt(x, y), false, ahora, tanqueActual, cargaActual));
        }
        return camiones;
    }

    public static void cargarBloqueos(String archivo) throws IOException {
        List<String> lineas = Files.readAllLines(Paths.get(archivo));
        
        for (String linea : lineas) {
            if (linea.trim().isEmpty() || linea.trim().startsWith("#")) continue;
            
            String[] partes = linea.split(":");
            if (partes.length != 2) {
                System.err.println("Formato inválido en línea: " + linea);
                continue;
            }

            String[] tiempos = partes[0].split("-");
            if (tiempos.length != 2) {
                System.err.println("Formato de tiempo inválido en línea: " + linea);
                continue;
            }

            LocalDateTime inicio = parsearFechaHora(tiempos[0]);
            LocalDateTime fin = parsearFechaHora(tiempos[1]);

            String[] coordenadas = partes[1].split(",");
            if (coordenadas.length % 2 != 0) {
                System.err.println("Número impar de coordenadas en línea: " + linea);
                continue;
            }

            for (int i = 0; i < coordenadas.length - 2; i += 2) {
                try {
                    int x1 = Integer.parseInt(coordenadas[i].trim());
                    int y1 = Integer.parseInt(coordenadas[i + 1].trim());
                    int x2 = Integer.parseInt(coordenadas[i + 2].trim());
                    int y2 = Integer.parseInt(coordenadas[i + 3].trim());

                    List<Nodo> intermedios = Utilidades.obtenerNodosIntermedios(x1, y1, x2, y2, grid);
                    for (Nodo nodo : intermedios) {
                        nodo.agregarBloqueo(inicio, fin);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Coordenada inválida en línea: " + linea);
                }
            }
        }
    }

    public static void cargarMantenimientos(String archivo, List<Camion> camiones) throws IOException {
        List<String> lineas = Files.readAllLines(Paths.get(archivo));
        
        for (String linea : lineas) {
            linea = linea.trim();
            if (linea.isEmpty() || linea.startsWith("#")) continue;
            
            String[] partes = linea.split(":");
            

            String fechaString = partes[0].trim();
            String codigoCamion = partes[1].trim();

            int anho = Integer.parseInt(fechaString.substring(0, 4));
            int mes = Integer.parseInt(fechaString.substring(4, 6));
            int dia = Integer.parseInt(fechaString.substring(6, 8));

            LocalDateTime inicio = LocalDateTime.of(anho, mes, dia, 0, 0);
            LocalDateTime fin = LocalDateTime.of(anho, mes, dia, 23, 59);

            TimeRange rango = new TimeRange(inicio, fin);

            camiones.stream()
                .filter(c -> c.getCodigo().equals(codigoCamion))
                .findFirst()
                .ifPresentOrElse(
                    c -> {
                        if (c.getMantenimientos() == null) {
                            c.setMantenimientos(new ArrayList<>());
                        }
                        c.getMantenimientos().add(rango);
                    },
                    () -> System.err.println("Camión con código " + codigoCamion + " no encontrado.")
                );
        }
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

        return LocalDateTime.of(2025, Month.JULY, dia, hora, minuto, 0, 0);
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