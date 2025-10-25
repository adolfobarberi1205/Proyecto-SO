/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladoros.core;

/**
 *
 * @author user
 */
public class Reloj extends Thread {
     private volatile int cicloActual = 0;
    private volatile int duracionCiclo = 500; // ms
    private volatile boolean activo = false;
    private volatile boolean pausado = false;

    private ciclolistener listener;

    public void setListener(ciclolistener l) {
        this.listener = l;
    }

    public void iniciar() {
        if (activo) return;
        activo = true;
        this.start();
    }

    public void detener() {
        activo = false;
        this.interrupt();
    }

    public void pausar() {
        pausado = true;
    }

    public void continuar() {
        pausado = false;
    }

    public boolean isPausado() {
        return pausado;
    }

    public void setDuracionCiclo(int ms) {
        if (ms < 10) ms = 10;
        this.duracionCiclo = ms;
    }

    public int getDuracionCiclo() {
        return duracionCiclo;
    }

    public int getCicloActual() {
        return cicloActual;
    }

    @Override
    public void run() {
        while (activo) {
            if (pausado) {
                dormir(50);
                continue;
            }
            cicloActual++;
            if (listener != null) {
                try {
                    listener.onTick(cicloActual, System.currentTimeMillis());
                } catch (Exception ignored) { /* no romper el hilo por UI */ }
            }
            dormir(duracionCiclo);
        }
    }

    private void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            // salir si nos detuvieron
            if (!activo) Thread.currentThread().interrupt();
        }
    }
}