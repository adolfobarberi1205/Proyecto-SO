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
    private JLabel lblPlan;
    private JLabel lblCiclo;
    private JButton btnIniciar;
    private JButton btnPausa;
    private JSpinner spDuracion;

    private JComboBox<String> cbAlgoritmo;
    private JSpinner spQuantum;

    private JButton btnNuevoProceso;
    private JList<String> listaListos;
    private JList<String> listaBloqueados;
    private JList<String> listaTerminados;
    private JLabel lblEjecucion;

    public VentanaPrincipal() {
        initUI();
        enlazarEventos();
        refrescarListas();
    }

    private void initUI() {
        setTitle("Simulador de Sistema Operativo - Planificación");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 650);
        setLocationRelativeTo(null);

        lblTitulo = new JLabel("Simulador de Planificación (FCFS / Round Robin)", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));

        lblPlan = new JLabel("Política actual: " + kernel.nombrePlanificador(), SwingConstants.CENTER);
        lblPlan.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        lblCiclo = new JLabel("Ciclo actual: 0", SwingConstants.CENTER);
        lblCiclo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        btnIniciar = new JButton("Iniciar");
        btnPausa  = new JButton("Pausar");
        btnPausa.setEnabled(false);

        spDuracion = new JSpinner(new SpinnerNumberModel(500, 10, 5000, 10));
        JLabel lblMs = new JLabel("ms/ciclo");

        cbAlgoritmo = new JComboBox<>(new String[]{"FCFS", "Round Robin", "SJF", "SRTF"});

        spQuantum = new JSpinner(new SpinnerNumberModel(3, 1, 50, 1));
        JLabel lblQ = new JLabel("Quantum:");

        btnNuevoProceso = new JButton("Nuevo Proceso");
        listaListos = new JList<>();
        listaBloqueados = new JList<>();
        listaTerminados = new JList<>();
        lblEjecucion = new JLabel("CPU: [sin proceso]", SwingConstants.CENTER);
        lblEjecucion.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JPanel panelControl = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelControl.add(new JLabel("Duración:"));
        panelControl.add(spDuracion);
        panelControl.add(lblMs);
        panelControl.add(new JLabel("Algoritmo:"));
        panelControl.add(cbAlgoritmo);
        panelControl.add(lblQ);
        panelControl.add(spQuantum);
        panelControl.add(btnIniciar);
        panelControl.add(btnPausa);
        panelControl.add(Box.createHorizontalStrut(20));
        panelControl.add(btnNuevoProceso);

        JPanel centro = new JPanel(new BorderLayout(10,10));

        JPanel superior = new JPanel();
        superior.setLayout(new BoxLayout(superior, BoxLayout.Y_AXIS));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblPlan.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblCiclo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblEjecucion.setAlignmentX(Component.CENTER_ALIGNMENT);
        superior.add(Box.createVerticalStrut(6));
        superior.add(lblTitulo);
        superior.add(lblPlan);
        superior.add(Box.createVerticalStrut(6));
        superior.add(lblCiclo);
        superior.add(Box.createVerticalStrut(6));
        superior.add(lblEjecucion);
        superior.add(Box.createVerticalStrut(6));

        JPanel columnas = new JPanel(new GridLayout(1, 3, 10, 10));
        columnas.add(panelLista("Cola de Listos", listaListos));
        columnas.add(panelLista("Bloqueados (E/S)", listaBloqueados));
        columnas.add(panelLista("Terminados", listaTerminados));

        centro.add(superior, BorderLayout.NORTH);
        centro.add(columnas, BorderLayout.CENTER);

        setLayout(new BorderLayout(10,10));
        add(centro, BorderLayout.CENTER);
        add(panelControl, BorderLayout.SOUTH);
    }

    private JPanel panelLista(String titulo, JList<String> list) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel(titulo, SwingConstants.CENTER), BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(list);
        sp.setPreferredSize(new Dimension(320, 380));
        p.add(sp, BorderLayout.CENTER);
        return p;
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
            aplicarAlgoritmoActual();
            kernel.iniciar();
            btnIniciar.setEnabled(false);
            btnPausa.setEnabled(true);
        });

        btnPausa.addActionListener(e -> {
            kernel.pausarOContinuar();
            if (btnPausa.getText().equals("Pausar")) btnPausa.setText("Continuar");
            else btnPausa.setText("Pausar");
        });

        spDuracion.addChangeListener(e -> kernel.setDuracionCiclo((Integer) spDuracion.getValue()));

        cbAlgoritmo.addActionListener(e -> {
            aplicarAlgoritmoActual();
        });

        spQuantum.addChangeListener(e -> {
            kernel.setQuantumSiRR((Integer) spQuantum.getValue());
        });

        btnNuevoProceso.addActionListener(e -> crearProcesoDesdeDialogo());

        // Por defecto, ocultar spinner de quantum si FCFS
        actualizarVisibilidadQuantum();
    }

    private void aplicarAlgoritmoActual() {
    String sel = (String) cbAlgoritmo.getSelectedItem();
    if ("Round Robin".equals(sel)) {
        int q = (Integer) spQuantum.getValue();
        kernel.setPlanificadorRR(q);
    } else if ("SJF".equals(sel)) {
        kernel.setPlanificadorSJF();
    } else if ("SRTF".equals(sel)) {
        kernel.setPlanificadorSRTF();
    } else {
        kernel.setPlanificadorFCFS();
    }
    lblPlan.setText("Política actual: " + kernel.nombrePlanificador());
    actualizarVisibilidadQuantum();
}

   private void actualizarVisibilidadQuantum() {
    boolean rr = "Round Robin".equals(cbAlgoritmo.getSelectedItem());
    spQuantum.setEnabled(rr);
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
            filasListos[i] = String.format("PID %d | %-12s | %-9s | %s | %d/%d",
                    p.getPid(), p.getNombre(), p.getTipo(), p.getEstado(),
                    p.getRestantes(), p.getTotalInstrucciones());
        }
        listaListos.setListData(filasListos);

        // Bloqueados
        listaBloqueados.setListData(kernel.snapshotBloqueadosStrings());

        // Terminados
        Proceso[] terms = kernel.snapshotTerminados();
        String[] filasTerm = new String[terms.length];
        for (int i = 0; i < terms.length; i++) {
            Proceso p = terms[i];
            filasTerm[i] = String.format("PID %d | %-12s | %-9s | %s | %d/%d",
                    p.getPid(), p.getNombre(), p.getTipo(), p.getEstado(),
                    p.getRestantes(), p.getTotalInstrucciones());
        }
        listaTerminados.setListData(filasTerm);
    }
}