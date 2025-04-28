// AdminLocations.java

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class AdminLocations extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private Color primaryColor = new Color(41, 128, 185); // Blue
    private Color accentColor = new Color(46, 204, 113); // Green for locations
    private Color lightColor = new Color(236, 240, 241); // Light gray
    private Color textColor = new Color(44, 62, 80); // Dark blue/gray
    private Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private Font headerFont = new Font("Segoe UI", Font.BOLD, 24);

    public AdminLocations(boolean isSuperAdmin) {
        setTitle("Gestion des Locations");
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
        
        JLabel titleLabel = new JLabel("Gestion des Locations");
        titleLabel.setFont(headerFont);
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        add(headerPanel, BorderLayout.NORTH);

        // Search panel
        JPanel searchPanel = new JPanel();
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        searchPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        JTextField searchField = new JTextField(20);
        searchField.setFont(mainFont);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        JButton searchButton = new JButton("Rechercher");
        searchButton.setFont(mainFont);
        searchButton.setBackground(primaryColor);
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        searchButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        
        JLabel searchLabel = new JLabel("Recherche client ou appartement: ");
        searchLabel.setFont(mainFont);
        searchLabel.setForeground(textColor);
        
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        
        add(searchPanel, BorderLayout.NORTH);

        // Main table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));

        String[] columns = {"ID", "Client", "Appartement", "Date debut", "Date fin", "Penalite", "Personnes", "Actions"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // Only allow editing for the Actions column
            }
        };
        
        table = new JTable(model);
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionBackground(new Color(230, 230, 230));
        table.setSelectionForeground(textColor);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        
        // Custom header renderer
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(lightColor);
        header.setForeground(textColor);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(189, 195, 199)));
        
        // Set column widths
        table.getColumnModel().getColumn(0).setMaxWidth(50); // ID column
        table.getColumnModel().getColumn(5).setMaxWidth(80); // Pénalité column
        table.getColumnModel().getColumn(6).setMaxWidth(80); // Personnes column
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        add(tablePanel, BorderLayout.CENTER);

        // Footer panel
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(lightColor);
        footerPanel.setPreferredSize(new Dimension(800, 60));
        footerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
        
        JButton addButton = new JButton("Ajouter une Location");
        addButton.setFont(mainFont);
        addButton.setBackground(accentColor);
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        addButton.addActionListener(e -> openLocationForm(null));
        
        JButton backButton = new JButton("Retour au Dashboard");
        backButton.setFont(mainFont);
        backButton.setBackground(primaryColor);
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        backButton.addActionListener(e -> {
            dispose();
            if (isSuperAdmin) new DashboardSuperAdmin();
            else new DashboardAdmin();
        });
        
        footerPanel.add(addButton);
        footerPanel.add(backButton);
        
        add(footerPanel, BorderLayout.SOUTH);

        // Add listeners
        searchButton.addActionListener(e -> loadLocations(searchField.getText()));
        searchField.addActionListener(e -> loadLocations(searchField.getText()));
        
        // Initialize location list
        loadLocations("");
        setVisible(true);
    }

    private void loadLocations(String filter) {
        model.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT l.*, " +
                    "CONCAT(c.nom, ' ', c.prenom) as client_nom, " +
                    "a.nom as appart_nom " +
                    "FROM locations l " +
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
                    row[5] = rs.getBigDecimal("penalite_retard") + " €";
                    row[6] = rs.getInt("nombre_personnes");

                    JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
                    actionPanel.setBackground(Color.WHITE);
                    
                    JButton btnUpdate = createActionButton("Modifier", new Color(52, 152, 219));
                    JButton btnDelete = createActionButton("Supprimer", new Color(231, 76, 60));

                    final ResultSet rsForUpdate = rs;
                    btnUpdate.addActionListener(e -> openLocationForm(rsForUpdate));
                    btnDelete.addActionListener(e -> {
                        int confirm = JOptionPane.showConfirmDialog(this, 
                                "Êtes-vous sûr de vouloir supprimer cette location ?", 
                                "Confirmation de suppression", 
                                JOptionPane.YES_NO_OPTION, 
                                JOptionPane.WARNING_MESSAGE);
                        if (confirm == JOptionPane.YES_OPTION) deleteLocation(id);
                    });

                    actionPanel.add(btnUpdate);
                    actionPanel.add(btnDelete);
                    row[7] = actionPanel;
                    model.addRow(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                    "Erreur lors du chargement des locations: " + e.getMessage(), 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }

        TableColumn actionCol = table.getColumn("Actions");
        actionCol.setCellRenderer(new ActionButtonRenderer());
        actionCol.setCellEditor(new ActionButtonEditor(table));
    }

    private JButton createActionButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(90, 30));
        return button;
    }

    private void deleteLocation(int id) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "DELETE FROM locations WHERE location_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Location supprimée avec succes.", 
                        "Suppression", JOptionPane.INFORMATION_MESSAGE);
                loadLocations("");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                    "Erreur lors de la suppression: " + e.getMessage(), 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openLocationForm(ResultSet rs) {
        JDialog dialog = new JDialog(this, "Gestion de Location", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        // Form panel with stylish look
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        // Form title
        JLabel titleLabel = new JLabel(rs == null ? "Ajouter une location" : "Modifier une location");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(textColor);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        formPanel.add(titleLabel);

        // Form fields
        JTextField clientIdField = createTextField();
        JTextField appartIdField = createTextField();
        JTextField dateDebutField = createTextField();
        dateDebutField.setText("YYYY-MM-DD");
        JTextField dateFinField = createTextField();
        dateFinField.setText("YYYY-MM-DD");
        JTextField penaliteField = createTextField();
        JTextField personnesField = createTextField();

        try {
            if (rs != null) {
                clientIdField.setText(rs.getString("client_id"));
                appartIdField.setText(rs.getString("appartement_id"));
                dateDebutField.setText(rs.getString("date_debut"));
                dateFinField.setText(rs.getString("date_fin"));
                penaliteField.setText(rs.getString("penalite_retard"));
                personnesField.setText(rs.getString("nombre_personnes"));
            } else {
                penaliteField.setText("0.00");
                personnesField.setText("1");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Create form sections
        addFormField(formPanel, "ID du Client:", clientIdField);
        addFormField(formPanel, "ID de l'Appartement:", appartIdField);
        addFormField(formPanel, "Date de debut (YYYY-MM-DD):", dateDebutField);
        addFormField(formPanel, "Date de fin (YYYY-MM-DD):", dateFinField);
        addFormField(formPanel, "Penalite retard (€):", penaliteField);
        addFormField(formPanel, "Nombre de personnes:", personnesField);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton cancelBtn = new JButton("Annuler");
        cancelBtn.setFont(mainFont);
        cancelBtn.setBackground(lightColor);
        cancelBtn.setForeground(textColor);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        JButton saveButton = new JButton("Enregistrer");
        saveButton.setFont(mainFont);
        saveButton.setBackground(accentColor);
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        saveButton.addActionListener(e -> {
            // Validate form
            try {
                Integer.parseInt(clientIdField.getText());
                Integer.parseInt(appartIdField.getText());
                Date.valueOf(dateDebutField.getText());
                Date.valueOf(dateFinField.getText());
                new java.math.BigDecimal(penaliteField.getText());
                Integer.parseInt(personnesField.getText());
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, 
                        "Veuillez verifier le format des donnees saisies.", 
                        "Erreur de format", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try (Connection conn = DBConnection.getConnection()) {
                String sql;
                if (rs == null) {
                    sql = "INSERT INTO locations (client_id, appartement_id, date_debut, date_fin, penalite_retard, nombre_personnes) " +
                          "VALUES (?, ?, ?, ?, ?, ?)";
                } else {
                    sql = "UPDATE locations SET client_id=?, appartement_id=?, date_debut=?, date_fin=?, penalite_retard=?, " +
                          "nombre_personnes=? WHERE location_id=?";
                }
                
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
                    JOptionPane.showMessageDialog(this, 
                            rs == null ? "Location ajoutee avec succes!" : "Location mise e jour avec succes!", 
                            "Succes", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, 
                        "Erreur lors de l'enregistrement: " + ex.getMessage(), 
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(cancelBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(saveButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
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
    
    private void addFormField(JPanel panel, String labelText, JComponent field) {
        JPanel fieldPanel = new JPanel(new BorderLayout(10, 0));
        fieldPanel.setBackground(Color.WHITE);
        
        JLabel label = new JLabel(labelText);
        label.setFont(mainFont);
        label.setForeground(textColor);
        label.setPreferredSize(new Dimension(180, 30));
        
        fieldPanel.add(label, BorderLayout.WEST);
        fieldPanel.add(field, BorderLayout.CENTER);
        
        panel.add(fieldPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
    }
    
    // Renderer for action buttons in the table
    class ActionButtonRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            if (value instanceof JPanel) {
                JPanel panel = (JPanel) value;
                panel.setBackground(isSelected ? new Color(230, 230, 230) : Color.WHITE);
                return panel;
            }
            
            return new JLabel(value == null ? "" : value.toString());
        }
    }

    // Editor for action buttons in the table
    class ActionButtonEditor extends DefaultCellEditor {
        private JPanel panel;

        public ActionButtonEditor(JTable table) {
            super(new JCheckBox());
            panel = null;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            if (value instanceof JPanel) {
                panel = (JPanel) value;
                panel.setBackground(new Color(230, 230, 230));
                return panel;
            }
            return new JLabel(value == null ? "" : value.toString());
        }

        @Override
        public Object getCellEditorValue() {
            return panel;
        }

        @Override
        public boolean stopCellEditing() {
            return super.stopCellEditing();
        }
    }
}
