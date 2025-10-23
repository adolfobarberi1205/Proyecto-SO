/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladoros.config;

import simuladoros.core.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** Persistencia simple en CSV: escenarios y resultados + MÉTRICAS. */
public class ArchivoConfig {

    private static final String SEP = ",";

    /** Guarda MÉTRICAS (al inicio) y luego el estado de procesos (Listos, CPU, Terminados). */
    public static void guardarCSV(File destino, Kernel k) throws IOException {
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(destino), StandardCharsets.UTF_8))) {

            // ======= MÉTRICAS =======
            long ciclosTot = k.getCiclosTotales();
            long cpuOcc    = k.getCiclosCpuOcupada();
            long term      = k.getProcesosCompletados();
            double usoCpu  = (ciclosTot == 0) ? 0.0 : (100.0 * cpuOcc / ciclosTot);
            String usoStr  = String.format(Locale.US, "%.2f", usoCpu);

            pw.println("# METRICAS");
            pw.println("ciclos_totales"      + SEP + ciclosTot);
            pw.println("ciclos_cpu_ocupada"  + SEP + cpuOcc);
            pw.println("uso_cpu_porcentaje"  + SEP + usoStr);
            pw.println("procesos_terminados" + SEP + term);
            pw.println(); // línea en blanco

            // ======= TABLA DE PROCESOS =======
            pw.println("PID,Nombre,Tipo,Prioridad,Total,Restantes,Estado,Arrival,Start,Completion");

            // Listos
            for (Proceso p : k.snapshotListos()) pw.println(row(p));

            // CPU actual (si hay)
            Proceso actual = k.getProcesoActual();
            if (actual != null) pw.println(row(actual));

            // Terminados
            for (Proceso p : k.snapshotTerminados()) pw.println(row(p));
        }
    }

    /** Carga procesos (solo datos básicos) a la cola de listos. Ignora PID del archivo y reasigna nuevos. */
    public static void cargarCSV(File origen, Kernel k) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(origen), StandardCharsets.UTF_8))) {
            String line;
            boolean enTabla = false;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Saltar sección de métricas / comentarios
                if (line.startsWith("#")) continue;
                if (line.equalsIgnoreCase("PID,Nombre,Tipo,Prioridad,Total,Restantes,Estado,Arrival,Start,Completion")) {
                    enTabla = true;
                    continue;
                }
                if (!enTabla) continue;

                String[] c = line.split(SEP);
                if (c.length < 6) continue;

                String nombre = c[1].trim();
                TipoProceso tipo = "IO_BOUND".equalsIgnoreCase(c[2].trim()) ? TipoProceso.IO_BOUND : TipoProceso.CPU_BOUND;
                int prioridad = parseIntSafe(c[3], 0);
                int total = parseIntSafe(c[4], 1);

                k.crearProceso(nombre, tipo, total, prioridad);
            }
        }
    }

    // Helpers
    private static String row(Proceso p) {
        return p.getPid() + SEP
                + safe(p.getNombre()) + SEP
                + p.getTipo() + SEP
                + p.getPrioridad() + SEP
                + p.getTotalInstrucciones() + SEP
                + p.getRestantes() + SEP
                + p.getEstado() + SEP
                + p.getArrivalCiclo() + SEP
                + p.getStartCiclo() + SEP
                + p.getCompletionCiclo();
    }

    private static String safe(String s) { return s == null ? "" : s.replace(",", " "); }

    private static int parseIntSafe(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }
}
