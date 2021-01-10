package LateralTransshipment_slow;

public class LTsolution {
	
	public int[][] inventoryPairs;
	public int[][][] optimalAction;
	public double[][] optimalCost;
	
	public LTsolution(
			int[][] inventoryPairs,
			int[][][] optimalAction,
			double[][] optimalCost) {
		this.inventoryPairs = inventoryPairs;
		this.optimalAction = optimalAction;
		this.optimalCost = optimalCost;
		
	}

}
