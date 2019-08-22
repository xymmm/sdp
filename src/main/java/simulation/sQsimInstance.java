package simulation;

public class sQsimInstance {
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

	/** sQ replenishment quantity **/
	public int[] actionQuantity;
	public int[] reorderPoint;

	public sQsimInstance(
			double fixedOrderingCost,
			double unitCost,
			double holdingCost,
			double penaltyCost,
			int[] demandMean,
			double tail,
			int minInventory,
			int maxInventory,
			int[] actionQuantity,
			int[] reorderPoint) {
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
	}

	public int getStages() {
		return this.demandMean.length;
	}

	public int getDemandMean(int currentStageIndex) {
		return this.demandMean[currentStageIndex];
	}

	public int getActionQuantity(int currentStageIndex) {
		return this.actionQuantity[currentStageIndex];
	}
	
	public int getInitialInventory() {
		return this.initialInventory;
	}
}
