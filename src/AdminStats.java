import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.*;

public class AdminStats extends JFrame {
    public AdminStats(boolean isSuperAdmin) {
        setTitle("Statistiques");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Monthly Revenue", new MonthlyRevenuePanel());
        tabs.add("Top Clients", new TopClientsPanel());
        tabs.add("Apartment Occupancy", new ApartmentOccupancyPanel());

        add(tabs, BorderLayout.CENTER);

        JButton backButton = new JButton("Retour au Dashboard");
        backButton.addActionListener(e -> {
            dispose();
            if (isSuperAdmin) new DashboardSuperAdmin();
            else new DashboardAdmin();
        });

        add(backButton, BorderLayout.SOUTH);
        setVisible(true);
    }
}

class MonthlyRevenuePanel extends JPanel {
    Map<String, Double> data = new LinkedHashMap<>();

    public MonthlyRevenuePanel() {
        fetchData();
    }

    private void fetchData() {
        String query = """
            SELECT MONTH(date_debut) AS mois, SUM(DATEDIFF(date_fin, date_debut) * a.prix_par_nuit) AS revenu
            FROM locations l
            JOIN appartements a ON l.appartement_id = a.appartement_id
            GROUP BY mois
            ORDER BY mois
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int month = rs.getInt("mois");
                double revenu = rs.getDouble("revenu");
                data.put(getMonthName(month), revenu);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getMonthName(int month) {
        return new String[]{"Janv", "Fév", "Mars", "Avr", "Mai", "Juin", "Juil", "Août", "Sept", "Oct", "Nov", "Déc"}[month - 1];
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g;
        int height = getHeight();
        int x = 60;
        double max = Collections.max(data.values());

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            int barHeight = (int) (entry.getValue() / max * (height - 100));
            g2.setColor(Color.BLUE);
            g2.fillRect(x, height - barHeight - 50, 30, barHeight);
            g2.setColor(Color.BLACK);
            g2.drawString(entry.getKey(), x, height - 30);
            x += 50;
        }
    }
}

class TopClientsPanel extends JPanel {
    Map<String, Double> data = new LinkedHashMap<>();

    public TopClientsPanel() {
        fetchData();
    }

    private void fetchData() {
        String query = """
            SELECT c.nom, SUM(DATEDIFF(l.date_fin, l.date_debut) * a.prix_par_nuit) AS revenu
            FROM locations l
            JOIN clients c ON l.client_id = c.client_id
            JOIN appartements a ON l.appartement_id = a.appartement_id
            GROUP BY c.client_id
            ORDER BY revenu DESC
            LIMIT 5
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                data.put(rs.getString("nom"), rs.getDouble("revenu"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g;
        int height = getHeight();
        int x = 60;
        double max = Collections.max(data.values());

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            int barHeight = (int) (entry.getValue() / max * (height - 100));
            g2.setColor(Color.ORANGE);
            g2.fillRect(x, height - barHeight - 50, 40, barHeight);
            g2.setColor(Color.BLACK);
            g2.drawString(entry.getKey(), x, height - 30);
            x += 100;
        }
    }
}

class ApartmentOccupancyPanel extends JPanel {
    Map<String, Integer> data = new HashMap<>();

    public ApartmentOccupancyPanel() {
        fetchData();
    }

    private void fetchData() {
        String query = """
            SELECT type_appartement, COUNT(*) AS count
            FROM locations l
            JOIN appartements a ON l.appartement_id = a.appartement_id
            GROUP BY type_appartement
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                data.put(rs.getString("type_appartement"), rs.getInt("count"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g;
        int total = data.values().stream().mapToInt(i -> i).sum();
        int startAngle = 0;
        int x = getWidth() / 2 - 150, y = 50;

        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.ORANGE};
        int i = 0;

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            int angle = (int) Math.round(entry.getValue() * 360.0 / total);
            g2.setColor(colors[i % colors.length]);
            g2.fillArc(x, y, 300, 300, startAngle, angle);
            g2.fillRect(x + 320, y + i * 20, 15, 15);
            g2.setColor(Color.BLACK);
            g2.drawString(entry.getKey(), x + 340, y + 15 + i * 20);
            startAngle += angle;
            i++;
        }
    }
}
