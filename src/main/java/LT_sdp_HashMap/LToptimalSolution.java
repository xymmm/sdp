package LT_sdp_HashMap;

import java.util.List;

public class LToptimalSolution {
	
	public double[][] optimalCost;
	public int[] optimalTransshipment;
	public int[] optimalQA;
	public int[] optimalQB;
	
	public LToptimalSolution(double[][] optimalCost, int[] optimalTransshipment, int[] optimalQA, int[] optimalQB) {
		this.optimalCost = optimalCost;
		this.optimalTransshipment = optimalTransshipment;
		this.optimalQA = optimalQA;
		this.optimalQB = optimalQB;
	}
}
