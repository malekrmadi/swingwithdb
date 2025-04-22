import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class DashboardClient extends JFrame {
    private int clientId;
    private Color primaryColor = new Color(41, 128, 185); // Blue
    private Color accentColor = new Color(26, 188, 156); // Green
    private Color lightColor = new Color(236, 240, 241); // Light gray
    private Color textColor = new Color(44, 62, 80); // Dark blue/gray
    private Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private Font headerFont = new Font("Segoe UI", Font.BOLD, 24);
    private Font subHeaderFont = new Font("Segoe UI", Font.BOLD, 18);

    public DashboardClient(int clientId) {
        this.clientId = clientId;

        setTitle("Dashboard Client");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(primaryColor);
        headerPanel.setPreferredSize(new Dimension(800, 80));
        headerPanel.setLayout(new BorderLayout());
        
        String nomComplet = getClientFullNameFromDatabase(clientId);

        JLabel welcomeLabel = new JLabel("  Bienvenue " + nomComplet);
        welcomeLabel.setFont(headerFont);
        welcomeLabel.setForeground(Color.WHITE);
        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        
        add(headerPanel, BorderLayout.NORTH);

        // Main content panel with sidebar layout
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        
        // Sidebar panel
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setBackground(lightColor);
        sidebarPanel.setPreferredSize(new Dimension(200, 500));
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        
        JLabel dashboardLabel = new JLabel("Menu Client");
        dashboardLabel.setFont(subHeaderFont);
        dashboardLabel.setForeground(textColor);
        dashboardLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        dashboardLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        sidebarPanel.add(dashboardLabel);
        
        // Dashboard buttons
        JButton btnSearch = createMenuButton("Trouver Appartement");
        JButton btnMyRentals = createMenuButton("Mes Locations");
        JButton btnProfile = createMenuButton("Mon Profil");
        JButton btnLogout = createMenuButton("Déconnexion");
        
        sidebarPanel.add(btnSearch);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebarPanel.add(btnMyRentals);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebarPanel.add(btnProfile);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebarPanel.add(btnLogout);
        
        contentPanel.add(sidebarPanel, BorderLayout.WEST);
        
        // Dashboard content panel
        JPanel dashboardContent = new JPanel();
        dashboardContent.setBackground(Color.WHITE);
        dashboardContent.setLayout(new BorderLayout());
        
        JLabel welcomeTitle = new JLabel("Tableau de bord Client");
        welcomeTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        welcomeTitle.setForeground(textColor);
        welcomeTitle.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 0));
        dashboardContent.add(welcomeTitle, BorderLayout.NORTH);
        
        // Dashboard cards
        JPanel cardsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        cardsPanel.setBackground(Color.WHITE);
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 20, 20));
        
        // Card 1
        JPanel card1 = createDashboardCard("Trouver un Appartement", "Recherchez et filtrez parmi nos appartements disponibles", new Color(52, 152, 219));
        cardsPanel.add(card1);
        
        // Card 2
        JPanel card2 = createDashboardCard("Mes Locations", "Gérez vos locations actuelles et passées", new Color(46, 204, 113));
        cardsPanel.add(card2);
        
        // Card 3
        JPanel card3 = createDashboardCard("Mon Profil", "Consultez et modifiez vos informations personnelles", new Color(155, 89, 182));
        cardsPanel.add(card3);
        
        // Card 4
        JPanel card4 = createDashboardCard("Aide & Support", "Contactez-nous pour toute assistance", new Color(230, 126, 34));
        cardsPanel.add(card4);
        
        dashboardContent.add(cardsPanel, BorderLayout.CENTER);
        contentPanel.add(dashboardContent, BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
        
        // Footer panel
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(lightColor);
        footerPanel.setPreferredSize(new Dimension(800, 40));
        
        JLabel footerLabel = new JLabel("© 2023 Gestion de Location. Tous droits réservés.");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerLabel.setForeground(textColor);
        footerPanel.add(footerLabel);
        
        add(footerPanel, BorderLayout.SOUTH);

        // Actions des boutons
        btnSearch.addActionListener(e -> {dispose(); new SearchAppartement(clientId);});
        btnMyRentals.addActionListener(e -> {dispose(); new MesLocations(clientId);});
        btnProfile.addActionListener(e -> {dispose(); new MonProfil(clientId);});
        btnLogout.addActionListener(e -> {
            dispose();
            new Login();
        });
        
        // Add action listeners to cards
        card1.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                dispose();
                new SearchAppartement(clientId);
            }
        });
        
        card2.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                dispose();
                new MesLocations(clientId);
            }
        });
        
        card3.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                dispose();
                new MonProfil(clientId);
            }
        });

        setVisible(true);
    }
    
    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(mainFont);
        button.setForeground(textColor);
        button.setBackground(lightColor);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(180, 40));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(224, 224, 224));
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(lightColor);
            }
        });
        
        return button;
    }
    
    private JPanel createDashboardCard(String title, String description, Color cardColor) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JPanel headerCard = new JPanel();
        headerCard.setBackground(cardColor);
        headerCard.setPreferredSize(new Dimension(100, 40));
        headerCard.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        headerCard.add(titleLabel);
        
        JPanel contentCard = new JPanel();
        contentCard.setBackground(Color.WHITE);
        contentCard.setLayout(new BorderLayout());
        contentCard.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel descLabel = new JLabel("<html><div style='width:150px'>" + description + "</div></html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(textColor);
        contentCard.add(descLabel, BorderLayout.CENTER);
        
        card.add(headerCard, BorderLayout.NORTH);
        card.add(contentCard, BorderLayout.CENTER);
        
        return card;
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
