/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladoros.core;

/**
 *
 * @author user
 */
/** SRTF (SJF expropiativo): siempre corre el de MENOR RESTANTE. */
public class PlanificadorSRTF implements Planificador {

    @Override
    public void onTick(Kernel k) {
        // Si CPU libre, tomar el que tenga MENOS RESTANTES
        if (k.cpuLibre() && !k.listosVacio()) {
            k.asignarCPU(k.desencolarListoMinRestantes());
        } else if (!k.cpuLibre() && !k.listosVacio()) {
            // Comparar el actual con el mejor de listos
            Proceso candidato = k.peekListoMinRestantes();
            Proceso actual = k.getProcesoActual();
            if (candidato != null && actual != null
                    && candidato.getRestantes() < actual.getRestantes()) {
                // Preemptar
                Proceso sacado = k.preemptarCPU();
                if (sacado != null) k.encolarListo(sacado);
                // Asignar el mejor
                k.asignarCPU(k.desencolarListoMinRestantes());
            }
        }

        // Ejecutar
        ProcesoEvento ev = k.ejecutarCPU();

        // Manejar evento terminado/bloqueado
        k.manejarEvento(ev);

        // Avanzar bloqueados
        k.liberarBloqueadosAListos();

        // Si CPU libre, volver a elegir el de MENOR RESTANTE
        if (k.cpuLibre() && !k.listosVacio()) {
            k.asignarCPU(k.desencolarListoMinRestantes());
        } else if (!k.cpuLibre() && !k.listosVacio()) {
            // Chequeo post-ejecución por si empeoró la situación
            Proceso candidato = k.peekListoMinRestantes();
            Proceso actual = k.getProcesoActual();
            if (candidato != null && actual != null
                    && candidato.getRestantes() < actual.getRestantes()) {
                Proceso sacado = k.preemptarCPU();
                if (sacado != null) k.encolarListo(sacado);
                k.asignarCPU(k.desencolarListoMinRestantes());
            }
        }
    }

    @Override
    public String nombre() { return "SRTF"; }
}