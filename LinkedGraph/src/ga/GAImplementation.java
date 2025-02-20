package ga;

import display.GraphDisplay;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import graph.*;
import java.io.BufferedWriter;
import java.io.FileWriter;

import linkedgraph.*;


/*
Parameters can be separated by space, tab, colon, and/or equals.

Example params file:

outPrefix angelo_is_cool
population = 106
generations : 10000
tournament : 1
crossover   0.23
mutation 1.0
chromosome 10
runs 10
source ecoli.txt
type BFS

 */
/**
 *
 * @author ar14rk
 */
public class GAImplementation {

	private long SEED;
	private String OUTPUT_FILENAME;
	private String SOURCE_FILENAME;
	private int GENERATION_SPAN;
	private int POPULATION_SIZE;
	private float COMPRESSION_RATE;
	private int TOURNAMENT_SIZE;
	private float CROSSOVER_RATE;
	private float MUTATION_RATE;
	private float ELITISM_RATE;
	private int ELITE_COUNT;
	private int CHROMOSOME_SIZE;
	private int DISTANCE_LIMIT;
	private int GRAPH_SIZE;
	private int RUN_SPAN;
	private Random RANDOM;
	private boolean VALID;
	private BufferedWriter OUTPUT;
	private String CHROMOSOME_TYPE = "";
	private String TEST_TYPE; // runtime (reset seed for all runs) or performance (set seed at beginning only)
	private boolean BFS_CACHE;
	private double DEG_SELECT_RATE;
	private final String DEFAULT_OUTPUT = "";
	private final float DEFAULT_RATE = -Float.MAX_VALUE;
	private final int DEFAULT_SIZE = Integer.MIN_VALUE;

	private Graph ORIGINAL_GRAPH;
	private Chromosome[] POPULATION;
	private int[] POPULATION_FITNESS;

	private static final String IN_DIRECTORY = "data/in/";
	private static final String OUT_DIRECTORY = "data/out/";

	private Map<String, Integer> CACHED_CHROMOSOME_FITNESS;

	// FOR DEBUGGING / INVESTIGATION ONLY
	private Map<String, Set<String>> TRANSFORMED_CHROMOSOMES;
	private Map<String, String> TRANSFORM_MAP;

	//cached fitnesses from valid chromosomes
	private int cacheAccesses;
	// number of times the same invalid chromosome is mapped to a valid one
	private int transformMapUses;
	// number of times a transformed chromosome is able to have fitness retrieved from the cache instead of collecting the fake links from the graph again
	private int postTransformCacheAccesses;
	// total number of evaluations
	private int evaluations;

	private boolean NOISY; // flag to enable/disable noisy console output

	private boolean SAVE_TRANSFORM = false; //save the first transformation of invalid chromosomes for future reuse

	/**
	 * Builds GA based on configuration file at fileLocation
	 *
	 * @param seed seed for the random number generator, can used system time as a randomly generated seed
	 *                or a fixed seed
	 * @param fileLocation filepath for the configuration file which specifies the parameters to be used
	 *                       and the input data source
	 */
	public GAImplementation(long seed, String fileLocation, boolean noisy) {
		this.NOISY = noisy;
		this.SEED = seed;
		this.RANDOM = new Random(this.SEED);
		if (!buildData(IN_DIRECTORY + fileLocation)) {
			return;
		}
		// compression suffix
		this.OUTPUT_FILENAME += "_cmp" + (int) (this.COMPRESSION_RATE * 100);
		// distance suffix
		this.OUTPUT_FILENAME += "_dst" + this.DISTANCE_LIMIT;
		// mutation suffix
		this.OUTPUT_FILENAME += "_mut" + (int) (this.MUTATION_RATE * 100);
		// crossover suffix
		this.OUTPUT_FILENAME += "_xvr" + (int) (this.CROSSOVER_RATE * 100);
		// run span
		this.OUTPUT_FILENAME += "_run" + this.RUN_SPAN;
		// generation span
		this.OUTPUT_FILENAME += "_gen" + this.GENERATION_SPAN;
		// chromosome type
		this.OUTPUT_FILENAME += "_type" + this.CHROMOSOME_TYPE;
		// save_transform
		this.OUTPUT_FILENAME += "_st-" + this.SAVE_TRANSFORM;
		// bfs_cache
		this.OUTPUT_FILENAME += "_bfsc-" +this.BFS_CACHE;
		if (this.CHROMOSOME_TYPE.equals("DEGREE") || this.CHROMOSOME_TYPE.equals("DEGREE2")) {
			this.OUTPUT_FILENAME += "_degr" + this.DEG_SELECT_RATE;
		}
		// seed suffix
		this.OUTPUT_FILENAME += "_" + this.SEED + ".csv";
	}

