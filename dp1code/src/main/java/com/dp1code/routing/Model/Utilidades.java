package com.dp1code.routing.Model;

import java.util.ArrayList;
import java.util.List;

public class Utilidades {
    public static List<Nodo> obtenerNodosIntermedios(int x1, int y1, int x2, int y2, Grid grid) {
        List<Nodo> nodos = new ArrayList<>();
        if (x1 == x2) {
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
                Nodo nodo = grid.getNodoAt(x1, y);
                if (nodo != null) {
                    nodos.add(nodo);
                }
            }
        } else if (y1 == y2) {
            for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
                Nodo nodo = grid.getNodoAt(x, y1);
                if (nodo != null) {
                    nodos.add(nodo);
                }
            }
        } else {
            // Si no es línea recta, puedes lanzar un warning o permitir diagonales si lo deseas
            System.err.println("Advertencia: No se admite bloqueo diagonal entre (" + x1 + "," + y1 + ") y (" + x2 + "," + y2 + ")");
        }
        return nodos;
    }


    public static boolean esPlantaPrincipal(Nodo nodo, List<Planta> plantas) {
        for(Planta planta : plantas) {
           if(planta.getUbicacion().getPosX() == nodo.getPosX() && planta.getUbicacion().getPosY() == nodo.getPosY() && planta.getTipo().equals("PRINCIPAL")) return true;
        }
        return false;
    }
    public static boolean esPlantaSecundaria(Nodo nodo, List<Planta> plantas) {
        for(Planta planta : plantas) {
           if(planta.getUbicacion().getPosX() == nodo.getPosX() && planta.getUbicacion().getPosY() == nodo.getPosY() && planta.getTipo().equals("SECUNDARIO")) return true;
        }
        return false;
    }


    public static Planta obtenerPlanta(Nodo nodo, List<Planta> plantas) {
        for(Planta planta: plantas){
            if(planta.getUbicacion().getPosX()== nodo.getPosX() && planta.getUbicacion().getPosY() == nodo.getPosY()){
                return planta;
            } 
        }
        return null;
    }
}
