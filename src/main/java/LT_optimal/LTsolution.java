package LT_optimal;

public class LTsolution {
	
	public int[][] inventory;
	public double[][][] LTaction;
	public double[][][] REaction;
	public double[][][] optimalCost;
	
	
	public LTsolution(int[][] inventory, double[][][] LTaction, double[][][] REaction, double[][][] optimalCost) {
		this.inventory = inventory;
		this.LTaction = LTaction;
		this.REaction = REaction;
		this.optimalCost = optimalCost;
	}

}
