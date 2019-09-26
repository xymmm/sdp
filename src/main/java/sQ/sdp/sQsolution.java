package sQ.sdp;

import sdp.data.Instance;

public class sQsolution {

	public int[] inventory;
	public double totalCost[][][];
	public boolean optimalAction[][][];

	public sQsolution(double[][][] totalCost, 
			boolean[][][] optimalAction, 
			int[] inventory) {
		this.totalCost = totalCost;
		this.optimalAction = optimalAction;
		this.inventory = inventory;
	}

	
	public int getOpt_aSQ(Instance instance) {
		// Determine the optimal a. What is the optimal a?
		int a = 1;
		int minIndex = a;
		double minCost = totalCost[instance.initialInventory - instance.minInventory][minIndex][0]; //Time zero
		do {
			if(minCost > totalCost[instance.initialInventory - instance.minInventory][a+1][0]) {
				minCost = totalCost[instance.initialInventory - instance.minInventory][a+1][0];
				minIndex = a+1;
			}
			a = a + 1;
		}while(a < instance.maxQuantity - 1);
		int opt_a = minIndex;
		return opt_a-1;
	}
	

	public int[] getsSQ(Instance instance, sQsolution sQsolution) {
		int[] s = new int[instance.getStages()];
		// Get the reorder points.
		for(int t=0;t<instance.getStages();t++) { // Time
			for(int i=0;i<inventory.length;i++) {  // Inventory   
				if(sQsolution.optimalAction[i][sQsolution.getOpt_aSQ(instance)][t] == false) {
					s[t] = i + instance.minInventory;
					break;
				}
			}
		}
		return s;
	}
	
	
}
