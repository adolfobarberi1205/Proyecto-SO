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

    private int nextPid = 1;
    private final ColaProceso colaListos = new ColaProceso();

    public void setCicloListener(ciclolistener l) {
        this.cicloListener = l;
        reloj.setListener(l);
    }

    // --- Control del reloj (igual que antes) ---
    public void iniciar() {
        if (reloj.isAlive()) return;
        if (!reloj.isAlive()) {
            Reloj nuevo = new Reloj();
            nuevo.setDuracionCiclo(reloj.getDuracionCiclo());
            if (cicloListener != null) nuevo.setListener(cicloListener);
            reloj = nuevo;
        }
        reloj.iniciar();
    }
    public void pausarOContinuar() {
        if (!reloj.isAlive()) return;
        if (reloj.isPausado()) reloj.continuar(); else reloj.pausar();
    }
    public void detener() { if (reloj != null && reloj.isAlive()) reloj.detener(); }
    public void setDuracionCiclo(int ms) { reloj.setDuracionCiclo(ms); }
    public int getDuracionCiclo() { return reloj.getDuracionCiclo(); }

    // --- Procesos y cola de listos ---
    public Proceso crearProceso(String nombre, TipoProceso tipo, int totalInstr) {
        Proceso p = new Proceso(nextPid++, nombre, tipo, totalInstr);
        p.setEstado(EstadoProceso.LISTO);
        colaListos.encolar(p);
        return p;
    }

    public Proceso[] snapshotListos() {
        return colaListos.toArray();
    }
}
    

