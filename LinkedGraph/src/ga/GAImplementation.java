package ga;

import display.GraphDisplay;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.LinkedList;
import java.util.Random;

import graph.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.PriorityQueue;
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
chromesome 10
runs 10
source ecoli.txt

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
	private int CHROMESOME_SIZE;
	private int DISTANCE_LIMIT;
	private int GRAPH_SIZE;
	private int RUN_SPAN;
	private Random RANDOM;
	private boolean VALID;
	private BufferedWriter OUTPUT;

	private final String DEFAULT_OUTPUT = "";
	private final float DEFAULT_RATE = Float.MIN_VALUE;
	private final int DEFAULT_SIZE = Integer.MIN_VALUE;

	private Graph ORIGINAL_GRAPH;
	private int[][][] POPULATION;
	private int[] POPULATION_FITNESS;

	private static final String IN_DIRECTORY = "data/in/";
	private static final String OUT_DIRECTORY = "data/out/";

	private List<List<Integer>> ORIGINAL_NEIGHBORHOODS;

	/**
	 * builds GA based on fileLocation file
	 *
	 * @param timeSuffix
	 * @param fileLocation
	 */
	public GAImplementation(long timeSuffix, String fileLocation) {
		this.SEED = timeSuffix;
		this.RANDOM = new Random(this.SEED);
		if (!buildData(IN_DIRECTORY + fileLocation)) {
			return;
		}
		if (this.COMPRESSION_RATE > 0 && this.COMPRESSION_RATE <= 1) {
			this.CHROMESOME_SIZE = (int) (this.COMPRESSION_RATE * this.GRAPH_SIZE);
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
		// seed suffix
		this.OUTPUT_FILENAME += "_" + this.SEED + ".csv";
	}

	/**
	 * Runs the Genetic Algorithm
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
				// CSV Columns
				this.OUTPUT.write("Source,"
						+ "Seed,"
						+ "Graph Size,"
						+ "Compression Rate,"
						+ "Elitism Rate,"
						+ "Tournament Size,"
						+ "Mutation Rate,"
						+ "Crossover Rate,"
						+ "Maximum Distance,"
						+ "Run Span,"
						+ "Run,"
						+ "Generation Span,"
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
						+ "Global Best Chromesome,"
						+ "Run Best Chromesome,"
						+ "Generation Best Chromesome,"
				);
				this.OUTPUT.newLine();
				this.OUTPUT.flush();
			} catch (Exception e) {
				System.out.println("Unable to write to file: " + e.getMessage());
			}
			this.ORIGINAL_NEIGHBORHOODS = new LinkedList<>();
			for (int i = 0; i < this.GRAPH_SIZE; i++) {
				this.ORIGINAL_NEIGHBORHOODS.add(new LinkedList<Integer>());
			}
			// initialize global settings
			int globalWorstFitness = Integer.MIN_VALUE;
			int globalBestFitness = Integer.MAX_VALUE;
			int[][] globalBest = new int[CHROMESOME_SIZE][2];
			int[][] globalWorst = new int[CHROMESOME_SIZE][2];
			long globalSum = 0;
			this.POPULATION_FITNESS = new int[this.POPULATION_SIZE];
			// Each Run
			for (int run = 1; run <= this.RUN_SPAN; run++) {
				initPopulation();

				// Grab initial population fitness
				for (int i = 0; i < this.POPULATION_SIZE; i++) {
					this.POPULATION_FITNESS[i] = this.Evaluate(this.POPULATION[i]);
				}

				int runWorstFitness = Integer.MIN_VALUE;
				int runBestFitness = Integer.MAX_VALUE;
				int[][] runBest = new int[CHROMESOME_SIZE][2];
				int[][] runWorst = new int[CHROMESOME_SIZE][2];
				long runSum = 0;

				// Each Generation
				for (int gen = 1; gen <= this.GENERATION_SPAN; gen++) {
					long startTime = System.currentTimeMillis();
					int genBestFitness = Integer.MAX_VALUE;
					int[][] genBest = new int[CHROMESOME_SIZE][2];
					int genWorstFitness = Integer.MIN_VALUE;
					int[][] genWorst = new int[CHROMESOME_SIZE][2];
					long genSum = 0;
					System.out.println("Thread " + Thread.currentThread().getId() + " Run " + run + " Generation " + gen);

					// Elitism
					int[][][] generation = this.getElitePopulation();
					for (int c = this.ELITE_COUNT; c < this.POPULATION_SIZE; c += 2) {
						// Get Parents via tournament selection
						int[][] parent1 = this.TournamentSelection();
						int[][] parent2 = this.TournamentSelection();
						// Apply Crossover
						if (this.RANDOM.nextDouble() < this.CROSSOVER_RATE) {
							this.Crossover(parent1, parent2);
						}
						// Apply Mutation
						if (this.RANDOM.nextDouble() < this.MUTATION_RATE) {
							this.Mutate(parent1);
						}
						if (this.RANDOM.nextDouble() < this.MUTATION_RATE) {
							this.Mutate(parent2);
						}
						// Add to generation
						if (c + 1 == this.POPULATION_SIZE) {
							int[][] randomParent = this.RANDOM.nextBoolean() ? parent1 : parent2;
							generation[c] = randomParent;
						} else {
							generation[c] = parent1;
							generation[c + 1] = parent2;
						}
					}
					// Collect fitnesses
					for (int i = 0; i < this.POPULATION_SIZE; i++) {
						int fitness = this.Evaluate(generation[i]);
						this.POPULATION_FITNESS[i] = fitness;
						// Collect Generation, Run, Global statistics
						if (fitness < genBestFitness) {
							genBest = Copy(generation[i]);
							genBestFitness = fitness;
							if (genBestFitness < runBestFitness) {
								runBest = Copy(genBest);
								runBestFitness = genBestFitness;
								if (runBestFitness < globalBestFitness) {
									globalBest = Copy(runBest);
									globalBestFitness = runBestFitness;
								}
							}
						}
						if (fitness > genWorstFitness) {
							genWorst = Copy(generation[i]);
							genWorstFitness = fitness;
							if (genWorstFitness > runWorstFitness) {
								runWorst = Copy(genWorst);
								runWorstFitness = genWorstFitness;
								if (runWorstFitness > globalWorstFitness) {
									globalWorst = Copy(runWorst);
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
						this.OUTPUT.write(this.SOURCE_FILENAME + ","
								+ this.SEED + ","
								+ this.GRAPH_SIZE + ","
								+ this.COMPRESSION_RATE + ","
								+ this.ELITISM_RATE + ","
								+ this.TOURNAMENT_SIZE + ","
								+ this.MUTATION_RATE + ","
								+ this.CROSSOVER_RATE + ","
								+ this.DISTANCE_LIMIT + ","
								+ this.RUN_SPAN + ","
								+ run + ","
								+ this.GENERATION_SPAN + ","
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
								+ "\"" + GAImplementation.buildChromesomeString(globalBest) + "\","
								+ "\"" + GAImplementation.buildChromesomeString(runBest) + "\","
								+ "\"" + GAImplementation.buildChromesomeString(genBest) + "\","
						);
						this.OUTPUT.newLine();
						this.OUTPUT.flush();
					} catch (Exception e) {
						System.out.println("Unable to write to file: " + e.getMessage());
					}
					System.out.println("Global Sum: " + globalSum);
					System.out.println("Global total: " + (this.POPULATION_SIZE * ((this.GENERATION_SPAN * (run - 1)) + gen)));
					System.out.println("Run Sum: " + runSum);
					System.out.println("Run total: " + (this.POPULATION_SIZE * gen));
					System.out.println("Generation Sum: " + genSum);
					System.out.println("Generation total: " + this.POPULATION_SIZE);
				}
			}

			try {
				this.OUTPUT.close();
			} catch (Exception e) {
				System.out.println("Unable to close file: " + e.getMessage());
			}
		}

	}

	/**
	 * returns chromesome at specified index
	 *
	 * @param index
	 * @return
	 */
	public int[][] getChromesome(int index) {
		if (index >= this.POPULATION_SIZE) {
			return null;
		}
		return Copy(this.POPULATION[index]);
	}

	/**
	 * prints all the chromesomes
	 */
	public void print() {
		for (int i = 0; i < this.POPULATION_SIZE; i++) {
			System.out.print("Chromesome " + i + " ");
			println(this.POPULATION[i]);
		}
	}

	/**
	 * prints specific chromesomes
	 *
	 * @param chromesome
	 */
	public static void print(int[][] chromesome) {
		System.out.print(GAImplementation.buildChromesomeString(chromesome));
	}

	/**
	 * prints specific chromesomes with a newline at the end
	 *
	 * @param chromesome
	 */
	public static void println(int[][] chromesome) {
		print(chromesome);
		System.out.println();
	}

	/*
		initializes the population with random data
	 */
	private void initPopulation() {
		if (!VALID) {
			return;
		}
		this.POPULATION = new int[this.POPULATION_SIZE][this.CHROMESOME_SIZE][2];
		for (int c = 0; c < this.POPULATION_SIZE; c++) {
			for (int g = 0; g < this.CHROMESOME_SIZE; g++) {
				this.MutateGene(this.POPULATION[c][g]);
			}
		}
	}

	/**
	 * deep copy chromesome
	 *
	 * @param pairs
	 * @return
	 */
	public static int[][] Copy(int[][] pairs) {
		int[][] returnPairs = new int[pairs.length][2];
		for (int i = 0; i < pairs.length; i++) {
			returnPairs[i][0] = pairs[i][0]; // root
			returnPairs[i][1] = pairs[i][1]; // offset
		}
		return returnPairs;
	}

	/**
	 * Gets a preset number of individual chromesomes, return the best
	 * chromesome
	 *
	 * @return chromesome
	 */
	public int[][] TournamentSelection() {
		int best = Integer.MAX_VALUE;
		int[][] winner = new int[this.CHROMESOME_SIZE][2];
		for (int i = 0; i < this.TOURNAMENT_SIZE; i++) {
			int randomIndex = this.RANDOM.nextInt(this.POPULATION_SIZE);
			int[][] participant = this.getChromesome(randomIndex);
			int fitness = this.EvaluatePrevious(randomIndex);
			if (fitness < best) {
				best = fitness;
				winner = participant;
			}
		}
		return winner;
		//this.RANDOM.nextInt(TOURNAMENT_SIZE)
	}

	/**
	 * cross over the two given chromesomes
	 *
	 * @param pair1
	 * @param pair2
	 */
	public void Crossover(int[][] pair1, int[][] pair2) {
		if (!VALID) {
			return;
		}
		int start = this.RANDOM.nextInt(CHROMESOME_SIZE);
		int end = this.RANDOM.nextInt(CHROMESOME_SIZE - start) + start;
		for (int i = 0; i < pair1.length; i++) {
			if (i >= start && i <= end) {
				int tempRoot = pair1[i][0];
				pair1[i][0] = pair2[i][0];
				pair2[i][0] = tempRoot;
				int tempOffset = pair1[i][1];
				pair1[i][1] = pair2[i][1];
				pair2[i][1] = tempOffset;
			}
		}
	}

	/*
		This method can only be done from the original graph, not constantly changing ones such as during eval
	 */
	private void MutateGene(int[] gene) {
		int randomRoot = this.RANDOM.nextInt(this.GRAPH_SIZE);
		gene[0] = randomRoot;
		if (this.ORIGINAL_NEIGHBORHOODS.get(randomRoot).size() < 1) {
			this.ORIGINAL_NEIGHBORHOODS.get(randomRoot).addAll(this.ORIGINAL_GRAPH.bfs(randomRoot, this.DISTANCE_LIMIT));
		}
		int randomNeighbor = this.ORIGINAL_NEIGHBORHOODS.get(randomRoot).get(this.RANDOM.nextInt(this.ORIGINAL_NEIGHBORHOODS.get(randomRoot).size()));
		int randomOffset = Math.floorMod(randomNeighbor - randomRoot, this.GRAPH_SIZE);
		gene[1] = randomOffset;
	}

	/**
	 * mutate the given chromesome
	 *
	 * @param pairs
	 */
	public void Mutate(int[][] pairs) {
		if (!VALID) {
			return;
		}
		int randomIndex = this.RANDOM.nextInt(pairs.length);
		this.MutateGene(pairs[randomIndex]);
	}

	/**
	 * Evaluates a single gene within a chromesome
	 *
	 * @param graph
	 * @param gene
	 * @return geneFitness
	 */
	public int EvaluateGene(LinkedGraph graph, int[] gene) {
		int from = gene[0];
		int to = (gene[0] + gene[1]) % this.GRAPH_SIZE;
		int fakelinks = graph.fakeLinks(from, to);
		// fakesLinks will return -1 if the two nodes are the same
		if (fakelinks < 0) {
			// in that case, update the possible neighbors
			List<Integer> possibleNeighbors = graph.bfs(from, this.DISTANCE_LIMIT);
			// try the next neighbor
			for (Integer neighbor : possibleNeighbors) {
				to = neighbor;
				int offset = Math.floorMod(neighbor - from, this.GRAPH_SIZE);
				gene[1] = offset;
				fakelinks = graph.fakeLinks(from, to);
				// do this until a valid neighbor is found
				if (fakelinks >= 0) {
					break;
				}
			}
			// if no valid neighbors could be found, mutate.
			while (fakelinks < 0) {
				from = this.RANDOM.nextInt(this.GRAPH_SIZE);
				possibleNeighbors = graph.bfs(from, this.DISTANCE_LIMIT);
				if (possibleNeighbors.size() < 1) {
					continue;
				}
				to = possibleNeighbors.get(this.RANDOM.nextInt(possibleNeighbors.size()));
				int randomOffset = Math.floorMod(to - from, this.GRAPH_SIZE);
				gene[0] = from;
				gene[1] = randomOffset;
				fakelinks = graph.fakeLinks(from, to);
			}
		}
		if (fakelinks < 0) {
			System.out.println("from: " + from);
			System.out.println("to: " + to);
			System.out.println("fakeLinks: " + fakelinks);
		}
		graph.merge(from, to);
		return fakelinks;
	}

	/**
	 * fitness function. updates chromesome if chromesome has some invalid
	 * genes.
	 *
	 * @param chromesome
	 * @return
	 */
	public int Evaluate(int[][] chromesome) {
		Graph current = this.ORIGINAL_GRAPH.deepCopy();
		// iterate through each gene
		int fitness = 0;
		int merged = 0;
		for (int i = 0; i < chromesome.length; i++) {
			fitness += this.EvaluateGene((LinkedGraph) current, chromesome[i]);
		}
		return fitness;
	}

	/**
	 * returns the fitness from the previous generation
	 *
	 * @param chromesome
	 * @return
	 */
	public int EvaluatePrevious(int chromesome) {
		// wrapper function primarily for sanity
		return this.POPULATION_FITNESS[chromesome];
	}

	/**
	 * returns a preset number of elites from the previous generation
	 *
	 * @return newPopulation
	 */
	public int[][][] getElitePopulation() {
		int[][][] newPop = new int[this.POPULATION_SIZE][this.CHROMESOME_SIZE][2];
		PriorityQueue<WrappedNode> fitness = new PriorityQueue<WrappedNode>();
		for (int i = 0; i < this.POPULATION_SIZE; i++) {
			fitness.add(new WrappedNode(i, EvaluatePrevious(i)));
		}
		for (int e = 0; e < this.ELITE_COUNT; e++) {
			//System.out.println(fitness.size());
			WrappedNode elite = fitness.remove();
			newPop[e] = this.getChromesome(elite.index);
		}
		return newPop;
	}

	// DEFAULTS AND DATA VALIDATION
	/*
	Apply default values to the parameters
	 */
	private void buildDefaults() {
		this.OUTPUT_FILENAME = DEFAULT_OUTPUT;
		this.SOURCE_FILENAME = DEFAULT_OUTPUT;
		this.GENERATION_SPAN = DEFAULT_SIZE;
		this.POPULATION_SIZE = DEFAULT_SIZE;
		this.TOURNAMENT_SIZE = DEFAULT_SIZE;
		this.CHROMESOME_SIZE = DEFAULT_SIZE;
		this.DISTANCE_LIMIT = DEFAULT_SIZE;
		this.ELITE_COUNT = DEFAULT_SIZE;
		this.GRAPH_SIZE = DEFAULT_SIZE;
		this.COMPRESSION_RATE = DEFAULT_RATE;
		this.CROSSOVER_RATE = DEFAULT_RATE;
		this.MUTATION_RATE = DEFAULT_RATE;
		this.ELITISM_RATE = DEFAULT_RATE;
		this.RUN_SPAN = 1;
	}

	// validates if properly built
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
		if (this.COMPRESSION_RATE > 1.0 || this.COMPRESSION_RATE < 0.0) {
			System.out.println("Compression rate invalid"
					+ ", use parameter: compression [0.0,1.0]");
			if (this.CHROMESOME_SIZE < 1) {
				this.VALID = false;
			}
		} else {
			this.POPULATION_SIZE = (int) (this.COMPRESSION_RATE * this.GRAPH_SIZE);
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
		if (this.CHROMESOME_SIZE < 1) {
			System.out.println("Chromesome size invalid"
					+ ", use parameter: chromesome [1,infinity)");
			if (this.COMPRESSION_RATE > 1.0 || this.COMPRESSION_RATE < 0.0) {
				this.VALID = false;
			}
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
		if (this.ELITISM_RATE > 1.0 || this.ELITISM_RATE < 0.0) {
			System.out.println("Elitism rate invalid"
					+ ", use parameter: elitism [0.0,1.0]");
			this.VALID = false;
		} else {
			this.ELITE_COUNT = (int) (this.ELITISM_RATE * this.POPULATION_SIZE);
			if (this.ELITE_COUNT < 1) {
				this.ELITE_COUNT = 1;
			}
			if (this.ELITE_COUNT > this.POPULATION_SIZE) {
				// all of them being elites is dumb.
				this.ELITE_COUNT = this.POPULATION_SIZE - 1;
			}
		}
		if (this.RUN_SPAN < 0) {
			System.out.println("Run size invalid"
					+ ", use parameter: runs [1,infinity]");
			this.VALID = false;
		}
		return this.VALID;
	}

	/*
		retrieves data for the GA from the given data file
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
					case "chromesome":
						this.CHROMESOME_SIZE = Integer.parseInt(data[1].trim());
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
	 * returns string representation of chromesome
	 *
	 * @param chromesome
	 * @return
	 */
	public static String buildChromesomeString(int[][] chromesome) {
		String output = "[";
		for (int i = 0; i < chromesome.length; i++) {
			if (i > 0) {
				output += ",";
			}
			output += "(";
			for (int j = 0; j < chromesome[i].length; j++) {
				if (j > 0) {
					output += ",";
				}
				output += chromesome[i][j];
			}
			output += ")";
		}
		output += "]";
		return output;
	}

	/**
	 * shows step by step fitness evaluation of chromesome
	 *
	 * @param testGraph
	 * @param chromesome
	 */
	public static void ViewChromesome(Graph testGraph, String chromesome) {
		LinkedGraph graph = (LinkedGraph) testGraph.deepCopy();
		chromesome = chromesome.replaceAll("\\]", "");
		chromesome = chromesome.replaceAll("\\[", "");
		String[] chromesomes = chromesome.split("\\),\\(");
		int sum = 0;
		
		for (String c : chromesomes) {
			c = c.replaceAll("\\(", "");
			c = c.replaceAll("\\)", "");
			String[] gene = c.split(",");
			int from = Integer.valueOf(gene[0]);
			int to = (Integer.valueOf(gene[1]) + from) % graph.getSize();
			int fakeLinks = graph.fakeLinks(from, to);
			System.out.println("Fake Links [" + from + "," + to + "]: " + fakeLinks);
			graph.merge(from, to);
			sum += fakeLinks;
		}

		System.out.println("Should be " + sum + " fitness");
		PrintChromesome(graph);

	}

	public static void PrintChromesome(LinkedGraph g) {
		g.print();
		GraphDisplay.displayLinkedGraph(g);
//		
	}
}
