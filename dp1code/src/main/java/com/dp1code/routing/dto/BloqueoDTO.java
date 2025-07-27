package com.dp1code.routing.dto;

import java.time.LocalDateTime;
import java.util.List;

public class BloqueoDTO {
    private List<Integer> nodoIds;
    private LocalDateTime inicio;
    private LocalDateTime fin;

    public List<Integer> getNodoIds() { return nodoIds; }
    public void setNodoIds(List<Integer> nodoIds) { this.nodoIds = nodoIds; }

    public LocalDateTime getInicio() { return inicio; }
    public void setInicio(LocalDateTime inicio) { this.inicio = inicio; }

    public LocalDateTime getFin() { return fin; }
    public void setFin(LocalDateTime fin) { this.fin = fin; }
}
