import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DashboardSuperAdmin extends JFrame {
    private Color accentColor = new Color(142, 68, 173);
    private Color lightColor = new Color(236, 240, 241);
    private Color textColor = new Color(44, 62, 80);
    private Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private Font headerFont = new Font("Segoe UI", Font.BOLD, 24);

    public DashboardSuperAdmin() {
        setTitle("Dashboard Super Admin");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(accentColor);
        headerPanel.setPreferredSize(new Dimension(800, 80));
        JLabel welcomeLabel = new JLabel("  Bienvenue Super Admin");
        welcomeLabel.setFont(headerFont);
        welcomeLabel.setForeground(Color.WHITE);
        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Main content without sidebar
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Tableau de bord Super Admin");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(textColor);
        title.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 0));
        contentPanel.add(title, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(3, 2, 20, 20));
        cards.setBackground(Color.WHITE);
        cards.setBorder(BorderFactory.createEmptyBorder(30, 20, 20, 20));

        cards.add(createDashboardCard("Gestion des Appartements", "Ajoutez, modifiez et supprimez des appartements", new Color(52, 152, 219), "Appartements"));
        cards.add(createDashboardCard("Gestion des Locations", "Consultez et gerez les locations", new Color(46, 204, 113), "Locations"));
        cards.add(createDashboardCard("Gestion des Clients", "Gerez les comptes clients", new Color(155, 89, 182), "Clients"));
        cards.add(createDashboardCard("Statistiques", "Consultez les statistiques de l'application", new Color(230, 126, 34), "Statistiques"));
        cards.add(createDashboardCard("Gestion des Admins", "Gerez les administrateurs", new Color(231, 76, 60), "Gestion des Admins"));

        contentPanel.add(cards, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    private JPanel createDashboardCard(String title, String desc, Color color, String route) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(color);
        header.setPreferredSize(new Dimension(100, 40));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);
        header.add(titleLabel);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel descLabel = new JLabel("<html><div style='width:150px'>" + desc + "</div></html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(textColor);
        content.add(descLabel, BorderLayout.CENTER);

        card.add(header, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);

        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                navigate(route);
            }
        });

        return card;
    }

    private void navigate(String destination) {
        dispose();
        switch (destination) {
            case "Appartements": new AdminAppartements(true); break;
            case "Locations": new AdminLocations(true); break;
            case "Clients": new AdminClients(true); break;
            case "Statistiques": new AdminStats(true); break;
            case "Gestion des Admins": new AdminAdmins(); break;
        }
    }
}
