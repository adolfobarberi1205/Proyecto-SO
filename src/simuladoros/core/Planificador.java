/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladoros.core;

/**
 *
 * @author user
 */
public interface Planificador {
    /** Ejecuta un tick de planificación sobre el kernel. */
    void onTick(Kernel k);
    /** Nombre visible de la política (para UI). */
    String nombre();
}
