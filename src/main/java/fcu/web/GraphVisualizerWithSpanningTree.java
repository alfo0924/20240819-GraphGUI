package fcu.web;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

public class GraphVisualizerWithSpanningTree extends JFrame {

    private JTextField vertexField, edgeField;
    private JTextArea outputArea;
    private JPanel graphPanel;
    private int vertexCount, edgeCount;
    private int[][] graph;
    private boolean[][] mstEdges;
    private Point[] points;

    public GraphVisualizerWithSpanningTree() {
        setTitle("Graph Visualizer with Spanning Tree");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Top Panel for Inputs
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());
        inputPanel.add(new JLabel("Number of Vertices:"));
        vertexField = new JTextField(5);
        inputPanel.add(vertexField);
        inputPanel.add(new JLabel("Number of Edges:"));
        edgeField = new JTextField(5);
        inputPanel.add(edgeField);

        JButton startButton = new JButton("Start");
        inputPanel.add(startButton);

        JButton mstButton = new JButton("Show Spanning Tree");
        inputPanel.add(mstButton);

        add(inputPanel, BorderLayout.NORTH);

        // Output Panel
        outputArea = new JTextArea(10, 30);
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        add(scrollPane, BorderLayout.EAST);

        // Graph Panel
        graphPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawSpanningTree(g);
            }
        };
        add(graphPanel, BorderLayout.CENTER);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    vertexCount = Integer.parseInt(vertexField.getText());
                    edgeCount = Integer.parseInt(edgeField.getText());
                    if (vertexCount <= 0 || edgeCount <= 0) {
                        outputArea.append("Number of vertices and edges must be greater than zero.\n");
                        return;
                    }
                    generateGraph();
                    mstEdges = null;
                    repaint();
                } catch (NumberFormatException ex) {
                    outputArea.append("Invalid input. Please enter valid numbers.\n");
                }
            }
        });

        mstButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (graph != null) {
                    computeSpanningTree();
                    repaint();
                } else {
                    outputArea.append("Please generate a graph first.\n");
                }
            }
        });
    }

    private void generateGraph() {
        Random random = new Random();
        graph = new int[vertexCount][vertexCount];
        points = new Point[vertexCount];
        outputArea.setText("");
        outputArea.append("Graph Details:\n");

        // Generate a connected graph
        for (int i = 1; i < vertexCount; i++) {
            int connectedTo = random.nextInt(i);
            int cost = random.nextInt(99) + 1;
            graph[i][connectedTo] = cost;
            graph[connectedTo][i] = cost;
            outputArea.append("Edge: v" + i + " - v" + connectedTo + " Cost: " + cost + "\n");
        }

        // Add remaining edges
        for (int i = 0; i < edgeCount - (vertexCount - 1); i++) {
            int v1 = random.nextInt(vertexCount);
            int v2 = random.nextInt(vertexCount);
            if (v1 != v2 && graph[v1][v2] == 0) {
                int cost = random.nextInt(99) + 1;
                graph[v1][v2] = cost;
                graph[v2][v1] = cost;
                outputArea.append("Edge: v" + v1 + " - v" + v2 + " Cost: " + cost + "\n");
            } else {
                i--; // Retry if the edge is not valid (self-loop or duplicate)
            }
        }

        // Generate random positions for vertices, without circular layout
        int panelWidth = graphPanel.getWidth();
        int panelHeight = graphPanel.getHeight();
        int xSpacing = panelWidth / (vertexCount + 1);
        int ySpacing = panelHeight / (vertexCount + 1);
        for (int i = 0; i < vertexCount; i++) {
            points[i] = new Point((i + 1) * xSpacing, (i + 1) * ySpacing);
        }
    }

    private void drawSpanningTree(Graphics g) {
        if (graph == null || mstEdges == null) return;

        Graphics2D g2d = (Graphics2D) g; // Use Graphics2D for better control

        // Draw spanning tree edges
        for (int i = 0; i < vertexCount; i++) {
            for (int j = i + 1; j < vertexCount; j++) {
                if (mstEdges[i][j]) {
                    g2d.setColor(Color.RED);
                    g2d.setStroke(new BasicStroke(4));  // Bold for MST edges
                    g2d.drawLine(points[i].x, points[i].y, points[j].x, points[j].y);

                    // Draw the cost label in the middle of the edge
                    int midX = (points[i].x + points[j].x) / 2;
                    int midY = (points[i].y + points[j].y) / 2;
                    g2d.setColor(Color.BLACK);
                    g2d.drawString(String.valueOf(graph[i][j]), midX, midY);
                }
            }
        }

        // Draw vertices
        for (int i = 0; i < vertexCount; i++) {
            g2d.setColor(Color.BLUE);  // Normal vertices
            g2d.fillOval(points[i].x - 10, points[i].y - 10, 20, 20);
            g2d.setColor(Color.WHITE);
            g2d.drawString("v" + i, points[i].x - 5, points[i].y + 5);
        }
    }

    private void computeSpanningTree() {
        mstEdges = new boolean[vertexCount][vertexCount];
        boolean[] inMST = new boolean[vertexCount];
        PriorityQueue<Edge> pq = new PriorityQueue<>((e1, e2) -> Integer.compare(e1.cost, e2.cost));

        inMST[0] = true;
        for (int i = 1; i < vertexCount; i++) {
            if (graph[0][i] != 0) {
                pq.offer(new Edge(0, i, graph[0][i]));
            }
        }

        while (!pq.isEmpty()) {
            Edge edge = pq.poll();
            if (!inMST[edge.to]) {
                mstEdges[edge.from][edge.to] = mstEdges[edge.to][edge.from] = true;
                inMST[edge.to] = true;

                for (int i = 0; i < vertexCount; i++) {
                    if (!inMST[i] && graph[edge.to][i] != 0) {
                        pq.offer(new Edge(edge.to, i, graph[edge.to][i]));
                    }
                }
            }
        }
        outputArea.append("Minimum Spanning Tree generated.\n");
    }

    private static class Edge {
        int from, to, cost;

        public Edge(int from, int to, int cost) {
            this.from = from;
            this.to = to;
            this.cost = cost;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GraphVisualizerWithSpanningTree().setVisible(true);
            }
        });
    }
}
