package graph;

import java.util.List;

/**
 * This object implements a graph with the specific use of graph compression in mind.
 * Internal representation of this object should be accomplished with an array of
 * lists containing integers. The constructor for the object should function as the
 * 'load' method and take a filename as an argument. Some typical graph objects
 * are left unimplemented or to be made private methods to avoid bitrot in the future.
 * @author Tyler
 */
public interface Graph {
    
    /**
     * Merges two nodes together using safe delete technique internally.
     * Consider implementing a private helper method to mark nodes deleted.
     * @param from Integer index to merge from.
     * @param to Integer index to merge to.
     */
    public void merge(int from, int to);
    
    /**
     * This method pre-computes the amount of fake links (error) that we be introduced
     * upon merging two nodes together.
     * To effectively use this method as a fitness measure, call this function before
     * performing the merge action.
     * Implementation can be accomplished by treating the lists at the 'from' and 
     * 'to' indices as sets, and taking the cardinality of the exclusive-or between
     * them. Note: if the nodes were originally connected, this measure must be 
     * subtracted by one.
     * @param from Integer index to sample the merge from.
     * @param to Integer index to sample the merge to.
     * @return Integer count of how many fake links would be generated by this merge.
     */
    public int fakeLinks(int from, int to);
    
    /**
     * Takes a deep copy of 'this' object and returns it.
     * @return Graph object that is a deep copy of 'this'.
     */
    public Graph deepCopy();
    
    /**
     * Returns a readable debugging format of the objects current state.
     * Format is: "0 -> 1,2,3,4" if the 0th node is connected to 1, 2, 3, and 4.
     * @return String representation of the current state of the object.
     */
    @Override
    public String toString();
    
    /**
     * Second string representation usable for visualization software.
     * See attached notes for format information.
     * @return String to be used in visualization.
     */
    public String toGraphViz();
    
    /**
     * Performs a breadth first search on the graph object, 'depth' many layers
     * deep and returns the node indices which have been found.
     * @param root Integer index of where to begin the search.
     * @param depth Integer count of the maximum steps a node can be from the root
     * in the search.
     * @return List of integers corresponding to indexes that form a neighborhood
     * 'depth' layers deep.
     */
    public List<Integer> bfs(int root, int depth);
    
    /**
     * This method uses Dijkstra's algorithm to determine the distance between two
     * nodes in the graph object.
     * Note that the graph is taken to be unweighted and undirected.
     * @param from Integer index from location.
     * @param to Integer index to location.
     * @return Integer distance between 'from' node and 'to' node.
     */
    public int distance(int from, int to);
    
}
