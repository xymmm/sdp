package sQ;

import sdp.data.Instance;

public class sQtsolution {

	public int[] inventory;
	public double totalCost[][][];
	public boolean optimalAction[][][];
	
	public sQtsolution(double[][][] totalCost, 
			boolean[][][] optimalAction, 
			int[] inventory) {
		this.totalCost = totalCost;
		this.optimalAction = optimalAction;
		this.inventory = inventory;
	}
	
	public int[] getQt(Instance instance) {
		int[] Qt = new int[instance.getStages()];
		for(int t=0; t<instance.getStages(); t++) {
			Qt[t] = getMinimumIndex(totalCost[t][instance.initialInventory - instance.minInventory]);
		}
		return Qt;
	}
	
	public int[] getssQt(Instance instance, sQtsolution sQtsolution) {
		int[] st = new int[instance.getStages()];
			// Get the reorder points.
			for(int t=0;t<instance.getStages();t++) { // Time
				for(int i=0;i<inventory.length-1;i++) {  // Inventory   
					if(sQtsolution.optimalAction[t][i+1][sQtsolution.getQt(instance)[t]] == false) {
						st[t] = i+1 + instance.minInventory;
						break;
					}
				}
			}
		return st;
	}
	
	private int getMinimumIndex(double[] arr) {
		int index = 0;
		double min = arr[0];
		for(int i=0; i<arr.length-1;i++) {
			if(arr[i+1]<min) {
				min = arr[i+1];
				index = i+1;
			}
		}
		return index;
	}
	
}
