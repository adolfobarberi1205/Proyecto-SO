/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladoros.core;
import simuladoros.core.Planificador;
import simuladoros.core.PlanificadorRR;
import simuladoros.core.PlanificadorFCFS;


/**
 *
 * @author user
 */
public class Kernel {
  // Reloj y notificación a UI
    private Reloj reloj = new Reloj();
    private ciclolistener cicloListener;

    // Colas del sistema
    private final ColaProceso colaListos = new ColaProceso();
    private final ColaProceso colaTerminados = new ColaProceso();
    private final ColaBloqueados colaBloqueados = new ColaBloqueados();

    // CPU
    private final CPU cpu = new CPU();

    // Planificador actual
    private Planificador planificador = new PlanificadorFCFS(); // default

    // PID
    private int nextPid = 1;

    public Kernel() {
        reloj.setListener((ciclo, ts) -> {
            if (cicloListener != null) cicloListener.onTick(ciclo, ts);
            planificador.onTick(this);
        });
    }

    // ---------------------- Política ----------------------
    public void setPlanificadorFCFS() { this.planificador = new PlanificadorFCFS(); }
    public void setPlanificadorRR(int quantum) { this.planificador = new PlanificadorRR(quantum); }
    public void setPlanificadorSJF() { this.planificador = new PlanificadorSJF(); }
    public void setPlanificadorSRTF() { this.planificador = new PlanificadorSRTF(); }
    public String nombrePlanificador() { return planificador.nombre(); }
    public void setQuantumSiRR(int q) { if (planificador instanceof PlanificadorRR rr) rr.setQuantum(q); }

    // ---------------------- Reloj -------------------------
    public void iniciar() {
        if (!reloj.isAlive()) {
            reloj = new Reloj();
            reloj.setListener((c, ts) -> {
                if (cicloListener != null) cicloListener.onTick(c, ts);
                planificador.onTick(this);
            });
            reloj.iniciar();
        }
    }
    public void pausarOContinuar() {
        if (!reloj.isAlive()) return;
        if (reloj.isPausado()) reloj.continuar(); else reloj.pausar();
    }
    public void detener() { if (reloj != null && reloj.isAlive()) reloj.detener(); }
    public void setDuracionCiclo(int ms) { reloj.setDuracionCiclo(ms); }
    public int getDuracionCiclo() { return reloj.getDuracionCiclo(); }
    public void setCicloListener(ciclolistener l) { this.cicloListener = l; }

    // ---------------------- Procesos ----------------------
    public Proceso crearProceso(String nombre, TipoProceso tipo, int totalInstr) {
        Proceso p = new Proceso(nextPid++, nombre, tipo, totalInstr);
        p.setEstado(EstadoProceso.LISTO);
        colaListos.encolar(p);
        return p;
    }

    // ---------------------- Snapshots para UI -------------
    public Proceso[] snapshotListos() { return colaListos.toArray(); }
    public Proceso[] snapshotTerminados() { return colaTerminados.toArray(); }
    public String[] snapshotBloqueadosStrings() { return colaBloqueados.toDisplayStrings(); }
    public Proceso getProcesoActual() { return cpu.getActual(); }

    // ---------------------- Operaciones para planificadores ---
    public boolean cpuLibre() { return cpu.estaLibre(); }
    public boolean listosVacio() { return colaListos.estaVacia(); }

    public Proceso desencolarListo() { return colaListos.desencolar(); }
    public void encolarListo(Proceso p) { colaListos.encolar(p); }

    public void asignarCPU(Proceso p) { cpu.asignar(p); }
    public ProcesoEvento ejecutarCPU() { return cpu.tick(); }
    public Proceso preemptarCPU() { return cpu.preempt(); }

    public Proceso peekListoMinRestantes() {
        Proceso[] arr = colaListos.toArray();
        if (arr.length == 0) return null;
        Proceso best = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if (arr[i].getRestantes() < best.getRestantes()) best = arr[i];
        }
        return best;
    }

    public Proceso desencolarListoMinTotal() { return colaListos.retirarMinPorTotal(); }
    public Proceso desencolarListoMinRestantes() { return colaListos.retirarMinPorRestantes(); }

    public void manejarEvento(ProcesoEvento ev) {
        switch (ev.getTipo()) {
            case TERMINADO -> colaTerminados.encolar(ev.getProceso());
            case BLOQUEADO -> colaBloqueados.bloquear(ev.getProceso(), ev.getIoEsperaCiclos());
            case NINGUNO -> { /* nada */ }
        }
    }

    public void liberarBloqueadosAListos() {
        Proceso[] libres = colaBloqueados.avanzarUnCicloYLiberar();
        for (Proceso p : libres) colaListos.encolar(p);
    }
}