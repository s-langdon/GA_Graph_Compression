/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threadedtests;

import ga.GAImplementation;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Angelo Romualdo <angelo.romualdo at brocku.ca>
 */
public class GARuns {

	public static final int MAX_THREADS = 1;
	public static int FIRST_TEST = 1;
	public static int LAST_TEST = 1;
	public static String[] TESTS = new String[]{
		"yeast?.dat"
	};
	
	
	public static void main(String... args) {
		boolean noisy = true;
		long SEED = 107651916186943L;
		// load the args -- any not specified in via command-line will be taken from what is set-above.
		// if using this method, at most one filename template can be set
		// (since this is used on the cluster, usually only 1 experimental setup is given anyway)
		int numArgs = args.length;
		// first arg is the seed
		if (numArgs >= 1) {
			try {
				SEED = Long.parseLong(args[0]);
			} catch (NumberFormatException e) {
				SEED = System.nanoTime();
				if (!args[0].toLowerCase().equals('r')) { //unintentionally set to random seed, display a note
					System.out.println("Invalid seed, system nano time used instead.");
				}
			}
		}
		//second is the first test number
		if (numArgs >= 2) {
			FIRST_TEST = Integer.parseInt(args[1]);
		}
		//third is the last test number
		if (numArgs >= 3) {
			LAST_TEST = Integer.parseInt(args[2]);
		}
		//fourth is the filename template for the experiment specification
		if(numArgs >= 4) {
			TESTS[0] = args[3];
		}
		//noisy console output flag
		if (numArgs >= 5) {
			if (args[4].toLowerCase().equals("quiet") || args[5].toLowerCase().equals("q") || args[5].toLowerCase().equals("false")) {
				noisy = false;
			}
		}

		// print out the settings
		System.out.println("Parameters: ");
		System.out.println("Seed: " + SEED);
		System.out.println("First test: " + FIRST_TEST);
		System.out.println("Last test: " + LAST_TEST);
		System.out.println("Input filenames: " + Arrays.toString(TESTS));


		GAThread[] threads = new GAThread[MAX_THREADS];
		ArrayList<GAImplementation> threadData = new ArrayList<>();
		for (int i = FIRST_TEST; i <= LAST_TEST; i ++) {
			for(int j = 0; j < TESTS.length; j ++){
				String filename = TESTS[j].replace("?", String.valueOf(i));
				System.out.println(filename);
				threadData.add(new GAImplementation(SEED, filename, noisy));
			}
		}

		Mediator shared = new Mediator(threadData);
		for (int i = 0; i < MAX_THREADS; i++) {
			(new GAThread(shared)).start();
		}
	}

}
