package Algoritmos.SA2;

import java.util.ArrayList;
import java.util.List;

public class PlanCamion {
    private Camion camion;
    private ArrayList<SubRuta> subRutas;

    public PlanCamion() {
        this.subRutas = new ArrayList<>();
    }

    public PlanCamion(Camion camion, ArrayList<SubRuta> subRutas) {
        this.camion = camion;
        this.subRutas = new ArrayList<>(subRutas);
    }

    public Camion getCamion() { return camion; }
    public void setCamion(Camion camion) { this.camion = camion; }

    public ArrayList<SubRuta> getSubRutas() { return subRutas; }
    public void setSubRutas(ArrayList<SubRuta> subRutas) { this.subRutas = subRutas; }

    public void addSubRuta(SubRuta sr) { this.subRutas.add(sr); }
}