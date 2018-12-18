/**
 * Class Orienteering uses an A* search algorithm to traverse an environment 
 * consisting of varying terrains and elevations in order to reach a set of
 * destinations. These terrains, elevations, and destinations are detailed in
 * the various input files. This program creates two output files, path.png
 * and directions.txt, that detail the solution found.
 *
 * Usage: java Orienteering terrain elevation course season
 *
 * @author  Peter Hogya
 * @version 26-Oct-2017
 */
import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.awt.image.BufferedImage;

public class Orienteering {    

	/*
	 * Main function
	 */
    public static void main( String[] args ){
    	
    	// Argument checks
    	if(args.length != 4) {
    		System.err.println("Incorrect number of arguments");
    		usage();
    	}
    	
    	// Get Terrain file
    	BufferedImage terrain = getImage(args[0]);
    	
    	// Check for seasonal changes
    	if(args[3].compareToIgnoreCase("fall") == 0) {
    		fallTerrain(terrain);
    	}
    	if(args[3].compareToIgnoreCase("winter") == 0) {
    		winterTerrain(terrain);
    	}
    	
    	// Parse elevation file
    	String elevation = null;
    	try {
			elevation = new String(Files.readAllBytes(Paths.get((Orienteering.class.getResource(args[1]).toURI()))));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
    	
    	String[] elArray = elevation.substring(3, elevation.length()).split("\\s+");
    	
    	String[][] elArray2 = new String[400][500];
    	for(int i=0; i<500; i++) {
    		for(int j=0; j<400; j++) {
    			elArray2[j][i] = elArray[j + (i*400)];
    		}
    	}
    	
    	// Parse course file
    	String course = null;
    	
    	try {
			course = new String(Files.readAllBytes(Paths.get((Orienteering.class.getResource(args[2]).toURI()))));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
    	
    	String[] cArray = course.split("\\s+");
    	
    	// Run A* algorithm
    	ArrayList<Node> path = new ArrayList<Node>();
    	
    	for(int i=0; i<(cArray.length-2); i+=2) {
    		path.addAll(aStarSearch(elArray2, terrain, Integer.valueOf(cArray[i]), Integer.valueOf(cArray[i+1]), 
    															Integer.valueOf(cArray[i+2]), Integer.valueOf(cArray[i+3])));
    	} 
    	
    	for(int i=path.size()-1; i>=0; i--) {
    		terrain.setRGB(path.get(i).x, path.get(i).y, -3342235);
    	} 
    	
    	// Produce output files
    	getDirections(path);
    	
    	File outputfile = new File("path.png");
    	try {
			ImageIO.write(terrain, "PNG", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    /*
     * Performs an A* search from the start position at x1, y1 to the goal position gx, gy.
     */
    public static ArrayList<Node> aStarSearch(String[][] elArray, BufferedImage terrain, int x1, int y1, int gx, int gy) {

    	ArrayList<Node> openList = new ArrayList<Node>();
    	ArrayList<Node> closedList = new ArrayList<Node>();
    	
    	// Add start node to list
    	Node start = new Node(x1,y1,0,0,null);
    	openList.add(start);
    	
    	// TravelTime placeholder and Node placeholder
    	double t;
    	Node temp;
    	// Variable to keep track of nodes in lists
    	int p = 0;
    	// Node to hold goal node
    	Node goal = null;
    	while( openList.isEmpty() != true ) {
    		
    		// Get smallest f
    		Node q = openList.get(0);
    		for(int i=1; i<openList.size(); i++) {
    			if(openList.get(i).f < q.f) {
    				q = openList.get(i);
    			}
    		}
    		
    		// Remove q
    		openList.remove(q);
    		
    		// Generate successors with OOB checks and impassable terrain checks
    		// N
    		if(q.y <= 498) {
    			t = travelTime(elArray,terrain,q.x,q.y,q.x,q.y+1);
    			if(t != -1) {
    				temp = new Node(q.x, q.y+1, q.g + t, minTTime(q.x,q.y+1,gx,gy),q);
    				
    				// Check for goal node
    				if( temp.x == gx && temp.y == gy){
    					goal = temp;
    					break;
    				}
    				
    				// Check if already in closedList
    				for(int i=0; i<closedList.size(); i++) {
    					if(closedList.get(i).x == temp.x && closedList.get(i).y == temp.y && closedList.get(i).f < temp.f) {
    						p = 1;
    					}
    				}
    				// If not in closed list check if in openList
    				if(p != 1){
    					for(int i=0; i<openList.size(); i++) {
        					if(openList.get(i).x == temp.x && openList.get(i).y == temp.y) {
        						if(openList.get(i).f < temp.f) {
        							p = 1;
        						}  
        					}
        				}
    				}
    				// If not in closedList and there isn't a better one in openList add to openList
    				if(p != 1){
    					openList.add(temp);
    				}
    				p = 0;
    			}
    		}
    		// S
    		if(q.y >= 1) {
    			t = travelTime(elArray,terrain,q.x,q.y,q.x,q.y-1);
    			if(t != -1) {
    				temp = new Node(q.x, q.y-1, q.g + t, minTTime(q.x,q.y-1,gx,gy),q);
    				
    				// Check for goal node
    				if( temp.x == gx && temp.y == gy){
    					goal = temp;
    					break;
    				}
    				
    				// Check if already in closedList
    				for(int i=0; i<closedList.size(); i++) {
    					if(closedList.get(i).x == temp.x && closedList.get(i).y == temp.y && closedList.get(i).f < temp.f) {
    						p = 1;
    					}
    				}
    				// If not in closed list check if in openList
    				if(p != 1){
    					for(int i=0; i<openList.size(); i++) {
        					if(openList.get(i).x == temp.x && openList.get(i).y == temp.y) {
        						if(openList.get(i).f < temp.f) {
        							p = 1;
        						} 
        					}
        				}
    				}
    				// If not in closedList and there isn't a better one in openList add to openList
    				if(p != 1){
    					openList.add(temp);
    				}
    				p = 0;
    			}
    		}
    		// E
    		if(q.x <= 393) {
    			t = travelTime(elArray,terrain,q.x,q.y,q.x+1,q.y);
    			if(t != -1) {
    				temp = new Node(q.x+1, q.y, q.g + t, minTTime(q.x+1,q.y,gx,gy),q);
    				
    				// Check for goal node
    				if( temp.x == gx && temp.y == gy){
    					goal = temp;
    					break;
    				}
    				
    				// Check if already in closedList
    				for(int i=0; i<closedList.size(); i++) {
    					if(closedList.get(i).x == temp.x && closedList.get(i).y == temp.y && closedList.get(i).f < temp.f) {
    						p = 1;
    					}
    				}
    				// If not in closed list check if in openList
    				if(p != 1){
    					for(int i=0; i<openList.size(); i++) {
        					if(openList.get(i).x == temp.x && openList.get(i).y == temp.y) {
        						if(openList.get(i).f < temp.f) {
        							p = 1;
        						}
        					}
        				}
    				}
    				// If not in closedList and there isn't a better one in openList add to openList
    				if(p != 1){
    					openList.add(temp);
    				}
    				p = 0;
    			}
    		}
    		// W
    		if(q.x >= 1) {
    			t = travelTime(elArray,terrain,q.x,q.y,q.x-1,q.y);
    			if(t != -1) {
    				temp = new Node(q.x-1, q.y, q.g + t, minTTime(q.x-1,q.y,gx,gy),q);
    				
    				// Check for goal node
    				if( temp.x == gx && temp.y == gy){
    					goal = temp;
    					break;
    				}
    				
    				// Check if already in closedList
    				for(int i=0; i<closedList.size(); i++) {
    					if(closedList.get(i).x == temp.x && closedList.get(i).y == temp.y && closedList.get(i).f < temp.f) {
    						p = 1;
    					}
    				}
    				// If not in closed list check if in openList
    				if(p != 1){
    					for(int i=0; i<openList.size(); i++) {
        					if(openList.get(i).x == temp.x && openList.get(i).y == temp.y) {
        						if(openList.get(i).f < temp.f) {
        							p = 1;
        						} 
        					}
        				}
    				}
    				// If not in closedList and there isn't a better one in openList add to openList
    				if(p != 1){
    					openList.add(temp);
    				}
    				p = 0;
    			}
    		}
    		// NE
    		if(q.x <= 393 && q.y <= 498) {
    			t = travelTime(elArray,terrain,q.x,q.y,q.x+1,q.y+1);
    			if(t != -1) {
    				temp = new Node(q.x+1, q.y+1, q.g + t, minTTime(q.x+1,q.y+1,gx,gy),q);
    				
    				// Check for goal node
    				if( temp.x == gx && temp.y == gy){
    					goal = temp;
    					break;
    				}
    				
    				// Check if already in closedList
    				for(int i=0; i<closedList.size(); i++) {
    					if(closedList.get(i).x == temp.x && closedList.get(i).y == temp.y && closedList.get(i).f < temp.f) {
    						p = 1;
    					}
    				}
    				// If not in closed list check if in openList
    				if(p != 1){
    					for(int i=0; i<openList.size(); i++) {
        					if(openList.get(i).x == temp.x && openList.get(i).y == temp.y) {
        						if(openList.get(i).f < temp.f) {
        							p = 1;
        						} 
        					}
        				}
    				}
    				// If not in closedList and there isn't a better one in openList add to openList
    				if(p != 1){
    					openList.add(temp);
    				}
    				p = 0;
    			}
    		}
    		// NW
    		if(q.x >= 1 && q.y <= 498) {
    			t = travelTime(elArray,terrain,q.x,q.y,q.x-1,q.y+1);
    			if(t != -1) {
    				temp = new Node(q.x-1, q.y+1, q.g + t, minTTime(q.x-1,q.y+1,gx,gy),q);
    				
    				// Check for goal node
    				if( temp.x == gx && temp.y == gy){
    					goal = temp;
    					break;
    				}
    				
    				// Check if already in closedList
    				for(int i=0; i<closedList.size(); i++) {
    					if(closedList.get(i).x == temp.x && closedList.get(i).y == temp.y && closedList.get(i).f < temp.f) {
    						p = 1;
    					}
    				}
    				// If not in closed list check if in openList
    				if(p != 1){
    					for(int i=0; i<openList.size(); i++) {
        					if(openList.get(i).x == temp.x && openList.get(i).y == temp.y) {
        						if(openList.get(i).f < temp.f) {
        							p = 1;
        						} 
        					}
        				}
    				}
    				// If not in closedList and there isn't a better one in openList add to openList
    				if(p != 1){
    					openList.add(temp);
    				}
    				p = 0;
    			}
    		}
    		// SE
    		if(q.x <= 393 && q.y >= 1) {
    			t = travelTime(elArray,terrain,q.x,q.y,q.x+1,q.y-1);
    			if(t != -1) {
    				temp = new Node(q.x+1, q.y-1, q.g + t, minTTime(q.x+1,q.y-1,gx,gy),q);
    				
    				// Check for goal node
    				if( temp.x == gx && temp.y == gy){
    					goal = temp;
    					break;
    				}
    				
    				// Check if already in closedList
    				for(int i=0; i<closedList.size(); i++) {
    					if(closedList.get(i).x == temp.x && closedList.get(i).y == temp.y && closedList.get(i).f < temp.f) {
    						p = 1;
    					}
    				}
    				// If not in closed list check if in openList
    				if(p != 1){
    					for(int i=0; i<openList.size(); i++) {
        					if(openList.get(i).x == temp.x && openList.get(i).y == temp.y) {
        						if(openList.get(i).f < temp.f) {
        							p = 1;
        						} 
        					}
        				}
    				}
    				// If not in closedList and there isn't a better one in openList add to openList
    				if(p != 1){
    					openList.add(temp);
    				}
    				p = 0;
    			}
    		}
    		// SW
    		if(q.x >= 1 && q.y >= 1) {
    			t = travelTime(elArray,terrain,q.x,q.y,q.x-1,q.y-1);
    			if(t != -1) {
    				temp = new Node(q.x-1, q.y-1, q.g + t, minTTime(q.x-1,q.y-1,gx,gy),q);
    				
    				// Check for goal node
    				if( temp.x == gx && temp.y == gy){
    					goal = temp;
    					break;
    				}
    				
    				// Check if already in closedList
    				for(int i=0; i<closedList.size(); i++) {
    					if(closedList.get(i).x == temp.x && closedList.get(i).y == temp.y && closedList.get(i).f < temp.f) {
    						p = 1;
    					}
    				}
    				// If not in closed list check if in openList
    				if(p != 1){
    					for(int i=0; i<openList.size(); i++) {
        					if(openList.get(i).x == temp.x && openList.get(i).y == temp.y) {
        						if(openList.get(i).f < temp.f) {
        							p = 1;
        						} 
        					}
        				}
    				}
    				// If not in closedList and there isn't a better one in openList add to openList
    				if(p != 1){
    					openList.add(temp);
    				}
    				p = 0;
    			}
    		}
    		closedList.add(q);
    	}
    	
    	ArrayList<Node> path = new ArrayList<Node>();
    	while(goal != null) {
    		path.add(goal);
    		goal = goal.parent;
    	}
    	return path;
    }
    
    /*
     * Heuristic function to estimate the cost of a path
     */
    public static double minTTime(int x1, int y1, int x2, int y2){
    	
    	// Find the distance from x1,y1 to x2,y2 using trig and weighting acres
    	double dx = Math.sqrt( Math.pow(Math.abs(x1-x2) * 10.29, 2) + Math.pow(Math.abs(y1-y2) * 7.55,2) );
    	
    	// The max walking speed using Tobler's Hiking function
    	double w = 6;
    	
    	// Return travel time as walking speed over distance
    	return dx / w;
    }
    
    /*
     * Cost function to calculate actual cost of travel from one node to an adjacent node
     */
    public static double travelTime(String[][] elArray, BufferedImage terrain, int x1, int y1, int x2, int y2) {
    	
    	// Checks for x1,y1 terrain that cannot be traversed to skip unnecessary calculations
			// Terrain is Out of Bounds
    	if(terrain.getRGB(x1,y1) == -3342235) {
    		return -1;
    	}
			// Terrain is Swamp/Lake/Marsh
    	if(terrain.getRGB(x1, y1) == -16776961) {
    		return -1;
    	}
			// Terrain is Impassable Vegetation
    	if(terrain.getRGB(x1, y1) == -16430824) {
    		return -1;
    	}
    	
    	// Checks for x2,y2 terrain that cannot be traversed to skip unnecessary calculations
    		// Terrain is Out of Bounds
    	if(terrain.getRGB(x2,y2) == -3342235) {
    		return -1;
    	}
    		// Terrain is Swamp/Lake/Marsh
    	if(terrain.getRGB(x2, y2) == -16776961) {
    		return -1;
    	}
    		// Terrain is Impassable Vegetation
    	if(terrain.getRGB(x2, y2) == -16430824) {
    		return -1;
    	}
    	
    	double dh = 0; // Elevation difference
    	double dx = 0; // Distance
    	double tm1 = 0; // First terrain multiplier
    	double tm2 = 0; // Second terrain multiplier
    	
    	// Get distance based on direction moving and acre size
    		// Moving North or South
    	if(x1 == x2 && y1 != y2) {
    		dx = 7.55;
    	}
    		// Moving East or West
    	else if (x1 != x2 && y1 == y2) {
    		dx = 10.29;
    	}
    		// Moving in an intermediate direction
    	else {
    		dx = 12.763;
    	}
    	
    	// Get elevation difference
    	dh = 100 * Math.abs((Double.parseDouble(elArray[x1][y1].substring(0,9))) - 
    			(Double.parseDouble(elArray[x2][y2].substring(0,9))));
    	
    	// Get first terrain multiplier
    		// Terrain is open land
    	if(terrain.getRGB(x1, y1) == -486382) {
    		tm1 = 1;
    	} 
    		// Terrain is Rough Meadow
    	else if(terrain.getRGB(x1, y1) == -16384) {
    		tm1 = 0.6;
    	}
    		// Terrain is Easy Movement Forest
    	else if(terrain.getRGB(x1, y1) == -1) {
    		tm1 = 0.8;
    	}
    		// Terrain is Slow Run Forest
    	else if(terrain.getRGB(x1, y1) == -16592836) {
    		tm1 = 0.7;
    	}
    		// Terrain is Walk Forest
    	else if(terrain.getRGB(x1, y1) == -16611288) {
    		tm1 = 0.6;
    	}
    		// Terrain is Paved Road
    	else if(terrain.getRGB(x1, y1) == -12111101) {
    		tm1 = 1;
    	}
    		// Terrain is Footpath
    	else if(terrain.getRGB(x1, y1) == -16777216 || terrain.getRGB(x1, y1) == 0){
    		tm1 = 1;
    	}
    		// Terrain is Leaves
    	else if(terrain.getRGB(x1, y1) == -16777215){
    		tm1 = 0.9;
    	}
    		// Terrain is Walkable Ice
    	else if(terrain.getRGB(x1, y1) == -16776962){
    		tm1 = 0.5;
    	}
    	
    	// Get second terrain multiplier
			// Terrain is open land
    	if(terrain.getRGB(x2, y2) == -486382) {
    		tm2 = 1;
    	} 
			// Terrain is Rough Meadow
    	else if(terrain.getRGB(x2, y2) == -16384) {
    		tm2 = 0.6;
    	}
			// Terrain is Easy Movement Forest
    	else if(terrain.getRGB(x2, y2) == -1) {
    		tm2 = 0.8;
    	}
			// Terrain is Slow Run Forest
    	else if(terrain.getRGB(x2, y2) == -16592836) {
    		tm2 = 0.7;
    	}
			// Terrain is Walk Forest
    	else if(terrain.getRGB(x2, y2) == -16611288) {
    		tm2 = 0.6;
    	}
			// Terrain is Paved Road
    	else if(terrain.getRGB(x2, y2) == -12111101) {
    		tm2 = 1;
    	}
			// Terrain is Footpath
    	else if(terrain.getRGB(x2, y2) == -16777216 || terrain.getRGB(x2, y2) == 0){
    		tm2 = 1;
    	}
    		// Terrain is Leaves
    	else if(terrain.getRGB(x2, y2) == -16777215){
    		tm2 = 0.9;
    	}
    		// Terrain is Walkable Ice
    	else if(terrain.getRGB(x2, y2) == -16776962){
    		tm2 = 0.5;
    	}
    	
    	// Walking speed based on Tobler's Hiking Function
    	double w = 6*Math.exp(-3.5 * (Math.abs(dh/dx) + 0.05 ));
    	
    	// Total travel time = time to leave current acre + time to get the center of next acre
    	double t = (((dx/2) / w) * tm1) + (((dx/2) / w) * tm2);
    		
		return t;
    }
    
    /*
     * Function to get terrain image data
     */
    public static BufferedImage getImage(String file) {
    	BufferedImage img = null;
    	try {
    	    img = ImageIO.read(new File(Orienteering.class.getResource(file).toURI()));
    	} catch (IOException e) {
    		System.err.println(e);
    	} catch (URISyntaxException e) {
			e.printStackTrace();
		}
    	return img;
    }
    
    /*
     * Outputs results to text file
     */
    public static void getDirections(ArrayList<Node> path) {
    	
    	PrintWriter writer = null;
		try {
			writer = new PrintWriter("directions.txt", "UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	writer.println("Directions:");
    	
    	String heading = null;
    	int acres = 0;
    	
    	for(int i=0; i<path.size()-1; i++) {
    		
    		// Heading North
    		if(path.get(i).x == path.get(i+1).x && path.get(i).y+1 == path.get(i+1).y) {
    			if(heading != "North"){
    				heading = "North";
    				if(acres != 0) {
    					writer.println(acres + " acres.");
    				}
    				writer.print("Go North for ");
    				acres = 1;
    			} else {
    				acres += 1;
    			}
    		}
    		// Heading South
    		if(path.get(i).x == path.get(i+1).x && path.get(i).y-1 == path.get(i+1).y) {
    			if(heading != "South"){
    				heading = "South";
    				if(acres != 0) {
    					writer.println(acres + " acres.");
    				}
    				writer.print("Go South for ");
    				acres = 1;
    			} else {
    				acres += 1;
    			}
    		}
    		// Heading East
    		if(path.get(i).x+1 == path.get(i+1).x && path.get(i).y == path.get(i+1).y) {
    			if(heading != "East"){
    				heading = "East";
    				if(acres != 0) {
    					writer.println(acres + " acres.");
    				}
    				writer.print("Go East for ");
    				acres = 1;
    			} else {
    				acres += 1;
    			}
    		}
    		// Heading West
    		if(path.get(i).x-1 == path.get(i+1).x && path.get(i).y == path.get(i+1).y) {
    			if(heading != "West"){
    				heading = "West";
    				if(acres != 0) {
    					writer.println(acres + " acres.");
    				}
    				writer.print("Go West for ");
    				acres = 1;
    			} else {
    				acres += 1;
    			}
    		}
    		// Heading Northeast
    		if(path.get(i).x+1 == path.get(i+1).x && path.get(i).y+1 == path.get(i+1).y) {
    			if(heading != "Northeast"){
    				heading = "Northeast";
    				if(acres != 0) {
    					writer.println(acres + " acres.");
    				}
    				writer.print("Go Northeast for ");
    				acres = 1;
    			} else {
    				acres += 1;
    			}
    		}
    		// Heading Northwest
    		if(path.get(i).x-1 == path.get(i+1).x && path.get(i).y+1 == path.get(i+1).y) {
    			if(heading != "Northwest"){
    				heading = "Northwest";
    				if(acres != 0) {
    					writer.println(acres + " acres.");
    				}
    				writer.print("Go Northwest for ");
    				acres = 1;
    			} else {
    				acres += 1;
    			}
    		}
    		// Heading Southeast
    		if(path.get(i).x+1 == path.get(i+1).x && path.get(i).y-1 == path.get(i+1).y) {
    			if(heading != "Southeast"){
    				heading = "Southeast";
    				if(acres != 0) {
    					writer.println(acres + " acres.");
    				}
    				writer.print("Go Southeast for ");
    				acres = 1;
    			} else {
    				acres += 1;
    			}
    		}
    		// Heading Southwest
    		if(path.get(i).x-1 == path.get(i+1).x && path.get(i).y-1 == path.get(i+1).y) {
    			if(heading != "Southwest"){
    				heading = "Southwest";
    				if(acres != 0) {
    					writer.println(acres + " acres.");
    				}
    				writer.print("Go Southwest for ");
    				acres = 1;
    			} else {
    				acres += 1;
    			}
    		}
    	}
    	writer.println(acres + " acres.");
    	writer.close();
    }
    
    /*
     * Adjusts terrain for fall weather
     */
    public static void fallTerrain(BufferedImage terrain) {
    	
    	// Iterate through terrain
    	for(int x=0; x<395; x++) {
    		for(int y=0; y<500; y++) {
    			
    			// Find footpaths
    			if(terrain.getRGB(x, y) == -16777216 || terrain.getRGB(x, y) == 0) {
    				
    				// Check adjacent acres for boundaries and trees, if adjacent trees then change terrain to leaves
    				if(y-1 >= 0) {
    					if(terrain.getRGB(x, y-1) == -1) {
    						terrain.setRGB(x, y, -16777215);
    					}
    				}
    				if(y+1 < 500) {
    					if(terrain.getRGB(x, y+1) == -1) {
    						terrain.setRGB(x, y, -16777215);
    					}
    				}
    				if(x-1 >= 0) {
    					if(terrain.getRGB(x-1, y) == -1) {
    						terrain.setRGB(x, y, -16777215);
    					}
    				}
    				if(x+1 < 395) {
    					if(terrain.getRGB(x+1, y) == -1) {
    						terrain.setRGB(x, y, -16777215);
    					}
    				}
    			}
    		}
    	}
    }
    
    /*
     * Adjusts terrain for winter weather
     */
    public static void winterTerrain(BufferedImage terrain) {
    	// Iterate through terrain
    	for(int x=0; x<395; x++) {
    		for(int y=0; y<500; y++) {
    			
    			// Check if terrain is not water or OOB
    			if(terrain.getRGB(x, y) != -16776961 && terrain.getRGB(x, y) != -3342235) {
    				
    				// Check adjacent acres for boundaries and water, if adjacent water then pass to freeze function
    				if(y-1 >= 0) {
    					if(terrain.getRGB(x, y-1) == -16776961) {
    						winterFreezeTerrain(terrain, x, y, 'S');
    					}
    				}
    				if(y+1 < 500) {
    					if(terrain.getRGB(x, y+1) == -16776961) {
    						winterFreezeTerrain(terrain, x, y, 'N');
    					}
    				}
    				if(x-1 >= 0) {
    					if(terrain.getRGB(x-1, y) == -16776961) {
    						winterFreezeTerrain(terrain, x, y, 'W');
    					}
    				}
    				if(x+1 < 395) {
    					if(terrain.getRGB(x+1, y) == -16776961) {
    						winterFreezeTerrain(terrain, x, y, 'E');
    					}
    				}
    			}
    		}
    	}
    }
    
    /*
     * Helper function to adjust terrain for winter weather
     */
    public static void winterFreezeTerrain(BufferedImage terrain, int x, int y, char direction) {
    	
    	// Northward Freeze
    	if(direction == 'N') {
    		for(int i=1; i<=7; i++) {
    			// Boundary Check
    			if(y+i < 500) {
    				// Check for water
    				if(terrain.getRGB(x, y+i) == -16776961) {
    					// Set terrain to walkable ice
    					terrain.setRGB(x, y+i, -16776962);
    				}
    			}
    		}
    	}
    	
    	// Southward Freeze
    	if(direction == 'S') {
    		for(int i=1; i<=7; i++) {
    			// Boundary Check
    			if(y-i < 500) {
    				// Check for water
    				if(terrain.getRGB(x, y-i) == -16776961) {
    					// Set terrain to walkable ice
    					terrain.setRGB(x, y-i, -16776962);
    				}
    			}
    		}
    	}
    	
    	// Eastward Freeze
    	if(direction == 'E') {
    		for(int i=1; i<=7; i++) {
    			// Boundary Check
    			if(x+i < 500) {
    				// Check for water
    				if(terrain.getRGB(x+i, y) == -16776961) {
    					// Set terrain to walkable ice
    					terrain.setRGB(x+i, y, -16776962);
    				}
    			}
    		}
    	}
    	
    	// Westward Freeze
    	if(direction == 'W') {
    		for(int i=1; i<=7; i++) {
    			// Boundary Check
    			if(x-i < 500) {
    				// Check for water
    				if(terrain.getRGB(x-i, y) == -16776961) {
    					// Set terrain to walkable ice
    					terrain.setRGB(x-i, y, -16776962);
    				}
    			}
    		}
    	}
    }
    
    /*
     * Prints usage message and exits
     */
    public static void usage(){
    	System.err.println("Usage: Orienteering terrain elevation course season");
    	System.err.println("Arguments terrain, elevation, and course should be file names including their extensions and season should be "
    			+ "a string for a season, summer, fall...");
    	System.exit(1);
    }
}
