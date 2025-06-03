package com.dp1code.routing.Model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.dp1code.routing.Model.PlanCamion;

public class SimuladorDinamico {
    private final ArrayList<PlanCamion> planes;
    private final ArrayList<Planta> plantas;
    private final ArrayList<Bloqueo> bloqueos;
    private final ArrayList<Mantenimiento> mantenimientos;
    private LocalDateTime relojGlobal;
    private ScheduledExecutorService ticker;

    /**
     * @param planes          Lista de planes (un PlanCamion por cada camión).
     * @param inicio          Hora de inicio del reloj simulado.
     * @param plantas         Lista de plantas (para recargas).
     * @param bloqueos        Lista de bloqueos (para evitar nodos bloqueados).
     * @param mantenimientos  Lista de mantenimientos (para penalizar o bloquear camiones).
     */
    public SimuladorDinamico(
            ArrayList<PlanCamion> planes,
            LocalDateTime inicio,
            ArrayList<Planta> plantas,
            ArrayList<Bloqueo> bloqueos,
            ArrayList<Mantenimiento> mantenimientos
    ) {
        this.planes = planes;
        this.relojGlobal = inicio;
        this.plantas = plantas;
        this.bloqueos = bloqueos;
        this.mantenimientos = mantenimientos;

        // Creamos un scheduler que cada 1 segundo real llame a avanzarUnMinutoSimulado()
        this.ticker = Executors.newSingleThreadScheduledExecutor();
        this.ticker.scheduleAtFixedRate(this::avanzarUnMinutoSimulado, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * Cada vez que se invoque, avanza el “relojGlobal” 1 minuto
     * y luego hace que cada PlanCamion se actualice hasta ese instante.
     */
    private void avanzarUnMinutoSimulado() {
        // 1) aumentamos el reloj en 1 minuto
        relojGlobal = relojGlobal.plusMinutes(1);

        // 2) actualizamos cada PlanCamion hasta este nuevo relojGlobal
        for (PlanCamion plan : planes) {
            // Construimos aquí un SimulatedAnnealing con las listas correctas
            SimulatedAnnealing sa = new SimulatedAnnealing(
                /* initialTemp    */ 0.0,
                /* coolingRate    */ 0.0,
                /* maxIterations  */ 0,
                /* plantas        */ plantas,
                /* bloqueos       */ bloqueos,
                /* mantenimientos */ mantenimientos
            );
            sa.actualizarCamionHasta(plan, relojGlobal);
        }
    }

    /** Devuelve la lista de planes ya “avanzados” al instante actual. */
    public List<PlanCamion> getPlanesActualizados() {
        return planes;
    }

    /** Devuelve el reloj simulado en este instante. */
    public LocalDateTime getRelojGlobal() {
        return relojGlobal;
    }

    /** Permite detener el scheduler cuando ya no sea necesario. */
    public void detener() {
        ticker.shutdownNow();
    }
}
