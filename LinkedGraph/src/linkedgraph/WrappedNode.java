package linkedgraph;

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
