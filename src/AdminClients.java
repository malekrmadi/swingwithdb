import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.sql.*;

public class AdminClients extends JFrame {
    private JTable table;
    private DefaultTableModel model;

    public AdminClients(boolean isSuperAdmin) {
        setTitle("Liste des Clients");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Champ de recherche
        JPanel searchPanel = new JPanel();
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Rechercher");
        searchPanel.add(new JLabel("Rechercher: "));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        add(searchPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Nom", "Prenom", "Email", "Téléphone", "Adresse", "Statut", "Fidélité", "Points", "Actions"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        table.setRowHeight(40);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Boutons bas
        JButton addButton = new JButton("Ajouter Client");
        addButton.addActionListener(e -> showClientForm(null));

        JButton backButton = new JButton("Retour au Dashboard");
        backButton.addActionListener(e -> {
            dispose();
            if (isSuperAdmin) new DashboardSuperAdmin();
            else new DashboardAdmin();
        });

        JPanel southPanel = new JPanel();
        southPanel.add(addButton);
        southPanel.add(backButton);
        add(southPanel, BorderLayout.SOUTH);

        searchButton.addActionListener(e -> loadClients(searchField.getText()));
        loadClients("");

        setVisible(true);
    }

    private void loadClients(String filtre) {
        model.setRowCount(0);

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM clients WHERE nom LIKE ? OR prenom LIKE ? OR email LIKE ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "%" + filtre + "%");
                stmt.setString(2, "%" + filtre + "%");
                stmt.setString(3, "%" + filtre + "%");
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    Object[] row = new Object[10];
                    for (int i = 0; i < 9; i++) row[i] = rs.getObject(i + 1);

                    int clientId = rs.getInt("client_id");
                    JButton updateBtn = new JButton("Modifier");
                    JButton deleteBtn = new JButton("Supprimer");

                    updateBtn.addActionListener(e -> showClientForm(clientId));
                    deleteBtn.addActionListener(e -> {
                        int confirm = JOptionPane.showConfirmDialog(this, "Supprimer ce client ?");
                        if (confirm == JOptionPane.YES_OPTION) deleteClient(clientId);
                    });

                    JPanel panel = new JPanel();
                    panel.add(updateBtn);
                    panel.add(deleteBtn);
                    row[9] = panel;

                    model.addRow(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des clients.");
        }

        TableColumn actionCol = table.getColumn("Actions");
        actionCol.setCellRenderer((t, val, s, f, r, c) -> (Component) val);
    }

    private void showClientForm(Integer clientId) {
        JDialog dialog = new JDialog(this, "Client", true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(0, 2));
        JTextField nom = new JTextField();
        JTextField prenom = new JTextField();
        JTextField email = new JTextField();
        JTextField motDePasse = new JTextField();
        JTextField telephone = new JTextField();
        JTextField adresse = new JTextField();
        JComboBox<String> statut = new JComboBox<>(new String[]{"actif", "inactif", "banni"});
        JCheckBox fidele = new JCheckBox("Client fidèle");
        JTextField points = new JTextField();

        panel.add(new JLabel("Nom")); panel.add(nom);
        panel.add(new JLabel("Prénom")); panel.add(prenom);
        panel.add(new JLabel("Email")); panel.add(email);
        panel.add(new JLabel("Mot de passe")); panel.add(motDePasse);
        panel.add(new JLabel("Téléphone")); panel.add(telephone);
        panel.add(new JLabel("Adresse")); panel.add(adresse);
        panel.add(new JLabel("Statut")); panel.add(statut);
        panel.add(new JLabel("Points fidélité")); panel.add(points);
        panel.add(new JLabel("")); panel.add(fidele);

        if (clientId != null) {
            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement("SELECT * FROM clients WHERE client_id = ?");
                stmt.setInt(1, clientId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    nom.setText(rs.getString("nom"));
                    prenom.setText(rs.getString("prenom"));
                    email.setText(rs.getString("email"));
                    motDePasse.setText(rs.getString("mot_de_passe"));
                    telephone.setText(rs.getString("telephone"));
                    adresse.setText(rs.getString("adresse"));
                    statut.setSelectedItem(rs.getString("statut"));
                    points.setText(String.valueOf(rs.getInt("points_fidelite")));
                    fidele.setSelected(rs.getBoolean("client_fidele"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        JButton saveBtn = new JButton("Enregistrer");
        saveBtn.addActionListener(e -> {
            try (Connection conn = DBConnection.getConnection()) {
                String sql;
                if (clientId == null) {
                    sql = "INSERT INTO clients(nom, prenom, email, mot_de_passe, telephone, adresse, statut, points_fidelite, client_fidele) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                } else {
                    sql = "UPDATE clients SET nom=?, prenom=?, email=?, mot_de_passe=?, telephone=?, adresse=?, statut=?, points_fidelite=?, client_fidele=? WHERE client_id=?";
                }

                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, nom.getText());
                stmt.setString(2, prenom.getText());
                stmt.setString(3, email.getText());
                stmt.setString(4, motDePasse.getText());
                stmt.setString(5, telephone.getText());
                stmt.setString(6, adresse.getText());
                stmt.setString(7, (String) statut.getSelectedItem());
                stmt.setInt(8, Integer.parseInt(points.getText()));
                stmt.setBoolean(9, fidele.isSelected());
                if (clientId != null) stmt.setInt(10, clientId);

                stmt.executeUpdate();
                dialog.dispose();
                loadClients("");
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Erreur lors de l'enregistrement");
            }
        });

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(saveBtn, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteClient(int clientId) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM clients WHERE client_id = ?");
            stmt.setInt(1, clientId);
            stmt.executeUpdate();
            loadClients("");
            JOptionPane.showMessageDialog(this, "Client supprimé.");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de la suppression");
        }
    }
}
