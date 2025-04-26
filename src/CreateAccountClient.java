import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class CreateAccountClient extends JFrame {
    private JTextField nomField, prenomField, emailField, adresseField, telephoneField;
    private JPasswordField passwordField, confirmPasswordField;
    private JButton createAccountButton, backButton;
    private Color primaryColor = new Color(52, 152, 219); // Modern Blue
    private Color backgroundColor = new Color(245, 245, 245); // Light background
    private Color textColor = new Color(33, 33, 33); // Neutral dark text
    private Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private Font headerFont = new Font("Segoe UI", Font.BOLD, 22);

    public CreateAccountClient() {
        setTitle("Creer un compte Client");
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(backgroundColor);
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(primaryColor);
        headerPanel.setPreferredSize(new Dimension(700, 60));
        JLabel titleLabel = new JLabel("Creer un compte Client");
        titleLabel.setFont(headerFont);
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(backgroundColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        nomField = addLabeledField(formPanel, "Nom", 0);
        prenomField = addLabeledField(formPanel, "Prenom", 1);
        emailField = addLabeledField(formPanel, "Email", 2);
        adresseField = addLabeledField(formPanel, "Adresse", 3);
        telephoneField = addLabeledField(formPanel, "Telephone", 4);

        JLabel passwordLabel = new JLabel("Mot de passe");
        passwordLabel.setFont(mainFont);
        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField();
        passwordField.setFont(mainFont);
        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);

        JLabel confirmPasswordLabel = new JLabel("Confirmer mot de passe");
        confirmPasswordLabel.setFont(mainFont);
        gbc.gridx = 0;
        gbc.gridy = 6;
        formPanel.add(confirmPasswordLabel, gbc);

        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setFont(mainFont);
        gbc.gridx = 1;
        formPanel.add(confirmPasswordField, gbc);

        createAccountButton = new JButton("Creer le compte");
        createAccountButton.setBackground(primaryColor);
        createAccountButton.setForeground(Color.WHITE);
        createAccountButton.setFocusPainted(false);
        createAccountButton.setFont(mainFont);
        createAccountButton.addActionListener(e -> createAccount());

        backButton = new JButton("Retour");
        backButton.setBackground(Color.WHITE);
        backButton.setForeground(primaryColor);
        backButton.setFocusPainted(false);
        backButton.setFont(mainFont);
        backButton.addActionListener(e -> {
            dispose();
            new Login();
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(backgroundColor);
        buttonPanel.add(createAccountButton);
        buttonPanel.add(backButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JTextField addLabeledField(JPanel panel, String label, int y) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.anchor = GridBagConstraints.LINE_END;

        JLabel jLabel = new JLabel(label);
        jLabel.setFont(mainFont);
        panel.add(jLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        JTextField field = new JTextField(20);
        field.setFont(mainFont);
        panel.add(field, gbc);

        return field;
    }

    private void createAccount() {
        String nom = nomField.getText();
        String prenom = prenomField.getText();
        String email = emailField.getText();
        String adresse = adresseField.getText();
        String telephone = telephoneField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Les mots de passe ne correspondent pas.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || adresse.isEmpty() || telephone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tous les champs doivent etre remplis.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sqlCheckEmail = "SELECT * FROM clients WHERE email = ?";
            PreparedStatement stmtCheckEmail = conn.prepareStatement(sqlCheckEmail);
            stmtCheckEmail.setString(1, email);
            ResultSet rsCheckEmail = stmtCheckEmail.executeQuery();

            if (rsCheckEmail.next()) {
                JOptionPane.showMessageDialog(this, "Cet email est deja utilise.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

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
                JOptionPane.showMessageDialog(this, "Compte cree avec succes.", "Succes", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                new Login();
            } else {
                JOptionPane.showMessageDialog(this, "Erreur lors de la creation du compte.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur de connexion a la base de donnees.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}