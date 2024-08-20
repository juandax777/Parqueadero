import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

public class MainGUI extends JFrame {
    private Parqueadero parqueadero;
    private String archivoDatos = "parqueadero.csv";
    private JTextField placaField;
    private JTextField ticketField;  // Campo para el ticket
    private DefaultTableModel vehiculosModel;
    private DefaultTableModel espaciosModel;
    private JTable vehiculosTable;
    private JTable espaciosTable;

    public MainGUI() {
        // Cargar datos previos o crear nuevo archivo CSV
        File file = new File(archivoDatos);
        if (file.exists()) {
            try {
                parqueadero = Parqueadero.cargarDatos(archivoDatos);
                JOptionPane.showMessageDialog(this, "Datos cargados exitosamente.");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error al cargar datos. Iniciando nuevo parqueadero.");
                parqueadero = new Parqueadero();
            }
        } else {
            parqueadero = new Parqueadero();
        }

        // Configuración del frame
        setTitle("Sistema de Parqueadero");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Crear panel de entrada
        JPanel inputPanel = new JPanel(new GridLayout(3, 1));
        inputPanel.add(new JLabel("Ingrese el número de ticket:"));
        ticketField = new JTextField();
        inputPanel.add(ticketField);
        inputPanel.add(new JLabel("Ingrese la placa del vehículo:"));
        placaField = new JTextField();
        inputPanel.add(placaField);

        // Crear botones
        JButton btnGuardar = new JButton("Guardar");
        JButton btnPagar = new JButton("Pagar");
        JButton btnEditar = new JButton("Editar");
        JButton btnDetallada = new JButton("Detallada");

        // Crear un subpanel para los botones
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 5, 5)); // 4 filas, 1 columna, 5px de espacio vertical
        buttonPanel.add(btnGuardar);
        buttonPanel.add(btnPagar);
        buttonPanel.add(btnEditar);
        buttonPanel.add(btnDetallada);

        // Agregar ActionListeners
        btnGuardar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registrarIngreso();
            }
        });

        btnPagar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registrarSalida();
            }
        });

        btnEditar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editarPlaca();
            }
        });

        btnDetallada.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrarInformeDetallado();
            }
        });

        // Crear modelos de tablas no editables
        vehiculosModel = new DefaultTableModel(new String[]{"Ticket", "Placa", "Hora de Entrada", "Tiempo Transcurrido"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // No permitir la edición de celdas
            }
        };
        vehiculosTable = new JTable(vehiculosModel);
        vehiculosTable.getTableHeader().setReorderingAllowed(false); // Deshabilitar el movimiento de columnas
        JScrollPane vehiculosScrollPane = new JScrollPane(vehiculosTable);

        espaciosModel = new DefaultTableModel(new String[]{"Número de Espacio", "Estado", "Placa", "Ticket"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // No permitir la edición de celdas
            }
        };
        espaciosTable = new JTable(espaciosModel);
        espaciosTable.getTableHeader().setReorderingAllowed(false); // Deshabilitar el movimiento de columnas
        JScrollPane espaciosScrollPane = new JScrollPane(espaciosTable);

        // Agregar componentes al frame
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(inputPanel, BorderLayout.NORTH);
        leftPanel.add(buttonPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JLabel("Vehículos en el Parqueadero"), BorderLayout.NORTH);
        rightPanel.add(vehiculosScrollPane, BorderLayout.CENTER);
        rightPanel.add(new JLabel("Estado del Parqueadero"), BorderLayout.SOUTH);
        rightPanel.add(espaciosScrollPane, BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        // Manejo de la selección en vehiculosTable
        vehiculosTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && vehiculosTable.getSelectedRow() != -1) {
                String placaSeleccionada = (String) vehiculosTable.getValueAt(vehiculosTable.getSelectedRow(), 1);
                placaField.setText(placaSeleccionada);
            }
        });

        // Manejo de la selección en espaciosTable
        espaciosTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && espaciosTable.getSelectedRow() != -1) {
                String placaSeleccionada = (String) espaciosTable.getValueAt(espaciosTable.getSelectedRow(), 2);
                if (!"N/A".equals(placaSeleccionada)) {
                    placaField.setText(placaSeleccionada);
                }
            }
        });

        // Actualizar la placa según el ticket ingresado
        ticketField.addActionListener(e -> {
            String ticketStr = ticketField.getText();
            if (ticketStr != null && !ticketStr.trim().isEmpty()) {
                try {
                    int ticketNumero = Integer.parseInt(ticketStr);
                    for (EspacioParqueadero espacio : parqueadero.obtenerEspacios()) {
                        if (espacio.estaOcupado() && espacio.getVehiculo().getTicketNumero() == ticketNumero) {
                            placaField.setText(espacio.getVehiculo().getPlaca());
                            return;
                        }
                    }
                    JOptionPane.showMessageDialog(this, "No se encontró un vehículo con ese número de ticket.");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Número de ticket inválido.");
                }
            }
        });

        // Actualizar tablas periódicamente
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    actualizarTablaVehiculos();
                    actualizarTablaEspacios();
                });
            }
        }, 0, 1000);

        // Guardar datos al cerrar
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                try {
                    parqueadero.guardarDatos(archivoDatos);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void registrarIngreso() {
        String placa = placaField.getText();

        // Expresión regular para validar el formato de la placa colombiana
        String regex = "^[A-Z]{3}[0-9]{3}$";

        // Validar el formato de la placa
        if (!Pattern.matches(regex, placa.toUpperCase())) {
            JOptionPane.showMessageDialog(this, "Formato de placa incorrecto. Debe tener 3 letras seguidas de 3 números (ej. TRE243).");
            return;
        }

        if (placa != null && !placa.trim().isEmpty()) {
            LocalDateTime horaEntrada = LocalDateTime.now();
            int ticketNumero = parqueadero.generarNuevoTicket(); // Generar ticket incremental
            Vehiculo vehiculo = new Vehiculo(placa, horaEntrada, ticketNumero);
            EspacioParqueadero espacio = parqueadero.asignarEspacio(vehiculo);

            if (espacio != null) {
                JOptionPane.showMessageDialog(this, "Vehículo registrado exitosamente. Número de ticket: " + ticketNumero + ".\nEspacio asignado: " + espacio.getNumeroEspacio());
                actualizarTablaVehiculos();
                actualizarTablaEspacios();
            } else {
                JOptionPane.showMessageDialog(this, "No hay espacios disponibles en el parqueadero.");
            }
        }
    }

    private void registrarSalida() {
        String ticketStr = JOptionPane.showInputDialog(this, "Ingrese el número del ticket:");
        if (ticketStr != null && !ticketStr.trim().isEmpty()) {
            try {
                int ticketNumero = Integer.parseInt(ticketStr);
                EspacioParqueadero espacioEncontrado = null;
                for (EspacioParqueadero espacio : parqueadero.obtenerEspacios()) {
                    if (espacio.estaOcupado() && espacio.getVehiculo().getTicketNumero() == ticketNumero) {
                        espacioEncontrado = espacio;
                        break;
                    }
                }

                if (espacioEncontrado != null) {
                    Vehiculo vehiculo = espacioEncontrado.getVehiculo();
                    vehiculo.setHoraSalida(LocalDateTime.now());
                    long tiempoEstadia = vehiculo.calcularTiempoEstadia();
                    long costo = vehiculo.calcularCosto();

                    JOptionPane.showMessageDialog(this, "Vehículo con placa " + vehiculo.getPlaca() + " ha estado " + tiempoEstadia + " minutos.\nCosto total: " + costo + " pesos.\nGracias por su pago. Puede salir.");

                    espacioEncontrado.liberarEspacio();
                    actualizarTablaVehiculos();
                    actualizarTablaEspacios();
                } else {
                    JOptionPane.showMessageDialog(this, "No se encontró un vehículo con ese número de ticket.");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Número de ticket inválido.");
            }
        }
    }

    private void editarPlaca() {
        String placaNueva = placaField.getText();

        // Expresión regular para validar el formato de la placa colombiana
        String regex = "^[A-Z]{3}[0-9]{3}$";

        // Validar el formato de la nueva placa
        if (!Pattern.matches(regex, placaNueva.toUpperCase())) {
            JOptionPane.showMessageDialog(MainGUI.this, "Formato de placa incorrecto. Debe tener 3 letras seguidas de 3 números (ej. TRE243).");
            return;
        }

        String ticketStr = ticketField.getText();
        if (ticketStr != null && !ticketStr.trim().isEmpty()) {
            try {
                int ticketNumero = Integer.parseInt(ticketStr);
                for (EspacioParqueadero espacio : parqueadero.obtenerEspacios()) {
                    if (espacio.estaOcupado() && espacio.getVehiculo().getTicketNumero() == ticketNumero) {
                        espacio.getVehiculo().setPlaca(placaNueva);
                        actualizarTablaVehiculos();
                        actualizarTablaEspacios();
                        JOptionPane.showMessageDialog(MainGUI.this, "Placa editada exitosamente.");
                        return;
                    }
                }
                JOptionPane.showMessageDialog(MainGUI.this, "No se encontró un vehículo con ese número de ticket.");
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(MainGUI.this, "Número de ticket inválido.");
            }
        }
    }

    private void mostrarInformeDetallado() {
        StringBuilder informe = new StringBuilder();
        informe.append("--- Informe Detallado del Parqueadero ---\n");
        for (EspacioParqueadero espacio : parqueadero.obtenerEspacios()) {
            if (espacio.estaOcupado()) {
                Vehiculo vehiculo = espacio.getVehiculo();
                informe.append("Placa: ").append(vehiculo.getPlaca())
                        .append(", Ticket: ").append(vehiculo.getTicketNumero())
                        .append(", Hora de Entrada: ").append(vehiculo.getHoraEntrada())
                        .append(", Hora de Salida: ").append(vehiculo.getHoraSalida() != null ? vehiculo.getHoraSalida() : "N/A")
                        .append(", Tiempo Estadia: ").append(vehiculo.calcularTiempoEstadia())
                        .append(", Costo: ").append(vehiculo.calcularCosto()).append("\n");
            }
        }
        JOptionPane.showMessageDialog(this, informe.toString());
    }

    private void actualizarTablaVehiculos() {
        vehiculosModel.setRowCount(0);
        for (EspacioParqueadero espacio : parqueadero.obtenerEspacios()) {
            if (espacio.estaOcupado()) {
                Vehiculo vehiculo = espacio.getVehiculo();
                long tiempoTranscurrido = Duration.between(vehiculo.getHoraEntrada(), LocalDateTime.now()).toMinutes();
                vehiculosModel.addRow(new Object[]{vehiculo.getTicketNumero(), vehiculo.getPlaca(), vehiculo.getHoraEntrada(), tiempoTranscurrido});
            }
        }
    }

    private void actualizarTablaEspacios() {
        espaciosModel.setRowCount(0);
        for (EspacioParqueadero espacio : parqueadero.obtenerEspacios()) {
            String estado = espacio.estaOcupado() ? "Ocupado" : "Libre";
            String placa = espacio.estaOcupado() ? espacio.getVehiculo().getPlaca() : "N/A";
            String ticket = espacio.estaOcupado() ? String.valueOf(espacio.getVehiculo().getTicketNumero()) : "N/A";
            espaciosModel.addRow(new Object[]{espacio.getNumeroEspacio(), estado, placa, ticket});
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainGUI().setVisible(true);
            }
        });
    }
}
