import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.text.NumberFormat;
import java.util.Locale;

public class AdminStats extends JFrame {
    private Color primaryColor = new Color(41, 128, 185); // Blue
    private Color accentColor = new Color(230, 126, 34); // Orange for stats
    private Color lightColor = new Color(236, 240, 241); // Light gray
    private Color textColor = new Color(44, 62, 80); // Dark blue/gray
    private Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private Font headerFont = new Font("Segoe UI", Font.BOLD, 24);

    public AdminStats(boolean isSuperAdmin) {
        setTitle("Statistiques");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(accentColor);
        headerPanel.setPreferredSize(new Dimension(800, 70));
        headerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 15));
        
        JLabel titleLabel = new JLabel("Tableau de Bord Statistiques");
        titleLabel.setFont(headerFont);
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        add(headerPanel, BorderLayout.NORTH);

        // Main content panel
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabs.setBackground(Color.WHITE);
        tabs.setForeground(textColor);
        
        tabs.add("apparetment les plus demandées", new TopAppartmentsPanel(primaryColor, lightColor, textColor, mainFont));
        tabs.add("Meilleurs Clients", new TopClientsPanel(primaryColor, lightColor, textColor, mainFont));
        tabs.add("Occupation par Type", new ApartmentOccupancyPanel(primaryColor, lightColor, textColor, mainFont));

        add(tabs, BorderLayout.CENTER);

        // Footer panel
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(lightColor);
        footerPanel.setPreferredSize(new Dimension(800, 60));
        footerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
        
        JButton backButton = new JButton("Retour au Dashboard");
        backButton.setFont(mainFont);
        backButton.setBackground(primaryColor);
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        backButton.addActionListener(e -> {
            dispose();
            if (isSuperAdmin) new DashboardSuperAdmin();
            else new DashboardAdmin();
        });
        
        footerPanel.add(backButton);
        add(footerPanel, BorderLayout.SOUTH);
        
        setVisible(true);
    }
}

class TopAppartmentsPanel extends JPanel {
    private Map<String, Integer> data = new LinkedHashMap<>();
    private JComboBox<String> cityFilter;
    private Color backgroundColor;
    private Color barColor;
    private Color barBorderColor;
    private Color textColor;
    private Font mainFont;

