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
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.PriorityQueue;

/**
 * This object implements the Graph interface designed by Tyler. Additional methods
 * such as addEdge or load have been added on to this implementation to assist
 * the developer.
 * @author aromualdo
 */
public class LinkedGraph implements Graph{

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
     * @param   size Integer value of the size of the file
     */
    private LinkedGraph(int size){
        this.SIZE = size;
        this.MAX_SIZE = size;
        this.MATRIX = new ArrayList<ArrayList<Integer>>(size);
        this.NODES = new Node[size];
        for(int i=0;i<this.NODES.length;i++){
            // populate nodes with default references
            this.NODES[i] = new Node(i);
            // populate adjacency list with empty lists
            this.MATRIX.add(new ArrayList<Integer>());
        }
    }
    
    /**
    * Getter method for MAX SIZE
    */
    public int getSize(){
        return this.MAX_SIZE;
    }
    
    /**
    * Getter method for SIZE
    */
    public int getCurrentSize(){
        return this.SIZE;
    }
    
    /**
     * Manually add in edges, primarily for testing smaller data sets.
     * 
     * @param from  Integer value of the index of the first node
     * @param to    Integer value of the index of the second node
     */
    public void addEdge(int from, int to){
        int aFrom = this.NODES[from].getId();
        int aTo = this.NODES[to].getId();
        if(!this.MATRIX.get(aFrom).contains(aTo)){
            this.MATRIX.get(aFrom).add(aTo);
        }
        if(!this.MATRIX.get(aTo).contains(aFrom)){
            this.MATRIX.get(aTo).add(aFrom);
        }
    }

