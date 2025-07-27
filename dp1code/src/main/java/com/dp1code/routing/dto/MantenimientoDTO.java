package com.dp1code.routing.dto;

import java.time.LocalDateTime;

public class MantenimientoDTO {
    private LocalDateTime inicio;
    private LocalDateTime fin;
    private String codigoCamion;
    private String tipo;

    public MantenimientoDTO() {}

    public MantenimientoDTO(LocalDateTime inicio, LocalDateTime fin, String codigoCamion, String tipo) {
        this.inicio = inicio;
        this.fin = fin;
        this.codigoCamion = codigoCamion;
        this.tipo = tipo;
    }

    public LocalDateTime getInicio() { return inicio; }
    public void setInicio(LocalDateTime inicio) { this.inicio = inicio; }

    public LocalDateTime getFin() { return fin; }
    public void setFin(LocalDateTime fin) { this.fin = fin; }

    public String getCodigoCamion() { return codigoCamion; }
    public void setCodigoCamion(String codigoCamion) { this.codigoCamion = codigoCamion; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}
