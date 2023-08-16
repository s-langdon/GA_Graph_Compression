package ga;

import linkedgraph.LinkedGraph;

import java.util.List;
import java.util.Random;

/**
 * This class implements a fixed Chromosome which, regardless of depth parameter,
 * always selects the first neighbour in the adjacency list of the root node.
 * This is not to serve any analytical or research value but rather to establish
 * a lower-bound on running time.
 */
public class FixedChromosome extends Chromosome {

    public FixedChromosome(Random RANDOM, int size, int maxDepth) {
        super(RANDOM, size, maxDepth);
    }

    @Override
    public Chromosome copy() {
        Chromosome copy = new FixedChromosome(this.RANDOM, this.size, this.maxDepth);
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
            neighbours = graph.getAdjacentNodes(randomRoot);
        } while(neighbours.size() == 0);
        // select a random neighbour from the list (always the first one)
        int randomNeighbor = neighbours.get(0);
        // calculate the offset value
        int randomOffset = Math.floorMod(randomNeighbor - randomRoot, graph.getSize());
        // set the offset
        this.genes[index][1] = randomOffset;


    }

    @Override
    public void validateGene(int index, LinkedGraph graph) {
        int from = this.genes[index][0];
        int to = (this.genes[index][0] + this.genes[index][1]) % graph.getSize();
        int[] tempGene = new int[]{this.genes[index][0], this.genes[index][1]};

        // if the gene is invalid, because it appears more than once in the chromosome
        // or merges two nodes already in the same cluster, replace it with a new gene
        if (duplicateGene(tempGene) || graph.sameCluster(from, to)) {

            List<Integer> possibleNeighbors = graph.getAdjacentNodes(from);
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
                possibleNeighbors = graph.getAdjacentNodes(from);
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
