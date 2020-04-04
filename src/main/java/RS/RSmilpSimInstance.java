package RS;

import umontreal.ssj.stat.Tally;

public class RSmilpSimInstance {

	public double[] demandMean;
	public double	stdParameter;
	public double 	fixedCost;
	public double 	unitCost;
	public double 	holdingCost; 
	public double 	penaltyCost; 
	public double 	initialStock; 
	public double[] uptoLevel; 
	public double[] purchase;
	public Tally 	statCost = new Tally("stats on cost");
	
	public RSmilpSimInstance(	
			double[] demandMean, 
			double	stdParameter,
			double fixedCost,
			double unitCost,
			double holdingCost, 
			double penaltyCost, 
			double initialStock, 
			double[] uptoLevel, 
			double[] purchase) {
		this.demandMean = demandMean;
		this.stdParameter = stdParameter;
		this.fixedCost = fixedCost;
		this.unitCost = unitCost;
		this.holdingCost = holdingCost;
		this.penaltyCost = penaltyCost;
		this.initialStock = initialStock;
		this.uptoLevel = uptoLevel;
		this.purchase = purchase;
	}

}
