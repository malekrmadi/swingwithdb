import javax.swing.*;

public class DashboardSuperAdmin extends JFrame {
    public DashboardSuperAdmin() {
        setTitle("Dashboard Super Admin");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(new JLabel("Bienvenue Super Admin", SwingConstants.CENTER));
        setVisible(true);
    }
}
