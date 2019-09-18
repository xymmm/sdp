package sdp.data;

public class NormalInstance {

	/*** Problem instance ***/
	public double fixedOrderingCost;
	public double unitCost;
	public double holdingCost;
	public double penaltyCost;
	public int[] demandMean;
	public double stdPara;
	public int initialInventory = 0;

	/** SDP boundary conditions **/
	public double tail;

	public int maxDemand;
	public int minInventory;
	public int maxInventory;
	public int maxQuantity;

	public NormalInstance(
			double fixedOrderingCost,
			double unitCost,
			double holdingCost,
			double penaltyCost,
			int[] demandMean,
			double stdPara,
			double tail,
			int minInventory,
			int maxInventory,
			int maxQuantity) {
		this.fixedOrderingCost = fixedOrderingCost;
		this.unitCost = unitCost;
		this.holdingCost = holdingCost;
		this.penaltyCost = penaltyCost;
		this.demandMean = demandMean;
		this.stdPara = stdPara;
		this.tail = tail;
		this.minInventory = minInventory;
		this.maxInventory = maxInventory;
		this.maxQuantity = maxQuantity;
	}

	public int getStages() {
		return this.demandMean.length;
	}

}
