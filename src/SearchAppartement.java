import javax.swing.*;

public class SearchAppartement extends JFrame {
    public SearchAppartement() {
        setTitle("Recherche d'appartement");
        setSize(400, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        add(new JLabel("Ici, vous pouvez chercher un appartement...", SwingConstants.CENTER));

        setVisible(true);
    }
}
