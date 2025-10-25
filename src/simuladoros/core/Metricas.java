package simuladoros.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Recolector de métricas del simulador.
 * - Utilización de CPU (%)
 * - Throughput (procesos/100 ticks y acumulado)
 * - Tiempo de respuesta promedio
 * - Equidad (Índice de Jain) sobre tiempo de CPU recibido
 *
 * NOTA: No usa ArrayList/Queue. Solo mapas y arreglos primitivos.
 */
public final class Metricas {

    // ---- Snapshot inmutable para la UI ----
    public static final class Snapshot {
        public final long tickTotal;
        public final long cpuBusyTicks;
        public final int completados;
        public final double utilizacion;         // 0..100
        public final double throughputVentana;   // proc / 100 ticks
        public final double throughputAcum;      // proc / tick (normalizado a 100)
        public final double tRespuestaProm;      // en ticks
        public final double equidadJain;         // 0..1
        // Serie de puntos para graficar
        public final double[] serieUtil;         // últimos N puntos (0..100)
        public final double[] serieThpt;         // últimos N puntos (proc/100 ticks)

        private Snapshot(long tickTotal, long cpuBusyTicks, int completados, double utilizacion,
                         double throughputVentana, double throughputAcum,
                         double tRespuestaProm, double equidadJain,
                         double[] serieUtil, double[] serieThpt) {
            this.tickTotal = tickTotal;
            this.cpuBusyTicks = cpuBusyTicks;
            this.completados = completados;
            this.utilizacion = utilizacion;
            this.throughputVentana = throughputVentana;
            this.throughputAcum = throughputAcum;
            this.tRespuestaProm = tRespuestaProm;
            this.equidadJain = equidadJain;
            this.serieUtil = serieUtil;
            this.serieThpt = serieThpt;
        }
    }

    // ---- Estado por proceso ----
    private static final class PStats {
        long arrivalTick = -1;
        long firstStartTick = -1;    // primer tick en CPU
        long cpuTicks = 0;           // servicio recibido
        long completionTick = -1;
    }

    // ---- Configuración de las series ----
    private static final int VENTANA_THROUGHPUT = 100;    // tamaño ventana en ticks
    private static final int TAM_SERIE = 180;             // ~ 3min si cada 1s = 1 tick (ajustable)

    // Ring buffers primitivos (sin ArrayList)
    private static final class Ring {
        final double[] data;
        int idx = 0;
        boolean lleno = false;
        Ring(int n) { data = new double[n]; }
        void push(double v) {
            data[idx++] = v;
            if (idx >= data.length) { idx = 0; lleno = true; }
        }
        double[] snapshot() {
            int n = lleno ? data.length : idx;
            double[] out = new double[n];
            // devolver en orden temporal
            if (!lleno) {
                for (int i = 0; i < n; i++) out[i] = data[i];
            } else {
                int p = idx;
                for (int i = 0; i < n; i++) {
                    out[i] = data[(p + i) % data.length];
                }
            }
            return out;
        }
    }

    // ---- Estado global ----
    private long tickTotal = 0;
    private long cpuBusyTicks = 0;
    private int completados = 0;

    private final Map<Integer, PStats> procesos = new HashMap<>();
    private final Ring serieUtil = new Ring(TAM_SERIE);
    private final Ring serieThpt = new Ring(TAM_SERIE);

    // para throughput por ventana
    private final int[] completadosPorTick = new int[VENTANA_THROUGHPUT];
    private int cursorVentana = 0;
    private int sumVentana = 0;

    // ---- API DE INSTRUMENTACIÓN (llamar desde Kernel/CPU) ----

    /** Llamar cuando un proceso entra al sistema. */
    public synchronized void onArrive(int pid, long tickActual) {
        PStats ps = procesos.get(pid);
        if (ps == null) {
            ps = new PStats();
            procesos.put(pid, ps);
        }
        ps.arrivalTick = tickActual;
    }

