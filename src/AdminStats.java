import javax.swing.*;

public class AdminStats extends JFrame {
    public AdminStats(boolean isSuperAdmin) {
        setTitle("Statistiques");
        setSize(400, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        add(new JLabel("Interface des statistiques", SwingConstants.CENTER), "Center");

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
