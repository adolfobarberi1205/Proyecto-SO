package simuladoros.ui;

import simuladoros.core.Metricas;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

/**
 * Panel Swing que muestra:
 *  - KPIs numéricos (Utilización, Throughput, T. respuesta, Equidad)
 *  - Dos gráficas en tiempo real (línea simple):
 *      1) Utilización CPU (%)
 *      2) Throughput (proc/100 ticks)
 *
 * Sin librerías externas; usa Java2D.
 */
public class PanelMetricas extends JPanel {

    private final JLabel lblUtil = new JLabel();
    private final JLabel lblThptV = new JLabel();
    private final JLabel lblThptA = new JLabel();
    private final JLabel lblResp = new JLabel();
    private final JLabel lblJain = new JLabel();

    private double[] serieUtil = new double[0];
    private double[] serieThpt = new double[0];

    private static final DecimalFormat DF1 = new DecimalFormat("0.0");
    private static final DecimalFormat DF2 = new DecimalFormat("0.00");

    public PanelMetricas() {
        setLayout(new BorderLayout(8,8));

        // KPIs arriba
        JPanel kpis = new JPanel(new GridLayout(2,3,8,4));
        kpis.setBorder(BorderFactory.createTitledBorder("Indicadores de rendimiento"));
        kpis.add(new JLabel("CPU Utilización (%):"));
        kpis.add(new JLabel("Throughput Ventana (proc/100t):"));
        kpis.add(new JLabel("Throughput Acum. (proc/100t):"));
        kpis.add(lblUtil);
        kpis.add(lblThptV);
        kpis.add(lblThptA);

        JPanel kpis2 = new JPanel(new GridLayout(1,2,8,4));
        kpis2.add(new JLabel("Tiempo de respuesta prom. (ticks):"));
        kpis2.add(new JLabel("Equidad (Jain 0..1):"));

        JPanel kpis2v = new JPanel(new GridLayout(1,2,8,4));
        kpis2v.add(lblResp);
        kpis2v.add(lblJain);

        JPanel top = new JPanel(new BorderLayout());
        top.add(kpis, BorderLayout.NORTH);
        top.add(kpis2, BorderLayout.CENTER);
        top.add(kpis2v, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);

        // Zona de gráficos
        JPanel charts = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                
                Insets ins = getInsets();
                int ox = ins.left;
                int oy = ins.top;
                int w = getWidth() - ins.left - ins.right;;
                int h = getHeight() - ins.top  - ins.bottom;;
                int mid = h/2;

                // panel superior: Utilización
                dibujarSerie(g2, ox, oy,          w, mid - 4, serieUtil,  0, 100,
            "Utilización CPU (%)");

                // panel inferior: Throughput
                dibujarSerie(g2, ox, oy + mid + 4, w, h - (mid + 4), serieThpt, 0, escalaMax(serieThpt),
            "Throughput (proc/100 ticks)");
            }
        };
        charts.setBorder(BorderFactory.createTitledBorder("Gráficas en tiempo real"));
        add(charts, BorderLayout.CENTER);
    }

    private static double escalaMax(double[] s) {
        double m = 1.0;
        for (double v : s) if (v > m) m = v;
        return m;
    }

    private static void dibujarSerie(Graphics2D g2, int x, int y, int w, int h,
                                     double[] serie, double min, double max,
                                     String titulo) {
        // marco
        g2.setColor(Color.DARK_GRAY);
        final int leftPad   = 40;
        final int topPad    = 28;  // deja espacio debajo del título "Gráficas en tiempo real"
        final int rightPad  = 60;
        final int bottomPad = 40;
        
        g2.drawString(titulo, x + leftPad, y + topPad - 10);
        
        // área interna de dibujo
        int gx = x + leftPad;
        int gy = y + topPad;
        int gw = w - rightPad;
        int gh = h - bottomPad;
        
        // marco
        g2.drawRect(gx, gy, gw, gh);

        if (serie == null || serie.length < 2) return;

        //int gx = x+40, gy = y+20, gw = w-60, gh = h-40;

        // eje y (ticks)
        g2.setFont(g2.getFont().deriveFont(10f));
        for (int i=0;i<=4;i++){
            int yy = gy + gh - (int)((i/4.0)*gh);
            g2.setColor(new Color(0,0,0,30));
            g2.drawLine(gx, yy, gx+gw, yy);
            g2.setColor(Color.GRAY);
            double v = min + (i/4.0)*(max-min);
            g2.drawString(DF1.format(v), gx-30, yy+4);
        }

        // polilínea
        g2.setColor(new Color(20,120,220));
        int n = serie.length;
        for (int i=1; i<n; i++){
            double v1 = serie[i-1], v2 = serie[i];
            int px1 = gx + (i-1)*gw/(n-1);
            int px2 = gx + i*gw/(n-1);
            int py1 = gy + gh - (int)((v1 - min) * gh / Math.max(1e-9,(max-min)));
            int py2 = gy + gh - (int)((v2 - min) * gh / Math.max(1e-9,(max-min)));
            g2.drawLine(px1, py1, px2, py2);
        }
    }

    /** Llamar periódicamente desde la UI con el snapshot de Metricas. */
    public void updateFrom(Metricas.Snapshot s) {
        if (s == null) return;
        lblUtil.setText(DF1.format(s.utilizacion));
        lblThptV.setText(DF1.format(s.throughputVentana));
        lblThptA.setText(DF1.format(s.throughputAcum));
        lblResp.setText(DF1.format(s.tRespuestaProm));
        lblJain.setText(DF2.format(s.equidadJain));
        this.serieUtil = s.serieUtil;
        this.serieThpt = s.serieThpt;
        repaint();
    }
}
