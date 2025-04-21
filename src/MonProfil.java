import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MonProfil extends JFrame {
    private int clientId;

    public MonProfil(int clientId) {
        this.clientId = clientId;

        setTitle("Mon Profil");
        setSize(400, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel label = new JLabel("Profil du client ID : " + clientId, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.PLAIN, 16));
        add(label, BorderLayout.CENTER);

        JButton backButton = new JButton("Retour au Dashboard");
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose(); // Fermer cette fenêtre
                new DashboardClient(clientId); // Revenir au Dashboard
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }
}
