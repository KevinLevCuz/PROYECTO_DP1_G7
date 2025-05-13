package Algoritmos.SA2;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Solucion {
    private ArrayList<PlanCamion> planesCamion;
    private double costo;

    public Solucion() {
        this.planesCamion = new ArrayList<>();
    }

    public Solucion(ArrayList<PlanCamion> planesCamion, double costo) {
        this.planesCamion = new ArrayList<>(planesCamion);
        this.costo = costo;
    }

    public ArrayList<PlanCamion> getPlanesCamion() {
        return planesCamion;
    }

    public void setPlanesCamion(ArrayList<PlanCamion> planesCamion) {
        this.planesCamion = planesCamion;
    }

    public double getCosto() {
        return costo;
    }

    public void setCosto(double costo) {
        this.costo = costo;
    }

    public void addPlanCamion(PlanCamion pc) {
        this.planesCamion.add(pc);
    }

    public void imprimirRutas() {
        for (PlanCamion pc : planesCamion) {
            System.out.println("Camión " + pc.getCamion().getCodigo() + ":");
            for (SubRuta sr : pc.getSubRutas()) {
                // Si tiene pedido, muestro su ID; si no, etiqueto como “Recarga/Retorno”
                String etiqueta = (sr.getPedido() != null)
                        ? "Pedido " + sr.getPedido().getId()
                        : "Recarga/Retorno";
                System.out.printf("  %s: de %s a %s%n",
                        etiqueta,
                        sr.getHoraInicio(),
                        sr.getHoraFin());
                System.out.println("    Trayectoria:");
                for (Nodo n : sr.getTrayectoria()) {
                    System.out.printf("      (%d,%d)%n", n.getPosX(), n.getPosY());
                }
            }
            System.out.println();
        }
    }

}
