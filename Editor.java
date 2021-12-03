import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * Client-server graphical editor
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; loosely based on CS 5 code by Tom Cormen
 * @author CBK, winter 2014, overall structure substantially revised
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 * @author CBK, spring 2016 and Fall 2016, restructured Shape and some of the GUI
 *
 * @author Logan Chang, CS10, PS6, 20F
 */

public class Editor extends JFrame {
    private static String serverIP = "localhost";            // IP address of sketch server
    // "localhost" for your own machine;
    // or ask a friend for their IP address

    private static final int width = 800, height = 800;        // canvas size

    // Current settings on GUI
    public enum Mode {
        DRAW, MOVE, RECOLOR, DELETE, FRONT, BACK
    }

    private Mode mode = Mode.DRAW;                // drawing/moving/recoloring/deleting objects
    private String shapeType = "ellipse";        // type of object to add
    private Color color = Color.black;            // current drawing color

    // Drawing state
    // these are remnants of my implementation; take them as possible suggestions or ignore them
    private Shape curr = null;                    // current shape (if any) being drawn
    private Sketch sketch;                        // holds and handles all the completed objects
    private int movingId = -1;                    // current shape id (if any; else -1) being moved
    private Point drawFrom = null;                // where the drawing started
    private Point moveFrom = null;                // where object is as it's being dragged
	private int tempID = 0;


    // Communication
    private EditorCommunicator comm;            // communication with the sketch server

    public Editor() {
        super("Graphical Editor");

        sketch = new Sketch();

        // Connect to server
		comm = new EditorCommunicator(serverIP, this);
		comm.start();

        // Helpers to create the canvas and GUI (buttons, etc.)
        JComponent canvas = setupCanvas();
        JComponent gui = setupGUI();

        // Put the buttons and canvas together into the window
        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());
        cp.add(canvas, BorderLayout.CENTER);
        cp.add(gui, BorderLayout.NORTH);

        // Usual initialization
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    /**
     * Creates a component to draw into
     */
    private JComponent setupCanvas() {
        JComponent canvas = new JComponent() {
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawSketch(g);
            }
        };

        canvas.setPreferredSize(new Dimension(width, height));

