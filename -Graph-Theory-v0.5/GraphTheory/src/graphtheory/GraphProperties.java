/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package graphtheory;

import javax.swing.*;
import java.awt.Color;
import java.awt.Graphics;
import java.util.*;

/**
 *
 * @author mk
 */
public class GraphProperties {

    public int[][] adjacencyMatrix;
    public int[][] distanceMatrix;
    public Vector<VertexPair> vpList;
    private int time = 0;

    public Set<Vertex> cutpoints;
    public Set<Edge> bridges = new HashSet<>();

    public String printWeightedDegrees(Vector<Vertex> vertexList, Vector<Edge> edgeList) {
        StringBuilder weightedDegrees = new StringBuilder();
        for (Vertex vertex : vertexList) {
            int weightedDegree = 0;
            for (Edge edge : edgeList) {
                if (edge.vertex1 == vertex || edge.vertex2 == vertex) {
                    weightedDegree += edge.weight; // Assuming `weight` is a field in Edge
                }
            }
            weightedDegrees.append("Weighted Degree of ").append(vertex.getName()).append(": ").append(weightedDegree).append("\n");
        }
        return weightedDegrees.toString();
    }

    public Map<Vertex, Double> computeBetweennessCentrality(Vector<Vertex> vList) {
        Map<Vertex, Double> betweenness = new HashMap<>();
        for (Vertex v : vList) {
            betweenness.put(v, 0.0);
        }

        for (Vertex s : vList) {
            Stack<Vertex> stack = new Stack<>();
            Map<Vertex, List<Vertex>> predecessors = new HashMap<>();
            Map<Vertex, Integer> distance = new HashMap<>();
            Map<Vertex, Integer> sigma = new HashMap<>();
            Map<Vertex, Double> delta = new HashMap<>();

            for (Vertex v : vList) {
                predecessors.put(v, new ArrayList<>());
                distance.put(v, -1);
                sigma.put(v, 0);
                delta.put(v, 0.0);
            }

            distance.put(s, 0);
            sigma.put(s, 1);
            Queue<Vertex> queue = new LinkedList<>();
            queue.add(s);

            while (!queue.isEmpty()) {
                Vertex v = queue.poll();
                stack.push(v);

                for (Vertex w : v.connectedVertices) {
                    if (distance.get(w) < 0) {
                        queue.add(w);
                        distance.put(w, distance.get(v) + 1);
                    }

                    if (distance.get(w) == distance.get(v) + 1) {
                        sigma.put(w, sigma.get(w) + sigma.get(v));
                        predecessors.get(w).add(v);
                    }
                }
            }

            while (!stack.isEmpty()) {
                Vertex w = stack.pop();
                for (Vertex v : predecessors.get(w)) {
                    delta.put(v, delta.get(v) + (sigma.get(v) * 1.0 / sigma.get(w)) * (1 + delta.get(w)));
                }
                if (!w.equals(s)) {
                    betweenness.put(w, betweenness.get(w) + delta.get(w));
                }
            }
        }

        // Normalize for undirected graphs
        for (Map.Entry<Vertex, Double> entry : betweenness.entrySet()) {
            entry.setValue(entry.getValue() / 2);
        }

        return betweenness;
    }

    public String printBetweenness(Vector<Vertex> vList) {
        StringBuilder sb = new StringBuilder();
        Map<Vertex, Double> betweenness = computeBetweennessCentrality(vList);

        sb.append("Betweenness Centrality:\n");
        for (Map.Entry<Vertex, Double> entry : betweenness.entrySet()) {
            sb.append("Vertex ").append(entry.getKey().name)
                    .append(": ").append(entry.getValue()).append("\n");
        }

        return sb.toString();
    }

    public Map<Vertex, Integer> computeDegrees(Vector<Vertex> vList) {
        Map<Vertex, Integer> degreeMap = new HashMap<>();

        for (Vertex v : vList) {
            degreeMap.put(v, v.getDegree()); // Assuming Vertex class has getDegree()
        }

        return degreeMap;
    }

    public String printDegrees(Vector<Vertex> vList) {
        StringBuilder sb = new StringBuilder();

        Map<Vertex, Integer> degreeMap = computeDegrees(vList);
        for (Map.Entry<Vertex, Integer> entry : degreeMap.entrySet()) {
            sb.append("Vertex ").append(entry.getKey().name)
                    .append(": Degree = ").append(entry.getValue()).append("\n");
        }

        return sb.toString();
    }

