package linkedgraph;

import graph.Graph;

// file input/output
import java.io.File;
import java.util.Scanner;
import java.io.IOException;

// datastructures
import java.util.List;
// could probably replace all instances of arraylist with linked list
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.PriorityQueue;

/**
 * This object implements the Graph interface designed by Tyler. Additional
 * methods such as addEdge or load have been added on to this implementation to
 * assist the developer.
 *
 * @author aromualdo
 */
public class LinkedGraph implements Graph {

	// set this to false to hide hashId when printing.
	private final boolean SHOW_MEMORY = false;
	/**
	 * The list of all the vertices in the graph.
	 */
	private Node[] NODES;
	/**
	 * The list of all the edges in the graph.
	 */
	private ArrayList<ArrayList<Integer>> MATRIX;
	/**
	 * The list of all the edges in the graph.
	 */
	private ArrayList<ArrayList<Integer>> ORIGINAL_MATRIX;
	/**
	 * The current size of the graph, after all the merges.
	 */
	private int SIZE;
	/**
	 * The maximum size of the graph, or rather, the original size of the graph.
	 */
	private int MAX_SIZE;

	/**
	 * Private constructor that constructs the required data for an empty
	 * LinkedGraph, such as the size, adjacency list, default nodes.
	 *
	 * @param size Integer value of the size of the file
	 */
	private LinkedGraph(int size) {
		this.SIZE = size;
		this.MAX_SIZE = size;
		this.MATRIX = new ArrayList<ArrayList<Integer>>(size);
		this.ORIGINAL_MATRIX = new ArrayList<ArrayList<Integer>>(size);
		this.NODES = new Node[size];
		for (int i = 0; i < this.NODES.length; i++) {
			// populate nodes with default references
			this.NODES[i] = new Node(i);
			// populate adjacency list with empty lists
			this.MATRIX.add(new ArrayList<Integer>());
			this.ORIGINAL_MATRIX.add(new ArrayList<Integer>());
		}
	}

	/**
	 * Getter method for MAX SIZE
	 */
	public int getSize() {
		return this.MAX_SIZE;
	}

	/**
	 * Getter method for SIZE
	 */
	public int getCurrentSize() {
		return this.SIZE;
	}

	/**
	 * Manually add in edges, primarily for testing smaller data sets.
	 *
	 * @param from Integer value of the index of the first node
	 * @param to Integer value of the index of the second node
	 */
	public void addEdge(int from, int to) {
		int aFrom = this.NODES[from].getId();
		int aTo = this.NODES[to].getId();
		// if the edge is not already in the adjacency list, add it
		if (!this.MATRIX.get(aFrom).contains(aTo)) {
			this.MATRIX.get(aFrom).add(aTo);
			this.ORIGINAL_MATRIX.get(aFrom).add(aTo);
		}
		if (!this.MATRIX.get(aTo).contains(aFrom)) {
			this.MATRIX.get(aTo).add(aFrom);
			this.ORIGINAL_MATRIX.get(aTo).add(aFrom);
		}
	}

	/**
	 * Loads and returns a LinkedGraph with an existing list of edges.
	 *
	 * @param m the list of edges in 2D ArrayList form
	 * @return
	 */
	public static LinkedGraph load(ArrayList<ArrayList<Integer>> m) {
		LinkedGraph other = new LinkedGraph(m.size());
		// populate the adjacency matrix/list
		for (int i = 0; i < other.MAX_SIZE; i++) {
			other.MATRIX.set(i, new ArrayList<Integer>(m.get(i)));
			other.ORIGINAL_MATRIX.set(i, new ArrayList<Integer>(m.get(i)));
		}
		return other;
	}

	/**
	 * Loads and returns a LinkedGraph with an existing data set. The expected
	 * format of the file is the first line being the integer number of vertices
	 * and each subsequent line contains an edge
	 * <br/>
	 * The below example creates a triangle.<br/>
	 * Example:     <br/>
	 * 3       <br/>
	 * 0 1   <br/>
	 * 0 2   <br/>
	 * 1 2   <br/>
	 *
	 * @param filename String name and location of the file
	 * @return
	 */
	public static LinkedGraph load(String filename) {
		File f = new File(filename);
		Scanner s = null;
		try {
			s = new Scanner(f);
		} catch (IOException e) {
			System.out.println("Graph not loaded properly: " + e.getMessage());
			return null;
		}
		int SIZE = s.nextInt();
		// set default data
		LinkedGraph other = new LinkedGraph(SIZE);
		// populate the adjacency matrix/list
		while (s.hasNext()) {
			int from = s.nextInt();
			int to = s.nextInt();
			other.addEdge(from, to);
		}
		return other;
	}

