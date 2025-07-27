package com.dp1code.routing.Model;

import java.util.ArrayList;

public class Simulacion {
    private ArrayList<Solucion> soluciones;

    public ArrayList<Solucion> getSoluciones() {
        return soluciones;
    }

    public void setSoluciones(ArrayList<Solucion> soluciones) {
        this.soluciones = soluciones;
    }

    public Simulacion(ArrayList<Solucion> soluciones) {
        this.soluciones = soluciones;
    }
}
