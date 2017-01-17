package graphcoloring;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

public class GraphReader {
	
	public static final boolean DEBUG = true;
	
	public static int nodes, edges;
	public static int adjacencyMatrix[][];
	//public static int vertexColor[];
	public static boolean mentionedAnswers[];
	public static int chromaticNumber = -1;

	public static void main(String[] args) throws Exception {
		File file = new File("graphs/block3_graph06.txt");

		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

		String line;

		while ((line = bufferedReader.readLine()) != null) {
			if(line.startsWith("//")) {
				continue;
			} else if(line.startsWith("VERTICES = ")) {
				nodes = Integer.parseInt(line.substring(11));
				if(DEBUG) System.out.println(nodes + " Nodes detected");
				adjacencyMatrix = new int[nodes][nodes];
				continue;
			} else if(line.startsWith("EDGES = ")) {
				edges = Integer.parseInt(line.substring(8));
				if(DEBUG) System.out.println(edges + " Edges detected");
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
		
		long dSatlStartTime = System.nanoTime();
		
		DSATURfinished dsatur = new DSATURfinished(nodes, edges, adjacencyMatrix);
		
		double dSatEndTime = System.nanoTime();
		
		double lDuration = (dSatEndTime - dSatlStartTime) / 1000000000;
		System.out.println("End runtime DSATUR " + lDuration + " Seconds.");
		
		int ub = dsatur.getUpperBound();
		
		long eigenlStartTime = System.nanoTime();
		Matrix A;
		
		if(adjacencyMatrix.length >= 1000) {
			A = new Matrix(1000, 1000);
			
			for(int i = 0; i < 1000; i++) {
				for(int j = 0; j < 1000; j++) {
					A.set(i, j, adjacencyMatrix[i][j]);
				}
			}
		} else {
			A = new Matrix(adjacencyMatrix.length, adjacencyMatrix[0].length);
		
			for(int i = 0; i < adjacencyMatrix.length; i++) {
				for(int j = 0; j < adjacencyMatrix[0].length; j++) {
					A.set(i, j, adjacencyMatrix[i][j]);
				}
			}
		}
		
		
		
		EigenvalueDecomposition eigen = A.eig();
		
		Matrix B = (Matrix) eigen.getD();
		
		int min = 0;
		int max = 0;
		
		for(int i = 0; i < B.getColumnDimension(); i++) {
			for(int j = 0; j < B.getRowDimension(); j++) {				
				if(min == 0) {
					min = (int)B.get(i, j);
				} else {
					if(A.get(i, j) < min) {
						min = (int)B.get(i, j);
					}
				}
				
				if(max == 0) {
					max = (int)B.get(i, j);
				} else {
					if(B.get(i, j) > max) {
						max = (int)B.get(i, j);
					}
				}
			}
		}
		
		/*for(int i = 0; i < adjacencyMatrix.length; i++) {
			for(int j = 0; j < adjacencyMatrix[0].length; j++) {
				System.out.print(B.get(i, j) + " ");
			}
			System.out.println("");
		}*/
		
		float lb = 1 - max / min; 
		System.out.println("Lower bound found by Eigenvalues " + lb);
		System.out.println("Upper bound found by DSATUR " + ub);
		
		double eigenlEndTime = System.nanoTime();

		lDuration = (eigenlEndTime - eigenlStartTime) / 1000000000;
		System.out.println("End runtime EigenValues " + lDuration + " Seconds.");
		
		//System.out.println("Attempting backtracking...");
		
		if(lb == ub) {
			System.out.println("Upper bound is the same as lower bound... Stopping program");
			System.exit(0);
		}
		mentionedAnswers = new boolean[ub + 1];
		for(int i = (int)ub - 1; i >= lb; i--) {
			
			int vertexColor[] = new int[nodes];
			System.out.println("Attempting backtracking with " + i + " colors");
			colorGraph(0, i, vertexColor);
		}
		
		System.out.println("End");
	}
	
	public static void colorGraph(int vertex, int colorLimit, int vertexColor[]) {
		for(int color = 1; color <= colorLimit; color++) {
			
			if(mentionedAnswers[color] || (chromaticNumber <= colorLimit && chromaticNumber != -1)) {
				break;
			}
			
			if(canColor(vertex, color, vertexColor)) {
				vertexColor[vertex] = color;
				
				/*for(int i = 0; i < vertexColor.length; i++) {
					System.out.print(vertexColor[i] + ", ");
				}
				System.out.println("");*/
				
				if(vertex + 1 < nodes) {
					colorGraph(vertex + 1, colorLimit, vertexColor);
				} else {
					printResult(distinctNumberOfItems(vertexColor), vertexColor);
				}
			}
		}
	}
	
	public static boolean canColor(int vertex, int color, int vertexColor[]) {
		for(int vertexToCheck = 0; vertexToCheck < adjacencyMatrix.length; vertexToCheck++) {
			if(adjacencyMatrix[vertex][vertexToCheck] == 1 && vertexColor[vertexToCheck] == color) {
				return false;
			}
		}
		return true;
	}
	
	public static void printResult(int distinctElements, int vertexColor[]) {
		
		if(distinctElements < chromaticNumber || chromaticNumber == -1) {
			chromaticNumber = distinctElements;
		}
		mentionedAnswers[distinctElements] = true;
		System.out.println("Found solution with " + distinctElements + " colors");
		
		for(int i = 0; i < vertexColor.length; i++) {
			System.out.print(vertexColor[i] + ", ");
		}
		System.out.println("");
		sanityCheck(vertexColor);
	}
	
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
