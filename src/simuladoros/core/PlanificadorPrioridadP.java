/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladoros.core;

/**
 *
 * @author user
 */
/** Prioridad EXPROPIATIVA: si llega uno de mejor prioridad, se preempta el actual. */
public class PlanificadorPrioridadP implements Planificador {

    @Override
    public void onTick(Kernel k) {
        // Si CPU libre, tomar el de mejor prioridad
        if (k.cpuLibre() && !k.listosVacio()) {
            k.asignarCPU(k.desencolarListoMinPrioridad());
        } else if (!k.cpuLibre() && !k.listosVacio()) {
            // Comparar prioridad del actual vs mejor de listos
            Proceso candidato = k.peekListoMinPrioridad();
            Proceso actual = k.getProcesoActual();
            if (candidato != null && actual != null
                    && candidato.getPrioridad() < actual.getPrioridad()) {
                // Preempción
                Proceso sacado = k.preemptarCPU();
                if (sacado != null) k.encolarListo(sacado);
                k.asignarCPU(k.desencolarListoMinPrioridad());
            }
        }

        // Ejecutar
        ProcesoEvento ev = k.ejecutarCPU();

        // Manejo de eventos
        k.manejarEvento(ev);

        // Avanzar bloqueados y regresar a listos
        k.liberarBloqueadosAListos();

        // Re-evaluar después de cambios
        if (k.cpuLibre() && !k.listosVacio()) {
            k.asignarCPU(k.desencolarListoMinPrioridad());
        } else if (!k.cpuLibre() && !k.listosVacio()) {
            Proceso candidato = k.peekListoMinPrioridad();
            Proceso actual = k.getProcesoActual();
            if (candidato != null && actual != null
                    && candidato.getPrioridad() < actual.getPrioridad()) {
                Proceso sacado = k.preemptarCPU();
                if (sacado != null) k.encolarListo(sacado);
                k.asignarCPU(k.desencolarListoMinPrioridad());
            }
        }
    }

    @Override
    public String nombre() { return "Prioridad (P)"; }
}