	/**
	 * Runs the Genetic Algorithm and saves data about GA run to output file
	 */
	public void run() {
		if (this.VALID) {
			try {
				FileWriter fw = new FileWriter(OUT_DIRECTORY + this.OUTPUT_FILENAME);
				this.OUTPUT = new BufferedWriter(fw);
			} catch (Exception e) {
				System.out.println("Error creating write file: " + e.getMessage());
			}
			try {
				// write the constant info on the first line
				this.OUTPUT.write("Source: " + this.SOURCE_FILENAME
						+ "; Type: " + this.CHROMOSOME_TYPE
						+ "; Seed: " + this.SEED
						+ "; Graph Size: " + this.GRAPH_SIZE
						+ "; Population Size: " + this.POPULATION_SIZE
						+ "; Compression Rate: " + String.format("%.5f%%", this.CHROMOSOME_SIZE / Double.valueOf(this.GRAPH_SIZE))
						+ "; Chromosome Size: " + this.CHROMOSOME_SIZE
						+ "; Elitism Rate: " + String.format("%.5f%%", this.ELITE_COUNT / Double.valueOf(this.GRAPH_SIZE))
						+ "; Elite Size: " + this.ELITE_COUNT
						+ "; Tournament Size: " + this.TOURNAMENT_SIZE
						+ "; Mutation Rate: " + String.format("%.5f%%", this.MUTATION_RATE)
						+ "; Crossover Rate: " + String.format("%.5f%%", this.CROSSOVER_RATE)
						+ "; Maximum Distance: " + this.DISTANCE_LIMIT
						+ "; Run Span: " + this.RUN_SPAN
						+ "; Generation Span: " + this.GENERATION_SPAN
						+ "; BFS Caching: " + this.BFS_CACHE
						+ "; Save Transformations: " + this.SAVE_TRANSFORM
						+ "; Degree Select Rate: " + this.DEG_SELECT_RATE);
				this.OUTPUT.newLine();
				// CSV Columns
				this.OUTPUT.write("Run,"
						+ "Generation,"
						+ "Time to Complete,"
						+ "Global Best Fitness,"
						+ "Global Average Fitness,"
						+ "Global Worst Fitness,"
						+ "Run Best Fitness,"
						+ "Run Average Fitness,"
						+ "Run Worst Fitness,"
						+ "Generation Best Fitness,"
						+ "Generation Average Fitness,"
						+ "Generation Worst Fitness,"
						+ "Global Best Chromosome,"
						+ "Run Best Chromosome,"
						+ "Generation Best Chromosome,"
						+ "Fitness Cache Accesses,"
						+ "Post-Transform Fitness Cache Accesses,"
						+ "Transform Map Uses,"
						+ "Evaluations,"
						+ "Fitness Cache Size,"
				);
				this.OUTPUT.newLine();
				this.OUTPUT.flush();
			} catch (Exception e) {
				System.out.println("Unable to write to file: " + e.getMessage());
			}

			this.CACHED_CHROMOSOME_FITNESS = new HashMap<>();

			this.TRANSFORMED_CHROMOSOMES = new HashMap<>();
			this.TRANSFORM_MAP = new HashMap<>();

			int totalCacheAccesses = 0;
			int totalEvaluations = 0;

			// initialize global settings
			int globalWorstFitness = Integer.MIN_VALUE;
			int globalBestFitness = Integer.MAX_VALUE;
			Chromosome globalBest = createChromosome();
			Chromosome globalWorst = createChromosome();
			long globalSum = 0;
			this.POPULATION_FITNESS = new int[this.POPULATION_SIZE];
			// Each run
			for (int run = 1; run <= this.RUN_SPAN; run++) {
				// If testing runtime performance, reset the seed at the beginning of each run.
				// This way each run should be identical in workload as it considers the same series of nodes.
				if (this.TEST_TYPE.equals("RUNTIME")) this.RANDOM = new Random(this.SEED);
				initPopulation();

				// Grab initial population fitness
				for (int i = 0; i < this.POPULATION_SIZE; i++) {
					this.POPULATION_FITNESS[i] = this.evaluate(this.POPULATION[i]);
				}

				int runWorstFitness = Integer.MIN_VALUE;
				int runBestFitness = Integer.MAX_VALUE;
				Chromosome runBest = createChromosome();
				Chromosome runWorst = createChromosome();
				long runSum = 0;

				// Each generation
				for (int gen = 1; gen <= this.GENERATION_SPAN; gen++) {
					long startTime = System.currentTimeMillis();
					int genBestFitness = Integer.MAX_VALUE;
					Chromosome genBest = createChromosome();
					int genWorstFitness = Integer.MIN_VALUE;
					Chromosome genWorst = createChromosome();
					long genSum = 0;

					//caching impact investigation
					this.cacheAccesses = 0;
					this.evaluations = 0;
					this.postTransformCacheAccesses = 0;
					this.transformMapUses = 0;

					if (this.NOISY) System.out.println("Thread " + Thread.currentThread().getId() + " Run " + run + " Generation " + gen);

					// Elitism
					Chromosome[] generation = this.getElitePopulation();
					// Apply crossover and mutation to generate the rest of the population
					for (int c = this.ELITE_COUNT; c < this.POPULATION_SIZE; c += 2) {
						// Get parents via tournament selection
						Chromosome parent1 = this.tournamentSelection();
						Chromosome parent2 = this.tournamentSelection();
						// Apply crossover
						if (this.RANDOM.nextDouble() < this.CROSSOVER_RATE) {
							this.crossover(parent1, parent2);
						}
						// Apply mutation
						if (this.RANDOM.nextDouble() < this.MUTATION_RATE) {
							parent1.mutate((LinkedGraph) this.ORIGINAL_GRAPH);
						}
						if (this.RANDOM.nextDouble() < this.MUTATION_RATE) {
							parent2.mutate((LinkedGraph) this.ORIGINAL_GRAPH);
						}
						// Add to generation
						if (c + 1 == this.POPULATION_SIZE) {
							Chromosome randomParent = this.RANDOM.nextBoolean() ? parent1 : parent2;
							generation[c] = randomParent;
						} else {
							generation[c] = parent1;
							generation[c + 1] = parent2;
						}
					}
					// Collect fitnesses
					for (int i = 0; i < this.POPULATION_SIZE; i++) {
						int fitness = this.evaluate(generation[i]);
						this.POPULATION_FITNESS[i] = fitness;
						// Collect generation, run, global statistics
						if (fitness < genBestFitness) {
							genBest = generation[i].copy();
							genBestFitness = fitness;
							if (genBestFitness < runBestFitness) {
								runBest = genBest.copy();
								runBestFitness = genBestFitness;
								if (runBestFitness < globalBestFitness) {
									globalBest = runBest.copy();
									globalBestFitness = runBestFitness;
								}
							}
						}
						if (fitness > genWorstFitness) {
							genWorst = generation[i].copy();
							genWorstFitness = fitness;
							if (genWorstFitness > runWorstFitness) {
								runWorst = genWorst.copy();
								runWorstFitness = genWorstFitness;
								if (runWorstFitness > globalWorstFitness) {
									globalWorst = runWorst.copy();
									globalWorstFitness = runWorstFitness;
								}
							}
						}
						globalSum += fitness;
						genSum += fitness;
						runSum += fitness;

						this.POPULATION[i] = generation[i];
					}
					// Output the results
					try {
						this.OUTPUT.write(run + ","
								+ gen + ","
								+ (System.currentTimeMillis() - startTime) + ","
								+ globalBestFitness + ","
								+ (globalSum / (this.POPULATION_SIZE * ((this.GENERATION_SPAN * (run - 1)) + gen))) + ","
								+ globalWorstFitness + ","
								+ runBestFitness + ","
								+ (runSum / (this.POPULATION_SIZE * gen)) + ","
								+ runWorstFitness + ","
								+ genBestFitness + ","
								+ (genSum / this.POPULATION_SIZE) + ","
								+ genWorstFitness + ","
								+ "\"" + globalBest.toString() + "\","
								+ "\"" + runBest.toString() + "\","
								+ "\"" + genBest.toString() + "\","
								+ this.cacheAccesses + ","
								+ this.postTransformCacheAccesses + ","
								+ this.transformMapUses + ","
								+ this.evaluations + ","
								+ this.CACHED_CHROMOSOME_FITNESS.size() + ","
						);
						this.OUTPUT.newLine();
						this.OUTPUT.flush();
					} catch (Exception e) {
						System.out.println("Unable to write to file: " + e.getMessage());
					}
					if (this.NOISY) {
						System.out.println("Global Sum: " + globalSum);
						System.out.println("Global total: " + (this.POPULATION_SIZE * ((this.GENERATION_SPAN * (run - 1)) + gen)));
						System.out.println("Run Sum: " + runSum);
						System.out.println("Run total: " + (this.POPULATION_SIZE * gen));
						System.out.println("Generation Sum: " + genSum);
						System.out.println("Generation total: " + this.POPULATION_SIZE);
					}
					totalCacheAccesses += this.cacheAccesses;
					totalEvaluations += this.evaluations;
				}
				// DEBUGGING/ADDITIONAL STATS
				System.out.println("Run " + run);
				System.out.println("Overall size of chromosome fitness cache: " + this.CACHED_CHROMOSOME_FITNESS.size());
				System.out.println("Overall cache accesses: " + totalCacheAccesses);
				System.out.println("Overall evaluations: " + totalEvaluations);
			}
			
			//DEBUGGING/INVESTIGATION ONLY
			int numTransformed = this.TRANSFORMED_CHROMOSOMES.size();
			int totalResults = 0;
			int numMoreThanOne = 0;
			int maxVariants = 0;
			for (Map.Entry<String, Set<String>> entry : this.TRANSFORMED_CHROMOSOMES.entrySet()) {
				String key = entry.getKey();
				Set transformedValues = entry.getValue();
				int variations = transformedValues.size();
				totalResults += variations;
				if (variations > 1) numMoreThanOne ++;
				if (variations > maxVariants) maxVariants = variations;
			}
			System.out.println(numTransformed + " chromosomes transformed.");
			System.out.println(totalResults + " total resulting chromosomes.");
			System.out.println(numMoreThanOne + "/" + numTransformed + " had more than one resulting transformation.");
			System.out.println((double)totalResults / (double) numTransformed + " average different results per transformed chromosome.");
			System.out.println("Maximum number of different variations from same starting chromosome: " + maxVariants);

			try {
				this.OUTPUT.close();
			} catch (Exception e) {
				System.out.println("Unable to close file: " + e.getMessage());
			}
		}
	}

