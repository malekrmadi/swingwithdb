import javax.swing.*;

public class AdminLocations extends JFrame {
    public AdminLocations(boolean isSuperAdmin) {
        setTitle("Gestion des Locations");
        setSize(400, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        add(new JLabel("Interface de gestion des locations", SwingConstants.CENTER), "Center");

        JButton backButton = new JButton("Retour au Dashboard");
        backButton.addActionListener(e -> {
            dispose();
            if (isSuperAdmin) {
                new DashboardSuperAdmin();
            } else {
                new DashboardAdmin();
            }
        });

        add(backButton, "South");
        setVisible(true);
    }
}
