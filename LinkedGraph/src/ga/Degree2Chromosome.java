package ga;

import linkedgraph.LinkedGraph;
import linkedgraph.WrappedNode;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

/**
 * For each selected root node, this chromosome looks at neighbours within a neighbourhood defined by a BFS that is at
 * most maxDepth, and chooses the neighbour with the lowest degree to merge together, with probability
 * MIN_DEG_SELECT_RATE
 *
 * This is a temporary second version which creates a priority queue for validating the chromosomes. If the merge is
 * invalid it will create a priority queue and select neighbours from it in order of degree.
 *
 * Both versions have been created in order to compare performance between the two versions. The goal is to see if the
 * creation of the priority queue has a large impact on the computational complexity/runtime, as well as what
 * performance benefit, if any, it provides.
 */
public class Degree2Chromosome extends Chromosome {

    private double MIN_DEG_SELECT_RATE = 1.0;

    public Degree2Chromosome(Random RANDOM, int size, int maxDepth) {
        super(RANDOM, size, maxDepth);
    }

    public Degree2Chromosome(Random RANDOM, int size, int maxDepth, double degreeSelectRate){
        super(RANDOM, size, maxDepth);
        this.MIN_DEG_SELECT_RATE = degreeSelectRate;
    }

    @Override
    public Chromosome copy() {
        Chromosome copy = new Degree2Chromosome(this.RANDOM, this.size, this.maxDepth, this.MIN_DEG_SELECT_RATE);
        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.genes[i].length; j++) {
                copy.genes[i][j] = this.genes[i][j];
            }
        }
        return copy;
    }

    @Override
    public void mutateGene(int index, LinkedGraph graph) {
        List<Integer> neighbours;
        int randomRoot;
        // for graphs with nodes that have degree = 0, select a different root node (in order to be able to find a neighbour)
        do {
            // select a random starting node
            randomRoot = this.RANDOM.nextInt(graph.getSize());
            // set that as the root of the selected gene
            this.genes[index][0] = randomRoot;
            // get the neighbours of that root within the specified distance
            neighbours = graph.bfs(randomRoot, this.maxDepth);
        } while(neighbours.size() == 0);

        int selectedNeighbour = neighbours.get(0); //initialize with the first neighbour

        // if less than rate, select the neighbour with min degree to merge with
        if (this.RANDOM.nextDouble() < MIN_DEG_SELECT_RATE){
            int minDeg = Integer.MAX_VALUE;
            // select the neighbour with the lowest degree to merge
            for (int i = 0; i < neighbours.size(); i++) {
                int neighbour = neighbours.get(i);
                int deg = graph.getOriginalDegree(neighbour);
                if (deg < minDeg) {
                    selectedNeighbour = neighbour;
                    minDeg = deg;
                }
            }
        } else { // else select a random neighbour
            // select a random neighbour from the list
            selectedNeighbour = neighbours.get(this.RANDOM.nextInt(neighbours.size()));
        }

        // calculate the offset value
        int randomOffset = Math.floorMod(selectedNeighbour - randomRoot, graph.getSize());
        // set the offset
        this.genes[index][1] = randomOffset;
    }

    /**
     * In the case that the gene is invalid, grab another gene at random, as in BFS.
     * @param index Index of the gene to check
     * @param graph Graph to use as context for validating the gene.
     */
    @Override
    public void validateGene(int index, LinkedGraph graph) {

        int from = this.genes[index][0];
        int to = (this.genes[index][0] + this.genes[index][1]) % graph.getSize();
        int[] tempGene = new int[]{this.genes[index][0], this.genes[index][1]};

        // if the gene is invalid, because it appears more than once in the chromosome
        // or merges two nodes already in the same cluster, replace it with a new gene
        if (duplicateGene(tempGene) || graph.sameCluster(from, to)) {

            List<Integer> possibleNeighbors = graph.bfs(from, this.maxDepth);

            // get the degree information for all the neighbours and create a priority queue
            // lightly misusing the WrappedNode for this class, using "distance" to store degree
            PriorityQueue<WrappedNode> neighboursByDegree = getNeighboursByDegree(possibleNeighbors, graph);

            // iterate through all neighbours within the distance limit of the 'from' node, ordered by lowest degree
            // select the first one which represents a valid merge, if it exists
            for (int i = 0; i < neighboursByDegree.size(); i++) {
                to = possibleNeighbors.get(i);
                tempGene[0] = from;
                tempGene[1] = Math.floorMod(to - from, graph.getSize());
                if (!graph.sameCluster(from, to) && !duplicateGene(tempGene)) {
                    break;
                }
            }

            // if no valid merges were found within the neighbourhood of the 'from' node
            // randomly select a new 'from' node and corresponding 'to' node
            while (duplicateGene(tempGene) || graph.sameCluster(from, to)) {
                from = this.RANDOM.nextInt(graph.getSize());
                possibleNeighbors = graph.bfs(from, this.maxDepth);
                if (possibleNeighbors.size() < 1) { //ignore isolated nodes
                    continue;
                }
                // this will only ever look at the first option
                // consider a refactor consider all of the neighbours of the random root OR not using the priority queue here?
                neighboursByDegree = getNeighboursByDegree(possibleNeighbors, graph);
                to = neighboursByDegree.remove().distance;

                tempGene[0] = from;
                tempGene[1] = Math.floorMod(to - from, graph.getSize());
            }
        }
        // update the gene
        this.genes[index][0] = from;
        this.genes[index][1] = Math.floorMod(to - from, graph.getSize());
    }

    // helper method for ordering the possible neighbours by their degree
    private PriorityQueue<WrappedNode> getNeighboursByDegree(List<Integer> neighbours, LinkedGraph graph){
        // get the degree information for all the neighbours and create a priority queue
        // lightly misusing the WrappedNode for this class, using "distance" to store degree
        PriorityQueue neighboursByDegree = new PriorityQueue(Comparator.naturalOrder());
        // populate the priority queue
        for(Integer neighbour : neighbours) {
            neighboursByDegree.add(new WrappedNode(neighbour, graph.getOriginalDegree(neighbour)));
        }
        return neighboursByDegree;
    }

}


