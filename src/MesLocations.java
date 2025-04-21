import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;


public class MesLocations extends JFrame {
    private int clientId;

    public MesLocations(int clientId) {
        this.clientId = clientId;

        setTitle("Mes Locations");
        setSize(600, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel label = new JLabel("Mes Locations", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        add(label, BorderLayout.NORTH);

        // Table pour afficher les données
        String[] columnNames = {"ID Location", "Appartement", "Date Début", "Date Fin", "Personnes", "Pénalité"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Charger les données depuis la BDD
        loadLocations(model);

        // Bouton retour
        JButton backButton = new JButton("Retour au Dashboard");
        backButton.addActionListener(e -> {
            dispose();
            new DashboardClient(clientId);
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void loadLocations(DefaultTableModel model) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT l.location_id, a.nom AS appartement, l.date_debut, l.date_fin, l.nombre_personnes, l.penalite_retard " +
                         "FROM locations l JOIN appartements a ON l.appartement_id = a.appartement_id " +
                         "WHERE l.client_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("location_id");
                String nomAppartement = rs.getString("appartement");
                Date debut = rs.getDate("date_debut");
                Date fin = rs.getDate("date_fin");
                int personnes = rs.getInt("nombre_personnes");
                double penalite = rs.getDouble("penalite_retard");

                model.addRow(new Object[]{id, nomAppartement, debut, fin, personnes, penalite});
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des locations : " + e.getMessage());
        }
    }
}
