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
    private final ColaBloqueados colaBloqueados = new ColaBloqueados();

    private final CPU cpu = new CPU();
    private int nextPid = 1;

    public Kernel() {
        reloj.setListener((ciclo, ts) -> onTick());
    }

    public void setCicloListener(ciclolistener l) { this.cicloListener = l; }

    // ------------------ Control del reloj ------------------
    public void iniciar() {
        if (!reloj.isAlive()) {
            reloj = new Reloj();
            reloj.setListener((c, ts) -> onTick());
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

    // ------------------ Procesos ------------------
    public Proceso crearProceso(String nombre, TipoProceso tipo, int totalInstr) {
        Proceso p = new Proceso(nextPid++, nombre, tipo, totalInstr);
        p.setEstado(EstadoProceso.LISTO);
        colaListos.encolar(p);
        return p;
    }

    public Proceso[] snapshotListos() { return colaListos.toArray(); }
    public Proceso[] snapshotTerminados() { return colaTerminados.toArray(); }
    public String[] snapshotBloqueadosStrings() { return colaBloqueados.toDisplayStrings(); }
    public Proceso getProcesoActual() { return cpu.getActual(); }

    // ------------------ Planificación FCFS + I/O ------------------
    private void onTick() {
        // 1) Notificar ciclo a la UI
        if (cicloListener != null)
            cicloListener.onTick(reloj.getCicloActual(), System.currentTimeMillis());

        // 2) Si CPU libre, asignar siguiente de Listos
        if (cpu.estaLibre() && !colaListos.estaVacia()) {
            cpu.asignar(colaListos.desencolar());
        }

        // 3) Ejecutar 1 instrucción y manejar evento
        ProcesoEvento ev = cpu.tick();
        switch (ev.getTipo()) {
            case TERMINADO -> colaTerminados.encolar(ev.getProceso());
            case BLOQUEADO -> colaBloqueados.bloquear(ev.getProceso(), ev.getIoEsperaCiclos());
            case NINGUNO -> { /* nada */ }
        }

        // 4) Avanzar la cola de bloqueados (decrementar esperas y liberar a LISTOS)
        Proceso[] liberados = colaBloqueados.avanzarUnCicloYLiberar();
        for (Proceso p : liberados) colaListos.encolar(p);

        // 5) Si CPU libre y hay listos, asignar siguiente
        if (cpu.estaLibre() && !colaListos.estaVacia()) {
            cpu.asignar(colaListos.desencolar());
        }
    }
}