    public Map<Vertex, Double> computeClosenessCentrality(Vector<Vertex> vList) {
        Map<Vertex, Double> closenessCentrality = new HashMap<>();

        // Generate the distance matrix (if not already generated)
        if (distanceMatrix == null) {
            generateDistanceMatrix(vList);
        }

        int N = vList.size(); // Total number of vertices

        // Calculate closeness centrality for each vertex
        for (Vertex v : vList) {
            int sumOfDistances = 0;
            int reachableVertices = 0;

            for (Vertex u : vList) {
                if (!u.equals(v)) {
                    int distance = distanceMatrix[vList.indexOf(v)][vList.indexOf(u)];
                    if (distance != Integer.MAX_VALUE) { // Ensure the vertex is reachable
                        sumOfDistances += distance;
                        reachableVertices++;
                    }
                }
            }

            // If the vertex is reachable to at least one other vertex
            if (reachableVertices > 0) {
                double averageDistance = (sumOfDistances * 1.0) / (N - 1);
                double centrality = 1.0 / averageDistance;
                closenessCentrality.put(v, centrality);
            } else {
                closenessCentrality.put(v, 0.0); // Disconnected vertex
            }
        }

        return closenessCentrality;
    }

    public String printClosenessCentrality(Vector<Vertex> vList) {
        StringBuilder sb = new StringBuilder();
        Map<Vertex, Double> closenessCentrality = computeClosenessCentrality(vList);

        sb.append("Closeness Centrality:\n");
        for (Map.Entry<Vertex, Double> entry : closenessCentrality.entrySet()) {
            sb.append("Vertex ").append(entry.getKey().name)
                    .append(": ").append(entry.getValue()).append("\n");
        }

        return sb.toString();
    }

    public Map<Vertex, Map<String, Double>> computeCentralityMeasures(Vector<Vertex> vList) {
        Map<Vertex, Map<String, Double>> centralityMeasures = new HashMap<>();

        // Compute Betweenness Centrality
        Map<Vertex, Double> betweenness = computeBetweennessCentrality(vList);

        // Compute Closeness Centrality
        Map<Vertex, Double> closeness = computeClosenessCentrality(vList);

        // Compute Degree Centrality
        Map<Vertex, Integer> degrees = computeDegrees(vList);

        // Combine all measures into a single map
        for (Vertex v : vList) {
            Map<String, Double> measures = new HashMap<>();
            measures.put("Betweenness", betweenness.get(v));
            measures.put("Closeness", closeness.get(v));
            measures.put("Degree", degrees.get(v).doubleValue()); // Convert to Double
            centralityMeasures.put(v, measures);
        }

        return centralityMeasures;
    }

    public String printCentralityMeasures(Vector<Vertex> vList) {
        StringBuilder sb = new StringBuilder();
        Map<Vertex, Map<String, Double>> centralityMeasures = computeCentralityMeasures(vList);

        // Table Header
        sb.append(String.format("%-15s %-15s %-15s %-15s\n", "Vertex", "Betweenness", "Closeness", "Degree"));

        // Table Rows
        for (Vertex v : vList) {
            Map<String, Double> measures = centralityMeasures.get(v);
            sb.append(String.format("%-15s %-15.4f %-15.4f %-15.4f\n",
                    v.name,
                    measures.get("Betweenness"),
                    measures.get("Closeness"),
                    measures.get("Degree")));
        }

        return sb.toString();
    }


    public Map<Integer, Double> computeDegreeDistribution(Vector<Vertex> vList) {
        Map<Integer, Integer> degreeCount = new HashMap<>();
        int totalVertices = vList.size();

        // Count the number of vertices with each degree
        for (Vertex v : vList) {
            int degree = v.getDegree();
            degreeCount.put(degree, degreeCount.getOrDefault(degree, 0) + 1);
        }

        // Convert counts to decimal fractions
        Map<Integer, Double> degreeDistribution = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : degreeCount.entrySet()) {
            int degree = entry.getKey();
            int count = entry.getValue();
            double fraction = (double) count / totalVertices;
            degreeDistribution.put(degree, fraction);
        }

