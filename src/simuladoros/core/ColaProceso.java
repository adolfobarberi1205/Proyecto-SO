/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladoros.core;

/**
 *
 * @author user
 *//** Cola FIFO hecha a mano para Proceso (sin usar ArrayList/Queue). */
public class ColaProceso {
   private static class Nodo {
        Proceso value;
        Nodo next;
        Nodo(Proceso v) { this.value = v; }
    }

    private Nodo head; // primero
    private Nodo tail; // último
    private int size = 0;

    public void encolar(Proceso p) {
        Nodo n = new Nodo(p);
        if (tail == null) {
            head = tail = n;
        } else {
            tail.next = n;
            tail = n;
        }
        size++;
    }

    public Proceso desencolar() {
        if (head == null) return null;
        Proceso v = head.value;
        head = head.next;
        if (head == null) tail = null;
        size--;
        return v;
    }

    public boolean estaVacia() { return size == 0; }
    public int tamano() { return size; }

    /** Fotografía del contenido actual de la cola. */
    public Proceso[] toArray() {
        Proceso[] arr = new Proceso[size];
        int i = 0;
        for (Nodo n = head; n != null; n = n.next) {
            arr[i++] = n.value;
        }
        return arr;
    }

    /** Saca y retorna el proceso con menor TOTAL de instrucciones. */
    public Proceso retirarMinPorTotal() {
        if (head == null) return null;
        Nodo prevMin = null, min = head;
        Nodo prev = null, cur = head;
        while (cur != null) {
            if (cur.value.getTotalInstrucciones() < min.value.getTotalInstrucciones()) {
                min = cur;
                prevMin = prev;
            }
            prev = cur;
            cur = cur.next;
        }
        return retirarNodo(min, prevMin);
    }

    /** Saca y retorna el proceso con menor RESTANTES. */
    public Proceso retirarMinPorRestantes() {
        if (head == null) return null;
        Nodo prevMin = null, min = head;
        Nodo prev = null, cur = head;
        while (cur != null) {
            if (cur.value.getRestantes() < min.value.getRestantes()) {
                min = cur;
                prevMin = prev;
            }
            prev = cur;
            cur = cur.next;
        }
        return retirarNodo(min, prevMin);
    }

    private Proceso retirarNodo(Nodo objetivo, Nodo previo) {
        if (objetivo == null) return null;
        Proceso v = objetivo.value;
        if (previo == null) { // objetivo es head
            head = objetivo.next;
            if (head == null) tail = null;
        } else {
            previo.next = objetivo.next;
            if (objetivo == tail) tail = previo;
        }
        size--;
        return v;
    }
}
