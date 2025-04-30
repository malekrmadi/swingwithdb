import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.awt.event.*;

public class SearchAppartement extends JFrame {
    private int clientId;
    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;
    private JComboBox<String> filterTypeBox;
    private Color primaryColor = new Color(41, 128, 185); // Blue
    private Color lightColor = new Color(236, 240, 241); // Light gray
    private Color textColor = new Color(44, 62, 80); // Dark blue/gray
    private Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private Font headerFont = new Font("Segoe UI", Font.BOLD, 24);
    private JButton dispoButton;


    public SearchAppartement(int clientId) {
        this.clientId = clientId;

        setTitle("Recherche d'appartement");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(primaryColor);
        headerPanel.setPreferredSize(new Dimension(800, 70));
        headerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 15));
        
        JLabel titleLabel = new JLabel("Recherche d'appartement");
        titleLabel.setFont(headerFont);
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        add(headerPanel, BorderLayout.NORTH);

        // Search and filter panel
        JPanel searchPanel = new JPanel();
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        searchPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
        
        JLabel searchLabel = new JLabel("Rechercher:");
        searchLabel.setFont(mainFont);
        searchLabel.setForeground(textColor);
        searchPanel.add(searchLabel);
        
        searchField = new JTextField(20);
        searchField.setFont(mainFont);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        searchPanel.add(searchField);
        
        JLabel filterLabel = new JLabel("Type:");
        filterLabel.setFont(mainFont);
        filterLabel.setForeground(textColor);
        searchPanel.add(filterLabel);
        
        filterTypeBox = new JComboBox<>(new String[]{"Tous", "Studio", "Appartement", "Maison", "Villa"});
        filterTypeBox.setFont(mainFont);
        searchPanel.add(filterTypeBox);

        dispoButton = new JButton("Trouver un appartement disponible");
        dispoButton.setFont(mainFont);
        dispoButton.setBackground(new Color(46, 204, 113)); // Vert par exemple
        dispoButton.setForeground(Color.WHITE);
        dispoButton.setFocusPainted(false);
        dispoButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        searchPanel.add(dispoButton);  // ou dans un autre panel selon ta mise en page

        
        JButton searchButton = new JButton("Rechercher");
        searchButton.setFont(mainFont);
        searchButton.setBackground(primaryColor);
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        searchButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        searchPanel.add(searchButton);
        
        add(searchPanel, BorderLayout.NORTH);
        
        // Create table with custom renderer for modern look
        model = new DefaultTableModel(new String[]{"ID", "Nom", "Adresse", "Ville", "Type", "Capacité", "Prix/Nuit", "Disponible", "Action"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return column == 8;
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
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(5).setMaxWidth(80);
        table.getColumnModel().getColumn(7).setMaxWidth(80);
        
        // Button column
        table.getColumn("Action").setCellRenderer(new ModernButtonRenderer());
        table.getColumn("Action").setCellEditor(new ModernButtonEditor(new JCheckBox()));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        add(tablePanel, BorderLayout.CENTER);
        
        // Footer panel with back button
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

        loadAppartements();
        
        // Add action listeners
        searchButton.addActionListener(e -> filterAppartements());
        searchField.addActionListener(e -> filterAppartements());
        filterTypeBox.addActionListener(e -> filterAppartements());
        dispoButton.addActionListener(e -> afficherPopupDisponibilites());

        
        setVisible(true);
    }

    private void filterAppartements() {
        String searchText = searchField.getText().toLowerCase().trim();
        String selectedType = (String) filterTypeBox.getSelectedItem();
        
        model.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT * FROM appartements";
            
            if (!selectedType.equals("Tous")) {
                query += " WHERE type_appartement = ?";
            }
            
            PreparedStatement stmt = conn.prepareStatement(query);
            
            if (!selectedType.equals("Tous")) {
                stmt.setString(1, selectedType);
            }
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                // Filter by search text if entered
                if (!searchText.isEmpty()) {
                    boolean match = false;
                    match |= rs.getString("nom").toLowerCase().contains(searchText);
                    match |= rs.getString("adresse").toLowerCase().contains(searchText);
                    match |= rs.getString("ville").toLowerCase().contains(searchText);
                    
                    if (!match) continue;
                }
                
                model.addRow(new Object[]{
                    rs.getInt("appartement_id"),
                    rs.getString("nom"),
                    rs.getString("adresse"),
                    rs.getString("ville"),
                    rs.getString("type_appartement"),
                    rs.getInt("capacite"),
                    rs.getDouble("prix_par_nuit") + " €",
                    rs.getInt("disponibilite") == 1 ? "Oui" : "Non",
                    "Réserver"
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
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
                    rs.getDouble("prix_par_nuit") + " €",
                    rs.getInt("disponibilite") == 1 ? "Oui" : "Non",
                    "Réserver"
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void faireReservation(int appartementId) {
        // Create custom date picker panel
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("Réservation d'appartement");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panel.add(titleLabel);
        
        JPanel datePanel = new JPanel(new GridLayout(3, 2, 10, 10));
        datePanel.setBackground(Color.WHITE);
        
        JLabel debutLabel = new JLabel("Date de début:");
        debutLabel.setFont(mainFont);
        datePanel.add(debutLabel);
        
        JSpinner dateDebut = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditorDebut = new JSpinner.DateEditor(dateDebut, "dd/MM/yyyy");
        dateDebut.setEditor(dateEditorDebut);
        dateDebut.setFont(mainFont);
        datePanel.add(dateDebut);
        
        JLabel finLabel = new JLabel("Date de fin:");
        finLabel.setFont(mainFont);
        datePanel.add(finLabel);
        
        JSpinner dateFin = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditorFin = new JSpinner.DateEditor(dateFin, "dd/MM/yyyy");
        dateFin.setEditor(dateEditorFin);
        dateFin.setFont(mainFont);
        datePanel.add(dateFin);

        JLabel personnesLabel = new JLabel("Nombre de personnes:");
        personnesLabel.setFont(mainFont);
        datePanel.add(personnesLabel);

        JSpinner spinnerPersonnes = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1)); // min=1, max=100
        spinnerPersonnes.setFont(mainFont);
        datePanel.add(spinnerPersonnes);
        
        panel.add(datePanel);

        int result = JOptionPane.showConfirmDialog(this, panel, "Réservation", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            Date debut = (Date) dateDebut.getValue();
            Date fin = (Date) dateFin.getValue();
            int nbPersonnes = (Integer) spinnerPersonnes.getValue();

            if (fin.before(debut)) {
                JOptionPane.showMessageDialog(this, "La date de fin doit être après la date de début.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String dDebut = sdf.format(debut);
            String dFin = sdf.format(fin);

            try (Connection conn = DBConnection.getConnection()) {
                // Vérifier la capacité de l'appartement
                String getCapaciteQuery = "SELECT capacite FROM appartements WHERE appartement_id = ?";
                PreparedStatement capStmt = conn.prepareStatement(getCapaciteQuery);
                capStmt.setInt(1, appartementId);
                ResultSet capRs = capStmt.executeQuery();
                int capacite = 0;
                if (capRs.next()) {
                    capacite = capRs.getInt("capacite");
                }

                if (nbPersonnes > capacite) {
                    JOptionPane.showMessageDialog(this, "Le nombre de personnes dépasse la capacité maximale de l'appartement (" + capacite + ").", "Capacité dépassée", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Vérifier disponibilité
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

                // Insérer la réservation
                String insert = "INSERT INTO locations (client_id, appartement_id, date_debut, date_fin, nombre_personnes) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insert);
                insertStmt.setInt(1, clientId);
                insertStmt.setInt(2, appartementId);
                insertStmt.setString(3, dDebut);
                insertStmt.setString(4, dFin);
                insertStmt.setInt(5, nbPersonnes);
                insertStmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Réservation effectuée avec succès !");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

private void afficherPopupDisponibilites() {
    JDialog dialog = new JDialog(this, "Rechercher un appartement disponible", true);
    dialog.setSize(800, 500);
    dialog.setLocationRelativeTo(this);
    dialog.setLayout(new BorderLayout());

    // Panel haut : champs de saisie
    JPanel formPanel = new JPanel(new GridLayout(2, 4, 10, 10));
    formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JLabel debutLabel = new JLabel("Date de début:");
    JSpinner dateDebut = new JSpinner(new SpinnerDateModel());
    dateDebut.setEditor(new JSpinner.DateEditor(dateDebut, "dd/MM/yyyy"));

    JLabel finLabel = new JLabel("Date de fin:");
    JSpinner dateFin = new JSpinner(new SpinnerDateModel());
    dateFin.setEditor(new JSpinner.DateEditor(dateFin, "dd/MM/yyyy"));

    JLabel personnesLabel = new JLabel("Nombre de personnes:");
    JSpinner nbPersonnesSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));

    JButton rechercherButton = new JButton("Rechercher");

    formPanel.add(debutLabel);
    formPanel.add(dateDebut);
    formPanel.add(finLabel);
    formPanel.add(dateFin);
    formPanel.add(personnesLabel);
    formPanel.add(nbPersonnesSpinner);
    formPanel.add(new JLabel()); // vide
    formPanel.add(rechercherButton);

    dialog.add(formPanel, BorderLayout.NORTH);

    // Tableau des résultats
    String[] columnNames = {"ID", "Nom", "Adresse", "Ville", "Type", "Capacité", "Prix/nuit", "Disponible"};
    DefaultTableModel popupModel = new DefaultTableModel(columnNames, 0);
    JTable resultsTable = new JTable(popupModel);
    JScrollPane scrollPane = new JScrollPane(resultsTable);
    dialog.add(scrollPane, BorderLayout.CENTER);

    // Action du bouton rechercher
    rechercherButton.addActionListener(e -> {
        popupModel.setRowCount(0); // vider tableau

        Date debut = (Date) dateDebut.getValue();
        Date fin = (Date) dateFin.getValue();
        int nbPersonnes = (Integer) nbPersonnesSpinner.getValue();

        if (fin.before(debut)) {
            JOptionPane.showMessageDialog(dialog, "La date de fin doit être après la date de début.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Convertir en java.sql.Date
        java.sql.Date sqlDebut = new java.sql.Date(debut.getTime());
        java.sql.Date sqlFin = new java.sql.Date(fin.getTime());

        try (Connection conn = DBConnection.getConnection()) {
            CallableStatement stmt = conn.prepareCall("{CALL GetAppartementsDisponibles(?, ?, ?)}");
            stmt.setInt(1, nbPersonnes);
            stmt.setDate(2, sqlDebut);  // ✅ Utiliser java.sql.Date ici
            stmt.setDate(3, sqlFin);    // ✅ Utiliser java.sql.Date ici
            System.out.println("CALL GetAppartementsDisponibles(" + nbPersonnes + ", '" + sqlDebut + "', '" + sqlFin + "')");

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                popupModel.addRow(new Object[]{
                    rs.getInt("appartement_id"),
                    rs.getString("nom"),
                    rs.getString("adresse"),
                    rs.getString("ville"),
                    rs.getString("type_appartement"),
                    rs.getInt("capacite"),
                    rs.getDouble("prix_par_nuit") + " €",
                    rs.getInt("disponibilite") == 1 ? "Oui" : "Non"
                });
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(dialog, "Erreur lors de l'accès à la base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    });


    dialog.setVisible(true);
}




    // Modern button renderer for table
    class ModernButtonRenderer extends JButton implements TableCellRenderer {
        public ModernButtonRenderer() {
            setOpaque(true);
            setForeground(Color.WHITE);
            setBackground(primaryColor);
            setFocusPainted(false);
            setBorderPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText("Réserver");
            return this;
        }
    }

    // Modern button editor for table
    class ModernButtonEditor extends DefaultCellEditor {
        private JButton button;
        private int selectedRow;

        public ModernButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setForeground(Color.WHITE);
            button.setBackground(primaryColor);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setFont(new Font("Segoe UI", Font.BOLD, 12));
            
            button.addActionListener(e -> {
                int appartementId = (int) model.getValueAt(selectedRow, 0);
                faireReservation(appartementId);
                fireEditingStopped();
            });
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            selectedRow = row;
            button.setText("Réserver");
            return button;
        }

        public Object getCellEditorValue() {
            return "Réserver";
        }
    }
}