    /** Llamar el primer tick que el proceso obtiene CPU. */
    public synchronized void onFirstStartIfNeeded(int pid, long tickActual) {
        PStats ps = procesos.get(pid);
        if (ps != null && ps.firstStartTick < 0) {
            ps.firstStartTick = tickActual;
        }
    }

    /** Llamar cada tick donde la CPU ejecuta al pid (servicio). */
    public synchronized void onCpuServiceTick(int pid) {
        PStats ps = procesos.get(pid);
        if (ps != null) ps.cpuTicks++;
    }

    /** Llamar cuando el proceso termina. */
    public synchronized void onComplete(int pid, long tickActual) {
        PStats ps = procesos.get(pid);
        if (ps != null) {
            ps.completionTick = tickActual;
            completados++;
            // marcar +1 en el tick actual para la ventana de throughput
            int prev = completadosPorTick[cursorVentana];
            sumVentana -= prev;
            completadosPorTick[cursorVentana] = 1;
            sumVentana += 1;
        }
    }

    /** Llamar en cada ciclo de reloj. */
    public synchronized void onTick(boolean cpuOcupada) {
        tickTotal++;
        if (cpuOcupada) cpuBusyTicks++;

        // avanzar ventana de throughput (si no hubo completion, es 0)
        if (completadosPorTick[cursorVentana] == 0) {
            // restar 0 no cambia sumVentana
        }
        // avanzar cursor y limpiar posición siguiente
        cursorVentana++;
        if (cursorVentana >= VENTANA_THROUGHPUT) cursorVentana = 0;
        sumVentana -= completadosPorTick[cursorVentana];
        completadosPorTick[cursorVentana] = 0;

        // actualizar series
        double util = getCpuUtilizacionPct();
        double thpt = getThroughputVentana();
        serieUtil.push(util);
        serieThpt.push(thpt);
    }

    // ---- Cálculos ----

    /** % de utilización acumulada. */
    private double getCpuUtilizacionPct() {
        if (tickTotal <= 0) return 0.0;
        return (cpuBusyTicks * 100.0) / (double) tickTotal;
    }

    /** proces/100 ticks en la ventana deslizante. */
    private double getThroughputVentana() {
        // sumVentana ya acumula cuántos completaron en los últimos 100 ticks
        return (sumVentana * 1.0); // ya está normalizado a “por 100 ticks”
    }

    /** proces/100 ticks acumulado global. */
    private double getThroughputAcumulado() {
        if (tickTotal <= 0) return 0.0;
        double porTick = completados / (double) tickTotal;
        return porTick * 100.0; // normalizamos a “por 100 ticks”
    }

    /** promedio (primerStart - arrival) para procesos que ya iniciaron. */
    private double getTiempoRespuestaProm() {
        long sum = 0;
        int n = 0;
        for (PStats ps : procesos.values()) {
            if (ps.arrivalTick >= 0 && ps.firstStartTick >= 0) {
                sum += (ps.firstStartTick - ps.arrivalTick);
                n++;
            }
        }
        return n == 0 ? 0.0 : (sum * 1.0) / n;
    }

    /** Índice de Jain sobre cpuTicks de procesos que han ejecutado algo. */
    private double getEquidadJain() {
        double sum = 0.0, sumSq = 0.0;
        int n = 0;
        for (PStats ps : procesos.values()) {
            if (ps.cpuTicks > 0) {
                double x = ps.cpuTicks;
                sum += x;
                sumSq += x * x;
                n++;
            }
        }
        if (n == 0) return 1.0; // trivialmente “justo” si nadie ejecutó aún
        return (sum * sum) / (n * sumSq + 1e-9);
    }

    // ---- Snapshot para UI ----
    public synchronized Snapshot snapshot() {
        return new Snapshot(
                tickTotal,
                cpuBusyTicks,
                completados,
                getCpuUtilizacionPct(),
                getThroughputVentana(),
                getThroughputAcumulado(),
                getTiempoRespuestaProm(),
                getEquidadJain(),
                serieUtil.snapshot(),
                serieThpt.snapshot()
        );
    }
}
