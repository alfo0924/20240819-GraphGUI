package fcu.web;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

public class GraphVisualizerWithMSTAndShortestPath extends JFrame {

    private JTextField vertexField, edgeField;
    private JTextArea outputArea;
    private JPanel graphPanel;
    private int vertexCount, edgeCount;
    private int[][] graph;
    private boolean[][] mstEdges;
    private Point[] points;
    private JTextField pointAField, pointBField;
    private int startVertex, endVertex;
    private boolean[][] shortestPathEdges;
    private Set<Integer> criticalPoints;

    public GraphVisualizerWithMSTAndShortestPath() {
        setTitle("Graph Visualizer with MST, Shortest Path, and Critical Points");
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

        inputPanel.add(new JLabel("Point A:"));
        pointAField = new JTextField(5);
        inputPanel.add(pointAField);

        inputPanel.add(new JLabel("Point B:"));
        pointBField = new JTextField(5);
        inputPanel.add(pointBField);

        JButton shortestPathButton = new JButton("Show Shortest Path");
        inputPanel.add(shortestPathButton);

        JButton criticalPointsButton = new JButton("Find Critical Points");
        inputPanel.add(criticalPointsButton);

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
                    shortestPathEdges = null;
                    criticalPoints = null;
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

        shortestPathButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (graph != null) {
                    try {
                        startVertex = Integer.parseInt(pointAField.getText());
                        endVertex = Integer.parseInt(pointBField.getText());
                        if (startVertex >= 0 && startVertex < vertexCount && endVertex >= 0 && endVertex < vertexCount) {
                            computeShortestPath();
                            repaint();
                        } else {
                            outputArea.append("Invalid vertices. Please enter valid vertex indices.\n");
                        }
                    } catch (NumberFormatException ex) {
                        outputArea.append("Invalid input. Please enter valid numbers.\n");
                    }
                } else {
                    outputArea.append("Please generate a graph first.\n");
                }
            }
        });

        criticalPointsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (graph != null) {
                    findCriticalPoints();
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

        // Generate random positions for vertices
        int panelWidth = graphPanel.getWidth();
        int panelHeight = graphPanel.getHeight();
        for (int i = 0; i < vertexCount; i++) {
            int x = random.nextInt(panelWidth - 40) + 20;
            int y = random.nextInt(panelHeight - 40) + 20;
            points[i] = new Point(x, y);
        }
    }

    private void drawGraph(Graphics g) {
        if (graph == null) return;

        Graphics2D g2d = (Graphics2D) g; // Use Graphics2D for better control

        // Draw edges
        for (int i = 0; i < vertexCount; i++) {
            for (int j = i + 1; j < vertexCount; j++) {
                if (graph[i][j] != 0) {
                    if (shortestPathEdges != null && shortestPathEdges[i][j]) {
                        g2d.setColor(Color.GREEN);
                        g2d.setStroke(new BasicStroke(3));  // Bold for shortest path
                    } else if (mstEdges != null && mstEdges[i][j]) {
                        g2d.setColor(Color.RED);
                        g2d.setStroke(new BasicStroke(4));  // Bold for MST
                    } else {
                        g2d.setColor(Color.BLACK);
                        g2d.setStroke(new BasicStroke(1));  // Regular for other edges
                    }
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
            if (criticalPoints != null && criticalPoints.contains(i)) {
                g2d.setColor(Color.ORANGE);  // Highlight critical points
            } else {
                g2d.setColor(Color.BLUE);  // Normal vertices
            }
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

        outputArea.append("\nSpanning Tree Edges:\n");
        while (!pq.isEmpty() && pq.size() < vertexCount) {
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

    private void computeShortestPath() {
        int[] dist = new int[vertexCount];
        int[] prev = new int[vertexCount];
        boolean[] visited = new boolean[vertexCount];
        PriorityQueue<Edge> pq = new PriorityQueue<>((e1, e2) -> Integer.compare(e1.cost, e2.cost));

        for (int i = 0; i < vertexCount; i++) {
            dist[i] = Integer.MAX_VALUE;
            prev[i] = -1;
        }

        dist[startVertex] = 0;
        pq.offer(new Edge(-1, startVertex, 0));

        while (!pq.isEmpty()) {
            Edge edge = pq.poll();
            int u = edge.v2;
            if (visited[u]) continue;
            visited[u] = true;

            for (int v = 0; v < vertexCount; v++) {
                if (graph[u][v] != 0 && !visited[v]) {
                    int newDist = dist[u] + graph[u][v];
                    if (newDist < dist[v]) {
                        dist[v] = newDist;
                        prev[v] = u;
                        pq.offer(new Edge(u, v, newDist));
                    }
                }
            }
        }

        // Trace the shortest path
        shortestPathEdges = new boolean[vertexCount][vertexCount];
        for (int at = endVertex; at != -1; at = prev[at]) {
            if (prev[at] != -1) {
                shortestPathEdges[at][prev[at]] = true;
                shortestPathEdges[prev[at]][at] = true;
            }
        }

        outputArea.append("\nShortest Path from v" + startVertex + " to v" + endVertex + ":\n");
        int at = endVertex;
        while (at != -1) {
            if (prev[at] != -1) {
                outputArea.append("Edge: v" + prev[at] + " - v" + at + " Cost: " + graph[prev[at]][at] + "\n");
            }
            at = prev[at];
        }
    }

    private void findCriticalPoints() {
        criticalPoints = new HashSet<>();
        boolean[] visited = new boolean[vertexCount];
        int[] disc = new int[vertexCount];
        int[] low = new int[vertexCount];
        int[] parent = new int[vertexCount];
        int time = 0;

        for (int i = 0; i < vertexCount; i++) {
            parent[i] = -1;
        }

        for (int i = 0; i < vertexCount; i++) {
            if (!visited[i]) {
                dfsArticulationPoint(i, visited, disc, low, parent, time);
            }
        }

        outputArea.append("\nCritical Points:\n");
        for (int point : criticalPoints) {
            outputArea.append("v" + point + "\n");
        }
    }

    private void dfsArticulationPoint(int u, boolean[] visited, int[] disc, int[] low, int[] parent, int time) {
        int children = 0;
        visited[u] = true;
        disc[u] = low[u] = ++time;

        for (int v = 0; v < vertexCount; v++) {
            if (graph[u][v] != 0) {
                if (!visited[v]) {
                    children++;
                    parent[v] = u;
                    dfsArticulationPoint(v, visited, disc, low, parent, time);

                    low[u] = Math.min(low[u], low[v]);

                    if (parent[u] == -1 && children > 1) {
                        criticalPoints.add(u);
                    }

                    if (parent[u] != -1 && low[v] >= disc[u]) {
                        criticalPoints.add(u);
                    }
                } else if (v != parent[u]) {
                    low[u] = Math.min(low[u], disc[v]);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GraphVisualizerWithMSTAndShortestPath graphVisualizer = new GraphVisualizerWithMSTAndShortestPath();
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