	public Node get(int index) {
		return this.NODES[this.NODES[index].getId()];
	}

	/**
	 *
	 * @param from index of the first node
	 * @param to index of the second node
	 * @return True if the nodes have been merged into the same supernode, False otherwise
	 */
	public boolean sameCluster(int from, int to) {
		int secondary = this.NODES[from].getId();
		int primary = this.NODES[to].getId();

		return secondary == primary;
	}

	/*
    From here on, the javadocs will be inherited from the Graph class that this
    code implements .
	 */
	public void merge(int from, int to) {

		int secondary = this.NODES[from].getId();
		int primary = this.NODES[to].getId();

		// if already in the same supernode, return
		if (primary == secondary) {
			System.out.println(from + " to " + to);
			return;
		}

		// get all nodes already merged with the secondary node
		Set<Integer> secondaryMerges = new HashSet<>(this.NODES[secondary].getMergeNodes());
		// add the secondary node itself to the nodes to be merged
		secondaryMerges.add(secondary);
		// do the same for the primary node (which we are merging into)
		Set<Integer> primaryMerges = new HashSet<>(this.NODES[primary].getMergeNodes());
		primaryMerges.add(primary);

		// if there wasn't already an edge between the primary and secondary node, add a fake edge to each
		if (!this.ORIGINAL_MATRIX.get(primary).contains(secondary)) {
			this.NODES[primary].addFakeEdge(secondary);
			this.NODES[secondary].addFakeEdge(primary);
		}

		// update the reference for the secondary node to reflect being absorbed into the primary node
		this.NODES[secondary].setReference(this.NODES[primary]);

		ArrayList<Integer> primaryCurrentNeighbors = new ArrayList<>(this.MATRIX.get(primary));
		ArrayList<Integer> secondaryCurrentNeighbors = new ArrayList<>(this.MATRIX.get(secondary));
		ArrayList<Integer> primaryOriginalNeighbors = new ArrayList<>(this.ORIGINAL_MATRIX.get(primary));
		ArrayList<Integer> secondaryOriginalNeighbors = new ArrayList<>(this.ORIGINAL_MATRIX.get(secondary));


		// for each of the nodes in the secondary (super)node that needs to be merged
		for (int secondaryMerge : secondaryMerges) {
			// for each of the nodes held in the primary (super)node that we are merging into
			for (int primaryMerge : primaryMerges) {
				// get the original set of neighbours for the secondary node
				HashSet<Integer> secondaryMergeOriginalNeighbors = new HashSet<>(this.ORIGINAL_MATRIX.get(secondaryMerge));
				// add in all nodes connected by a fake edge to the list of neighbours
				secondaryMergeOriginalNeighbors.addAll(this.NODES[secondaryMerge].getFakeEdges());
				// remove any nodes that are already adjacent to the node we are merging into
				secondaryMergeOriginalNeighbors.removeAll(this.ORIGINAL_MATRIX.get(primaryMerge));
				// remove the node we are merging into from the neighbours
				secondaryMergeOriginalNeighbors.remove(primaryMerge);
				// all of the edges and fake edges to the node being merged into which
				// did not already exist to the primary node are added as fake edges
				this.NODES[primaryMerge].addFakeEdges(secondaryMergeOriginalNeighbors);

				// add the corresponding fake edge to each of the secondary node's neighbours
				for (int secondaryMergeNeighbor : secondaryMergeOriginalNeighbors) {
					this.NODES[secondaryMergeNeighbor].addFakeEdge(primaryMerge);
				}

				// repeat the above process to add fake edges from the neighbours of the primary node to the secondary node
				HashSet<Integer> primaryMergeOriginalNeighbors = new HashSet<>(this.ORIGINAL_MATRIX.get(primaryMerge));
				primaryMergeOriginalNeighbors.addAll(this.NODES[primaryMerge].getFakeEdges());
				primaryMergeOriginalNeighbors.removeAll(this.ORIGINAL_MATRIX.get(secondaryMerge));
				primaryMergeOriginalNeighbors.remove(secondaryMerge);
				this.NODES[secondaryMerge].addFakeEdges(primaryMergeOriginalNeighbors);
				for (int primaryMergeNeighbor : primaryMergeOriginalNeighbors) {
					this.NODES[primaryMergeNeighbor].addFakeEdge(secondaryMerge);
				}
			}
		}

		// once all the fake edges have been calculated, absorbs the secondary node(s) into the primary node
		this.NODES[primary].absorb(secondaryMerges);

		Set<Integer> newPrimary = new HashSet<Integer>();

		// removes edges to each other, since the nodes are now the same supernode
		if (primaryCurrentNeighbors.contains(secondary)) {
			primaryCurrentNeighbors.remove(primaryCurrentNeighbors.indexOf(secondary));
		}
		if (secondaryCurrentNeighbors.contains(primary)) {
			secondaryCurrentNeighbors.remove(secondaryCurrentNeighbors.indexOf(primary));
		}

		// combine neighbors
		newPrimary.addAll(secondaryCurrentNeighbors);
		newPrimary.addAll(primaryCurrentNeighbors);

		// set new neighbor list to A+B-{to, from}
		this.MATRIX.set(primary, new ArrayList<>(newPrimary));

		// updates neighbors' neighbors to include primary (to)
		for (int neighbor : newPrimary) {
			// remove if secondary exists
			if (this.MATRIX.get(neighbor).contains(secondary)) {
				this.MATRIX.get(neighbor).remove(
						this.MATRIX.get(neighbor).indexOf(secondary)
				);
			}
			// add if primary doesn't exist
			if (!this.MATRIX.get(neighbor).contains(primary)) {
				this.MATRIX.get(neighbor).add(primary);
			}
		}
		// updates size
		this.SIZE--;
	}

