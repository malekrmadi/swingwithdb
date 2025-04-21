import javax.swing.*;

public class AdminAdmins extends JFrame {
    public AdminAdmins() {
        setTitle("Gestion des Admins");
        setSize(400, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Affichage d'un message statique pour l'instant
        add(new JLabel("Interface de gestion des Admins", SwingConstants.CENTER), "Center");

        JButton backButton = new JButton("Retour au Dashboard");
        backButton.addActionListener(e -> {
            dispose();
            new DashboardSuperAdmin(); // Retour au Dashboard Super Admin
        });

        add(backButton, "South");
        setVisible(true);
    }
}
