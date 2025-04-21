import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DashboardAdmin extends JFrame {
    public DashboardAdmin() {
        setTitle("Dashboard Admin");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(6, 1, 10, 10));

        JButton btnAppartements = new JButton("Appartements");
        JButton btnLocations = new JButton("Locations");
        JButton btnStatistiques = new JButton("Statistiques");
        JButton btnClients = new JButton("Clients");
        JButton btnLogout = new JButton("Déconnexion");

        add(new JLabel("Bienvenue Admin", SwingConstants.CENTER));
        add(btnAppartements);
        add(btnLocations);
        add(btnStatistiques);
        add(btnClients);
        add(btnLogout);

        btnAppartements.addActionListener(e -> {
            dispose();
            new AdminAppartements(false); // Appel correct
        });

        btnLocations.addActionListener(e -> {
            dispose();
            new AdminLocations(false); // Appel correct
        });

        btnStatistiques.addActionListener(e -> {
            dispose();
            new AdminStats(false); // Appel correct
        });

        btnClients.addActionListener(e -> {
            dispose();
            new AdminClients(false); // Appel correct
        });

        btnLogout.addActionListener(e -> {
            dispose();
            new Login(); // Remplacez par votre écran de login
        });

        setVisible(true);
    }
}
