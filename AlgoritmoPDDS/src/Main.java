import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class Main {
    
    static double costeTotalFinal = 0;
    static double fitnessPromPorPedido = 0;
    static int numPedidosTotales = 0;
    static int tiempoTotal = 0;
    static List<Planta> plantas = new ArrayList<>();
    static List<Pedido> pedidos = new ArrayList<>();
    static List<Camion> camiones = new ArrayList<>();

    //static LocalDateTime fechaSimulada = LocalDateTime.of(2025, Month.MAY, 5, 0, 1); 
    static Grid grid = new Grid(71,51);

    public List<Asignacion> AlgoritmoGenetico(List<Pedido> pedidos, List<Camion> camiones, 
                                           List<Planta> plantas, LocalDateTime fechaSimulada) {
        SolucionGA solucionGA = new SolucionGA(pedidos, camiones, plantas, fechaSimulada);
        return solucionGA.ejecutarAlgoritmoGenetico(pedidos);
    }
 
    public static void main(String[] args) {
        try{
            //Aqui instanciamos un objeto de la clase Main.
            Main main = new Main();
            LocalDateTime fechaSimulada = LocalDateTime.of(2025, Month.MAY, 1, 1, 0); 
            LocalDateTime fechaMinima = LocalDateTime.of(2025, Month.JANUARY, 1, 0, 0); 
            LocalDateTime fechaHoraLimite = LocalDateTime.of(2025, Month.MAY, 3, 17, 59); 

            //Metodo que cargará todos los archivos
            cargarArchivos();

            //En esta lista se guardarán todas las asignaciones realizadas por el algoritmo genético, cambiará en cada iteración.
            List<Asignacion> asignaciones = new ArrayList<>();

            //Este es el tiempo en minutos, que se incrementa en cada iteración.
            // Ejemplo de uso: Se ejecuta el algoritmo a las 12:00pm, la siguiente iteracción será en 12:00pm + tiempo de Salto(en minutos).
            int tiempoSalto = 210;

            //Se ejecutará el algoritmo genético tantas veces hasta que llegué a la fecha limite. Teniendo en cuenta que el tiempo esta aumenta en cada iteración.
            while(fechaSimulada.isBefore(fechaHoraLimite)){
                System.out.println("====INICIO: Fecha de simulación: "+fechaSimulada+"====");

                List<Pedido> pedidosParaAsignar = new ArrayList<>();
                List<Pedido> pedidosNoEntregados = new ArrayList<>();
                List<Camion> camionesOrdenados = new ArrayList<>();

                //Si las asignaciones no estan vacias, se actualizan los datos.
                if(!asignaciones.isEmpty()){
                    //Aqui ordenamos los camiones de forma que los que han sido asignados en la anterior ejecución, vayan primero para que sigan con sus rutas.
                    camiones.sort((a, b) -> {
                        boolean aTieneAsignacion = a.isAsignacionSimulada() && !(a.getUbicacion().getPosX() == plantas.get(0).getUbicacion().getPosX() && a.getUbicacion().getPosY() == plantas.get(0).getUbicacion().getPosY());
                        boolean bTieneAsignacion = b.isAsignacionSimulada() && !(b.getUbicacion().getPosX() == plantas.get(0).getUbicacion().getPosX() && b.getUbicacion().getPosY() == plantas.get(0).getUbicacion().getPosY());

                        if (aTieneAsignacion && !bTieneAsignacion){
                            return -1;
                        } 
                        if (!aTieneAsignacion && bTieneAsignacion){
                            return 1;
                        }
                        return 0;
                    });

                    //Aqui actualizamos todos los datos, que dejó la ejecución anterior.
                    pedidosNoEntregados = main.actualizarDatos(fechaSimulada, asignaciones, plantas);

                    //Esto es porque en esta nueva ejecución quiero la lista vacia para guardar nuevas asignaciones.
                    asignaciones = new ArrayList<>();
                }
                
                // Esto es porque todos los pedidos que aún no han sido entregados de la anterior ejecución, se van a asignar en esta nueva ejecución.
                for(Pedido pedido: pedidosNoEntregados){
                    pedidosParaAsignar.add(pedido);
                }

                // Ordenamos por fecha Máxima de entrega.
                pedidos.sort(Comparator.comparing(Pedido::getFechaMaximaEntrega));

                //Esto es porque los pedidos nuevos que llegan en el tiempo intermedio entre la antigua ejecución y la actual, también van.
                for(Pedido pedido:pedidos){
                    if(pedido.estaEntre(fechaSimulada.minusMinutes(tiempoSalto), fechaSimulada) && !pedido.isEntregado()){
                        pedidosParaAsignar.add(pedido);
                    }
                }                

                System.out.println("Los pedidos a asignar son estos:");
                for(Pedido pedido:pedidosParaAsignar){
                    System.out.println("La ubicación del pedido es: "+pedido.getUbicacion().detallarEnString() + " y la fecha de registro es: "+pedido.getFechaRegistro()+" y la fecha máxima de entrega es: "+pedido.getFechaMaximaEntrega());
                }
                //Si la lista de pedidos a asignar no esta vacia, se ejecuta el algoritmo genético.
                if(!pedidosParaAsignar.isEmpty()){

                    asignaciones = main.generarSolucionInicial(pedidosParaAsignar, camiones, plantas, fechaSimulada);
                                    
                    
                    int totalPedidos = 0;
                    // Contar pedidos asignados y calcular costos
                    for (Asignacion asignacion : asignaciones) {
                        for (SubRuta subRuta : asignacion.getSubRutas()) {
                            if (subRuta.getPedido() != null) {
                                totalPedidos++;
                            }
                        }
                    }
                    /* 
                    // Confirmar asignaciones en objetos reales
                    for (Asignacion asignacion : asignaciones) {
                        for (SubRuta subRuta : asignacion.getSubRutas()) {
                            if (subRuta.getPedido() != null) {
                                main.confirmarAsignacionSimulada(asignacion, subRuta.getPedido(), plantas);
                            }
                        }
                        asignacion.getCamion().setAsignacionSimulada(true);
                    }*/
                }

                //Este es para cualquier camión que no se haya asignado, y este en ruta de regreso de la anterior ejecución, asignarle su ruta de regreso.
                for(Camion camion: camiones){
                    if((camion.getUbicacion().getPosX()!=0 || camion.getUbicacion().getPosY()!=0 )&& !camion.isAsignacionSimulada()){
                        Asignacion nuevaAsignacion = main.AsignarTrayectoriaRegreso(camion, fechaSimulada, fechaHoraLimite);
                        asignaciones.add(nuevaAsignacion);
                    }
                }
                 
                //Visualización
                

                //Mostrar detalle del primer camión (ejemplo)
                if (!asignaciones.isEmpty()) {
                    VisualizadorAsignaciones.mostrarResumenAsignaciones(asignaciones,grid, fechaSimulada, pedidos, plantas);
                    for(Asignacion asignacion: asignaciones){
                        VisualizadorAsignaciones.mostrarDetalleCamion(asignacion, grid, fechaSimulada, pedidos, plantas);                        
                    }
                }

                fechaSimulada=fechaSimulada.plusMinutes(tiempoSalto);
            }
            
            // Mostrar pedidos no asignados
            VisualizadorAsignaciones.mostrarPedidosNoAsignados(pedidos);
            
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
        List<Asignacion> AsignacionesPorSeguir = new ArrayList<>();
        
        for(Asignacion asignacion: asignaciones){
            contador=0;
            asignacion.getCamion().resetSimulacion();
            asignacion.getCamion().setAsignacionSimulada(false);
            asignacion.getCamion().setSegundosFaltantesParaSalir(0);

            //Si aún la asignación no se ha dado.
            if(asignacion.getFechaPartida().isAfter(fechaHora)){
                AsignacionesPorSeguir.add(asignacion);
                for(SubRuta subRuta: asignacion.getSubRutas()){
                    if(subRuta.getPedido()!=null){
                        pedidosAunNoEntregados.add(subRuta.getPedido());
                    }
                }
                continue;
            }
            
            // Si la asignación ya se ha terminado.
            if(!asignacion.getSubRutas().isEmpty() && !asignacion.getSubRutas().getLast().getFechaLlegada().isAfter(fechaHora)){
                asignacion.getCamion().setUbicacion(grid.getNodoAt(0, 0));
                for(SubRuta subRuta: asignacion.getSubRutas()){
                    double glpConsumida = asignacion.getCamion().calcularConsumo(subRuta.getTrayectoria().size()-1);
                    asignacion.getCamion().setGlpTanqueRest(asignacion.getCamion().getGlpTanqueRest()-glpConsumida);
                    if(subRuta.getPedido()!=null){
                        subRuta.getPedido().setEntregado(true);
                       System.out.println("El pedido con id: "+ subRuta.getPedido().getId()+" ha sido entregado");
                        asignacion.getCamion().setGlpCargaRest(asignacion.getCamion().getGlpCargaRest()-subRuta.getPedido().getCantidadGlp());
                    }
                    if(Utilidades.esPlanta(subRuta.getUbicacionFin(), plantas)){
                        Double glpCamionFaltante = asignacion.getCamion().getGlpCarga()- asignacion.getCamion().getGlpCargaRest();
                        asignacion.getCamion().setGlpCargaRest(asignacion.getCamion().getGlpCarga());
                        Planta planta = Utilidades.obtenerPlanta(subRuta.getUbicacionFin(), plantas);
                        planta.setGlpRest(planta.getGlpRest()- glpCamionFaltante);
                    }
                }
                int tiempoParaSalir = (int) Duration.between(asignacion.getSubRutas().getLast().getFechaLlegada(),fechaHora).toSeconds();
                asignacion.getCamion().setSegundosFaltantesParaSalir(900-tiempoParaSalir);
                continue;
            }
            
            //Si la asignación está en camino.
            contador = 0;
            for(SubRuta subRuta: asignacion.getSubRutas()){
                contador++;

                //Si ya pasó esa subRuta
                if(!subRuta.getFechaLlegada().isAfter(fechaHora)){
                    asignacion.getCamion().setUbicacion(subRuta.getUbicacionFin());
                    double glpConsumida = asignacion.getCamion().calcularConsumo(subRuta.getTrayectoria().size()-1);
                    asignacion.getCamion().setGlpTanqueRest(asignacion.getCamion().getGlpTanqueRest()-glpConsumida);
                    if(subRuta.getPedido()!=null){
                        subRuta.getPedido().setEntregado(true);
                       System.out.println("El pedido con id: "+ subRuta.getPedido().getId()+" ha sido entregado");
                        asignacion.getCamion().setGlpCargaRest(asignacion.getCamion().getGlpCargaRest()-subRuta.getPedido().getCantidadGlp());
                    }
                    if(Utilidades.esPlanta(subRuta.getUbicacionFin(), plantas)){
                        Double glpCamionFaltante = asignacion.getCamion().getGlpCarga()- asignacion.getCamion().getGlpCargaRest();
                        asignacion.getCamion().setGlpCargaRest(asignacion.getCamion().getGlpCarga());
                        Planta planta = Utilidades.obtenerPlanta(subRuta.getUbicacionFin(), plantas);
                        planta.setGlpRest(planta.getGlpRest()- glpCamionFaltante);
                    }
                    if(subRuta.getPedido()!=null || Utilidades.esPlantaPrincipal(subRuta.getUbicacionFin(), plantas)){
                        int tiempoParaSalir = (int) Duration.between(subRuta.getFechaLlegada(),fechaHora).toSeconds();
                        asignacion.getCamion().setSegundosFaltantesParaSalir(900-tiempoParaSalir);
                    }
                } 

                // Si el camión se quedó justo en su subRuta.
                else if (( !subRuta.getFechaPartida().isAfter(fechaHora) ) && subRuta.getFechaLlegada().isAfter(fechaHora)){ 
                    tiempoDespuesDePartir = (int) Duration.between(subRuta.getFechaPartida(), fechaHora).toSeconds();
                    int distanciaRecorrida = tiempoDespuesDePartir/72;
                    asignacion.getCamion().setUbicacion(subRuta.getTrayectoria().get(distanciaRecorrida));
                    
                    double glpConsumida = asignacion.getCamion().calcularConsumo(distanciaRecorrida);
                    asignacion.getCamion().setGlpTanqueRest(asignacion.getCamion().getGlpTanqueRest()-glpConsumida);
                    if(asignacion.getSubRutas().getLast()==subRuta){
                        asignacion.getCamion().setDeRegreso(true);
                        asignacion.getCamion().setAsignacionSimulada(false);
                    }
                    if(subRuta.getPedido()!=null){
                        pedidosAunNoEntregados.add(subRuta.getPedido());
                    }
                    asignacion.getCamion().setSegundosFaltantesParaSalir(0);
                    break;
                } 
                
                //Si no llego a alcanzar la SubRuta. 
                else {
                    if (subRuta.getPedido() != null && !pedidosAunNoEntregados.contains(subRuta.getPedido())) {
                        pedidosAunNoEntregados.add(subRuta.getPedido());
                    }
                }
            }
        }
        return pedidosAunNoEntregados;
    }

    public List<Asignacion> generarSolucionInicial(List<Pedido> pedidosParaAsignar, List<Camion> camiones, List<Planta> plantas, LocalDateTime fechaSimulada) {
        
        List<Asignacion> asignaciones = new ArrayList<>();

        // Ordenar pedidos por prioridad (fecha límite más cercana primero)
        //pedidosPendientes.sort(Comparator.comparing(Pedido::getFechaMaximaEntrega));
    
        for (Pedido pedido : pedidosParaAsignar) {
            boolean asignado = false;
            for (Camion camion : camiones) {
                
                //System.out.println("El camion a verificar es:"+ camion.getCodigo()+" y su simulacion estado es:"+camion.isAsignacionSimulada());
                if(camion.isAsignacionSimulada()){
                    continue;   
                }
                
                List<SubRuta> subRutas = intentarAsignarPedidoSimple(camion, pedido, plantas,pedidosParaAsignar, fechaSimulada);
                System.out.println("La subRutas que me mandaron son:");
                if (subRutas != null) {
                    // Confirmar asignación en objetos reales
                    for(SubRuta subRuta: subRutas){
                        System.out.println("La subRuta inicial es: "+subRuta.getUbicacionInicio().detallarEnString()+" y la final es: "+subRuta.getUbicacionFin().detallarEnString()+" y el camión que esta tomando es: "+camion.getCodigo());
                    }
                    Asignacion asignacion = new Asignacion(camion, subRutas, subRutas.getFirst().getFechaPartida());
                    asignaciones.add(asignacion);
                    confirmarAsignacionSimulada(asignacion, pedido, plantas);
                    camion.setAsignacionSimulada(true);
                    asignado = true;
                    break;
                } else {
                   // System.out.println("No se pudo asignar con el camion: "+camion.getCodigo());
                }
            }
        }
        return asignaciones;
    }

    private Asignacion AsignarTrayectoriaRegreso(Camion camion, LocalDateTime fechaSimulada, LocalDateTime fechaHoraLimite) {
        Asignacion asignacion = new Asignacion();
        List<SubRuta> subRutas = new ArrayList<>();
        SubRuta subRuta = new SubRuta(camion.getUbicacion(), grid.getNodoAt(0, 0), null);
        Map.Entry<List<Nodo>,Integer> resultado = subRuta.generarTrayectoria(grid, fechaSimulada.plusSeconds(camion.getSegundosFaltantesParaSalir()), fechaHoraLimite.plusDays(10));
        List<Nodo> trayectoria = resultado.getKey();
        subRuta.setTrayectoria(trayectoria);
        double consumoGlp = camion.calcularConsumo(trayectoria.size()-1);
       // System.out.println("El camion es: "+camion.getCodigo()+" y "+camion.getGlpTanqueRest()+ " y el consumo a gastar es: "+ consumoGlp+ " y trayectoria es: "+trayectoria);
        if(camion.getGlpTanqueRest()>consumoGlp || trayectoria.isEmpty()){
            subRuta.setFechaPartida(fechaSimulada.plusSeconds(camion.getSegundosFaltantesParaSalir()));
            subRuta.setFechaLlegada(subRuta.getFechaPartida().plusSeconds((trayectoria.size()-1)*72));
            subRutas.add(subRuta);
            return new Asignacion(camion, subRutas,subRuta.getFechaPartida());
        } else {
            System.out.println("Espero nunca ingrese aquí, sino hay algo mal en la validacion del consumo del glp al realizar la vuelta.");
        }
       return asignacion;
    }

    public List<SubRuta> intentarAsignarPedidoSimple(Camion camion, Pedido pedido, List<Planta> plantas, List<Pedido> pedidosParaAsignar, LocalDateTime fechaSimulada) {
        // Opción 1: Ruta directa (UbicacionCamión -> Pedido -> Planta Principal)
        List<SubRuta> rutaDirecta = Arrays.asList(
            new SubRuta(camion.getUbicacion(), pedido.getUbicacion(),pedido),
            new SubRuta(pedido.getUbicacion(), plantas.get(0).getUbicacion(),null)
        );
        int segundosRetorno = verificarRutaSimulacion(camion, pedido, plantas, rutaDirecta, pedidosParaAsignar, fechaSimulada);
        if (segundosRetorno!=-1) {
            AsignarFechasYTrayectoriaSubRutas(rutaDirecta, pedidosParaAsignar,plantas,camion,segundosRetorno, fechaSimulada);
            System.out.println("la primera es:"+ camion.estaDisponibleEnRango(rutaDirecta.getFirst().getFechaPartida(), rutaDirecta.getLast().getFechaLlegada()) + " y el segundo es:"+!pedido.getFechaMaximaEntrega().isBefore(rutaDirecta.getFirst().getFechaLlegada()));
            System.out.println("Porque el pedido es: "+pedido.getFechaMaximaEntrega()+" y la ruta directa el primero su fecha de llegada es: "+rutaDirecta.getFirst().getFechaLlegada());
            if(camion.estaDisponibleEnRango(rutaDirecta.getFirst().getFechaPartida(), rutaDirecta.getLast().getFechaLlegada()) && !pedido.getFechaMaximaEntrega().isBefore(rutaDirecta.getFirst().getFechaLlegada())){
                System.out.println("Entro aquí a ruta directa");
                return rutaDirecta;
            }
        
        } 
        // Opción 2: Ruta con Recarga en Planta (UbicacionCamión -> Planta -> Pedido -> Planta Principal)
        for (Planta planta : plantas) {
            List<SubRuta> rutaConRecarga = Arrays.asList(
                new SubRuta(camion.getUbicacion(), planta.getUbicacion(), null),
                new SubRuta(planta.getUbicacion(), pedido.getUbicacion(), pedido),
                new SubRuta(pedido.getUbicacion(), plantas.get(0).getUbicacion(), null)
            );
            
            segundosRetorno = verificarRutaSimulacion(camion, pedido, plantas, rutaConRecarga, pedidosParaAsignar, fechaSimulada);
           // System.out.println("En la ruta con retorno, los segundos retorno dieron: "+segundosRetorno+" cuando la planta era la "+planta.getId());
            if (segundosRetorno != -1) {
                AsignarFechasYTrayectoriaSubRutas(rutaConRecarga, pedidosParaAsignar, plantas, camion, segundosRetorno, fechaSimulada);
                if(camion.estaDisponibleEnRango(rutaConRecarga.getFirst().getFechaPartida(), 
                rutaConRecarga.getLast().getFechaLlegada()) && 
                !pedido.getFechaMaximaEntrega().isBefore(rutaConRecarga.getFirst().getFechaLlegada())) {
                    System.out.println("Entro aquí a ruta con recarga");
                    return rutaConRecarga;
                }
            }
        }
        return null;
    }

    private void AsignarFechasYTrayectoriaSubRutas(List<SubRuta> RutaCompleta, List<Pedido> pedidos, List<Planta> plantas, Camion camion, int segundosRetorno, LocalDateTime fechaSimulada) {
        System.out.println("La fecha sumulada es: "+ fechaSimulada);
        LocalDateTime fechaSimuladaTemp = fechaSimulada.plusSeconds(segundosRetorno);
         System.out.println("La fecha simuladaTemp es: "+ fechaSimuladaTemp);
        for(SubRuta subRuta: RutaCompleta){
            List<Nodo> trayectoria = subRuta.getTrayectoria();
            subRuta.setTrayectoria(trayectoria);
            //double distancia = Utilidades.calcularDistanciaDeTrayectoria(trayectoria);

            //double tiempoAntesDePartir = 0;
            int tiempoTrayectoria = (trayectoria.size()-1)*72;

            subRuta.setFechaPartida(fechaSimuladaTemp);

            fechaSimuladaTemp = fechaSimuladaTemp.plusSeconds((long) tiempoTrayectoria);

            subRuta.setFechaLlegada(fechaSimuladaTemp);

            System.out.println("La SubRuta, su fecha de partida es: "+subRuta.getFechaPartida()+" y de llegada es: "+subRuta.getFechaLlegada());

            if(Utilidades.esPedido(subRuta.getUbicacionFin(), pedidos) || Utilidades.esPlanta(subRuta.getUbicacionFin(), plantas)){
                fechaSimuladaTemp = fechaSimuladaTemp.plusMinutes(15);
            }
        }
       }


    private int verificarRutaSimulacion(Camion camion, Pedido pedido, List<Planta> plantas, List<SubRuta> subRutas, List<Pedido> pedidos, LocalDateTime fechaSimulada) {
        Camion camionSim = camion.crearCopiaSimulacion();
        List<Planta> plantasSim = clonarListaPlantasSimulacion(plantas);
        
        int segundosRetorno = -1;
        
        List<Nodo> trayectoria = new ArrayList<>();
        Map.Entry<List<Nodo>,Integer> resultado = null;
        int segundos = 0;
        Nodo posicionActual = camionSim.getUbicacion();
        segundos = 0;
        int segundosMinimos = 14401;
        LocalDateTime fechaSimuladaProvisional = fechaSimulada;
        

        Boolean existeAsignacion = false;
        Double distanciaCompleta = 0.0;
        int contadorWhiles = 0;
        

        
        for (SubRuta subRuta : subRutas) {
            int paradas15min = 0;
            trayectoria = new ArrayList<>();
            resultado = null;

            //Estamos generando la SubRuta que seguirá el camión
            resultado = subRuta.generarTrayectoria(grid,fechaSimuladaProvisional, Utilidades.fechaMaximaTrayectoria(subRutas,subRuta));
            trayectoria = resultado.getKey();
            segundos = resultado.getValue();

            //Asignamos la trayectoria generada a la SubRuta
            subRuta.setTrayectoria(trayectoria);
            // Seguimos obteniendo el menor tiempo posible en el cual la trayectoria no realiza ningún cambio(Osea en ese tiempo no hay ningún bloqueo que afecte esa ruta.)
            if(segundos<segundosMinimos){
                segundosMinimos = segundos;
            }

            //Aquí obtenemos la distancia en km que recorrerá el camión en esta subRuta.
            double distancia = trayectoria.size() -1;
            distanciaCompleta += distancia;

            double consumo = camionSim.calcularConsumo(distancia);
            //Aqui verificamos combustible, si aún le queda para recorrer esta subruta.
            if (camionSim.getGlpTanqueRest() < consumo) {
                return -1;
            }
            // Actualizamos simulación
            camionSim.setGlpTanqueRest(camionSim.getGlpTanqueRest() - consumo);

            //Como vemos que si se puede recorrer la SubRuta. Veremos después de cuanto tiempo saldrá para la siguiente SubRuta.
            // Los posibles finales de la SubRuta son que: Que sea pedido, plantaPrincipal, planta secundaria o camión.

            if(subRuta.getPedido()!=null){
                paradas15min++;
                if(camionSim.getGlpCargaRest() < pedido.getCantidadGlp()){
                    return -1;
                }
            } else if (Utilidades.esPlantaPrincipal(subRuta.getUbicacionFin(), plantasSim) && subRuta!=subRutas.getLast()){
                paradas15min++;
                camionSim.setGlpTanqueRest(camionSim.getGlpTanque());
                camionSim.setGlpCargaRest(camionSim.getGlpCarga());
            } else if(Utilidades.esPlantaSecundaria(subRuta.getUbicacionFin(), plantasSim)){
                camionSim.setGlpTanqueRest(camionSim.getGlpTanque());
                camionSim.setGlpCargaRest(camionSim.getGlpCarga());
                
                Planta planta = Utilidades.obtenerPlanta(subRuta.getUbicacionFin(), plantasSim);
                if (planta.getGlpRest() < pedido.getCantidadGlp()) {
                    return -1;
                }
                planta.setGlpRest(planta.getGlpRest() - pedido.getCantidadGlp());
            } 

            //Vemos cuando tiempo será la siguiente fechaSimuladaProvisional para cuando llegué al final de la SubRuta.
            fechaSimuladaProvisional = fechaSimuladaProvisional.plusSeconds((long) distancia*72);


            // Como hasta ahora en este metodo VerificarRutaSimulación estamos viendo solo 1 pedido, esto solo se hará 1 vez.
            if (subRuta.getPedido() != null) {
                if(fechaSimuladaProvisional.isBefore(pedido.getFechaRegistro().plusHours(4))){
                    existeAsignacion = true;
                    if(segundosMinimos>=14401){ //Osea no hay bloqueos
                        segundosRetorno = (int) Duration.between(fechaSimulada, pedido.getFechaRegistro().plusHours(4)).toSeconds() - (int)(distanciaCompleta*72+(paradas15min-1)*900);
                    } else {
                        //Aquí tengo que colocar el codigo si el camino cambia antes de las 4horas.
                        //Tengo que ver que si cambia despues que pase por ese normal, pero si cambia antes, hay un problema.
                    }  
                } 
                
            }

            //Agregamos las paradas de 15min para salir en la siguiente SubRuta.
            fechaSimuladaProvisional = fechaSimuladaProvisional.plusSeconds((long) paradas15min*900);
        }

        return segundosRetorno;
    }
    private List<Planta> clonarListaPlantasSimulacion(List<Planta> originales) {
        List<Planta> copia = new ArrayList<>();
        for (Planta p : originales) {
            Planta clon = new Planta(p.getTipo(), 
                                new Nodo(p.getUbicacion().getPosX(), p.getUbicacion().getPosY())); // Copia del nodo
            
            clon.setGlpMaxima(p.getGlpMaxima());
            clon.setGlpRest(p.getGlpRest());
            clon.setGlpRestSim(p.getGlpRestSim());
            copia.add(clon);
        }
        return copia;
    }

    public void confirmarAsignacionSimulada(Asignacion asignacion, Pedido pedido, List<Planta> plantasReales) {
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
    static void cargarArchivos() throws IOException {
        //Cargamos plantas
        Planta plantaPrincipal = new Planta("PRINCIPAL", new Nodo(0, 0));
        Planta plantaSecundaria1 = new Planta("SECUNDARIA", new Nodo(5, 5));
        Planta plantaSecundaria2 = new Planta("SECUNDARIA", new Nodo(10, 10));
        plantas.add(plantaPrincipal);
        plantas.add(plantaSecundaria1);
        plantas.add(plantaSecundaria2);

        //Cargamos pedidos
        pedidos = cargarPedidos("..\\data\\pedidos.txt");
        numPedidosTotales = pedidos.size();

        //Cargamos camiones
        camiones = cargarCamiones("..\\data\\camiones.txt",plantaPrincipal.getUbicacion());
        
        //Cargamos Bloqueos en los nodos del grid.
        cargarBloqueos("..\\data\\bloqueos.txt", grid);

        //Cargamos mantenimientos en la lista de mantenimiento de los camiones.
        cargaMantenimientos("..\\data\\mantenimiento.txt",camiones);
    }



    static List<Pedido> cargarPedidos(String archivo) throws IOException {
        List<Pedido> pedidos = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(archivo));
        String linea;

        while ((linea = br.readLine()) != null) {
           // System.out.println("Ingreso aqui es:"+linea);
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

