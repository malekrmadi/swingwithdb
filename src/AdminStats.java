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
        
        tabs.add("Revenus Mensuels", new MonthlyRevenuePanel(primaryColor, lightColor, textColor, mainFont));
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

class MonthlyRevenuePanel extends JPanel {
    private Map<String, Double> data = new LinkedHashMap<>();
    private Color primaryColor;
    private Color lightColor;
    private Color textColor;
    private Font mainFont;
    private NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.FRANCE);

    public MonthlyRevenuePanel(Color primaryColor, Color lightColor, Color textColor, Font mainFont) {
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
            SELECT MONTH(date_debut) AS mois, SUM(DATEDIFF(date_fin, date_debut) * a.prix_par_nuit) AS revenu
            FROM locations l
            JOIN appartements a ON l.appartement_id = a.appartement_id
            GROUP BY mois
            ORDER BY mois
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int month = rs.getInt("mois");
                double revenu = rs.getDouble("revenu");
                data.put(getMonthName(month), revenu);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getMonthName(int month) {
        return new String[]{"Janv", "Fév", "Mars", "Avr", "Mai", "Juin", "Juil", "Août", "Sept", "Oct", "Nov", "Déc"}[month - 1];
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
        int marginLeft = 80;
        int marginRight = 40;
        int marginTop = 80;
        int marginBottom = 60;
        int chartWidth = width - marginLeft - marginRight;
        int chartHeight = height - marginTop - marginBottom;
        
        // Draw chart title
        g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
        g2.setColor(textColor);
        g2.drawString("Revenus Mensuels (en €)", width / 2 - 120, 40);
        
        // Draw Y axis
        g2.setColor(textColor);
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(marginLeft, marginTop, marginLeft, height - marginBottom);
        
        // Draw X axis
        g2.drawLine(marginLeft, height - marginBottom, marginLeft + chartWidth, height - marginBottom);
        
        // Draw bars
        double max = Collections.max(data.values());
        int barWidth = chartWidth / (data.size() * 2);
        int x = marginLeft + barWidth;
        
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            double value = entry.getValue();
            int barHeight = (int)(value / max * chartHeight);
            
            // Draw bar
            GradientPaint gradient = new GradientPaint(
                x, height - marginBottom - barHeight, 
                primaryColor,
                x, height - marginBottom,
                primaryColor.darker()
            );
            g2.setPaint(gradient);
            g2.fillRoundRect(x, height - marginBottom - barHeight, barWidth, barHeight, 10, 10);
            
            // Draw value
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.setColor(textColor);
            String valueStr = currencyFormatter.format(value);
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(valueStr);
            g2.drawString(valueStr, x + barWidth/2 - textWidth/2, height - marginBottom - barHeight - 5);
            
            // Draw month
            g2.setColor(textColor);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            textWidth = fm.stringWidth(entry.getKey());
            g2.drawString(entry.getKey(), x + barWidth/2 - textWidth/2, height - marginBottom + 20);
            
            x += barWidth * 2;
        }
        
        // Draw Y axis labels
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g2.setColor(textColor);
        
        int numYLabels = 5;
        for (int i = 0; i <= numYLabels; i++) {
            int y = height - marginBottom - (i * chartHeight / numYLabels);
            double value = (i * max / numYLabels);
            g2.drawLine(marginLeft - 5, y, marginLeft, y);
            String valueStr = currencyFormatter.format(value);
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(valueStr);
            g2.drawString(valueStr, marginLeft - textWidth - 10, y + 5);
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

class TopClientsPanel extends JPanel {
    private Map<String, Double> data = new LinkedHashMap<>();
    private Color primaryColor;
    private Color lightColor;
    private Color textColor;
    private Font mainFont;
    private NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.FRANCE);

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
            SELECT c.nom, c.prenom, SUM(DATEDIFF(l.date_fin, l.date_debut) * a.prix_par_nuit) AS revenu
            FROM locations l
            JOIN clients c ON l.client_id = c.client_id
            JOIN appartements a ON l.appartement_id = a.appartement_id
            GROUP BY c.client_id
            ORDER BY revenu DESC
            LIMIT 5
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String fullName = rs.getString("prenom") + " " + rs.getString("nom");
                data.put(fullName, rs.getDouble("revenu"));
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
        g2.drawString("Top 5 des Clients (Dépenses Totales)", width / 2 - 150, 40);
        
        // Draw Y axis
        g2.setColor(textColor);
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(marginLeft, marginTop, marginLeft, height - marginBottom);
        
        // Draw X axis
        g2.drawLine(marginLeft, height - marginBottom, marginLeft + chartWidth, height - marginBottom);
        
        // Draw bars
        double max = Collections.max(data.values());
        int barHeight = 30;
        int gap = 20;
        int y = marginTop + gap;
        int i = 0;
        
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            double value = entry.getValue();
            int barWidth = (int)(value / max * chartWidth);
            
            // Draw client name
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2.setColor(textColor);
            g2.drawString(entry.getKey(), marginLeft - 190, y + barHeight/2 + 5);
            
            // Draw bar
            Color barColor = new Color(52, 152, 219).darker(); // Blue
            g2.setColor(barColor);
            g2.fillRoundRect(marginLeft, y, barWidth, barHeight, 8, 8);
            
            // Draw darker border
            g2.setColor(barColor.darker());
            g2.drawRoundRect(marginLeft, y, barWidth, barHeight, 8, 8);
            
            // Draw value
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2.setColor(Color.WHITE);
            String valueStr = currencyFormatter.format(value);
            g2.drawString(valueStr, marginLeft + 10, y + barHeight/2 + 5);
            
            y += barHeight + gap;
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
