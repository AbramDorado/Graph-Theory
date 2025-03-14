package graphtheory;

/**
 *
 * @author mk
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class Canvas {

    public JFrame frame;
    private JMenuBar menuBar;
    private CanvasPane canvas;
    private Graphics2D graphic;
    private Color backgroundColour;
    private Image canvasImage,  canvasImage2;
    private int selectedTool;
    private int selectedWindow;
    private Dimension screenSize;
    public int width,  height;
    private int clickedVertexIndex;
    private int clickedEdgeIndex;
    private FileManager fileManager = new FileManager();


    /////////////
    private Vector<Vertex> vertexList;
    private Vector<Edge> edgeList;
    private GraphProperties gP = new GraphProperties();
    /////////////

    public Canvas(String title, int width, int height, Color bgColour) {
        frame = new JFrame();
        frame.setTitle(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        canvas = new CanvasPane();
        InputListener inputListener = new InputListener();
        canvas.addMouseListener(inputListener);
        canvas.addMouseMotionListener(inputListener);
        frame.setContentPane(canvas);

        this.width = width;
        this.height = height;
        canvas.setPreferredSize(new Dimension(width, height));

        //events
        menuBar = new JMenuBar();
        JMenu menuOptions = new JMenu("Tools");
        JMenu menuOptions1 = new JMenu("File");
        JMenu menuOptions2 = new JMenu("Extras");
        JMenu menuOptions3 = new JMenu("Window");

        JMenuItem item = new JMenuItem("Add Vertex");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK));
        item.addActionListener(new MenuListener());
        menuOptions.add(item);
        item = new JMenuItem("Open File");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        item.addActionListener(new MenuListener());
        menuOptions1.add(item);
        item = new JMenuItem("Save to File");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        item.addActionListener(new MenuListener());
        menuOptions1.add(item);
        item = new JMenuItem("Add Edges");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK));
        item.addActionListener(new MenuListener());
        menuOptions.add(item);
        item = new JMenuItem("Grab Tool");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK));
        item.addActionListener(new MenuListener());
        menuOptions.add(item);
        item = new JMenuItem("Remove Tool");
        item.addActionListener(new MenuListener());
        menuOptions.add(item);
        item = new JMenuItem("Auto Arrange Vertices");
        item.addActionListener(new MenuListener());

        menuOptions2.add(item);
        item = new JMenuItem("Remove All");
        item.addActionListener(new MenuListener());
        menuOptions2.add(item);

        item = new JMenuItem("Graph");
        item.addActionListener(new MenuListener());
        menuOptions3.add(item);
        item = new JMenuItem("Properties");
        item.addActionListener(new MenuListener());
        menuOptions3.add(item);

        menuBar.add(menuOptions1);
        menuBar.add(menuOptions);
        menuBar.add(menuOptions2);
        menuBar.add(menuOptions3);

        frame.setJMenuBar(menuBar);

        backgroundColour = bgColour;

        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setBounds(screenSize.width / 2 - width / 2, screenSize.height / 2 - height / 2, width, height);
        frame.pack();
        setVisible(true);

        vertexList = new Vector<Vertex>();
        edgeList = new Vector<Edge>();

    }

    class InputListener implements MouseListener, MouseMotionListener {

        @Override
        public void mouseClicked(MouseEvent e) {

            if (selectedWindow == 0) {
                switch (selectedTool) {
                    case 1: {
                        // Prompt user for name, weight, and label
                        String name = JOptionPane.showInputDialog("Enter vertex name:");
                        if (name == null || name.trim().isEmpty()) {
                            return; // Cancel if no name is provided
                        }
                        // Create vertex with user input
                        Vertex v = new Vertex(name, e.getX(), e.getY());

                        vertexList.add(v);
                        v.draw(graphic);
                        Canvas.this.refresh();
                        break;
                    }
                    case 4: {
                        for (Vertex v : vertexList) {
                            if (v.hasIntersection(e.getX(), e.getY())) {
                                for (Edge d : edgeList) {
                                    if (d.vertex1 == v || d.vertex2 == v) {
                                        edgeList.remove(d);
                                    }
                                }
                                for (Vertex x : vertexList) {
                                    if (x.connectedToVertex(v)) {
                                        x.connectedVertices.remove(v);
                                    }
                                }
                                vertexList.remove(v);
                            }
                        } break;
                    }
                }
                //refresh();
            }


        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (selectedWindow == 0 && vertexList.size() > 0) {
                switch (selectedTool) {
                    case 2: {
                        for (Vertex v : vertexList) {
                            if (v.hasIntersection(e.getX(), e.getY())) {
                                v.wasClicked = true;
                                clickedVertexIndex = vertexList.indexOf(v);
                            } else {
                                v.wasClicked = false;
                            }
                        }
                        break;
                    }
                    case 3: {

                        for (Edge d : edgeList) {
                            if (d.hasIntersection(e.getX(), e.getY())) {
                                d.wasClicked = true;
                                clickedEdgeIndex = edgeList.indexOf(d);
                            } else {
                                d.wasClicked = false;
                            }
                        }
                        for (Vertex v : vertexList) {
                            if (v.hasIntersection(e.getX(), e.getY())) {
                                v.wasClicked = true;
                                clickedVertexIndex = vertexList.indexOf(v);
                            } else {
                                v.wasClicked = false;
                            }
                        }
                        break;
                    }
                }
            }

        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (selectedWindow == 0 && vertexList.size() > 0) {
                switch (selectedTool) {
                    case 2: {
                        Vertex parentV = vertexList.get(clickedVertexIndex);
                        for (Vertex v : vertexList) {
                            if (v.hasIntersection(e.getX(), e.getY()) && v != parentV && !v.connectedToVertex(parentV)) {              //System.out.println(clickedVertexIndex+" "+vertexList.indexOf(v));
                                String weightStr = JOptionPane.showInputDialog("Enter weight for this edge:");
                                int weight = Integer.parseInt(weightStr);
                                Edge edge = new Edge(v, parentV, weight);
                                v.addVertex(parentV);
                                parentV.addVertex(v);
                                v.wasClicked = false;
                                parentV.wasClicked = false;
                                edgeList.add(edge);
                            } else {
                                v.wasClicked = false;
                            }
                        }
                        break;
                    }
                    case 3: {
                        vertexList.get(clickedVertexIndex).wasClicked = false;
                        break;
                    }
                }
            }
            erase();
            refresh();
        }

        @Override
        public void mouseDragged(MouseEvent e) {

            if (selectedWindow == 0 && vertexList.size() > 0) {
                erase();
                switch (selectedTool) {
                    case 2: {
                        graphic.setColor(Color.RED);
                        drawLine(vertexList.get(clickedVertexIndex).location.x, vertexList.get(clickedVertexIndex).location.y, e.getX(), e.getY());
                        break;

                    }
                    case 3: {
                        if (vertexList.get(clickedVertexIndex).wasClicked) {
                            vertexList.get(clickedVertexIndex).location.x = e.getX();
                            vertexList.get(clickedVertexIndex).location.y = e.getY();
                        }
                        break;
                    }
                }
                refresh();
            }

        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (selectedWindow == 0) {
                for (Edge d : edgeList) {
                    if (d.hasIntersection(e.getX(), e.getY())) {
                        d.wasFocused = true;
                    } else {
                        d.wasFocused = false;
                    }
                }
                for (Vertex v : vertexList) {
                    if (v.hasIntersection(e.getX(), e.getY())) {
                        v.wasFocused = true;
                    } else {
                        v.wasFocused = false;
                    }
                }
                refresh();
            }

        }
    }

    class MenuListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("Add Vertex")) {
                selectedTool = 1;
            } else if (command.equals("Add Edges")) {
                selectedTool = 2;
            } else if (command.equals("Grab Tool")) {
                selectedTool = 3;
            } else if (command.equals("Remove Tool")) {
                selectedTool = 4;
            } else if (command.equals("Auto Arrange Vertices")) {
                arrangeVertices();
                erase();
            } else if (command.equals("Remove All")) {
                edgeList.removeAllElements();
                vertexList.removeAllElements();
                clickedVertexIndex = 0;
                erase();
            } else if (command.equals("Open File")) {
                int returnValue = fileManager.jF.showOpenDialog(frame);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    loadFile(fileManager.loadFile(fileManager.jF.getSelectedFile()));
                    System.out.println(fileManager.jF.getSelectedFile());
                    selectedWindow=0;
                }
            } else if (command.equals("Save to File")) {
                int returnValue = fileManager.jF.showSaveDialog(frame);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    fileManager.saveFile(vertexList,fileManager.jF.getSelectedFile());
                    System.out.println(fileManager.jF.getSelectedFile());
                }
            } else if (command.equals("Graph")) {
                selectedWindow = 0;
                erase();
            } else if (command.equals("Properties")) {
                selectedWindow = 1;
                if (vertexList.size() > 0) {
                    //adjacency list
                    int[][] matrix = gP.generateAdjacencyMatrix(vertexList, edgeList);

                    //connectivity
                    Vector<Vertex> tempList = gP.vertexConnectivity(vertexList);

                    /*
                    for (Vertex v : tempList) {
                        vertexList.get(vertexList.indexOf(v)).wasClicked = true;
                    }
                    */
                    reloadVertexConnections(matrix, vertexList);

                    //distance
                    gP.generateDistanceMatrix(vertexList);

                    //VD paths
                    gP.displayContainers(vertexList);


                    // cutpoints
                    Set<Vertex> cutpointSet = gP.identifyCutpoints(vertexList);
                    for (Vertex c : cutpointSet) {
                        c.wasClicked = true;
                    }
                    //bridges
                    Set<Edge> bridgeSet = gP.identifyBridges(vertexList, edgeList);
                    for (Edge b : bridgeSet) {
                        b.wasClicked = true;
                    }

                    // degree distribution
                    gP.computeDegreeDistribution(vertexList);

                    gP.displayDegreeDistributionGraph(vertexList, 600, 400);

                    //refresh only if the graph isnt empty
                    refresh();
                    //gP.drawNWideDiameter();
                }
                erase();
                refresh();
            }

        }
    }

    private void arrangeVertices() {
        double deg2rad = Math.PI / 180;
        double radius = height / 5;
        double centerX = width / 2;
        double centerY = height / 2;
        int interval = 360 / vertexList.size();


        for (int i = 0; i < vertexList.size(); i++) {
            double degInRad = i * deg2rad * interval;
            double x = centerX + (Math.cos(degInRad) * radius);
            double y = centerY + (Math.sin(degInRad) * radius);
            int X = (int) x;
            int Y = (int) y;
            vertexList.get(i).location.x = X;
            vertexList.get(i).location.y = Y;
        }

    }

    private void reloadVertexConnections(int[][] aMatrix, Vector<Vertex> vList) {
        for (Vertex v : vList) {
            v.connectedVertices.clear();
        }

        for (int i = 0; i < aMatrix.length; i++) {
            for (int j = 0; j < aMatrix.length; j++) {
                if (aMatrix[i][j] == 1) {
                    vList.get(i).addVertex(vList.get(j));
                }
            }
        }

    }

    private void loadFile(Vector<Vector> File) {
        vertexList = File.firstElement();
        edgeList = File.lastElement();
        erase();
    }

    public void refresh() {
        for (Edge e : edgeList) {
            e.draw(graphic);
        }
        for (Vertex v : vertexList) {
            v.draw(graphic);
        }

        canvas.repaint();
    }

    public void setVisible(boolean visible) {
        if (graphic == null) {
            Dimension size = canvas.getSize();
            canvasImage = canvas.createImage(size.width, size.height);
            canvasImage2 = canvas.createImage(size.width, size.height);
            graphic = (Graphics2D) canvasImage.getGraphics();
            graphic.setColor(backgroundColour);
            graphic.fillRect(0, 0, size.width, size.height);
            graphic.setColor(Color.black);
        }
        frame.setVisible(visible);
    }

    public boolean isVisible() {
        return frame.isVisible();
    }

    public void erase() {
        graphic.clearRect(0, 0, width, height);
    }

    public void erase(int x, int y, int x1, int y2) {
        graphic.clearRect(x, y, x1, y2);
    }

    public void drawString(String text, int x, int y, float size) {
        Font orig = graphic.getFont();
        graphic.setFont(graphic.getFont().deriveFont(1, size));
        graphic.drawString(text, x, y);
        graphic.setFont(orig);
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        graphic.drawLine(x1, y1, x2, y2);
    }

    private class CanvasPane extends JPanel {

        public void paint(Graphics g) {
            switch (selectedWindow) {
                case 0: {   // Graph window
                    graphic.drawString("Vertex Count=" + vertexList.size() +
                            "  Edge Count=" + edgeList.size() +
                            "  Selected Tool=" + selectedTool, 50, height / 2 + (height * 2) / 5);
                    g.drawImage(canvasImage, 0, 0, null); // layer 1
                    g.setColor(Color.black);
                    break;
                }
                case 1: {   // Properties window
                    canvasImage2.getGraphics().clearRect(0, 0, width, height); // Clear
                    gP.drawAdjacencyMatrix(canvasImage2.getGraphics(), vertexList, width / 2 + 50, 50);
                    gP.drawDistanceMatrix(canvasImage2.getGraphics(), vertexList, width / 2 + 50, height / 2 + 50);
                    g.drawImage(canvasImage2, 0, 0, null); // layer 1

                    drawString("Nodes in red are cutpoints & Edges in red are bridges.", 100, height - 30, 20);
                    drawString("Graph will disconnect if one of them is removed.", 100, height - 10, 20);
                    g.drawString("See output console for Diameter of Graph", 100, height / 2 + 60);
                    g.drawString(gP.printCutpoints(canvasImage2.getGraphics()), 100, height / 2 + 30);
                    g.drawString(gP.printBridges(), 100, height / 2 + 45);

                    // Compute degree distribution
                    int yPosition = height / 2 + 75; // Start drawing below bridges info
                    /*
                    g.drawString("Degree Distribution:", 100, yPosition);

                    Map<Integer, Double> degreeDist = gP.computeDegreeDistribution(vertexList);
                    int vertexCount = vertexList.size();

                    for (Map.Entry<Integer, Double> entry : degreeDist.entrySet()) {
                        yPosition += 15;
                        g.drawString("Degree " + entry.getKey() + ": " + entry.getValue() + "/" + vertexCount + " vertices", 100, yPosition);
                    }


                    /*

                    // Add spacing before printing vertex degrees
                    yPosition += 20;
                    g.drawString("Vertex Degrees:", 100, yPosition);

                    // Get the degree information from GraphProperties
                    String degreesText = gP.printDegrees(vertexList);
                    String[] lines = degreesText.split("\n");

                    for (String line : lines) {
                        yPosition += 15; // Space out each line
                        g.drawString(line, 100, yPosition);
                    }


                    // Add spacing before printing weighted vertex degrees
                    yPosition += 20;
                    g.drawString("Weighted Vertex Degrees:", 100, yPosition);



                    // Get the weighted degree information from GraphProperties
                    String weightedDegreesText = gP.printWeightedDegrees(vertexList, edgeList);
                    String[] weightedLines = weightedDegreesText.split("\n");

                    for (String line : weightedLines) {
                        yPosition += 15; // Space out each line
                        g.drawString(line, 100, yPosition);
                    }


                    String betweennessText = gP.printBetweenness(vertexList);
                    String[] betweennessLines = betweennessText.split("\n");

                    for (String line : betweennessLines) {
                        yPosition += 15; // Space out each line
                        g.drawString(line, 100, yPosition);
                    }


                    String closenessText = gP.printClosenessCentrality(vertexList);
                    String[] closenessLines = closenessText.split("\n");

                    for (String line : closenessLines) {
                        yPosition += 15; // Space out each line
                        g.drawString(line, 100, yPosition);
                    }


                    */

                    yPosition += 25; // Adjust position as needed
                    g.drawString("Centrality Measures:", 100, yPosition);

                    String centralityTable = gP.printCentralityMeasures(vertexList, edgeList);
                    String[] centralityLines = centralityTable.split("\n");

                    for (String line : centralityLines) {
                        yPosition += 15; // Space out each line
                        g.drawString(line, 100, yPosition);
                    }

                    g.drawImage(canvasImage.getScaledInstance(width / 2, height / 2, Image.SCALE_SMOOTH), 0, 0, null); // Layer 1
                    g.draw3DRect(0, 0, width / 2, height / 2, true);
                    g.setColor(Color.black);

                    break;
                }
            }
        }
    }
}

