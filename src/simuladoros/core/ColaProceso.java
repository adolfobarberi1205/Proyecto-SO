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

    /** Fotografía inmutable del contenido actual de la cola. */
    public Proceso[] toArray() {
        Proceso[] arr = new Proceso[size];
        int i = 0;
        for (Nodo n = head; n != null; n = n.next) {
            arr[i++] = n.value;
        }
        return arr;
    }
}