	/**
	 * Returns a copy of the chromosome at specified index
	 *
	 * @param index of the chromosome within the population
	 * @return a copy of the chromosome, null if the index is out of bounds.
	 */
	public Chromosome getChromosome(int index) {
		if (index >= this.POPULATION_SIZE) {
			return null;
		}
		return (this.POPULATION[index].copy());
	}

	/**
	 * Prints the string representation of the specified chromosome
	 *
	 * @param chromosome the chromosome to print
	 */
	public static void print(int[][] chromosome) {
		System.out.print(GAImplementation.buildChromosomeString(chromosome));
	}

	/**
	 * Prints specific chromosomes with a newline at the end
	 *
	 * @param chromosome the chromosome to print
	 */
	public static void println(int[][] chromosome) {
		print(chromosome);
		System.out.println();
	}


	/**
	 * Initializes the population with random data
	 */
	private void initPopulation() {
		if (!VALID) {
			return;
		}
		this.POPULATION = new Chromosome[this.POPULATION_SIZE];
		for (int c = 0; c < this.POPULATION_SIZE; c++) {
			this.POPULATION[c] = createChromosome();
			this.POPULATION[c].init((LinkedGraph) this.ORIGINAL_GRAPH);
		}
	}

	/**
	 * Randomly selects a preset number of individual chromosomes, returns the best
	 * chromosome of the selected set
	 *
	 * @return the chromosome with the best fitness value of the selected set
	 */
	public Chromosome tournamentSelection() {
		int best = Integer.MAX_VALUE;
		Chromosome winner = createChromosome();
		for (int i = 0; i < this.TOURNAMENT_SIZE; i++) {
			int randomIndex = this.RANDOM.nextInt(this.POPULATION_SIZE);
			Chromosome participant = this.getChromosome(randomIndex);
			int fitness = this.evaluatePrevious(randomIndex);
			if (fitness < best) {
				best = fitness;
				winner = participant;
			}
		}
		return winner;
	}

