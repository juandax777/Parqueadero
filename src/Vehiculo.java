import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

public class Vehiculo implements Serializable {
    private String placa;
    private LocalDateTime horaEntrada;
    private LocalDateTime horaSalida;
    private int ticketNumero;

    public Vehiculo(String placa, LocalDateTime horaEntrada, int ticketNumero) {
        this.placa = placa;
        this.horaEntrada = horaEntrada;
        this.ticketNumero = ticketNumero;
        this.horaSalida = null;
    }

    public String getPlaca() {
        return placa;
    }

    public LocalDateTime getHoraEntrada() {
        return horaEntrada;
    }

    public LocalDateTime getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(LocalDateTime horaSalida) {
        this.horaSalida = horaSalida;
    }

    public int getTicketNumero() {
        return ticketNumero;
    }

    public long calcularTiempoEstadia() {
        if (horaSalida != null) {
            return Duration.between(horaEntrada, horaSalida).toMinutes();
        } else {
            return Duration.between(horaEntrada, LocalDateTime.now()).toMinutes();
        }
    }

    public long calcularCosto() {
        long minutos = calcularTiempoEstadia();
        return minutos * 100; // Ejemplo: 100 pesos por minuto
    }

    // Método para actualizar la placa
    public void setPlaca(String placa) {
        this.placa = placa;
    }
}
