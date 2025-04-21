import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class MonProfil extends JFrame {
    private int clientId;
    private JTextField nomField, prenomField, emailField, adresseField, telephoneField;
    private JPasswordField motDePasseField;

    public MonProfil(int clientId) {
        this.clientId = clientId;

        setTitle("Mon Profil");
        setSize(400, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(7, 2, 10, 10));

        nomField = new JTextField();
        prenomField = new JTextField();
        emailField = new JTextField();
        emailField.setEditable(false);
        adresseField = new JTextField();
        telephoneField = new JTextField();
        motDePasseField = new JPasswordField();
        motDePasseField.setEditable(false);

        formPanel.add(new JLabel("Nom :"));
        formPanel.add(nomField);
        formPanel.add(new JLabel("Prénom :"));
        formPanel.add(prenomField);
        formPanel.add(new JLabel("Email :"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Adresse :"));
        formPanel.add(adresseField);
        formPanel.add(new JLabel("Téléphone :"));
        formPanel.add(telephoneField);
        formPanel.add(new JLabel("Mot de passe :"));
        formPanel.add(motDePasseField);

        add(formPanel, BorderLayout.CENTER);

        JButton saveButton = new JButton("Enregistrer les modifications");
        JButton backButton = new JButton("Retour au Dashboard");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        loadClientData();

        saveButton.addActionListener(e -> updateClientData());
        backButton.addActionListener(e -> {
            dispose();
            new DashboardClient(clientId);
        });

        setVisible(true);
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
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des données.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateClientData() {
        String nom = nomField.getText();
        String prenom = prenomField.getText();
        String adresse = adresseField.getText();
        String telephone = telephoneField.getText();

        try (Connection conn = DBConnection.getConnection()) {
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
            } else {
                JOptionPane.showMessageDialog(this, "Aucune modification enregistrée.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de la mise à jour.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}
