package ga;

import linkedgraph.LinkedGraph;
import java.util.Random;

public abstract class Chromosome {

    // attributes
    Random RANDOM; // shared Random object
    int maxDepth; // defines how many edges away something can be to be a valid merge candidate -- defines "local"
    int size; // size of the chromosome -- number of merges to compress the graph
    int[][] genes; // holds the root/offset pairs that define the merges
    int fitness; // fitness -- not strictly necessary/used at the moment, but nice for debugging etc

    // constructor
    protected Chromosome(Random RANDOM, int size, int maxDepth) {
        this.RANDOM = RANDOM;
        this.maxDepth = maxDepth;
        this.size = size;
        this.genes = new int[this.size][2]; // initializes the genes as an empty array
    }


    /**
     * Initializes the Chromosome with random
     * @param graph the graph to be compressed by the sequence of merges in the chromosome
     */
    public void init(LinkedGraph graph){
        for (int i = 0; i < size; i++){
            this.mutateGene(i, graph);
        }
    }

    /**
     * Deep-copies a Chromosome
     * @return a new Chromosome object with the same attributes as this one.
     */
    public abstract Chromosome copy();

    /**
     * Replaces the specified gene with a new random gene, in-place.
     * @param index the index of the gene to mutate
     * @param graph the graph for which the mutation must be valid
     */
    public abstract void mutateGene(int index, LinkedGraph graph);

    /**
     * Checks that the gene is valid given the context of the rest of the Chromosome and the graph to which the gene
     * should be applied. A gene is invalid if there is another identical gene in the same Chromosome or if the gene
     * specifies a merge between two nodes which are already in the same supernode in the graph (this may occur as a
     * result of previous merges in the Chromosome which have already been applied to the graph).
     *
     * If the gene is invalid, it should be replaced by a random valid gene, maintaining the same root node if possible.
     *
     * @param index Index of the gene to check
     * @param graph Graph to use as context for validating the gene.
     */
    public abstract void validateGene(int index, LinkedGraph graph);

    /**
     * Performs the merge specified by the gene on the graph passed in.
     * @param index the index of the gene
     * @param graph the graph to apply the merge to
     */
    public void applyGene(int index, LinkedGraph graph){
        int from = this.genes[index][0];
        int to = (this.genes[index][1] + from) % graph.getSize();
        graph.merge(from, to);
    }

    /**
     * Randomly replaces one of the genes with a new random gene.
     * @param graph graph that the new merge/gene must exist within
     */
    public void mutate(LinkedGraph graph){
        // get a random gene to mutate
        int geneIndex = this.RANDOM.nextInt(this.size);
        // mutate the selected gene, using the graph for appropriate context
        this.mutateGene(geneIndex, graph);
    }

    /**
     * Checks if the specified gene appears more than once in the Chromosome
     * @param gene to check for duplicates of
     * @return True if the gene exists more than once in the Chromosome, false otherwise.
     */
    public boolean duplicateGene(int[] gene){
        boolean found = false;
        for (int i = 0; i < this.size; i++) {
            if (this.genes[i][0] == gene[0] && this.genes[i][1] == gene[1]) {
                if(found){
                    return true;
                } else {
                    found = true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        String output = "[";
        for (int i = 0; i < this.size; i++) {
            if (i > 0) {
                output += ",";
            }
            output += "(";
            for (int j = 0; j < this.genes[i].length; j++) {
                if (j > 0) {
                    output += ",";
                }
                output += this.genes[i][j];
            }
            output += ")";
        }
        output += "]";
        return output;
    }
}
