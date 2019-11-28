package sQ.simulation;

import umontreal.ssj.stat.Tally;

public class sQsimInstanceDouble {
	
	
	/*** Problem instance ***/
	public double fixedOrderingCost;
	public double unitCost;
	public double holdingCost;
	public double penaltyCost;
	
	public int[] demandMean;
	public double initialInventory = 0;

	/** SDP boundary conditions **/
	public double tail;

	public double maxDemand;
	public double minInventory;
	public double maxInventory;

	/** sQ replenishment quantity **/
	public double[] actionQuantity;
	public double[] reorderPoint;
	
	public double coe;
	
	public Tally statCost = new Tally("stats on cost");

	public sQsimInstanceDouble(
			double fixedOrderingCost,
			double unitCost,
			double holdingCost,
			double penaltyCost,
			int[] demandMean,
			double tail,
			double minInventory,
			double maxInventory,
			double[] actionQuantity,
			double[] reorderPoint,
			double coe) {
		this.fixedOrderingCost = fixedOrderingCost;
		this.unitCost = unitCost;
		this.holdingCost = holdingCost;
		this.penaltyCost = penaltyCost;
		this.demandMean = demandMean;
		this.tail = tail;
		this.minInventory = minInventory;
		this.maxInventory = maxInventory;
		this.actionQuantity = actionQuantity;
		this.reorderPoint = reorderPoint;
		this.coe = coe;
	}

	public int getStages() {
		return this.demandMean.length;
	}

	public double getActionQuantity(int currentStageIndex) {
		return this.actionQuantity[currentStageIndex];
	}
	
	public double getInitialInventory() {
		return this.initialInventory;
	}


}
