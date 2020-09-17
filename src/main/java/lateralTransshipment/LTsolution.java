package lateralTransshipment;

public class LTsolution {
	
	public int[][] inventoryPairs;
	public int[][][] optimalAction;
	public double[][] optimalCost;
	
	public LTsolution(
			int[][] inventoryPairs,
			int[][][] optimalAction,
			double[][] optimalCost) {
		inventoryPairs = this.inventoryPairs;
		optimalAction = this.optimalAction;
		optimalCost = this.optimalCost;
		
	}

}
