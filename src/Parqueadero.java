import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Parqueadero {
    private List<EspacioParqueadero> espacios;
    private int ultimoTicketNumero;

    public Parqueadero() {
        this.espacios = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            this.espacios.add(new EspacioParqueadero(i));
        }
        this.ultimoTicketNumero = 0; // Inicializa el contador de tickets
    }

    public EspacioParqueadero asignarEspacio(Vehiculo vehiculo) {
        for (EspacioParqueadero espacio : espacios) {
            if (!espacio.estaOcupado()) {
                espacio.ocuparEspacio(vehiculo);
                return espacio;
            }
        }
        return null;
    }

    public int generarNuevoTicket() {
        return ++ultimoTicketNumero;
    }

    public List<EspacioParqueadero> obtenerEspacios() {
        return espacios;
    }

    // Guardar datos en un archivo CSV
    public void guardarDatos(String archivo) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(archivo))) {
            // Escribir encabezado
            writer.println("numeroEspacio,placa,horaEntrada,horaSalida,ticketNumero");

            // Escribir datos de cada espacio
            for (EspacioParqueadero espacio : espacios) {
                Vehiculo vehiculo = espacio.getVehiculo();
                if (vehiculo != null) {
                    writer.println(espacio.getNumeroEspacio() + "," +
                            vehiculo.getPlaca() + "," +
                            vehiculo.getHoraEntrada() + "," +
                            (vehiculo.getHoraSalida() != null ? vehiculo.getHoraSalida() : "") + "," +
                            vehiculo.getTicketNumero());
                } else {
                    writer.println(espacio.getNumeroEspacio() + ",,,,");
                }
            }
        }
    }

    // Cargar datos desde un archivo CSV
    public static Parqueadero cargarDatos(String archivo) throws IOException {
        Parqueadero parqueadero = new Parqueadero();
        List<EspacioParqueadero> espaciosCargados = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea = reader.readLine(); // Leer el encabezado

            while ((linea = reader.readLine()) != null) {
                if (linea.trim().isEmpty()) {
                    continue; // Ignorar líneas vacías
                }

                String[] datos = linea.split(",");

                // Validar si es una línea de un espacio vacío
                int numeroEspacio = Integer.parseInt(datos[0]);
                EspacioParqueadero espacio = new EspacioParqueadero(numeroEspacio);

                if (datos.length >= 2 && !datos[1].isEmpty()) {
                    String placa = datos[1];
                    LocalDateTime horaEntrada = datos[2].isEmpty() ? null : LocalDateTime.parse(datos[2]);
                    LocalDateTime horaSalida = datos[3].isEmpty() ? null : LocalDateTime.parse(datos[3]);
                    int ticketNumero = datos[4].isEmpty() ? 0 : Integer.parseInt(datos[4]);

                    Vehiculo vehiculo = new Vehiculo(placa, horaEntrada, ticketNumero);
                    vehiculo.setHoraSalida(horaSalida);
                    espacio.ocuparEspacio(vehiculo);

                    // Actualizar el último número de ticket
                    if (ticketNumero > parqueadero.ultimoTicketNumero) {
                        parqueadero.ultimoTicketNumero = ticketNumero;
                    }
                }
                espaciosCargados.add(espacio);
            }
        }

        // Integrar los espacios cargados con los vacíos que faltan
        for (int i = 1; i <= 20; i++) {
            boolean encontrado = false;
            for (EspacioParqueadero espacio : espaciosCargados) {
                if (espacio.getNumeroEspacio() == i) {
                    parqueadero.espacios.set(i - 1, espacio);
                    encontrado = true;
                    break;
                }
            }
            if (!encontrado) {
                // Si el espacio no fue cargado, se deja como vacío
                parqueadero.espacios.set(i - 1, new EspacioParqueadero(i));
            }
        }

        return parqueadero;
    }
}
