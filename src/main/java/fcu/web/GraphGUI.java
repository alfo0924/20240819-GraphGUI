package fcu.web;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class GraphGUI extends JFrame {
    private Graph graph;
    private JPanel graphPanel;
    private JButton calcMSTButton, findCriticalNodesButton, findShortestPathButton, startButton;
    private JTextField srcField, destField, vertexCountField, edgeCountField;
    private List<Graph.Edge> mst;
    private Set<Integer> criticalNodes;
    private List<Integer> shortestPath;
    private int src, dest;

    public GraphGUI() {
        graph = new Graph(0);

        setTitle("D1204433 林俊傑 Graph Algorithm Visualizer");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        graphPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGraph(g);
            }
        };
        add(graphPanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        calcMSTButton = new JButton("Calculate MST");
        findCriticalNodesButton = new JButton("Find Critical Nodes");
        findShortestPathButton = new JButton("Find Shortest Path");
        startButton = new JButton("Start");
        srcField = new JTextField(5);
        destField = new JTextField(5);
        vertexCountField = new JTextField(5);
        edgeCountField = new JTextField(5);

        controlPanel.add(new JLabel("Vertices:"));
        controlPanel.add(vertexCountField);
        controlPanel.add(new JLabel("Edges:"));
        controlPanel.add(edgeCountField);
        controlPanel.add(startButton);
        controlPanel.add(calcMSTButton);
        controlPanel.add(findCriticalNodesButton);
        controlPanel.add(new JLabel("Start:"));
        controlPanel.add(srcField);
        controlPanel.add(new JLabel("End:"));
        controlPanel.add(destField);
        controlPanel.add(findShortestPathButton);

        add(controlPanel, BorderLayout.SOUTH);

        startButton.addActionListener(e -> {
            try {
                int vertices = Integer.parseInt(vertexCountField.getText());
                int edges = Integer.parseInt(edgeCountField.getText());
                graph = new Graph(vertices);
                generateConnectedGraph(edges);
                mst = null;
                criticalNodes = null;
                shortestPath = null;
                repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers for vertices and edges");
            }
        });

        calcMSTButton.addActionListener(e -> {
            mst = graph.kruskalMST();
            criticalNodes = null;
            shortestPath = null;
            repaint();
        });

        findCriticalNodesButton.addActionListener(e -> {
            criticalNodes = graph.findCriticalNodes();
            mst = null;
            shortestPath = null;
            repaint();
        });

        findShortestPathButton.addActionListener(e -> {
            try {
                src = Integer.parseInt(srcField.getText());
                dest = Integer.parseInt(destField.getText());
                shortestPath = graph.dijkstra(src, dest);
                mst = null;
                criticalNodes = null;
                repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid node numbers");
            }
        });
    }

    private void generateConnectedGraph(int edgeCount) {
        Random random = new Random();
        List<Integer> connectedVertices = new ArrayList<>();
        connectedVertices.add(0);

        // 確保所有頂點都連接
        for (int i = 1; i < graph.vertices; i++) {
            int connectedVertex = connectedVertices.get(random.nextInt(connectedVertices.size()));
            int weight = random.nextInt(99) + 1; // 1-99
            graph.addEdge(i, connectedVertex, weight);
            connectedVertices.add(i);
            edgeCount--;
        }

        // 添加剩餘的邊
        while (edgeCount > 0 && graph.edges.size() < graph.vertices * (graph.vertices - 1) / 2) {
            int u = random.nextInt(graph.vertices);
            int v = random.nextInt(graph.vertices);
            if (u != v && !graph.hasEdge(u, v)) {
                int weight = random.nextInt(99) + 1; // 1-99
                graph.addEdge(u, v, weight);
                edgeCount--;
            }
        }
    }

    private void drawGraph(Graphics g) {
        int width = graphPanel.getWidth();
        int height = graphPanel.getHeight();
        int rows = (int) Math.ceil(Math.sqrt(graph.vertices));
        int cols = (int) Math.ceil((double) graph.vertices / rows);

        if (rows == 0 || cols == 0) {
            return; // 避免除以零
        }

        int cellWidth = width / cols;
        int cellHeight = height / rows;

        // Draw edges
        for (Graph.Edge edge : graph.edges) {
            int x1 = (edge.source % cols) * cellWidth + cellWidth / 2;
            int y1 = (edge.source / cols) * cellHeight + cellHeight / 2;
            int x2 = (edge.destination % cols) * cellWidth + cellWidth / 2;
            int y2 = (edge.destination / cols) * cellHeight + cellHeight / 2;
            if (mst != null && mst.contains(edge)) {
                g.setColor(Color.RED);
            } else if (shortestPath != null && shortestPath.contains(edge.source) && shortestPath.contains(edge.destination)) {
                g.setColor(Color.BLUE);
            } else {
                g.setColor(Color.BLACK);
            }
            g.drawLine(x1, y1, x2, y2);
            g.drawString(String.valueOf(edge.weight), (x1 + x2) / 2, (y1 + y2) / 2);
        }

        // Draw vertices
        for (int i = 0; i < graph.vertices; i++) {
            int x = (i % cols) * cellWidth + cellWidth / 2 - 10;
            int y = (i / cols) * cellHeight + cellHeight / 2 - 10;
            if (criticalNodes != null && criticalNodes.contains(i)) {
                g.setColor(Color.RED);
            } else if (shortestPath != null && shortestPath.contains(i)) {
                g.setColor(Color.BLUE);
            } else {
                g.setColor(Color.BLACK);
            }
            g.fillRect(x, y, 20, 20);
            g.setColor(Color.WHITE);
            g.drawString(String.valueOf(i), x + 7, y + 15);
        }

        // Display vertex and edge information
        g.setColor(Color.BLACK);
        g.drawString("Vertices: " + graph.vertices, 10, 20);
        g.drawString("Edges: " + graph.edges.size(), 10, 40);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GraphGUI().setVisible(true));
    }
}

