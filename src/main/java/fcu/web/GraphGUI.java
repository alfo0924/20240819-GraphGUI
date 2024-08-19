package fcu.web;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

public class GraphGUI extends JFrame {
    private int vertexCount;
    private int edgeCount;
    private java.util.List<Edge> edges;
    private JPanel graphPanel;
    private JTextArea outputArea;
    private JTextField vertexField;
    private JTextField edgeField;
    private Map<Integer, Point> vertexPositions;
    private List<Edge> mstEdges;

    public GraphGUI() {
        setTitle("Graph and MST Visualizer");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        vertexField = new JTextField(5);
        edgeField = new JTextField(5);
        JButton generateButton = new JButton("Generate Graph");
        JButton mstButton = new JButton("Show MST");

        controlPanel.add(new JLabel("Vertices:"));
        controlPanel.add(vertexField);
        controlPanel.add(new JLabel("Edges:"));
        controlPanel.add(edgeField);
        controlPanel.add(generateButton);
        controlPanel.add(mstButton);

        add(controlPanel, BorderLayout.NORTH);

        graphPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (edges != null) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setStroke(new BasicStroke(2));

                    // Draw all edges
                    for (Edge edge : edges) {
                        Point p1 = vertexPositions.get(edge.v1);
                        Point p2 = vertexPositions.get(edge.v2);
                        g2.setColor(Color.LIGHT_GRAY);
                        g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                        g2.setColor(Color.BLACK);
                        g2.drawString(String.valueOf(edge.cost), (p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
                    }

                    // Highlight MST edges
                    if (mstEdges != null) {
                        g2.setColor(Color.RED);
                        for (Edge edge : mstEdges) {
                            Point p1 = vertexPositions.get(edge.v1);
                            Point p2 = vertexPositions.get(edge.v2);
                            g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                        }
                    }

                    // Draw vertices
                    for (Map.Entry<Integer, Point> entry : vertexPositions.entrySet()) {
                        Point p = entry.getValue();
                        g2.setColor(Color.BLUE);
                        g2.fillOval(p.x - 5, p.y - 5, 10, 10);
                        g2.setColor(Color.BLACK);
                        g2.drawString("V" + entry.getKey(), p.x - 15, p.y - 10);
                    }
                }
            }
        };
        add(graphPanel, BorderLayout.CENTER);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        add(new JScrollPane(outputArea), BorderLayout.SOUTH);

        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateGraph();
            }
        });

        mstButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showMST();
            }
        });
    }

    private void generateGraph() {
        try {
            vertexCount = Integer.parseInt(vertexField.getText());
            edgeCount = Integer.parseInt(edgeField.getText());

            if (vertexCount < 2 || edgeCount < 1 || edgeCount > vertexCount * (vertexCount - 1) / 2) {
                JOptionPane.showMessageDialog(this, "Invalid input values.");
                return;
            }

            edges = new ArrayList<>();
            vertexPositions = new HashMap<>();
            Random rand = new Random();
            Set<String> existingEdges = new HashSet<>();

            // Generate random positions for vertices
            for (int i = 0; i < vertexCount; i++) {
                int x = rand.nextInt(graphPanel.getWidth() - 50) + 25;
                int y = rand.nextInt(graphPanel.getHeight() - 50) + 25;
                vertexPositions.put(i, new Point(x, y));
            }

            // Generate random edges
            for (int i = 0; i < edgeCount; i++) {
                int v1 = rand.nextInt(vertexCount);
                int v2 = rand.nextInt(vertexCount);
                if (v1 == v2 || existingEdges.contains(v1 + "-" + v2) || existingEdges.contains(v2 + "-" + v1)) {
                    i--;
                    continue;
                }
                int cost = rand.nextInt(99) + 1;
                edges.add(new Edge(v1, v2, cost));
                existingEdges.add(v1 + "-" + v2);
            }

            outputArea.setText("Generated Graph:\n");
            for (Edge edge : edges) {
                outputArea.append("Edge: " + edge.v1 + " - " + edge.v2 + " Cost: " + edge.cost + "\n");
            }

            mstEdges = null; // Clear MST edges when generating a new graph
            graphPanel.repaint();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers.");
        }
    }

    private void showMST() {
        if (edges == null || edges.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please generate a graph first.");
            return;
        }

        Collections.sort(edges, Comparator.comparingInt(e -> e.cost));

        int[] parent = new int[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            parent[i] = i;
        }

        mstEdges = new ArrayList<>();
        for (Edge edge : edges) {
            int root1 = find(parent, edge.v1);
            int root2 = find(parent, edge.v2);

            if (root1 != root2) {
                mstEdges.add(edge);
                parent[root1] = root2;
            }
        }

        outputArea.append("\nMinimum Spanning Tree:\n");
        for (Edge edge : mstEdges) {
            outputArea.append("Edge: " + edge.v1 + " - " + edge.v2 + " Cost: " + edge.cost + "\n");
        }

        graphPanel.repaint();
    }

    private int find(int[] parent, int vertex) {
        if (parent[vertex] != vertex) {
            parent[vertex] = find(parent, parent[vertex]);
        }
        return parent[vertex];
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GraphGUI gui = new GraphGUI();
            gui.setVisible(true);
        });
    }

    class Edge {
        int v1, v2, cost;

        Edge(int v1, int v2, int cost) {
            this.v1 = v1;
            this.v2 = v2;
            this.cost = cost;
        }
    }
}