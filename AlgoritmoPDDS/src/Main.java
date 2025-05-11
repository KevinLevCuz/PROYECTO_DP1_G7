import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class Main {
    private static Nodo ubicacionInicial = new Nodo(0,0);
    static Random rand = new Random();
    static int TAMANO_POBLACION = 1;
    static int GENERACIONES = 200;
    static double PROB_MUTACION = 0.1;
    static double PROB_CRUCE = 0.8;
    static int TORNEOS_K = 3; 
    public static void main(String[] args) {
        try{
            Main main = new Main();

            LocalDateTime fechaSimulacion = LocalDateTime.of(2025, Month.MAY, 1, 8, 0); 
            
            List<Pedido> pedidos = cargarPedidos("..\\data\\pedidos.txt");
            List<Camion> camiones = cargarCamiones("..\\data\\camiones.txt");
            List<Bloqueo> bloqueos = cargarBloqueos("..\\data\\bloqueos.txt");
            List<Mantenimiento> mantenimientos = cargaMantenimientos("..\\data\\mantenimiento.txt");

            List<Planta> plantas = new ArrayList<>();
            Planta plantaPrincipal = new Planta("PRINCIPAL", new Nodo(0, 0));
            Planta plantaSecundaria1 = new Planta("SECUNDARIA", new Nodo(5, 5));
            Planta plantaSecundaria2 = new Planta("SECUNDARIA", new Nodo(10, 10));
            plantas.add(plantaPrincipal);
            plantas.add(plantaSecundaria1);
            plantas.add(plantaSecundaria2);

            main.AlgoritmoGenetico(pedidos, camiones, bloqueos, mantenimientos, plantas, fechaSimulacion);


        } catch (IOException e){
            System.err.println("Error al cargar archivos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void AlgoritmoGenetico(List<Pedido> pedidos, List<Camion> camiones, List<Bloqueo> bloqueos, List<Mantenimiento> mantenimientos, List<Planta> plantas, LocalDateTime fechaHora){
        List<Solucion> poblacion = new ArrayList<>();
        List<Pedido> pedidosNoEntregados = pedidos;
        List<Mantenimiento> mantenimientosVigentes = mantenimientos;
        List<Bloqueo> bloqueosVigentes = bloqueos;
        poblacion = InicializarPoblacion(TAMANO_POBLACION, pedidosNoEntregados, mantenimientosVigentes, plantas, camiones, bloqueosVigentes,fechaHora);
        // FOR (TANTAS GENERACIONES)
        // EVALUACION DE LA POBLACION (Metodo calcular el fitness)
        // SELECCIONAMOS LOS PADRES(VAMOS UNOS 3 DE LOS MEJORES CONJUNTOS DE RUTAS)
        // CRUZAMIENTO
        // MUTAR
        
    }

    private List<Solucion> InicializarPoblacion(int tamPoblacion,List<Pedido> pedidosNoEntregados,List<Mantenimiento> mantenimientosVigentes,List<Planta> plantas, List<Camion> camiones,List<Bloqueo> bloqueosVigentes, LocalDateTime fechaHora){
        List<Solucion> poblacionInicial = new ArrayList<>();
        Solucion solucion;
        for(int i=0; i<tamPoblacion; i++){
            solucion = CrearSolucion(pedidosNoEntregados, mantenimientosVigentes, plantas, camiones, bloqueosVigentes,fechaHora);
            
            for(Ruta ruta: solucion.getSolucion()){
                System.out.println("Ruta: Camion: "+ ruta.getCamion().getCodigo()+" Nodos:"+ruta.detallarNodos());
            }

            poblacionInicial.add(solucion);
        }
        return poblacionInicial;
    }

    private Solucion CrearSolucion(List<Pedido> pedidosNoEntregados, List<Mantenimiento> mantenimientosVigentes, 
                                 List<Planta> plantas, List<Camion> camionesDisponibles,
                                 List<Bloqueo> bloqueosVigentes, LocalDateTime fechaSimulacion) {
        
        AsignadorCamiones asignador = new AsignadorCamiones(fechaSimulacion);
        asignador.setCamiones(camionesDisponibles);
        asignador.setPlantas(plantas);
        asignador.setBloqueos(bloqueosVigentes);
        asignador.setMantenimientos(mantenimientosVigentes);
        
        return asignador.asignarPedidos(pedidosNoEntregados);
    }

    //Carga de archivos
    static List<Pedido> cargarPedidos(String archivo) throws IOException {
        List<Pedido> pedidos = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(archivo));
        String linea;

        while ((linea = br.readLine()) != null) {
            String[] partes = linea.split(":");
            String tiempo = partes[0];
            String datos = partes[1];

            int año = 2025;
            int mes = 5;
            int dia = Integer.parseInt(tiempo.substring(0, 2));
            int hora = Integer.parseInt(tiempo.substring(3, 5));
            int minuto = Integer.parseInt(tiempo.substring(6, 8));
            LocalDateTime fechaHora = LocalDateTime.of(año, mes, dia, hora, minuto);

            String[] valores = datos.split(",");
            int ubicacionX = Integer.parseInt(valores[0]);
            int ubicacionY = Integer.parseInt(valores[1]);
            Nodo ubicacion = new Nodo(ubicacionX,ubicacionY);

            String idCliente = valores[2];

            String cantidadGlpString = valores[3].replace("m3", "");
            int cantidadGlp = Integer.parseInt(cantidadGlpString);

            String plazoHorasMaximoString = valores[4].replace("h", "");
            int plazoHorasMaximo = Integer.parseInt(plazoHorasMaximoString);

            Ruta ruta = new Ruta();

            Pedido pedido = new Pedido(ubicacion, ruta,  idCliente, cantidadGlp, fechaHora ,plazoHorasMaximo);
            pedidos.add(pedido);
        }

        br.close();
        return pedidos;
    }
    
    static List<Camion> cargarCamiones(String archivo) throws IOException {
        List<Camion> camiones = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(archivo));
        String linea;
        while ((linea = br.readLine()) != null) {

            if (linea.trim().isEmpty() || linea.trim().startsWith("#"))
                continue;
            String[] partes = linea.split(",");

            try {
                String tipo = partes[0].trim();
                Camion camion = new Camion(tipo,ubicacionInicial);
                camiones.add(camion);
            } catch (Exception e) {
                System.err.println("Error al parsear línea: " + linea);
                e.printStackTrace();
            }

        }
        br.close();
        return camiones;
    }

    static List<Bloqueo> cargarBloqueos(String archivo) throws IOException {
        List<Bloqueo> bloqueos = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(archivo));
        String linea;

        while ((linea = br.readLine()) != null) {

            if (linea.trim().isEmpty() || linea.trim().startsWith("#")) {
                continue;
            }

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

            List<Nodo> nodosBloqueados = new ArrayList<>();
            for (int i = 0; i < coordenadas.length; i += 2) {
                try {
                    int x = Integer.parseInt(coordenadas[i].trim());
                    int y = Integer.parseInt(coordenadas[i + 1].trim());
                    nodosBloqueados.add(new Nodo(x, y));
                } catch (NumberFormatException e) {
                    System.err.println("Coordenada inválida en línea: " + linea);
                }
            }
            bloqueos.add(new Bloqueo(nodosBloqueados, inicio, fin));
        }
        br.close();
        return bloqueos;
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

    public static List<Mantenimiento> cargaMantenimientos(String archivo) throws IOException {
        List<Mantenimiento> listaMantenimientos = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(archivo));
        String linea;

        while ((linea = br.readLine()) != null) {
            linea = linea.trim();
            if (linea.isEmpty() || linea.startsWith("#")) {
                continue; 
            }

            String[] partes = linea.split(":");
            if (partes.length != 2) {
                System.err.println("Línea inválida: " + linea);
                continue; 
            }

            String fechaString = partes[0].trim(); 
            String codigoCamion = partes[1].trim();

            int anho = Integer.parseInt(fechaString.substring(0, 4));
            int mes = Integer.parseInt(fechaString.substring(4, 6));
            int dia = Integer.parseInt(fechaString.substring(6, 8));

            LocalDateTime fechaHoraInicio = LocalDateTime.of(anho, mes, dia,0,0);
            LocalDateTime fechaHoraFin = LocalDateTime.of(anho, mes, dia,23,59);

            Mantenimiento mantenimiento = new Mantenimiento(fechaHoraInicio, fechaHoraFin, codigoCamion,"PREVENTIVO");

            listaMantenimientos.add(mantenimiento);
        }
        br.close();
        return listaMantenimientos;
    }

}

