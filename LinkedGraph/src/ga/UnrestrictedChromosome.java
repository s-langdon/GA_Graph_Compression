package ga;

import linkedgraph.LinkedGraph;

import java.util.List;
import java.util.Random;

/**
 * This chromosome does not have a locality restriction of any kind associated with its merges.
 * This serves as a performance and runtime baseline to compare against other methods.
 */
public class UnrestrictedChromosome extends Chromosome {

    public UnrestrictedChromosome(Random RANDOM, int size, int maxDepth) {
        super(RANDOM, size, maxDepth);
    }

    @Override
    public Chromosome copy() {
        Chromosome copy = new UnrestrictedChromosome(this.RANDOM, this.size, this.maxDepth);
        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.genes[i].length; j++) {
                copy.genes[i][j] = this.genes[i][j];
            }
        }
        return copy;
    }

    @Override
    public void mutateGene(int index, LinkedGraph graph) {
        // select a random starting node
        int randomRoot = this.RANDOM.nextInt(graph.getSize());
        // set that as the root of the selected gene
        this.genes[index][0] = randomRoot;
        // calculate the offset value
        int randomOffset = this.RANDOM.nextInt(graph.getSize() - 1) + 1;
        // set the offset
        this.genes[index][1] = randomOffset;
    }

    @Override
    public void validateGene(int index, LinkedGraph graph) {
        int from = this.genes[index][0];
        int to = (this.genes[index][0] + this.genes[index][1]) % graph.getSize();
        int[] tempGene = new int[]{this.genes[index][0], this.genes[index][1]};
        int offset = this.genes[index][1];

        // if the gene is invalid, because it appears more than once in the chromosome
        // or merges two nodes already in the same cluster, replace it with a new gene
        if (duplicateGene(tempGene) || graph.sameCluster(from, to)) {
            // make one attempt to only change the offset
            offset = this.RANDOM.nextInt(graph.getSize() - 1) + 1;
            to = Math.floorMod(from + offset, graph.getSize());
            tempGene[0] = from;
            tempGene[1] = offset;
            // if that doesn't work, randomly select a new 'from' node and corresponding 'to' node
            while (duplicateGene(tempGene) || graph.sameCluster(from, to)) {
                from = this.RANDOM.nextInt(graph.getSize());
                offset = this.RANDOM.nextInt(graph.getSize() - 1) + 1;
                to = Math.floorMod(from + offset, graph.getSize());
                tempGene[0] = from;
                tempGene[1] = offset;
            }
        }
        // update the gene
        this.genes[index][0] = from;
        this.genes[index][1] = offset;
    }
}
