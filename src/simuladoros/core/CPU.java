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
     * - Si termina, devuelve evento TERMINADO.
     * - Si solicita E/S (solo IO_BOUND), devuelve evento BLOQUEADO (espera fija).
     * - Si nada especial, devuelve NINGUNO.
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

        // ¿Solicita E/S? (regla simple para demo: IO_BOUND se bloquea cada 5 instrucciones)
        if (actual.getTipo() == TipoProceso.IO_BOUND) {
            // se bloquea cuando al proceso le queden múltiplos de 5 instrucciones
            if (restantes > 0 && (restantes % 5 == 0)) {
                actual.setEstado(EstadoProceso.BLOQUEADO);
                Proceso bloqueado = actual;
                actual = null;
                // Esperará 3 ciclos de I/O
                return ProcesoEvento.bloqueado(bloqueado, 3);
            }
        }

        return ProcesoEvento.ninguno();
    }

    public boolean estaLibre() { return actual == null; }

    public Proceso getActual() { return actual; }

    /** Cargar proceso en CPU */
    public void asignar(Proceso p) {
        if (p == null) return;
        p.setEstado(EstadoProceso.EJECUTANDO);
        this.actual = p;
    }

    /** Liberar CPU */
    public void liberar() { this.actual = null; }
}