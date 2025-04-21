import javax.swing.*;

public class DashboardAdmin extends JFrame {
    public DashboardAdmin() {
        setTitle("Dashboard Admin");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(new JLabel("Bienvenue Admin", SwingConstants.CENTER));
        setVisible(true);
    }
}
