import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Date;

public class AppartementsLibres extends JFrame {
    private boolean isSuperAdmin;

    public AppartementsLibres(boolean isSuperAdmin) {
        this.isSuperAdmin = isSuperAdmin;
        setTitle("Appartements Libres");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
        Color primaryColor = new Color(41, 128, 185);
        Color accentColor = new Color(39, 174, 96);
        Color backgroundColor = Color.WHITE;

        // Filtres
        JPanel formPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Rechercher des appartements disponibles"));
        formPanel.setBackground(backgroundColor);

        JLabel debutLabel = new JLabel("Date de début:");
        debutLabel.setFont(mainFont);
        JSpinner dateDebut = new JSpinner(new SpinnerDateModel());
        dateDebut.setEditor(new JSpinner.DateEditor(dateDebut, "dd/MM/yyyy"));

        JLabel finLabel = new JLabel("Date de fin:");
        finLabel.setFont(mainFont);
        JSpinner dateFin = new JSpinner(new SpinnerDateModel());
        dateFin.setEditor(new JSpinner.DateEditor(dateFin, "dd/MM/yyyy"));

        JLabel personnesLabel = new JLabel("Nombre de personnes:");
        personnesLabel.setFont(mainFont);
        JSpinner nbPersonnesSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));

        JButton rechercherButton = new JButton("Rechercher");
        rechercherButton.setBackground(accentColor);
        rechercherButton.setForeground(Color.WHITE);
        rechercherButton.setFont(mainFont);
        rechercherButton.setFocusPainted(false);

        formPanel.add(debutLabel);
        formPanel.add(dateDebut);
        formPanel.add(finLabel);
        formPanel.add(dateFin);
        formPanel.add(personnesLabel);
        formPanel.add(nbPersonnesSpinner);
        formPanel.add(new JLabel());
        formPanel.add(rechercherButton);

        add(formPanel, BorderLayout.NORTH);

        // Tableau des résultats
        String[] columnNames = {"ID", "Nom", "Adresse", "Ville", "Type", "Capacité", "Prix/nuit", "Disponible"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(tableModel);
        table.setFont(mainFont);
        table.setRowHeight(22);
        table.getTableHeader().setFont(mainFont.deriveFont(Font.BOLD));
        JScrollPane scrollPane = new JScrollPane(table);

        add(scrollPane, BorderLayout.CENTER);

        // Bouton retour
        JButton backButton = new JButton("Retour");
        backButton.setFont(mainFont);
        backButton.setBackground(primaryColor);
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> {
            dispose();
            new AdminAppartements(isSuperAdmin);
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(backgroundColor);
        bottomPanel.add(backButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // Action recherche
        rechercherButton.addActionListener(e -> {
            tableModel.setRowCount(0);

            Date debut = (Date) dateDebut.getValue();
            Date fin = (Date) dateFin.getValue();
            int nbPersonnes = (Integer) nbPersonnesSpinner.getValue();

            if (fin.before(debut)) {
                JOptionPane.showMessageDialog(this, "La date de fin doit être après la date de début.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            java.sql.Date sqlDebut = new java.sql.Date(debut.getTime());
            java.sql.Date sqlFin = new java.sql.Date(fin.getTime());

            try (Connection conn = DBConnection.getConnection()) {
                CallableStatement stmt = conn.prepareCall("{CALL GetAppartementsDisponibles(?, ?, ?)}");
                stmt.setInt(1, nbPersonnes);
                stmt.setDate(2, sqlDebut);
                stmt.setDate(3, sqlFin);

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
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
                JOptionPane.showMessageDialog(this, "Erreur lors de l'accès à la base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        setVisible(true);
    }
}
