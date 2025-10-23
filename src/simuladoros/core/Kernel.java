/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladoros.core;
import simuladoros.core.Planificador;
import simuladoros.core.PlanificadorRR;
import simuladoros.core.PlanificadorFCFS;
import simuladoros.core.ciclolistener;



/**
 *
 * @author user
 */
public class Kernel {
    // Reloj y notificación a UI
    private Reloj reloj = new Reloj();
    private ciclolistener cicloListener;

    // Colas del sistema
    private ColaProceso colaListos = new ColaProceso();
    private ColaProceso colaTerminados = new ColaProceso();
    private ColaBloqueados colaBloqueados = new ColaBloqueados();

    // CPU
    private final CPU cpu = new CPU();

    // Planificador actual
    private Planificador planificador = new PlanificadorFCFS(); // default

    // PID
    private int nextPid = 1;

    // Métricas simples
    private long ciclosTotales = 0;
    private long ciclosCpuOcupada = 0;
    private long procesosCompletados = 0;

    public Kernel() {
        reloj.setListener((ciclo, ts) -> {
            // Notificar a la UI siempre
            if (cicloListener != null) cicloListener.onTick(ciclo, ts);

            // (4) Optimización: si todo está inactivo, no planificar
            if (cpu.estaLibre() && colaListos.estaVacia() && colaBloqueados.estaVacia()) {
                ciclosTotales++; // igual contamos ciclo para el reloj
                return;
            }

            // Delegar la política
            planificador.onTick(this);

            // Métricas
            ciclosTotales++;
            if (!cpu.estaLibre()) ciclosCpuOcupada++;
        });
    }

    // ---------------------- Política ----------------------
    public void setPlanificadorFCFS() { this.planificador = new PlanificadorFCFS(); }
    public void setPlanificadorRR(int quantum) { this.planificador = new PlanificadorRR(quantum); }
    public void setPlanificadorSJF() { this.planificador = new PlanificadorSJF(); }
    public void setPlanificadorSRTF() { this.planificador = new PlanificadorSRTF(); }
    public void setPlanificadorPrioridadNP() { this.planificador = new PlanificadorPrioridadNP(); }
    public void setPlanificadorPrioridadP() { this.planificador = new PlanificadorPrioridadP(); }
    public String nombrePlanificador() { return planificador.nombre(); }
    public void setQuantumSiRR(int q) { if (planificador instanceof PlanificadorRR rr) rr.setQuantum(q); }

    // ---------------------- Reloj -------------------------
    public void iniciar() {
        if (!reloj.isAlive()) {
            reloj = new Reloj();
            reloj.setListener((c, ts) -> {
                if (cicloListener != null) cicloListener.onTick(c, ts);
                if (cpu.estaLibre() && colaListos.estaVacia() && colaBloqueados.estaVacia()) {
                    ciclosTotales++;
                    return;
                }
                planificador.onTick(this);
                ciclosTotales++;
                if (!cpu.estaLibre()) ciclosCpuOcupada++;
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
    public int getCicloActual() { return reloj.getCicloActual(); }
    public void setCicloListener(ciclolistener l) { this.cicloListener = l; }

    // ---------------------- Reset (1) ----------------------
    public void reiniciarSimulacion() {
        // parar reloj
        detener();
        // reset de colas y contadores
        colaListos = new ColaProceso();
        colaTerminados = new ColaProceso();
        colaBloqueados = new ColaBloqueados();
        nextPid = 1;
        ciclosTotales = 0;
        ciclosCpuOcupada = 0;
        procesosCompletados = 0;
        // limpiar CPU (el hilo de CPU no existe aparte; basta liberar)
        if (!cpu.estaLibre()) cpu.liberar();
        // resetear reloj (nuevo objeto con ciclo en 0)
        reloj = new Reloj();
        reloj.setListener((ciclo, ts) -> {
            if (cicloListener != null) cicloListener.onTick(ciclo, ts);
            if (cpu.estaLibre() && colaListos.estaVacia() && colaBloqueados.estaVacia()) {
                ciclosTotales++;
                return;
            }
            planificador.onTick(this);
            ciclosTotales++;
            if (!cpu.estaLibre()) ciclosCpuOcupada++;
        });
    }

    // ---------------------- Procesos ----------------------
    // Compatibilidad
    public Proceso crearProceso(String nombre, TipoProceso tipo, int totalInstr) {
        return crearProceso(nombre, tipo, totalInstr, 0);
    }

    public Proceso crearProceso(String nombre, TipoProceso tipo, int totalInstr, int prioridad) {
        Proceso p = new Proceso(nextPid++, nombre, tipo, totalInstr);
        p.setPrioridad(prioridad);
        p.setEstado(EstadoProceso.LISTO);
        p.setArrivalCiclo(getCicloActual());
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

    public void asignarCPU(Proceso p) {
        if (p.getStartCiclo() < 0) p.setStartCiclo(getCicloActual()); // métrica tiempo de respuesta
        cpu.asignar(p);
    }
    public ProcesoEvento ejecutarCPU() {
        ProcesoEvento ev = cpu.tick();
        if (ev.getTipo() == ProcesoEvento.Tipo.TERMINADO) {
            Proceso fin = ev.getProceso();
            fin.setCompletionCiclo(getCicloActual());
            procesosCompletados++;
        }
        return ev;
    }
    public Proceso preemptarCPU() { return cpu.preempt(); }

    // SJF/SRTF
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

    // Prioridad
    public Proceso peekListoMinPrioridad() {
        Proceso[] arr = colaListos.toArray();
        if (arr.length == 0) return null;
        Proceso best = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if (arr[i].getPrioridad() < best.getPrioridad()) best = arr[i];
        }
        return best;
    }
    public Proceso desencolarListoMinPrioridad() { return colaListos.retirarMinPorPrioridad(); }

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

    // ---------------------- Métricas (5) ----------------------
    public long getCiclosTotales() { return ciclosTotales; }
    public long getCiclosCpuOcupada() { return ciclosCpuOcupada; }
    public long getProcesosCompletados() { return procesosCompletados; }

    /** Utilidad para el compañero de gráficas: resumen listo para consumir. */
    public String obtenerResumenMetricas() {
        double usoCpu = (ciclosTotales == 0) ? 0.0 : (100.0 * ciclosCpuOcupada / ciclosTotales);
        return String.format("ciclosTotales=%d;cpuOcupada=%d;usoCpu=%.2f%%;terminados=%d",
                ciclosTotales, ciclosCpuOcupada, usoCpu, procesosCompletados);
    }
}