        return degreeDistribution;
    }

    public void displayDegreeDistributionGraph(Vector<Vertex> vList, int width, int height) {
        // Compute degree distribution as decimal fractions
        Map<Integer, Double> degreeDistribution = computeDegreeDistribution(vList);

        // Create a new JFrame for the graph
        JFrame frame = new JFrame("Degree Distribution Graph");
        frame.setSize(width, height); // Set the size of the pop-up window
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Create a custom JPanel to draw the graph
        JPanel graphPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawDegreeDistributionGraph(g, degreeDistribution, width, height);
            }
        };

        frame.add(graphPanel);
        frame.setVisible(true);
    }

    private void drawDegreeDistributionGraph(Graphics g, Map<Integer, Double> degreeDistribution, int width, int height) {
        int margin = 50;
        int barWidth = 30;
        int maxBarHeight = height - 2 * margin; // Adjust bar height based on window height
        int x = margin;
        int y = height - margin;

        // Find the maximum fraction for scaling
        double maxFraction = degreeDistribution.values().stream().max(Double::compare).orElse(1.0);

        // Draw axes
        g.setColor(Color.BLACK);
        g.drawLine(margin, margin, margin, y); // Y-axis
        g.drawLine(margin, y, width - margin, y); // X-axis

        // Draw bars for each degree
        for (Map.Entry<Integer, Double> entry : degreeDistribution.entrySet()) {
            int degree = entry.getKey();
            double fraction = entry.getValue();
            int barHeight = (int) ((fraction / maxFraction) * maxBarHeight);

            // Draw bar
            g.setColor(Color.BLUE);
            g.fillRect(x, y - barHeight, barWidth, barHeight);

            // Draw degree label
            g.setColor(Color.BLACK);
            g.drawString(String.valueOf(degree), x + barWidth / 2 - 5, y + 15);

            // Draw fraction label
            g.drawString(String.format("%.2f", fraction), x + barWidth / 2 - 10, y - barHeight - 5);

            x += barWidth + 10; // Move to the next bar
        }

        // Draw Y-axis labels
        g.drawString("Frequency", margin - 40, margin - 10);
        g.drawString("0", margin - 20, y);
        g.drawString(String.format("%.2f", maxFraction), margin - 40, margin);
    }

    public int[][] generateAdjacencyMatrix(Vector<Vertex> vList, Vector<Edge> eList) {
        adjacencyMatrix = new int[vList.size()][vList.size()];

        for (int a = 0; a < vList.size(); a++)//initialize
        {
            for (int b = 0; b < vList.size(); b++) {
                adjacencyMatrix[a][b] = 0;
            }
        }

        for (int i = 0; i < eList.size(); i++) {
            adjacencyMatrix[vList.indexOf(eList.get(i).vertex1)][vList.indexOf(eList.get(i).vertex2)] = 1;
            adjacencyMatrix[vList.indexOf(eList.get(i).vertex2)][vList.indexOf(eList.get(i).vertex1)] = 1;
        }
        return adjacencyMatrix;
    }

    public Set<Vertex> identifyCutpoints(Vector<Vertex> vList) {
        cutpoints = new HashSet<>();
        int[] discoveryTime = new int[vList.size()];
        int[] low = new int[vList.size()];
        boolean[] visited = new boolean[vList.size()];
        int[] parent = new int[vList.size()];
        int time = 0;

        // initialize
        for (int i = 0; i < vList.size(); i++) {
            parent[i] = -1;
            visited[i] = false;
        }

        for (int i = 0; i < vList.size(); i++) {
            if (!visited[i]) {
                dfs(vList, i, visited, discoveryTime, low, parent, cutpoints, time);
            }
        }

        return cutpoints;
    }

    public String printCutpoints(Graphics g) {
        StringBuilder sb = new StringBuilder();
        sb.append("Cutpoints : [");
        for (Vertex v : cutpoints) {
            sb.append(v.name).append(", ");
        }
        if (!cutpoints.isEmpty()) {
            sb.setLength(sb.length() - 2);
        }
        sb.append("]");
        return sb.toString();
    }
    private void dfs(Vector<Vertex> vList, int u, boolean[] visited, int[] discoveryTime, int[] low, int[] parent, Set<Vertex> cutpoints, int time) {
        int children = 0;
        visited[u] = true;
        discoveryTime[u] = low[u] = ++time;

        for (Vertex v : vList.get(u).connectedVertices) {
            int vIndex = vList.indexOf(v);
            if (!visited[vIndex]) {
                children++;
                parent[vIndex] = u;
                dfs(vList, vIndex, visited, discoveryTime, low, parent, cutpoints, time);

                low[u] = Math.min(low[u], low[vIndex]);

                if (parent[u] == -1 && children > 1) {
                    cutpoints.add(vList.get(u));
                }

                if (parent[u] != -1 && low[vIndex] >= discoveryTime[u]) {
                    cutpoints.add(vList.get(u));
                }
            } else if (vIndex != parent[u]) {
                low[u] = Math.min(low[u], discoveryTime[vIndex]);
            }
        }
    }


    public Set<Edge> identifyBridges(Vector<Vertex> vList, Vector<Edge> eList) {
        bridges = new HashSet<>();
        int[] discoveryTime = new int[vList.size()];
        int[] low = new int[vList.size()];
        boolean[] visited = new boolean[vList.size()];
        int[] parent = new int[vList.size()];
        int time = 0;
    
        // Initialize
        for (int i = 0; i < vList.size(); i++) {
            parent[i] = -1;
            visited[i] = false;
        }
    
        for (int i = 0; i < vList.size(); i++) {
            if (!visited[i]) {
                dfsForBridges(vList, eList, i, visited, discoveryTime, low, parent, time);
            }
        }
        System.out.println("Bridges detected: " + bridges);

        return bridges;
    }

    public String printBridges() {
        StringBuilder sb = new StringBuilder();
        sb.append("Bridges : [");
        for (Edge e : bridges) {
            sb.append("(").append(e.vertex1.name).append("-").append(e.vertex2.name).append("), ");
        }
        if (!bridges.isEmpty()) {
            sb.setLength(sb.length() - 2);
        }
        sb.append("]");
        return sb.toString();
    }
    

    private void dfsForBridges(Vector<Vertex> vList, Vector<Edge> eList, int u, boolean[] visited, int[] discoveryTime, int[] low, int[] parent, int time) {
        visited[u] = true;
        discoveryTime[u] = low[u] = ++time;

        for (Vertex v : vList.get(u).connectedVertices) {
            int vIndex = vList.indexOf(v);
            if (!visited[vIndex]) {
                parent[vIndex] = u;
                dfsForBridges(vList, eList, vIndex, visited, discoveryTime, low, parent, time);

                low[u] = Math.min(low[u], low[vIndex]);

                // If the edge (u, v) is a bridge
                if (low[vIndex] > discoveryTime[u]) {
                    for (Edge edge : eList) {
                        if ((edge.vertex1 == vList.get(u) && edge.vertex2 == vList.get(vIndex)) ||
                            (edge.vertex1 == vList.get(vIndex) && edge.vertex2 == vList.get(u))) {
                            bridges.add(edge);
                        }
                    }
                }
            } else if (vIndex != parent[u]) {
                low[u] = Math.min(low[u], discoveryTime[vIndex]);
            }
        }
    }


    public int[][] generateDistanceMatrix(Vector<Vertex> vList) {
        distanceMatrix = new int[vList.size()][vList.size()];

        for (int a = 0; a < vList.size(); a++)//initialize
        {
            for (int b = 0; b < vList.size(); b++) {
                distanceMatrix[a][b] = 0;
            }
        }

        VertexPair vp;
        int shortestDistance;
        for (int i = 0; i < vList.size(); i++) {
            for (int j = i + 1; j < vList.size(); j++) {
                vp = new VertexPair(vList.get(i), vList.get(j));
                shortestDistance = vp.getShortestDistance();
                distanceMatrix[vList.indexOf(vp.vertex1)][vList.indexOf(vp.vertex2)] = shortestDistance;
                distanceMatrix[vList.indexOf(vp.vertex2)][vList.indexOf(vp.vertex1)] = shortestDistance;
            }
        }
        return distanceMatrix;
    }

    public void displayContainers(Vector<Vertex> vList) {
        vpList = new Vector<VertexPair>();
        int[] kWideGraph = new int[10];
        for (int i = 0; i < kWideGraph.length; i++) {
            kWideGraph[i] = -1;
        }



        VertexPair vp;

        for (int a = 0; a < vList.size(); a++) {    // assign vertex pairs
            for (int b = a + 1; b < vList.size(); b++) {
                vp = new VertexPair(vList.get(a), vList.get(b));
                vpList.add(vp);
                int longestWidth = 0;
                System.out.println(">Vertex Pair " + vList.get(a).name + "-" + vList.get(b).name + "\n All Paths:");
                vp.generateVertexDisjointPaths();
                for (int i = 0; i < vp.VertexDisjointContainer.size(); i++) {//for every container of the vertex pair
                    int width = vp.VertexDisjointContainer.get(i).size();
                    Collections.sort(vp.VertexDisjointContainer.get(i), new descendingWidthComparator());
                    int longestLength = vp.VertexDisjointContainer.get(i).firstElement().size();
                    longestWidth = Math.max(longestWidth, width);
                    System.out.println("\tContainer " + i + " - " + "Width=" + width + " - Length=" + longestLength);

                    for (int j = 0; j < vp.VertexDisjointContainer.get(i).size(); j++) //for every path in the container
                    {
                        System.out.println("\t\tPath " + j + "\n\t\t\t");
                        for (int k = 0; k < vp.VertexDisjointContainer.get(i).get(j).size(); k++) {
                            System.out.print("-" + vp.VertexDisjointContainer.get(i).get(j).get(k).name);
                        }
                        System.out.println();
                    }

                }
                //d-wide for vertexPair
                for (int k = 1; k <= longestWidth; k++) { // 1-wide, 2-wide, 3-wide...
                    int minLength = 999;
                    for (int m = 0; m < vp.VertexDisjointContainer.size(); m++) // for each container with k-wide select shortest length
                    {
                        minLength = Math.min(minLength, vp.VertexDisjointContainer.get(m).size());
                    }
                    if (minLength != 999) {
                        System.out.println(k + "-wide for vertexpair(" + vp.vertex1.name + "-" + vp.vertex2.name + ")=" + minLength);
                        kWideGraph[k] = Math.max(kWideGraph[k], minLength);
                    }
                }
            }
        }

        for (int i = 0; i < kWideGraph.length; i++) {
            if (kWideGraph[i] != -1) {
                System.out.println("D" + i + "(G)=" + kWideGraph[i]);
            }
        }


    }

    public void drawAdjacencyMatrix(Graphics g, Vector<Vertex> vList, int x, int y) {
        int cSize = 20;
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(x, y-30, vList.size() * cSize+cSize, vList.size() * cSize+cSize);
        g.setColor(Color.black);
        g.drawString("AdjacencyMatrix", x, y - cSize);
        for (int i = 0; i < vList.size(); i++) {
            g.setColor(Color.RED);
            g.drawString(vList.get(i).name, x + cSize + i * cSize, y);
            g.drawString(vList.get(i).name, x, cSize + i * cSize + y);
            g.setColor(Color.black);
            for (int j = 0; j < vList.size(); j++) {
                g.drawString("" + adjacencyMatrix[i][j], x + cSize * (j + 1), y + cSize * (i + 1));
            }
        }
    }

    public void drawDistanceMatrix(Graphics g, Vector<Vertex> vList, int x, int y) {
        int cSize = 20;
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(x, y-30, vList.size() * cSize+cSize, vList.size() * cSize+cSize);
        g.setColor(Color.black);
        g.drawString("ShortestPathMatrix", x, y - cSize);
        for (int i = 0; i < vList.size(); i++) {
            g.setColor(Color.RED);
            g.drawString(vList.get(i).name, x + cSize + i * cSize, y);
            g.drawString(vList.get(i).name, x, cSize + i * cSize + y);
            g.setColor(Color.black);
            for (int j = 0; j < vList.size(); j++) {
                g.drawString("" + distanceMatrix[i][j], x + cSize * (j + 1), y + cSize * (i + 1));
            }
        }
    }

    public Vector<Vertex> vertexConnectivity(Vector<Vertex> vList) {
        Vector<Vertex> origList = new Vector<Vertex>();
        Vector<Vertex> tempList = new Vector<Vertex>();
        Vector<Vertex> toBeRemoved = new Vector<Vertex>();
        Vertex victim;


        origList.setSize(vList.size());
        Collections.copy(origList, vList);

        int maxPossibleRemove = 0;
        while (graphConnectivity(origList)) {
            Collections.sort(origList, new ascendingDegreeComparator());
            maxPossibleRemove = origList.firstElement().getDegree();

            for (Vertex v : origList) {
                if (v.getDegree() == maxPossibleRemove) {
                    for (Vertex z : v.connectedVertices) {
                        if (!tempList.contains(z)) {
                            tempList.add(z);
                        }
                    }
                }
            }

            while (graphConnectivity(origList) && tempList.size() > 0) {
                Collections.sort(tempList, new descendingDegreeComparator());
                victim = tempList.firstElement();
                tempList.removeElementAt(0);
                origList.remove(victim);
                for (Vertex x : origList) {
                    x.connectedVertices.remove(victim);
                }
                toBeRemoved.add(victim);
            }
            tempList.removeAllElements();
        }

        return toBeRemoved;
    }

    private boolean graphConnectivity(Vector<Vertex> vList) { //Vertex connectivity

        Vector<Vertex> visitedList = new Vector<Vertex>();

        recurseGraphConnectivity(vList.firstElement().connectedVertices, visitedList); //recursive function
        if (visitedList.size() != vList.size()) {
            return false;
        } else {
            return true;
        }
    }

    private void recurseGraphConnectivity(Vector<Vertex> vList, Vector<Vertex> visitedList) {
        for (Vertex v : vList) {
            {
                if (!visitedList.contains(v)) {
                    visitedList.add(v);
                    recurseGraphConnectivity(v.connectedVertices, visitedList);
                }
            }
        }
    }

    private class ascendingDegreeComparator implements Comparator {

        public int compare(Object v1, Object v2) {

            if (((Vertex) v1).getDegree() > ((Vertex) v2).getDegree()) {
                return 1;
            } else if (((Vertex) v1).getDegree() > ((Vertex) v2).getDegree()) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    private class descendingDegreeComparator implements Comparator {

        public int compare(Object v1, Object v2) {

            if (((Vertex) v1).getDegree() > ((Vertex) v2).getDegree()) {
                return -1;
            } else if (((Vertex) v1).getDegree() > ((Vertex) v2).getDegree()) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    private class descendingWidthComparator implements Comparator {

        public int compare(Object v1, Object v2) {

            if (((Vector<Vertex>) v1).size() > (((Vector<Vertex>) v2).size())) {
                return -1;
            } else if (((Vector<Vertex>) v1).size() < (((Vector<Vertex>) v2).size())) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    public void findBridges(List<Vertex> vertices, VertexPair vertexPair) {
        Map<Vertex, Integer> discoveryTime = new HashMap<>();
        Map<Vertex, Integer> low = new HashMap<>();
        Map<Vertex, Vertex> parent = new HashMap<>();
        Set<Vertex> visited = new HashSet<>();

        // Initialize tracking maps
        for (Vertex v : vertices) {
            discoveryTime.put(v, -1);
            low.put(v, -1);
            parent.put(v, null);
        }

        // Call DFS for bridge detection
        for (Vertex v : vertices) {
            if (!visited.contains(v)) {
                dfsForBridges(v, visited, discoveryTime, low, parent);
            }
        }
    }

    private void dfsForBridges(Vertex u, Set<Vertex> visited,
                               Map<Vertex, Integer> discoveryTime,
                               Map<Vertex, Integer> low,
                               Map<Vertex, Vertex> parent) {
        visited.add(u);
        discoveryTime.put(u, time);
        low.put(u, time);
        time++;

        for (Vertex v : u.connectedVertices) {
            if (!visited.contains(v)) {  // If v is not visited, it's a tree edge
                parent.put(v, u);
                dfsForBridges(v, visited, discoveryTime, low, parent);

                // Check if the subtree has a back connection
                low.put(u, Math.min(low.get(u), low.get(v)));

                // **Bridge condition**: If no back connection, it's a bridge
                if (low.get(v) > discoveryTime.get(u)) {
                    System.out.println("Bridge found: " + u.name + " - " + v.name);
                }
            } else if (!v.equals(parent.get(u))) { // Back edge case
                low.put(u, Math.min(low.get(u), discoveryTime.get(v)));
            }
        }
    }
}
