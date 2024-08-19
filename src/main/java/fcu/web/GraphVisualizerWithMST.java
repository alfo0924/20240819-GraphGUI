package fcu.web;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.PriorityQueue;
import java.util.Random;

public class GraphVisualizerWithMST extends JFrame {
    private JTextField vertexField, edgeField;
    private JTextArea outputArea;
    private JPanel graphPanel;
    private int vertexCount, edgeCount;
    private int[][] graph;
    private boolean[][] mstEdges;

    public GraphVisualizerWithMST() {
        setTitle("Graph Visualizer with MST");
        setSize(900, 600);
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

        JButton mstButton = new JButton("Show MST");
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
                drawGraph(g);
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
                    computeMST();
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
        outputArea.setText("");
        outputArea.append("Graph Details:\n");

        for (int i = 0; i < edgeCount; i++) {
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
    }

    private void drawGraph(Graphics g) {
        if (graph == null) return;

        int panelWidth = graphPanel.getWidth();
        int panelHeight = graphPanel.getHeight();
        int radius = Math.min(panelWidth, panelHeight) / 3;
        int centerX = panelWidth / 2;
        int centerY = panelHeight / 2;

        Point[] points = new Point[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            double angle = 2 * Math.PI * i / vertexCount;
            int x = centerX + (int) (radius * Math.cos(angle));
            int y = centerY + (int) (radius * Math.sin(angle));
            points[i] = new Point(x, y);
            g.fillOval(x - 10, y - 10, 20, 20);
            g.drawString("v" + i, x - 15, y - 15);
        }

        for (int i = 0; i < vertexCount; i++) {
            for (int j = i + 1; j < vertexCount; j++) {
                if (graph[i][j] != 0) {
                    if (mstEdges != null && mstEdges[i][j]) {
                        g.setColor(Color.RED); // Highlight MST edges
                    } else {
                        g.setColor(Color.BLACK);
                    }
                    g.drawLine(points[i].x, points[i].y, points[j].x, points[j].y);
                    int midX = (points[i].x + points[j].x) / 2;
                    int midY = (points[i].y + points[j].y) / 2;
                    g.drawString(String.valueOf(graph[i][j]), midX, midY);
                }
            }
        }
    }

    private void computeMST() {
        mstEdges = new boolean[vertexCount][vertexCount];
        boolean[] inMST = new boolean[vertexCount];
        PriorityQueue<Edge> pq = new PriorityQueue<>((e1, e2) -> Integer.compare(e1.cost, e2.cost));
        inMST[0] = true;

        for (int i = 1; i < vertexCount; i++) {
            if (graph[0][i] != 0) {
                pq.offer(new Edge(0, i, graph[0][i]));
            }
        }

        outputArea.append("\nMinimum Spanning Tree Edges:\n");

        while (!pq.isEmpty()) {
            Edge edge = pq.poll();
            if (inMST[edge.v2]) continue;

            inMST[edge.v2] = true;
            mstEdges[edge.v1][edge.v2] = true;
            mstEdges[edge.v2][edge.v1] = true;
            outputArea.append("Edge: v" + edge.v1 + " - v" + edge.v2 + " Cost: " + edge.cost + "\n");

            for (int i = 0; i < vertexCount; i++) {
                if (!inMST[i] && graph[edge.v2][i] != 0) {
                    pq.offer(new Edge(edge.v2, i, graph[edge.v2][i]));
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GraphVisualizerWithMST graphVisualizer = new GraphVisualizerWithMST();
            graphVisualizer.setVisible(true);
        });
    }

    class Edge {
        int v1, v2, cost;

        public Edge(int v1, int v2, int cost) {
            this.v1 = v1;
            this.v2 = v2;
            this.cost = cost;
        }
    }
}
