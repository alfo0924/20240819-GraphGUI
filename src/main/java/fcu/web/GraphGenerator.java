import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

public class GraphGenerator extends JFrame {
    private JTextField vertexInput;
    private JTextField edgeInput;
    private JTextField startVertexInput;
    private JTextField endVertexInput;
    private JButton startButton;
    private JButton mstButton;
    private JButton apButton;
    private JButton spButton;
    private JTextArea infoArea;
    private GraphPanel graphPanel;
    private java.util.List<Edge> edgeList;
    private int[] parent;
    private boolean[] articulationPoints;
    private java.util.List<Integer> shortestPath;

    public GraphGenerator() {
        setTitle("Graph Generator");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Input Panel
        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Vertices:"));
        vertexInput = new JTextField(5);
        inputPanel.add(vertexInput);
        inputPanel.add(new JLabel("Edges:"));
        edgeInput = new JTextField(5);
        inputPanel.add(edgeInput);
        startButton = new JButton("Start");
        inputPanel.add(startButton);

        // MST Button
        mstButton = new JButton("Minimum Spanning Tree");
        inputPanel.add(mstButton);
        mstButton.setEnabled(false);

        // Articulation Point Button
        apButton = new JButton("Find Articulation Points");
        inputPanel.add(apButton);
        apButton.setEnabled(false);

        // Shortest Path Input and Button
        inputPanel.add(new JLabel("Start Vertex:"));
        startVertexInput = new JTextField(5);
        inputPanel.add(startVertexInput);
        inputPanel.add(new JLabel("End Vertex:"));
        endVertexInput = new JTextField(5);
        inputPanel.add(endVertexInput);
        spButton = new JButton("Find Shortest Path");
        inputPanel.add(spButton);
        spButton.setEnabled(false);

        add(inputPanel, BorderLayout.NORTH);

        // Info Area
        infoArea = new JTextArea();
        infoArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(infoArea);
        add(scrollPane, BorderLayout.EAST);

        // Graph Panel
        graphPanel = new GraphPanel();
        add(graphPanel, BorderLayout.CENTER);

        // Button Actions
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateGraph();
                mstButton.setEnabled(true);
                apButton.setEnabled(true);
                spButton.setEnabled(true);
            }
        });

        mstButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calculateMST();
            }
        });

        apButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                findArticulationPoints();
            }
        });

        spButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                findShortestPath();
            }
        });
    }

    private void generateGraph() {
        int vertices = Integer.parseInt(vertexInput.getText());
        int edges = Integer.parseInt(edgeInput.getText());

        // Generate random graph
        Random rand = new Random();
        edgeList = new ArrayList<>();
        while (edgeList.size() < edges) {
            int v1 = rand.nextInt(vertices);
            int v2 = rand.nextInt(vertices);
            if (v1 != v2) {
                int cost = rand.nextInt(99) + 1;
                boolean exists = false;
                for (Edge edge : edgeList) {
                    if (edge.connects(v1, v2)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    edgeList.add(new Edge(v1, v2, cost));
                }
            }
        }

        StringBuilder info = new StringBuilder();
        info.append("Vertices: ").append(vertices).append("\n");
        for (Edge edge : edgeList) {
            info.append("Edge: v").append(edge.v1).append(" - v").append(edge.v2)
                    .append(" (Cost: ").append(edge.cost).append(")\n");
        }
        infoArea.setText(info.toString());

        graphPanel.setGraphData(vertices, edgeList);

        parent = new int[vertices];
        articulationPoints = new boolean[vertices];
        shortestPath = new ArrayList<>();
        for (int i = 0; i < vertices; i++) {
            parent[i] = i;
        }
    }

    private int find(int i) {
        if (parent[i] != i) {
            parent[i] = find(parent[i]);
        }
        return parent[i];
    }

    private void union(int i, int j) {
        parent[find(i)] = find(j);
    }

    private void calculateMST() {
        java.util.List<Edge> mstEdges = new ArrayList<>();
        edgeList.sort(Comparator.comparingInt(e -> e.cost));

        for (Edge edge : edgeList) {
            int root1 = find(edge.v1);
            int root2 = find(edge.v2);
            if (root1 != root2) {
                mstEdges.add(edge);
                union(root1, root2);
            }
        }

        StringBuilder info = new StringBuilder();
        info.append("Minimum Spanning Tree Edges:\n");
        for (Edge edge : mstEdges) {
            info.append("Edge: v").append(edge.v1).append(" - v").append(edge.v2)
                    .append(" (Cost: ").append(edge.cost).append(")\n");
        }
        infoArea.setText(info.toString());

        graphPanel.setMSTData(mstEdges);
    }

    private void findArticulationPoints() {
        int vertices = parent.length;
        boolean[] visited = new boolean[vertices];
        int[] discoveryTime = new int[vertices];
        int[] low = new int[vertices];
        int[] parent = new int[vertices];
        Arrays.fill(parent, -1);
        boolean[] ap = new boolean[vertices];

        // Initialize time
        int[] time = {0};

        for (int i = 0; i < vertices; i++) {
            if (!visited[i]) {
                articulationDFS(i, visited, discoveryTime, low, parent, ap, time);
            }
        }

        articulationPoints = ap;

        // Display articulation points
        StringBuilder info = new StringBuilder();
        info.append("Articulation Points:\n");
        for (int i = 0; i < vertices; i++) {
            if (ap[i]) {
                info.append("Vertex: v").append(i).append("\n");
            }
        }
        infoArea.setText(info.toString());

        graphPanel.setArticulationPoints(ap);
    }

    private void articulationDFS(int u, boolean[] visited, int[] discoveryTime, int[] low, int[] parent, boolean[] ap, int[] time) {
        int children = 0;
        visited[u] = true;
        discoveryTime[u] = low[u] = ++time[0];

        for (Edge edge : edgeList) {
            int v = -1;
            if (edge.v1 == u) {
                v = edge.v2;
            } else if (edge.v2 == u) {
                v = edge.v1;
            }

            if (v != -1) {
                if (!visited[v]) {
                    children++;
                    parent[v] = u;
                    articulationDFS(v, visited, discoveryTime, low, parent, ap, time);

                    low[u] = Math.min(low[u], low[v]);

                    if (parent[u] == -1 && children > 1) {
                        ap[u] = true;
                    }

                    if (parent[u] != -1 && low[v] >= discoveryTime[u]) {
                        ap[u] = true;
                    }
                } else if (v != parent[u]) {
                    low[u] = Math.min(low[u], discoveryTime[v]);
                }
            }
        }
    }

    private void findShortestPath() {
        int startVertex = Integer.parseInt(startVertexInput.getText());
        int endVertex = Integer.parseInt(endVertexInput.getText());
        int vertices = parent.length;

        int[] dist = new int[vertices];
        int[] prev = new int[vertices];
        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(prev, -1);

        dist[startVertex] = 0;

        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingInt(node -> node.cost));
        queue.add(new Node(startVertex, 0));

        while (!queue.isEmpty()) {
            Node currentNode = queue.poll();
            int u = currentNode.vertex;

            for (Edge edge : edgeList) {
                int v = -1;
                if (edge.v1 == u) {
                    v = edge.v2;
                } else if (edge.v2 == u) {
                    v = edge.v1;
                }

                if (v != -1 && dist[u] + edge.cost < dist[v]) {
                    dist[v] = dist[u] + edge.cost;
                    prev[v] = u;
                    queue.add(new Node(v, dist[v]));
                }
            }
        }

        shortestPath = new ArrayList<>();
        for (int at = endVertex; at != -1; at = prev[at]) {
            shortestPath.add(at);
        }
        Collections.reverse(shortestPath);

        StringBuilder info = new StringBuilder();
        info.append("Shortest Path from v").append(startVertex).append(" to v").append(endVertex).append(":\n");
        for (int vertex : shortestPath) {
            info.append("v").append(vertex).append(" ");
        }
        info.append("\nTotal Cost: ").append(dist[endVertex]);
        infoArea.setText(info.toString());

        graphPanel.setShortestPath(shortestPath);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GraphGenerator frame = new GraphGenerator();
            frame.setVisible(true);
        });
    }
}

