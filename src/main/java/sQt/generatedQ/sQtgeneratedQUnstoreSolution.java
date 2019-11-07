package sQt.generatedQ;

public class sQtgeneratedQUnstoreSolution {
	
	//public double[] inventory;
	public double minCost;
	public double[] optQ;
	//public double[][] totalCost;
	public int optG;
	public long timeConsumed_sQt;

	public sQtgeneratedQUnstoreSolution(
			double minCost, 
			double[] optQ,
			int optG,
			long timeConsumed_sQt
			) {
		this.minCost = minCost;
		this.optQ = optQ;
		this.optG = optG;
		this.timeConsumed_sQt = timeConsumed_sQt;
	}

}
