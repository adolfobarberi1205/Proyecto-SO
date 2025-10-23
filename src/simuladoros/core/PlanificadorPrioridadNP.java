/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladoros.core;

/**
 *
 * @author user
 */
/** Prioridad NO expropiativa: siempre el de menor prioridad (número más bajo). */
public class PlanificadorPrioridadNP implements Planificador {

    @Override
    public void onTick(Kernel k) {
        // Si CPU libre, asignar el de mejor prioridad
        if (k.cpuLibre() && !k.listosVacio()) {
            k.asignarCPU(k.desencolarListoMinPrioridad());
        }

        // Ejecutar
        ProcesoEvento ev = k.ejecutarCPU();

        // Manejar evento
        k.manejarEvento(ev);

        // Avanzar bloqueados
        k.liberarBloqueadosAListos();

        // Si CPU libre, asignar siguiente mejor prioridad
        if (k.cpuLibre() && !k.listosVacio()) {
            k.asignarCPU(k.desencolarListoMinPrioridad());
        }
    }

    @Override
    public String nombre() { return "Prioridad (NP)"; }
}
