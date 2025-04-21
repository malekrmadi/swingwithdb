import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class CreateAccountClient extends JFrame {
    private JTextField nomField, prenomField, emailField, adresseField, telephoneField;
    private JPasswordField passwordField, confirmPasswordField;
    private JButton createAccountButton;

    public CreateAccountClient() {
        setTitle("Créer un compte Client");
        setSize(400, 300); // Augmenter la taille pour ajouter les nouveaux champs
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialisation des champs
        nomField = new JTextField(20);
        prenomField = new JTextField(20);
        emailField = new JTextField(20);
        adresseField = new JTextField(20);
        telephoneField = new JTextField(20);
        passwordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);
        createAccountButton = new JButton("Créer le compte");

        // Création du panneau avec une grille
        JPanel panel = new JPanel(new GridLayout(7, 2)); // Modifier la grille pour ajouter 7 lignes
        panel.add(new JLabel("Nom :"));
        panel.add(nomField);
        panel.add(new JLabel("Prénom :"));
        panel.add(prenomField);
        panel.add(new JLabel("Email :"));
        panel.add(emailField);
        panel.add(new JLabel("Adresse :"));
        panel.add(adresseField);  // Ajouter le champ Adresse
        panel.add(new JLabel("Téléphone :"));
        panel.add(telephoneField);  // Ajouter le champ Téléphone
        panel.add(new JLabel("Mot de passe :"));
        panel.add(passwordField);
        panel.add(new JLabel("Confirmer mot de passe :"));
        panel.add(confirmPasswordField);
        panel.add(new JLabel(""));
        panel.add(createAccountButton);

        // Ajouter le panneau à la fenêtre
        add(panel);

        createAccountButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String nom = nomField.getText();
                String prenom = prenomField.getText();
                String email = emailField.getText();
                String adresse = adresseField.getText();
                String telephone = telephoneField.getText();
                String password = new String(passwordField.getPassword());
                String confirmPassword = new String(confirmPasswordField.getPassword());

                // Vérifier si les mots de passe correspondent
                if (!password.equals(confirmPassword)) {
                    JOptionPane.showMessageDialog(CreateAccountClient.this, "Les mots de passe ne correspondent pas.", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Vérifier si tous les champs sont remplis
                if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || adresse.isEmpty() || telephone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    JOptionPane.showMessageDialog(CreateAccountClient.this, "Tous les champs doivent être remplis.", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try (Connection conn = DBConnection.getConnection()) {
                    // Vérifier si l'email existe déjà
                    String sqlCheckEmail = "SELECT * FROM clients WHERE email = ?";
                    PreparedStatement stmtCheckEmail = conn.prepareStatement(sqlCheckEmail);
                    stmtCheckEmail.setString(1, email);
                    ResultSet rsCheckEmail = stmtCheckEmail.executeQuery();

                    if (rsCheckEmail.next()) {
                        JOptionPane.showMessageDialog(CreateAccountClient.this, "Cet email est déjà utilisé.", "Erreur", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Insérer le nouveau client dans la base de données
                    String sqlInsert = "INSERT INTO clients (nom, prenom, email, adresse, telephone, mot_de_passe) VALUES (?, ?, ?, ?, ?, ?)";
                    PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert);
                    stmtInsert.setString(1, nom);
                    stmtInsert.setString(2, prenom);
                    stmtInsert.setString(3, email);
                    stmtInsert.setString(4, adresse);
                    stmtInsert.setString(5, telephone);
                    stmtInsert.setString(6, password);
                    int rowsAffected = stmtInsert.executeUpdate();

                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(CreateAccountClient.this, "Compte créé avec succès.", "Succès", JOptionPane.INFORMATION_MESSAGE);
                        dispose(); // Fermer la fenêtre de création de compte
                        new Login(); // Retourner à la page de login
                    } else {
                        JOptionPane.showMessageDialog(CreateAccountClient.this, "Erreur lors de la création du compte.", "Erreur", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(CreateAccountClient.this, "Erreur de connexion à la base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        setVisible(true);
    }
}
