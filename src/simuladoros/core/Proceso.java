/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladoros.core;

/**
 *
 * @author user
 */
public class Proceso {
      private final int pid;
    private String nombre;
    private TipoProceso tipo;
    private EstadoProceso estado;

    private int totalInstrucciones;
    private int restantes;
    private int prioridad = 0;

    // Métricas (ciclos)
    private int arrivalCiclo = -1;
    private int startCiclo = -1;
    private int completionCiclo = -1;

    public Proceso(int pid, String nombre, TipoProceso tipo, int totalInstrucciones) {
        this.pid = pid;
        this.nombre = nombre;
        this.tipo = tipo;
        this.totalInstrucciones = Math.max(1, totalInstrucciones);
        this.restantes = this.totalInstrucciones;
        this.estado = EstadoProceso.NUEVO;
    }

    public int getPid() { return pid; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public TipoProceso getTipo() { return tipo; }
    public void setTipo(TipoProceso tipo) { this.tipo = tipo; }

    public EstadoProceso getEstado() { return estado; }
    public void setEstado(EstadoProceso estado) { this.estado = estado; }

    public int getTotalInstrucciones() { return totalInstrucciones; }
    public int getRestantes() { return restantes; }
    public void setRestantes(int r) { this.restantes = Math.max(0, r); }

    public int getPrioridad() { return prioridad; }
    public void setPrioridad(int prioridad) { this.prioridad = prioridad; }

    public int getArrivalCiclo() { return arrivalCiclo; }
    public void setArrivalCiclo(int c) { this.arrivalCiclo = c; }

    public int getStartCiclo() { return startCiclo; }
    public void setStartCiclo(int c) { this.startCiclo = c; }

    public int getCompletionCiclo() { return completionCiclo; }
    public void setCompletionCiclo(int c) { this.completionCiclo = c; }

    @Override
    public String toString() {
        return String.format("PID %d - %s [%s] %d/%d",
                pid, nombre, estado, restantes, totalInstrucciones);
    }
}