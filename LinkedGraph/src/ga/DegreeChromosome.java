package ga;

import linkedgraph.LinkedGraph;

import java.util.List;
import java.util.Random;

/**
 * For each selected root node, this chromosome looks at neighbours within a neighbourhood defined by a BFS that is at
 * most maxDepth, and chooses the neighbour with the lowest degree to merge together, with probability
 * MIN_DEG_SELECT_RATE
 */
public class DegreeChromosome extends Chromosome {

    private double MIN_DEG_SELECT_RATE = 1.0;

    public DegreeChromosome(Random RANDOM, int size, int maxDepth) {
        super(RANDOM, size, maxDepth);
    }

    public DegreeChromosome(Random RANDOM, int size, int maxDepth, double degreeSelectRate) {
        super(RANDOM, size, maxDepth);
        this.MIN_DEG_SELECT_RATE = degreeSelectRate;
    }

    @Override
    public Chromosome copy() {
        Chromosome copy = new DegreeChromosome(this.RANDOM, this.size, this.maxDepth, this.MIN_DEG_SELECT_RATE);
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
            // iterate through all neighbours within the distance limit of the 'from' node
            // select the first one which represents a valid merge, if it exists
            for (Integer neighbor : possibleNeighbors) {
                to = neighbor;
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
                if (possibleNeighbors.size() < 1) {
                    continue;
                }
                to = possibleNeighbors.get(this.RANDOM.nextInt(possibleNeighbors.size()));

                tempGene[0] = from;
                tempGene[1] = Math.floorMod(to - from, graph.getSize());
            }
        }
        // update the gene
        this.genes[index][0] = from;
        this.genes[index][1] = Math.floorMod(to - from, graph.getSize());
    }
}
