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
    private Color primaryColor = new Color(41, 128, 185); // Blue
    private Color accentColor = new Color(46, 204, 113); // Green for locations
    private Color lightColor = new Color(236, 240, 241); // Light gray
    private Color textColor = new Color(44, 62, 80); // Dark blue/gray
    private Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private Font headerFont = new Font("Segoe UI", Font.BOLD, 24);

    public MesLocations(int clientId) {
        this.clientId = clientId;

        setTitle("Mes Locations");
        setSize(800, 600);
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

        // Table pour afficher les données avec style moderne
        String[] columnNames = {"ID", "Appartement", "Date Début", "Date Fin", "Personnes", "Pénalité", "Statut"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make the table non-editable
            }
        };
        
        JTable table = new JTable(model);
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
        table.getColumnModel().getColumn(4).setMaxWidth(80); // Personnes column
        table.getColumnModel().getColumn(5).setMaxWidth(80); // Pénalité column
        
        // Custom cell renderer for date and status
        TableCellRenderer dateRenderer = new DefaultTableCellRenderer() {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Date) {
                    setText(formatter.format((Date) value));
                }
                return c;
            }
        };
        
        TableCellRenderer statusRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (value != null) {
                    String status = value.toString();
                    if (status.equals("En cours")) {
                        setForeground(new Color(39, 174, 96)); // Green for active
                    } else if (status.equals("Terminée")) {
                        setForeground(new Color(149, 165, 166)); // Gray for past
                    } else if (status.equals("À venir")) {
                        setForeground(new Color(52, 152, 219)); // Blue for upcoming
                    }
                }
                
                return c;
            }
        };
        
        table.getColumnModel().getColumn(2).setCellRenderer(dateRenderer); // Date début
        table.getColumnModel().getColumn(3).setCellRenderer(dateRenderer); // Date fin
        table.getColumnModel().getColumn(6).setCellRenderer(statusRenderer); // Statut
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);

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

        // Charger les données depuis la BDD
        loadLocations(model);

        setVisible(true);
    }

    private void loadLocations(DefaultTableModel model) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT l.location_id, a.nom AS appartement, l.date_debut, l.date_fin, l.nombre_personnes, l.penalite_retard " +
                         "FROM locations l JOIN appartements a ON l.appartement_id = a.appartement_id " +
                         "WHERE l.client_id = ? ORDER BY l.date_debut DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date currentDate = new Date();

            while (rs.next()) {
                int id = rs.getInt("location_id");
                String nomAppartement = rs.getString("appartement");
                Date debut = rs.getDate("date_debut");
                Date fin = rs.getDate("date_fin");
                int personnes = rs.getInt("nombre_personnes");
                double penalite = rs.getDouble("penalite_retard");
                
                // Déterminer le statut de la location
                String status;
                if (currentDate.before(debut)) {
                    status = "À venir";
                } else if (currentDate.after(fin)) {
                    status = "Terminée";
                } else {
                    status = "En cours";
                }

                // Format montant de pénalité
                String penaliteFormatted = penalite > 0 ? penalite + " €" : "-";

                model.addRow(new Object[]{id, nomAppartement, debut, fin, personnes, penaliteFormatted, status});
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des locations : " + e.getMessage(), 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}
