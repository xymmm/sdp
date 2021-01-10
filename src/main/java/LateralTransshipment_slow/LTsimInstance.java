package LateralTransshipment_slow;

import umontreal.ssj.stat.Tally;

public class LTsimInstance{

	public int[] demandMean1;
	public int[] demandMean2;
	public int maxInventory;
	public int minInventory;
	public double K;
	public double z;
	public double R;
	public double v;
	public double h;
	public double b;
	
	public int[][] inventoryPairs;
	public int[][][] optimalAction;
	public double[][] optimalCost;
	
	public int[] start = {1,5};
	
	public Tally statCost = new Tally("stats on cost");
	
	public LTsimInstance(	
			int[] demandMean1,
			int[] demandMean2,
			int maxInventory,
			int minInventory,
			double K,
			double z,
			double R,
			double v,
			double h,
			double b,
			int[][] inventoryPairs,
			int[][][] optimalAction,
			double[][] optimalCost) {
		this.demandMean1 = demandMean1;
		this.demandMean2 = demandMean2;
		this.maxInventory = maxInventory;
		this.minInventory = minInventory;
		this.K = K;
		this.z = z;
		this.R = R;
		this.v = v;
		this.h = h;
		this.b = b;
		this.inventoryPairs = inventoryPairs;
		this.optimalAction = optimalAction;
		this.optimalCost = optimalCost;		
	}
	
	public int[] getInitialState() {
		return start;
	}
	
}
