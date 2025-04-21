import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DashboardSuperAdmin extends JFrame {
    public DashboardSuperAdmin() {
        setTitle("Dashboard Super Admin");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(7, 1, 10, 10));

        JButton btnAppartements = new JButton("Appartements");
        JButton btnLocations = new JButton("Locations");
        JButton btnStatistiques = new JButton("Statistiques");
        JButton btnClients = new JButton("Clients");
        JButton btnGestionAdmins = new JButton("Gestion des Admins");
        JButton btnLogout = new JButton("Déconnexion");

        add(new JLabel("Bienvenue Super Admin", SwingConstants.CENTER));
        add(btnAppartements);
        add(btnLocations);
        add(btnStatistiques);
        add(btnClients);
        add(btnGestionAdmins);
        add(btnLogout);

        btnAppartements.addActionListener(e -> {
            dispose();
            new AdminAppartements(true); // Appel correct
        });

        btnLocations.addActionListener(e -> {
            dispose();
            new AdminLocations(true); // Appel correct
        });

        btnStatistiques.addActionListener(e -> {
            dispose();
            new AdminStats(true); // Appel correct
        });

        btnClients.addActionListener(e -> {
            dispose();
            new AdminClients(true); // Appel correct
        });

        btnGestionAdmins.addActionListener(e -> {
            dispose();
            new AdminAdmins(); // Remplacez par la gestion des admins si nécessaire
        });

        btnLogout.addActionListener(e -> {
            dispose();
            new Login(); // Remplacez par votre écran de login
        });

        setVisible(true);
    }
}
