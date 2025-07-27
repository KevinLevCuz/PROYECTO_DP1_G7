package com.dp1code.routing.Service;

import com.dp1code.routing.dto.SubrutaDTO;
import org.springframework.stereotype.Service;

@Service
public class SubrutaService {

    public void registrarSubRuta(SubrutaDTO dto) {
        // Aquí harías la lógica de conversión a entidad y persistencia si corresponde
        System.out.println("SubRuta registrada:");
        System.out.println("Inicio: (" + dto.getInicioPosX() + "," + dto.getInicioPosY() + ")");
        System.out.println("Fin: (" + dto.getFinPosX() + "," + dto.getFinPosY() + ")");
        System.out.println("Pedido ID: " + dto.getPedidoId());
        System.out.println("Hora Inicio: " + dto.getHoraInicio());
        System.out.println("Hora Fin: " + dto.getHoraFin());
        System.out.println("Nodos en trayectoria: " + dto.getTrayectoria().size());
    }
}
