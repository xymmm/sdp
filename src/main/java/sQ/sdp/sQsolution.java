package sQ.sdp;

import sdp.data.Instance;

public class sQsolution {

	public int[] inventory;
	public double totalCost[][][];
	public boolean optimalAction[][][];
	public long timeConsumedsQ;

	public sQsolution(double[][][] totalCost, 
			boolean[][][] optimalAction, 
			int[] inventory,
			long timeConsumedsQ) {
		this.totalCost = totalCost;
		this.optimalAction = optimalAction;
		this.inventory = inventory;
		this.timeConsumedsQ = timeConsumedsQ;
	}

	
	public int getOpt_a(Instance instance) {
		// Determine the optimal a. What is the optimal a?
		int a = 0;
		int minIndex = a;
		double minCost = totalCost[minIndex][instance.initialInventory - instance.minInventory][0]; //Time zero
		do {
			if(minCost > totalCost[a+1][instance.initialInventory - instance.minInventory][0]) {
				minCost = totalCost[a+1][instance.initialInventory - instance.minInventory][0];
				minIndex = a+1;
			}
			a = a + 1;
		}while(a < instance.maxQuantity - 1);
		int opt_a = minIndex;
		return opt_a;
	}
		
}
