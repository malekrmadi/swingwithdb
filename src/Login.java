import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Login extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton, createAccountButton;
    private Color primaryColor = new Color(52, 152, 219);
    private Color backgroundColor = new Color(245, 245, 245);
    private Color buttonColor = new Color(41, 128, 185);
    private Color textColor = new Color(44, 62, 80);
    private Font mainFont = new Font("SansSerif", Font.PLAIN, 14);
    private Font titleFont = new Font("SansSerif", Font.BOLD, 22);

    public Login() {
        setTitle("Connexion");
        setSize(500, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(backgroundColor);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(primaryColor);
        headerPanel.setPreferredSize(new Dimension(500, 70));
        JLabel headerLabel = new JLabel("Gestion de Location");
        headerLabel.setFont(titleFont);
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(backgroundColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JLabel loginLabel = new JLabel("Connexion");
        loginLabel.setFont(titleFont);
        loginLabel.setForeground(textColor);
        gbc.gridy = 0;
        formPanel.add(loginLabel, gbc);

        JLabel emailLabel = new JLabel("Email");
        emailLabel.setFont(mainFont);
        gbc.gridy = 1;
        formPanel.add(emailLabel, gbc);

        emailField = new JTextField();
        emailField.setFont(mainFont);
        emailField.setBorder(new LineBorder(Color.LIGHT_GRAY));
        gbc.gridy = 2;
        formPanel.add(emailField, gbc);

        JLabel passwordLabel = new JLabel("Mot de passe");
        passwordLabel.setFont(mainFont);
        gbc.gridy = 3;
        formPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField();
        passwordField.setFont(mainFont);
        passwordField.setBorder(new LineBorder(Color.LIGHT_GRAY));
        gbc.gridy = 4;
        formPanel.add(passwordField, gbc);

        loginButton = new JButton("Connexion");
        loginButton.setBackground(buttonColor);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setFont(mainFont);
        gbc.gridy = 5;
        formPanel.add(loginButton, gbc);

        createAccountButton = new JButton("Creer un compte client");
        createAccountButton.setBackground(Color.WHITE);
        createAccountButton.setForeground(buttonColor);
        createAccountButton.setFont(mainFont);
        createAccountButton.setFocusPainted(false);
        gbc.gridy = 6;
        formPanel.add(createAccountButton, gbc);

        add(formPanel, BorderLayout.CENTER);



        // Login button logic
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String email = emailField.getText();
                String password = new String(passwordField.getPassword());

                try (Connection conn = DBConnection.getConnection()) {
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

                    JOptionPane.showMessageDialog(Login.this, "Email ou mot de passe incorrect.", "Erreur", JOptionPane.ERROR_MESSAGE);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(Login.this, "Erreur de connexion a la base de donnees.", "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Create account logic
        createAccountButton.addActionListener(e -> {
            dispose();
            new CreateAccountClient();
        });

        setVisible(true);
    }
}
