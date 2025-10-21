/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladoros.core;

/**
 * * Simula la CPU que ejecuta un proceso a la vez.
 * Recibe ticks del reloj mediante el Kernel.
 */
public class CPU {
    private Proceso actual;

    /** Ejecuta una instrucción del proceso actual. */
    public void tick() {
        if (actual == null) return;

        // Restar una instrucción
        int restantes = actual.getRestantes() - 1;
        actual.setRestantes(restantes);

        // Si ya terminó, cambia estado
        if (restantes <= 0) {
            actual.setEstado(EstadoProceso.TERMINADO);
            actual = null;
        }
    }

    public boolean estaLibre() {
        return actual == null;
    }

    public Proceso getActual() {
        return actual;
    }

    /** Cargar proceso en CPU */
    public void asignar(Proceso p) {
        if (p == null) return;
        p.setEstado(EstadoProceso.EJECUTANDO);
        this.actual = p;
    }

    /** Liberar CPU (proceso terminado o suspendido) */
    public void liberar() {
        this.actual = null;
    }
}
