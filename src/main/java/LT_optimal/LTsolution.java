package LT_optimal;

public class LTsolution {
	
	public int[] inventory;
	public int[][][] LTaction;
	public int[][][] Q1;
	public int[][][] Q2;
	public double[][][] optimalCost;
	
	
	public LTsolution(int[] inventory, int[][][] LTaction, int[][][] Q1, int[][][] Q2, double[][][] optimalCost) {
		this.inventory = inventory;
		this.LTaction = LTaction;
		this.Q1 = Q1; 
		this.Q2 = Q2;
		this.optimalCost = optimalCost;
	}
	


}
