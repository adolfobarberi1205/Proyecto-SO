/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladoros.ui;
import javax.swing.*;
import java.awt.*;
/**
 *
 * @author user
 */
public class VentanaPrincipal  extends JFrame{
    // Componentes principales
    private JLabel lblTitulo;
    private JLabel lblCiclo;
    private JButton btnIniciar;

    public VentanaPrincipal() {
        initUI();
    }

    /**
     * Inicializa la interfaz gráfica básica.
     */
    private void initUI() {
        setTitle("Simulador de Sistema Operativo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 300);
        setLocationRelativeTo(null); // Centrar ventana

        lblTitulo = new JLabel("Simulador de Planificación de Procesos", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));

        lblCiclo = new JLabel("Ciclo actual: 0", SwingConstants.CENTER);
        lblCiclo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        btnIniciar = new JButton("Iniciar Simulación");

        // Diseño
        setLayout(new BorderLayout());
        add(lblTitulo, BorderLayout.NORTH);
        add(lblCiclo, BorderLayout.CENTER);
        add(btnIniciar, BorderLayout.SOUTH);
    }
    
}
