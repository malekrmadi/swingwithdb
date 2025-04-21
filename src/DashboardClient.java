import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class DashboardClient extends JFrame {
    private int clientId;

    public DashboardClient(int clientId) {
        this.clientId = clientId;

        setTitle("Dashboard Client");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] infosClient = getClientInfoFromDatabase(clientId);
        String nom = infosClient[0];
        String prenom = infosClient[1];

        JLabel label = new JLabel("Bienvenue " + prenom + " " + nom, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 16));

        add(label);
        setVisible(true);
    }

    private String[] getClientInfoFromDatabase(int clientId) {
        String nom = "Client";
        String prenom = "";

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT nom, prenom FROM clients WHERE client_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                nom = rs.getString("nom");
                prenom = rs.getString("prenom");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new String[]{nom, prenom};
    }
}
