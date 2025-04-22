import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Login extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton, createAccountButton;
    private Color primaryColor = new Color(41, 128, 185); // Blue
    private Color lightColor = new Color(236, 240, 241); // Light gray
    private Color textColor = new Color(44, 62, 80); // Dark blue/gray
    private Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private Font headerFont = new Font("Segoe UI", Font.BOLD, 24);

    public Login() {
        setTitle("Connexion");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(primaryColor);
        headerPanel.setPreferredSize(new Dimension(800, 100));
        headerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        
        JLabel titleLabel = new JLabel("Gestion de Location");
        titleLabel.setFont(headerFont);
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        add(headerPanel, BorderLayout.NORTH);

        // Login form panel
        JPanel formPanel = new JPanel();
        formPanel.setBackground(Color.WHITE);
        formPanel.setLayout(null);
        
        JLabel loginLabel = new JLabel("Connectez-vous");
        loginLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        loginLabel.setForeground(textColor);
        loginLabel.setBounds(300, 30, 200, 30);
        formPanel.add(loginLabel);
        
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(mainFont);
        emailLabel.setForeground(textColor);
        emailLabel.setBounds(250, 100, 100, 25);
        formPanel.add(emailLabel);
        
        emailField = new JTextField();
        emailField.setFont(mainFont);
        emailField.setBounds(250, 130, 300, 35);
        emailField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        formPanel.add(emailField);
        
        JLabel passwordLabel = new JLabel("Mot de passe:");
        passwordLabel.setFont(mainFont);
        passwordLabel.setForeground(textColor);
        passwordLabel.setBounds(250, 180, 150, 25);
        formPanel.add(passwordLabel);
        
        passwordField = new JPasswordField();
        passwordField.setFont(mainFont);
        passwordField.setBounds(250, 210, 300, 35);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        formPanel.add(passwordField);
        
        loginButton = new JButton("Connexion");
        loginButton.setFont(mainFont);
        loginButton.setBounds(250, 270, 300, 40);
        loginButton.setBackground(primaryColor);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder());
        formPanel.add(loginButton);
        
        createAccountButton = new JButton("Créer un compte Client");
        createAccountButton.setFont(mainFont);
        createAccountButton.setBounds(250, 330, 300, 40);
        createAccountButton.setBackground(lightColor);
        createAccountButton.setForeground(textColor);
        createAccountButton.setFocusPainted(false);
        createAccountButton.setBorder(BorderFactory.createEmptyBorder());
        formPanel.add(createAccountButton);
        
        add(formPanel, BorderLayout.CENTER);

        // Footer panel
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(lightColor);
        footerPanel.setPreferredSize(new Dimension(800, 50));
        
        JLabel footerLabel = new JLabel("© 2023 Gestion de Location. Tous droits réservés.");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerLabel.setForeground(textColor);
        footerPanel.add(footerLabel);
        
        add(footerPanel, BorderLayout.SOUTH);

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String email = emailField.getText();
                String password = new String(passwordField.getPassword());

                try (Connection conn = DBConnection.getConnection()) {

                    // Vérifier si l'utilisateur est un admin
                    String sqlAdmin = "SELECT * FROM admin WHERE email = ? AND mot_de_passe = ?";
                    PreparedStatement stmtAdmin = conn.prepareStatement(sqlAdmin);
                    stmtAdmin.setString(1, email);
                    stmtAdmin.setString(2, password);
                    ResultSet rsAdmin = stmtAdmin.executeQuery();

                    if (rsAdmin.next()) {
                        boolean isSuperAdmin = rsAdmin.getInt("superadmin") == 1;
                        dispose();
                        if (isSuperAdmin) {
                            new DashboardSuperAdmin();
                        } else {
                            new DashboardAdmin();
                        }
                        return;
                    }

                    // Vérifier si l'utilisateur est un client
                    String sqlClient = "SELECT * FROM clients WHERE email = ? AND mot_de_passe = ?";
                    PreparedStatement stmtClient = conn.prepareStatement(sqlClient);
                    stmtClient.setString(1, email);
                    stmtClient.setString(2, password);
                    ResultSet rsClient = stmtClient.executeQuery();

                    if (rsClient.next()) {
                        int clientId = rsClient.getInt("client_id");
                        dispose();
                        new DashboardClient(clientId);
                        return;
                    }

                    // Si aucun compte correspondant
                    JOptionPane.showMessageDialog(Login.this, "Email ou mot de passe incorrect.", "Erreur", JOptionPane.ERROR_MESSAGE);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(Login.this, "Erreur de connexion à la base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Bouton pour créer un compte
        createAccountButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose(); // Fermer la fenêtre de login
                new CreateAccountClient(); // Ouvrir le formulaire de création de compte
            }
        });

        setVisible(true);
    }
}
