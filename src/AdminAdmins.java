import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.sql.*;

public class AdminAdmins extends JFrame {
    private JTable table;
    private DefaultTableModel model;

    public AdminAdmins() {
        setTitle("Gestion des Admins");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new String[]{"ID", "Nom", "Prénom", "Email", "Super Admin", "Actions"}, 0);
        table = new JTable(model);
        table.setRowHeight(40);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton addButton = new JButton("Ajouter Admin");
        addButton.addActionListener(e -> openAdminForm(null));

        JButton backButton = new JButton("Retour au Dashboard");
        backButton.addActionListener(e -> {
            dispose();
            new DashboardSuperAdmin();
        });

        JPanel southPanel = new JPanel();
        southPanel.add(addButton);
        southPanel.add(backButton);
        add(southPanel, BorderLayout.SOUTH);

        loadAdmins();

        setVisible(true);
    }

    private void loadAdmins() {
        model.setRowCount(0);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM admin");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("admin_id");
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                String email = rs.getString("email");
                boolean superAdmin = rs.getBoolean("superadmin");

                JButton updateBtn = new JButton("Modifier");
                JButton deleteBtn = new JButton("Supprimer");

                updateBtn.addActionListener(e -> {
                    Admin admin = new Admin(id, nom, prenom, email, "", superAdmin);
                    openAdminForm(admin);
                });

                deleteBtn.addActionListener(e -> {
                    int confirm = JOptionPane.showConfirmDialog(this, "Confirmer la suppression ?", "Suppression", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        deleteAdmin(id);
                    }
                });

                JPanel actionPanel = new JPanel();
                actionPanel.add(updateBtn);
                actionPanel.add(deleteBtn);

                model.addRow(new Object[]{id, nom, prenom, email, superAdmin ? "Oui" : "Non", actionPanel});
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des administrateurs.");
        }

        TableColumn actionCol = table.getColumn("Actions");
        actionCol.setCellRenderer((table, value, isSelected, hasFocus, row, column) -> (Component) value);
    }

    private void openAdminForm(Admin admin) {
        JDialog dialog = new JDialog(this, admin == null ? "Ajouter Admin" : "Modifier Admin", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(6, 2, 5, 5));

        JTextField nomField = new JTextField(admin != null ? admin.nom : "");
        JTextField prenomField = new JTextField(admin != null ? admin.prenom : "");
        JTextField emailField = new JTextField(admin != null ? admin.email : "");
        JPasswordField passField = new JPasswordField();
        JCheckBox superAdminCheck = new JCheckBox("Super Admin", admin != null && admin.superadmin);

        dialog.add(new JLabel("Nom:"));
        dialog.add(nomField);
        dialog.add(new JLabel("Prénom:"));
        dialog.add(prenomField);
        dialog.add(new JLabel("Email:"));
        dialog.add(emailField);
        dialog.add(new JLabel(admin == null ? "Mot de passe:" : "Nouveau mot de passe:"));
        dialog.add(passField);
        dialog.add(new JLabel("Rôle:"));
        dialog.add(superAdminCheck);

        JButton saveBtn = new JButton("Enregistrer");
        saveBtn.addActionListener(e -> {
            String nom = nomField.getText().trim();
            String prenom = prenomField.getText().trim();
            String email = emailField.getText().trim();
            String mdp = new String(passField.getPassword()).trim();
            boolean superadmin = superAdminCheck.isSelected();

            if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Veuillez remplir tous les champs requis.");
                return;
            }

            if (admin == null) {
                insertAdmin(nom, prenom, email, mdp, superadmin);
            } else {
                updateAdmin(admin.id, nom, prenom, email, mdp, superadmin);
            }

            dialog.dispose();
        });

        dialog.add(saveBtn);
        dialog.setVisible(true);
    }

    private void insertAdmin(String nom, String prenom, String email, String mdp, boolean superadmin) {
        String sql = "INSERT INTO admin (nom, prenom, email, mot_de_passe, superadmin) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nom);
            stmt.setString(2, prenom);
            stmt.setString(3, email);
            stmt.setString(4, mdp); // En production, hasher le mot de passe
            stmt.setBoolean(5, superadmin);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Admin ajouté avec succès.");
            loadAdmins();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de l'ajout.");
        }
    }

    private void updateAdmin(int id, String nom, String prenom, String email, String mdp, boolean superadmin) {
        try (Connection conn = DBConnection.getConnection()) {
            String query = mdp.isEmpty()
                    ? "UPDATE admin SET nom = ?, prenom = ?, email = ?, superadmin = ? WHERE admin_id = ?"
                    : "UPDATE admin SET nom = ?, prenom = ?, email = ?, mot_de_passe = ?, superadmin = ? WHERE admin_id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, nom);
                stmt.setString(2, prenom);
                stmt.setString(3, email);
                if (!mdp.isEmpty()) {
                    stmt.setString(4, mdp);
                    stmt.setBoolean(5, superadmin);
                    stmt.setInt(6, id);
                } else {
                    stmt.setBoolean(4, superadmin);
                    stmt.setInt(5, id);
                }
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Admin mis à jour.");
                loadAdmins();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de la mise à jour.");
        }
    }

    private void deleteAdmin(int id) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM admin WHERE admin_id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Admin supprimé.");
            loadAdmins();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de la suppression.");
        }
    }

    // Petite classe utilitaire pour manipuler les admins
    private static class Admin {
        int id;
        String nom, prenom, email, motDePasse;
        boolean superadmin;

        public Admin(int id, String nom, String prenom, String email, String motDePasse, boolean superadmin) {
            this.id = id;
            this.nom = nom;
            this.prenom = prenom;
            this.email = email;
            this.motDePasse = motDePasse;
            this.superadmin = superadmin;
        }
    }
}