    /**
     * Loads and returns a LinkedGraph with an existing list of edges.
     * 
     * @param matrix the list of edges in 2D ArrayList form
     * @return
     */
    public static LinkedGraph load(ArrayList<ArrayList<Integer>> m){
        LinkedGraph other = new LinkedGraph(m.size());
        // populate the adjacency matrix/list
        for(int i=0;i<other.MAX_SIZE;i++){
            other.MATRIX.set(i,new ArrayList<Integer>(m.get(i)));
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
     *      3       <br/>
     *      0   1   <br/>
     *      0   2   <br/>
     *      1   2   <br/>
     * 
     * @param filename String name and location of the file
     * @return
     */
    public static LinkedGraph load(String filename){
        File f = new File(filename);
        Scanner s = null;
        try{
            s = new Scanner(f);
        }
        catch(IOException e){
            System.out.println("Graph not loaded properly: "+e.getMessage());
            return null;
        }
        int SIZE = s.nextInt();
        // set default data
        LinkedGraph other = new LinkedGraph(SIZE);
        // populate the adjacency matrix/list
        while(s.hasNext()){
            int from = s.nextInt();
            int to = s.nextInt();
			other.addEdge(from, to);
        }
        return other;
    }
    
    /*
    From here on, the javadocs will be inherited from the Graph class that this
    code implements .
    */
    
    public void merge(int from, int to){
        
        int aFrom = this.NODES[from].getId();
        int aTo = this.NODES[to].getId();
        if(aFrom==aTo) return;
        this.NODES[aFrom].setReference(NODES[aTo]);
        ArrayList<Integer> master = new ArrayList<>(this.MATRIX.get(aTo));
        ArrayList<Integer> slave = new ArrayList<>(this.MATRIX.get(aFrom));
        // Ensure nodes are unique
        Set<Integer> newMaster = new HashSet<Integer>();
        // removes links to each other
        if(master.contains(aFrom))master.remove(master.indexOf(aFrom));
        if(slave.contains(aTo))slave.remove(slave.indexOf(aTo));
        newMaster.addAll(master);
        newMaster.addAll(slave);
        
        // set new neighbor list to A+B-{to, from}
        this.MATRIX.set(aTo, new ArrayList<>(newMaster));
        
        // updates neighbors' neighbors to include master(to)
        for(int neighbor: newMaster){
            // remove if slave exists
            if(this.MATRIX.get(neighbor).contains(aFrom)){
                this.MATRIX.get(neighbor).remove(
                        this.MATRIX.get(neighbor).indexOf(aFrom)
                );
            }
            // add if master doesn't exist
            if(!this.MATRIX.get(neighbor).contains(aTo)){
                this.MATRIX.get(neighbor).add(aTo);
            }
        }
        
        // updates size
        this.SIZE--;
    }

    public int fakeLinks(int from, int to){
        int aFrom = this.NODES[from].getId();
        int aTo = this.NODES[to].getId();
		if(aFrom == aTo) return -1;

        // Get the exclusive-OR by removing elements if they exist in both
        Set<Integer> fakes = new HashSet<Integer>(this.MATRIX.get(aFrom));
		fakes.remove(aFrom);
		Set<Integer> fakeCompares = new HashSet<Integer>(this.MATRIX.get(aTo));
		fakeCompares.remove(aTo);
		for(Integer t: fakeCompares){
            if(fakes.contains(t)){
                fakes.remove(t);
            }else{
                fakes.add(t);
            }
        }
        // return size-2 they're initially connected
        if(this.MATRIX.get(aTo).contains(aFrom) || this.MATRIX.get(aFrom).contains(aTo)){
			if(fakes.size()<2) System.out.println("CRAP");
            return fakes.size()-2;
        }
        // return size+1 if they weren't initially connected
        return fakes.size()+1;
    }
    public LinkedGraph deepCopy(){
        // create default object
        LinkedGraph other = LinkedGraph.load(this.MATRIX);
        // update size with current size
        other.SIZE = this.SIZE;
        // update reference of each node
        for(int i=0;i<this.MAX_SIZE;i++){
            int index = this.NODES[i].getId();
            other.NODES[i].setReference(other.NODES[index]);
        }
        return other;
    }
    public String toString(){
        // addings each vertice to arraylist to make use of String.join
        ArrayList<String> returnValue = new ArrayList<String>();
        for(int i = 0;i<this.MAX_SIZE;i++){
            // only print out nodes that haven't been merged into an other node
            if(this.NODES[i].getId() == i){
                ArrayList<String> neighbors = new ArrayList<String>();
                // adding to arraylist to make use of String.join
                for (Integer iNeighbor: this.MATRIX.get(i)) {
                    neighbors.add(String.valueOf(iNeighbor));
                }
                String vertice = "{("+i;
                if(this.SHOW_MEMORY) vertice+=","+Integer.toHexString(this.NODES[i].hashCode());
                vertice+=") -> "+String.join(",",neighbors)+"}";
                returnValue.add(vertice);
            }
        }
        return "{"+String.join(",",returnValue)+"}";
    }
    public String toGraphViz(){
        // need particular format for this
        return null;
    }
    public List<Integer> bfs(int root, int depth){
        int rootValue = this.NODES[root].getId();
		//System.out.println("Root: "+rootValue);
        Set<Integer> explored = new HashSet<>();
        Queue<WrappedNode> toExplore = new LinkedList<>();
        // initialize root as what needs to be explored
        explored.add(rootValue);
        toExplore.add(new WrappedNode(rootValue,0));
        // explore while there are items to explore
        while(!toExplore.isEmpty()){
            WrappedNode at = toExplore.remove();
			int atIndex = this.NODES[at.index].getId();
			int atDistance = at.distance;
            if(atDistance < depth){
				List<Integer> neighbors = this.MATRIX.get(atIndex);
				//System.out.println("Found "+neighbors.size()+" neighbors for "+atIndex+" current distance "+atDistance);
						
				// explore neighboring nodes
				for(int index : neighbors){
					int iValue = this.NODES[index].getId();
					//if not previously visited, queue up the item
					if(!explored.contains(iValue)){
						//System.out.println("Checking "+atIndex+"'s neighor "+iValue+" "+(atDistance+1)+" away");
						toExplore.add(new WrappedNode(iValue,atDistance+1));
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
    public int distance(int from, int to){
        int aFrom = this.NODES[from].getId();
        int aTo = this.NODES[to].getId();
		int shortest = Integer.MAX_VALUE;
        // keeping a track of previously visited to avoid infinite loops
        ArrayList<Integer> explored = new ArrayList<Integer>();
        PriorityQueue<WrappedNode> toExplore = new PriorityQueue<WrappedNode>();
        // initialize from Node to be the first node to check
        toExplore.add(new WrappedNode(aFrom,0));
        while(!toExplore.isEmpty()){
            WrappedNode current = toExplore.remove();
			int currentIndex = this.NODES[current.index].getId();
			int currentDistance = current.distance;
            // if we're where we want to be, return the distance
            List<Integer> neigh = this.MATRIX.get(currentIndex);
			//System.out.println("Found "+neigh.size()+" neighbors for "+currentIndex+" current distance "+currentDistance+".");
			// explore neighboring nodes
            for(int index : neigh){
                int iValue = this.NODES[index].getId();
                if(iValue == aTo && (current.distance+1)<shortest){
					shortest = current.distance+1;
				}
				// if not previously visited, queue up the item
                if(!explored.contains(iValue)){
                    explored.add(iValue);
                    toExplore.add(new WrappedNode(iValue,current.distance+1));
                }
            }

        }
        // if unable to be found, return -1
        return shortest==Integer.MAX_VALUE ? -1 : shortest;
    }
}
