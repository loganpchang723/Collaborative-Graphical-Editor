import java.awt.*;
import java.util.ArrayList;

/**
 * Utility class for handling messages to and from clients and servers
 *
 * @author Logan Chang, CS10, PS6, 20F
 */
public class MessageHandler {

    /**
     * Handles messages FROM server TO client
     *
     * @param line   Message from server
     * @param sketch Sketch of the Editor message was sent to
     */
    public static void handleClient(String line, Sketch sketch) {
        //message in form: id_action_additionalProperties
        //if id is MAX INT, then it means the client didn't choose a valid shape, so handle with output message and ignore
        String[] msg = line.split("_");
        if (msg[1].equals("delete")) {
            if(Integer.parseInt(msg[0]) == Integer.MAX_VALUE){
                System.out.println("You didn't choose anything to delete");
                return;
            }
            delete(msg, sketch);
            System.out.println("Shape ID " + Integer.parseInt(msg[0]) + " was deleted");
        } else if (msg[1].equals("recolor")) {
            if(Integer.parseInt(msg[0]) == Integer.MAX_VALUE){
                System.out.println("You didn't choose anything to recolor");
                return;
            }
            recolor(msg, sketch);
            System.out.println("Shape ID " + Integer.parseInt(msg[0]) + " was recolored to " + (new Color(Integer.parseInt(msg[2])).toString()));
        } else if (msg[1].equals("move")) {
            if(Integer.parseInt(msg[0]) == Integer.MAX_VALUE){
                System.out.println("You didn't choose anything to move");
                return;
            }
            move(msg, sketch);
            System.out.println("Shape ID " + Integer.parseInt(msg[0]) + " was moved");
        } else if (msg[1].equals("front")) {
            if(Integer.parseInt(msg[0]) == Integer.MAX_VALUE){
                System.out.println("You didn't choose anything to send forward");
                return;
            }
            sendFront(msg, sketch);
            System.out.println("Shape ID " + Integer.parseInt(msg[0]) + " was sent to the front and now has ID " + (sketch.getShapes().navigableKeySet().last()));
        } else if (msg[1].equals("back")) {
            if(Integer.parseInt(msg[0]) == Integer.MAX_VALUE){
                System.out.println("You didn't choose anything to send backward");
                return;
            }
            sendBack(msg, sketch);
            System.out.println("Shape ID " + Integer.parseInt(msg[0]) + " was sent to the back and now has ID " + (sketch.getShapes().navigableKeySet().first()));
        } else if (msg[1].equals("add")) {
            if (msg[2].contains("polyline")) {
                buildPoly(msg, sketch);
                System.out.println("New freehand with ID " + Integer.parseInt(msg[0]) + " was added");
            } else {
                String[] attr = msg[2].split(" ");
                if (attr[0].equals("ellipse")) {
                    buildEllipse(msg, attr, sketch);
                    System.out.println("New ellipse with ID " + Integer.parseInt(msg[0]) + " was added");
                } else if (attr[0].equals("rectangle")) {
                    buildRectangle(msg, attr, sketch);
                    System.out.println("New rectangle with ID " + Integer.parseInt(msg[0]) + " was added");
                } else if (attr[0].equals("segment")) {
                    buildSegment(msg, attr, sketch);
                    System.out.println("New segment with ID " + Integer.parseInt(msg[0]) + " was added");
                }
            }
        } else if (msg[0].equals("all")) {
            String[] shapes = msg[1].substring(1, msg[1].length() - 1).strip().split(":");
            for (String shape : shapes) {
                String[] info = shape.strip().split("=");
                String format = info[0] + "_nothing_" + info[1];
                String[] formatted = format.split("_");
                if (formatted[2].contains("polyline")) {
                    buildPoly(formatted, sketch);
                } else {
                    String[] attr = formatted[2].split(" ");
                    if (attr[0].equals("ellipse")) buildEllipse(formatted, attr, sketch);
                    else if (attr[0].equals("rectangle")) buildRectangle(formatted, attr, sketch);
                    else if (attr[0].equals("segment")) buildSegment(formatted, attr, sketch);
                }
            }
            System.out.println("All existing shapes have been added");
        }

    }

