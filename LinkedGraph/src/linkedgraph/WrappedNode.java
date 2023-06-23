package linkedgraph;

/**
 * A wrapper class for the Node class for use in BFS, shortest path calculations.
 */
public class WrappedNode implements Comparable<WrappedNode>{
    public int index;
    public int distance;
    public WrappedNode(int i, int d){
        this.index = i;
        this.distance = d;
    }        
    public int compareTo(WrappedNode other){
        return -Integer.compare(other.distance,this.distance);
    }
}
