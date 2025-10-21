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

    /**
     * Ejecuta una instrucción del proceso actual.
     * Devuelve evento TERMINADO/BLOQUEADO/ninguno.
     */
    public ProcesoEvento tick() {
        if (actual == null) return ProcesoEvento.ninguno();

        // Ejecutar 1 instrucción
        int restantes = actual.getRestantes() - 1;
        actual.setRestantes(restantes);

        // ¿Terminó?
        if (restantes <= 0) {
            actual.setEstado(EstadoProceso.TERMINADO);
            Proceso terminado = actual;
            actual = null;
            return ProcesoEvento.terminado(terminado);
        }

        // ¿Pide I/O? (regla simple demo para IO_BOUND)
        if (actual.getTipo() == TipoProceso.IO_BOUND) {
            if (restantes > 0 && (restantes % 5 == 0)) { // cada 5 instrucciones
                actual.setEstado(EstadoProceso.BLOQUEADO);
                Proceso bloqueado = actual;
                actual = null;
                return ProcesoEvento.bloqueado(bloqueado, 3); // espera 3 ciclos
            }
        }

        return ProcesoEvento.ninguno();
    }

    /** Preempción: saca el proceso actual y lo devuelve en estado LISTO. */
    public Proceso preempt() {
        if (actual == null) return null;
        Proceso p = actual;
        p.setEstado(EstadoProceso.LISTO);
        actual = null;
        return p;
    }

    public boolean estaLibre() { return actual == null; }
    public Proceso getActual() { return actual; }

    public void asignar(Proceso p) {
        if (p == null) return;
        p.setEstado(EstadoProceso.EJECUTANDO);
        this.actual = p;
    }

    public void liberar() { this.actual = null; }
}