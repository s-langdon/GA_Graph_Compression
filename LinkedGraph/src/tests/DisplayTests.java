/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import ga.GAImplementation;
import linkedgraph.LinkedGraph;

/**
 *
 * @author Angelo Romualdo <angelo.romualdo at brocku.ca>
 */
public class DisplayTests {

    public static void main(String args[]) {
        LinkedGraph ecoli = LinkedGraph.load("./ecoli.txt");
        LinkedGraph figeys = LinkedGraph.load("./figeys.txt");
        LinkedGraph yeast = LinkedGraph.load("./yeast.txt");
        LinkedGraph test = LinkedGraph.load("./test.txt");
        LinkedGraph sample1 = LinkedGraph.load("./sample1.txt");
        LinkedGraph sample2 = LinkedGraph.load("./sample2.txt");

        String sample1_0 = "[0,0]";
        String sample1_1 = "[(1,1),(4,1)]"; // 5 fake links

        String sample2_0 = "[0,0]";
        String sample2_1 = "[(1,1)]"; // 0
        String sample2_2 = "[(1,1),(2,1)]"; // 0
        String sample2_3 = "[(1,1),(2,1),(3,1)]"; //0
        String sample2_4 = "[(0,1)]"; // 3
        String sample2_5 = "[(1,1),(2,1),(3,1),(]"; //6

        GAImplementation.ViewChromesome(sample2, sample2_4);

    }
}
