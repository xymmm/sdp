package LT_optimal;

public class LTsolution {
	
	public int[][] inventory;
	public double[][] LTaction;
	public double[][] Q1;
	public double[][] Q2;
	public double[][] optimalCost;
	
	
	public LTsolution(int[][] inventory, double[][] LTaction, double[][] Q1, double[][] Q2, double[][] optimalCost) {
		this.inventory = inventory;
		this.LTaction = LTaction;
		this.Q1 = Q1; 
		this.Q2 = Q2;
		this.optimalCost = optimalCost;
	}

}
