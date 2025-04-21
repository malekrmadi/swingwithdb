import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Login extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton, createAccountButton;

    public Login() {
        setTitle("Login");
        setSize(350, 250);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        emailField = new JTextField(20);
        passwordField = new JPasswordField(20);
        loginButton = new JButton("Connexion");
        createAccountButton = new JButton("Créer un compte Client");

        JPanel panel = new JPanel(new GridLayout(4, 2));
        panel.add(new JLabel("Email :"));
        panel.add(emailField);
        panel.add(new JLabel("Mot de passe :"));
        panel.add(passwordField);
        panel.add(new JLabel(""));
        panel.add(loginButton);
        panel.add(createAccountButton);

        add(panel);

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String email = emailField.getText();
                String password = new String(passwordField.getPassword());

                try (Connection conn = DBConnection.getConnection()) {

                    // Vérifier d'abord si l'utilisateur est un admin
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

                    // Sinon, vérifier si c'est un client
                    String sqlClient = "SELECT * FROM clients WHERE email = ? AND mot_de_passe = ?";
                    PreparedStatement stmtClient = conn.prepareStatement(sqlClient);
                    stmtClient.setString(1, email);
                    stmtClient.setString(2, password);
                    ResultSet rsClient = stmtClient.executeQuery();

                    if (rsClient.next()) {
                        dispose();
                        new DashboardClient();
                        return;
                    }

                    // Si aucun des deux ne correspond
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