	/**
	 * Performs 2 point crossover on the two given chromosomes.
	 * Order in which the chromosomes are supplied to the method is not important.
	 *
	 * @param chromosome1 the first chromosome involved in the crossover
	 * @param chromosome2 the second chromosome involved in the crossover
	 */
	public void crossover(Chromosome chromosome1, Chromosome chromosome2) {
		if (!VALID) {
			return;
		}
		int start = this.RANDOM.nextInt(CHROMOSOME_SIZE);
		int end = this.RANDOM.nextInt(CHROMOSOME_SIZE - start) + start;
		for (int i = 0; i < this.CHROMOSOME_SIZE; i++) {
			if (i >= start && i <= end) {
				int tempRoot = chromosome1.genes[i][0];
				chromosome1.genes[i][0] = chromosome2.genes[i][0];
				chromosome2.genes[i][0] = tempRoot;
				int tempOffset = chromosome1.genes[i][1];
				chromosome1.genes[i][1] = chromosome2.genes[i][1];
				chromosome2.genes[i][1] = tempOffset;
			}
		}
	}

	/**
	 * Fitness function. Evaluates the number of fake links created as a result of the merge-sequence specified by the
	 * chromosome. Updates chromosome if it has some invalid genes (these are randomly replaced with new, valid genes).
	 *
	 * @param chromosome The chromosome to evaluate
	 * @return The fitness of the chromosome
	 */
	public int evaluate(Chromosome chromosome) {
		this.evaluations++;
		String chromosomeString = chromosome.toString();
		if (this.CACHED_CHROMOSOME_FITNESS.containsKey(chromosomeString)) {
			this.cacheAccesses++;
			return this.CACHED_CHROMOSOME_FITNESS.get(chromosomeString);
		}

		// check to see if it's an invalid chromosome that has been transformed before
		if (this.SAVE_TRANSFORM && this.TRANSFORM_MAP.containsKey(chromosomeString)) {
			String c = this.TRANSFORM_MAP.get(chromosomeString);
			if(this.CACHED_CHROMOSOME_FITNESS.containsKey(c)) {
				this.transformMapUses++;
				return this.CACHED_CHROMOSOME_FITNESS.get(c);
			}
		}

		// if we don't already have the fitness calculation, begin the process for calculation
		LinkedGraph current = (LinkedGraph) this.ORIGINAL_GRAPH.deepCopy();
		// iterate through each gene, applying the changes to the graph
		for (int i = 0; i < this.CHROMOSOME_SIZE; i++) {
			chromosome.validateGene(i, current);
			chromosome.applyGene(i, current);
		}
		// re-build the string representing the chromosome, may have been altered
		// during the evaluation process to remove invalid merges
		// check if the validated gene fitness has already been calculated
		String currentChromosomeString = chromosome.toString();

		// If not saving the same transformation, measure the number of new variants
		if (!currentChromosomeString.equals(chromosomeString)) {
			if(!this.SAVE_TRANSFORM) { // DEBUGGING / MEASURING
				// if it's already in there, add the currentChromosome to the set of transformations
				if (this.TRANSFORMED_CHROMOSOMES.containsKey(chromosomeString)) {
					this.TRANSFORMED_CHROMOSOMES.get(chromosomeString).add(currentChromosomeString);
				} else { //add it
					Set transformations = new HashSet<String>();
					transformations.add(currentChromosomeString);
					this.TRANSFORMED_CHROMOSOMES.put(chromosomeString, transformations);
				}
			} else { // this should only ever happen once
				this.TRANSFORM_MAP.put(chromosomeString, currentChromosomeString);
			}
		}

		// check if we have the fitness for this new chromosome already, if so, return that
		if (this.CACHED_CHROMOSOME_FITNESS.containsKey(currentChromosomeString)) {
			this.postTransformCacheAccesses++;
			return this.CACHED_CHROMOSOME_FITNESS.get(currentChromosomeString);
		}

		// determine the number of fake links introduced into the graph as a result
		int fitness = current.totalFakeLinks();

		// put the new fitness in the cache
		this.CACHED_CHROMOSOME_FITNESS.put(currentChromosomeString, fitness);

		return fitness;
	}

