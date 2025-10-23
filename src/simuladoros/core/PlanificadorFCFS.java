/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladoros.core;

/**
 *
 * @author user
 */
public class PlanificadorFCFS implements Planificador {
    @Override
    public void onTick(Kernel k) {
        // 1) Si CPU libre, asignar siguiente de listos
        if (k.cpuLibre() && !k.listosVacio()) {
            k.asignarCPU(k.desencolarListo());
        }

        // 2) Ejecutar 1 instrucción
        ProcesoEvento ev = k.ejecutarCPU();

        // 3) Manejar evento (terminado/bloqueado)
        k.manejarEvento(ev);

        // 4) Avanzar bloqueados y regresar liberados a listos
        k.liberarBloqueadosAListos();

        // 5) Si CPU libre y hay listos, asignar
        if (k.cpuLibre() && !k.listosVacio()) {
            k.asignarCPU(k.desencolarListo());
        }
    }

    @Override
    public String nombre() { return "FCFS"; }
}

