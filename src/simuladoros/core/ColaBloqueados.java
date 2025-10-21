/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladoros.core;

/**
 *
 * @author user
 */
public class ColaBloqueados {
      private static class Nodo {
        Proceso value;
        int espera; // ciclos restantes de espera I/O
        Nodo next;
        Nodo(Proceso v, int espera) { this.value = v; this.espera = espera; }
    }

    private Nodo head, tail;
    private int size = 0;

    public void bloquear(Proceso p, int esperaCiclos) {
        if (p == null) return;
        p.setEstado(EstadoProceso.BLOQUEADO);
        Nodo n = new Nodo(p, Math.max(1, esperaCiclos));
        if (tail == null) { head = tail = n; }
        else { tail.next = n; tail = n; }
        size++;
    }

    /** Avanza 1 ciclo: decrementa espera y devuelve los procesos que pasan a LISTO. */
    public Proceso[] avanzarUnCicloYLiberar() {
        // 1ª pasada: contar cuántos se liberan
        int liberar = 0;
        for (Nodo n = head; n != null; n = n.next) {
            if (n.espera - 1 <= 0) liberar++;
        }
        if (liberar == 0) {
            // de todos modos decrementamos espera
            for (Nodo n = head; n != null; n = n.next) n.espera = Math.max(0, n.espera - 1);
            return new Proceso[0];
        }
        // 2ª pasada: decrementar, extraer liberados y quitar nodos
        Proceso[] arr = new Proceso[liberar];
        int i = 0;
        Nodo prev = null, curr = head;
        while (curr != null) {
            curr.espera = Math.max(0, curr.espera - 1);
            if (curr.espera == 0) {
                // extraer
                Proceso p = curr.value;
                p.setEstado(EstadoProceso.LISTO);
                arr[i++] = p;
                // eliminar nodo
                if (prev == null) { head = curr.next; }
                else { prev.next = curr.next; }
                if (curr == tail) tail = prev;
                size--;
                curr = (prev == null) ? head : prev.next;
            } else {
                prev = curr;
                curr = curr.next;
            }
        }
        return arr;
    }

    public boolean estaVacia() { return size == 0; }
    public int tamano() { return size; }

    /** Para mostrar en UI. */
    public String[] toDisplayStrings() {
        String[] rows = new String[size];
        int i = 0;
        for (Nodo n = head; n != null; n = n.next) {
            Proceso p = n.value;
            rows[i++] = String.format("PID %d | %-12s | espera: %d ciclos | %d/%d",
                    p.getPid(), p.getNombre(), n.espera, p.getRestantes(), p.getTotalInstrucciones());
        }
        return rows;
    }
}

