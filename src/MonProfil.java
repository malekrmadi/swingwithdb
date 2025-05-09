import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class MonProfil extends JFrame {
    private int clientId;
    private JTextField nomField, prenomField, emailField, adresseField, telephoneField;
    private JPasswordField motDePasseField, newPasswordField, confirmPasswordField;
    private JCheckBox changePasswordCheckbox;
    private Color primaryColor = new Color(41, 128, 185); // Blue
    private Color accentColor = new Color(155, 89, 182); // Purple for profile
    private Color lightColor = new Color(236, 240, 241); // Light gray
    private Color textColor = new Color(44, 62, 80); // Dark blue/gray
    private Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private Font headerFont = new Font("Segoe UI", Font.BOLD, 24);

    public MonProfil(int clientId) {
        this.clientId = clientId;

        setTitle("Mon Profil");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(accentColor);
        headerPanel.setPreferredSize(new Dimension(800, 70));
        headerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 15));
        
        JLabel titleLabel = new JLabel("Mon Profil");
        titleLabel.setFont(headerFont);
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        add(headerPanel, BorderLayout.NORTH);

        // Main content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        
        // Personal info section
        JLabel personalInfoLabel = new JLabel("Informations personnelles");
        personalInfoLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        personalInfoLabel.setForeground(textColor);
        personalInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(personalInfoLabel);
        
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(3, 4, 15, 15));
        formPanel.setBackground(Color.WHITE);
        formPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.setMaximumSize(new Dimension(720, 150));
        
        nomField = createTextField();
        prenomField = createTextField();
        emailField = createTextField();
        emailField.setEditable(false);
        emailField.setBackground(lightColor);
        adresseField = createTextField();
        telephoneField = createTextField();
        
        formPanel.add(createLabelPanel("Nom:"));
        formPanel.add(nomField);
        formPanel.add(createLabelPanel("Prenom:"));
        formPanel.add(prenomField);
        formPanel.add(createLabelPanel("Email:"));
        formPanel.add(emailField);
        formPanel.add(createLabelPanel("Telephone:"));
        formPanel.add(telephoneField);
        formPanel.add(createLabelPanel("Adresse:"));
        formPanel.add(adresseField);
        
        contentPanel.add(formPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Password section
        JLabel passwordLabel = new JLabel("Informations de connexion");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        passwordLabel.setForeground(textColor);
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(passwordLabel);
        
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Current password
        JPanel currentPasswordPanel = new JPanel(new BorderLayout(10, 0));
        currentPasswordPanel.setBackground(Color.WHITE);
        currentPasswordPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        currentPasswordPanel.setMaximumSize(new Dimension(720, 35));
        
        JLabel currentPassLabel = new JLabel("Mot de passe actuel:");
        currentPassLabel.setFont(mainFont);
        currentPassLabel.setForeground(textColor);
        currentPasswordPanel.add(currentPassLabel, BorderLayout.WEST);
        
        motDePasseField = new JPasswordField();
        motDePasseField.setEditable(false);
        motDePasseField.setBackground(lightColor);
        motDePasseField.setFont(mainFont);
        motDePasseField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        currentPasswordPanel.add(motDePasseField, BorderLayout.CENTER);
        
        contentPanel.add(currentPasswordPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Change password option
        JPanel changePassPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        changePassPanel.setBackground(Color.WHITE);
        changePassPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        changePasswordCheckbox = new JCheckBox("Changer mon mot de passe");
        changePasswordCheckbox.setFont(mainFont);
        changePasswordCheckbox.setForeground(textColor);
        changePasswordCheckbox.setBackground(Color.WHITE);
        changePassPanel.add(changePasswordCheckbox);
        
        contentPanel.add(changePassPanel);
        
        // New password fields (initially hidden)
        JPanel newPasswordsPanel = new JPanel(new GridLayout(2, 2, 15, 10));
        newPasswordsPanel.setBackground(Color.WHITE);
        newPasswordsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        newPasswordsPanel.setMaximumSize(new Dimension(720, 90));
        newPasswordsPanel.setVisible(false);
        
        newPasswordField = new JPasswordField();
        newPasswordField.setFont(mainFont);
        newPasswordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setFont(mainFont);
        confirmPasswordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        newPasswordsPanel.add(createLabelPanel("Nouveau mot de passe:"));
        newPasswordsPanel.add(newPasswordField);
        newPasswordsPanel.add(createLabelPanel("Confirmer mot de passe:"));
        newPasswordsPanel.add(confirmPasswordField);
        
        contentPanel.add(newPasswordsPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Toggle password change fields
        changePasswordCheckbox.addActionListener(e -> {
            newPasswordsPanel.setVisible(changePasswordCheckbox.isSelected());
            revalidate();
            repaint();
        });
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JButton saveButton = new JButton("Enregistrer les modifications");
        saveButton.setFont(mainFont);
        saveButton.setBackground(accentColor);
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        JButton backButton = new JButton("Retour au Dashboard");
        backButton.setFont(mainFont);
        backButton.setBackground(lightColor);
        backButton.setForeground(textColor);
        backButton.setFocusPainted(false);
        backButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        buttonPanel.add(backButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(saveButton);
        
        contentPanel.add(buttonPanel);
        add(contentPanel, BorderLayout.CENTER);
        


        loadClientData();

        saveButton.addActionListener(e -> updateClientData());
        backButton.addActionListener(e -> {
            dispose();
            new DashboardClient(clientId);
        });

        setVisible(true);
    }
    
    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(mainFont);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }
    
    private JPanel createLabelPanel(String text) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Color.WHITE);
        
        JLabel label = new JLabel(text);
        label.setFont(mainFont);
        label.setForeground(textColor);
        panel.add(label);
        
        return panel;
    }

    private void loadClientData() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT nom, prenom, email, adresse, telephone, mot_de_passe FROM clients WHERE client_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                nomField.setText(rs.getString("nom"));
                prenomField.setText(rs.getString("prenom"));
                emailField.setText(rs.getString("email"));
                adresseField.setText(rs.getString("adresse"));
                telephoneField.setText(rs.getString("telephone"));
                motDePasseField.setText(rs.getString("mot_de_passe"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des donnees.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateClientData() {
        String nom = nomField.getText();
        String prenom = prenomField.getText();
        String adresse = adresseField.getText();
        String telephone = telephoneField.getText();
        
        if (nom.isEmpty() || prenom.isEmpty() || adresse.isEmpty() || telephone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tous les champs sont obligatoires.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (changePasswordCheckbox.isSelected()) {
                String newPassword = new String(newPasswordField.getPassword());
                String confirmPassword = new String(confirmPasswordField.getPassword());
                
                if (newPassword.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Le nouveau mot de passe ne peut pas être vide.", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (!newPassword.equals(confirmPassword)) {
                    JOptionPane.showMessageDialog(this, "Les mots de passe ne correspondent pas.", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                String sql = "UPDATE clients SET nom = ?, prenom = ?, adresse = ?, telephone = ?, mot_de_passe = ? WHERE client_id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, nom);
                stmt.setString(2, prenom);
                stmt.setString(3, adresse);
                stmt.setString(4, telephone);
                stmt.setString(5, newPassword);
                stmt.setInt(6, clientId);
                
                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated > 0) {
                    JOptionPane.showMessageDialog(this, "Informations et mot de passe mis à jour avec succès.");
                    motDePasseField.setText(newPassword);
                    changePasswordCheckbox.setSelected(false);
                    newPasswordField.setText("");
                    confirmPasswordField.setText("");
                }
            } else {
                String sql = "UPDATE clients SET nom = ?, prenom = ?, adresse = ?, telephone = ? WHERE client_id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, nom);
                stmt.setString(2, prenom);
                stmt.setString(3, adresse);
                stmt.setString(4, telephone);
                stmt.setInt(5, clientId);

                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated > 0) {
                    JOptionPane.showMessageDialog(this, "Informations mises à jour avec succès.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de la mise à jour.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}
