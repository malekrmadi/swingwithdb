import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.sql.*;

public class AdminAppartements extends JFrame {
    private JTable table;
    private DefaultTableModel model;

    public AdminAppartements(boolean isSuperAdmin) {
        setTitle("Gestion des Appartements");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel searchPanel = new JPanel();
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Rechercher");
        searchPanel.add(new JLabel("Rechercher:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        add(searchPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "Nom", "Adresse", "Ville", "Type", "Capacité", "Prix", "Disponibilité", "Statut", "Date Ajout", "Note", "Actions"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(40);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JButton addButton = new JButton("Ajouter Appartement");
        addButton.addActionListener(e -> openAppartementForm(null));

        JButton backButton = new JButton("Retour au Dashboard");
        backButton.addActionListener(e -> {
            dispose();
            if (isSuperAdmin) {
                new DashboardSuperAdmin();
            } else {
                new DashboardAdmin();
            }
        });

        JPanel southPanel = new JPanel();
        southPanel.add(addButton);
        southPanel.add(backButton);
        add(southPanel, BorderLayout.SOUTH);

        searchButton.addActionListener(e -> loadAppartements(searchField.getText()));
        loadAppartements("");

        setVisible(true);
    }

    private void loadAppartements(String filtre) {
        model.setRowCount(0);

        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT * FROM appartements WHERE nom LIKE ? OR ville LIKE ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, "%" + filtre + "%");
                stmt.setString(2, "%" + filtre + "%");
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    Object[] row = new Object[12];
                    for (int i = 0; i < 11; i++) {
                        row[i] = rs.getObject(i + 1);
                    }

                    int id = rs.getInt("appartement_id");
                    JPanel actionPanel = new JPanel(new FlowLayout());

                    JButton btnUpdate = new JButton("Modifier");
                    btnUpdate.addActionListener(e -> openAppartementForm(id));

                    JButton btnDelete = new JButton("Supprimer");
                    btnDelete.addActionListener(e -> {
                        int confirm = JOptionPane.showConfirmDialog(this, "Supprimer cet appartement ?");
                        if (confirm == JOptionPane.YES_OPTION) {
                            deleteAppartement(id);
                        }
                    });

                    actionPanel.add(btnUpdate);
                    actionPanel.add(btnDelete);
                    row[11] = actionPanel;
                    model.addRow(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur de chargement des appartements.");
        }

        TableColumn actionCol = table.getColumn("Actions");
        actionCol.setCellRenderer((table, value, isSelected, hasFocus, row, column) -> (Component) value);
    }

    private void deleteAppartement(int id) {
        try (Connection conn = DBConnection.getConnection()) {
            String deleteSQL = "DELETE FROM appartements WHERE appartement_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteSQL)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Appartement supprimé avec succès.");
                loadAppartements("");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de la suppression.");
        }
    }

    private void openAppartementForm(Integer appartementId) {
        JFrame form = new JFrame(appartementId == null ? "Ajouter Appartement" : "Modifier Appartement");
        form.setSize(400, 500);
        form.setLayout(new GridLayout(0, 2));
        form.setLocationRelativeTo(null);

        JTextField nom = new JTextField();
        JTextField adresse = new JTextField();
        JTextField ville = new JTextField();
        JTextField type = new JTextField();
        JTextField capacite = new JTextField();
        JTextField prix = new JTextField();
        JCheckBox dispo = new JCheckBox("Disponible");
        JComboBox<String> statut = new JComboBox<>(new String[]{"disponible", "en_renovation", "en_maintenance"});

        form.add(new JLabel("Nom:")); form.add(nom);
        form.add(new JLabel("Adresse:")); form.add(adresse);
        form.add(new JLabel("Ville:")); form.add(ville);
        form.add(new JLabel("Type:")); form.add(type);
        form.add(new JLabel("Capacité:")); form.add(capacite);
        form.add(new JLabel("Prix par nuit:")); form.add(prix);
        form.add(new JLabel("Disponibilité:")); form.add(dispo);
        form.add(new JLabel("Statut:")); form.add(statut);

        JButton saveButton = new JButton("Enregistrer");
        form.add(saveButton);

        if (appartementId != null) {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "SELECT * FROM appartements WHERE appartement_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, appartementId);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        nom.setText(rs.getString("nom"));
                        adresse.setText(rs.getString("adresse"));
                        ville.setText(rs.getString("ville"));
                        type.setText(rs.getString("type_appartement"));
                        capacite.setText(String.valueOf(rs.getInt("capacite")));
                        prix.setText(String.valueOf(rs.getDouble("prix_par_nuit")));
                        dispo.setSelected(rs.getBoolean("disponibilite"));
                        statut.setSelectedItem(rs.getString("statut"));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        saveButton.addActionListener(e -> {
            try (Connection conn = DBConnection.getConnection()) {
                String sql;
                if (appartementId == null) {
                    sql = "INSERT INTO appartements (nom, adresse, ville, type_appartement, capacite, prix_par_nuit, disponibilite, statut) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                } else {
                    sql = "UPDATE appartements SET nom=?, adresse=?, ville=?, type_appartement=?, capacite=?, prix_par_nuit=?, disponibilite=?, statut=? WHERE appartement_id=?";
                }
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, nom.getText());
                    stmt.setString(2, adresse.getText());
                    stmt.setString(3, ville.getText());
                    stmt.setString(4, type.getText());
                    stmt.setInt(5, Integer.parseInt(capacite.getText()));
                    stmt.setDouble(6, Double.parseDouble(prix.getText()));
                    stmt.setBoolean(7, dispo.isSelected());
                    stmt.setString(8, (String) statut.getSelectedItem());
                    if (appartementId != null) stmt.setInt(9, appartementId);

                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(form, "Appartement enregistré avec succès.");
                    form.dispose();
                    loadAppartements("");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(form, "Erreur lors de l'enregistrement.");
            }
        });

        form.setVisible(true);
    }
}