	/**
	 * Returns the fitness from the previous generation
	 *
	 * @param chromosome the index of the chromosome in the population
	 * @return the fitness of the chromosome, as evaluated in the previous generation
	 */
	public int evaluatePrevious(int chromosome) {
		// wrapper function primarily for sanity
		return this.POPULATION_FITNESS[chromosome];
	}

	/**
	 * Creates a new population and pre-populates it with the specified number of elites from the previous generation.
	 *
	 * @return newPopulation, an integer array with the first ELITE_COUNT positions holding the best chromosomes
	 * from the last generation, the rest of the positions remaining empty
	 */
	public Chromosome[] getElitePopulation() {
		Chromosome[] newPop = new Chromosome[this.POPULATION_SIZE];
		PriorityQueue<WrappedNode> fitness = new PriorityQueue<WrappedNode>();
		for (int i = 0; i < this.POPULATION_SIZE; i++) {
			fitness.add(new WrappedNode(i, evaluatePrevious(i)));
		}
		for (int e = 0; e < this.ELITE_COUNT; e++) {
			WrappedNode elite = fitness.remove();
			newPop[e] = this.getChromosome(elite.index);
		}
		return newPop;
	}


	/**
	 * Creates a Chromosome of the correct concrete type based on the CHROMOSOME_TYPE parameter and the other specified
	 * constant parameters.
	 * @return a new Chromosome object
	 */
	public Chromosome createChromosome(){
		switch (this.CHROMOSOME_TYPE) {
			case "BFS":
				return new BFSChromosome(this.RANDOM, this.CHROMOSOME_SIZE, this.DISTANCE_LIMIT);
			case "RANDOMADD":
				return new RandomChromosome(this.RANDOM, this.CHROMOSOME_SIZE, this.DISTANCE_LIMIT);
			case "FIXED":
				return new FixedChromosome(this.RANDOM, this.CHROMOSOME_SIZE, this.DISTANCE_LIMIT);
			case "UNRESTRICTED":
				return new UnrestrictedChromosome(this.RANDOM, this.CHROMOSOME_SIZE, this.DISTANCE_LIMIT);
			case "DEGREE":
				return new DegreeChromosome(this.RANDOM, this.CHROMOSOME_SIZE, this.DISTANCE_LIMIT, this.DEG_SELECT_RATE);
			case "DEGREE2":
				return new Degree2Chromosome(this.RANDOM, this.CHROMOSOME_SIZE, this.DISTANCE_LIMIT, this.DEG_SELECT_RATE);
			default:
				return new BFSChromosome(this.RANDOM, this.CHROMOSOME_SIZE, this.DISTANCE_LIMIT);
		}
	}

