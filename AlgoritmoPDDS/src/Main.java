import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class Main {
    static LocalDateTime fechaSimulada = LocalDateTime.of(2025, Month.MAY, 5, 0, 30); 
    static LocalDateTime fechaMinima = LocalDateTime.of(2025, Month.JANUARY, 1, 0, 0); 
    static LocalDateTime fechaHoraLimite = LocalDateTime.of(2025, Month.MAY, 5, 9, 59); 

    //static LocalDateTime fechaSimulada = LocalDateTime.of(2025, Month.MAY, 5, 0, 1); 
    static Grid grid = new Grid(70,50);
    public static void main(String[] args) {
        try{
            Main main = new Main();
            
            List<Planta> plantas = new ArrayList<>();
            Planta plantaPrincipal = new Planta("PRINCIPAL", new Nodo(0, 0));
            Planta plantaSecundaria1 = new Planta("SECUNDARIA", new Nodo(5, 5));
            Planta plantaSecundaria2 = new Planta("SECUNDARIA", new Nodo(10, 10));
            plantas.add(plantaPrincipal);
            plantas.add(plantaSecundaria1);
            plantas.add(plantaSecundaria2);

            List<Pedido> pedidos = cargarPedidos("..\\data\\pedidos.txt");
            List<Camion> camiones = cargarCamiones("..\\data\\camiones.txt",plantaPrincipal.getUbicacion());
            cargarBloqueos("..\\data\\bloqueos.txt", grid);
            cargaMantenimientos("..\\data\\mantenimiento.txt",camiones);
            List<Asignacion> asignaciones = new ArrayList<>();
            int tiempoSalto = 30;

           while(fechaSimulada.isBefore(fechaHoraLimite)){
                System.out.println("\n\n\n=======INICIO DE ASIGNACION =================================");
                System.out.println("Ingreso: La fecha Simulada es: "+fechaSimulada);
                List<Pedido> pedidosParaAsignar = new ArrayList<>();
                 List<Pedido> pedidosNoEntregados = new ArrayList<>();
                 List<Camion> camionesOrdenados = new ArrayList<>();

                if(!asignaciones.isEmpty()){

                    camiones.sort((a, b) -> {
                        boolean aTieneAsignacion = a.isAsignacionSimulada() && !(a.getUbicacion().getPosX() == plantaPrincipal.getUbicacion().getPosX() && a.getUbicacion().getPosY() == plantaPrincipal.getUbicacion().getPosY());
                        boolean bTieneAsignacion = b.isAsignacionSimulada() && !(b.getUbicacion().getPosX() == plantaPrincipal.getUbicacion().getPosX() && b.getUbicacion().getPosY() == plantaPrincipal.getUbicacion().getPosY());

                        // Queremos que los que tienen pedidos (≠ 0) vayan primero
                        if (aTieneAsignacion && !bTieneAsignacion){
                            return -1;
                        } 
                        if (!aTieneAsignacion && bTieneAsignacion){
                            return 1;
                        }
                        return 0; // si ambos son iguales (ambos 0 o ambos ≠ 0), mantenemos el orden
                    });

                   /* for(Camion camion:camiones){
                        System.out.print("Camion con codigo: "+ camion.getCodigo()+" y ubicacion: "+camion.getUbicacion().detallarEnString()+" y la asignacion es: "+camion.isAsignacionSimulada());
                    }
                    System.out.println("\n");*/ 

                    pedidosNoEntregados = main.actualizarDatos(fechaSimulada, asignaciones, plantas);
                    asignaciones = new ArrayList<>();
                    
                    System.out.println("\n");
                    
                    //for(Camion camion: camiones){
                      //  System.out.println("El camion con codigo:"+camion.getCodigo()+" esta disponible?"+ camion.isDisponible(fechaSimulada)+"en la ubicacion: "+camion.getUbicacion().detallarEnString()+ " con una carga restante de: "+camion.getGlpCargaRest()+" y un tanque restante de: "+camion.getGlpTanqueRest()+"su simulacion esta en: "+camion.getGlpCargaRestSim()+" y el del tanque esta en:"+camion.getGlpTanqueRestSim()+" y su estado de simulacion esta en: "+camion.isAsignacionSimulada());
                    //}
                    /*System.out.println("======================0");
                    for(Pedido pedido: pedidos){
                        System.out.println("El pedido es:"+pedido.getId()+" con una cantidad: "+pedido.getCantidadGlp()+" y tiene estado: "+pedido.isEntregado());
                    }
                    for(Pedido pedido: pedidosParaAsignar){
                        System.out.println("Pedidos para asignar en esta asignacion: ");
                        System.out.println("El pedido es:"+pedido.getId()+" con una cantidad: "+pedido.getCantidadGlp()+" y tiene estado: "+pedido.isEntregado());
                    }*/
                }
                for(Pedido pedido: pedidosNoEntregados){
                    pedidosParaAsignar.add(pedido);
                }
                pedidos.sort(Comparator.comparing(Pedido::getFechaMaximaEntrega));
                for(Pedido pedido:pedidos){
                    if(pedido.estaEntre(fechaSimulada.minusMinutes(tiempoSalto), fechaSimulada) && !pedido.isEntregado()){
                        pedidosParaAsignar.add(pedido);
                    }
                }
                for(Pedido pedido: pedidosParaAsignar){
                    System.out.println("La lista queda asi: "+ pedido.getUbicacion().detallarEnString());
                }
                
                if(!pedidosParaAsignar.isEmpty()){
                    asignaciones = main.generarSolucionInicial(pedidosParaAsignar, camiones, plantas, fechaSimulada);
                } else {
                    System.out.println("No hay pedidos pendientes. Con fecha simulada"+fechaSimulada);
                }
                
                for(Camion camion: camiones){
                    //System.out.println("El codigo es: "+camion.getCodigo()+" y la ubicacion en X:"+camion.getUbicacion().getPosX()+" y la de Y:"+camion.getUbicacion().getPosY()+" y su estado de asignqacion simulada es:"+camion.isAsignacionSimulada());
                    if((camion.getUbicacion().getPosX()!=0 || camion.getUbicacion().getPosY()!=0 )&& !camion.isAsignacionSimulada()){
                        Asignacion nuevaAsignacion = main.AsignarTrayectoriaRegreso(camion);
                        System.out.println("Ingreso una nueva asignacion:");
                        asignaciones.add(nuevaAsignacion);
                    }
                }

                if(asignaciones.isEmpty()){
                    asignaciones = new ArrayList<>();
                }
                // Visualización
                VisualizadorAsignaciones.mostrarResumenAsignaciones(asignaciones,grid, fechaSimulada, pedidos, plantas);

                //  Mostrar detalle del primer camión (ejemplo)
                if (!asignaciones.isEmpty()) {
                    for(Asignacion asignacion: asignaciones){
                        VisualizadorAsignaciones.mostrarDetalleCamion(asignacion, grid, fechaSimulada, pedidos, plantas);                        
                    }
                }

                fechaSimulada=fechaSimulada.plusMinutes(tiempoSalto);
           }

            
            // Mostrar pedidos no asignados
            //VisualizadorAsignaciones.mostrarPedidosNoAsignados(pedidos);
            
            // Mostrar estado de las plantas
            //Utilidades.mostrarEstadoPlantas(plantas); 
            /* 
            for (Camion camion : camiones) {
                System.out.println("Código: " + camion.getCodigo());
                System.out.println("Tipo: " + camion.getTipo());
                System.out.println("Ubicación: " + camion.getUbicacion()); 
                System.out.println("GLP en tanque: " + camion.getGlpTanque());
                System.out.println("GLP en carga: " + camion.getGlpCarga());
                System.out.println("GLP restante en tanque: " + camion.getGlpTanqueRest());
                System.out.println("GLP restante en carga: " + camion.getGlpCargaRest());
                System.out.println("GLP restante simulado en tanque: " + camion.getGlpTanqueRestSim());
                System.out.println("GLP restante simulado en carga: " + camion.getGlpCargaRestSim());
                System.out.println("Disponible: " + camion.isDisponible(fechaSimulada));
                System.out.println("Mantenimientos: ");
                for (TimeRange t : camion.getMantenimientos()) {
                    System.out.println("  Desde: " + t.getStart() + " hasta: " + t.getEnd());
                }
                System.out.println("-----------------------------");
            }
           
            for (Planta planta : plantas) {
                System.out.println("Código: " + planta.getId());
                System.out.println("Tipo: " + planta.getTipo());
                System.out.println("Ubicación: " + planta.getUbicacion()); 
                System.out.println("Maxima GLP: " + planta.getGlpMaxima());
                System.out.println("GLP restante: " + planta.getGlpRest());
                System.out.println("GLP restante simulado: " + planta.getGlpRestSim());
                System.out.println("-----------------------------");
            }*/
            
        
            //main.AlgoritmoGenetico(pedidos, camiones, plantas, fechaSimulada);
        } catch (IOException e){
            System.err.println("Error al cargar archivos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Pedido> actualizarDatos(LocalDateTime fechaHora, List<Asignacion> asignaciones, List<Planta> plantas){
        int tiempoDespuesDePartir = 0;
        int contador = 0;
        List<Pedido> pedidosAunNoEntregados = new ArrayList<>();
        for(Asignacion asignacion: asignaciones){
            asignacion.getCamion().resetSimulacion();
            asignacion.getCamion().setAsignacionSimulada(false);
            asignacion.getCamion().setSegundosFaltantesParaSalir(0);
            if(asignacion.getFechaPartida().isAfter(fechaHora)){
                for(SubRuta subRuta: asignacion.getSubRutas()){
                    if(subRuta.getPedido()!=null){
                        pedidosAunNoEntregados.add(subRuta.getPedido());
                    }
                }
                continue;
            }
            
            if(!asignacion.getSubRutas().isEmpty() && !asignacion.getSubRutas().getLast().getFechaLlegada().isAfter(fechaHora)){
                asignacion.getCamion().setUbicacion(grid.getNodoAt(0, 0));
                for(SubRuta subRuta: asignacion.getSubRutas()){
                    contador++;
                    double glpConsumida = asignacion.getCamion().calcularConsumo(subRuta.getTrayectoria().size()-1);
                    asignacion.getCamion().setGlpTanqueRest(asignacion.getCamion().getGlpTanqueRest()-glpConsumida);
                    if(subRuta.getPedido()!=null){
                        subRuta.getPedido().setEntregado(true);
                        asignacion.getCamion().setGlpCargaRest(asignacion.getCamion().getGlpCargaRest()-subRuta.getPedido().getCantidadGlp());
                    }
                    if(Utilidades.esPlanta(subRuta.getUbicacionFin(), plantas)){
                        Planta planta = Utilidades.obtenerPlanta(subRuta.getUbicacionFin(), plantas);
                        planta.setGlpRest(planta.getGlpRest()- Utilidades.obtenerGlpACargar(asignacion,contador+1));
                    }
                }
                continue;
            }
            
            contador = 0;
            for(SubRuta subRuta: asignacion.getSubRutas()){
                contador++;
                if(!subRuta.getFechaLlegada().isAfter(fechaHora)){
                    asignacion.getCamion().setUbicacion(subRuta.getUbicacionFin());
                    double glpConsumida = asignacion.getCamion().calcularConsumo(subRuta.getTrayectoria().size()-1);
                    asignacion.getCamion().setGlpTanqueRest(asignacion.getCamion().getGlpTanqueRest()-glpConsumida);
                    if(subRuta.getPedido()!=null){
                        subRuta.getPedido().setEntregado(true);
                        asignacion.getCamion().setGlpCargaRest(asignacion.getCamion().getGlpCargaRest()-subRuta.getPedido().getCantidadGlp());
                    }
                    if(Utilidades.esPlanta(subRuta.getUbicacionFin(), plantas)){
                        Planta planta = Utilidades.obtenerPlanta(subRuta.getUbicacionFin(), plantas);
                        planta.setGlpRest(planta.getGlpRest()- Utilidades.obtenerGlpACargar(asignacion,contador+1));
                    }
                    int tiempoParaSalir = (int) Duration.between(subRuta.getFechaLlegada(),fechaHora).toSeconds();
                    asignacion.getCamion().setSegundosFaltantesParaSalir(900-tiempoParaSalir);
                } else if (( !subRuta.getFechaPartida().isAfter(fechaHora) ) && subRuta.getFechaLlegada().isAfter(fechaHora)){
                    tiempoDespuesDePartir = (int) Duration.between(subRuta.getFechaPartida(), fechaHora).toSeconds();
                    int distanciaRecorrida = tiempoDespuesDePartir/72;
                    asignacion.getCamion().setUbicacion(subRuta.getTrayectoria().get(distanciaRecorrida+1));
                    
                    double glpConsumida = asignacion.getCamion().calcularConsumo(distanciaRecorrida);
                    asignacion.getCamion().setGlpTanqueRest(asignacion.getCamion().getGlpTanqueRest()-glpConsumida);
                    if(asignacion.getSubRutas().getLast()==subRuta){
                        asignacion.getCamion().setDeRegreso(true);
                        asignacion.getCamion().setAsignacionSimulada(false);
                    }
                    if(subRuta.getPedido()!=null){
                        pedidosAunNoEntregados.add(subRuta.getPedido());
                    }
                } 
            }
        }
        return pedidosAunNoEntregados;
    }

    public List<Asignacion> generarSolucionInicial(List<Pedido> pedidos, List<Camion> camiones, 
                                                 List<Planta> plantas, LocalDateTime fechaSimulada) {
        List<Asignacion> asignaciones = new ArrayList<>();
        List<Pedido> pedidosPendientes = new ArrayList<>(pedidos);

        // Ordenar pedidos por prioridad (fecha límite más cercana primero)
        //pedidosPendientes.sort(Comparator.comparing(Pedido::getFechaMaximaEntrega));
    
        for (Pedido pedido : pedidosPendientes) {
            boolean asignado = false;
            for (Camion camion : camiones) {
                
                System.out.println("El camion a verificar es:"+ camion.getCodigo()+" y su simulacion estado es:"+camion.isAsignacionSimulada());
                if(camion.isAsignacionSimulada()){
                    continue;
                }
                
                List<SubRuta> subRutas = intentarAsignarPedidoSimple(camion, pedido, plantas, fechaSimulada,pedidos);
                if(subRutas == null){
                   // intentarAsignarPedidoConRecarga(pedido, plantas, fechaSimulada, pedidosPendientes, asignaciones);
                }
                if (subRutas != null) {
                    // Confirmar asignación en objetos reales
                    Asignacion asignacion = new Asignacion(camion, subRutas, subRutas.getFirst().getFechaPartida());
                    asignaciones.add(asignacion);
                    confirmarAsignacionSimulada(asignacion, pedido, plantas);
                    camion.setAsignacionSimulada(true);
                    asignado = true;
                    break;
                } else {
                    System.out.println("No se pudo asignar con el camion: "+camion.getCodigo());
                }
            }
            
            if (!asignado) {
                System.out.println("Advertencia: No se pudo asignar pedido " + pedido.getId()+ " con cantidad de: "+ pedido.getCantidadGlp()+ " y el cliente es:"+pedido.getIdCliente()+" y su fecha maxima es: "+pedido.getFechaMaximaEntrega()+" y la fecha simulada es: "+fechaSimulada);
                System.out.println("Se intentará nuevamente:");
                // return generarSolucionInicial(pedidos, camiones, plantas, fechaSimulada);
            }
        }        
        return asignaciones;
    }
/* 
    private void AsignarDatosReales(List<Asignacion> asignaciones, List<Pedido> pedidos, List<Planta> plantas) {
        for(Asignacion asignacion: asignaciones){
            Camion camion = asignacion.getCamion();
            double consumoSubRuta = 0;
            for(SubRuta subruta: asignacion.getSubRutas()){
                consumoSubRuta = camion.calcularConsumo(subruta.getTrayectoria().size()-1);
                camion.setGlpTanqueRest(camion.getGlpTanqueRest()-consumoSubRuta);
                if(Utilidades.esPedido(subruta.getUbicacionFin(), pedidos)){
                    Pedido pedido = Utilidades.obtenerPedido(subruta.getUbicacionFin(), pedidos);
                    camion.setGlpCargaRest(camion.getGlpCargaRest()-pedido.getCantidadGlp());
                }
                if(Utilidades.esPlanta(subruta.getUbicacionFin(), plantas) && subruta!=asignacion.getSubRutas().getLast()){
                    Planta planta = Utilidades.obtenerPlanta(subruta.getUbicacionFin(), plantas);
                    double glpACargar = 0;
                    planta.setGlpRest(planta.getGlpRest()-glpACargar);
                    camion.setGlpTanqueRest(camion.getGlpTanque());
                }
            }
        }
    }*/

    private Asignacion AsignarTrayectoriaRegreso(Camion camion) {
        Asignacion asignacion = new Asignacion();
        List<SubRuta> subRutas = new ArrayList<>();
        SubRuta subRuta = new SubRuta(camion.getUbicacion(), grid.getNodoAt(0, 0), null);
        Map.Entry<List<Nodo>,Integer> resultado = subRuta.generarTrayectoria(grid, fechaSimulada, fechaHoraLimite.plusDays(10));
        List<Nodo> trayectoria = resultado.getKey();
        subRuta.setTrayectoria(trayectoria);
        double consumoGlp = camion.calcularConsumo(trayectoria.size()-1);
       // System.out.println("El camion es: "+camion.getCodigo()+" y "+camion.getGlpTanqueRest()+ " y el consumo a gastar es: "+ consumoGlp+ " y trayectoria es: "+trayectoria);
        if(camion.getGlpTanqueRest()>consumoGlp || trayectoria.isEmpty()){
            subRuta.setFechaPartida(fechaSimulada.plusSeconds(camion.getSegundosFaltantesParaSalir()));
            subRuta.setFechaLlegada(subRuta.getFechaPartida().plusSeconds(Utilidades.calcularDistanciaDeTrayectoria(trayectoria)*72));
            subRutas.add(subRuta);
            return new Asignacion(camion, subRutas,subRuta.getFechaPartida());
        } else {
            System.out.println("Espero nunca ingrese aquí, sino hay algo mal en la validacion del consumo del glp al realizar la vuelta.");
        }
       return asignacion;
    }

    private List<SubRuta> intentarAsignarPedidoSimple(Camion camion, Pedido pedido, List<Planta> plantas, LocalDateTime fechaHora, List<Pedido> pedidos) {
        Camion camionSim = camion.crearCopiaSimulacion();
        List<Planta> plantasSim = clonarListaPlantasSimulacion(plantas);
        
        // Opción 1: Ruta directa (UbicacionCamión -> Pedido -> Planta)
        List<SubRuta> rutaDirecta = Arrays.asList(
            new SubRuta(camionSim.getUbicacion(), pedido.getUbicacion(),pedido),
            new SubRuta(pedido.getUbicacion(), plantasSim.get(0).getUbicacion(),null)
        );
        int segundosRetorno = verificarRutaSimulacion(camionSim, pedido, plantasSim, rutaDirecta, pedidos);
        if (segundosRetorno!=-1) {
            AsignarFechasYTrayectoriaSubRutas(rutaDirecta, pedidos,plantas,camionSim,segundosRetorno);
            if(camionSim.estaDisponibleEnRango(rutaDirecta.getFirst().getFechaPartida(), rutaDirecta.getLast().getFechaLlegada()) && !pedido.getFechaMaximaEntrega().isBefore(rutaDirecta.getFirst().getFechaLlegada())){
                return rutaDirecta;
            }
        
        } 
        
        return null;
    }

/* 
    private void intentarAsignarPedidoConEntrega(Pedido pedido, List<Planta> plantas, LocalDateTime fechaHora, List<Pedido> pedidos, List<Asignacion> asignaciones){
        for(Asignacion asignacion: asignaciones){
           Camion camionAsignado = asignacion.getCamion().crearCopiaSimulacion();
           List<Planta> plantasSim = clonarListaPlantasSimulacion(plantas);
            List<SubRuta> subRutas = asignacion.getSubRutas();
            for(Planta planta: plantas){
                
                List<SubRuta> rutaConRecarga = new ArrayList<>();
                for (int i = 0; i < subRutas.size() - 1; i++) {
                    rutaConRecarga.add(subRutas.get(i));
                }
                rutaConRecarga.add(new SubRuta(subRutas.get(subRutas.size() - 1).getUbicacionFin(), planta.getUbicacion())); 
                rutaConRecarga.add(new SubRuta(planta.getUbicacion(), pedido.getUbicacion()));
                rutaConRecarga.add(new SubRuta(pedido.getUbicacion(), plantasSim.get(0).getUbicacion())); 
                if (verificarRutaSimulacion(camionAsignado, pedido, plantasSim, rutaConRecarga, pedidos)) {
                    AsignarFechasYTrayectoriaSubRutas(rutaConRecarga, pedidos,plantas, camionAsignado);
                    if(camionAsignado.estaDisponibleEnRango(rutaConRecarga.getFirst().getFechaPartida(), rutaConRecarga.getLast().getFechaLlegada())){
                        System.out.println("Se campeonó");
                        asignaciones.remove(asignacion);
                        asignaciones.add(new Asignacion(camionAsignado, rutaConRecarga, rutaConRecarga.getFirst().getFechaPartida()));
                    }
                }
            }
        }
    }

    private void intentarAsignarPedidoConRecarga(Pedido pedido, List<Planta> plantas, LocalDateTime fechaHora, List<Pedido> pedidos, List<Asignacion> asignaciones) {
        Asignacion asignacionAEliminar = null;
        Asignacion asignacionANueva = null;

        for (Asignacion asignacion : asignaciones) {
            Camion camionAsignado = asignacion.getCamion().crearCopiaSimulacion();
            List<Planta> plantasSim = clonarListaPlantasSimulacion(plantas);
            List<SubRuta> subRutas = asignacion.getSubRutas();

            for (Planta planta : plantas) {
                List<SubRuta> rutaConRecarga = new ArrayList<>();
                for (int i = 0; i < subRutas.size() - 1; i++) {
                    rutaConRecarga.add(subRutas.get(i));
                }

                rutaConRecarga.add(new SubRuta(subRutas.get(subRutas.size() - 1).getUbicacionFin(), planta.getUbicacion(),null));
                rutaConRecarga.add(new SubRuta(planta.getUbicacion(), pedido.getUbicacion(),pedido));
                rutaConRecarga.add(new SubRuta(pedido.getUbicacion(), plantasSim.get(0).getUbicacion(),null));

                int segundosRetorno = verificarRutaSimulacion(camionAsignado, pedido, plantasSim, rutaConRecarga, pedidos);
                if (segundosRetorno!=-1) {
                    AsignarFechasYTrayectoriaSubRutas(rutaConRecarga, pedidos, plantas, camionAsignado,segundosRetorno);
                    if (camionAsignado.estaDisponibleEnRango(
                            rutaConRecarga.getFirst().getFechaPartida(),
                            rutaConRecarga.getLast().getFechaLlegada())) {

                        System.out.println("Se campeonó");
                        camionAsignado.setGlpCargaRestSim(camionAsignado.getGlpCargaRestSim() - pedido.getCantidadGlp());
                        asignacionAEliminar = asignacion;
                        asignacionANueva = new Asignacion(camionAsignado, rutaConRecarga, rutaConRecarga.getFirst().getFechaPartida());
                        break; // Rompemos bucle interno
                    }
                }
            }

            if (asignacionAEliminar != null) break; 
        }

        if (asignacionAEliminar != null && asignacionANueva != null) {
            asignaciones.remove(asignacionAEliminar);
            asignaciones.add(asignacionANueva);
        }
    }*/

    private void AsignarFechasYTrayectoriaSubRutas(List<SubRuta> RutaCompleta, List<Pedido> pedidos, List<Planta> plantas, Camion camion, int segundosRetorno) {
        LocalDateTime fechaSimuladaTemp = fechaSimulada.plusSeconds(segundosRetorno);
        for(SubRuta subRuta: RutaCompleta){
            List<Nodo> trayectoria = subRuta.getTrayectoria();
            subRuta.setTrayectoria(trayectoria);
            //double distancia = Utilidades.calcularDistanciaDeTrayectoria(trayectoria);

            //double tiempoAntesDePartir = 0;
            int tiempoTrayectoria = (trayectoria.size()-1)*72;

            subRuta.setFechaPartida(fechaSimuladaTemp);

            fechaSimuladaTemp = fechaSimuladaTemp.plusSeconds((long) tiempoTrayectoria);

            subRuta.setFechaLlegada(fechaSimuladaTemp);

            if(Utilidades.esPedido(subRuta.getUbicacionFin(), pedidos) || Utilidades.esPlanta(subRuta.getUbicacionFin(), plantas)){
                fechaSimuladaTemp = fechaSimuladaTemp.plusMinutes(15);
            }
        }
       }

    private int verificarRutaSimulacion(Camion camionSim, Pedido pedido, List<Planta> plantasSim, List<SubRuta> subRutas, List<Pedido> pedidos) {
        int segundosRetorno = -1;
        
        List<Nodo> trayectoria = new ArrayList<>();
        Map.Entry<List<Nodo>,Integer> resultado = null;
        int segundos = 0;

        if(Utilidades.esPedido(subRutas.get(0).getUbicacionFin(), pedidos)){
            if (camionSim.getGlpCargaRest() < pedido.getCantidadGlp()) {
                System.out.println("No alcanza la carga");
                return -1;
            }
        }
        
        Nodo posicionActual = camionSim.getUbicacion();
        double tiempoRestParaSalir = 0; // en minutos
        segundos = 0;
        int segundosMinimos = 14401;
        int segundosAñadir = 0;

        LocalDateTime fechaSimuladaProvisional = fechaSimulada;


        if(fechaSimulada.isAfter(pedido.getFechaMaximaEntrega())){
            System.out.println("Se sobrepasa la fecha maxima de pedido");
            return -1;
        }
        
        Boolean existeAsignacion = false;
        Double distanciaCompleta = 0.0;
        int contadorWhiles = 0;
        
    
        int paradas15min = 0;
        for (SubRuta subRuta : subRutas) {
            if(Utilidades.esPedido(subRuta.getUbicacionFin(), pedidos) || (Utilidades.esPlantaPrincipal(subRuta.getUbicacionFin(), plantasSim) && subRuta!=subRutas.getLast())){
                paradas15min++;
            }
            trayectoria = new ArrayList<>();
            resultado = null;
            System.out.println("La fecha maxima de esta subRuta es: "+Utilidades.fechaMaximaTrayectoria(subRutas,subRuta)+ " y la subruta es: "+subRuta);
            
            resultado = subRuta.generarTrayectoria(grid,fechaSimuladaProvisional, Utilidades.fechaMaximaTrayectoria(subRutas,subRuta));
            trayectoria = resultado.getKey();
            segundos = resultado.getValue();

            if(segundos<segundosMinimos){
                segundosMinimos = segundos;
                
            }

            double distancia = Utilidades.calcularDistanciaDeTrayectoria(trayectoria);
            
            distanciaCompleta += distancia;
            fechaSimuladaProvisional = fechaSimuladaProvisional.plusSeconds((long) distancia*72);
            subRuta.setTrayectoria(trayectoria);
            
            double consumo = camionSim.calcularConsumo(distancia);
            //Aqui verificamos combustible, si aún le queda para recorrer esta subruta.
            if (camionSim.getGlpTanqueRest() < consumo) {
                System.out.println("No le alcanza el tanque.");
                return -1;
            }
            
            // Actualizar simulación
            camionSim.setGlpTanqueRest(camionSim.getGlpTanqueRest() - consumo);
            
            // Si llegamos a una planta, recargamos
            if (Utilidades.esPlanta(subRuta.getUbicacionFin(), plantasSim)) {
                camionSim.setGlpTanqueRest(camionSim.getGlpTanque());
                
                // Si no es la planta principal, verificar GLP disponible
                if (!Utilidades.esPlantaPrincipal(subRuta.getUbicacionFin(), plantasSim)) {
                    Planta planta = Utilidades.obtenerPlanta(subRuta.getUbicacionFin(), plantasSim);
                    if(planta==null){
                        System.out.println("Hay un error en la asignacion de algo de las plantas.");
                    }
                    if (planta.getGlpRest() < pedido.getCantidadGlp()) {
                        System.out.println("Esta en una planta, y a la planta no le alcanza el plg para que puedan cargar.");
                        return -1;
                    }
                    planta.setGlpRest(planta.getGlpRest() - pedido.getCantidadGlp());
                }
            }

            posicionActual = subRuta.getUbicacionFin();
            if (Utilidades.esPedido(posicionActual, pedidos)){
                if(fechaSimuladaProvisional.isBefore(pedido.getFechaRegistro().plusHours(4))){
                    existeAsignacion = true;
                    if(segundosMinimos==14401){
                        segundosAñadir = (int) Duration.between(fechaSimulada, pedido.getFechaRegistro().plusHours(4)).toSeconds() - (int)(distanciaCompleta*72+(paradas15min-1)*900);
                    }
                    fechaSimuladaProvisional = fechaSimuladaProvisional.plusMinutes(15);
                } 
            }
            System.out.println("Paso todo correctamente");


        }
        
        
        // Actualizar GLP de carga en simulación
        if(camionSim.getGlpCargaRest() < pedido.getCantidadGlp()){
            return -1;
        }
        camionSim.setGlpCargaRest(camionSim.getGlpCargaRest() - pedido.getCantidadGlp());

        segundosRetorno=segundosAñadir;
        System.out.println("Los segundos retorno son: "+ segundosRetorno);
        return segundosRetorno;
        //ME QUEDE AQUI
    }
    private List<Planta> clonarListaPlantasSimulacion(List<Planta> originales) {
        List<Planta> copia = new ArrayList<>();
        for (Planta p : originales) {
            Planta clon = new Planta(p.getTipo(), p.getUbicacion()); // no copies la ubicación
            clon.setGlpMaxima(p.getGlpMaxima());
            clon.setGlpRest(p.getGlpRest());
            clon.setGlpRestSim(p.getGlpRestSim()); // importante
            copia.add(clon);
        }
        return copia;
    }

    private void confirmarAsignacionSimulada(Asignacion asignacion, Pedido pedido, List<Planta> plantasReales) {
        asignacion.getCamion().setGlpCargaRestSim(asignacion.getCamion().getGlpCargaRest() - pedido.getCantidadGlp());
        //camionReal.setUbicacion(subRutas.get(subRutas.size()-1).getUbicacionFin()); las ubicaciones se actualizan en la siguiente ejecucion.
        int numSubRutas = asignacion.getSubRutas().size();
        // Actualizar plantas reales
        for (SubRuta subRuta : asignacion.getSubRutas()) {
            //Si el inicio es planta y despues regresa a la misma planta Principal. Este es raro, pero pa' seguir viendo el problema.
            if(Utilidades.esPlanta(subRuta.getUbicacionInicio(),plantasReales) && numSubRutas==2){
                Planta planta = Utilidades.obtenerPlanta(subRuta.getUbicacionInicio(), plantasReales);
                planta.setGlpRestSim(planta.getGlpRestSim() - pedido.getCantidadGlp());
            }   
        }
        
        pedido.setAsignadoSim(true);
    }
































    //Carga de archivos
    static List<Pedido> cargarPedidos(String archivo) throws IOException {
        List<Pedido> pedidos = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(archivo));
        String linea;

        while ((linea = br.readLine()) != null) {
            System.out.println("Ingreso aqui es:"+linea);
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

            Pedido pedido = new Pedido(ubicacion,  idCliente, cantidadGlp, fechaHora ,plazoHorasMaximo);
            pedidos.add(pedido);
        }

        br.close();
        return pedidos;
    }
    
    static List<Camion> cargarCamiones(String archivo, Nodo ubicacionInicial) throws IOException {
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

    static void cargarBloqueos(String archivo, Grid grid) throws IOException {
        
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
        br.close();
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

    public static void cargaMantenimientos(String archivo, List<Camion> camiones) throws IOException {
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

        LocalDateTime inicio = LocalDateTime.of(anho, mes, dia, 0, 0);
        LocalDateTime fin = LocalDateTime.of(anho, mes, dia, 23, 59);

        TimeRange rango = new TimeRange(inicio, fin);

        Camion camionEncontrado = camiones.stream()
            .filter(c -> c.getCodigo().equals(codigoCamion))
            .findFirst()
            .orElse(null);

        if (camionEncontrado != null) {
            if (camionEncontrado.getMantenimientos() == null) {
                camionEncontrado.setMantenimientos(new ArrayList<>());
            }
            camionEncontrado.getMantenimientos().add(rango);
        } else {
            System.err.println("Camión con código " + codigoCamion + " no encontrado.");
        }
    }
    br.close();
}

}

