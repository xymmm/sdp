package reorderQuantitySystem;

import umontreal.ssj.stat.Tally;

public class simInstance {

	/*** Problem instance ***/
	public double fixedOrderingCost;
	public double unitCost;
	public double holdingCost;
	public double penaltyCost;
	
	public int[] demandMean;
	public int initialInventory = 0;

	/** SDP boundary conditions **/
	public double tail;

	public int minInventory;
	public int maxInventory;
	
	public double coe;
	
	public int[] reorderPoint;
	public int[] orderQuantity;
	
	public Tally statCost = new Tally("stats on cost");

	public simInstance(
			double fixedOrderingCost,
			double unitCost,
			double holdingCost,
			double penaltyCost,
			int[] demandMean,
			double tail,
			int minInventory,
			int maxInventory,
			double coe,
			int[] reorderPoint,
			int[] orderQuantity) {
		this.fixedOrderingCost = fixedOrderingCost;
		this.unitCost = unitCost;
		this.holdingCost = holdingCost;
		this.penaltyCost = penaltyCost;
		this.demandMean = demandMean;
		this.tail = tail;
		this.minInventory = minInventory;
		this.maxInventory = maxInventory;
		this.coe = coe;
		this.reorderPoint = reorderPoint;
		this.orderQuantity = orderQuantity;
	}

	public int getStages() {
		return this.demandMean.length;
	}
	
	public double getInitialInventory() {
		return this.initialInventory;
	}

	
}
