package Algoritmos.SA2;

import java.io.BufferedReader;
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

public class Main {
    // private static Nodo ubicacionInicial = new Nodo(0,0);
    public static void main(String[] args) throws IOException {

        LocalDateTime ahora = LocalDateTime.now();

        ArrayList<Pedido> pedidos = cargarPedidos("data/pedidos.txt");
        ArrayList<Camion> camiones = cargarCamiones("data/camiones.txt");
        ArrayList<Bloqueo> bloqueos = cargarBloqueos("data/bloqueos.txt");
        ArrayList<Mantenimiento> mantenimientos = cargarMantenimientos("data/mantenimiento.txt");

        ArrayList<Planta> plantas = new ArrayList<>();

        Planta plantaPrincipal = new Planta("PRINCIPAL", new Nodo(0, 0));
        Planta plantaSecundaria1 = new Planta("SECUNDARIA", new Nodo(5, 5));
        Planta plantaSecundaria2 = new Planta("SECUNDARIA", new Nodo(10, 10));

        plantas.add(plantaPrincipal);
        plantas.add(plantaSecundaria1);
        plantas.add(plantaSecundaria2);

        SimulatedAnnealing sa = new SimulatedAnnealing(
                10, 0.003, 3,
                plantas, bloqueos, mantenimientos);
        Solucion mejor = sa.optimize(pedidos, camiones, plantas, bloqueos, mantenimientos, ahora);
        System.out.println("Se llegó a dar solución\n");
        mejor.imprimirRutas();

    }

    public static ArrayList<Pedido> cargarPedidos(String filePath) throws IOException {
        ArrayList<Pedido> pedidos = new ArrayList<>();
        Path path = Paths.get(filePath);
        // Base del mes de simulación: primer día a las 00:00
        LocalDateTime base = LocalDateTime.now()
                .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        for (String line : Files.readAllLines(path)) {
            if (line.isBlank())
                continue;
            String[] parts = line.split(":");
            // Tiempo de llegada
            String[] ts = parts[0].split("[dhm]");
            int d = Integer.parseInt(ts[0]);
            int h = Integer.parseInt(ts[1]);
            int m = Integer.parseInt(ts[2]);
            LocalDateTime horaPedido = base.plusDays(d).plusHours(h).plusMinutes(m);
            // Datos restantes
            String[] vals = parts[1].split(",");
            int x = Integer.parseInt(vals[0]);
            int y = Integer.parseInt(vals[1]);
            String id = vals[2];
            int m3 = Integer.parseInt(vals[3].replace("m3", ""));
            int hLim = Integer.parseInt(vals[4].replace("h", ""));
            LocalDateTime plazoMax = horaPedido.plusHours(hLim);
            Pedido p = new Pedido(new Nodo(x, y), id, m3, horaPedido, plazoMax);
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
            Nodo ubic = new Nodo(0, 0);
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

}