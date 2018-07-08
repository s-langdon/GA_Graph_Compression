package display;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import linkedgraph.*;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSinkImages;
import org.graphstream.stream.file.FileSinkImages.LayoutPolicy;
import org.graphstream.stream.file.FileSinkImages.OutputType;
import org.graphstream.stream.file.FileSinkImages.Resolutions;

/**
 *
 * @author Angelo Romualdo <angelo.romualdo at brocku.ca>
 */
public class GraphDisplay {

	public static void displayLinkedGraph(LinkedGraph g) {
		Graph graph = new SingleGraph("LinkedGraph");

		graph.addAttribute("ui.quality");
		graph.addAttribute("ui.antialias");
		graph.addAttribute("ui.stylesheet",
				"node {"
					+ "size: 3px;"
					+ "fill-color: #777;"
					+ "z-index: 0;"
					+ "text-background-mode: rounded-box;"
					+ "text-padding: 5;"
					+ "text-background-color: white;"
				+ "}"
				+ "edge {"
					+ "shape: line;"
					+ "fill-color: #222;"
					+ "z-index: -1;"
					+ "arrow-size: 5px, 2px;"
					+ "size-mode:fit;"
				+ "}"
		);

		for (int i = 0; i < g.getSize(); i++) {
			if (g.get(i).getId() == i) {
				try {
					graph.addNode("" + i);
					org.graphstream.graph.Node node = graph.getNode("" + i);
					node.addAttribute("ui.label", "" + i);
				} catch (Exception e) {
				}
				for (Integer iNeighbor : g.bfs(i, 1)) {
					try {
						graph.addNode("" + iNeighbor);
						org.graphstream.graph.Node node = graph.getNode("" + iNeighbor);
						node.addAttribute("ui.label", "" + iNeighbor);
					} catch (Exception e) {
					}
					try {
						graph.addEdge("n" + i + "n" + iNeighbor, "" + i, "" + iNeighbor);
					} catch (Exception e) {
					}
				}
			}
		}
//		FileSinkImages pic = new FileSinkImages(OutputType.PNG, Resolutions.VGA);
//		pic.setLayoutPolicy(LayoutPolicy.COMPUTED_FULLY_AT_NEW_IMAGE);
//		try {
//			pic.writeAll(graph, "data/out/out.png");
//		} catch (Exception ex) {
//			System.out.println("Error writing to image: " + ex.getMessage());
//		}
		graph.display();
	}
}
