import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * A multi-segment Shape, with straight lines connecting "joint" points -- (x1,y1) to (x2,y2) to (x3,y3) ...
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2016
 * @author CBK, updated Fall 2016
 */
public class Polyline implements Shape {
	// TODO: YOUR CODE HERE
	private int x1, y1, x2, y2;		// two endpoints
	ArrayList<Segment> segments;
	Color color;

	public Polyline(int x1, int y1, int x2, int y2, Color color){
		segments = new ArrayList<>();
		this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
		this.color = color;
	}
	public Polyline(int x1, int y1, Color color){
		segments = new ArrayList<>();
		this.x1 = x1; this.y1 = y1; this.x2 = x1; this.y2 = y1;
		this.color = color;
	}
	public Polyline(ArrayList<Segment> segments, Color color){
		this.segments = segments;
		this.color = color;
	}

	public void addSegment(Segment s){
		segments.add(s);
	}
	@Override
	public void moveBy(int dx, int dy) {
		for(Segment s: segments){
			s.moveBy(dx,dy);
		}
	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public void setColor(Color color) {
		for(Segment s: segments) s.setColor(color);
	}
	
	@Override
	public boolean contains(int x, int y) {
		for(Segment s: segments){
			if(s.contains(x,y)) return true;
		}
		return false;
	}

	@Override
	public void draw(Graphics g) {
		g.setColor(color);
		for(Segment s: segments){
			s.draw(g);
		}
	}

	@Override
	public String toString() {
		return "polyline "+segments.toString()+" "+color.getRGB();
	}
}