        canvas.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent event) {
                handlePress(event.getPoint());
            }

            public void mouseReleased(MouseEvent event) {
                handleRelease();
            }
        });

        canvas.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent event) {
                handleDrag(event.getPoint());
            }
        });

        return canvas;
    }

    /**
     * Creates a panel with all the buttons
     */
    private JComponent setupGUI() {
        // Select type of shape
        String[] shapes = {"ellipse", "freehand", "rectangle", "segment"};
        JComboBox<String> shapeB = new JComboBox<String>(shapes);
        shapeB.addActionListener(e -> shapeType = (String) ((JComboBox<String>) e.getSource()).getSelectedItem());

        // Select drawing/recoloring color
        // Following Oracle example
        JButton chooseColorB = new JButton("choose color");
        JColorChooser colorChooser = new JColorChooser();
        JLabel colorL = new JLabel();
        colorL.setBackground(Color.black);
        colorL.setOpaque(true);
        colorL.setBorder(BorderFactory.createLineBorder(Color.black));
        colorL.setPreferredSize(new Dimension(25, 25));
        JDialog colorDialog = JColorChooser.createDialog(chooseColorB,
                "Pick a Color",
                true,  //modal
                colorChooser,
                e -> {
                    color = colorChooser.getColor();
                    colorL.setBackground(color);
                },  // OK button
                null); // no CANCEL button handler
        chooseColorB.addActionListener(e -> colorDialog.setVisible(true));

        // Mode: draw, move, recolor, or delete
        JRadioButton drawB = new JRadioButton("draw");
        drawB.addActionListener(e -> mode = Mode.DRAW);
        drawB.setSelected(true);
        JRadioButton moveB = new JRadioButton("move");
        moveB.addActionListener(e -> mode = Mode.MOVE);
        JRadioButton recolorB = new JRadioButton("recolor");
        recolorB.addActionListener(e -> mode = Mode.RECOLOR);
        JRadioButton deleteB = new JRadioButton("delete");
        deleteB.addActionListener(e -> mode = Mode.DELETE);
        JRadioButton frontB = new JRadioButton("send front");
        frontB.addActionListener(e -> mode = Mode.FRONT);
        JRadioButton backB = new JRadioButton("send back");
        backB.addActionListener(e -> mode = Mode.BACK);
        ButtonGroup modes = new ButtonGroup(); // make them act as radios -- only one selected
        modes.add(drawB);
        modes.add(moveB);
        modes.add(recolorB);
        modes.add(deleteB);
        modes.add(frontB);
        modes.add(backB);
        JPanel modesP = new JPanel(new GridLayout(1, 0)); // group them on the GUI
        modesP.add(drawB);
        modesP.add(moveB);
        modesP.add(recolorB);
        modesP.add(deleteB);
        modesP.add(frontB);
        modesP.add(backB);

        // Put all the stuff into a panel
        JComponent gui = new JPanel();
        gui.setLayout(new FlowLayout());
        gui.add(shapeB);
        gui.add(chooseColorB);
        gui.add(colorL);
        gui.add(modesP);
        return gui;
    }

    /**
     * Getter for the sketch instance variable
     */
    public Sketch getSketch() {
        return sketch;
    }

    /**
     * Draws all the shapes in the sketch,
     * along with the object currently being drawn in this editor (not yet part of the sketch)
     */
    public void drawSketch(Graphics g) {
        // TODO: YOUR CODE HERE
        sketch.drawAll(g);
        if (curr != null) curr.draw(g);
    }

    // Helpers for event handlers

    /**
     * Helper method for press at point
     * In drawing mode, start a new object;
     * in moving mode, (request to) start dragging if clicked in a shape;
     * in recoloring mode, (request to) change clicked shape's color
     * in deleting mode, (request to) delete clicked shape
     */
    private void handlePress(Point p) {
        // In drawing mode, start drawing a new shape
        if (mode == Editor.Mode.DRAW) {
            if (shapeType.equals("ellipse")) curr = new Ellipse(p.x, p.y, color);
            else if (shapeType.equals("rectangle")) curr = new Rectangle(p.x, p.y, color);
            else if (shapeType.equals("segment")) curr = new Segment(p.x, p.y, color);
            else if (shapeType.equals("freehand")) curr = new Polyline(p.x, p.y, color);
            drawFrom = p;
        }
        // In moving mode, start dragging if clicked in the shape
        else {
            int id = sketch.chosenID(p.x, p.y);
            if (mode == Editor.Mode.MOVE) {
            	movingId = id;
            	moveFrom = p;
            }
            // In recoloring mode, change the shape's color if clicked in it
            else if (mode == Editor.Mode.RECOLOR) {
                String message = id+"_recolor_"+color.getRGB();
                comm.send(message);
            }
            // In deleting mode, delete the shape if clicked in it
            else if (mode == Editor.Mode.DELETE) {
                String message = id+"_delete";
                comm.send(message);
            }
            // In front mode, move the selected shape to the front of the canvas if clicked in it
            else if (mode == Editor.Mode.FRONT) {
                String message = id+"_front";
                comm.send(message);
            }
            // In front mode, move the selected shape to the back of the canvas if clicked in it
            else if (mode == Editor.Mode.BACK) {
                String message = id+"_back";
                comm.send(message);
            }
        }
    }

    /**
     * Helper method for drag to new point
     * In drawing mode, update the other corner of the object;
     * in moving mode, (request to) drag the object
     */
    private void handleDrag(Point p) {
        // In drawing mode, revise the shape as it is stretched out
        if (mode == Editor.Mode.DRAW) {
            if (curr instanceof Rectangle) curr = new Rectangle(drawFrom.x, drawFrom.y, p.x, p.y, color);
            else if (curr instanceof Ellipse) curr = new Ellipse(drawFrom.x, drawFrom.y, p.x, p.y, color);
            else if (curr instanceof Polyline) {
                Segment s = new Segment(drawFrom.x, drawFrom.y, p.x, p.y, color);
                ((Polyline) curr).addSegment(s);
                drawFrom = p;
            } else if (curr instanceof Segment) curr = new Segment(drawFrom.x, drawFrom.y, p.x, p.y, color);
            repaint();
        }
        // In moving mode, shift the object and keep track of where next step is from
        if (mode == Editor.Mode.MOVE) {
            if (moveFrom != null) {
                String message = movingId+"_move_"+(p.x - moveFrom.x)+"_"+(p.y - moveFrom.y);
                comm.send(message);
                moveFrom = p;
            }
        }
    }

    /**
     * Helper method for release
     * In drawing mode, pass the add new object request on to the server;
     * in moving mode, release it
     */
    private void handleRelease() {
        if (mode == Mode.DRAW) {
            String message = "add_"+curr.toString();
            comm.send(message);
        	curr = null;
        }
        if (mode == Editor.Mode.MOVE) {
            if (moveFrom != null) {
                moveFrom = null;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Editor();
            }
        });
    }
}
