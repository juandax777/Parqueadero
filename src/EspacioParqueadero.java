import java.io.Serializable;

public class EspacioParqueadero implements Serializable {
    private int numeroEspacio;
    private Vehiculo vehiculo;

    public EspacioParqueadero(int numeroEspacio) {
        this.numeroEspacio = numeroEspacio;
        this.vehiculo = null;
    }

    public int getNumeroEspacio() {
        return numeroEspacio;
    }

    public boolean estaOcupado() {
        return vehiculo != null;
    }

    public void ocuparEspacio(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
    }

    public void liberarEspacio() {
        this.vehiculo = null;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }
}
