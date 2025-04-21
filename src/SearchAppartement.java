import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SearchAppartement extends JFrame {
    private int clientId;
    private JTable table;
    private DefaultTableModel model;

    public SearchAppartement(int clientId) {
        this.clientId = clientId;

        setTitle("Recherche d'appartement");
        setSize(800, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        model = new DefaultTableModel(new String[]{"ID", "Nom", "Adresse", "Ville", "Type", "Capacité", "Prix", "Disponibilité", "Action"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return column == 8;
            }
        };
        table = new JTable(model);
        table.getColumn("Action").setCellRenderer(new ButtonRenderer());
        table.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JButton backButton = new JButton("Retour au Dashboard");
        backButton.addActionListener(e -> {
            dispose();
            new DashboardClient(clientId);
        });
        add(backButton, BorderLayout.SOUTH);

        loadAppartements();
        setVisible(true);
    }

    private void loadAppartements() {
        model.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT * FROM appartements";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("appartement_id"),
                    rs.getString("nom"),
                    rs.getString("adresse"),
                    rs.getString("ville"),
                    rs.getString("type_appartement"),
                    rs.getInt("capacite"),
                    rs.getDouble("prix_par_nuit"),
                    rs.getInt("disponibilite") == 1 ? "Oui" : "Non",
                    "Faire une réservation"
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void faireReservation(int appartementId) {
        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.add(new JLabel("Date début:"));
        JSpinner dateDebut = new JSpinner(new SpinnerDateModel());
        panel.add(dateDebut);
        panel.add(new JLabel("Date fin:"));
        JSpinner dateFin = new JSpinner(new SpinnerDateModel());
        panel.add(dateFin);

        int result = JOptionPane.showConfirmDialog(this, panel, "Choisissez les dates", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            Date debut = (Date) dateDebut.getValue();
            Date fin = (Date) dateFin.getValue();

            if (fin.before(debut)) {
                JOptionPane.showMessageDialog(this, "La date de fin doit être après la date de début.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String dDebut = sdf.format(debut);
            String dFin = sdf.format(fin);

            try (Connection conn = DBConnection.getConnection()) {
                String checkQuery = "SELECT COUNT(*) FROM locations WHERE appartement_id = ? AND ((date_debut <= ? AND date_fin >= ?) OR (date_debut <= ? AND date_fin >= ?) OR (date_debut >= ? AND date_fin <= ?))";
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setInt(1, appartementId);
                checkStmt.setString(2, dDebut);
                checkStmt.setString(3, dDebut);
                checkStmt.setString(4, dFin);
                checkStmt.setString(5, dFin);
                checkStmt.setString(6, dDebut);
                checkStmt.setString(7, dFin);

                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                if (rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this, "Appartement non disponible sur cette période. Choisissez une autre.", "Indisponible", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String insert = "INSERT INTO locations (client_id, appartement_id, date_debut, date_fin, nombre_personnes) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insert);
                insertStmt.setInt(1, clientId);
                insertStmt.setInt(2, appartementId);
                insertStmt.setString(3, dDebut);
                insertStmt.setString(4, dFin);
                insertStmt.setInt(5, 1); // nombre_personnes par défaut à 1
                insertStmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Réservation effectuée avec succès !");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Render & Editor pour le bouton dans JTable
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setText("Faire une réservation");
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private int selectedRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("Faire une réservation");
            button.addActionListener(e -> {
                int appartementId = (int) model.getValueAt(selectedRow, 0);
                faireReservation(appartementId);
            });
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            selectedRow = row;
            return button;
        }
    }
}
