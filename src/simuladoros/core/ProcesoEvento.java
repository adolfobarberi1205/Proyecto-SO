/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladoros.core;

/**
 *
 * @author user
 */
public class ProcesoEvento {
    public enum Tipo { NINGUNO, TERMINADO, BLOQUEADO }

    private final Tipo tipo;
    private final Proceso proceso;
    private final int ioEsperaCiclos; // solo si BLOQUEADO

    private ProcesoEvento(Tipo tipo, Proceso proceso, int ioEsperaCiclos) {
        this.tipo = tipo;
        this.proceso = proceso;
        this.ioEsperaCiclos = ioEsperaCiclos;
    }

    public static ProcesoEvento ninguno() {
        return new ProcesoEvento(Tipo.NINGUNO, null, 0);
    }

    public static ProcesoEvento terminado(Proceso p) {
        return new ProcesoEvento(Tipo.TERMINADO, p, 0);
    }

    public static ProcesoEvento bloqueado(Proceso p, int espera) {
        return new ProcesoEvento(Tipo.BLOQUEADO, p, espera);
    }

    public Tipo getTipo() { return tipo; }
    public Proceso getProceso() { return proceso; }
    public int getIoEsperaCiclos() { return ioEsperaCiclos; }
}

