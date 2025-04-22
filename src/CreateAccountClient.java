import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class CreateAccountClient extends JFrame {
    private JTextField nomField, prenomField, emailField, adresseField, telephoneField;
    private JPasswordField passwordField, confirmPasswordField;
    private JButton createAccountButton, backButton;
    private Color primaryColor = new Color(41, 128, 185); // Blue
    private Color lightColor = new Color(236, 240, 241); // Light gray
    private Color textColor = new Color(44, 62, 80); // Dark blue/gray
    private Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private Font headerFont = new Font("Segoe UI", Font.BOLD, 24);

    public CreateAccountClient() {
        setTitle("Créer un compte Client");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(primaryColor);
        headerPanel.setPreferredSize(new Dimension(800, 70));
        headerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        
        JLabel titleLabel = new JLabel("Créer un compte Client");
        titleLabel.setFont(headerFont);
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        add(headerPanel, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel();
        formPanel.setBackground(Color.WHITE);
        formPanel.setLayout(null);
        
        // Initialisation des champs
        nomField = createStyledTextField(formPanel, "Nom:", 50, 30);
        prenomField = createStyledTextField(formPanel, "Prénom:", 50, 100);
        emailField = createStyledTextField(formPanel, "Email:", 50, 170);
        adresseField = createStyledTextField(formPanel, "Adresse:", 50, 240);
        telephoneField = createStyledTextField(formPanel, "Téléphone:", 50, 310);
        
        JLabel passwordLabel = new JLabel("Mot de passe:");
        passwordLabel.setFont(mainFont);
        passwordLabel.setForeground(textColor);
        passwordLabel.setBounds(50, 380, 150, 25);
        formPanel.add(passwordLabel);
        
        passwordField = new JPasswordField();
        passwordField.setFont(mainFont);
        passwordField.setBounds(50, 410, 300, 35);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        formPanel.add(passwordField);
        
        JLabel confirmPasswordLabel = new JLabel("Confirmer mot de passe:");
        confirmPasswordLabel.setFont(mainFont);
        confirmPasswordLabel.setForeground(textColor);
        confirmPasswordLabel.setBounds(400, 380, 200, 25);
        formPanel.add(confirmPasswordLabel);
        
        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setFont(mainFont);
        confirmPasswordField.setBounds(400, 410, 300, 35);
        confirmPasswordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        formPanel.add(confirmPasswordField);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBounds(50, 470, 700, 40);
        buttonPanel.setLayout(new GridLayout(1, 2, 50, 0));
        
        createAccountButton = new JButton("Créer le compte");
        createAccountButton.setFont(mainFont);
        createAccountButton.setBackground(primaryColor);
        createAccountButton.setForeground(Color.WHITE);
        createAccountButton.setFocusPainted(false);
        createAccountButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        createAccountButton.addActionListener(e -> createAccount());
        
        backButton = new JButton("Retour");
        backButton.setFont(mainFont);
        backButton.setBackground(lightColor);
        backButton.setForeground(textColor);
        backButton.setFocusPainted(false);
        backButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        backButton.addActionListener(e -> {
            dispose();
            new Login();
        });
        
        buttonPanel.add(createAccountButton);
        buttonPanel.add(backButton);
        formPanel.add(buttonPanel);
        
        add(formPanel, BorderLayout.CENTER);

        // Footer panel
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(lightColor);
        footerPanel.setPreferredSize(new Dimension(800, 40));
        
        JLabel footerLabel = new JLabel("© 2023 Gestion de Location. Tous droits réservés.");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerLabel.setForeground(textColor);
        footerPanel.add(footerLabel);
        
        add(footerPanel, BorderLayout.SOUTH);

        setVisible(true);
    }
    
    private void createAccount() {
        String nom = nomField.getText();
        String prenom = prenomField.getText();
        String email = emailField.getText();
        String adresse = adresseField.getText();
        String telephone = telephoneField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // Vérifier si les mots de passe correspondent
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Les mots de passe ne correspondent pas.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Vérifier si tous les champs sont remplis
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || adresse.isEmpty() || telephone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tous les champs doivent être remplis.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            // Vérifier si l'email existe déjà
            String sqlCheckEmail = "SELECT * FROM clients WHERE email = ?";
            PreparedStatement stmtCheckEmail = conn.prepareStatement(sqlCheckEmail);
            stmtCheckEmail.setString(1, email);
            ResultSet rsCheckEmail = stmtCheckEmail.executeQuery();

            if (rsCheckEmail.next()) {
                JOptionPane.showMessageDialog(this, "Cet email est déjà utilisé.", "Erreur", JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(this, "Compte créé avec succès.", "Succès", JOptionPane.INFORMATION_MESSAGE);
                dispose(); // Fermer la fenêtre de création de compte
                new Login(); // Retourner à la page de login
            } else {
                JOptionPane.showMessageDialog(this, "Erreur lors de la création du compte.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur de connexion à la base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private JTextField createStyledTextField(JPanel panel, String labelText, int x, int y) {
        JLabel label = new JLabel(labelText);
        label.setFont(mainFont);
        label.setForeground(textColor);
        label.setBounds(x, y, 150, 25);
        panel.add(label);
        
        JTextField textField = new JTextField();
        textField.setFont(mainFont);
        textField.setBounds(x, y + 30, 300, 35);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        panel.add(textField);
        
        return textField;
    }
}
