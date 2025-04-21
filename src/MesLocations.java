import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MesLocations extends JFrame {
    private int clientId;

    public MesLocations(int clientId) {
        this.clientId = clientId;

        setTitle("Mes Locations");
        setSize(400, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel label = new JLabel("Affichage des locations du client ID : " + clientId, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.PLAIN, 16));
        add(label, BorderLayout.CENTER);

        JButton backButton = new JButton("Retour au Dashboard");
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose(); // Fermer cette fenêtre
                new DashboardClient(clientId); // Ouvrir le dashboard du client
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }
}
