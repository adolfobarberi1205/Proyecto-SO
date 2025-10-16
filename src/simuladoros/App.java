/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package simuladoros;

import simuladoros.ui.VentanaPrincipal;
/**
 *
 * @author user
 */
public class App {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
         System.out.println("Iniciando simulador de Sistema Operativo...");
        javax.swing.SwingUtilities.invokeLater(() -> {
            new VentanaPrincipal().setVisible(true);
        });
    }
}
    
    

