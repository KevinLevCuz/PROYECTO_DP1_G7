import java.time.LocalDateTime;

public class Pedido {
    private static int contadorId = 1;
    private int id;
    private Nodo ubicacion; 
    private Ruta ruta;
    private String idCliente;
    private double cantidadGlp;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaMaximaEntrega;
    private double plazoHorasMaximo;
    private boolean entregado;

    public Pedido(Nodo ubicacion, Ruta ruta, String idCliente, double cantidadGlp, LocalDateTime fechaRegistro, double plazoHorasMaximo) {
        this.id = contadorId++;
        this.ubicacion = ubicacion;
        this.ruta = ruta;
        this.idCliente = idCliente;
        this.cantidadGlp = cantidadGlp;
        this.entregado=false;
        this.fechaRegistro = fechaRegistro;
        this.plazoHorasMaximo = plazoHorasMaximo;
        this.fechaMaximaEntrega = sumarHoras(fechaRegistro, plazoHorasMaximo);
    }

    public LocalDateTime sumarHoras(LocalDateTime fechaHora, double horas){
        long horasEnteras = (long) horas;
        long minutos = Math.round((horas - horasEnteras) * 60);
        return fechaHora.plusHours(horasEnteras).plusMinutes(minutos);
    }

    public int getId() {
        return id;
    }
    public Nodo getUbicacion() {
        return ubicacion;
    }
    public void setUbicacion(Nodo ubicacion) {
        this.ubicacion = ubicacion;
    }
    public Ruta getRuta() {
        return ruta;
    }
    public void setRuta(Ruta ruta) {
        this.ruta = ruta;
    }
    public String getIdCliente() {
        return idCliente;
    }
    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }
    public double getCantidadGlp() {
        return cantidadGlp;
    }
    public void setCantidadGlp(double cantidadGlp) {
        this.cantidadGlp = cantidadGlp;
    }
    public boolean isEntregado() {
        return entregado;
    }
    public void setEntregado(boolean entregado) {
        this.entregado = entregado;
    }
    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }
    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
    public LocalDateTime getFechaMaximaEntrega() {
        return fechaMaximaEntrega;
    }
    public void setFechaMaximaEntrega(LocalDateTime fechaMaximaEntrega) {
        this.fechaMaximaEntrega = fechaMaximaEntrega;
    }
    public double getPlazoHorasMaximo() {
        return plazoHorasMaximo;
    }
    public void setPlazoHorasMaximo(double plazoHorasMaximo) {
        this.plazoHorasMaximo = plazoHorasMaximo;
    }
}
