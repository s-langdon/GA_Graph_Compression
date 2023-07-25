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
		long SEED = 107651916186943L;
		// load the args -- any not specified in via command-line will be taken from what is set-above.
		// a somewhat ridiculous method of parsing the command-line arguments
		// arguments should be passed in the following order: seed firstTest lastTest filenames
		if (args.length > 3) {
			TESTS = new String[args.length - 3];
		}
		for (int i = 0; i < args.length; i++) {
			if (i == 0) {
				SEED = Long.parseLong(args[i]);
			}
			if (i == 1) {
				FIRST_TEST = Integer.parseInt(args[i]);
			}
			if (i == 2) {
				LAST_TEST = Integer.parseInt(args[i]);
			}
			// parse in everything else as a test filename
			if(i > 2) {
				TESTS[i-3] = args[i];
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
				threadData.add(new GAImplementation(SEED, filename));
			}
		}

		Mediator shared = new Mediator(threadData);
		for (int i = 0; i < MAX_THREADS; i++) {
			(new GAThread(shared)).start();
		}
	}

}
