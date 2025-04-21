import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class DashboardClient extends JFrame {
    private int clientId;

    public DashboardClient(int clientId) {
        this.clientId = clientId;

        setTitle("Dashboard Client");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        String nomComplet = getClientFullNameFromDatabase(clientId);

        JLabel welcomeLabel = new JLabel("Bienvenue " + nomComplet, SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(welcomeLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        JButton btnSearch = new JButton("Trouver Appartement");
        JButton btnMyRentals = new JButton("Mes Locations");
        JButton btnProfile = new JButton("Mon Profil");
        JButton btnLogout = new JButton("Déconnexion");

        buttonPanel.add(btnSearch);
        buttonPanel.add(btnMyRentals);
        buttonPanel.add(btnProfile);
        buttonPanel.add(btnLogout);

        add(buttonPanel, BorderLayout.CENTER);

        // Actions des boutons
        btnSearch.addActionListener(e -> {dispose(); new SearchAppartement();});
        btnMyRentals.addActionListener(e -> {dispose(); new MesLocations(clientId);});
        btnProfile.addActionListener(e -> {dispose(); new MonProfil(clientId);});
        btnLogout.addActionListener(e -> {
            dispose();
            new Login();
        });

        setVisible(true);
    }

    private String getClientFullNameFromDatabase(int clientId) {
        String nom = "Client";

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT nom, prenom FROM clients WHERE client_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                nom = rs.getString("prenom") + " " + rs.getString("nom");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return nom;
    }
}
