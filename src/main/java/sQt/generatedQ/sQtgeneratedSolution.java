package sQt.generatedQ;

public class sQtgeneratedSolution {
	
	public double[] inventory;
	public double[] minCost;
	public double[][] optQ;
	public double[][][] totalCost;
	public int[] optG;
	public long timeConsumed_sQt;

	public sQtgeneratedSolution(
			double[] inventory,
			double[] minCost, 
			double[][] optQ,
			double[][][] totalCost,
			int[] optG,
			long timeConsumed_sQt
			) {
		this.inventory = inventory;
		this.minCost = minCost;
		this.optQ = optQ;
		this.totalCost = totalCost;
		this.optG = optG;
		this.timeConsumed_sQt = timeConsumed_sQt;
	}


}
