package com.dp1code.routing.Service;

import com.dp1code.routing.dto.TimeRangeDTO;
import org.springframework.stereotype.Service;

@Service
public class TimeRangeService {

    public void registrarTimeRange(TimeRangeDTO dto) {
        System.out.println("TimeRange registrado:");
        System.out.println("Inicio: " + dto.getStart());
        System.out.println("Fin: " + dto.getEnd());
        // Aquí podrías guardar en la BD si corresponde
    }
}
