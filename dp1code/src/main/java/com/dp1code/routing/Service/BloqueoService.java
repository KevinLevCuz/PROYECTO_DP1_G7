package com.dp1code.routing.Service;

import com.dp1code.routing.dto.BloqueoDTO;
import org.springframework.stereotype.Service;

@Service
public class BloqueoService {

    public void registrarBloqueo(BloqueoDTO bloqueo) {
        System.out.println("Bloqueo recibido:");
        System.out.println("Nodos: " + bloqueo.getNodoIds());
        System.out.println("Inicio: " + bloqueo.getInicio());
        System.out.println("Fin: " + bloqueo.getFin());
        // Aquí iría lógica de inserción a base de datos si se desea
    }
}
