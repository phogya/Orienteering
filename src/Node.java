/**
 * Class Node is a helper class for Orienteering. Each Node
 * contains information regarding the traversal of an acre.
 *
 * @author  Peter Hogya
 * @version 26-Oct-2017
 */
public class Node { 

		int x;
		int y;
		double g;
		double h;
		double f;
		Node parent;
		
		public Node(int x, int y, double g, double h, Node parent) {
			this.x = x;
			this.y = y;
			this.g = g;
			this.h = h;
			this.f = g + h;
			this.parent = parent;
		}
}