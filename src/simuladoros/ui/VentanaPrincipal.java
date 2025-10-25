/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladoros.ui;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import simuladoros.core.ciclolistener;
import simuladoros.core.Kernel;
import simuladoros.core.Proceso;
import simuladoros.core.TipoProceso;
import simuladoros.config.ArchivoConfig;

/**
 *
 * @author user
 */
public class VentanaPrincipal  extends JFrame{
   // --- Núcleo ---
    private final Kernel kernel = new Kernel();

    // --- Componentes UI ---
    private JLabel lblTitulo;
    private JLabel lblPlan;
    private JLabel lblCiclo;
    private JLabel lblEjecucion;

    private JComboBox<String> cbAlgoritmo;
    private JSpinner spQuantum;
    private JSpinner spDuracion;

    private JButton btnIniciar;
    private JButton btnPausa;
    private JButton btnReiniciar;
    private JButton btnNuevoProceso;
    private JButton btnGuardar;
    private JButton btnCargar;

    private JList<String> listaListos;
    private JList<String> listaBloqueados;
    private JList<String> listaTerminados;

    // METRICAS: panel y timer
    private PanelMetricas panelMetricas;
    private javax.swing.Timer timerMetricas; // usar el Timer de Swing

    // METRICAS: tabs
    private JTabbedPane tabsCentro;

    public VentanaPrincipal() {
        initUI();
        enlazarEventos();
        refrescarListas();

        // METRICAS: iniciar refresco periódico de métricas (cada 250 ms)
        timerMetricas = new javax.swing.Timer(250, e -> {
            if (kernel != null && panelMetricas != null) {
                var snap = kernel.getMetricas().snapshot();
                panelMetricas.updateFrom(snap);
            }
        });
        timerMetricas.start();
    }

    // ================================
    // UI
    // ================================
    private void initUI() {
        setTitle("Simulador de Sistema Operativo - Planificación");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);