	public int totalFakeLinks() {
		int total = 0;
		for (Node node : this.NODES) {
			total += node.getFakeEdges().size();
		}
		return total / 2;
	}

	public int printFakeLinks() {
		int total = 0;
		for (Node node : this.NODES) {
			total += node.getFakeEdges().size();
			System.out.println(node.ID + ": " + node.getFakeEdges());
		}
		return total / 2;
	}

	public int fakeLinks(int from, int to) {
		System.err.println("method 'fakeLinks' disabled.");
		return -1;
		/*
		int aFrom = this.NODES[from].getId();
		int aTo = this.NODES[to].getId();
		if (aFrom == aTo) {
			return -1;
		}

		// links from previous merges
		int linksFrom = this.NODES[aFrom].getLinks();
		int linksTo = this.NODES[aTo].getLinks();
		int mergedCount = 0;
		List<Integer> mergedFrom = this.NODES[aFrom].getMergeNodes();
		for (int node : mergedFrom) {
			if (!this.ORIGINAL_MATRIX.get(aTo).contains(node)) {
				mergedCount++;
			}
		}
		List<Integer> mergedTo = this.NODES[aTo].getMergeNodes();
		for (int node : mergedTo) {
			if (!this.ORIGINAL_MATRIX.get(aFrom).contains(node)) {
				mergedCount++;
			}
		}
		int inheritedLinks = linksFrom + linksTo + mergedCount;

		Set<Integer> fakes = new HashSet<Integer>(this.MATRIX.get(aFrom));
		fakes.remove(aFrom);
		Set<Integer> fakeCompares = new HashSet<Integer>(this.MATRIX.get(aTo));
		fakeCompares.remove(aTo);
		for (Integer t : fakeCompares) {
			if (fakes.contains(t)) {
				fakes.remove(t);
			} else {
				fakes.add(t);
			}
		}
		// return size+1 if they weren't initially connected
		int rawLinks = fakes.size() + 1;
		// return size-2 they're initially connected
		if (this.MATRIX.get(aTo).contains(aFrom) || this.MATRIX.get(aFrom).contains(aTo)) {
			if (fakes.size() < 2) {
				System.out.println("CRAP");
			}
			rawLinks = fakes.size() - 2;
		}
		return rawLinks + inheritedLinks;
		 */
	}

	public LinkedGraph deepCopy() {
		// create default object
		LinkedGraph other = LinkedGraph.load(this.MATRIX);
		// update size with current size
		other.SIZE = this.SIZE;
		// update reference of each node
		for (int i = 0; i < this.MAX_SIZE; i++) {
			int index = this.NODES[i].getId();
			other.NODES[i].setReference(other.NODES[index]);
		}
		return other;
	}

	public void print() {
		for (int i = 0; i < this.MAX_SIZE; i++) {
			// only print out nodes that haven't been merged into an other node
			if (this.NODES[i].getId() == i) {
				ArrayList<String> neighbors = new ArrayList<String>();
				// adding to arraylist to make use of String.join
				for (Integer iNeighbor : this.MATRIX.get(i)) {
					neighbors.add(String.valueOf(iNeighbor));
				}
				String vertice = "{(" + i;
				if (this.SHOW_MEMORY) {
					vertice += "," + Integer.toHexString(this.NODES[i].hashCode());
				}
				if (this.NODES[i].getMergeNodes().size() > 0) {
					vertice += ": " + this.NODES[i].getMergeNodes();
				}
				vertice += ") -> " + String.join(",", neighbors) + "}";
				System.out.println(vertice);
			}
		}
	}

