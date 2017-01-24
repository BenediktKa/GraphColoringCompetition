package graphcoloring;

import java.util.ArrayList;
import java.util.List;

public class BronKerbosch {

	private int lb, nodes, counter;
	private int[][] adjacencyMatrix;
	
	/**
	 * @param intiVerticies
	 * @param initAdjacencyMatrix
	 */
	public BronKerbosch(int intiVerticies, int initAdjacencyMatrix[][]) {
		nodes = intiVerticies;
		adjacencyMatrix = initAdjacencyMatrix;
		
		List<Integer> P = new ArrayList<Integer>();
		
		for(int i = 0; i < nodes; i++) {
			P.add(i);
		}
		
		try {
			bronKerbosch(P, new ArrayList<Integer>(), new ArrayList<Integer>());
		} catch(Exception e) {}
	}
	
	/**
	 * @param P
	 * @param R
	 * @param X
	 * @throws Exception
	 */
	public void bronKerbosch(List<Integer> P, List<Integer> R, List<Integer> X) throws Exception {
		counter++;
		if (counter == 100000) {
			throw new Exception();
		}
		if (P.size() == 0 && X.size() == 0) {
			if (R.size() > lb) {
				lb = R.size();
			}
		}
		
		while (P.size() > 0) {
			bronKerbosch(intersection(P, P.get(0)), union(R, P.get(0)), intersection(X, P.get(0)));
			
			X.add(P.get(0));
			P.remove(0);	
		}
	}

	/**
	 * @param P
	 * @param v
	 * @return
	 */
	public List<Integer> intersection(List<Integer> P, int v) {
		List<Integer> newP = new ArrayList<Integer>();
		
		for (int i = 0; i < nodes; i++) {
			if(adjacencyMatrix[v][i] == 1 && P.contains(i)) {
				newP.add(i);
			}
		}
		return newP;
	}
	
	/**
	 * @param R
	 * @param v
	 * @return
	 */
	public List<Integer> union(List<Integer> R, int v) {
		List<Integer> newR = new ArrayList<Integer>(R);
		newR.add(v);
		
		
		return newR;
	}
	
	/**
	 * @return
	 */
	public int getLowerBound() {
		return lb;
	}
}