	// DEFAULTS AND DATA VALIDATION

	/**
	 * Sets all parameters to their default values.
	 */
	private void buildDefaults() {
		this.OUTPUT_FILENAME = DEFAULT_OUTPUT;
		this.SOURCE_FILENAME = DEFAULT_OUTPUT;
		this.GENERATION_SPAN = DEFAULT_SIZE;
		this.POPULATION_SIZE = DEFAULT_SIZE;
		this.TOURNAMENT_SIZE = DEFAULT_SIZE;
		this.CHROMOSOME_SIZE = DEFAULT_SIZE;
		this.DISTANCE_LIMIT = DEFAULT_SIZE;
		this.ELITE_COUNT = DEFAULT_SIZE;
		this.GRAPH_SIZE = DEFAULT_SIZE;
		this.COMPRESSION_RATE = DEFAULT_RATE;
		this.CROSSOVER_RATE = DEFAULT_RATE;
		this.MUTATION_RATE = DEFAULT_RATE;
		this.ELITISM_RATE = DEFAULT_RATE;
		this.RUN_SPAN = 1;
		this.CHROMOSOME_TYPE = "BFS";
		this.TEST_TYPE  = "PERFORMANCE"; // set seed once at the beginning, do not reset each run.
		this.DEG_SELECT_RATE = 1.0; //always choose the neighbour with min degree
	}

