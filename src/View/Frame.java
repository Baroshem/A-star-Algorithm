package View;

import Controller.AStar;
import Model.Node;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Frame extends JPanel implements ActionListener, MouseListener, MouseMotionListener, KeyListener {
    private Controller ch;
    private AStar pathfinding;
    private boolean showSteps;
    private int size;
    private char currentKey = (char) 0;
    private Node startNode, endNode;
    private String mode;

    private Timer timer = new Timer(100, this);

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    public boolean showSteps() {
        return showSteps;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        MapCalculations(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        MapCalculations(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        currentKey = e.getKeyChar();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        currentKey = (char) 0;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Moves one step ahead in path finding (called on timer)
        if (pathfinding.isRunning() && showSteps) {
            pathfinding.findPath(pathfinding.getPar());
            mode = "Running";
        }
        // Actions of run/stop/clear button
        if(e.getActionCommand() != null) {
            if(e.getActionCommand().equals("run") && !pathfinding.isRunning()) {
                ch.getB("run").setText("stop");
                try {
                    start();
                } catch (OutOfPointsEx x) {
                    System.out.println(x.getError());
                }
            }
            else if(e.getActionCommand().equals("clear")) {
                ch.getB("run").setText("run");
                mode = "Map Creation";
                ch.getL("noPathT").setVisible(false);
                pathfinding.reset();
            }
            else if(e.getActionCommand().equals("stop")) {
                ch.getB("run").setText("start");
                timer.stop();
            }
            else if(e.getActionCommand().equals("start")) {
                ch.getB("run").setText("stop");
                timer.start();
            }
        }
        repaint();
    }

    public Frame() {
        ch = new Controller(this);
        size = 25;
        mode = "Map Creation";
        showSteps = true;
        setLayout(null);
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        // Set up pathfinding
        pathfinding = new AStar(this, size);
        pathfinding.setDiagonal(true);

        // Set up window
        JFrame window = new JFrame();
        window.setContentPane(this);
        window.setTitle("A* Algorithm");
        window.getContentPane().setPreferredSize(new Dimension(800, 700));
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        // Add all controls
        ch.addAll();

        this.revalidate();
        this.repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draws grid
        g.setColor(Color.GRAY);
        for (int j = 0; j < this.getHeight(); j += size) {
            for (int i = 0; i < this.getWidth(); i += size) {
                g.drawRect(i, j, size, size);
            }
        }

        // Draws all borders
        g.setColor(Color.black);
        for (int i = 0; i < pathfinding.getBorderList().size(); i++) {
            g.fillRect(pathfinding.getBorderList().get(i).getX() + 1, pathfinding.getBorderList().get(i).getY() + 1,
                    size - 1, size - 1);
        }

        // Draws all open Nodes (path finding nodes)
        for (int i = 0; i < pathfinding.getOpenList().size(); i++) {
            Node current = pathfinding.getOpenList().get(i);
            g.setColor(style.getGreenHighlight());
            g.fillRect(current.getX() + 1, current.getY() + 1, size - 1, size - 1);
        }

        // Draws all closed nodes
        for (int i = 0; i < pathfinding.getClosedList().size(); i++) {
            Node current = pathfinding.getClosedList().get(i);

            g.setColor(style.getRedHighlight());
            g.fillRect(current.getX() + 1, current.getY() + 1, size - 1, size - 1);
        }

        // Draw all final path nodes
        for (int i = 0; i < pathfinding.getPathList().size(); i++) {
            Node current = pathfinding.getPathList().get(i);

            g.setColor(style.getBlueHighlight());
            g.fillRect(current.getX() + 1, current.getY() + 1, size - 1, size - 1);
        }

        // Draws start of path
        if (startNode != null) {
            g.setColor(Color.blue);
            g.fillRect(startNode.getX() + 1, startNode.getY() + 1, size - 1, size - 1);
        }
        // Draws end of path
        if (endNode != null) {
            g.setColor(Color.red);
            g.fillRect(endNode.getX() + 1, endNode.getY() + 1, size - 1, size - 1);
        }

        g.setColor(style.getBtnPanel());

        // Drawing control panel rectangle
        g.fillRect(10, 10, 322, 90);

        // Setting mode text
        ch.getL("modeText").setText("Mode: " + mode);

        // Position all controls
        ch.position();

        // Setting numbers in pathfinding lists
        ch.getL("openC").setText(Integer.toString(pathfinding.getOpenList().size()));
        ch.getL("closedC").setText(Integer.toString(pathfinding.getClosedList().size()));
        ch.getL("pathC").setText(Integer.toString(pathfinding.getPathList().size()));

        File f = new File("./output/output.txt");
        try {
            FileWriter fw = new FileWriter(f);
            PrintWriter pw = new PrintWriter(fw);
            pw.printf("Open nodes: %s ", pathfinding.getOpenList().size());
            pw.printf("Closed nodes: %s ", pathfinding.getClosedList().size());
            pw.printf("Final path length: %s", pathfinding.getPathList().size());
            pw.close();
        } catch (IOException e) {
            e.getMessage();
        }

        // Getting values from checkboxes
        showSteps = ch.getC("showStepsCheck").isSelected();

        // If no path is found
        if (pathfinding.isNoPath()) {
            // Set timer for animation
            timer.setDelay(30);
            timer.start();

            // Set text of "run" button to "clear"
            ch.getB("run").setText("clear");

            // Set mode to "No Path"
            mode = "No Path";

            // Place "No Path" text on screen in center
            ch.noPathTBounds();
            ch.getL("noPathT").setVisible(true);
            this.add(ch.getL("noPathT"));
            this.revalidate();
        }

        // If pathfinding is complete (found path)
        if (pathfinding.isComplete()) {
            // Set run button to clear
            ch.getB("run").setText("clear");

            // Set timer delay, start for background animation
            timer.setDelay(30);
            timer.start();

            // Set completed mode
            if(showSteps) {
                mode = "Completed";
            }
        }
    }

    private void MapCalculations(MouseEvent e) {
        // If left mouse button is clicked
        if (SwingUtilities.isLeftMouseButton(e)) {
            // If 's' is pressed create start node
            if (currentKey == 's') {
                int xRollover = e.getX() % size;
                int yRollover = e.getY() % size;

                if (startNode == null) {
                    startNode = new Node(e.getX() - xRollover, e.getY() - yRollover);
                } else {
                    startNode.setXY(e.getX() - xRollover, e.getY() - yRollover);
                }
                repaint();
            }
            // If 'e' is pressed create end node
            else if (currentKey == 'e') {
                int xRollover = e.getX() % size;
                int yRollover = e.getY() % size;

                if (endNode == null) {
                    endNode = new Node(e.getX() - xRollover, e.getY() - yRollover);
                } else {
                    endNode.setXY(e.getX() - xRollover, e.getY() - yRollover);
                }
                repaint();
            }
            // Otherwise, create a wall
            else {
                int xBorder = e.getX() - (e.getX() % size);
                int yBorder = e.getY() - (e.getY() % size);

                Node newBorder = new Node(xBorder, yBorder);
                pathfinding.addBorder(newBorder);

                repaint();
            }
        }
        // If right mouse button is clicked
        else if (SwingUtilities.isRightMouseButton(e)) {
            int mouseBoxX = e.getX() - (e.getX() % size);
            int mouseBoxY = e.getY() - (e.getY() % size);

            // If 's' is pressed remove start node
            if (currentKey == 's') {
                if (startNode != null && mouseBoxX == startNode.getX() && startNode.getY() == mouseBoxY) {
                    startNode = null;
                    repaint();
                }
            }
            // If 'e' is pressed remove end node
            else if (currentKey == 'e') {
                if (endNode != null && mouseBoxX == endNode.getX() && endNode.getY() == mouseBoxY) {
                    endNode = null;
                    repaint();
                }
            }
            // Otherwise, remove wall
            else {
                int Location = pathfinding.searchBorder(mouseBoxX, mouseBoxY);
                if (Location != -1) {
                    pathfinding.removeBorder(Location);
                }
                repaint();
            }
        }
    }

    private void start() throws OutOfPointsEx {
        if(startNode != null && endNode != null) {
            if (!showSteps) {
                pathfinding.start(startNode, endNode);
            } else {
                pathfinding.setup(startNode, endNode);
                timer.setDelay(50);
                timer.start();
            }
        }
        else {
            OutOfPointsEx o = new OutOfPointsEx();
            o.setError("ERROR: Needs start and end points to run.");
            throw o;
        }
    }
}