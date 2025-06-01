import java.time.LocalDateTime;
import java.util.List;

public class Asignacion{
    private Camion camion;
    private List<SubRuta> subRutas;
    private LocalDateTime fechaPartida;

    public Asignacion(){}
    public Asignacion(Camion camion, List<SubRuta> subRutas, LocalDateTime fechaPartida){
        this.camion = camion;
        this.subRutas = subRutas;
        this.fechaPartida = fechaPartida;
    }

    public Camion getCamion() {
        return camion;
    }
    public void setCamion(Camion camion) {
        this.camion = camion;
    }
    public List<SubRuta> getSubRutas() {
        return subRutas;
    }
    public void setSubrutas(List<SubRuta> subRutas) {
        this.subRutas = subRutas;
    }
    public LocalDateTime getFechaPartida() {
        return fechaPartida;
    }
    public void setFechaPartida(LocalDateTime fechaPartida) {
        this.fechaPartida = fechaPartida;
    }

}