    public TopAppartmentsPanel(Color primaryColor, Color lightColor, Color textColor, Font mainFont) {
        this.backgroundColor = lightColor;
        this.barColor = primaryColor;
        this.barBorderColor = primaryColor.darker();
        this.textColor = textColor;
        this.mainFont = mainFont;

        setLayout(new BorderLayout());
        setBackground(backgroundColor);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        cityFilter = new JComboBox<>();
        cityFilter.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cityFilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fetchData((String) cityFilter.getSelectedItem());
                repaint();
            }
        });

        add(cityFilter, BorderLayout.NORTH);
        fetchCities();
        fetchData((String) cityFilter.getSelectedItem());
    }

    private void fetchCities() {
        String query = "SELECT DISTINCT ville FROM appartements";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            cityFilter.addItem("Toutes les villes");

            while (rs.next()) {
                String city = rs.getString("ville");
                cityFilter.addItem(city);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

  private void fetchData(String selectedCity) {
    data.clear();
    String query = """
        SELECT a.nom, COUNT(l.location_id) AS nb_locations
        FROM locations l
        JOIN appartements a ON l.appartement_id = a.appartement_id
    """;
    if (selectedCity != null && !selectedCity.equals("Toutes les villes")) {
        query += " WHERE a.ville = ?";
    }
    query += " GROUP BY a.appartement_id, a.nom ORDER BY nb_locations DESC LIMIT 5";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {

        if (selectedCity != null && !selectedCity.equals("Toutes les villes")) {
            stmt.setString(1, selectedCity);
        }

        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String apartmentName = rs.getString("nom"); // <-- Correct maintenant
                int numberOfRentals = rs.getInt("nb_locations");
                data.put(apartmentName, numberOfRentals);
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data.isEmpty()) {
            drawNoDataMessage(g);
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int marginLeft = 150;
        int marginRight = 50;
        int marginTop = 80;
        int marginBottom = 60;
        int chartWidth = width - marginLeft - marginRight;

        g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
        g2.setColor(textColor);
        g2.drawString("Top 5 Appartements les plus demandés", width / 2 - 180, 50);

        int max = Collections.max(data.values());
        int barHeight = 25;
        int gap = 25;
        int y = marginTop;

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            int value = entry.getValue();
            int barWidth = (int)((double)value / max * chartWidth);

            g2.setFont(mainFont);
            g2.setColor(textColor);
            g2.drawString(entry.getKey(), marginLeft - 130, y + barHeight / 2 + 5);

            g2.setColor(barColor);
            g2.fillRoundRect(marginLeft, y, barWidth, barHeight, 10, 10);

            g2.setColor(barBorderColor);
            g2.drawRoundRect(marginLeft, y, barWidth, barHeight, 10, 10);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2.drawString(value + " locations", marginLeft + 10, y + barHeight / 2 + 5);

            y += barHeight + gap;
        }
    }

    private void drawNoDataMessage(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
        g2.setColor(textColor);
        g2.drawString("Aucune donnée disponible", getWidth() / 2 - 100, getHeight() / 2);
    }
}


class TopClientsPanel extends JPanel {
    private Map<String, Integer> data = new LinkedHashMap<>();
    private Color primaryColor;
    private Color lightColor;
    private Color textColor;
    private Font mainFont;

    public TopClientsPanel(Color primaryColor, Color lightColor, Color textColor, Font mainFont) {
        this.primaryColor = primaryColor;
        this.lightColor = lightColor;
        this.textColor = textColor;
        this.mainFont = mainFont;
        
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        fetchData();
    }

    private void fetchData() {
        String query = """
            SELECT c.nom, c.prenom, COUNT(l.location_id) AS nombre_locations
            FROM locations l
            JOIN clients c ON l.client_id = c.client_id
            GROUP BY c.client_id
            ORDER BY nombre_locations DESC
            LIMIT 5
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String fullName = rs.getString("prenom") + " " + rs.getString("nom");
                data.put(fullName, rs.getInt("nombre_locations"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data.isEmpty()) {
            drawNoDataMessage(g);
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        int marginLeft = 200;
        int marginRight = 120;
        int marginTop = 80;
        int marginBottom = 60;
        int chartWidth = width - marginLeft - marginRight;
        int chartHeight = height - marginTop - marginBottom;
        
        // Draw chart title
        g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
        g2.setColor(textColor);
        g2.drawString("Top 5 des Clients (Nombre de Locations)", width / 2 - 170, 40);
        
        // Draw Y axis
        g2.setColor(textColor);
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(marginLeft, marginTop, marginLeft, height - marginBottom);
        
        // Draw X axis
        g2.drawLine(marginLeft, height - marginBottom, marginLeft + chartWidth, height - marginBottom);
        
        // Draw bars
        int max = Collections.max(data.values());
        int barHeight = 30;
        int gap = 20;
        int y = marginTop + gap;
        
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            int value = entry.getValue();
            int barWidth = (int)((double)value / max * chartWidth);
            
            // Draw client name
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2.setColor(textColor);
            g2.drawString(entry.getKey(), marginLeft - 190, y + barHeight/2 + 5);
            
            // Draw bar
            Color barColor = new Color(46, 204, 113).darker(); // Green
            g2.setColor(barColor);
            g2.fillRoundRect(marginLeft, y, barWidth, barHeight, 8, 8);
            
            // Draw darker border
            g2.setColor(barColor.darker());
            g2.drawRoundRect(marginLeft, y, barWidth, barHeight, 8, 8);
            
            // Draw value
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2.setColor(Color.WHITE);
            String valueStr = value + " locations";
            g2.drawString(valueStr, marginLeft + 10, y + barHeight/2 + 5);
            
            y += barHeight + gap;
        }
    }
    
    private void drawNoDataMessage(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
        g2.setColor(textColor);
        g2.drawString("Aucune donnée disponible", getWidth()/2 - 100, getHeight()/2);
    }
}

class ApartmentOccupancyPanel extends JPanel {
    private Map<String, Integer> data = new LinkedHashMap<>();
    private Color primaryColor;
    private Color lightColor;
    private Color textColor;
    private Font mainFont;

    public ApartmentOccupancyPanel(Color primaryColor, Color lightColor, Color textColor, Font mainFont) {
        this.primaryColor = primaryColor;
        this.lightColor = lightColor;
        this.textColor = textColor;
        this.mainFont = mainFont;
        
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        fetchData();
    }

    private void fetchData() {
        String query = """
            SELECT type_appartement, COUNT(*) AS count
            FROM locations l
            JOIN appartements a ON l.appartement_id = a.appartement_id
            GROUP BY type_appartement
            ORDER BY count DESC
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                data.put(rs.getString("type_appartement"), rs.getInt("count"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data.isEmpty()) {
            drawNoDataMessage(g);
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        
        // Draw chart title
        g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
        g2.setColor(textColor);
        g2.drawString("Répartition des Locations par Type d'Appartement", width / 2 - 200, 40);
        
        int pieSize = Math.min(width, height) - 200;
        int x = (width - pieSize) / 2;
        int y = 80;
        
        int total = data.values().stream().mapToInt(i -> i).sum();
        int startAngle = 0;

        // Define vibrant colors for the pie chart sections
        Color[] colors = {
            new Color(52, 152, 219),  // Blue
            new Color(46, 204, 113),  // Green
            new Color(155, 89, 182),  // Purple
            new Color(231, 76, 60),   // Red
            new Color(241, 196, 15),  // Yellow
            new Color(230, 126, 34),  // Orange
            new Color(26, 188, 156)   // Turquoise
        };
        
        // Calculate percentage and draw pie slices
        int i = 0;
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            int angle = (int) Math.round(entry.getValue() * 360.0 / total);
            Color sliceColor = colors[i % colors.length];
            
            // Draw the pie slice
            g2.setColor(sliceColor);
            g2.fillArc(x, y, pieSize, pieSize, startAngle, angle);
            
            // Draw outline for each slice
            g2.setColor(sliceColor.darker());
            g2.setStroke(new BasicStroke(1));
            g2.drawArc(x, y, pieSize, pieSize, startAngle, angle);
            
            startAngle += angle;
            i++;
        }
        
        // Draw the legend
        int legendX = x;
        int legendY = y + pieSize + 30;
        int legendSquareSize = 15;
        
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        i = 0;
        int legendItemsPerRow = 3;
        int legendItemWidth = width / legendItemsPerRow;
        
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            int row = i / legendItemsPerRow;
            int col = i % legendItemsPerRow;
            
            int itemX = legendX + col * legendItemWidth;
            int itemY = legendY + row * 25;
            
            // Draw color square
            g2.setColor(colors[i % colors.length]);
            g2.fillRect(itemX, itemY, legendSquareSize, legendSquareSize);
            g2.setColor(colors[i % colors.length].darker());
            g2.drawRect(itemX, itemY, legendSquareSize, legendSquareSize);
            
            // Draw type name and count
            g2.setColor(textColor);
            double percentage = entry.getValue() * 100.0 / total;
            String legendText = entry.getKey() + " (" + entry.getValue() + " - " + String.format("%.1f", percentage) + "%)";
            g2.drawString(legendText, itemX + legendSquareSize + 5, itemY + 13);
            
            i++;
        }
    }
    
    private void drawNoDataMessage(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
        g2.setColor(textColor);
        g2.drawString("Aucune donnée disponible", getWidth()/2 - 100, getHeight()/2);
    }
}


////



///