    /**
     * Handles messages FROM client TO server
     *
     * @param line   Message from client
     * @param server SketchServer object needed to access master sketch (needed different method composition)
     */
    public static void handleServer(String line, SketchServer server) {
        //message in form: action_additionalProperties
        String[] msg = line.split("_");
        if (msg[1].equals("delete")) {
            delete(msg, server.getSketch());
        } else if (msg[1].equals("recolor")) {
            recolor(msg, server.getSketch());
        } else if (msg[1].equals("move")) {
            move(msg, server.getSketch());
        } else if (msg[1].equals("front")) {
            sendFront(msg, server.getSketch());
        } else if (msg[1].equals("back")) {
            sendBack(msg, server.getSketch());
        } else if (msg[0].equals("add")) {
            if (msg[1].contains("polyline")) {
                buildPoly(msg, server);
            } else {
                String[] attr = msg[1].split(" ");
                if (attr[0].equals("ellipse")) buildEllipse(attr, server);
                else if (attr[0].equals("rectangle")) buildRectangle(attr, server);
                else if (attr[0].equals("segment")) buildSegment(attr, server);
            }
        }

    }

    //common methods

    /**
     * Delete shape from sketch
     *
     * @param msg    Message with deletion info
     * @param sketch Sketch to delete shape from
     */
    public static void delete(String[] msg, Sketch sketch) {
        sketch.removeShape(Integer.parseInt(msg[0]));
    }

    /**
     * Recolor shape in sketch
     *
     * @param msg    Message with shape and color info
     * @param sketch Sketch to recolor shape in
     */
    public static void recolor(String[] msg, Sketch sketch) {
        sketch.recolorShape(Integer.parseInt(msg[0]), new Color(Integer.parseInt(msg[2])));
    }

    /**
     * Move shape in sketch
     *
     * @param msg    Message with shape and movement amount info
     * @param sketch Sketch to move shape in
     */
    public static void move(String[] msg, Sketch sketch) {
        sketch.moveShape(Integer.parseInt(msg[0]), Integer.parseInt(msg[2]), Integer.parseInt(msg[3]));
    }

    /**
     * Send shape to front of sketch
     *
     * @param msg    Message with shape to be sent forward info
     * @param sketch Sketch to send shape forward in
     */
    public static void sendFront(String[] msg, Sketch sketch) {
        sketch.sendFront(Integer.parseInt(msg[0]));
    }

    /**
     * Send shape to back of sketch
     *
     * @param msg    Message with shape to be sent backwards info
     * @param sketch Sketch to send shape backward in
     */
    public static void sendBack(String[] msg, Sketch sketch) {
        sketch.sendBack(Integer.parseInt(msg[0]));
    }

    //adding shapes in clients (method composition uses Sketch objects)

    /**
     * Add an ellipse to client sketch
     *
     * @param msg    Initial message to client to add ellipse
     * @param attr   Attributes of ellipse to be added
     * @param sketch Sketch to add ellipse to
     */
    public static void buildEllipse(String[] msg, String[] attr, Sketch sketch) {
        sketch.addShape(Integer.parseInt(msg[0]), new Ellipse(Integer.parseInt(attr[1]), Integer.parseInt(attr[2]), Integer.parseInt(attr[3]), Integer.parseInt(attr[4]), new Color(Integer.parseInt(attr[5]))));
    }

    /**
     * Add a rectangle to client sketch
     *
     * @param msg    Initial message to client to add rectangle
     * @param attr   Attributes of rectangle to be added
     * @param sketch Sketch to add rectangle to
     */
    public static void buildRectangle(String[] msg, String[] attr, Sketch sketch) {
        sketch.addShape(Integer.parseInt(msg[0]), new Rectangle(Integer.parseInt(attr[1]), Integer.parseInt(attr[2]), Integer.parseInt(attr[3]), Integer.parseInt(attr[4]), new Color(Integer.parseInt(attr[5]))));
    }

    /**
     * Add a segment to client sketch
     *
     * @param msg    Initial message to client to add segment
     * @param attr   Attributes of segment to be added
     * @param sketch Sketch to add segment to
     */
    public static void buildSegment(String[] msg, String[] attr, Sketch sketch) {
        sketch.addShape(Integer.parseInt(msg[0]), new Segment(Integer.parseInt(attr[1]), Integer.parseInt(attr[2]), Integer.parseInt(attr[3]), Integer.parseInt(attr[4]), new Color(Integer.parseInt(attr[5]))));
    }

