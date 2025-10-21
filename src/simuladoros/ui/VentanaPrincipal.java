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
    private JLabel lblEjecucion;

    public VentanaPrincipal() {
        initUI();
        enlazarEventos();
        refrescarListaListos();
    }

    private void initUI() {
        setTitle("Simulador de Sistema Operativo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(720, 500);
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

        JPanel centro = new JPanel();
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        lblCiclo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblEjecucion.setAlignmentX(Component.CENTER_ALIGNMENT);
        centro.add(Box.createVerticalStrut(10));
        centro.add(lblCiclo);
        centro.add(Box.createVerticalStrut(10));
        centro.add(lblEjecucion);
        centro.add(Box.createVerticalStrut(10));
        centro.add(new JLabel("Cola de Listos:"));
        JScrollPane sp = new JScrollPane(listaListos);
        sp.setPreferredSize(new Dimension(650, 220));
        centro.add(sp);

        setLayout(new BorderLayout(10,10));
        add(lblTitulo, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(panelControl, BorderLayout.SOUTH);
    }

    private void enlazarEventos() {
        kernel.setCicloListener((numeroCiclo, ts) ->
                SwingUtilities.invokeLater(() -> {
                    lblCiclo.setText("Ciclo actual: " + numeroCiclo);
                    actualizarVistaProcesos();
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
        refrescarListaListos();
    }

    private void actualizarVistaProcesos() {
        refrescarListaListos();
        Proceso actual = kernel.getProcesoActual();
        if (actual != null)
            lblEjecucion.setText("CPU: " + actual.getNombre() + " (" + actual.getRestantes() + "/" + actual.getTotalInstrucciones() + ")");
        else
            lblEjecucion.setText("CPU: [sin proceso]");
    }

    private void refrescarListaListos() {
        Proceso[] arr = kernel.snapshotListos();
        String[] filas = new String[arr.length];
        for (int i = 0; i < arr.length; i++) {
            Proceso p = arr[i];
            filas[i] = String.format("PID %d | %-12s | %-9s | Estado: %s | %d/%d",
                    p.getPid(), p.getNombre(), p.getTipo(), p.getEstado(),
                    p.getRestantes(), p.getTotalInstrucciones());
        }
        listaListos.setListData(filas);
    }
}