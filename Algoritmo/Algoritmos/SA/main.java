package Algoritmos.SA;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.io.*;
import java.time.LocalDateTime;
import java.time.Month;
import java.awt.Point;

public class main {
    public static void main(String[] args) throws IOException {
        List<Pedido> pedidos = cargarPedido("data/pedidos.txt");
        List<Camion> camiones = cargarCamione("data/camiones.txt");
        Set<Bloqueo> bloqueos = cargarBloqueo("data/bloqueos.txt");
        // Crear el objeto de Simulated Annealing sin bloqueos
        System.out.println("Iniciando Simulated Annealing...");
        SimulatedAnnealing SA = new SimulatedAnnealing(pedidos, camiones, bloqueos);
        SA.ejecutar();

    }


    // Métodos de carga de datos
    static List<Pedido> cargarPedido(String archivo) throws IOException {
        List<Pedido> pedidos = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(archivo));
        String linea;

        while ((linea = br.readLine()) != null) {
            // Omitir líneas vacías o comentarios (líneas que no empiezan con número)
            if (linea.trim().isEmpty() || !Character.isDigit(linea.trim().charAt(0))) {
                continue;
            }

            String[] partes = linea.split(":");
            String tiempo = partes[0];
            String datos = partes[1];

            // Parsear tiempo (ej. 01d12h25m)
            int dia = Integer.parseInt(tiempo.substring(0, 2));
            int hora = Integer.parseInt(tiempo.substring(3, 5));
            int minuto = Integer.parseInt(tiempo.substring(6, 8));
            int tiempoEnMinutos = hora + minuto * 60; // redondeo de minutos si deseas

            // Parsear datos después de los dos puntos
            String[] valores = datos.split(",");
            int ubicacionX = Integer.parseInt(valores[0]);
            int ubicacionY = Integer.parseInt(valores[1]);
            String idCliente = valores[2]; // quitar el 'c-'
            Cliente cliente = new Cliente(1, idCliente); // Asumiendo que tienes este constructor

            String cantidadStr = valores[3].replace("m3", "");
            int cantidad = Integer.parseInt(cantidadStr);

            String horaStr = valores[4].replace("h", "");
            int plazo = Integer.parseInt(horaStr);

            // Crear el pedido
            Pedido pedido = new Pedido();
            pedido.setId(pedidos.size() + 1); // Generar ID incremental
            pedido.setDia(dia);
            pedido.setCantidad(cantidad);
            pedido.setUbicacionX(ubicacionX);
            pedido.setUbicacionY(ubicacionY);
            pedido.setHora(tiempoEnMinutos); // Asignas el plazo (4h) a hora, o si prefieres usa otro campo
            pedido.setCliente(cliente);
            pedido.setEstado("pendiente"); // o lo que corresponda
            pedido.setTiempoMaximo(plazo);
            pedidos.add(pedido);
        }

        br.close();
        return pedidos;
    }

    static List<Camion> cargarCamione(String archivo) throws IOException {
        List<Camion> camiones = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(archivo));
        String linea;

        int[] contadorTipo = new int[Camion.TipoCamion.values().length]; // Para generar códigos únicos por tipo

        while ((linea = br.readLine()) != null) {
            if (linea.trim().isEmpty() || linea.trim().startsWith("#"))
                continue;

            String[] partes = linea.split(",");
            if (partes.length != 5) {
                System.err.println("Línea inválida (esperados 5 valores): " + linea);
                continue;
            }

            try {
                Camion.TipoCamion tipo = Camion.TipoCamion.valueOf(partes[0].trim());
                double pesoVacio = Double.parseDouble(partes[1].trim());
                int capacidadEfectiva = Integer.parseInt(partes[2].trim());
                double pesoMaximoCarga = Double.parseDouble(partes[3].trim());
                double _ = Double.parseDouble(partes[4].trim()); // este valor no se usa en la clase, pero lo ignoramos

                // Crear un código del tipo TA01, TB02, etc.
                int indice = tipo.ordinal();
                contadorTipo[indice]++;
                String codigo = String.format("%s%02d", tipo.name(), contadorTipo[indice]);

                Camion camion = new Camion(codigo, tipo, pesoVacio, capacidadEfectiva, pesoMaximoCarga);
                camiones.add(camion);
            } catch (Exception e) {
                System.err.println("Error al parsear línea: " + linea);
                e.printStackTrace();
            }
        }

        br.close();
        return camiones;
    }

    static Set<Bloqueo> cargarBloqueo(String archivo) throws IOException {
        Set<Bloqueo> bloqueos = new HashSet<>();
        BufferedReader br = new BufferedReader(new FileReader(archivo));
        String linea;

        while ((linea = br.readLine()) != null) {
            // Omitir líneas vacías o comentarios
            if (linea.trim().isEmpty() || linea.trim().startsWith("#")) {
                continue;
            }

            // Dividir la línea en partes: tiempo y coordenadas
            String[] partes = linea.split(":");
            if (partes.length != 2) {
                System.err.println("Formato inválido en línea: " + linea);
                continue;
            }

            // Procesar el rango de tiempo
            String[] tiempos = partes[0].split("-");
            if (tiempos.length != 2) {
                System.err.println("Formato de tiempo inválido en línea: " + linea);
                continue;
            }

            // Parsear fecha/hora de inicio
            LocalDateTime inicio = parsearFechaHora(tiempos[0]);
            // Parsear fecha/hora de fin
            LocalDateTime fin = parsearFechaHora(tiempos[1]);

            // Procesar las coordenadas de los nodos bloqueados
            String[] coordenadas = partes[1].split(",");
            if (coordenadas.length % 2 != 0) {
                System.err.println("Número impar de coordenadas en línea: " + linea);
                continue;
            }

            List<Point> nodosBloqueados = new ArrayList<>();
            for (int i = 0; i < coordenadas.length; i += 2) {
                try {
                    int x = Integer.parseInt(coordenadas[i].trim());
                    int y = Integer.parseInt(coordenadas[i + 1].trim());
                    nodosBloqueados.add(new Point(x, y));
                } catch (NumberFormatException e) {
                    System.err.println("Coordenada inválida en línea: " + linea);
                }
            }

            // Crear el bloqueo y agregarlo al conjunto
            bloqueos.add(new Bloqueo(inicio, fin, nodosBloqueados));
        }

        br.close();
        return bloqueos;

    }

    private static LocalDateTime parsearFechaHora(String texto) {
        // Eliminar posibles espacios
        texto = texto.trim();

        // Dividir en días, horas, minutos
        String[] partes = texto.split("[dhm]");
        if (partes.length < 3) {
            throw new IllegalArgumentException("Formato de fecha inválido: " + texto);
        }

        // Obtener valores numéricos
        int dia = Integer.parseInt(partes[0]);
        int hora = Integer.parseInt(partes[1]);
        int minuto = Integer.parseInt(partes[2]);
        // Crear objeto LocalDateTime (asumiendo mes actual y año actual)
        return LocalDateTime.of(2025, Month.JANUARY, dia, hora, minuto, 0, 0);

    }
}

