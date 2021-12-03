import java.awt.*;
import java.util.TreeMap;

/**
 * Sketch object to hold all of the shapes currently being acted upon (both client and server side)
 *
 * @author Logan Chang, CS10, PS6, 20F
 * @author Ashna Kumar, CS10, PS6, 20F
 */
public class Sketch {
    TreeMap<Integer, Shape> shapes; //Map of id -> shape

    /**
     * Constructs a sketch object
     */
    public Sketch(){
        shapes = new TreeMap<>();
    }

    /**
     * Gets all the shapes in the current sketch
     * @return  All the shapes in id -> shape from in a TreeMap
     */
    public synchronized TreeMap<Integer, Shape> getShapes() {
        return shapes;
    }

    /**
     * Adds a shape to sketch
     * @param id    Id of shape to be added
     * @param s     Shape to be added
     */
    public synchronized void addShape(int id, Shape s){
        shapes.put(id, s);
    }

    /**
     * Removes a shape to sketch
     * @param id    Id of shape to be removed
     */
    public synchronized void removeShape(int id){
        if(id == Integer.MAX_VALUE){
            System.out.println("You didn't choose anything to remove");
            return;
        }
        shapes.remove(id);
    }

    /**
     * Recolor a shape
     * @param id        Id of shape to be recolored
     * @param color     Color to change shape to
     */
    public synchronized void recolorShape(int id, Color color){
        if(id == Integer.MAX_VALUE){
            System.out.println("You didn't choose anything to recolor");
            return;
        }
        shapes.get(id).setColor(color);
    }

    /**
     * Get the ID of a clicked shape
     * @param x x-pos of click
     * @param y y-pos of click
     * @return  Id of clicked shape, closest to "top" (most recently added)
     */
    public synchronized int chosenID(int x, int y){
        int res = Integer.MAX_VALUE;
        for(int id: shapes.descendingKeySet()){
            if(shapes.get(id).contains(x,y)) {
                res = id;
                break;
            }
        }
        return res;
    }

    /**
     * Moves the selected shape in the sketch
     * @param id    Id of shape to move
     * @param dx    Amount to move shape in x-dir
     * @param dy    Amount to move shape in y-dir
     */
    public synchronized void moveShape(int id,int dx, int dy){
        if(id == Integer.MAX_VALUE){
            System.out.println("You didn't choose anything to move");
            return;
        }
        shapes.get(id).moveBy(dx,dy);
    }

    /**
     * Draw all shapes currently in sketch
     * @param g Graphics object
     */
    public synchronized void drawAll(Graphics g){
        for(int id: shapes.navigableKeySet()) shapes.get(id).draw(g);
    }

    /**
     * Send the chosen shape to the front of the sketch
     * @param id    Id of shape to move to the front
     */
    public synchronized void sendFront(int id){
        //give the shape the biggest id currently in the sketch + 1
        if(id == Integer.MAX_VALUE){
            System.out.println("You didn't choose anything to send forward");
            return;
        }
        int biggestID = shapes.navigableKeySet().last();
        shapes.put(biggestID+1, shapes.get(id));
        shapes.remove(id);
    }

    /**
     * Send the selected shape to the back
     * @param id    ID of shape to be sent back
     */
    public synchronized void sendBack(int id){
        //give the shape the smallest id currently in the sketch - 1
        if(id == Integer.MAX_VALUE){
            System.out.println("You didn't choose anything to send backward");
            return;
        }
        int smallestID = shapes.navigableKeySet().first();
        shapes.put(smallestID-1,shapes.get(id));
        shapes.remove(id);
    }

    /**
     * String representation of sketch
     * @return  All shapes in sketch "id=shape.toString" format
     */
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        for(int id: shapes.navigableKeySet()){
            sb.append(id+"="+shapes.get(id).toString());
            sb.append(":");
        }
        sb.delete(sb.length()-1, sb.length());
        sb.append("}");
        return sb.toString();
    }
}
