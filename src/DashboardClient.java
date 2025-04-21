import javax.swing.*;

public class DashboardClient extends JFrame {
    public DashboardClient() {
        setTitle("Dashboard Client");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(new JLabel("Bienvenue Client", SwingConstants.CENTER));
        setVisible(true);
    }
}