class GraphPanel extends JPanel {
    private int vertices;
    private java.util.List<Edge> edges;
    private java.util.List<Edge> mstEdges;
    private boolean[] articulationPoints;
    private java.util.List<Integer> shortestPath;
    private Map<Integer, Point> vertexPositions;

    public void setGraphData(int vertices, java.util.List<Edge> edges) {
        this.vertices = vertices;
        this.edges = edges;
        this.mstEdges = null;
        this.articulationPoints = null;
        this.shortestPath = null;
        calculateTreeLayout();
        repaint();
    }

    public void setMSTData(java.util.List<Edge> mstEdges) {
        this.mstEdges = mstEdges;
        repaint();
    }

    public void setArticulationPoints(boolean[] articulationPoints) {
        this.articulationPoints = articulationPoints;
        repaint();
    }

    public void setShortestPath(java.util.List<Integer> shortestPath) {
        this.shortestPath = shortestPath;
        repaint();
    }

    private void calculateTreeLayout() {
        vertexPositions = new HashMap<>();
        int levelHeight = 80;
        int levelWidth = getWidth() / (vertices + 1);

        // 使用BFS生成樹狀佈局
        Queue<Integer> queue = new LinkedList<>();
        boolean[] visited = new boolean[vertices];
        queue.offer(0); // 從頂點0開始
        visited[0] = true;

        int level = 0;
        int nodesInCurrentLevel = 1;
        int nodesInNextLevel = 0;
        int nodeCount = 0;

        while (!queue.isEmpty()) {
            int v = queue.poll();
            int x = levelWidth * (nodeCount + 1);
            int y = levelHeight * (level + 1);
            vertexPositions.put(v, new Point(x, y));

            for (Edge edge : edges) {
                int neighbor = (edge.v1 == v) ? edge.v2 : (edge.v2 == v ? edge.v1 : -1);
                if (neighbor != -1 && !visited[neighbor]) {
                    queue.offer(neighbor);
                    visited[neighbor] = true;
                    nodesInNextLevel++;
                }
            }

            nodeCount++;
            if (nodeCount == nodesInCurrentLevel) {
                level++;
                nodesInCurrentLevel = nodesInNextLevel;
                nodesInNextLevel = 0;
                nodeCount = 0;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (vertices == 0 || edges == null || vertexPositions == null) return;

        int radius = 20;

        // 繪製邊
        g.setColor(Color.BLUE);
        for (Edge edge : edges) {
            Point p1 = vertexPositions.get(edge.v1);
            Point p2 = vertexPositions.get(edge.v2);
            if (p1 != null && p2 != null) {
                g.drawLine(p1.x, p1.y, p2.x, p2.y);
                int midX = (p1.x + p2.x) / 2;
                int midY = (p1.y + p2.y) / 2;
                g.drawString(String.valueOf(edge.cost), midX, midY);
            }
        }

        // 繪製頂點
        for (int i = 0; i < vertices; i++) {
            Point p = vertexPositions.get(i);
            if (p != null) {
                g.setColor(Color.WHITE);
                g.fillOval(p.x - radius / 2, p.y - radius / 2, radius, radius);
                g.setColor(Color.BLACK);
                g.drawOval(p.x - radius / 2, p.y - radius / 2, radius, radius);
                g.drawString("v" + i, p.x - 10, p.y + 5);
            }
        }

        // 繪製最小生成樹（如果有）
        if (mstEdges != null) {
            g.setColor(Color.RED);
            for (Edge edge : mstEdges) {
                Point p1 = vertexPositions.get(edge.v1);
                Point p2 = vertexPositions.get(edge.v2);
                if (p1 != null && p2 != null) {
                    g.drawLine(p1.x, p1.y, p2.x, p2.y);
                }
            }
        }

        // 標記關節點（如果有）
        if (articulationPoints != null) {
            g.setColor(Color.GREEN);
            for (int i = 0; i < articulationPoints.length; i++) {
                if (articulationPoints[i]) {
                    Point p = vertexPositions.get(i);
                    if (p != null) {
                        g.fillOval(p.x - radius / 2, p.y - radius / 2, radius, radius);
                    }
                }
            }
        }

        // 高亮最短路徑（如果有）
        if (shortestPath != null && !shortestPath.isEmpty()) {
            g.setColor(Color.MAGENTA);
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(3));
            for (int i = 0; i < shortestPath.size() - 1; i++) {
                Point p1 = vertexPositions.get(shortestPath.get(i));
                Point p2 = vertexPositions.get(shortestPath.get(i + 1));
                if (p1 != null && p2 != null) {
                    g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                }
            }
        }
    }
}

class Edge {
    int v1, v2, cost;

    public Edge(int v1, int v2, int cost) {
        this.v1 = v1;
        this.v2 = v2;
        this.cost = cost;
    }

    public boolean connects(int v1, int v2) {
        return (this.v1 == v1 && this.v2 == v2) || (this.v1 == v2 && this.v2 == v1);
    }
}

class Node {
    int vertex, cost;

    public Node(int vertex, int cost) {
        this.vertex = vertex;
        this.cost = cost;
    }
}
