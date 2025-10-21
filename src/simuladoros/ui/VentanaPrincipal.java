/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladoros.ui;
import javax.swing.*;
import java.awt.*;
import simuladoros.core.ciclolistener;
import simuladoros.core.Kernel;
import simuladoros.core.Proceso;
import simuladoros.core.TipoProceso;

/**
 *
 * @author user
 */
public class VentanaPrincipal  extends JFrame{
 private final Kernel kernel = new Kernel();

    private JLabel lblTitulo;
    private JLabel lblCiclo;
    private JButton btnIniciar;
    private JButton btnPausa;
    private JSpinner spDuracion;

    private JButton btnNuevoProceso;
    private JList<String> listaListos;
    private JList<String> listaTerminados;
    private JLabel lblEjecucion;

    public VentanaPrincipal() {
        initUI();
        enlazarEventos();
        refrescarListas();
    }

    private void initUI() {
        setTitle("Simulador de Sistema Operivo - FCFS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 560);
        setLocationRelativeTo(null);

        lblTitulo = new JLabel("Simulador de Planificación FCFS", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));

        lblCiclo = new JLabel("Ciclo actual: 0", SwingConstants.CENTER);
        lblCiclo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        btnIniciar = new JButton("Iniciar");
        btnPausa  = new JButton("Pausar");
        btnPausa.setEnabled(false);

        spDuracion = new JSpinner(new SpinnerNumberModel(500, 10, 5000, 10));
        JLabel lblMs = new JLabel("ms/ciclo");

        btnNuevoProceso = new JButton("Nuevo Proceso");
        listaListos = new JList<>();
        listaTerminados = new JList<>();
        lblEjecucion = new JLabel("CPU: [sin proceso]", SwingConstants.CENTER);
        lblEjecucion.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JPanel panelControl = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelControl.add(new JLabel("Duración:"));
        panelControl.add(spDuracion);
        panelControl.add(lblMs);
        panelControl.add(btnIniciar);
        panelControl.add(btnPausa);
        panelControl.add(Box.createHorizontalStrut(20));
        panelControl.add(btnNuevoProceso);

        // Panel central con dos columnas: Listos y Terminados
        JPanel centro = new JPanel(new BorderLayout(10,10));

        JPanel superior = new JPanel();
        superior.setLayout(new BoxLayout(superior, BoxLayout.Y_AXIS));
        lblCiclo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblEjecucion.setAlignmentX(Component.CENTER_ALIGNMENT);
        superior.add(Box.createVerticalStrut(10));
        superior.add(lblCiclo);
        superior.add(Box.createVerticalStrut(10));
        superior.add(lblEjecucion);
        superior.add(Box.createVerticalStrut(10));

        JPanel columnas = new JPanel(new GridLayout(1, 2, 10, 10));
        JPanel colListos = new JPanel(new BorderLayout());
        colListos.add(new JLabel("Cola de Listos", SwingConstants.CENTER), BorderLayout.NORTH);
        colListos.add(new JScrollPane(listaListos), BorderLayout.CENTER);

        JPanel colTerminados = new JPanel(new BorderLayout());
        colTerminados.add(new JLabel("Procesos Terminados", SwingConstants.CENTER), BorderLayout.NORTH);
        colTerminados.add(new JScrollPane(listaTerminados), BorderLayout.CENTER);

        columnas.add(colListos);
        columnas.add(colTerminados);

        centro.add(superior, BorderLayout.NORTH);
        centro.add(columnas, BorderLayout.CENTER);

        setLayout(new BorderLayout(10,10));
        add(lblTitulo, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(panelControl, BorderLayout.SOUTH);
    }

    private void enlazarEventos() {
        kernel.setCicloListener((numeroCiclo, ts) ->
                SwingUtilities.invokeLater(() -> {
                    lblCiclo.setText("Ciclo actual: " + numeroCiclo);
                    actualizarVistaCPU();
                    refrescarListas();
                })
        );

        btnIniciar.addActionListener(e -> {
            kernel.setDuracionCiclo((Integer) spDuracion.getValue());
            kernel.iniciar();
            btnIniciar.setEnabled(false);
            btnPausa.setEnabled(true);
        });

        btnPausa.addActionListener(e -> {
            kernel.pausarOContinuar();
            if (btnPausa.getText().equals("Pausar")) btnPausa.setText("Continuar");
            else btnPausa.setText("Pausar");
        });

        spDuracion.addChangeListener(e ->
                kernel.setDuracionCiclo((Integer) spDuracion.getValue())
        );

        btnNuevoProceso.addActionListener(e -> crearProcesoDesdeDialogo());
    }

    private void crearProcesoDesdeDialogo() {
        String nombre = JOptionPane.showInputDialog(this, "Nombre del proceso:", "Nuevo Proceso", JOptionPane.QUESTION_MESSAGE);
        if (nombre == null || nombre.trim().isEmpty()) return;

        Object opcion = JOptionPane.showInputDialog(
                this,
                "Tipo de proceso:",
                "Nuevo Proceso",
                JOptionPane.QUESTION_MESSAGE,
                null,
                new Object[]{"CPU_BOUND", "IO_BOUND"},
                "CPU_BOUND"
        );
        if (opcion == null) return;
        TipoProceso tipo = "IO_BOUND".equals(opcion.toString()) ? TipoProceso.IO_BOUND : TipoProceso.CPU_BOUND;

        String totalStr = JOptionPane.showInputDialog(this, "Total de instrucciones:", "50");
        if (totalStr == null) return;
        int total;
        try {
            total = Math.max(1, Integer.parseInt(totalStr.trim()));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Valor inválido. Intenta de nuevo.");
            return;
        }

        kernel.crearProceso(nombre.trim(), tipo, total);
        refrescarListas();
    }

    private void actualizarVistaCPU() {
        Proceso actual = kernel.getProcesoActual();
        if (actual != null) {
            lblEjecucion.setText("CPU: " + actual.getNombre()
                    + " (" + actual.getRestantes() + "/" + actual.getTotalInstrucciones() + ")");
        } else {
            lblEjecucion.setText("CPU: [sin proceso]");
        }
    }

    private void refrescarListas() {
        // Listos
        Proceso[] listos = kernel.snapshotListos();
        String[] filasListos = new String[listos.length];
        for (int i = 0; i < listos.length; i++) {
            Proceso p = listos[i];
            filasListos[i] = String.format("PID %d | %-12s | %-9s | Estado: %s | %d/%d",
                    p.getPid(), p.getNombre(), p.getTipo(), p.getEstado(),
                    p.getRestantes(), p.getTotalInstrucciones());
        }
        listaListos.setListData(filasListos);

        // Terminados
        Proceso[] terms = kernel.snapshotTerminados();
        String[] filasTerm = new String[terms.length];
        for (int i = 0; i < terms.length; i++) {
            Proceso p = terms[i];
            filasTerm[i] = String.format("PID %d | %-12s | %-9s | Estado: %s | %d/%d",
                    p.getPid(), p.getNombre(), p.getTipo(), p.getEstado(),
                    p.getRestantes(), p.getTotalInstrucciones());
        }
        listaTerminados.setListData(filasTerm);
    }
}