/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladoros.core;

/**
 *
 * @author user
 */
public class PlanificadorRR  implements Planificador {
        private int quantum;             // en ciclos
    private int restanteQuantum = 0; // contador decreciente

    public PlanificadorRR(int quantum) {
        this.quantum = Math.max(1, quantum);
    }

    public void setQuantum(int q) {
        this.quantum = Math.max(1, q);
    }

    @Override
    public void onTick(Kernel k) {
        // Si CPU libre, tomar siguiente y resetear quantum
        if (k.cpuLibre() && !k.listosVacio()) {
            k.asignarCPU(k.desencolarListo());
            restanteQuantum = quantum;
        }

        // Ejecutar 1 instrucción
        ProcesoEvento ev = k.ejecutarCPU();
        if (restanteQuantum > 0) restanteQuantum--;

        // Manejar eventos terminales/bloqueo
        if (ev.getTipo() == ProcesoEvento.Tipo.TERMINADO) {
            k.manejarEvento(ev);
            restanteQuantum = 0; // proceso ya no está en CPU
        } else if (ev.getTipo() == ProcesoEvento.Tipo.BLOQUEADO) {
            k.manejarEvento(ev);
            restanteQuantum = 0; // proceso ya no está en CPU
        } else {
            // Si no hubo evento y se agotó el quantum, PREEMPTAR
            if (restanteQuantum == 0 && !k.cpuLibre()) {
                Proceso preemptado = k.preemptarCPU(); // vuelve a LISTO
                if (preemptado != null) k.encolarListo(preemptado);
            }
        }

        // Avanzar bloqueados y regresar liberados
        k.liberarBloqueadosAListos();

        // Si CPU libre, asignar siguiente y resetear quantum
        if (k.cpuLibre() && !k.listosVacio()) {
            k.asignarCPU(k.desencolarListo());
            restanteQuantum = quantum;
        }
    }

    @Override
    public String nombre() { return "Round Robin"; }
}

    
