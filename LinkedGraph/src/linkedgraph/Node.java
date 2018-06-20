package linkedgraph;

/**
 * Node class that holds a reference to it's actual value
 * @author ar14rk
 */
public class Node{
    private Node REFERENCE;
    private int ID;

    /**
     * @param id the index of value of the node
     */
    public Node(int id){
        this.ID = id;
        this.REFERENCE = this;
    }

    /**
     * This method will only copy the object's value, not the reference.
     * @param other the node object to copy
     */
    public Node(Node other){
        this(other.ID);
    }

    /**
     * Sets node reference to itself
     */
    public void cleanReference(){
        this.setReference(this);
    }

    /**
     * This method hops through all the references and returns the very 
     * latest reference in the list
     * @return
     */
    public Node getReference(){
        // This method is used primarily to avoid loops
        if(this.REFERENCE==this)return this;
        return this.REFERENCE.getReference();
    }

    /**
     * This sets this node's reference to be last reference in the list 
     * of references
     * @param other
     */
    public void setReference(Node other){
        this.REFERENCE = other.getReference();
    }

    /**
     * Returns the value of the end of the list of references
     * @return
     */
    public int getId(){
        if(this.REFERENCE==this) return this.ID;
        return this.REFERENCE.getId();
    }

	@Override
    public String toString(){
        if(this.REFERENCE==this){
            return "("+this.ID+":"+Integer.toHexString(this.hashCode())+")";
        }else{
            return "("+
                    this.ID+":"+
                    Integer.toHexString(this.hashCode())+"->"+
                    this.REFERENCE.toString()+
                    ")";
        }
    }
}