	/**
	 * Checks that the parameters set through the provided configuration are valid (fall within allowable ranges etc).
	 * Sets the "VALID" variable accordingly.
	 * @return True if the parameters are valid, False otherwise.
	 */
	private boolean isProperlyBuilt() {
		this.VALID = true;
		if (this.OUTPUT_FILENAME.equals(DEFAULT_OUTPUT)) {
			System.out.println("Output filename not specified"
					+ ", use parameter: outPrefix");
			this.VALID = false;
		}
		if (this.SOURCE_FILENAME.equals(DEFAULT_OUTPUT)) {
			System.out.println("Source filename not specified"
					+ ", use parameter: source");
			this.VALID = false;
		}
		if (this.COMPRESSION_RATE < 1.0 && this.COMPRESSION_RATE > 0.0) {
			this.CHROMOSOME_SIZE = (int) (this.COMPRESSION_RATE * this.GRAPH_SIZE);
		} else {
			if (this.CHROMOSOME_SIZE < 1) {
				System.out.println("Compression rate invalid"
						+ ", use parameter: compression [0.0,1.0]");
				this.VALID = false;
			} else {
				System.out.println("Compression rate invalid"
						+ ", defaulting to parameter: chromosome "
						+ this.CHROMOSOME_SIZE
				);
			}
		}
		if (this.POPULATION_SIZE < 1) {
			System.out.println("Population size invalid"
					+ ", use parameter: population [1,infinity)");
		}
		if (this.TOURNAMENT_SIZE < 1) {
			System.out.println("Tournament size invalid"
					+ ", use parameter: tournament [1,population_size]");
			this.VALID = false;
		} else if (this.TOURNAMENT_SIZE > this.POPULATION_SIZE) {
			this.TOURNAMENT_SIZE = this.POPULATION_SIZE;
		}

		if (this.GENERATION_SPAN < 1) {
			System.out.println("Generation size invalid"
					+ ", use parameter: generations [1,infinity)");
			this.VALID = false;
		}
		if (this.DISTANCE_LIMIT < 1) {
			System.out.println("Distance limit invalid"
					+ ", use parameter: maxDistance [1,infinity)");
			this.VALID = false;
		}
		if (this.TOURNAMENT_SIZE < 1) {
			System.out.println("Tournament size invalid"
					+ ", use parameter: tournament [1,infinity)");
			this.VALID = false;
		}
		if (this.CROSSOVER_RATE > 1.0 || this.CROSSOVER_RATE < 0.0) {
			System.out.println("Crossover rate invalid"
					+ ", use parameter: crossover [0.0,1.0]");
			this.VALID = false;
		}
		if (this.MUTATION_RATE > 1.0 || this.MUTATION_RATE < 0.0) {
			System.out.println("Mutation rate invalid"
					+ ", use parameter: mutation [0.0,1.0]");
			this.VALID = false;
		}
		if (this.ELITISM_RATE < 1.0 && this.ELITISM_RATE > 0.0) {
			System.out.println(this.ELITISM_RATE);
			this.ELITE_COUNT = (int) (this.ELITISM_RATE * this.POPULATION_SIZE);
			if (this.ELITE_COUNT < 1 || this.ELITE_COUNT >= this.POPULATION_SIZE) {
				System.out.println("Invalid number of elites: " + this.ELITE_COUNT);
				this.VALID = false;
			}
		} else {
			if (this.ELITE_COUNT < 1) {
				System.out.println("Elitism rate invalid"
						+ ", use parameter: elitism [0.0,1.0]");
				this.VALID = false;
			} else {
				System.out.println("Elitism rate invalid"
						+ ", defaulting to parameter: elites "
						+ this.ELITE_COUNT
				);
				if (this.ELITE_COUNT < 1 || this.ELITE_COUNT >= this.POPULATION_SIZE) {
					System.out.println("Invalid number of elites: " + this.ELITE_COUNT);
					this.VALID = false;
				}
			}
		}
		if (this.RUN_SPAN < 0) {
			System.out.println("Run size invalid"
					+ ", use parameter: runs [1,infinity]");
			this.VALID = false;
		}
		return this.VALID;
	}