    /**
     * Add a polyline to client sketch
     *
     * @param msg    Initial message to client to add polyline that contains polylines attributes
     * @param sketch Sketch to add polyline to
     */
    public static void buildPoly(String[] msg, Sketch sketch) {
        ArrayList<Segment> segments = new ArrayList<>();
        //msg has a list of segments that make up the polyline that need to be parsed
        String segmentList = msg[2].substring(msg[2].indexOf('[') + 1, msg[2].indexOf(']'));
        int RGBval = Integer.MAX_VALUE;
        if (segmentList.length() < 1) return;
        //create segments from each in the list of segments and add them to a new polyline object
        for (String seg : segmentList.split(",")) {
            String[] attr = seg.strip().split(" ");
            if (RGBval == Integer.MAX_VALUE) RGBval = (Integer.parseInt(attr[5]));
            Segment part = new Segment(Integer.parseInt(attr[1]), Integer.parseInt(attr[2]), Integer.parseInt(attr[3]), Integer.parseInt(attr[4]), new Color(RGBval));
            segments.add(part);
        }
        sketch.addShape(Integer.parseInt(msg[0]), new Polyline(segments, new Color(RGBval)));
    }

    //adding shapes in server (composition uses SketchServer for differentiation from client-side additions)

    /**
     * Add ellipse to master sketch of server
     *
     * @param attr   Attributes of the ellipse to be added
     * @param server SketchServer's sketch where ellipse will be added
     */
    public static void buildEllipse(String[] attr, SketchServer server) {
        //add shape to sketch with a new ID
        server.getSketch().addShape(server.getId(), new Ellipse(Integer.parseInt(attr[1]), Integer.parseInt(attr[2]), Integer.parseInt(attr[3]), Integer.parseInt(attr[4]), new Color(Integer.parseInt(attr[5]))));
    }

    /**
     * Add rectangleto master sketch of server
     *
     * @param attr   Attributes of the rectangle to be added
     * @param server SketchServer's sketch where rectangle will be added
     */
    public static void buildRectangle(String[] attr, SketchServer server) {
        //add shape to sketch with a new ID
        server.getSketch().addShape(server.getId(), new Rectangle(Integer.parseInt(attr[1]), Integer.parseInt(attr[2]), Integer.parseInt(attr[3]), Integer.parseInt(attr[4]), new Color(Integer.parseInt(attr[5]))));
    }

    /**
     * Add segment to master sketch of server
     *
     * @param attr   Attributes of the segment to be added
     * @param server SketchServer's sketch where segment will be added
     */
    public static void buildSegment(String[] attr, SketchServer server) {
        //add shape to sketch with a new ID
        server.getSketch().addShape(server.getId(), new Segment(Integer.parseInt(attr[1]), Integer.parseInt(attr[2]), Integer.parseInt(attr[3]), Integer.parseInt(attr[4]), new Color(Integer.parseInt(attr[5]))));
    }

    /**
     * Add polyline to master sketch of server
     *
     * @param msg    Initial message and attributes of polyline to be added
     * @param server SketchServer's sketch where segment will be added
     */
    public static void buildPoly(String[] msg, SketchServer server) {
        ArrayList<Segment> segments = new ArrayList<>();
        //msg has a list of segments that make up the polyline that need to be parsed
        String segmentList = msg[1].substring(msg[1].indexOf('[') + 1, msg[1].indexOf(']'));
        int RGBval = Integer.MAX_VALUE;
        if (segmentList.length() < 1) return;
        //create segments from each in the list of segments and add them to a new polyline object
        for (String seg : segmentList.split(",")) {
            String[] attr = seg.strip().split(" ");
            if (RGBval == Integer.MAX_VALUE) RGBval = (Integer.parseInt(attr[5]));
            Segment part = new Segment(Integer.parseInt(attr[1]), Integer.parseInt(attr[2]), Integer.parseInt(attr[3]), Integer.parseInt(attr[4]), new Color(RGBval));
            segments.add(part);
        }
        //add shape to sketch with a new ID
        server.getSketch().addShape(server.getId(), new Polyline(segments, new Color(RGBval)));
    }


}