        // Encabezados
        lblTitulo = new JLabel("Simulador de Planificación de Procesos", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));

        lblPlan = new JLabel("Política actual: " + kernel.nombrePlanificador(), SwingConstants.CENTER);
        lblPlan.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        lblCiclo = new JLabel("Ciclo actual: 0", SwingConstants.CENTER);
        lblCiclo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        lblEjecucion = new JLabel("CPU: [sin proceso]", SwingConstants.CENTER);
        lblEjecucion.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Controles
        cbAlgoritmo = new JComboBox<>(new String[]{
                "FCFS", "Round Robin", "SJF", "SRTF", "Prioridad (NP)", "Prioridad (P)"
        });
        spQuantum = new JSpinner(new SpinnerNumberModel(3, 1, 50, 1));
        spDuracion = new JSpinner(new SpinnerNumberModel(500, 10, 5000, 10));

        btnIniciar = new JButton("Iniciar");
        btnPausa = new JButton("Pausar");
        btnPausa.setEnabled(false);
        btnReiniciar = new JButton("Reiniciar");
        btnNuevoProceso = new JButton("Nuevo Proceso");
        btnGuardar = new JButton("Guardar CSV");
        btnCargar = new JButton("Cargar CSV");

        // Listas
        listaListos = new JList<>();
        listaBloqueados = new JList<>();
        listaTerminados = new JList<>();

        // Panel superior (títulos + estado)
        JPanel panelTop = new JPanel();
        panelTop.setLayout(new BoxLayout(panelTop, BoxLayout.Y_AXIS));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblPlan.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblCiclo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblEjecucion.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelTop.add(Box.createVerticalStrut(6));
        panelTop.add(lblTitulo);
        panelTop.add(lblPlan);
        panelTop.add(Box.createVerticalStrut(6));
        panelTop.add(lblCiclo);
        panelTop.add(Box.createVerticalStrut(6));
        panelTop.add(lblEjecucion);
        panelTop.add(Box.createVerticalStrut(6));
        panelTop.add(Box.createVerticalStrut(6));

        // Panel central (3 columnas) -> irá dentro de una pestaña
        JPanel columnas = new JPanel(new GridLayout(1, 3, 10, 10));
        columnas.add(panelLista("Cola de Listos", listaListos));
        columnas.add(panelLista("Bloqueados (E/S)", listaBloqueados));
        columnas.add(panelLista("Terminados", listaTerminados));

        // METRICAS: crear panel de métricas
        panelMetricas = new PanelMetricas();

        // METRICAS: JTabbedPane con "Colas" y "Métricas"
        tabsCentro = new JTabbedPane();
        tabsCentro.addTab("Colas", columnas);
        tabsCentro.addTab("Métricas", panelMetricas);

        // Panel controles inferior
        JPanel panelControl = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelControl.add(new JLabel("Duración (ms/ciclo):"));
        panelControl.add(spDuracion);
        panelControl.add(new JLabel("Algoritmo:"));
        panelControl.add(cbAlgoritmo);
        panelControl.add(new JLabel("Quantum:"));
        panelControl.add(spQuantum);
        panelControl.add(btnIniciar);
        panelControl.add(btnPausa);
        panelControl.add(btnReiniciar);
        panelControl.add(btnNuevoProceso);
        panelControl.add(btnGuardar);
        panelControl.add(btnCargar);

        setLayout(new BorderLayout(10, 10));
        add(panelTop, BorderLayout.NORTH);

        // METRICAS: en lugar de añadir 'columnas' directo, añadimos el tabbed
        add(tabsCentro, BorderLayout.CENTER);

        add(panelControl, BorderLayout.SOUTH);

        actualizarVisibilidadQuantum();
    }

    private JPanel panelLista(String titulo, JList<String> list) {
        JPanel p = new JPanel(new BorderLayout());
        JLabel l = new JLabel(titulo, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        p.add(l, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(list);
        sp.setPreferredSize(new Dimension(340, 420));
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    // ================================
    // Eventos
    // ================================
    private void enlazarEventos() {
        // Tick del reloj -> refrescar datos
        kernel.setCicloListener((numeroCiclo, ts) ->
                SwingUtilities.invokeLater(() -> {
                    lblCiclo.setText("Ciclo actual: " + numeroCiclo);
                    actualizarVistaCPU();
                    refrescarListas();
                    // METRICAS: el Timer ya actualiza el panel periódicamente
                })
        );

        btnIniciar.addActionListener(e -> {
            aplicarAlgoritmoActual();
            // Validar duración
            int v = (Integer) spDuracion.getValue();
            if (v < 10) { v = 10; spDuracion.setValue(10); }
            kernel.setDuracionCiclo(v);

            kernel.iniciar();
            btnIniciar.setEnabled(false);
            btnPausa.setEnabled(true);
        });

        btnPausa.addActionListener(e -> {
            kernel.pausarOContinuar();
            if (btnPausa.getText().equals("Pausar")) btnPausa.setText("Continuar");
            else btnPausa.setText("Pausar");
        });

        btnReiniciar.addActionListener(e -> {
            kernel.reiniciarSimulacion();
            btnIniciar.setEnabled(true);
            btnPausa.setEnabled(false);
            btnPausa.setText("Pausar");
            lblCiclo.setText("Ciclo actual: 0");
            lblEjecucion.setText("CPU: [sin proceso]");
            refrescarListas();

            // METRICAS: refrescar panel a cero tras reinicio
            if (panelMetricas != null) {
                panelMetricas.updateFrom(kernel.getMetricas().snapshot());
            }
        });

        spDuracion.addChangeListener(e -> {
            int v = (Integer) spDuracion.getValue();
            if (v < 10) { spDuracion.setValue(10); v = 10; }
            kernel.setDuracionCiclo(v);
        });

        cbAlgoritmo.addActionListener(e -> {
            aplicarAlgoritmoActual();
        });

        spQuantum.addChangeListener(e -> {
            kernel.setQuantumSiRR((Integer) spQuantum.getValue());
        });

        btnNuevoProceso.addActionListener(e -> crearProcesoDesdeDialogo());

        btnGuardar.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                try {
                    ArchivoConfig.guardarCSV(f, kernel);
                    JOptionPane.showMessageDialog(this, "Guardado OK.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error guardando: " + ex.getMessage());
                }
            }
        });

        btnCargar.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                try {
                    kernel.reiniciarSimulacion();
                    ArchivoConfig.cargarCSV(f, kernel);
                    btnIniciar.setEnabled(true);
                    btnPausa.setEnabled(false);
                    btnPausa.setText("Pausar");
                    refrescarListas();

                    // METRICAS: snapshot tras cargar escenario
                    if (panelMetricas != null) {
                        panelMetricas.updateFrom(kernel.getMetricas().snapshot());
                    }

                    JOptionPane.showMessageDialog(this, "Escenario cargado.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error cargando: " + ex.getMessage());
                }
            }
        });
    }

    // ================================
    // Lógica de UI auxiliar
    // ================================
    private void aplicarAlgoritmoActual() {
        String sel = (String) cbAlgoritmo.getSelectedItem();

        if ("Round Robin".equals(sel)) {
            int q = (Integer) spQuantum.getValue();
            kernel.setPlanificadorRR(q);
        } else if ("SJF".equals(sel)) {
            kernel.setPlanificadorSJF();
        } else if ("SRTF".equals(sel)) {
            kernel.setPlanificadorSRTF();
        } else if ("Prioridad (NP)".equals(sel)) {
            kernel.setPlanificadorPrioridadNP();
        } else if ("Prioridad (P)".equals(sel)) {
            kernel.setPlanificadorPrioridadP();
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
        if (nombre == null) return;
        nombre = nombre.trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre no puede estar vacío."); return;
        }
        if (nombre.length() > 20) {
            JOptionPane.showMessageDialog(this, "El nombre es muy largo (máx. 20)."); return;
        }

        Object opcion = JOptionPane.showInputDialog(
                this, "Tipo de proceso:", "Nuevo Proceso",
                JOptionPane.QUESTION_MESSAGE, null,
                new Object[]{"CPU_BOUND", "IO_BOUND"}, "CPU_BOUND"
        );
        if (opcion == null) return;
        TipoProceso tipo = "IO_BOUND".equals(opcion.toString()) ? TipoProceso.IO_BOUND : TipoProceso.CPU_BOUND;

        String totalStr = JOptionPane.showInputDialog(this, "Total de instrucciones (1..10000):", "50");
        if (totalStr == null) return;
        int total;
        try {
            total = Integer.parseInt(totalStr.trim());
            if (total < 1 || total > 10000) throw new IllegalArgumentException();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Valor inválido para total de instrucciones."); return;
        }

        String prioStr = JOptionPane.showInputDialog(this, "Prioridad (entero, menor = más prioridad, -10..10):", "0");
        if (prioStr == null) return;
        int prio;
        try {
            prio = Integer.parseInt(prioStr.trim());
            if (prio < -10 || prio > 10) throw new IllegalArgumentException();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Prioridad inválida (rango -10..10)."); return;
        }

        kernel.crearProceso(nombre, tipo, total, prio);
        refrescarListas();
    }

    private void actualizarVistaCPU() {
        Proceso actual = kernel.getProcesoActual();
        if (actual != null) {
            lblEjecucion.setText("CPU: " + actual.getNombre()
                    + " (" + actual.getRestantes() + "/" + actual.getTotalInstrucciones() + ")"
                    + " | prio:" + actual.getPrioridad());
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
            filasListos[i] = String.format(
                    "PID %d | %-12s | %-9s | prio:%d | %s | %d/%d",
                    p.getPid(), p.getNombre(), p.getTipo(), p.getPrioridad(),
                    p.getEstado(), p.getRestantes(), p.getTotalInstrucciones()
            );
        }
        listaListos.setListData(filasListos);

        // Bloqueados
        listaBloqueados.setListData(kernel.snapshotBloqueadosStrings());

        // Terminados
        Proceso[] terms = kernel.snapshotTerminados();
        String[] filasTerm = new String[terms.length];
        for (int i = 0; i < terms.length; i++) {
            Proceso p = terms[i];
            filasTerm[i] = String.format(
                    "PID %d | %-12s | %-9s | prio:%d | %s | %d/%d",
                    p.getPid(), p.getNombre(), p.getTipo(), p.getPrioridad(),
                    p.getEstado(), p.getRestantes(), p.getTotalInstrucciones()
            );
        }
        listaTerminados.setListData(filasTerm);
    }
}
