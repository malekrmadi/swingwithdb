import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class AdminClients extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private Color primaryColor = new Color(41, 128, 185); // Blue
    private Color accentColor = new Color(155, 89, 182); // Purple for client management
    private Color lightColor = new Color(236, 240, 241); // Light gray
    private Color textColor = new Color(44, 62, 80); // Dark blue/gray
    private Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private Font headerFont = new Font("Segoe UI", Font.BOLD, 24);

    public AdminClients(boolean isSuperAdmin) {
        setTitle("Gestion des Clients");
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
        
        JLabel titleLabel = new JLabel("Gestion des Clients");
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
        
        String[] columns = {"ID", "Nom", "Prénom", "Email", "Téléphone", "Adresse", "Statut", "Fidélité", "Points", "Actions"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 9; // Only allow editing for the Actions column
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
        table.getColumnModel().getColumn(7).setMaxWidth(80); // Fidélité column
        table.getColumnModel().getColumn(8).setMaxWidth(80); // Points column
        
        // Custom renderer for the Actions column
        TableColumn actionCol = table.getColumn("Actions");
        actionCol.setCellRenderer(new ActionButtonRenderer());
        
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
        
        JButton addButton = new JButton("Ajouter un Client");
        addButton.setFont(mainFont);
        addButton.setBackground(accentColor);
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        addButton.addActionListener(e -> showClientForm(null));
        
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
        searchButton.addActionListener(e -> loadClients(searchField.getText()));
        searchField.addActionListener(e -> loadClients(searchField.getText()));
        
        // Initialize client list
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
                    row[0] = rs.getInt("client_id");
                    row[1] = rs.getString("nom");
                    row[2] = rs.getString("prenom");
                    row[3] = rs.getString("email");
                    row[4] = rs.getString("telephone");
                    row[5] = rs.getString("adresse");
                    row[6] = rs.getString("statut") != null ? rs.getString("statut") : "actif";
                    row[7] = rs.getBoolean("client_fidele") ? "Oui" : "Non";
                    row[8] = rs.getInt("points_fidelite");

                    int clientId = rs.getInt("client_id");
                    JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
                    panel.setBackground(Color.WHITE);
                    
                    JButton updateBtn = createActionButton("Modifier", new Color(52, 152, 219));
                    JButton deleteBtn = createActionButton("Supprimer", new Color(231, 76, 60));

                    updateBtn.addActionListener(e -> showClientForm(clientId));
                    deleteBtn.addActionListener(e -> {
                        int confirm = JOptionPane.showConfirmDialog(this, 
                                "Êtes-vous sûr de vouloir supprimer ce client ?", 
                                "Confirmation de suppression", 
                                JOptionPane.YES_NO_OPTION, 
                                JOptionPane.WARNING_MESSAGE);
                        if (confirm == JOptionPane.YES_OPTION) deleteClient(clientId);
                    });

                    panel.add(updateBtn);
                    panel.add(deleteBtn);
                    row[9] = panel;
                    model.addRow(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                    "Erreur lors du chargement des clients: " + e.getMessage(), 
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

    private void showClientForm(Integer clientId) {
        JDialog dialog = new JDialog(this, clientId == null ? "Ajouter un client" : "Modifier un client", true);
        dialog.setSize(500, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        // Form panel with stylish look
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        // Form fields
        JTextField nom = createTextField();
        JTextField prenom = createTextField();
        JTextField email = createTextField();
        JPasswordField motDePasse = new JPasswordField();
        motDePasse.setFont(mainFont);
        motDePasse.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        JTextField telephone = createTextField();
        JTextField adresse = createTextField();
        
        String[] statutOptions = {"actif", "inactif", "banni"};
        JComboBox<String> statut = new JComboBox<>(statutOptions);
        statut.setFont(mainFont);
        statut.setBackground(Color.WHITE);
        
        JCheckBox fidele = new JCheckBox("Client fidèle");
        fidele.setFont(mainFont);
        fidele.setBackground(Color.WHITE);
        
        JTextField points = createTextField();
        
        // Create form sections
        addFormField(formPanel, "Nom:", nom);
        addFormField(formPanel, "Prénom:", prenom);
        addFormField(formPanel, "Email:", email);
        addFormField(formPanel, "Mot de passe:", motDePasse);
        addFormField(formPanel, "Téléphone:", telephone);
        addFormField(formPanel, "Adresse:", adresse);
        addFormField(formPanel, "Statut:", statut);
        addFormField(formPanel, "Points fidélité:", points);
        
        JPanel fidelePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fidelePanel.setBackground(Color.WHITE);
        fidelePanel.add(fidele);
        formPanel.add(fidelePanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Load client data if editing
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
                    statut.setSelectedItem(rs.getString("statut") != null ? rs.getString("statut") : "actif");
                    points.setText(String.valueOf(rs.getInt("points_fidelite")));
                    fidele.setSelected(rs.getBoolean("client_fidele"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(dialog, 
                        "Erreur lors du chargement des données client: " + e.getMessage(), 
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // Default values for new client
            points.setText("0");
            statut.setSelectedItem("actif");
        }

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
        
        JButton saveBtn = new JButton("Enregistrer");
        saveBtn.setFont(mainFont);
        saveBtn.setBackground(accentColor);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        saveBtn.addActionListener(e -> {
            // Validate form
            if (nom.getText().isEmpty() || prenom.getText().isEmpty() || email.getText().isEmpty() 
                    || motDePasse.getPassword().length == 0) {
                JOptionPane.showMessageDialog(dialog, "Veuillez remplir tous les champs obligatoires.", 
                        "Champs manquants", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                int pointsValue = Integer.parseInt(points.getText());
                if (pointsValue < 0) {
                    JOptionPane.showMessageDialog(dialog, "Les points doivent être un nombre positif.", 
                            "Valeur incorrecte", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Les points doivent être un nombre entier.", 
                        "Valeur incorrecte", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
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
                stmt.setString(4, new String(motDePasse.getPassword()));
                stmt.setString(5, telephone.getText());
                stmt.setString(6, adresse.getText());
                stmt.setString(7, (String) statut.getSelectedItem());
                stmt.setInt(8, Integer.parseInt(points.getText()));
                stmt.setBoolean(9, fidele.isSelected());
                if (clientId != null) stmt.setInt(10, clientId);

                stmt.executeUpdate();
                dialog.dispose();
                loadClients("");
                
                JOptionPane.showMessageDialog(this, 
                        clientId == null ? "Client ajouté avec succès!" : "Client mis à jour avec succès!", 
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, 
                        "Erreur lors de l'enregistrement: " + ex.getMessage(), 
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(cancelBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(saveBtn);

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

    private void deleteClient(int clientId) {
        try (Connection conn = DBConnection.getConnection()) {
            try {
                // Begin transaction
                conn.setAutoCommit(false);
                
                // First check if client has locations
                PreparedStatement checkStmt = conn.prepareStatement(
                        "SELECT COUNT(*) FROM locations WHERE client_id = ?");
                checkStmt.setInt(1, clientId);
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                int locationCount = rs.getInt(1);
                
                if (locationCount > 0) {
                    int confirm = JOptionPane.showConfirmDialog(this, 
                            "Ce client a " + locationCount + " location(s). La suppression supprimera également toutes ses locations. Continuer?", 
                            "Attention", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    
                    if (confirm != JOptionPane.YES_OPTION) {
                        conn.rollback();
                        return;
                    }
                    
                    // Delete associated locations first
                    PreparedStatement deleteLocationsStmt = conn.prepareStatement(
                            "DELETE FROM locations WHERE client_id = ?");
                    deleteLocationsStmt.setInt(1, clientId);
                    deleteLocationsStmt.executeUpdate();
                }
                
                // Now delete the client
                PreparedStatement deleteClientStmt = conn.prepareStatement(
                        "DELETE FROM clients WHERE client_id = ?");
                deleteClientStmt.setInt(1, clientId);
                deleteClientStmt.executeUpdate();
                
                // Commit transaction
                conn.commit();
                loadClients("");
                JOptionPane.showMessageDialog(this, "Client supprimé avec succès.");
                
            } catch (SQLException e) {
                conn.rollback();  // Rollback on error
                throw e;  // Re-throw for outer catch
            } finally {
                conn.setAutoCommit(true);  // Restore auto-commit
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                    "Erreur lors de la suppression: " + e.getMessage(), 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
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

