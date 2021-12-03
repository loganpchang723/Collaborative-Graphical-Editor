import java.net.*;
import java.util.*;
import java.io.*;

/**
 * A server to handle sketches: getting requests from the clients,
 * updating the overall state, and passing them on to the clients
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; revised Winter 2014 to separate SketchServerCommunicator
 * @author Logan Chang, CS10, PS6, 20F
 * @author Ashna Kumar, CS10, PS6, 20F
 */
public class SketchServer {
    private ServerSocket listen;                        // for accepting connections
    private ArrayList<SketchServerCommunicator> comms;    // all the connections with clients
    private Sketch sketch;                                // the state of the world
    private int id;

    public SketchServer(ServerSocket listen) {
        this.listen = listen;
        sketch = new Sketch();
        comms = new ArrayList<SketchServerCommunicator>();
        id = 0;
    }

    /**
     * Returns current ID that the server is on to distribute to each newly-added shape
     *
     * @return ID of newly-added shape, as an int
     */
    public int getId() {
        if (sketch.getShapes().size() == 0) id = 0;
        else id = (sketch.getShapes().navigableKeySet().last() + 1);
        return id;
    }

    public Sketch getSketch() {
        return sketch;
    }

    /**
     * The usual loop of accepting connections and firing off new threads to handle them
     */
    public void getConnections() throws IOException {
        System.out.println("server ready for connections");
        while (true) {
            SketchServerCommunicator comm = new SketchServerCommunicator(listen.accept(), this);
            comm.setDaemon(true);
            comm.start();
            addCommunicator(comm);
        }
    }

    /**
     * Adds the communicator to the list of current communicators
     */
    public synchronized void addCommunicator(SketchServerCommunicator comm) {
        comms.add(comm);
    }

    /**
     * Removes the communicator from the list of current communicators
     */
    public synchronized void removeCommunicator(SketchServerCommunicator comm) {
        comms.remove(comm);
    }

    /**
     * Sends the message from the one communicator to all (including the originator)
     */
    public synchronized void broadcast(String msg) {
        System.out.println("Message: "+msg);
        if (msg.split("_")[0].equals("add")) {
            msg = id + "_" + msg;
        }
        for (SketchServerCommunicator comm : comms) {
            comm.send(msg);
        }
    }

    public static void main(String[] args) throws Exception {
        new SketchServer(new ServerSocket(4242)).getConnections();
    }
}
