import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class AdminAdmins extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private Color primaryColor = new Color(41, 128, 185); // Blue
    private Color accentColor = new Color(142, 68, 173); // Purple for admin management
    private Color lightColor = new Color(236, 240, 241); // Light gray
    private Color textColor = new Color(44, 62, 80); // Dark blue/gray
    private Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private Font headerFont = new Font("Segoe UI", Font.BOLD, 24);

    public AdminAdmins() {
        setTitle("Gestion des Admins");
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
        
        JLabel titleLabel = new JLabel("Gestion des Administrateurs");
        titleLabel.setFont(headerFont);
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        add(headerPanel, BorderLayout.NORTH);

        // Main table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        model = new DefaultTableModel(new String[]{"ID", "Nom", "Prénom", "Email", "Super Admin", "Actions"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only allow editing for the Actions column
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
        table.getColumnModel().getColumn(4).setMaxWidth(100); // Super Admin column
        
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
        
        JButton addButton = new JButton("Ajouter un Admin");
        addButton.setFont(mainFont);
        addButton.setBackground(accentColor);
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        addButton.addActionListener(e -> openAdminForm(null));
        
        JButton backButton = new JButton("Retour au Dashboard");
        backButton.setFont(mainFont);
        backButton.setBackground(primaryColor);
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        backButton.addActionListener(e -> {
            dispose();
            new DashboardSuperAdmin();
        });
        
        footerPanel.add(addButton);
        footerPanel.add(backButton);
        
        add(footerPanel, BorderLayout.SOUTH);

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

                JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
                actionPanel.setBackground(Color.WHITE);
                
                JButton updateBtn = createActionButton("Modifier", new Color(52, 152, 219));
                JButton deleteBtn = createActionButton("Supprimer", new Color(231, 76, 60));

                updateBtn.addActionListener(e -> {
                    Admin admin = new Admin(id, nom, prenom, email, "", superAdmin);
                    openAdminForm(admin);
                });

                deleteBtn.addActionListener(e -> {
                    int confirm = JOptionPane.showConfirmDialog(this, 
                        "Êtes-vous sûr de vouloir supprimer cet administrateur ?", 
                        "Confirmation de suppression", 
                        JOptionPane.YES_NO_OPTION, 
                        JOptionPane.WARNING_MESSAGE);
                    if (confirm == JOptionPane.YES_OPTION) {
                        deleteAdmin(id);
                    }
                });

                actionPanel.add(updateBtn);
                actionPanel.add(deleteBtn);

                model.addRow(new Object[]{id, nom, prenom, email, superAdmin ? "Oui" : "Non", actionPanel});
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Erreur lors du chargement des administrateurs: " + e.getMessage(), 
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }

        TableColumn actionCol = table.getColumn("Actions");
        actionCol.setCellRenderer(new ActionButtonRenderer());
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

    private void openAdminForm(Admin admin) {
        JDialog dialog = new JDialog(this, admin == null ? "Ajouter un Admin" : "Modifier un Admin", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        // Form panel with stylish look
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        // Form title
        JLabel titleLabel = new JLabel(admin == null ? "Ajouter un administrateur" : "Modifier un administrateur");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(textColor);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        formPanel.add(titleLabel);

        // Form fields
        JTextField nomField = createTextField(admin != null ? admin.nom : "");
        JTextField prenomField = createTextField(admin != null ? admin.prenom : "");
        JTextField emailField = createTextField(admin != null ? admin.email : "");
        JPasswordField passField = new JPasswordField();
        passField.setFont(mainFont);
        passField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        JCheckBox superAdminCheck = new JCheckBox("Super Admin", admin != null && admin.superadmin);
        superAdminCheck.setFont(mainFont);
        superAdminCheck.setBackground(Color.WHITE);
        superAdminCheck.setForeground(textColor);

        // Create form sections
        addFormField(formPanel, "Nom:", nomField);
        addFormField(formPanel, "Prénom:", prenomField);
        addFormField(formPanel, "Email:", emailField);
        addFormField(formPanel, admin == null ? "Mot de passe:" : "Nouveau mot de passe:", passField);
        
        JPanel checkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkPanel.setBackground(Color.WHITE);
        JLabel roleLabel = new JLabel("Rôle:");
        roleLabel.setFont(mainFont);
        roleLabel.setForeground(textColor);
        roleLabel.setPreferredSize(new Dimension(120, 30));
        checkPanel.add(roleLabel);
        checkPanel.add(superAdminCheck);
        formPanel.add(checkPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

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
            String nom = nomField.getText().trim();
            String prenom = prenomField.getText().trim();
            String email = emailField.getText().trim();
            String mdp = new String(passField.getPassword()).trim();
            boolean superadmin = superAdminCheck.isSelected();

            if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, 
                    "Veuillez remplir tous les champs obligatoires.", 
                    "Champs manquants", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (admin == null && mdp.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, 
                    "Veuillez saisir un mot de passe pour le nouvel administrateur.", 
                    "Mot de passe requis", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (admin == null) {
                insertAdmin(nom, prenom, email, mdp, superadmin);
            } else {
                updateAdmin(admin.id, nom, prenom, email, mdp, superadmin);
            }

            dialog.dispose();
        });

        buttonPanel.add(cancelBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(saveBtn);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JTextField createTextField(String initialValue) {
        JTextField field = new JTextField(initialValue);
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
            JOptionPane.showMessageDialog(this, 
                "Administrateur ajouté avec succès.", 
                "Succès", JOptionPane.INFORMATION_MESSAGE);
            loadAdmins();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Erreur lors de l'ajout: " + e.getMessage(), 
                "Erreur", JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(this, 
                    "Administrateur mis à jour avec succès.", 
                    "Succès", JOptionPane.INFORMATION_MESSAGE);
                loadAdmins();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Erreur lors de la mise à jour: " + e.getMessage(), 
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteAdmin(int id) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM admin WHERE admin_id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, 
                "Administrateur supprimé avec succès.", 
                "Succès", JOptionPane.INFORMATION_MESSAGE);
            loadAdmins();
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
