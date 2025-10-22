/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladoros.core;

/**
 *
 * @author user
 */
/** SJF no expropiativo: elige el proceso con menor TOTAL (o restantes iniciales). */
public class PlanificadorSJF implements Planificador {

    @Override
    public void onTick(Kernel k) {
        // Si CPU libre, elegir el más corto por TOTAL
        if (k.cpuLibre() && !k.listosVacio()) {
            k.asignarCPU(k.desencolarListoMinTotal());
        }

        // Ejecutar
        ProcesoEvento ev = k.ejecutarCPU();

        // Manejar evento
        k.manejarEvento(ev);

        // Avanzar bloqueados
        k.liberarBloqueadosAListos();

        // Si CPU libre, asignar siguiente más corto
        if (k.cpuLibre() && !k.listosVacio()) {
            k.asignarCPU(k.desencolarListoMinTotal());
        }
    }

    @Override
    public String nombre() { return "SJF"; }
}
