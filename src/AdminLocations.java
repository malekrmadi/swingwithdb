// AdminLocations.java

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.sql.*;

public class AdminLocations extends JFrame {
    private JTable table;
    private DefaultTableModel model;

    public AdminLocations(boolean isSuperAdmin) {
        setTitle("Gestion des Locations");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel searchPanel = new JPanel();
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Rechercher");
        searchPanel.add(new JLabel("Recherche client ou appart:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        add(searchPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "Client", "Appartement", "Date début", "Date fin", "Pénalité", "Personnes", "Actions"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        table.setRowHeight(40);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton addButton = new JButton("Ajouter Location");
        addButton.addActionListener(e -> openLocationForm(null));
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

        searchButton.addActionListener(e -> loadLocations(searchField.getText()));
        loadLocations("");

        setVisible(true);
    }

    private void loadLocations(String filter) {
        model.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT l.*, c.nom AS client_nom, a.nom AS appart_nom FROM locations l " +
                           "JOIN clients c ON l.client_id = c.client_id " +
                           "JOIN appartements a ON l.appartement_id = a.appartement_id " +
                           "WHERE c.nom LIKE ? OR a.nom LIKE ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, "%" + filter + "%");
                stmt.setString(2, "%" + filter + "%");
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    Object[] row = new Object[8];
                    int id = rs.getInt("location_id");
                    row[0] = id;
                    row[1] = rs.getString("client_nom");
                    row[2] = rs.getString("appart_nom");
                    row[3] = rs.getDate("date_debut");
                    row[4] = rs.getDate("date_fin");
                    row[5] = rs.getBigDecimal("penalite_retard");
                    row[6] = rs.getInt("nombre_personnes");

                    JButton btnUpdate = new JButton("Modifier");
                    JButton btnDelete = new JButton("Supprimer");
                    btnUpdate.addActionListener(e -> openLocationForm(rs));
                    btnDelete.addActionListener(e -> {
                        int confirm = JOptionPane.showConfirmDialog(this, "Supprimer cette location ?");
                        if (confirm == JOptionPane.YES_OPTION) deleteLocation(id);
                    });

                    JPanel actionPanel = new JPanel(new FlowLayout());
                    actionPanel.add(btnUpdate);
                    actionPanel.add(btnDelete);
                    row[7] = actionPanel;
                    model.addRow(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement.");
        }

        TableColumn actionCol = table.getColumn("Actions");
        actionCol.setCellRenderer((table, value, isSelected, hasFocus, row, column) -> (Component) value);
    }

    private void deleteLocation(int id) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "DELETE FROM locations WHERE location_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Location supprimée.");
                loadLocations("");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur suppression.");
        }
    }

    private void openLocationForm(ResultSet rs) {
        JDialog dialog = new JDialog(this, "Formulaire Location", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(6, 2));

        JTextField clientIdField = new JTextField();
        JTextField appartIdField = new JTextField();
        JTextField dateDebutField = new JTextField("YYYY-MM-DD");
        JTextField dateFinField = new JTextField("YYYY-MM-DD");
        JTextField penaliteField = new JTextField();
        JTextField personnesField = new JTextField();

        try {
            if (rs != null) {
                clientIdField.setText(rs.getString("client_id"));
                appartIdField.setText(rs.getString("appartement_id"));
                dateDebutField.setText(rs.getString("date_debut"));
                dateFinField.setText(rs.getString("date_fin"));
                penaliteField.setText(rs.getString("penalite_retard"));
                personnesField.setText(rs.getString("nombre_personnes"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        dialog.add(new JLabel("Client ID:"));
        dialog.add(clientIdField);
        dialog.add(new JLabel("Appartement ID:"));
        dialog.add(appartIdField);
        dialog.add(new JLabel("Date début:"));
        dialog.add(dateDebutField);
        dialog.add(new JLabel("Date fin:"));
        dialog.add(dateFinField);
        dialog.add(new JLabel("Pénalité retard:"));
        dialog.add(penaliteField);
        dialog.add(new JLabel("Nombre de personnes:"));
        dialog.add(personnesField);

        JButton saveButton = new JButton("Enregistrer");
        saveButton.addActionListener(e -> {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = (rs != null) ?
                    "UPDATE locations SET client_id=?, appartement_id=?, date_debut=?, date_fin=?, penalite_retard=?, nombre_personnes=? WHERE location_id=?" :
                    "INSERT INTO locations (client_id, appartement_id, date_debut, date_fin, penalite_retard, nombre_personnes) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, Integer.parseInt(clientIdField.getText()));
                    stmt.setInt(2, Integer.parseInt(appartIdField.getText()));
                    stmt.setDate(3, Date.valueOf(dateDebutField.getText()));
                    stmt.setDate(4, Date.valueOf(dateFinField.getText()));
                    stmt.setBigDecimal(5, new java.math.BigDecimal(penaliteField.getText()));
                    stmt.setInt(6, Integer.parseInt(personnesField.getText()));
                    if (rs != null) {
                        stmt.setInt(7, rs.getInt("location_id"));
                    }
                    stmt.executeUpdate();
                    dialog.dispose();
                    loadLocations("");
                    JOptionPane.showMessageDialog(this, "Enregistré avec succès !");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erreur enregistrement.");
            }
        });

        dialog.add(saveButton);
        dialog.setVisible(true);
    }
}
