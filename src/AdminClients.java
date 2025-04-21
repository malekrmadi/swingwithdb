import javax.swing.*;

public class AdminClients extends JFrame {
    public AdminClients(boolean isSuperAdmin) {
        setTitle("Liste des Clients");
        setSize(400, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        add(new JLabel("Interface de gestion des clients", SwingConstants.CENTER), "Center");

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