class Graph {
    int vertices;
    List<Edge> edges;

    public Graph(int v) {
        this.vertices = Math.max(0, v);
        edges = new ArrayList<>();
    }

    public void addEdge(int u, int v, int w) {
        edges.add(new Edge(u, v, w));
    }

    public boolean hasEdge(int u, int v) {
        for (Edge edge : edges) {
            if ((edge.source == u && edge.destination == v) || (edge.source == v && edge.destination == u)) {
                return true;
            }
        }
        return false;
    }

    public List<Edge> kruskalMST() {
        List<Edge> result = new ArrayList<>();
        edges.sort(Comparator.comparingInt(e -> e.weight));

        DisjointSet ds = new DisjointSet(vertices);

        for (Edge edge : edges) {
            if (ds.find(edge.source) != ds.find(edge.destination)) {
                result.add(edge);
                ds.union(edge.source, edge.destination);
            }
        }

        return result;
    }

    public Set<Integer> findCriticalNodes() {
        Set<Integer> criticalNodes = new HashSet<>();
        boolean[] visited = new boolean[vertices];
        int[] disc = new int[vertices];
        int[] low = new int[vertices];
        int[] parent = new int[vertices];
        Arrays.fill(parent, -1);

        for (int i = 0; i < vertices; i++) {
            if (!visited[i]) {
                dfsForCriticalNodes(i, visited, disc, low, parent, criticalNodes);
            }
        }

        return criticalNodes;
    }

    private int time = 0;
    private void dfsForCriticalNodes(int u, boolean[] visited, int[] disc, int[] low, int[] parent, Set<Integer> criticalNodes) {
        visited[u] = true;
        disc[u] = low[u] = ++time;
        int children = 0;

        for (Edge edge : edges) {
            if (edge.source == u || edge.destination == u) {
                int v = (edge.source == u) ? edge.destination : edge.source;
                if (!visited[v]) {
                    children++;
                    parent[v] = u;
                    dfsForCriticalNodes(v, visited, disc, low, parent, criticalNodes);
                    low[u] = Math.min(low[u], low[v]);

                    if (parent[u] == -1 && children > 1) {
                        criticalNodes.add(u);
                    }
                    if (parent[u] != -1 && low[v] >= disc[u]) {
                        criticalNodes.add(u);
                    }
                } else if (v != parent[u]) {
                    low[u] = Math.min(low[u], disc[v]);
                }
            }
        }
    }

    public List<Integer> dijkstra(int start, int end) {
        int[] dist = new int[vertices];
        int[] prev = new int[vertices];
        PriorityQueue<Node> pq = new PriorityQueue<>();
        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(prev, -1);
        dist[start] = 0;
        pq.offer(new Node(start, 0));
        while (!pq.isEmpty()) {
            Node node = pq.poll();
            int u = node.vertex;
            if (u == end) break;
            for (Edge edge : edges) {
                if (edge.source == u || edge.destination == u) {
                    int v = (edge.source == u) ? edge.destination : edge.source;
                    int alt = dist[u] + edge.weight;
                    if (alt < dist[v]) {
                        dist[v] = alt;
                        prev[v] = u;
                        pq.offer(new Node(v, alt));
                    }
                }
            }
        }
        List<Integer> path = new ArrayList<>();
        for (int at = end; at != -1; at = prev[at]) {
            path.add(at);
        }
        Collections.reverse(path);
        return path;
    }

    class Edge {
        int source, destination, weight;
        Edge(int s, int d, int w) {
            this.source = s;
            this.destination = d;
            this.weight = w;
        }
    }

    class Node implements Comparable<Node> {
        int vertex, dist;
        Node(int v, int d) {
            vertex = v;
            dist = d;
        }
        public int compareTo(Node n) {
            return Integer.compare(this.dist, n.dist);
        }
    }
}

class DisjointSet {
    private int[] parent;
    private int[] rank;

    public DisjointSet(int n) {
        parent = new int[n];
        rank = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
        }
    }

    public int find(int x) {
        if (parent[x] != x) {
            parent[x] = find(parent[x]);
        }
        return parent[x];
    }

    public void union(int x, int y) {
        int xRoot = find(x);
        int yRoot = find(y);

        if (xRoot == yRoot) {
            return;
        }

        if (rank[xRoot] < rank[yRoot]) {
            parent[xRoot] = yRoot;
        } else if (rank[xRoot] > rank[yRoot]) {
            parent[yRoot] = xRoot;
        } else {
            parent[yRoot] = xRoot;
            rank[xRoot]++;
        }
    }
}