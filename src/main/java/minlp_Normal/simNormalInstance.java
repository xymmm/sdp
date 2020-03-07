package minlp_Normal;

import umontreal.ssj.stat.Tally;

public class simNormalInstance {
	
	public double[] demandMean; 
	public double fixedCost;
	public double unitCost;
	public double holdingCost; 
	public double penaltyCost; 
	public double initialStock; 
	public double stdParameter; 
	public double[] schedule; 
	public double[] reorderPoints;
	public Tally statCost = new Tally("stats on cost");
	
	public simNormalInstance(
			double[] demandMean, 
			double fixedCost,
			double unitCost,
			double holdingCost, 
			double penaltyCost, 
			double initialStock, 
			double stdParameter, 
			double[] schedule, 
			double[] reorderPoints
			) {
		this.demandMean = demandMean;
		this.fixedCost = fixedCost;
		this.unitCost = unitCost;
		this.holdingCost = holdingCost;
		this.penaltyCost = penaltyCost;
		this.initialStock = initialStock;
		this.stdParameter = stdParameter;
		this.schedule = schedule;
		this.reorderPoints = reorderPoints;
	}
}