	/**
	 * Reads configuration file and sets GA parameters, inputs, and outputs accordingly.
	 * Runs a check to ensure the specified configuration is valid.
	 * @param filename the path of the configuration file
	 * @return True if the configuration is valid, False otherwise
	 */
	private boolean buildData(String filename) {
		buildDefaults();
		try {
			List<String> lines = Files.readAllLines(Paths.get(filename), Charset.defaultCharset());
			for (String line : lines) {
				String[] data = line.split("[\\s\\t:=]+");
				//System.out.println("["+String.join(",",data)+"]");
				if (data.length != 2) {
					continue;
				}
				switch (data[0].trim()) {
					case "compression":
						this.COMPRESSION_RATE = Float.parseFloat(data[1].trim());
						break;
					case "generations":
						this.GENERATION_SPAN = Integer.parseInt(data[1].trim());
						break;
					case "tournament":
						this.TOURNAMENT_SIZE = Integer.parseInt(data[1].trim());
						break;
					case "crossover":
						this.CROSSOVER_RATE = Float.parseFloat(data[1].trim());
						break;
					case "mutation":
						this.MUTATION_RATE = Float.parseFloat(data[1].trim());
						break;
					case "elitism":
						this.ELITISM_RATE = Float.parseFloat(data[1].trim());
						break;
					case "elites":
						this.ELITE_COUNT = Integer.parseInt(data[1].trim());
						break;
					case "chromosome":
						this.CHROMOSOME_SIZE = Integer.parseInt(data[1].trim());
						break;
					case "population":
						this.POPULATION_SIZE = Integer.parseInt(data[1].trim());
						break;
					case "outPrefix":
						this.OUTPUT_FILENAME = data[1].trim();
						break;
					case "maxDistance":
						this.DISTANCE_LIMIT = Integer.parseInt(data[1].trim());
						break;
					case "runs":
						this.RUN_SPAN = Integer.parseInt(data[1].trim());
						break;
					case "source":
						this.SOURCE_FILENAME = data[1].trim();
						LinkedGraph g = LinkedGraph.load(this.SOURCE_FILENAME);
						this.GRAPH_SIZE = g.getSize();
						this.ORIGINAL_GRAPH = g;
						break;
					case "type":
						this.CHROMOSOME_TYPE = data[1].trim().toUpperCase();
						break;
					case "testType":
						this.TEST_TYPE = data[1].trim().toUpperCase();
						break;
					case "cache":
						if (data[1].trim().toUpperCase().equals("TRUE")) {
							// this is terrible, but it will work
							((LinkedGraph) this.ORIGINAL_GRAPH).setCache(true);
							this.BFS_CACHE = true;
						} else {
							this.BFS_CACHE = false;
						}
						break;
					case "saveTransform":
						if (data[1].trim().toUpperCase().equals("TRUE")) {
							this.SAVE_TRANSFORM = true;
						} else {
							this.SAVE_TRANSFORM = false;
						}
						break;
					case "degreeSelectRate":
						this.DEG_SELECT_RATE = Float.parseFloat(data[1].trim());
						break;
					default:
						break;
				}
			}
		} catch (IOException e) {
			System.out.println("Error reading from file: " + e.getMessage());
			isProperlyBuilt();
			buildDefaults();
			return false;
		} catch (NumberFormatException e) {
			System.out.println("Error converting number: " + e.getMessage());
			isProperlyBuilt();
			buildDefaults();
			return false;
		}
		return isProperlyBuilt();
	}

	/**
	 * Returns string representation of chromosome
	 *
	 * @param chromosome
	 * @return
	 */
	public static String buildChromosomeString(int[][] chromosome) {
		String output = "[";
		for (int i = 0; i < chromosome.length; i++) {
			if (i > 0) {
				output += ",";
			}
			output += "(";
			for (int j = 0; j < chromosome[i].length; j++) {
				if (j > 0) {
					output += ",";
				}
				output += chromosome[i][j];
			}
			output += ")";
		}
		output += "]";
		return output;
	}

	/**
	 * Builds a graph which has been compressed by applying the sequence of merges specified in the chromosome,
	 * using the supplied testGraph as the original starting graph.
	 * @param testGraph initial graph to compress using the chromosome
	 * @param chromosome string representation of a chromosome specifying the sequence of merges to make within the graph
	 * @return a new graph which has been compressed using the chromosome
	 */
	public static LinkedGraph buildChromosome(LinkedGraph testGraph, String chromosome) {
		LinkedGraph graph = testGraph.deepCopy();
		chromosome = chromosome.replaceAll("\\]", "");
		chromosome = chromosome.replaceAll("\\[", "");
		String[] chromosomes = chromosome.split("\\),\\(");
		int sum = 0;

		for (String c : chromosomes) {
			c = c.replaceAll("\\(", "");
			c = c.replaceAll("\\)", "");
			String[] gene = c.split(",");
			int from = Integer.valueOf(gene[0]);
			int to = (Integer.valueOf(gene[1]) + from) % graph.getSize();
			graph.merge(from, to);
		}

		return graph;
	}

	/**
	 * Shows step by step fitness evaluation of chromosome
	 *
	 * @param testGraph
	 * @param chromosome
	 */
	public static void viewChromosome(LinkedGraph testGraph, String chromosome) {
		LinkedGraph graph = buildChromosome(testGraph, chromosome);
		System.out.println("Should be " + graph.totalFakeLinks() + " fitness");
		printChromosome(graph);
	}

	public static void printChromosome(LinkedGraph g) {
		g.print();
		GraphDisplay.displayLinkedGraph(g);
	}
}
