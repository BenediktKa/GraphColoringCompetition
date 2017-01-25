package graphcoloring;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

public class GraphReader {
	
	public static final boolean DEBUG = true;
	
	public static int nodes, edges;
	public static int lb, ub;
	public static int adjacencyMatrix[][];
	public static double pageRankOrder[][];
	public static boolean mentionedAnswers[];
	public static int chromaticNumber = -1;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		File file = new File(args[0]);

		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

		String line;

		while ((line = bufferedReader.readLine()) != null) {
			if(line.startsWith("//")) {
				continue;
			} else if(line.startsWith("VERTICES = ")) {
				nodes = Integer.parseInt(line.substring(11));
				adjacencyMatrix = new int[nodes][nodes];
				pageRankOrder = new double[nodes][nodes];
				continue;
			} else if(line.startsWith("EDGES = ")) {
				edges = Integer.parseInt(line.substring(8));
				continue;
			} else {
				
				String nodes[] = line.split("\\s");
				int node1 = Integer.parseInt(nodes[0]) - 1;
				int node2 = Integer.parseInt(nodes[1]) - 1;
				
				adjacencyMatrix[node1][node2] = 1;
				adjacencyMatrix[node2][node1] = 1;
			}
		}
		bufferedReader.close();
		
		//DSAT
		DSATUR dsatur = new DSATUR(nodes, edges, adjacencyMatrix);
		ub = dsatur.getUpperBound();
		System.out.println("NEW BEST UPPER BOUND = " + ub);
		
		pageRankOrder = dsatur.getPageRankOrder();
		
		//BRON KERBOSCH
		BronKerbosch bronKerbosch = new BronKerbosch(nodes, adjacencyMatrix);
		lb = bronKerbosch.getLowerBound();
		System.out.println("NEW BEST LOWER BOUND = " + lb);
		
		if(lb == ub) {
			System.out.println("CHROMATIC NUMBER = " + ub);
			System.exit(0);
		}
		
		
		mentionedAnswers = new boolean[ub + 1];
		for(int i = (int)ub - 1; i >= lb; i--) {
			int count = 0;
			int vertexColor[] = new int[nodes];
			colorGraph((int)pageRankOrder[0][0], i, vertexColor, count);
		}
	}
	
	/**
	 * @param vertex
	 * @param colorLimit
	 * @param vertexColor
	 */
	public static void colorGraph(int vertex, int colorLimit, int vertexColor[], int count) {
		for(int color = 1; color <= colorLimit; color++) {
			
			if(mentionedAnswers[colorLimit] || (chromaticNumber <= colorLimit && chromaticNumber != -1)) {
				break;
			}
			
			if(canColor(vertex, color, vertexColor)) {
				vertexColor[vertex] = color;
				int notColored = -1;
				
				if(++count < nodes) {
					colorGraph((int)pageRankOrder[count][0], colorLimit, vertexColor, count);
				} else if((notColored = allColored(vertexColor)) != -1) {
					colorGraph((int)pageRankOrder[notColored][0], colorLimit, vertexColor, notColored);
				} else {
					printResult(distinctNumberOfItems(vertexColor), vertexColor);
				}
			}
		}
	}
	
	/**
	 * @param vertex
	 * @param colors
	 * @param vertexColor
	 * @return
	 */
	public static boolean canColor(int vertex, int color, int vertexColor[]) {
		for(int vertexToCheck = 0; vertexToCheck < adjacencyMatrix.length; vertexToCheck++) {
			if(adjacencyMatrix[vertex][vertexToCheck] == 1 && vertexColor[vertexToCheck] == color) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * @param distinctElements
	 * @param vertexColor
	 */
	public static void printResult(int distinctElements, int vertexColor[]) {
		
		if(distinctElements < chromaticNumber || chromaticNumber == -1) {
			chromaticNumber = distinctElements;
		}
		mentionedAnswers[distinctElements] = true;
		System.out.println("NEW BEST UPPER BOUND = " + distinctElements);
		
		for(int i = 0; i < vertexColor.length; i++) {
			System.out.print(vertexColor[i] + ", ");
		}
		System.out.println("");
		sanityCheck(vertexColor);
	}
	
	public static int allColored(int vertexColor[]) {
		for(int i = 0; i < nodes; i++) {
			if(vertexColor[i] == 0) {
				for(int j = 0; j < nodes; j++) {
					if(i == pageRankOrder[j][0]) {
						return j;
					}
				}
			}
		}
		return -1;
	}
	
	/**
	 * @param array
	 * @return
	 */
	public static int distinctNumberOfItems(int[] array) {
		if (array.length <= 1) {
			return array.length;
		}

		Set<Integer> set = new HashSet<Integer>();
		for (int i : array)
			if(i != 0)
				set.add(i);

		return set.size();
	}
	
	public static void sanityCheck(int vertexColor[]) {	
		for(int i = 0; i < adjacencyMatrix.length; i++) {
			for(int j = 0; j < adjacencyMatrix[0].length; j++) {
				if(adjacencyMatrix[i][j] == 1) {
					if(vertexColor[i] == vertexColor[j]) {
						System.out.println("Invalid coloring! Nodes " + i + " and " + j);
						System.out.println("Colors " + vertexColor[i] + " and " + vertexColor[j]);
					}
				}
			}
		}
		System.out.println("End of sanity check");
	}
}
