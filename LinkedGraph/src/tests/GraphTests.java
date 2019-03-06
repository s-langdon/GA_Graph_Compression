/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import ga.GAImplementation;
import java.util.Random;
import linkedgraph.*;

/**
 *
 * @author aromualdo
 */
public class GraphTests {

    /*
	input file 
	Generation : int
	population : int
	tourny select : < 
	crossover : < 1
	mutation : < 1
	chromesome length : int(500)
	
	ouput_prefix :String 
	
	main class
	new GA(System.nanotime(), args[0])
     */
    public static void main(String... args) {
        String[][] tests = new String[][]{
            /* 2 edges, 3 nodes */
            // edge to root
            {"test/test1.txt", "1", "[(0,1)]"},
            // root to edge
            {"test/test1.txt", "1", "[(0,1)]"},
            // edge to edge
            {"test/test1.txt", "1", "[(0,2)]"},
            // opposite edge to edge
            {"test/test1.txt", "1", "[(2,1)]"},
            /* star, continuously add edges */
            {"test/test2.txt", "1", "[(1,1)]"},
            {"test/test2.txt", "3", "[(1,1),(2,1)]"},
            {"test/test2.txt", "6", "[(1,1),(2,1),(3,1)]"},
            {"test/test2.txt", "6", "[(1,1),(2,1),(3,1),(4,1)]"},
            /* star, root to edges*/
            {"test/test2.txt", "3", "[(0,1)]"},
            {"test/test2.txt", "5", "[(0,1),(1,1)]"},
            {"test/test2.txt", "6", "[(0,1),(1,1),(2,1)]"},
            {"test/test2.txt", "6", "[(0,1),(1,1),(2,1),(3,1)]"},
            /* star, root to edge and edge to edge*/
            {"test/test2.txt", "4", "[(0,1),(2,1)]"},
            /* line with loops at the end*/
            // edge to root
            {"test/test3.txt", "1", "[(0,2)]"},
            {"test/test3.txt", "2", "[(0,2),(1,1)]"},
            // edge to edge
            {"test/test3.txt", "0", "[(0,1)]"},
            {"test/test3.txt", "0", "[(0,1),(7,1)]"},
            // merged edges to root
            {"test/test3.txt", "2", "[(0,1),(7,1),(1,1)]"},
            {"test/test3.txt", "4", "[(0,1),(7,1),(1,1),(6,1)]"},
            // line to root
            {"test/test3.txt", "3", "[(2,1)]"},
            {"test/test3.txt", "3", "[(2,1),(0,1)]"},
            {"test/test3.txt", "5", "[(2,1),(0,1),(1,1)]"},
            /* some kind of carbine looking graph*/
            {"test/test4.txt", "4", "[(0,1)]"},
            {"test/test4.txt", "5", "[(0,1),(2,1)]"},
            {"test/test4.txt", "2", "[(4,2)]"},
            {"test/test4.txt", "5", "[(0,6)]"},
            /* C, 4 nodes 3 edges */
            {"test/test5.txt", "1", "[(0,1)]"},
            {"test/test5.txt", "3", "[(0,1),(1,1)]"},
            {"test/test5.txt", "3", "[(0,1),(1,1),(2,1)]"},
            // edges first
            {"test/test5.txt", "3", "[(0,1),(2,1)]"},
            {"test/test5.txt", "3", "[(0,1),(2,1),(1,1)]"},
            // root first
            {"test/test5.txt", "2", "[(1,1)]"},
            {"test/test5.txt", "3", "[(1,1),(0,1)]"},
            {"test/test5.txt", "3", "[(1,1),(0,1),(2,1)]"},
            /* 6 node circle */
            {"test/test6.txt", "5", "[(0,3)]"},
            {"test/test6.txt", "8", "[(0,3),(2,3)]"},
            {"test/test6.txt", "9", "[(0,3),(2,3),(0,1)]"},
            {"test/test6.txt", "9", "[(0,3),(2,3),(0,1),(1,1)]"},
            // test that 5 (which contains 0,1,2,3) merged with 4 results in the same
            {"test/test6.txt", "9", "[(0,3),(2,3),(0,1),(1,1),(0,1)]"},
            {"test/test6.txt", "9", "[(0,3),(2,3),(0,1),(1,1),(1,1)]"},
            {"test/test6.txt", "9", "[(0,3),(2,3),(0,1),(1,1),(2,1)]"},
            {"test/test6.txt", "9", "[(0,3),(2,3),(0,1),(1,1),(3,1)]"},
            {"test/test6.txt", "9", "[(0,3),(2,3),(0,1),(1,1),(4,1)]"},
            {"test/test6.txt", "9", "[(0,3),(2,3),(0,1),(1,1),(5,1)]"}
        };
        for (int i = 0; i < tests.length; i++) {
            LinkedGraph graph = LinkedGraph.load(tests[i][0]);
            int expectedFakeLinks = Integer.valueOf(tests[i][1]);
            String chromesome = tests[i][2];
            LinkedGraph result = GAImplementation.BuildChromesome(graph, chromesome);

            int actualFakeLinks = result.totalFakeLinks();

            if (actualFakeLinks == expectedFakeLinks) {
                //System.out.println("Test "+i+" Passed!");
            } else {
                System.out.println("Test " + i + " FAILED!");
                System.out.println("GRAPH ~~~~~~~~~~~~~~~~~~~");
                result.print();
                System.out.println("FAKE LINKS ~~~~~~~~~~~~~~");
                result.printFakeLinks();
                System.out.println("~~~~~~~~~~~");
                System.out.println("Chromesome: " + chromesome);
                System.out.println("Expected: " + expectedFakeLinks);
                System.out.println("Actual: " + actualFakeLinks);
                System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~");
            }
        }
    }
}
