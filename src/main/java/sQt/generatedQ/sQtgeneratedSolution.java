package sQt.generatedQ;

public class sQtgeneratedSolution {
	
	public double[] inventory;
	public double[] minCost;
	public double[][] optQ;
	public double[][][] totalCost;
	public int[] optG;

	public sQtgeneratedSolution(
			double[] inventory,
			double[] minCost, 
			double[][] optQ,
			double[][][] totalCost,
			int[] optG
			) {
		this.inventory = inventory;
		this.minCost = minCost;
		this.optQ = optQ;
		this.totalCost = totalCost;
		this.optG = optG;
	}


}
