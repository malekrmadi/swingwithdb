import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MesLocations extends JFrame {
    private int clientId;
    private Color primaryColor = new Color(41, 128, 185);
    private Color accentColor = new Color(46, 204, 113);
    private Color lightColor = new Color(236, 240, 241);
    private Color textColor = new Color(44, 62, 80);
    private Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private Font headerFont = new Font("Segoe UI", Font.BOLD, 24);
    private DefaultTableModel model;

    public MesLocations(int clientId) {
        this.clientId = clientId;

        setTitle("Mes Locations");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(accentColor);
        headerPanel.setPreferredSize(new Dimension(800, 70));
        headerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 15));

        JLabel titleLabel = new JLabel("Mes Locations");
        titleLabel.setFont(headerFont);
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        add(headerPanel, BorderLayout.NORTH);

        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel infoLabel = new JLabel("Voici la liste de vos locations actuelles et passées:");
        infoLabel.setFont(mainFont);
        infoLabel.setForeground(textColor);
        infoPanel.add(infoLabel);

        contentPanel.add(infoPanel, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"ID", "Appartement", "Date Début", "Date Fin", "Personnes", "Action"};
        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Action column editable (for buttons)
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(50);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionBackground(new Color(230, 230, 230));
        table.setSelectionForeground(textColor);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(lightColor);
        header.setForeground(textColor);

        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(4).setMaxWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(200);

        // Renderer pour les boutons
        table.getColumn("Action").setCellRenderer(new ActionCellRenderer());
        table.getColumn("Action").setCellEditor(new ActionCellEditor(new JCheckBox(), this));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        contentPanel.add(scrollPane, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);

        // Footer panel
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(lightColor);
        footerPanel.setPreferredSize(new Dimension(800, 60));
        footerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 10));

        JButton backButton = new JButton("Retour au Dashboard");
        backButton.setFont(mainFont);
        backButton.setBackground(primaryColor);
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        backButton.addActionListener(e -> {
            dispose();
            new DashboardClient(clientId);
        });
        footerPanel.add(backButton);

        add(footerPanel, BorderLayout.SOUTH);

        // Load locations
        loadLocations();
        setVisible(true);
    }

    private void loadLocations() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT l.location_id, a.nom AS appartement, l.date_debut, l.date_fin, l.nombre_personnes " +
                    "FROM locations l JOIN appartements a ON l.appartement_id = a.appartement_id " +
                    "WHERE l.client_id = ? ORDER BY l.date_debut DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();

            model.setRowCount(0); // Clear table before loading
            while (rs.next()) {
                int id = rs.getInt("location_id");
                String nomAppartement = rs.getString("appartement");
                Date debut = rs.getDate("date_debut");
                Date fin = rs.getDate("date_fin");
                int personnes = rs.getInt("nombre_personnes");

                model.addRow(new Object[]{id, nomAppartement, debut, fin, personnes, ""});
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des locations : " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Action quand on clique sur "Annuler" ou "Rendre Appartement"
    public void annulerLocation(int locationId) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Êtes-vous sûr de vouloir annuler cette location ?", "Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "DELETE FROM locations WHERE location_id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, locationId);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Location annulée avec succès !");
                loadLocations();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erreur lors de l'annulation : " + e.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void rendreAppartement(int locationId) {
        JDialog dialog = new JDialog(this, "Rendre Appartement", true);
        dialog.setSize(300, 200);
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("Date de retour (yyyy-MM-dd) :");
        JTextField dateField = new JTextField();

        JButton confirmButton = new JButton("Confirmer");
        confirmButton.setBackground(accentColor);
        confirmButton.setForeground(Color.WHITE);

        panel.add(label);
        panel.add(dateField);
        panel.add(confirmButton);

        dialog.add(panel, BorderLayout.CENTER);

        confirmButton.addActionListener(e -> {
            String dateRetourStr = dateField.getText().trim();
            if (!dateRetourStr.isEmpty()) {
                try (Connection conn = DBConnection.getConnection()) {
                    String sql = "{ CALL calculer_penalite_retard(?, ?) }"; // ta procédure stockée
                    CallableStatement stmt = conn.prepareCall(sql);
                    stmt.setInt(1, locationId);
                    stmt.setDate(2, java.sql.Date.valueOf(dateRetourStr));
                    stmt.execute();
                    JOptionPane.showMessageDialog(this, "Appartement rendu avec succès !");
                    dialog.dispose();
                    loadLocations();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Veuillez saisir une date valide.");
            }
        });

        dialog.setVisible(true);
    }

    // Custom Renderer
    class ActionCellRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
        JPanel panel;
        JButton annulerButton;
        JButton rendreButton;

        public ActionCellRenderer() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            annulerButton = new JButton("Annuler");
            rendreButton = new JButton("Rendre");

            annulerButton.setBackground(new Color(231, 76, 60));
            annulerButton.setForeground(Color.WHITE);
            rendreButton.setBackground(new Color(52, 152, 219));
            rendreButton.setForeground(Color.WHITE);

            panel.add(annulerButton);
            panel.add(rendreButton);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            return panel;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                                                      int row, int column) {
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }

    // Custom Editor pour les boutons
    class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {
        JPanel panel;
        JButton annulerButton;
        JButton rendreButton;
        MesLocations parent;
        int selectedRow;

        public ActionCellEditor(JCheckBox checkBox, MesLocations parent) {
            this.parent = parent;
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));

            annulerButton = new JButton("Annuler");
            rendreButton = new JButton("Rendre");

            annulerButton.setBackground(new Color(231, 76, 60));
            annulerButton.setForeground(Color.WHITE);
            rendreButton.setBackground(new Color(52, 152, 219));
            rendreButton.setForeground(Color.WHITE);

            annulerButton.addActionListener(e -> {
                int locationId = (int) parent.model.getValueAt(selectedRow, 0);
                parent.annulerLocation(locationId);
            });

            rendreButton.addActionListener(e -> {
                int locationId = (int) parent.model.getValueAt(selectedRow, 0);
                parent.rendreAppartement(locationId);
            });

            panel.add(annulerButton);
            panel.add(rendreButton);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                                                      int row, int column) {
            this.selectedRow = row;
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }
}
