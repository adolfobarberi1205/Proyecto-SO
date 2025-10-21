/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladoros.core;

/**
 *
 * @author user
 */
public class Kernel {
 private Reloj reloj = new Reloj();
    private ciclolistener cicloListener;

    private final ColaProceso colaListos = new ColaProceso();
    private final ColaProceso colaTerminados = new ColaProceso();

    private final CPU cpu = new CPU();

    private int nextPid = 1;

    public Kernel() {
        // Cada tick del reloj invoca planificador
        reloj.setListener((ciclo, ts) -> onTick());
    }

    public void setCicloListener(ciclolistener l) {
        this.cicloListener = l;
    }

    // --------------------------------------------------
    // Control del reloj
    // --------------------------------------------------
    public void iniciar() {
        if (!reloj.isAlive()) {
            reloj = new Reloj();
            reloj.setListener((ciclo, ts) -> onTick());
            reloj.iniciar();
        }
    }

    public void pausarOContinuar() {
        if (!reloj.isAlive()) return;
        if (reloj.isPausado()) reloj.continuar(); else reloj.pausar();
    }

    public void detener() {
        if (reloj != null && reloj.isAlive()) reloj.detener();
    }

    public void setDuracionCiclo(int ms) { reloj.setDuracionCiclo(ms); }
    public int getDuracionCiclo() { return reloj.getDuracionCiclo(); }

    // --------------------------------------------------
    // Procesos
    // --------------------------------------------------
    public Proceso crearProceso(String nombre, TipoProceso tipo, int totalInstr) {
        Proceso p = new Proceso(nextPid++, nombre, tipo, totalInstr);
        p.setEstado(EstadoProceso.LISTO);
        colaListos.encolar(p);
        return p;
    }

    public Proceso[] snapshotListos() {
        return colaListos.toArray();
    }

    public Proceso getProcesoActual() {
        return cpu.getActual();
    }

    // --------------------------------------------------
    // Núcleo de planificación FCFS
    // --------------------------------------------------
    private void onTick() {
        // 1️⃣ Notificar a la UI si hay listener
        if (cicloListener != null)
            cicloListener.onTick(reloj.getCicloActual(), System.currentTimeMillis());

        // 2️⃣ Si CPU libre, tomar siguiente proceso de la cola
        if (cpu.estaLibre() && !colaListos.estaVacia()) {
            Proceso siguiente = colaListos.desencolar();
            cpu.asignar(siguiente);
        }

        // 3️⃣ Ejecutar instrucción
        cpu.tick();

        // 4️⃣ Si terminó el proceso, mover a terminados
        if (cpu.getActual() == null && !colaListos.estaVacia()) {
            // CPU libre pero hay más procesos, asignar siguiente
            Proceso siguiente = colaListos.desencolar();
            cpu.asignar(siguiente);
        }
    }

    public Proceso[] snapshotTerminados() {
        return colaTerminados.toArray();
    }
}
    

