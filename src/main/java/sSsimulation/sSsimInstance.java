package sSsimulation;

import umontreal.ssj.stat.Tally;

public class sSsimInstance {
	/*** Problem instance ***/
	public double fixedOrderingCost;
	public double unitCost;
	public double holdingCost;
	public double penaltyCost;
	public int[] demandMean;
	public int initialInventory = 0;


	/** SDP boundary conditions **/
	public double tail;

	public int maxDemand;
	public int minInventory;
	public int maxInventory;

	/** sS replenishment quantity **/
	public int[] actionS;
	public int[] reorderPoint;
	
	public Tally statCost = new Tally("stats on cost");

	public sSsimInstance(
			double fixedOrderingCost,
			double unitCost,
			double holdingCost,
			double penaltyCost,
			int[] demandMean,
			int minInventory,
			int maxInventory,
			int[] actionS,
			int[] reorderPoint) {
		this.fixedOrderingCost = fixedOrderingCost;
		this.unitCost = unitCost;
		this.holdingCost = holdingCost;
		this.penaltyCost = penaltyCost;
		this.demandMean = demandMean;
		this.minInventory = minInventory;
		this.maxInventory = maxInventory;
		this.actionS = actionS;
		this.reorderPoint = reorderPoint;
	}

	public int getStages() {
		return this.demandMean.length;
	}

	public int getActionQuantity(int currentStageIndex, int inventoryLevel) {
		return this.actionS[currentStageIndex] - inventoryLevel;
	}
	
	public int getInitialInventory() {
		return this.initialInventory;
	}
}