	public String toString() {
		// addings each vertice to arraylist to make use of String.join
		ArrayList<String> returnValue = new ArrayList<String>();
		for (int i = 0; i < this.MAX_SIZE; i++) {
			// only print out nodes that haven't been merged into an other node
			if (this.NODES[i].getId() == i) {
				ArrayList<String> neighbors = new ArrayList<String>();
				// adding to arraylist to make use of String.join
				for (Integer iNeighbor : this.MATRIX.get(i)) {
					neighbors.add(String.valueOf(iNeighbor));
				}
				ArrayList<Integer> cluster = new ArrayList<>(this.NODES[i].getMergeNodes());
				cluster.add(i);
				Collections.sort(cluster);
				String vertice = "{" + cluster;
				if (this.SHOW_MEMORY) {
					vertice += ":" + Integer.toHexString(this.NODES[i].hashCode());
				}
				vertice += " -> " + String.join(",", neighbors) + "}";
				returnValue.add(vertice);
			}
		}
		return "{" + String.join(",", returnValue) + "}";
	}

	public String toGraphViz() {
		// need particular format for this
		return null;
	}

	public List<Integer> bfs(int root, int depth) {
		int rootValue = this.NODES[root].getId();
		//System.out.println("Root: "+rootValue);
		Set<Integer> explored = new HashSet<>();
		Queue<WrappedNode> toExplore = new LinkedList<>();
		// initialize root as what needs to be explored
		explored.add(rootValue);
		toExplore.add(new WrappedNode(rootValue, 0));
		// explore while there are items to explore
		while (!toExplore.isEmpty()) {
			WrappedNode at = toExplore.remove();
			int atIndex = this.NODES[at.index].getId();
			int atDistance = at.distance;
			// if we need are not at max depth, add the neighbours of this node to explore
			if (atDistance < depth) {
				List<Integer> neighbors = this.MATRIX.get(atIndex);
				//System.out.println("Found "+neighbors.size()+" neighbors for "+atIndex+" current distance "+atDistance);

				// explore neighboring nodes
				for (int index : neighbors) {
					int iValue = this.NODES[index].getId();
					//if not previously visited, queue up the item
					if (!explored.contains(iValue)) {
						//System.out.println("Checking "+atIndex+"'s neighor "+iValue+" "+(atDistance+1)+" away");
						toExplore.add(new WrappedNode(iValue, atDistance + 1));
						explored.add(iValue);
					}
				}
			}
			//System.out.println("Done checking "+atIndex);
		}
		List<Integer> returnValue = new ArrayList<>(explored);
		returnValue.remove(returnValue.indexOf(rootValue));
		//System.out.println("retvalu"+returnValue);
		return returnValue;
	}

	public int distance(int from, int to) {
		int aFrom = this.NODES[from].getId();
		int aTo = this.NODES[to].getId();

		if (aFrom == aTo) {
			return 0;
		}

		int shortest = Integer.MAX_VALUE;
		// keeping a track of previously visited to avoid infinite loops
		ArrayList<Integer> explored = new ArrayList<Integer>();
		PriorityQueue<WrappedNode> toExplore = new PriorityQueue<WrappedNode>();
		// initialize from Node to be the first node to check
		toExplore.add(new WrappedNode(aFrom, 0));
		while (!toExplore.isEmpty()) {
			WrappedNode current = toExplore.remove();
			int currentIndex = this.NODES[current.index].getId();
			int currentDistance = current.distance;
			// if we're where we want to be, return the distance
			List<Integer> neigh = this.MATRIX.get(currentIndex);
			//System.out.println("Found "+neigh.size()+" neighbors for "+currentIndex+" current distance "+currentDistance+".");
			// explore neighboring nodes
			for (int index : neigh) {
				int iValue = this.NODES[index].getId();
				if (iValue == aTo && (current.distance + 1) < shortest) {
					shortest = current.distance + 1;
				}
				// if not previously visited, queue up the item
				if (!explored.contains(iValue)) {
					explored.add(iValue);
					toExplore.add(new WrappedNode(iValue, current.distance + 1));
				}
			}

		}
		// if unable to be found, return -1
		return shortest == Integer.MAX_VALUE ? -1 : shortest;
	}
}
