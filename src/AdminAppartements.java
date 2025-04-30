import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class AdminAppartements extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private Color primaryColor = new Color(41, 128, 185); // Blue
    private Color accentColor = new Color(52, 152, 219); // Light blue for appartements
    private Color lightColor = new Color(236, 240, 241); // Light gray
    private Color textColor = new Color(44, 62, 80); // Dark blue/gray
    private Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private Font headerFont = new Font("Segoe UI", Font.BOLD, 24);

    public AdminAppartements(boolean isSuperAdmin) {
        setTitle("Gestion des Appartements");
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
        
        JLabel titleLabel = new JLabel("Gestion des Appartements");
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
        
        JLabel searchLabel = new JLabel("Rechercher: ");
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

        String[] columns = {"ID", "Nom", "Adresse", "Ville", "Type", "Capacite", "Prix", "Disponible", "Statut", "Date Ajout", "Note", "Actions"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 11; // Only allow editing for the Actions column
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
        table.getColumnModel().getColumn(5).setMaxWidth(80); // Capacité column
        table.getColumnModel().getColumn(7).setMaxWidth(80); // Disponible column
        table.getColumnModel().getColumn(10).setMaxWidth(60); // Note column
        
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
        
        JButton addButton = new JButton("Ajouter un Appartement");
        addButton.setFont(mainFont);
        addButton.setBackground(accentColor);
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        addButton.addActionListener(e -> openAppartementForm(null));
        
        JButton backButton = new JButton("Retour au Dashboard");
        backButton.setFont(mainFont);
        backButton.setBackground(primaryColor);
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        backButton.addActionListener(e -> {
            dispose();
            if (isSuperAdmin) {
                new DashboardSuperAdmin();
            } else {
                new DashboardAdmin();
            }
        });
        
        footerPanel.add(addButton);
        footerPanel.add(backButton);
        
        add(footerPanel, BorderLayout.SOUTH);

        // Add listeners
        searchButton.addActionListener(e -> loadAppartements(searchField.getText()));
        searchField.addActionListener(e -> loadAppartements(searchField.getText()));
        
        // Initialize appartement list
        loadAppartements("");
        setVisible(true);
    }

    private void loadAppartements(String filtre) {
        model.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM appartements";
            if (!filtre.isEmpty()) {
                sql += " WHERE nom LIKE ? OR adresse LIKE ? OR ville LIKE ?";
            }
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                if (!filtre.isEmpty()) {
                    stmt.setString(1, "%" + filtre + "%");
                    stmt.setString(2, "%" + filtre + "%");
                    stmt.setString(3, "%" + filtre + "%");
                }
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    Object[] row = new Object[12];
                    row[0] = rs.getInt("appartement_id");
                    row[1] = rs.getString("nom");
                    row[2] = rs.getString("adresse");
                    row[3] = rs.getString("ville");
                    row[4] = rs.getString("type_appartement");
                    row[5] = rs.getInt("capacite");
                    row[6] = rs.getDouble("prix_par_nuit") + " €";
                    row[7] = rs.getBoolean("disponibilite") ? "Oui" : "Non";
                    row[8] = rs.getString("statut");
                    row[9] = rs.getDate("date_ajout");
                    row[10] = rs.getDouble("rating");

                    int id = rs.getInt("appartement_id");
                    JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
                    actionPanel.setBackground(Color.WHITE);

                    JButton btnUpdate = createActionButton("Modifier", new Color(52, 152, 219));
                    JButton btnDelete = createActionButton("Supprimer", new Color(231, 76, 60));

                    btnUpdate.addActionListener(e -> openAppartementForm(id));
                    btnDelete.addActionListener(e -> {
                        int confirm = JOptionPane.showConfirmDialog(this, 
                                "Êtes-vous sûr de vouloir supprimer cet appartement ?", 
                                "Confirmation de suppression", 
                                JOptionPane.YES_NO_OPTION, 
                                JOptionPane.WARNING_MESSAGE);
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
            JOptionPane.showMessageDialog(this, 
                    "Erreur lors du chargement des appartements: " + e.getMessage(), 
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

    private void deleteAppartement(int id) {
        try (Connection conn = DBConnection.getConnection()) {
            try {
                // Begin transaction
                conn.setAutoCommit(false);
                
                // Check if there are any locations using this appartement
                String checkSQL = "SELECT COUNT(*) FROM locations WHERE appartement_id = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSQL)) {
                    checkStmt.setInt(1, id);
                    ResultSet rs = checkStmt.executeQuery();
                    rs.next();
                    int locationCount = rs.getInt(1);
                    
                    if (locationCount > 0) {
                        int confirm = JOptionPane.showConfirmDialog(this, 
                                "Cet appartement a " + locationCount + " location(s). La suppression echouera à moins de supprimer ces locations. Continuer?", 
                                "Attention", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        
                        if (confirm != JOptionPane.YES_OPTION) {
                            conn.rollback();
                            return;
                        }
                    }
                }
                
                String deleteSQL = "DELETE FROM appartements WHERE appartement_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteSQL)) {
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                    conn.commit();
                    JOptionPane.showMessageDialog(this, "Appartement supprime avec succes.");
                    loadAppartements("");
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                    "Erreur lors de la suppression: " + e.getMessage(), 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openAppartementForm(Integer appartementId) {
        JDialog dialog = new JDialog(this, appartementId == null ? "Ajouter un appartement" : "Modifier un appartement", true);
        dialog.setSize(500, 550);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JTextField nom = createTextField();
        JTextField adresse = createTextField();
        JTextField ville = createTextField();
        JTextField capacite = createTextField();
        JTextField prix = createTextField();

        // Type dropdown
        String[] typeOptions = {"studio", "villa", "duplex"};
        JComboBox<String> type = new JComboBox<>(typeOptions);
        type.setFont(mainFont);
        type.setBackground(Color.WHITE);

        // Statut dropdown
        String[] statutOptions = {"disponible", "en_renovation", "en_maintenance"};
        JComboBox<String> statut = new JComboBox<>(statutOptions);
        statut.setFont(mainFont);
        statut.setBackground(Color.WHITE);

        addFormField(formPanel, "Nom:", nom);
        addFormField(formPanel, "Adresse:", adresse);
        addFormField(formPanel, "Ville:", ville);
        addFormField(formPanel, "Type:", type);
        addFormField(formPanel, "Capacite:", capacite);
        addFormField(formPanel, "Prix par nuit :", prix);
        addFormField(formPanel, "Statut:", statut);

        // Load appartement data if editing
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
                        type.setSelectedItem(rs.getString("type_appartement"));
                        capacite.setText(String.valueOf(rs.getInt("capacite")));
                        prix.setText(String.valueOf(rs.getDouble("prix_par_nuit")));
                        statut.setSelectedItem(rs.getString("statut"));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(dialog, 
                        "Erreur lors du chargement des donnees: " + e.getMessage(), 
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            statut.setSelectedItem("disponible");
        }

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
            if (nom.getText().isEmpty() || adresse.getText().isEmpty() || ville.getText().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Veuillez remplir tous les champs obligatoires.", 
                        "Champs manquants", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                int capaciteValue = Integer.parseInt(capacite.getText());
                if (capaciteValue <= 0) {
                    JOptionPane.showMessageDialog(dialog, "La capacite doit être un nombre positif.", 
                            "Valeur incorrecte", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "La capacite doit être un nombre entier.", 
                        "Valeur incorrecte", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                double prixValue = Double.parseDouble(prix.getText());
                if (prixValue <= 0) {
                    JOptionPane.showMessageDialog(dialog, "Le prix doit etre un nombre positif.", 
                            "Valeur incorrecte", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Le prix doit etre un nombre decimal.", 
                        "Valeur incorrecte", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String selectedStatut = (String) statut.getSelectedItem();
            boolean isDisponible = selectedStatut.equals("disponible"); // Si statut ≠ "disponible", disponibilité = false

            try (Connection conn = DBConnection.getConnection()) {
                String sql;
                if (appartementId == null) {
                    sql = "INSERT INTO appartements (nom, adresse, ville, type_appartement, capacite, prix_par_nuit, statut, disponibilite) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                } else {
                    sql = "UPDATE appartements SET nom=?, adresse=?, ville=?, type_appartement=?, capacite=?, prix_par_nuit=?, statut=?, disponibilite=? WHERE appartement_id=?";
                }

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, nom.getText());
                    stmt.setString(2, adresse.getText());
                    stmt.setString(3, ville.getText());
                    stmt.setString(4, (String) type.getSelectedItem());
                    stmt.setInt(5, Integer.parseInt(capacite.getText()));
                    stmt.setDouble(6, Double.parseDouble(prix.getText()));
                    stmt.setString(7, selectedStatut);
                    stmt.setBoolean(8, isDisponible);
                    if (appartementId != null) stmt.setInt(9, appartementId);

                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(dialog, 
                            appartementId == null ? "Appartement ajoute avec succes!" : "Appartement mis a jour avec succes!", 
                            "Succès", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadAppartements("");
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
        label.setPreferredSize(new Dimension(120, 30));
        
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
